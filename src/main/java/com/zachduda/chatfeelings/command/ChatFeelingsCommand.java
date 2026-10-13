package com.zachduda.chatfeelings.command;

import com.zachduda.chatfeelings.Main;
import com.zachduda.chatfeelings.config.Messages;
import com.zachduda.chatfeelings.config.Settings;
import com.zachduda.chatfeelings.feeling.Feeling;
import com.zachduda.chatfeelings.hook.PunishmentHook;
import com.zachduda.chatfeelings.storage.PlayerData;
import com.zachduda.chatfeelings.util.Log;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/** /chatfeelings (and /cf): help, stats, mute, ignore, reload and friends. */
public final class ChatFeelingsCommand implements TabExecutor {
    private static final long RELOAD_COOLDOWN_SECONDS = 10;
    /** /cf mutelist checks every player against punishment plugins or a database, so it's rate limited. */
    private static final long MUTE_LIST_COOLDOWN_SECONDS = 60;

    private final Main plugin;
    private final Feedback feedback;
    private final Permissions permissions;
    private final AtomicLong lastReload = new AtomicLong();
    private final AtomicLong lastMuteList = new AtomicLong();

    public ChatFeelingsCommand(Main plugin, Feedback feedback, Permissions permissions) {
        this.plugin = plugin;
        this.feedback = feedback;
        this.permissions = permissions;
    }

    private Messages msg() {
        return feedback.messages();
    }

    private Settings settings() {
        return plugin.getSettings();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (args.length == 0) {
            header(sender);
            feedback.send(sender, "&8&l> &#f4fcab/cf help &7&ofor commands & settings.");
            feedback.send(sender, "");
            feedback.pop(sender);
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "help" -> help(sender);
            case "version" -> version(sender);
            case "stats" -> stats(sender, args);
            case "reload" -> reload(sender);
            case "uuid" -> uuid(sender, args);
            case "mutelist" -> muteList(sender);
            case "mute" -> mute(sender, args, true);
            case "unmute" -> mute(sender, args, false);
            case "ignore", "unignore" -> ignore(sender, args);
            case "debug-generate-file" -> debugGenerateFile(sender);
            default -> {
                header(sender);
                feedback.send(sender, "&8&l> &#FF8C6B&lHmm. &7That command does not exist.");
                feedback.send(sender, "");
                feedback.bass(sender);
            }
        }
        return true;
    }

    private void header(CommandSender sender) {
        feedback.send(sender, "");
        feedback.send(sender, msg().get("Prefix-Header"));
    }

    private boolean denied(CommandSender sender, String node, boolean adminNode) {
        if (permissions.has(sender, node, adminNode)) {
            return false;
        }
        feedback.noPermission(sender);
        return true;
    }

    private void neverJoined(CommandSender sender, String name) {
        feedback.bass(sender);
        feedback.prefixed(sender, msg().get("Player-Never-Joined").replace("%player%", name));
    }

    private void help(CommandSender sender) {
        final Messages m = msg();
        final String d = "Command-Help.Descriptions.";
        header(sender);
        feedback.send(sender, "&8&l> &#f4fcab/cf help &7" + m.get(d + "Help"));
        if (permissions.has(sender, "chatfeelings.ignore")) {
            feedback.send(sender, "&8&l> &#f4fcab/cf ignore (player) &7" + m.get(d + "Ignore"));
            feedback.send(sender, "&8&l> &#f4fcab/cf ignore all &7" + m.get(d + "Ignore-All"));
        }
        if (permissions.has(sender, "chatfeelings.stats")) {
            feedback.send(sender, "&8&l> &#f4fcab/cf stats &7" + m.get(d + "Stats"));
        }
        if (permissions.has(sender, "chatfeelings.stats.others", true)) {
            feedback.send(sender, "&8&l> &#f4fcab/cf stats (player) &7" + m.get(d + "Stats-Others"));
        }
        if (permissions.has(sender, "chatfeelings.mute", true)) {
            feedback.send(sender, "&8&l> &#f4fcab/cf mute (player) &7" + m.get(d + "Mute"));
            feedback.send(sender, "&8&l> &#f4fcab/cf unmute (player) &7" + m.get(d + "Unmute"));
            feedback.send(sender, "&8&l> &#f4fcab/cf mutelist &7" + m.get(d + "Mute-List"));
        }
        if (permissions.has(sender, "chatfeelings.admin", true)) {
            feedback.send(sender, "&8&l> &#f4fcab/cf version &7" + m.get(d + "Plugin-Version"));
            feedback.send(sender, "&8&l> &#f4fcab/cf reload &7" + m.get(d + "Plugin-Reload"));
        }
        feedback.send(sender, "&8&l> &#fcdcab&l/feelings &7" + m.get(d + "Feelings"));
        feedback.send(sender, "");
        feedback.pop(sender);
    }

    private void version(CommandSender sender) {
        header(sender);
        feedback.send(sender, "&8&l> &7You're running &fv" + plugin.getPluginVersion());
        feedback.send(sender, "");
        feedback.pop(sender);
    }

    private void stats(CommandSender sender, String[] args) {
        if (denied(sender, "chatfeelings.stats", false)) {
            return;
        }
        if (args.length == 1) {
            if (!(sender instanceof Player player)) {
                feedback.message(sender, "No-Player");
                return;
            }
            final UUID own = player.getUniqueId();
            plugin.getScheduler().async(() -> showStats(sender, own, true));
            return;
        }
        if (denied(sender, "chatfeelings.stats.others", true)) {
            return;
        }
        if (args[1].equalsIgnoreCase("console")) {
            feedback.message(sender, "Console-Not-Player");
            feedback.bass(sender);
            return;
        }
        final String lookup = args[1];
        plugin.getScheduler().async(() -> {
            final UUID uuid = plugin.getPlayerData().findUUID(lookup);
            if (uuid == null) {
                neverJoined(sender, lookup);
                return;
            }
            showStats(sender, uuid, false);
        });
    }

    /** Runs in the background: may read storage. */
    private void showStats(CommandSender sender, UUID uuid, boolean own) {
        final PlayerData data = plugin.getPlayerData().get(uuid);
        if (data == null) {
            feedback.bass(sender);
            feedback.message(sender, "Folder-Not-Found");
            return;
        }
        final String name = data.getUsername();
        feedback.send(sender, msg().get(own ? "Stats-Header-Own" : "Stats-Header-Other").replace("%player%", name));

        final int total = data.getTotalSent();
        if (total == 0) {
            feedback.send(sender, own ? "&f   &8&l> &7You haven't sent anyone feelings yet!"
                    : "&f   &8&l> &f" + name + " &7hasn't sent feelings yet!");
        } else {
            for (Feeling f : plugin.getFeelings().all()) {
                final int sent = data.getSent(f.displayName());
                if (sent > 0) {
                    feedback.send(sender, "&f   &8&l> &7" + f.statsLabel() + ": &f&l" + sent);
                }
            }
            feedback.send(sender, "&f   &8&l> &e" + (own ? "You've" : "They've") + " Sent: &f&l" + total);
        }
        feedback.pop(sender);
    }

    private void reload(CommandSender sender) {
        if (denied(sender, "chatfeelings.admin", true)) {
            return;
        }
        final long now = System.currentTimeMillis();
        final long secondsLeft = (lastReload.get() / 1000 + RELOAD_COOLDOWN_SECONDS) - now / 1000;
        if (secondsLeft > 0) {
            feedback.prefixed(sender, "&7Please wait &f&l" + secondsLeft + "s &7until reloading again.");
            feedback.bass(sender);
            return;
        }
        lastReload.set(now);
        header(sender);

        // Reloading reads and writes several files, so keep it off the server threads.
        plugin.getScheduler().async(() -> {
            final boolean storageTypeChanged;
            try {
                storageTypeChanged = plugin.reloadAll();
            } catch (RuntimeException err) {
                Log.warn("Error occurred when trying to reload ChatFeelings", err);
                feedback.send(sender, Log.isDebug()
                        ? "&8&l> &4&lError! &fSomething in your config isn't right. Check console!"
                        : "&8&l> &4&lError! &fSomething in your ChatFeelings files is wrong.");
                feedback.bass(sender);
                return;
            }
            if (storageTypeChanged) {
                feedback.send(sender, "&8&l> &#FF8C6BHeads up! &7Changing the storage type requires a full server restart.");
            }
            final long took = System.currentTimeMillis() - now;
            final String time = took >= 1000 ? (took / 1000.0) + "s" : took + "ms";
            feedback.send(sender, msg().get("Reload").replace("%time%", time));
            if (sender instanceof Player) {
                Log.info("Configuration & Files reloaded by " + sender.getName() + " in " + time);
            }
            feedback.send(sender, "");
            feedback.levelUp(sender);
        });
    }

    private void uuid(CommandSender sender, String[] args) {
        if (denied(sender, "chatfeelings.admin", true)) {
            return;
        }
        if (args.length == 1) {
            feedback.message(sender, "No-Player");
            feedback.bass(sender);
            return;
        }
        if (args[1].equalsIgnoreCase("console")) {
            feedback.message(sender, "Console-Not-Player");
            feedback.bass(sender);
            return;
        }
        final String lookup = args[1];
        plugin.getScheduler().async(() -> {
            final UUID uuid = plugin.getPlayerData().findUUID(lookup);
            if (uuid == null) {
                neverJoined(sender, lookup);
                return;
            }
            final String name = plugin.getPlayerData().findName(uuid);
            feedback.prefixed(sender, "&fThe UUID of " + (name != null ? name : lookup) + " is &7" + uuid);
            feedback.pop(sender);
        });
    }

    private void muteList(CommandSender sender) {
        if (denied(sender, "chatfeelings.mute", true)) {
            return;
        }
        final long now = System.currentTimeMillis();
        final long secondsLeft = (lastMuteList.get() / 1000 + MUTE_LIST_COOLDOWN_SECONDS) - now / 1000;
        if (secondsLeft > 0) {
            feedback.prefixed(sender, "&7Please wait &f&l" + secondsLeft + "s &7before checking the mute list.");
            return;
        }
        if (plugin.getPunishments().hasLiteBans() || plugin.getPunishments().hasAdvancedBan()
                || plugin.getPlayerData().isSharedDatabase()) {
            lastMuteList.set(now);
        }

        plugin.getScheduler().async(() -> {
            final List<PlayerData> all = plugin.getPlayerData().listAll();
            if (all.isEmpty()) {
                feedback.message(sender, "Folder-Not-Found");
                feedback.bass(sender);
                return;
            }
            final Messages m = msg();
            feedback.send(sender, "");
            feedback.send(sender, m.get("Mute-List-Header"));
            int totalMuted = 0;
            for (PlayerData data : all) {
                final PunishmentHook mutedBy = plugin.getPunishments().findMute(data.getUuid(), data.getIp());
                if (!data.isMuted() && mutedBy == null) {
                    continue;
                }
                totalMuted++;
                String line = m.get("Mute-List-Player").replace("%player%", data.getUsername());
                if (mutedBy != null) {
                    line += " &#FF8C6B(" + mutedBy.name() + (data.isMuted() ? " & CF)" : ")");
                }
                feedback.send(sender, line);
            }
            final String totalKey = switch (totalMuted) {
                case 0 -> "Mute-List-Total-Zero";
                case 1 -> "Mute-List-Total-One";
                default -> "Mute-List-Total-Many";
            };
            feedback.send(sender, m.get(totalKey).replace("%total%", Integer.toString(totalMuted)));
            feedback.send(sender, "");
            feedback.pop(sender);
        });
    }

    private void mute(CommandSender sender, String[] args, boolean muting) {
        if (!permissions.has(sender, "chatfeelings.mute", true)) {
            feedback.noPermission(sender);
            if (settings().extraHelp()) {
                feedback.message(sender, "No-Perm-Mute-Suggestion");
            }
            return;
        }
        if (args.length == 1) {
            feedback.message(sender, muting ? "No-Player-Mute" : "No-Player-Unmute");
            feedback.bass(sender);
            return;
        }
        if (muting && args[1].equalsIgnoreCase(sender.getName())) {
            feedback.bass(sender);
            feedback.message(sender, "Cant-Mute-Self");
            return;
        }

        final String lookup = args[1];
        plugin.getScheduler().async(() -> {
            final UUID uuid = plugin.getPlayerData().findUUID(lookup);
            final PlayerData data = uuid == null ? null : plugin.getPlayerData().load(uuid);
            if (data == null) {
                neverJoined(sender, lookup);
                return;
            }
            final Messages m = msg();
            final String name = data.getUsername();

            if (muting) {
                if (data.isMuted()) {
                    feedback.bass(sender);
                    feedback.message(sender, "Player-Already-Muted");
                    if (settings().extraHelp()) {
                        feedback.message(sender, "Already-Mute-Unmute-Suggestion");
                    }
                    return;
                }
                if (!plugin.getPlayerData().setMuted(uuid, true)) {
                    feedback.bass(sender);
                    feedback.prefixed(sender, "&#FF8C6BError. &fWe couldn't save that player's mute status. Check console.");
                    return;
                }
                feedback.prefixed(sender, m.get("Player-Has-Been-Muted").replace("%player%", name));
                feedback.pop(sender);
                if (plugin.getPunishments().findMute(uuid, data.getIp()) != null) {
                    feedback.prefixed(sender, m.get("Extra-Mute-Present").replace("%player%", name));
                }
                return;
            }

            if (data.isMuted()) {
                if (!plugin.getPlayerData().setMuted(uuid, false)) {
                    feedback.bass(sender);
                    feedback.prefixed(sender, "&#FF8C6BError. &fWe couldn't save that player's mute status. Check console.");
                    return;
                }
                feedback.prefixed(sender, m.get("Player-Has-Been-Unmuted").replace("%player%", name));
                feedback.pop(sender);
                return;
            }

            feedback.bass(sender);
            final PunishmentHook mutedBy = plugin.getPunishments().findMute(uuid, data.getIp());
            final String key = mutedBy != null && mutedBy.mutedMessageKey() != null
                    ? mutedBy.mutedMessageKey() : "Player-Already-Unmuted";
            // Older messages.yml defaults used "%player" without the closing %, so handle both.
            feedback.prefixed(sender, m.get(key).replace("%player%", name).replace("%player", name));
        });
    }

    private void ignore(CommandSender sender, String[] args) {
        if (denied(sender, "chatfeelings.ignore", false)) {
            return;
        }
        if (!(sender instanceof Player player)) {
            feedback.prefixed(sender, "&#FF8C6B&lSorry. &fOnly players can ignore other players.");
            return;
        }
        final Settings s = settings();
        final UUID self = player.getUniqueId();

        if (args.length == 1) {
            if (s.ignoreListCooldownEnabled()) {
                if (plugin.getCooldowns().isIgnoreListCooldown(self)) {
                    feedback.message(sender, "Ignore-List-Cooldown");
                    feedback.bass(sender);
                    return;
                }
                plugin.getCooldowns().startIgnoreListCooldown(self, s.ignoreListCooldownSeconds());
            }
            plugin.getScheduler().async(() -> showIgnoreList(sender, self));
            return;
        }

        if (args[1].equalsIgnoreCase(sender.getName())) {
            feedback.bass(sender);
            feedback.message(sender, "Cant-Ignore-Self");
            return;
        }
        if (args[1].equalsIgnoreCase("console")) {
            feedback.message(sender, "Console-Not-Player");
            feedback.bass(sender);
            return;
        }

        // Reserved here, on the command thread, so repeated commands can't all slip through while the first is
        // still saving. Handed back if the change doesn't go through, so a typo doesn't cost the wait.
        final long now = System.currentTimeMillis();
        final long cooldownMillis = s.ignoreCooldownSeconds() * 1000L;
        final boolean holdsCooldown = s.ignoreCooldownEnabled() && cooldownMillis > 0
                && !permissions.has(sender, "chatfeelings.bypasscooldowns", true);
        if (holdsCooldown && plugin.getCooldowns().reserveIgnore(self, now, cooldownMillis) > 0) {
            feedback.bass(sender);
            feedback.message(sender, "Ignore-Cooldown");
            return;
        }
        final Runnable releaseCooldown = () -> {
            if (holdsCooldown) {
                plugin.getCooldowns().cancelIgnore(self, now, cooldownMillis);
            }
        };

        final String lookup = args[1];
        plugin.getScheduler().async(() -> {
            final Messages m = msg();
            if (lookup.equalsIgnoreCase("all")) {
                final Boolean allowing = plugin.getPlayerData().toggleAllowFeelings(self);
                if (allowing == null) {
                    releaseCooldown.run();
                    feedback.prefixed(sender, "&#FF8C6BSorry!&f We couldn't find your player data.");
                    feedback.bass(sender);
                    return;
                }
                feedback.message(sender, allowing ? "Ignoring-Off-All" : "Ignoring-On-All");
                feedback.pop(sender);
                return;
            }

            final UUID target = plugin.getPlayerData().findUUID(lookup);
            if (target == null) {
                releaseCooldown.run();
                neverJoined(sender, lookup);
                return;
            }
            if (target.equals(self)) {
                releaseCooldown.run();
                feedback.bass(sender);
                feedback.message(sender, "Cant-Ignore-Self");
                return;
            }
            final Boolean nowIgnoring = plugin.getPlayerData().toggleIgnoring(self, target);
            if (nowIgnoring == null) {
                releaseCooldown.run();
                feedback.prefixed(sender, "&#FF8C6BSorry!&f We couldn't find your player data.");
                feedback.bass(sender);
                return;
            }
            feedback.prefixed(sender, m.get(nowIgnoring ? "Ignoring-On-Player" : "Ignoring-Off-Player").replace("%player%", lookup));
            feedback.pop(sender);
        });
    }

    /** Runs in the background: may read storage. */
    private void showIgnoreList(CommandSender sender, UUID self) {
        final PlayerData data = plugin.getPlayerData().load(self);
        if (data == null) {
            feedback.message(sender, "Folder-Not-Found");
            feedback.bass(sender);
            return;
        }
        final Messages m = msg();
        feedback.send(sender, " ");
        feedback.send(sender, m.get("Ignore-List-Header"));
        if (data.getIgnoring().isEmpty()) {
            feedback.send(sender, m.get(data.isAllowingFeelings() ? "Ignore-List-None" : "Ignore-List-All"));
        } else {
            for (String ignored : data.getIgnoring()) {
                try {
                    final String name = plugin.getPlayerData().findName(UUID.fromString(ignored));
                    if (name != null) {
                        feedback.send(sender, "  &8&l> &f&l" + name);
                    }
                } catch (IllegalArgumentException badUuid) {
                    Log.debug("Skipping invalid UUID in " + sender.getName() + "'s ignore list: " + ignored);
                }
            }
        }
        feedback.send(sender, " ");
        feedback.pop(sender);
    }

    private void debugGenerateFile(CommandSender sender) {
        if (denied(sender, "chatfeelings.admin", true)) {
            return;
        }
        if (!Log.isDebug()) {
            feedback.prefixed(sender, "&7Enable debug mode in your config to use this.");
            feedback.bass(sender);
            return;
        }
        final long start = System.currentTimeMillis();
        plugin.getScheduler().async(() -> {
            final UUID uuid = UUID.randomUUID();
            try {
                plugin.getPlayerData().recordLogin(uuid, "User-" + uuid, "127.0.0.1", System.currentTimeMillis());
                feedback.prefixed(sender, "&a&lDone! &7Player test data (" + plugin.getPlayerData().storage().getName()
                        + ") created in &f" + (System.currentTimeMillis() - start) + "ms");
                feedback.pop(sender);
            } catch (RuntimeException err) {
                Log.warn("Test data creation failed", err);
                feedback.prefixed(sender, "&7See console. Test data creation failed.");
                feedback.bass(sender);
            }
        });
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        final List<String> options = new ArrayList<>();
        if (args.length == 1) {
            options.add("help");
            if (permissions.has(sender, "chatfeelings.stats")) {
                options.add("stats");
            }
            if (permissions.has(sender, "chatfeelings.mute", true)) {
                options.add("mute");
                options.add("unmute");
                options.add("mutelist");
            }
            if (permissions.has(sender, "chatfeelings.ignore")) {
                options.add("ignore");
            }
            if (permissions.has(sender, "chatfeelings.admin", true)) {
                options.add("reload");
                options.add("version");
            }
            return StringUtil.copyPartialMatches(args[0], options, new ArrayList<>());
        }
        if (args.length == 2) {
            final String sub = args[0].toLowerCase(Locale.ROOT);
            final boolean wantsPlayer = switch (sub) {
                case "ignore", "unignore" -> {
                    final boolean allowed = permissions.has(sender, "chatfeelings.ignore");
                    if (allowed) {
                        options.add("all");
                    }
                    yield allowed;
                }
                case "mute", "unmute" -> permissions.has(sender, "chatfeelings.mute", true);
                case "stats" -> permissions.has(sender, "chatfeelings.stats.others", true);
                case "uuid" -> permissions.has(sender, "chatfeelings.admin", true);
                default -> false;
            };
            if (wantsPlayer) {
                options.addAll(FeelingCommand.visiblePlayerNames(sender));
            }
            return StringUtil.copyPartialMatches(args[1], options, new ArrayList<>());
        }
        return options;
    }
}
