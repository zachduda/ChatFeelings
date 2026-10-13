package com.zachduda.chatfeelings.config;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Set;

/**
 * An immutable snapshot of config.yml. A new one is built on startup and on every /cf reload and swapped in
 * atomically, so async tasks always see a consistent set of values and never touch the live config.
 * (Storage settings are read separately by StorageFactory, since they only apply on startup.)
 */
public record Settings(
        boolean globalFeelings,
        boolean globalBroadcastToConsole,
        boolean preventSelfFeelings,
        boolean violentCommandHarm,
        boolean feelingCooldownEnabled,
        int feelingCooldownSeconds,
        boolean ignoreCooldownEnabled,
        int ignoreCooldownSeconds,
        boolean ignoreListCooldownEnabled,
        int ignoreListCooldownSeconds,
        boolean radiusEnabled,
        double radius,
        boolean particles,
        boolean sounds,
        boolean extraHelp,
        boolean useFeelingPermissions,
        Set<String> disabledSendingWorlds,
        Set<String> disabledReceivingWorlds,
        boolean multiVersionSupport,
        boolean useNicknamePlaceholder,
        String nicknamePlaceholder,
        int helpPageLength,
        boolean cfAlias,
        boolean vanishCheck,
        boolean vanishUseLegacy,
        boolean cleanupPlayerData,
        int cleanupAfterDays,
        boolean eraseIfBanned,
        boolean checkForUpdates,
        boolean metrics,
        boolean debug,
        boolean reduceConsoleMessages) {

    public static Settings from(ConfigurationSection c) {
        return new Settings(
                c.getBoolean("General.Global-Feelings.Enabled", false),
                c.getBoolean("General.Global-Feelings.Broadcast-To-Console", true),
                c.getBoolean("General.Prevent-Self-Feelings", true),
                c.getBoolean("General.Violent-Command-Harm", true),
                c.getBoolean("General.Cooldowns.Feelings.Enabled", true),
                Math.max(0, c.getInt("General.Cooldowns.Feelings.Seconds", 10)),
                c.getBoolean("General.Cooldowns.Ignoring.Enabled", true),
                Math.max(0, c.getInt("General.Cooldowns.Ignoring.Seconds", 5)),
                c.getBoolean("General.Cooldowns.Ignore-List.Enabled", true),
                Math.max(0, c.getInt("General.Cooldowns.Ignore-List.Seconds", 10)),
                c.getBoolean("General.Radius.Enabled", false),
                c.getDouble("General.Radius.Radius-In-Blocks", 35),
                c.getBoolean("General.Particles", true),
                c.getBoolean("General.Sounds", true),
                c.getBoolean("General.Extra-Help", true),
                c.getBoolean("General.Use-Feeling-Permissions", false),
                Set.copyOf(c.getStringList("General.Disabled-Sending-Worlds")),
                Set.copyOf(c.getStringList("General.Disabled-Receiving-Worlds")),
                c.getBoolean("General.Multi-Version-Support", false),
                c.getBoolean("General.Use-Nickname-Placeholder", false),
                c.getString("General.Nickname-Placeholder", "%essentials_nickname%"),
                Math.max(1, c.getInt("General.Help-Page-Length", 6)),
                c.getBoolean("Other.CF-Alias", true),
                c.getBoolean("Other.Vanished-Players.Check", true),
                c.getBoolean("Other.Vanished-Players.Use-Legacy", false),
                c.getBoolean("Other.Player-Files.Cleanup", true),
                c.getInt("Other.Player-Files.Cleanup-After-Days", 185),
                c.getBoolean("Other.Player-Files.Erase-If-Banned", true),
                c.getBoolean("Other.Updates.Check", true),
                c.getBoolean("Other.Metrics", true),
                c.getBoolean("Other.Debug", false),
                c.getBoolean("Other.Reduce-Console-Msgs", false));
    }
}
