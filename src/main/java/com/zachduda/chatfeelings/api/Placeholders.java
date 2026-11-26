package com.zachduda.chatfeelings.api;

import com.zachduda.chatfeelings.Main;
import com.zachduda.chatfeelings.feeling.Feeling;
import com.zachduda.chatfeelings.storage.PlayerData;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.util.Locale;
import java.util.function.Function;

/**
 * PlaceholderAPI placeholders: %chatfeelings_total_sent%, %chatfeelings_incoming_allow% and
 * %chatfeelings_total_hugs% etc.
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

        final String id = identifier.toLowerCase(Locale.ROOT);
        if (id.equals("total_sent")) {
            return Integer.toString(data == null ? 0 : data.getTotalSent());
        }
        if (id.equals("incoming_allow")) {
            return (data == null || data.isAllowingFeelings()) ? yes : no;
        }
        final Feeling feeling = feelingFor(id, plugin.getFeelings()::get);
        if (feeling != null) {
            return Integer.toString(data == null ? 0 : data.getSent(feeling.displayName()));
        }
        return null;
    }

    /**
     * The feeling a lower-case "total_&lt;feeling&gt;" identifier counts, also accepting the plural ("total_hugs",
     * "total_punches"), or null. Placeholders refresh constantly on scoreboards, so this is a couple of map
     * lookups rather than a scan of every feeling.
     */
    static Feeling feelingFor(String id, Function<String, Feeling> byName) {
        if (!id.startsWith("total_")) {
            return null;
        }
        final String name = id.substring("total_".length());
        Feeling feeling = byName.apply(name);
        if (feeling == null && name.endsWith("s")) {
            feeling = byName.apply(name.substring(0, name.length() - 1));
        }
        if (feeling == null && name.endsWith("es")) {
            feeling = byName.apply(name.substring(0, name.length() - 2));
        }
        return feeling;
    }
}
