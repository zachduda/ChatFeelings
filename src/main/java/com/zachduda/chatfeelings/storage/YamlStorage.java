package com.zachduda.chatfeelings.storage;

import com.zachduda.chatfeelings.util.Log;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * The original storage format: one Data/&lt;uuid&gt;.yml file per player plus Data/global.yml.
 * <p>
 * Every method is synchronized so concurrent async tasks (ie: a feeling's stat update racing a
 * quit's last-on update) can't read-modify-write the same file and lose each other's changes.
 */
public class YamlStorage implements PlayerStorage {
    static final String GLOBAL_FILE = "global.yml";
    static final int FILE_VERSION = 2;

    private final File folder;

    /** Lower-cased username -> UUID, built lazily so name lookups don't re-parse every file each time. */
    private final Map<String, UUID> nameIndex = new HashMap<>();
    private final Map<String, Long> nameIndexLastOn = new HashMap<>();
    private boolean nameIndexBuilt = false;

    public YamlStorage(File dataFolder) {
        this.folder = new File(dataFolder, "Data");
    }

    @Override
    public String getName() {
        return "YAML";
    }

    File getFolder() {
        return folder;
    }

    private File playerFile(UUID uuid) {
        return new File(folder, uuid + ".yml");
    }

    private boolean ensureFolder() {
        return folder.exists() || folder.mkdirs();
    }

    private void save(FileConfiguration fc, File f) {
        if (!ensureFolder()) {
            Log.warn("Unable to create the Data folder at " + folder.getPath());
            return;
        }
        try {
            fc.save(f);
        } catch (IOException err) {
            Log.warn("Unable to save " + f.getName(), err);
        }
    }

    /**
     * Loads a player (or global stats) file to change it, decoding it the same lenient way the read path does.
     * <ul>
     *     <li>Missing file: a new, empty config.</li>
     *     <li>File that isn't valid YAML: it's moved to Data/invalid (so an admin's hand edit with a typo can be
     *     recovered), then a new, empty config if {@code freshIfMovedAside}, otherwise null.</li>
     *     <li>Anything else (the file couldn't be read, or couldn't be moved aside): null, and the caller must not
     *     write, so the file is never saved over.</li>
     * </ul>
     */
    private YamlConfiguration loadForWrite(File f, boolean freshIfMovedAside) {
        if (!f.exists()) {
            return new YamlConfiguration();
        }
        final YamlConfiguration yml = new YamlConfiguration();
        try {
            yml.load(f);
            return yml;
        } catch (InvalidConfigurationException invalid) {
            Log.warn(f.getName() + " isn't valid YAML: " + invalid.getMessage());
            return quarantine(f) && freshIfMovedAside ? new YamlConfiguration() : null;
        } catch (IOException unreadable) {
            Log.warn("Couldn't read " + f.getName() + ", so it wasn't changed: " + unreadable.getMessage());
            return null;
        }
    }

    /** Moves a player file into Data/invalid, keeping any earlier file of the same name. */
    private boolean quarantine(File f) {
        final File invalidFolder = new File(folder, "invalid");
        if (!invalidFolder.exists() && !invalidFolder.mkdirs()) {
            Log.warn("Unable to create " + invalidFolder.getPath() + " for invalid player files.");
            return false;
        }
        File target = new File(invalidFolder, f.getName());
        if (target.exists()) {
            target = new File(invalidFolder, f.getName() + "." + System.currentTimeMillis());
        }
        if (f.renameTo(target)) {
            Log.warn("Moved unreadable player file " + f.getName() + " to Data/invalid/" + target.getName() + ".");
            return true;
        }
        Log.warn("Unable to move unreadable player file " + f.getName() + " to Data/invalid.");
        return false;
    }

    /** Parses a player file, or returns null if it's missing or lacks the required keys. */
    static PlayerData parse(FileConfiguration fc) {
        final String uuidStr = fc.getString("UUID");
        final String username = fc.getString("Username");
        if (uuidStr == null || username == null || !fc.contains("Last-On")) {
            return null;
        }

        final UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException badUuid) {
            return null;
        }

        Map<String, Integer> stats = new HashMap<>();
        ConfigurationSection sent = fc.getConfigurationSection("Stats.Sent");
        if (sent != null) {
            for (String key : sent.getKeys(false)) {
                if (!key.equals("Total")) {
                    stats.put(key, sent.getInt(key));
                }
            }
        }

        return new PlayerData(uuid, username, fc.getString("IP"), fc.getLong("Last-On"),
                fc.getBoolean("Allow-Feelings", true), fc.getBoolean("Muted", false),
                new LinkedHashSet<>(fc.getStringList("Ignoring")), stats, fc.getInt("Stats.Sent.Total"));
    }

    private List<File> playerFiles() {
        List<File> out = new ArrayList<>();
        File[] files = folder.listFiles();
        if (files == null) {
            return out;
        }
        for (File f : files) {
            final String name = f.getName();
            // Skip global stats and OS junk such as macOS' .DS_Store.
            if (f.isFile() && name.toLowerCase(Locale.ROOT).endsWith(".yml") && !name.equalsIgnoreCase(GLOBAL_FILE)) {
                out.add(f);
            }
        }
        return out;
    }

    private void indexName(String username, UUID uuid, long lastOn) {
        final String key = username.toLowerCase(Locale.ROOT);
        final Long existing = nameIndexLastOn.get(key);
        if (existing == null || lastOn >= existing) {
            nameIndex.put(key, uuid);
            nameIndexLastOn.put(key, lastOn);
        }
    }

    private void buildNameIndex() {
        if (nameIndexBuilt) {
            return;
        }
        for (File f : playerFiles()) {
            PlayerData data = parse(YamlConfiguration.loadConfiguration(f));
            if (data != null) {
                indexName(data.getUsername(), data.getUuid(), data.getLastOn());
            }
        }
        nameIndexBuilt = true;
    }

    private void unindex(UUID uuid) {
        nameIndex.entrySet().removeIf(e -> {
            if (e.getValue().equals(uuid)) {
                nameIndexLastOn.remove(e.getKey());
                return true;
            }
            return false;
        });
    }

    @Override
    public synchronized PlayerData load(UUID uuid) {
        File f = playerFile(uuid);
        if (!f.exists()) {
            return null;
        }
        return parse(YamlConfiguration.loadConfiguration(f));
    }

    @Override
    public synchronized UUID findUUIDByName(String username) {
        buildNameIndex();
        return nameIndex.get(username.toLowerCase(Locale.ROOT));
    }

    @Override
    public synchronized void recordLogin(UUID uuid, String username, String ip, long lastOn) {
        File f = playerFile(uuid);
        final FileConfiguration fc = loadForWrite(f, true);
        if (fc == null) {
            return;
        }

        // Only set defaults for brand-new (or pre-mute-system) files, never reset an existing player's choices.
        if (!fc.contains("UUID")) {
            fc.set("UUID", uuid.toString());
        }
        if (!fc.contains("Allow-Feelings")) {
            fc.set("Allow-Feelings", true);
        }
        if (!fc.contains("Muted")) {
            fc.set("Muted", false);
        }
        fc.set("Version", FILE_VERSION);
        fc.set("IP", ip);
        fc.set("Username", username);
        fc.set("Last-On", lastOn);
        save(fc, f);

        if (nameIndexBuilt) {
            unindex(uuid);
            indexName(username, uuid, lastOn);
        }
    }

    @Override
    public synchronized boolean setMuted(UUID uuid, boolean muted) {
        File f = playerFile(uuid);
        if (!f.exists()) {
            return false;
        }
        FileConfiguration fc = loadForWrite(f, false);
        if (fc == null) {
            return false;
        }
        fc.set("Muted", muted);
        save(fc, f);
        return true;
    }

    @Override
    public synchronized Boolean toggleAllowFeelings(UUID uuid) {
        File f = playerFile(uuid);
        if (!f.exists()) {
            return null;
        }
        FileConfiguration fc = loadForWrite(f, false);
        if (fc == null) {
            return null;
        }
        final boolean allow = !fc.getBoolean("Allow-Feelings", true);
        fc.set("Allow-Feelings", allow);
        save(fc, f);
        return allow;
    }

    @Override
    public synchronized Boolean toggleIgnoring(UUID owner, UUID target) {
        File f = playerFile(owner);
        if (!f.exists()) {
            return null;
        }
        FileConfiguration fc = loadForWrite(f, false);
        if (fc == null) {
            return null;
        }
        List<String> ignoring = new ArrayList<>(fc.getStringList("Ignoring"));
        final String t = target.toString();
        final boolean nowIgnoring;
        if (ignoring.remove(t)) {
            nowIgnoring = false;
        } else {
            ignoring.add(t);
            nowIgnoring = true;
        }
        fc.set("Ignoring", ignoring);
        save(fc, f);
        return nowIgnoring;
    }

    @Override
    public synchronized void incrementSent(UUID uuid, String capitalizedFeeling) {
        File f = playerFile(uuid);
        if (!f.exists()) {
            return;
        }
        FileConfiguration fc = loadForWrite(f, false);
        if (fc == null) {
            return;
        }
        fc.set("Stats.Sent." + capitalizedFeeling, fc.getInt("Stats.Sent." + capitalizedFeeling) + 1);
        fc.set("Stats.Sent.Total", fc.getInt("Stats.Sent.Total") + 1);
        save(fc, f);
    }

    @Override
    public synchronized void incrementGlobalSent(String capitalizedFeeling) {
        File f = new File(folder, GLOBAL_FILE);
        final FileConfiguration fc = loadForWrite(f, true);
        if (fc == null) {
            return;
        }
        fc.set("Feelings.Sent." + capitalizedFeeling, fc.getInt("Feelings.Sent." + capitalizedFeeling) + 1);
        save(fc, f);
    }

    @Override
    public synchronized Map<String, Integer> getGlobalSent() {
        return readGlobalSent(new File(folder, GLOBAL_FILE));
    }

    static Map<String, Integer> readGlobalSent(File f) {
        Map<String, Integer> out = new HashMap<>();
        if (!f.exists()) {
            return out;
        }
        ConfigurationSection sent = YamlConfiguration.loadConfiguration(f).getConfigurationSection("Feelings.Sent");
        if (sent != null) {
            for (String key : sent.getKeys(false)) {
                out.put(key, sent.getInt(key));
            }
        }
        return out;
    }

    @Override
    public synchronized List<PlayerData> loadAll() {
        List<PlayerData> out = new ArrayList<>();
        for (File f : playerFiles()) {
            PlayerData data = parse(YamlConfiguration.loadConfiguration(f));
            if (data != null) {
                out.add(data);
            }
        }
        return out;
    }

    @Override
    public synchronized void delete(UUID uuid) {
        File f = playerFile(uuid);
        if (f.exists() && !f.delete()) {
            Log.debug("Unable to delete player file: " + f.getName());
        }
        unindex(uuid);
    }

    @Override
    public synchronized boolean deleteIfUnchanged(UUID uuid, long expectedLastOn) {
        final PlayerData current = load(uuid);
        if (current == null || current.getLastOn() != expectedLastOn) {
            return false;
        }
        delete(uuid);
        return true;
    }

    /**
     * Moves unreadable or incomplete player files into Data/invalid instead of deleting them, so a file that
     * was damaged (ie: by a crash mid-save) can still be recovered by hand.
     */
    @Override
    public synchronized int cleanupInvalid() {
        int moved = 0;
        for (File f : playerFiles()) {
            if (parse(YamlConfiguration.loadConfiguration(f)) == null && quarantine(f)) {
                moved++;
            }
        }
        if (moved > 0) {
            Log.info("Moved " + moved + " unreadable player file(s) to Data/invalid.");
        }
        return moved;
    }
}
