package com.zachduda.chatfeelings.config;

import com.zachduda.chatfeelings.util.Log;
import com.zachduda.chatfeelings.util.YamlFiles;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An immutable snapshot of messages.yml. Loading migrates old keys, fills in anything missing and saves
 * the file once. Lookups fall back to the built-in default, so a missing key never produces a null.
 */
public final class Messages {
    static final int VERSION = 13;

    private final Map<String, String> values;

    private Messages(Map<String, String> values) {
        this.values = Collections.unmodifiableMap(values);
    }

    /** The message for {@code key}, falling back to its default (or "" for unknown keys). */
    public String get(String key) {
        String value = values.get(key);
        if (value == null) {
            value = DefaultMessages.VALUES.get(key);
        }
        return value == null ? "" : value;
    }

    /** The message for {@code key} if messages.yml defines one, otherwise null. For optional keys. */
    public String getIfSet(String key) {
        return values.get(key);
    }

    public static Messages defaults() {
        return new Messages(new HashMap<>(DefaultMessages.VALUES));
    }

    public static Messages load(File dataFolder) {
        final File file = new File(dataFolder, "messages.yml");
        final YamlFiles.Loaded loaded = YamlFiles.load(file);
        if (!loaded.ok()) {
            Log.warn("messages.yml has a formatting error, so default messages are being used until it's fixed. "
                    + "The file was not changed.");
            return defaults();
        }

        final YamlConfiguration yml = loaded.config();
        boolean changed = false;

        if (!loaded.existed() || !yml.contains("Version")) {
            YamlFiles.setHeader(yml, List.of(
                    "Looking for the messages used by each feeling?",
                    "They're in the Feelings folder, one file per feeling."));
            changed = true;
            if (!loaded.existed()) {
                Log.info("Created new messages.yml file...");
            }
        } else {
            changed = migrate(yml);
        }

        for (Map.Entry<String, String> def : DefaultMessages.VALUES.entrySet()) {
            changed |= setIfMissing(yml, def.getKey(), def.getValue(), true);
        }

        if (yml.getInt("Version") != VERSION) {
            yml.set("Version", VERSION);
            changed = true;
        }

        if (changed) {
            YamlFiles.save(yml, file);
        }

        Map<String, String> values = new HashMap<>();
        for (String key : yml.getKeys(true)) {
            if (!yml.isConfigurationSection(key)) {
                Object value = yml.get(key);
                if (value != null) {
                    values.put(key, String.valueOf(value));
                }
            }
        }
        return new Messages(values);
    }

    /** Upgrades older messages.yml layouts. Returns true if anything changed. */
    static boolean migrate(YamlConfiguration yml) {
        final int version = yml.getInt("Version");
        if (version == VERSION) {
            return false;
        }
        Log.info("Updating your messages.yml with new additional messages...");

        if (version < 6) {
            yml.set("Reload", DefaultMessages.VALUES.get("Reload"));
        }
        if (version < 7) {
            yml.set("Player-Is-Sleeping", null); // added in v3, removed in v7
            yml.set("No-Player-Ignore", null); // removed in v7
        }
        if (version < 10) {
            final String prefix = yml.getString("Prefix");
            if (prefix != null) {
                yml.set("Prefix", prefix + " &f"); // the space after the prefix stopped being added in code in v10
            }
        }
        if (version < 12) {
            // Early files had these keys misspelled ("Ingoring"); move them to the correct spelling.
            rename(yml, "Ingoring-On-Player", "Ignoring-On-Player");
            rename(yml, "Ingoring-Off-Player", "Ignoring-Off-Player");
            rename(yml, "Ingoring-On-All", "Ignoring-On-All");
            rename(yml, "Ingoring-Off-All", "Ignoring-Off-All");
            rename(yml, "Command_Descriptions.Wb", "Command_Descriptions.Welcomeback");
        }
        if (version < 13) {
            Log.notice("[New] Now supporting HEX color codes! We'll keep your old messages.yml as it is, but figured we'd tell you! :)");
        }
        return true;
    }

    private static void rename(YamlConfiguration yml, String from, String to) {
        final String old = yml.getString(from);
        if (old != null) {
            setIfMissing(yml, to, old, false);
            yml.set(from, null);
        }
    }

    /** Sets {@code key} if it's absent or blank (null). Returns true if it was set. */
    private static boolean setIfMissing(YamlConfiguration yml, String key, String value, boolean warnIfBlank) {
        if (!yml.contains(key)) {
            yml.set(key, value);
            return true;
        }
        if (yml.getString(key) == null) {
            if (warnIfBlank) {
                Log.warn("Replacing '" + key + "' in messages.yml, it was left blank.");
            }
            yml.set(key, value);
            return true;
        }
        return false;
    }
}
