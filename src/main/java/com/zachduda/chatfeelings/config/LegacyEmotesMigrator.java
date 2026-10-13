package com.zachduda.chatfeelings.config;

import com.zachduda.chatfeelings.util.Log;
import com.zachduda.chatfeelings.util.YamlFiles;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Locale;

/**
 * Moves feelings out of the old single emotes.yml file into one Feelings/&lt;name&gt;.yml file each.
 * Every upgrade step is applied to the same in-memory copy, which is saved once at the end, so earlier
 * steps can't be overwritten by a stale copy. Safe to run on every startup; it's a no-op once migrated.
 */
public final class LegacyEmotesMigrator {
    static final int EMOTES_VERSION = 9;

    private LegacyEmotesMigrator() {
    }

    public static void migrate(File dataFolder, File feelingsFolder) {
        final File emotesFile = new File(dataFolder, "emotes.yml");
        if (!emotesFile.exists()) {
            return;
        }
        final YamlFiles.Loaded loaded = YamlFiles.load(emotesFile);
        if (!loaded.ok()) {
            Log.warn("emotes.yml couldn't be read, so any feelings still in it weren't moved to the Feelings folder.");
            return;
        }
        final YamlConfiguration emotes = loaded.config();
        boolean changed = false;

        if (emotes.contains("Version") && emotes.getInt("Version") != EMOTES_VERSION) {
            Log.info("Updating your emotes.yml for the latest update...");
            upgrade(emotes, emotes.getInt("Version"));
            changed = true;
        }

        final ConfigurationSection legacy = emotes.getConfigurationSection("Feelings");
        if (legacy != null) {
            int migrated = 0;
            for (String key : legacy.getKeys(false)) {
                final ConfigurationSection section = legacy.getConfigurationSection(key);
                final File target = new File(feelingsFolder, key.toLowerCase(Locale.ROOT) + ".yml");
                if (section == null || target.exists()) {
                    continue; // never clobber a file that's already been split out
                }
                final YamlConfiguration out = new YamlConfiguration();
                for (String path : section.getKeys(true)) {
                    if (!section.isConfigurationSection(path)) {
                        out.set(path, section.get(path));
                    }
                }
                if (YamlFiles.save(out, target)) {
                    migrated++;
                }
            }
            if (migrated > 0) {
                Log.notice("Migrated " + migrated + " feeling(s) out of emotes.yml and into the Feelings folder.");
            }
            emotes.set("Feelings", null);
            changed = true;
        }

        if (emotes.getInt("Version") != EMOTES_VERSION) {
            emotes.set("Version", EMOTES_VERSION);
            changed = true;
        }
        if (changed) {
            YamlFiles.save(emotes, emotesFile);
        }
    }

    /** Fixes from older emotes.yml versions, applied in memory before the feelings are split out. */
    static void upgrade(YamlConfiguration emotes, int version) {
        if (version <= 4) {
            final String path = "Feelings.Welcomeback.Msgs.Sender";
            final String current = emotes.getString(path);
            if (current == null || current.equalsIgnoreCase("&7You told &#c3ff9b&l%player% welcome back!")) {
                emotes.set(path, "&7You told &#c3ff9b&l%player%&r &7welcome back!");
            }
        }
        if (version <= 3) {
            final String path = "Feelings.Bite.Msgs.Sender";
            final String current = emotes.getString(path);
            if (current != null && current.contains("info")) {
                emotes.set(path, "&7You sink your teeth into &#FF8C6B&l%player%&r&7's skin");
            }
        }
        if (version <= 5) {
            // "Wb" was renamed to "Welcomeback". Copy anything the new section is missing, then drop the old one.
            final ConfigurationSection wb = emotes.getConfigurationSection("Feelings.Wb");
            if (wb != null) {
                for (String path : wb.getKeys(true)) {
                    if (wb.isConfigurationSection(path)) {
                        continue;
                    }
                    final String target = "Feelings.Welcomeback." + path;
                    if (!emotes.contains(target) || emotes.getString(target) == null) {
                        emotes.set(target, wb.get(path));
                    }
                }
                emotes.set("Feelings.Wb", null);
            }
        }
        if (version <= 6) {
            final String path = "Feelings.Hug.Sounds.Sound1.Name";
            final String current = emotes.getString(path);
            if (current != null && current.equalsIgnoreCase("ENTITY_CAT_PURREOW")) {
                emotes.set(path, "ENTITY.CAT.PURREOW");
            }
        }
    }
}
