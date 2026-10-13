package com.zachduda.chatfeelings.feeling;

import com.zachduda.chatfeelings.api.FeelingGlobalNotifyEvent;
import com.zachduda.chatfeelings.api.FeelingRecieveEvent;
import com.zachduda.chatfeelings.api.FeelingSendEvent;
import com.zachduda.chatfeelings.command.Feedback;
import com.zachduda.chatfeelings.command.Permissions;
import com.zachduda.chatfeelings.config.Messages;
import com.zachduda.chatfeelings.config.Settings;
import com.zachduda.chatfeelings.effect.ParticleEffects;
import com.zachduda.chatfeelings.hook.NicknameResolver;
import com.zachduda.chatfeelings.hook.PunishmentHook;
import com.zachduda.chatfeelings.hook.Punishments;
import com.zachduda.chatfeelings.hook.VanishCheck;
import com.zachduda.chatfeelings.platform.Scheduler;
import com.zachduda.chatfeelings.storage.PlayerData;
import com.zachduda.chatfeelings.storage.PlayerDataService;
import com.zachduda.chatfeelings.util.Cooldowns;
import com.zachduda.chatfeelings.util.Log;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Sends a feeling. The work is split into steps that each run on the right thread, with no step waiting on
 * another:
 * <ol>
 *     <li>command thread: arguments, permission, cooldown, target lookup ({@link #send})</li>
 *     <li>target's thread: vanish, receiving world and radius checks ({@link #checkTarget})</li>
 *     <li>background: mute and ignore checks, which may read storage ({@link #checkPlayerData})</li>
 *     <li>sender's thread: {@link FeelingSendEvent} ({@link #fireSendEvent})</li>
 *     <li>target's thread: {@link FeelingRecieveEvent}, messages, damage, particles and sounds ({@link #deliver})</li>
 * </ol>
 * On Spigot/Paper the "own thread" steps all run on the main thread; on Folia they run on the region thread
 * that owns each player.
 */
public final class FeelingService {
    private final Supplier<Settings> settings;
    private final Supplier<Messages> messages;
    private final Scheduler scheduler;
    private final FeelingRegistry registry;
    private final PlayerDataService data;
    private final Punishments punishments;
    private final VanishCheck vanish;
    private final NicknameResolver names;
    private final Permissions permissions;
    private final Cooldowns cooldowns;
    private final Feedback feedback;
    private final ParticleEffects particles;

    public FeelingService(Supplier<Settings> settings, Supplier<Messages> messages, Scheduler scheduler,
                          FeelingRegistry registry, PlayerDataService data, Punishments punishments,
                          VanishCheck vanish, NicknameResolver names, Permissions permissions, Cooldowns cooldowns,
                          Feedback feedback, ParticleEffects particles) {
        this.settings = settings;
        this.messages = messages;
        this.scheduler = scheduler;
        this.registry = registry;
        this.data = data;
        this.punishments = punishments;
        this.vanish = vanish;
        this.names = names;
        this.permissions = permissions;
        this.cooldowns = cooldowns;
        this.feedback = feedback;
        this.particles = particles;
    }

    /** One attempt to send a feeling, carrying what's needed to give the cooldown back if it fails. */
    private final class Attempt {
        final CommandSender sender;
        final Player senderPlayer;
        final Feeling feeling;
        final String typedTarget;
        final boolean holdsCooldown;
        final long reservedAt;
        final long cooldownMillis;

        Attempt(CommandSender sender, Feeling feeling, String typedTarget, boolean holdsCooldown,
                long reservedAt, long cooldownMillis) {
            this.sender = sender;
            this.senderPlayer = sender instanceof Player p ? p : null;
            this.feeling = feeling;
            this.typedTarget = typedTarget;
            this.holdsCooldown = holdsCooldown;
            this.reservedAt = reservedAt;
            this.cooldownMillis = cooldownMillis;
        }

        void fail(String messageKey) {
            failWith(messages.get().get(messageKey));
        }

        void failWith(String message) {
            releaseCooldown();
            feedback.prefixed(sender, message);
            feedback.bass(sender);
        }

        void targetGone() {
            failWith(messages.get().get("Player-Offline").replace("%player%", typedTarget));
        }

        void releaseCooldown() {
            if (holdsCooldown) {
                cooldowns.cancelFeeling(senderPlayer.getUniqueId(), reservedAt, cooldownMillis);
            }
        }
    }

    /** Step 1, on the thread that ran the command. */
    public void send(CommandSender sender, String commandName, String[] args) {
        final Settings s = settings.get();
        final Messages m = messages.get();
        final Feeling feeling = registry.get(commandName);
        if (feeling == null) {
            feedback.message(sender, "Emote-Disabled");
            feedback.bass(sender);
            return;
        }

        final Player senderPlayer = sender instanceof Player p ? p : null;
        if (senderPlayer != null && s.useFeelingPermissions() && !permissions.canUse(sender, feeling)) {
            feedback.noPermission(sender);
            return;
        }

        final long now = System.currentTimeMillis();
        final long cooldownMillis = s.feelingCooldownSeconds() * 1000L;
        final boolean holdsCooldown = senderPlayer != null && s.feelingCooldownEnabled() && cooldownMillis > 0
                && !permissions.has(sender, "chatfeelings.bypasscooldowns", true);
        if (holdsCooldown) {
            final long remaining = cooldowns.reserveFeeling(senderPlayer.getUniqueId(), now, cooldownMillis);
            if (remaining > 0) {
                feedback.prefixed(sender, m.get("Cooldown-Active").replace("%time%", ((remaining + 999) / 1000) + "s"));
                feedback.bass(sender);
                return;
            }
        }

        final Attempt attempt = new Attempt(sender, feeling, args.length > 0 ? args[0] : "", holdsCooldown, now, cooldownMillis);
        if (args.length == 0) {
            attempt.fail("No-Player");
            return;
        }
        if (senderPlayer != null && s.disabledSendingWorlds().contains(senderPlayer.getWorld().getName())) {
            attempt.fail("Sending-World-Disabled");
            return;
        }
        if (!feeling.enabled()) {
            attempt.fail("Emote-Disabled");
            return;
        }
        if (args[0].equalsIgnoreCase("console")) {
            attempt.fail("Console-Not-Player");
            return;
        }
        final Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            attempt.targetGone();
            return;
        }
        if (s.preventSelfFeelings() && target.getName().equalsIgnoreCase(sender.getName())) {
            attempt.failWith(m.get("Sender-Is-Target").replace("%command%", selfVerb(feeling.name())));
            return;
        }

        // The sender's position is read here, on their own thread, and compared on the target's thread.
        final Location senderLocation = senderPlayer != null && s.radiusEnabled() ? senderPlayer.getLocation() : null;
        scheduler.entity(target, () -> checkTarget(attempt, target, senderLocation), attempt::targetGone);
    }

    /** Step 2, on the target's thread. */
    private void checkTarget(Attempt a, Player target, Location senderLocation) {
        final Settings s = settings.get();
        if (!target.isOnline() || vanish.isVanished(target)) {
            a.targetGone();
            return;
        }
        if (s.disabledReceivingWorlds().contains(target.getWorld().getName())) {
            a.fail("Receiving-World-Disabled");
            return;
        }
        if (senderLocation != null) {
            final String outside = messages.get().get("Outside-Of-Radius")
                    .replace("%player%", target.getName()).replace("%command%", a.feeling.name());
            if (!target.getWorld().equals(senderLocation.getWorld())) {
                a.failWith(outside);
                return;
            }
            final double distance = senderLocation.distance(target.getLocation());
            if (distance > s.radius()) {
                Log.debug(a.sender.getName() + " was outside the radius of " + s.radius() + ". (They're " + distance + ")");
                a.failWith(outside);
                return;
            }
        }
        scheduler.async(() -> checkPlayerData(a, target));
    }

    /** Step 3, in the background: these checks can hit the disk, a database or a punishment plugin. */
    private void checkPlayerData(Attempt a, Player target) {
        if (a.senderPlayer != null) {
            final UUID sender = a.senderPlayer.getUniqueId();
            final PunishmentHook mutedBy = punishments.findMute(sender, null);
            if (mutedBy != null) {
                Log.debug(a.sender.getName() + " tried to use /" + a.feeling.name() + ", but is muted by " + mutedBy.name() + ".");
                a.fail("Is-Muted");
                return;
            }
            final PlayerData own = data.get(sender);
            if (own != null && own.isMuted()) {
                Log.debug(a.sender.getName() + " tried to use /" + a.feeling.name() + ", but was muted (via CF).");
                a.fail("Is-Muted");
                return;
            }
        }

        final PlayerData targetData = data.get(target.getUniqueId());
        if (targetData != null) {
            if (a.senderPlayer != null && targetData.isIgnoring(a.senderPlayer.getUniqueId())) {
                Log.debug("Not sending feeling to " + target.getName() + " because they are ignoring " + a.sender.getName());
                a.failWith(messages.get().get("Target-Is-Ignoring").replace("%player%", target.getName()));
                return;
            }
            if (!targetData.isAllowingFeelings()) {
                Log.debug("Blocking feeling because " + target.getName() + " is blocking ALL.");
                a.fail("Target-Is-Ignoring-All");
                return;
            }
        }

        final List<Player> recipients = settings.get().globalFeelings()
                ? globalRecipients(a, target, targetData) : List.of();
        scheduler.forSender(a.sender, () -> fireSendEvent(a, target, recipients));
    }

    /** Everyone who should see a global feeling: online players not blocking all feelings or the sender. */
    private List<Player> globalRecipients(Attempt a, Player target, PlayerData targetData) {
        final List<Player> recipients = new ArrayList<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            final PlayerData onlineData = online.getUniqueId().equals(target.getUniqueId())
                    ? targetData : data.get(online.getUniqueId());
            final boolean isSender = online.equals(a.senderPlayer);
            if (onlineData != null && !onlineData.isAllowingFeelings() && !isSender) {
                Log.debug(online.getName() + " is blocking all feelings. Skipping Global Msg!");
                continue;
            }
            if (a.senderPlayer != null && onlineData != null && onlineData.isIgnoring(a.senderPlayer.getUniqueId())) {
                Log.debug(online.getName() + " is blocking feelings from " + a.sender.getName() + ". Skipping global msg!");
                continue;
            }
            recipients.add(online);
        }
        return recipients;
    }

    /** Step 4, on the sender's thread. */
    private void fireSendEvent(Attempt a, Player target, List<Player> recipients) {
        final FeelingSendEvent event = new FeelingSendEvent(a.sender, target, a.feeling.displayName());
        call(event);
        if (event.isCancelled()) {
            Log.debug("A plugin cancelled " + a.sender.getName() + "'s /" + a.feeling.name() + " via FeelingSendEvent.");
            a.releaseCooldown();
            return;
        }
        scheduler.entity(target, () -> deliver(a, target, recipients), a::targetGone);
    }

    /** Step 5, on the target's thread. */
    private void deliver(Attempt a, Player target, List<Player> recipients) {
        if (!target.isOnline()) {
            a.targetGone();
            return;
        }
        final Feeling f = a.feeling;
        // Fired as the deprecated misspelled class, a subclass of FeelingReceiveEvent, so listeners of either get it.
        @SuppressWarnings("deprecation")
        final FeelingRecieveEvent received = new FeelingRecieveEvent(target, a.sender, f.displayName());
        call(received);
        if (received.isCancelled()) {
            Log.debug("A plugin cancelled " + a.sender.getName() + "'s /" + f.name() + " via FeelingReceiveEvent.");
            a.releaseCooldown();
            return;
        }

        final Settings s = settings.get();
        final String senderName = names.displayName(a.sender);
        final String targetName = names.displayName(target);
        if (s.globalFeelings()) {
            final String global = f.globalMessage().replace("%sender%", senderName).replace("%target%", targetName);
            for (Player recipient : recipients) {
                if (a.senderPlayer == null) {
                    feedback.send(recipient, global);
                    continue;
                }
                scheduler.entity(recipient, () -> {
                    final FeelingGlobalNotifyEvent notify = new FeelingGlobalNotifyEvent(recipient, a.sender, target, f.displayName());
                    call(notify);
                    if (!notify.isCancelled()) {
                        feedback.send(recipient, global);
                    }
                });
            }
            if (s.globalBroadcastToConsole()) {
                feedback.send(Bukkit.getConsoleSender(), global);
            }
        } else {
            feedback.send(target, f.targetMessage().replace("%player%", senderName));
            feedback.send(a.sender, f.senderMessage().replace("%player%", targetName));
        }

        if (s.violentCommandHarm() && f.violent()) {
            if (target.isSleeping()) {
                Log.debug("Skipped damage to " + target.getName() + ", as they were sleeping.");
            } else {
                try {
                    target.damage(0.01D);
                } catch (RuntimeException err) {
                    Log.debug("Unable to damage player " + target.getName() + ": " + err);
                }
            }
        }
        if (s.particles()) {
            particles.play(target, f.particle());
        }
        if (s.sounds()) {
            playSounds(a, target, s);
        }
        if (a.senderPlayer != null) {
            final UUID sender = a.senderPlayer.getUniqueId();
            scheduler.async(() -> data.recordFeelingSent(sender, f.displayName()));
        }
    }

    private void playSounds(Attempt a, Player target, Settings s) {
        final FeelingSound first = a.feeling.sound1();
        if (first.isPlayable()) {
            play(target, first.sound(), first.volume(), first.pitch());
            playToSender(a, first.sound(), first.volume(), first.pitch());
        }
        final FeelingSound second = a.feeling.sound2();
        if (second.isPlayable()) {
            final boolean musicDisc = second.name().toUpperCase(Locale.ROOT).contains("DISC");
            if (musicDisc && s.multiVersionSupport()) {
                // Plays a substitute instead of a disc, which would otherwise need stopping on older clients.
                play(target, Sound.AMBIENT_CAVE, 2.0F, 0.5F);
            } else {
                play(target, second.sound(), second.volume(), second.pitch());
                if (!musicDisc) {
                    playToSender(a, second.sound(), second.volume(), second.pitch());
                }
            }
        }
    }

    private void playToSender(Attempt a, Sound sound, float volume, float pitch) {
        if (a.senderPlayer != null) {
            final Player p = a.senderPlayer;
            scheduler.entity(p, () -> play(p, sound, volume, pitch));
        }
    }

    /** Must run on {@code player}'s thread. */
    private static void play(Player player, Sound sound, float volume, float pitch) {
        try {
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (RuntimeException err) {
            Log.debug("Couldn't play a feeling sound to " + player.getName() + ": " + err);
        }
    }

    private static void call(Event event) {
        try {
            Bukkit.getPluginManager().callEvent(event);
        } catch (RuntimeException err) {
            Log.warn("Error while calling " + event.getEventName(), err);
        }
    }

    /** The verb in "You can't %command% yourself". */
    private static String selfVerb(String feeling) {
        return switch (feeling) {
            case "welcomeback" -> "welcome-back";
            case "yell" -> "yell at";
            case "cry" -> "cry on";
            default -> feeling;
        };
    }
}
