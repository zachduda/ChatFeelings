package com.zachduda.chatfeelings;

import com.zachduda.chatfeelings.update.UpdateChecker;
import com.zachduda.chatfeelings.util.McVersion;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

final class PlayerListener implements Listener {
    private static final UUID DEVELOPER = UUID.fromString("6191ff85-e092-4e9a-94bd-63df409c2079");

    private final Main plugin;

    PlayerListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        plugin.recordJoin(player);

        final UpdateChecker updates = plugin.getUpdateChecker();
        if (updates != null && updates.isOutdated() && plugin.getSettings().checkForUpdates()
                && plugin.getPermissions().has(player, "chatfeelings.admin", true)) {
            plugin.getFeedback().prefixed(player, "&#f4fcabUpdate Available &8› &7Download @ &fzduda.dev/cf &7("
                    + plugin.getPluginVersion() + " → &f" + updates.getPostedVersion() + "&r&7)");
        }
        if (player.getUniqueId().equals(DEVELOPER)) {
            plugin.getFeedback().send(player, "&7This server is running &fChatFeelings &6v" + plugin.getPluginVersion()
                    + " &7for " + McVersion.current());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        plugin.recordQuit(event.getPlayer());
    }
}
