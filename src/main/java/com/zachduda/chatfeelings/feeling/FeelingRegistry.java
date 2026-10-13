package com.zachduda.chatfeelings.feeling;

import com.zachduda.chatfeelings.config.LegacyEmotesMigrator;
import com.zachduda.chatfeelings.effect.ParticlePresets;
import com.zachduda.chatfeelings.util.Log;
import com.zachduda.chatfeelings.util.Text;
import com.zachduda.chatfeelings.util.YamlFiles;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Loads the Feelings folder: one file per feeling, built-in or custom. Each {@link #reload()} parses every
 * file once and swaps in an immutable snapshot, so nothing reads feeling files from disk while running.
 */
public final class FeelingRegistry {
    private final File dataFolder;
    private final File folder;
    private final SoundResolver sounds;

    private volatile Map<String, Feeling> feelings = Collections.emptyMap();

    public FeelingRegistry(File dataFolder, SoundResolver sounds) {
        this.dataFolder = dataFolder;
        this.folder = new File(dataFolder, "Feelings");
        this.sounds = sounds;
    }

    public Feeling get(String name) {
        return name == null ? null : feelings.get(name.toLowerCase(Locale.ROOT));
    }

    public boolean isFeeling(String name) {
        return get(name) != null;
    }

    /** Every usable feeling: built-ins first (in their usual order), then custom ones alphabetically. */
    public Collection<Feeling> all() {
        return feelings.values();
    }

    public List<String> names() {
        return List.copyOf(feelings.keySet());
    }

    public List<Feeling> custom() {
        return feelings.values().stream().filter(f -> !f.builtin()).toList();
    }

    /** Reads every feeling file, writing defaults for built-ins first. Does file I/O, so not on a tick thread. */
    public void reload() {
        if (!folder.exists() && !folder.mkdirs()) {
            Log.warn("Unable to create the Feelings folder at " + folder.getPath());
        }
        LegacyEmotesMigrator.migrate(dataFolder, folder);

        final Map<String, Feeling> loaded = new LinkedHashMap<>();
        for (String name : BuiltinFeelings.NAMES) {
            loaded.put(name, loadBuiltin(BuiltinFeelings.get(name)));
        }
        for (Map.Entry<String, File> custom : discoverCustomFiles().entrySet()) {
            final Feeling feeling = loadCustom(custom.getKey(), custom.getValue());
            if (feeling != null) {
                loaded.put(feeling.name(), feeling);
            }
        }
        feelings = Collections.unmodifiableMap(loaded);
    }

    private Feeling loadBuiltin(BuiltinFeelings.Defaults defaults) {
        final File file = new File(folder, defaults.name() + ".yml");
        final YamlFiles.Loaded loaded = YamlFiles.load(file);
        YamlConfiguration fc = loaded.config();
        if (loaded.ok()) {
            if (writeDefaults(fc, defaults, !loaded.existed())) {
                YamlFiles.save(fc, file);
            }
        } else {
            Log.warn("Feelings/" + file.getName() + " has a formatting error, so /" + defaults.name()
                    + " uses its default messages until it's fixed. The file was not changed.");
            fc = new YamlConfiguration();
            writeDefaults(fc, defaults, true);
        }
        return parse(defaults.name(), fc, defaults);
    }

    /** Adds whatever's missing from a built-in feeling's file. Returns true if the file needs saving. */
    private boolean writeDefaults(YamlConfiguration fc, BuiltinFeelings.Defaults d, boolean isNew) {
        boolean changed = isNew;
        changed |= setIfMissing(fc, "Enable", true);
        changed |= setIfMissing(fc, "Msgs.Sender", d.sender());
        changed |= setIfMissing(fc, "Msgs.Target", d.target());
        changed |= setIfMissing(fc, "Msgs.Global", d.global());
        changed |= setIfMissing(fc, "Sounds.Sound1.Name", availableSound(d.sound1()));
        changed |= setIfMissing(fc, "Sounds.Sound1.Volume", d.volume1());
        changed |= setIfMissing(fc, "Sounds.Sound1.Pitch", d.pitch1());
        changed |= setIfMissing(fc, "Sounds.Sound2.Name", availableSound(d.sound2()));
        changed |= setIfMissing(fc, "Sounds.Sound2.Volume", d.volume2());
        changed |= setIfMissing(fc, "Sounds.Sound2.Pitch", d.pitch2());
        changed |= setIfMissing(fc, "Particle", d.particle());
        changed |= setIfMissing(fc, "Violent", d.violent());
        return changed;
    }

    /** The default sound, or its fallback if this server is too old to have it. */
    private String availableSound(String soundName) {
        if (SoundResolver.isSilent(soundName) || sounds.exists(soundName)) {
            return soundName;
        }
        final String fallback = BuiltinFeelings.fallbackSound(soundName);
        return fallback != null ? fallback : soundName;
    }

    private static boolean setIfMissing(ConfigurationSection fc, String path, Object value) {
        if (!fc.contains(path)) {
            fc.set(path, value);
            return true;
        }
        if (value instanceof String && fc.getString(path) == null) {
            Log.warn("Replacing '" + path + "' in a Feelings file, it was left blank.");
            fc.set(path, value);
            return true;
        }
        return false;
    }

    /**
     * Finds custom feeling files, keyed by lower-case name. Built-ins and unreleased placeholders are skipped,
     * and when two files only differ by case (possible on Linux) only the first is used.
     */
    private Map<String, File> discoverCustomFiles() {
        final File[] files = folder.listFiles((dir, filename) -> filename.toLowerCase(Locale.ROOT).endsWith(".yml"));
        final Map<String, File> byName = new LinkedHashMap<>();
        if (files == null) {
            return byName;
        }
        Arrays.sort(files, Comparator.comparing(File::getName));
        for (File file : files) {
            final String base = file.getName().substring(0, file.getName().length() - 4);
            final String name = base.toLowerCase(Locale.ROOT);
            if (BuiltinFeelings.isBuiltin(name) || BuiltinFeelings.RESERVED_UNRELEASED.contains(name)) {
                continue;
            }
            final File existing = byName.putIfAbsent(name, file);
            if (existing != null) {
                Log.warn("Feelings/" + existing.getName() + " and Feelings/" + file.getName()
                        + " would both be /" + name + ". Only " + existing.getName() + " is used; rename or remove the other.");
            }
        }
        return byName;
    }

    private Feeling loadCustom(String name, File file) {
        final String base = file.getName().substring(0, file.getName().length() - 4);
        final YamlFiles.Loaded loaded = YamlFiles.load(file);
        if (!loaded.ok()) {
            Log.warn("Skipping custom feeling '" + base + "': Feelings/" + file.getName() + " has a formatting error.");
            return null;
        }
        final List<String> errors = FeelingValidator.validate(base, loaded.config(), sounds::exists, ParticlePresets::isValid);
        if (!errors.isEmpty()) {
            Log.warn("Skipping invalid custom feeling '" + base + "' in Feelings/" + file.getName() + ":");
            for (String error : errors) {
                Log.warn("   - " + error);
            }
            return null;
        }
        Log.info("Loaded custom feeling: /" + name);
        return parse(name, loaded.config(), null);
    }

    /** @param defaults the built-in defaults, or null for a custom feeling */
    private Feeling parse(String name, ConfigurationSection fc, BuiltinFeelings.Defaults defaults) {
        final String displayName = Text.capitalize(name);
        final String particle = ParticlePresets.normalize(fc.getString("Particle", defaults != null ? defaults.particle() : "None"));
        if (particle == null) {
            Log.warn("Feelings/" + name + ".yml: 'Particle' isn't a known preset, so it won't show particles.");
        }
        return new Feeling(
                name,
                displayName,
                defaults != null,
                fc.getBoolean("Enable", defaults != null),
                fc.getString("Msgs.Sender", defaults != null ? defaults.sender() : ""),
                fc.getString("Msgs.Target", defaults != null ? defaults.target() : ""),
                fc.getString("Msgs.Global", defaults != null ? defaults.global() : ""),
                blankToNull(fc.getString("Description")),
                parseSound(name, fc, "Sounds.Sound1"),
                parseSound(name, fc, "Sounds.Sound2"),
                particle == null || particle.equals(ParticlePresets.NONE) ? null : particle,
                fc.getBoolean("Violent", defaults != null && defaults.violent()),
                fc.getString("Stats-Label", defaults != null ? defaults.statsLabel() : displayName + "s"));
    }

    private FeelingSound parseSound(String feeling, ConfigurationSection fc, String base) {
        final String soundName = fc.getString(base + ".Name");
        if (SoundResolver.isSilent(soundName)) {
            return FeelingSound.NONE;
        }
        final Sound sound = sounds.resolve(soundName);
        if (sound == null) {
            Log.warn("Feelings/" + feeling + ".yml: '" + base + ".Name' (" + soundName
                    + ") isn't a sound on this server, so it won't play.");
        }
        return new FeelingSound(soundName, sound, (float) fc.getDouble(base + ".Volume", 1.0),
                (float) fc.getDouble(base + ".Pitch", 1.0));
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
