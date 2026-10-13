package com.zachduda.chatfeelings.api;

import com.zachduda.chatfeelings.Main;
import com.zachduda.chatfeelings.feeling.Feeling;
import com.zachduda.chatfeelings.storage.PlayerData;
import com.zachduda.chatfeelings.util.Log;
import com.zachduda.chatfeelings.util.Text;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * ChatFeelings' public API. Methods marked "MUST USE ASYNC" may query storage or a punishment plugin's database
 * and throw if called from a server thread; the others are best called asynchronously too.
 */
@SuppressWarnings("unused")
public class ChatFeelingsAPI {

    private static Main plugin() {
        return Main.getPlugin(Main.class);
    }

    private static void requireAsync(String method) throws IllegalAccessException {
        if (Bukkit.isPrimaryThread()) {
            throw new IllegalAccessException("ChatFeelings API " + method + " must be accessed asynchronously.");
        }
    }

    /**
     * MUST USE ASYNC. Whether a player is muted, through ChatFeelings (/cf mute) or a supported punishment plugin.
     *
     * @param username the username to check
     * @return true if muted
     */
    public static boolean isMuted(String username) throws IllegalAccessException {
        requireAsync("isMuted()");
        final UUID uuid = plugin().getPlayerData().findUUID(username);
        return uuid != null && mutedNow(uuid);
    }

    /**
     * MUST USE ASYNC. Whether a player is muted, through ChatFeelings (/cf mute) or a supported punishment plugin.
     *
     * @param uuid the UUID to check
     * @return true if muted
     */
    public static boolean isMuted(UUID uuid) throws IllegalAccessException {
        requireAsync("isMuted()");
        return mutedNow(uuid);
    }

    private static boolean mutedNow(UUID uuid) {
        final PlayerData data = plugin().getPlayerData().get(uuid);
        return (data != null && data.isMuted()) || plugin().getPunishments().findMute(uuid, null) != null;
    }

    /**
     * Recommended to use async. Whether ChatFeelings has data for a player.
     *
     * @param uuid the UUID to check
     */
    public static boolean hasPlayedBefore(UUID uuid) {
        return plugin().getPlayerData().get(uuid) != null;
    }

    /**
     * Recommended to use async. Whether ChatFeelings has data for a player.
     *
     * @param username the username to check
     */
    public static boolean hasPlayedBefore(String username) {
        return plugin().getPlayerData().findUUID(username) != null;
    }

    /**
     * MUST USE ASYNC. Whether a player is banned, through a supported punishment plugin or the vanilla ban list.
     *
     * @param username the username to check
     * @return true if banned
     */
    public static boolean isBanned(String username) throws IllegalAccessException {
        requireAsync("isBanned()");
        final UUID uuid = plugin().getPlayerData().findUUID(username);
        return uuid != null && plugin().getPunishments().findBan(uuid, null) != null;
    }

    /**
     * MUST USE ASYNC. Whether a player is banned, through a supported punishment plugin or the vanilla ban list.
     *
     * @param uuid the UUID to check
     * @return true if banned
     */
    public static boolean isBanned(UUID uuid) throws IllegalAccessException {
        requireAsync("isBanned()");
        return plugin().getPunishments().findBan(uuid, null) != null;
    }

    /**
     * A feeling's global message from its Feelings/&lt;name&gt;.yml file.
     *
     * @param feeling the feeling's name, ie: "hug"
     * @return the message, or null if there's no such feeling
     */
    public static String getGlobalEmoteMessage(String feeling) {
        final Feeling f = feeling(feeling, "getGlobalEmoteMessage");
        return f == null ? null : f.globalMessage();
    }

    /**
     * A feeling's message for the sender from its Feelings/&lt;name&gt;.yml file.
     *
     * @param feeling the feeling's name, ie: "hug"
     * @return the message, or null if there's no such feeling
     */
    public static String getSenderEmoteMessage(String feeling) {
        final Feeling f = feeling(feeling, "getSenderEmoteMessage");
        return f == null ? null : f.senderMessage();
    }

    /**
     * A feeling's message for the target from its Feelings/&lt;name&gt;.yml file.
     *
     * @param feeling the feeling's name, ie: "hug"
     * @return the message, or null if there's no such feeling
     */
    public static String getTargetEmoteMessage(String feeling) {
        final Feeling f = feeling(feeling, "getTargetEmoteMessage");
        return f == null ? null : f.targetMessage();
    }

    private static Feeling feeling(String name, String method) {
        final Feeling f = plugin().getFeelings().get(name);
        if (f == null) {
            Log.warn("[API] " + method + " failed. No such feeling: " + name);
        }
        return f;
    }

    /** Recommended to use async. How many times a player sent a feeling. */
    public static int getSentStats(String name, String feeling) {
        final UUID uuid = plugin().getPlayerData().findUUID(name);
        return uuid == null ? 0 : getSentStats(uuid, feeling);
    }

    /** Recommended to use async. How many times a player sent a feeling. */
    public static int getSentStats(UUID uuid, String feeling) {
        if (feeling == null || !plugin().getFeelings().isFeeling(feeling)) {
            return 0;
        }
        final PlayerData data = plugin().getPlayerData().get(uuid);
        return data == null ? 0 : data.getSent(Text.capitalize(feeling.toLowerCase(Locale.ROOT)));
    }

    /** Recommended to use async. How many feelings a player has sent in total. */
    public static int getTotalFeelingsSent(String name) {
        final UUID uuid = plugin().getPlayerData().findUUID(name);
        return uuid == null ? 0 : getTotalFeelingsSent(uuid);
    }

    /** Recommended to use async. How many feelings a player has sent in total. */
    public static int getTotalFeelingsSent(UUID uuid) {
        final PlayerData data = plugin().getPlayerData().get(uuid);
        return data == null ? 0 : data.getTotalSent();
    }

    public static boolean usingAdvancedBans() {
        return plugin().getPunishments().hasAdvancedBan();
    }

    public static boolean usingLiteBans() {
        return plugin().getPunishments().hasLiteBans();
    }

    public static boolean usingEssentials() {
        return plugin().getPunishments().hasEssentials();
    }

    public static boolean hookedToPunishments() {
        return usingAdvancedBans() || usingEssentials() || usingLiteBans();
    }

    /** Every usable feeling's name (lower-case), including custom feelings. A copy, so it can't be modified. */
    public static List<String> getFeelingsList() {
        return plugin().getFeelings().names();
    }

    /** Recommended to use async. False if the player has turned on "/cf ignore all". */
    public static boolean isAcceptingFeelings(UUID u) {
        final PlayerData data = plugin().getPlayerData().get(u);
        return data == null || data.isAllowingFeelings();
    }
}
