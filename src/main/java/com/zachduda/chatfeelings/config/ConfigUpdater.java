package com.zachduda.chatfeelings.config;

import com.zachduda.chatfeelings.util.Log;
import com.zachduda.chatfeelings.util.McVersion;
import com.zachduda.chatfeelings.util.YamlFiles;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

/** Brings config.yml up to date: adds missing keys, applies version migrations and writes the header. */
public final class ConfigUpdater {
    static final int VERSION = 8;
    private static final String DISCORD = "zachduda.com/discord";

    private ConfigUpdater() {
    }

    /** Updates the plugin's loaded config and saves it, unless config.yml couldn't be parsed. */
    public static void update(JavaPlugin plugin) {
        final File file = new File(plugin.getDataFolder(), "config.yml");
        final boolean fileReadable = YamlFiles.load(file).ok();
        final FileConfiguration c = plugin.getConfig();

        c.options().copyDefaults(true);
        migrate(c);
        trackServerVersion(c);
        YamlFiles.setHeader(c, List.of(
                "Thanks for downloading ChatFeelings!",
                "Each feeling's messages & sounds are in the Feelings folder. Other messages are in messages.yml.",
                "Having trouble? Join our support discord: " + DISCORD));

        if (fileReadable) {
            plugin.saveConfig();
        } else {
            Log.warn("config.yml has a formatting error, so default settings are being used until it's fixed. "
                    + "The file was not changed.");
        }
    }

    static void migrate(FileConfiguration c) {
        if (c.getBoolean("General.Radius.Enabled") && c.getInt("General.Radius.Radius-In-Blocks") == 0) {
            Log.warn("Feeling radius cannot be 0, disabling the radius.");
            c.set("General.Radius.Radius-In-Blocks", 35);
            c.set("General.Radius.Enabled", false);
        }

        if (!c.contains("Version", true)) {
            return;
        }
        final int version = c.getInt("Version");
        if (version == VERSION) {
            return;
        }
        if (version <= 4) {
            c.set("Other.Bypass-Version-Block", null);
        }
        c.set("General.Use-Feeling-Permissions", true);
        c.set("General.Multi-Version-Support", false);
        if (version < 6) {
            c.set("General.No-Violent-Cmds-When-Sleeping", null);
            c.set("General.Cooldowns.Ignore-List.Enabled", true);
            c.set("General.Cooldowns.Ignore-List.Seconds", 10);
        }
        if (version < 7) {
            c.set("Cooldowns.Ignore-List.Enabled", null);
            c.set("Cooldowns.Ignore-List.Seconds", null);
        }
        if (version < 8) {
            c.set("Other.CF-Alias", true);
        }
        c.set("Version", VERSION);
    }

    /** Warns once when the server's Minecraft version changes, since sound names may need updating. */
    private static void trackServerVersion(FileConfiguration c) {
        final String current = McVersion.current().majorMinor(".");
        final String last = c.getString("LVU");
        if (last != null && !last.isEmpty() && !last.equals(current)) {
            Log.info("--------------------[ Server Version Updated ]----------------------");
            Log.warn("Your server was running " + last + " and is now running " + current + ".");
            Log.warn("Sound names in the Feelings folder may need to be changed if any sounds stop working.");
            Log.info("------------------------------------------------------------------");
        }
        c.set("LVU", current);
    }
}
