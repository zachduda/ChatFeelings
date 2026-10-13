package com.zachduda.chatfeelings.hook;

import com.zachduda.chatfeelings.config.Messages;
import com.zachduda.chatfeelings.config.Settings;
import com.zachduda.chatfeelings.util.Log;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * The name shown for a player in feeling messages: their nickname through a PlaceholderAPI placeholder when
 * General.Use-Nickname-Placeholder is on, otherwise their username. The console shows as Console-Name.
 */
public final class NicknameResolver {
    private final boolean papiInstalled;
    private volatile boolean useNicknames = false;
    private volatile String placeholder = "%essentials_nickname%";
    private volatile String consoleName = "The Server";

    public NicknameResolver(boolean papiInstalled) {
        this.papiInstalled = papiInstalled;
    }

    /** Applies the current settings. Called on startup and reload. */
    public void configure(Settings settings, Messages messages) {
        final String console = messages.getIfSet("Console-Name");
        consoleName = console != null ? console : "The Server";
        placeholder = settings.nicknamePlaceholder();
        useNicknames = papiInstalled && settings.useNicknamePlaceholder() && placeholder != null && !placeholder.isBlank();
    }

    public String displayName(CommandSender who) {
        if (!(who instanceof Player player)) {
            return consoleName;
        }
        if (!useNicknames) {
            return player.getName();
        }
        try {
            final String nick = PapiBridge.apply(player, placeholder);
            // An unresolved placeholder comes back unchanged; fall back to the username then.
            if (nick == null || nick.isBlank() || nick.equals(placeholder)) {
                return player.getName();
            }
            return nick;
        } catch (RuntimeException | LinkageError err) {
            Log.debug("Couldn't resolve the nickname placeholder for " + player.getName() + ": " + err);
            return player.getName();
        }
    }
}
