package com.zachduda.chatfeelings.api;

import com.zachduda.chatfeelings.Main;
import com.zachduda.chatfeelings.storage.PlayerData;
import com.zachduda.chatfeelings.util.Text;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

/**
 * PlaceholderAPI placeholders: %chatfeelings_total_sent%, %chatfeelings_incoming_allow% and
 * %chatfeelings_total_&lt;feeling&gt;% (also with an "s"/"es" ending, ie: total_hugs).
 * <p>
 * Placeholders are often requested every tick from the main thread (scoreboards, tab lists), so they only read
 * cached data. A player who isn't cached yet shows defaults while their data loads in the background.
 */
@SuppressWarnings("unused")
public class Placeholders extends PlaceholderExpansion {
    private final Main plugin;
    private String yes = "true";
    private String no = "false";

    public Placeholders(Main plugin) {
        this.plugin = plugin;
        try {
            yes = PlaceholderAPIPlugin.booleanTrue();
            no = PlaceholderAPIPlugin.booleanFalse();
        } catch (RuntimeException | LinkageError err) {
            plugin.getLogger().info("Unable to hook into PAPI API for boolean results. Defaulting...");
        }
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String getAuthor() {
        return plugin.getAuthor();
    }

    @Override
    public String getIdentifier() {
        return "chatfeelings";
    }

    @Override
    public String getVersion() {
        return plugin.getPluginVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        if (player == null) {
            return "";
        }
        final PlayerData data = plugin.getPlayerData().peek(player.getUniqueId());
        if (data == null) {
            plugin.getPlayerData().prefetch(player.getUniqueId(), plugin.getScheduler()::async);
        }

        if (identifier.equalsIgnoreCase("total_sent")) {
            return Integer.toString(data == null ? 0 : data.getTotalSent());
        }
        if (identifier.equalsIgnoreCase("incoming_allow")) {
            return (data == null || data.isAllowingFeelings()) ? yes : no;
        }
        for (String fl : plugin.getFeelings().names()) {
            if (identifier.equalsIgnoreCase("total_" + fl) || identifier.equalsIgnoreCase("total_" + fl + "s")
                    || identifier.equalsIgnoreCase("total_" + fl + "es")) {
                return Integer.toString(data == null ? 0 : data.getSent(Text.capitalize(fl)));
            }
        }
        return null;
    }
}
