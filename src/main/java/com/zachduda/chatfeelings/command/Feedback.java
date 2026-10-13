package com.zachduda.chatfeelings.command;

import com.zachduda.chatfeelings.config.Messages;
import com.zachduda.chatfeelings.config.Settings;
import com.zachduda.chatfeelings.platform.Scheduler;
import com.zachduda.chatfeelings.util.Log;
import com.zachduda.chatfeelings.util.Text;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.Supplier;

/** Sends coloured messages and the little UI sounds that go with command replies. */
public final class Feedback {
    private final Supplier<Settings> settings;
    private final Supplier<Messages> messages;
    private final Scheduler scheduler;

    public Feedback(Supplier<Settings> settings, Supplier<Messages> messages, Scheduler scheduler) {
        this.settings = settings;
        this.messages = messages;
        this.scheduler = scheduler;
    }

    public Messages messages() {
        return messages.get();
    }

    /** Sends a message as-is (after colouring). Safe from any thread. */
    public void send(CommandSender to, String message) {
        to.sendMessage(Text.color(message));
    }

    /** Sends a message after the configured prefix. */
    public void prefixed(CommandSender to, String message) {
        to.sendMessage(Text.color(messages.get().get("Prefix")) + Text.color(message));
    }

    /** Sends a messages.yml entry after the prefix. */
    public void message(CommandSender to, String key) {
        prefixed(to, messages.get().get(key));
    }

    public void noPermission(CommandSender to) {
        message(to, "No-Permission");
        bass(to);
    }

    public void pop(CommandSender to) {
        play(to, Sound.ENTITY_CHICKEN_EGG, 2.0F, 2.0F);
    }

    public void bass(CommandSender to) {
        play(to, Sound.BLOCK_NOTE_BLOCK_BASS, 2.0F, 1.3F);
    }

    public void levelUp(CommandSender to) {
        play(to, Sound.ENTITY_PLAYER_LEVELUP, 2.0F, 2.0F);
    }

    private void play(CommandSender to, Sound sound, float volume, float pitch) {
        if (!settings.get().sounds() || !(to instanceof Player player)) {
            return;
        }
        scheduler.entity(player, () -> {
            try {
                player.playSound(player.getLocation(), sound, volume, pitch);
            } catch (RuntimeException err) {
                Log.debug("Couldn't play a sound to " + player.getName() + ": " + err);
            }
        });
    }
}
