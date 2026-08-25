package com.zachduda.chatfeelings;

import com.zachduda.chatfeelings.other.Supports;
import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;

public class FileSetup {
    private static final Main plugin = Main.getPlugin(Main.class);

    /** Sub-folder of the plugin's data folder that holds one .yml file per feeling. */
    private static final String FEELINGS_FOLDER = "Feelings";

    /** A feeling's file name (minus ".yml") doubles as its command name, so it must be a safe/sane command name. */
    private static final Pattern VALID_FEELING_NAME = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_-]{0,31}$");

    /** Names ChatFeelings already uses for its own commands. A feeling can never claim one of these. */
    private static final Set<String> RESERVED_FEELING_NAMES = Set.of("chatfeelings", "cf", "feelings", "emotions", "help");

    /** Built-in feelings that are intentionally scaffolded but not yet wired up as usable commands. */
    private static final Set<String> INACTIVE_BUILTIN_FEELINGS = Set.of("knock");

    private static boolean saveFile(FileConfiguration fc, File f) {
        try {
            fc.save(f);
            return true;
        } catch (Exception err) {
            Main.log("[!] Failed to save file changes. See error below:", true, true);
            err.printStackTrace();
            return false;
        }
    }

    private static File getFolder() {
        return Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("ChatFeelings")).getDataFolder();
    }

    private static void setMsgs(String configpath, String msg) {
        File msgsfile = new File(getFolder(), File.separator + "messages.yml");
        FileConfiguration msgs;
        try {
            msgs = YamlConfiguration.loadConfiguration(new InputStreamReader(Files.newInputStream(msgsfile.toPath()), StandardCharsets.UTF_8));
        } catch (IOException e) {
            if(Main.debug) {
                Main.debug("Unable to decode or create messages.yml file:");
                throw new RuntimeException(e);
            } else {
                Main.log("There was an error when trying to modify or create your messages.yml", true, true);
                return;
            }
        }

        if (!msgsfile.exists()) {
            saveFile(msgs, msgsfile);
        }

        if (!msgs.contains(configpath)) {
            msgs.set(configpath, msg);
        } else if (msgs.getString(configpath) == null) {
            Main.log("Replacing '" + configpath + " in messages.yml, it was left blank.", false, true);
            msgs.set(configpath, msg);
        }

        saveFile(msgs, msgsfile);
    }

    private static void forceMsgs(String configpath, String msg) {
        File msgsfile = new File(getFolder(), File.separator + "messages.yml");
        FileConfiguration msgs;
        try {
            msgs = YamlConfiguration.loadConfiguration(new InputStreamReader(Files.newInputStream(msgsfile.toPath()), StandardCharsets.UTF_8));
        } catch (IOException e) {
            if(Main.debug) {
                Main.debug("Unable to decode or create messages.yml file:");
                throw new RuntimeException(e);
            } else {
                Main.log("There was an error when trying to modify or create your messages.yml", true, true);
                return;
            }
        }

        if (!msgsfile.exists()) {
            saveFile(msgs, msgsfile);
        }

        msgs.set(configpath, msg);
        saveFile(msgs, msgsfile);
    }

    private static void setMsgsVersion(int vers) {
        File msgsfile = new File(getFolder(), File.separator + "messages.yml");
        FileConfiguration msgs;
        try {
            msgs = YamlConfiguration.loadConfiguration(new InputStreamReader(Files.newInputStream(msgsfile.toPath()), StandardCharsets.UTF_8));
        } catch (IOException e) {
            if(Main.debug) {
                Main.debug("Unable to decode or create messages.yml file:");
                throw new RuntimeException(e);
            } else {
                Main.log("There was an error when trying to modify or create your messages.yml", true, true);
                return;
            }
        }

        if (!msgs.contains("Version") || msgs.getInt("Version") != vers) {
            msgs.set("Version", vers);
            saveFile(msgs, msgsfile);
        }
    }

    private static void forceEmotes(String configpath, String msg) {
        File emotesfile = new File(getFolder(), File.separator + "emotes.yml");
        FileConfiguration emotes;
        try {
            emotes = YamlConfiguration.loadConfiguration(new InputStreamReader(Files.newInputStream(emotesfile.toPath()), StandardCharsets.UTF_8));
        } catch (IOException e) {
            if(Main.debug) {
                Main.debug("Unable to decode or create emotes.yml file:");
                throw new RuntimeException(e);
            } else {
                Main.log("There was an error when trying to modify or create your emotes.yml", true, true);
                return;
            }
        }

        if (!emotesfile.exists()) {
            saveFile(emotes, emotesfile);
        }

        emotes.set(configpath, msg);
        saveFile(emotes, emotesfile);
    }

    private static void setEmotes(String configpath, String msg) {
        File emotesfile = new File(getFolder(), File.separator + "emotes.yml");
        FileConfiguration emotes;
        try {
            emotes = YamlConfiguration.loadConfiguration(new InputStreamReader(Files.newInputStream(emotesfile.toPath()), StandardCharsets.UTF_8));
        } catch (IOException e) {
            if(Main.debug) {
                Main.debug("Unable to decode or create emotes.yml file:");
                throw new RuntimeException(e);
            } else {
                Main.log("There was an error when trying to modify or create your emotes.yml", true, true);
                return;
            }
        }

        if (!emotesfile.exists()) {
            saveFile(emotes, emotesfile);
        }

        if (!emotes.contains(configpath)) {
            emotes.set(configpath, msg);
        } else {
            if (emotes.getString(configpath) == null) {
                plugin.getLogger().warning("Replacing '" + configpath + " in emotes.yml, it was left blank.");
                emotes.set(configpath, msg);
            }
        }

        saveFile(emotes, emotesfile);
    }

    private static void setEmotesVersion(int vers) {
        File emotesfile = new File(getFolder(), File.separator + "emotes.yml");
        FileConfiguration emotes = YamlConfiguration.loadConfiguration(emotesfile);

        if (!emotesfile.exists()) {
            saveFile(emotes, emotesfile);
        }

        if (!emotes.contains("Version") || emotes.getInt("Version") != vers) {
            emotes.set("Version", vers);
            saveFile(emotes, emotesfile);
        }
    }

    private static void setEmotesDouble(String configpath, Double dubdub) {
        File emotesfile = new File(getFolder(), File.separator + "emotes.yml");
        FileConfiguration emotes = YamlConfiguration.loadConfiguration(emotesfile);
        if (!emotesfile.exists()) {
            saveFile(emotes, emotesfile);
        }

        if (!emotes.contains(configpath)) {
            emotes.set(configpath, dubdub);
        } else if (emotes.getString(configpath) == null) {
            plugin.getLogger().warning("Replacing '" + configpath + " (double) in emotes.yml, it was left blank.");
            emotes.set(configpath, dubdub);
        }
        saveFile(emotes, emotesfile);
    }

    private static void setEmotesBoolean(String configpath, boolean siono) {
        File emotesfile = new File(getFolder(), File.separator + "emotes.yml");
        FileConfiguration emotes = YamlConfiguration.loadConfiguration(emotesfile);
        if (!emotesfile.exists()) {
            saveFile(emotes, emotesfile);
        }

        if (!emotes.contains(configpath)) {
            emotes.set(configpath, siono);
        } else if (emotes.getString(configpath) == null) {
            plugin.getLogger().warning("Replacing '" + configpath + " (boolean) in emotes.yml, it was left blank.");
            emotes.set(configpath, siono);
        }
        saveFile(emotes, emotesfile);
    }

    private static boolean validSound(String sound) {
        try {
            return Registry.SOUNDS.match(sound) != null;
        } catch(Exception e) {
            return false;
        }
    }

    private static boolean isSilentSound(String sound) {
        return sound.equalsIgnoreCase("none") || sound.equalsIgnoreCase("off") || sound.equalsIgnoreCase("null");
    }

    // ---------------------------------------------------------------------
    // Feelings/ folder: one .yml file per feeling (built-in or custom).
    // ---------------------------------------------------------------------

    private static File getFeelingsFolder() {
        File folder = new File(getFolder(), File.separator + FEELINGS_FOLDER);
        if (!folder.exists() && !folder.mkdirs()) {
            Main.log("Unable to create the Feelings folder at " + folder.getPath(), true, true);
        }
        return folder;
    }

    private static File getFeelingFile(String name) {
        return new File(getFeelingsFolder(), File.separator + name.toLowerCase(Locale.ROOT) + ".yml");
    }

    /** Always returns a usable (possibly empty) config, so callers don't need to null-check a missing/deleted file. */
    public static FileConfiguration loadFeelingConfig(String name) {
        return YamlConfiguration.loadConfiguration(getFeelingFile(name));
    }

    public static boolean getFeelingBoolean(String name, String path) {
        return loadFeelingConfig(name).getBoolean(path);
    }

    public static String getFeelingString(String name, String path) {
        return loadFeelingConfig(name).getString(path);
    }

    public static double getFeelingDouble(String name, String path) {
        return loadFeelingConfig(name).getDouble(path);
    }

    private static boolean setIfMissing(FileConfiguration fc, String path, String value) {
        if (!fc.contains(path)) {
            fc.set(path, value);
            return true;
        }
        if (fc.getString(path) == null) {
            Main.log("Replacing '" + path + "' in a Feelings/ file, it was left blank.", false, true);
            fc.set(path, value);
            return true;
        }
        return false;
    }

    private static boolean setIfMissing(FileConfiguration fc, String path, double value) {
        if (!fc.contains(path)) {
            fc.set(path, value);
            return true;
        }
        return false;
    }

    private static boolean setIfMissing(FileConfiguration fc, String path, boolean value) {
        if (!fc.contains(path)) {
            fc.set(path, value);
            return true;
        }
        return false;
    }

    /**
     * Writes the default values for one of ChatFeelings' built-in feelings into Feelings/&lt;name&gt;.yml,
     * without touching anything a server admin has already customized.
     */
    private static void writeFeelingDefaults(String name, boolean enable,
                                              String senderMsg, String targetMsg, String globalMsg,
                                              String sound1Name, double sound1Volume, double sound1Pitch,
                                              String sound2Name, double sound2Volume, double sound2Pitch) {
        File f = getFeelingFile(name);
        FileConfiguration fc = YamlConfiguration.loadConfiguration(f);
        boolean changed = !f.exists();

        changed |= setIfMissing(fc, "Enable", enable);
        changed |= setIfMissing(fc, "Msgs.Sender", senderMsg);
        changed |= setIfMissing(fc, "Msgs.Target", targetMsg);
        changed |= setIfMissing(fc, "Msgs.Global", globalMsg);
        changed |= setIfMissing(fc, "Sounds.Sound1.Name", sound1Name);
        changed |= setIfMissing(fc, "Sounds.Sound1.Volume", sound1Volume);
        changed |= setIfMissing(fc, "Sounds.Sound1.Pitch", sound1Pitch);
        changed |= setIfMissing(fc, "Sounds.Sound2.Name", sound2Name);
        changed |= setIfMissing(fc, "Sounds.Sound2.Volume", sound2Volume);
        changed |= setIfMissing(fc, "Sounds.Sound2.Pitch", sound2Pitch);

        if (changed) {
            saveFile(fc, f);
        }
    }

    /**
     * Moves any "Feelings.*" entries left over in an old-style emotes.yml into their own file
     * inside the Feelings folder. Safe to call every startup: it's a no-op once migrated.
     */
    private static void migrateLegacyFeelingsToFolder(FileConfiguration emotes, File emotesfile) {
        ConfigurationSection legacy = emotes.getConfigurationSection("Feelings");
        if (legacy == null) {
            return;
        }

        int migrated = 0;
        for (String key : legacy.getKeys(false)) {
            File target = getFeelingFile(key);
            if (target.exists()) {
                continue; // don't clobber a file that's already been split out
            }

            ConfigurationSection section = legacy.getConfigurationSection(key);
            if (section == null) {
                continue;
            }

            FileConfiguration fc = new YamlConfiguration();
            for (String path : section.getKeys(true)) {
                if (!section.isConfigurationSection(path)) {
                    fc.set(path, section.get(path));
                }
            }

            if (saveFile(fc, target)) {
                migrated++;
            }
        }

        if (migrated > 0) {
            Main.log("Migrated " + migrated + " feeling(s) out of emotes.yml and into the new Feelings/ folder.", true, false);
        }

        emotes.set("Feelings", null);
        saveFile(emotes, emotesfile);
    }

    // ---------------------------------------------------------------------
    // Custom feeling validation & discovery
    // ---------------------------------------------------------------------

    private static List<String> validateMessage(FileConfiguration fc, String path) {
        List<String> errors = new ArrayList<>();
        String value = fc.getString(path);
        if (value == null || value.isBlank()) {
            errors.add("Missing or empty '" + path + "'.");
        }
        return errors;
    }

    private static List<String> validateSound(FileConfiguration fc, String base) {
        List<String> errors = new ArrayList<>();
        String name = fc.getString(base + ".Name");

        if (name == null || name.isBlank()) {
            errors.add("Missing '" + base + ".Name' (use 'None' if you don't want this sound).");
            return errors;
        }
        if (!isSilentSound(name) && !validSound(name)) {
            errors.add("'" + base + ".Name' isn't a recognized sound: " + name);
        }

        if (!fc.isSet(base + ".Volume") || !(fc.get(base + ".Volume") instanceof Number)) {
            errors.add("Missing or invalid '" + base + ".Volume' (must be a number).");
        } else if (fc.getDouble(base + ".Volume") < 0 || fc.getDouble(base + ".Volume") > 10) {
            errors.add("'" + base + ".Volume' must be between 0 and 10.");
        }

        if (!fc.isSet(base + ".Pitch") || !(fc.get(base + ".Pitch") instanceof Number)) {
            errors.add("Missing or invalid '" + base + ".Pitch' (must be a number).");
        } else if (fc.getDouble(base + ".Pitch") < 0 || fc.getDouble(base + ".Pitch") > 2) {
            errors.add("'" + base + ".Pitch' must be between 0 and 2.");
        }

        return errors;
    }

    /** Validates that a feeling's name and file contents are safe & complete enough to register as a command. */
    static List<String> validateFeelingConfig(String name, FileConfiguration fc) {
        List<String> errors = new ArrayList<>();

        if (!VALID_FEELING_NAME.matcher(name).matches()) {
            errors.add("Invalid name: must start with a letter and contain only letters, numbers, '-' or '_' (max 32 characters).");
        }
        if (RESERVED_FEELING_NAMES.contains(name.toLowerCase(Locale.ROOT))) {
            errors.add("'" + name + "' is reserved for a ChatFeelings command and can't be used as a feeling name.");
        }

        if (!fc.isBoolean("Enable")) {
            errors.add("Missing or invalid 'Enable' (must be true or false).");
        }

        errors.addAll(validateMessage(fc, "Msgs.Sender"));
        errors.addAll(validateMessage(fc, "Msgs.Target"));
        errors.addAll(validateMessage(fc, "Msgs.Global"));

        errors.addAll(validateSound(fc, "Sounds.Sound1"));
        errors.addAll(validateSound(fc, "Sounds.Sound2"));

        return errors;
    }

    /**
     * Scans the Feelings folder for any .yml file that isn't a built-in feeling, validates it,
     * and returns the names of the ones that are safe to register as commands. Invalid files are
     * logged and skipped rather than breaking plugin startup.
     */
    private static List<String> discoverCustomFeelings() {
        List<String> found = new ArrayList<>();
        File[] files = getFeelingsFolder().listFiles((dir, filename) -> filename.toLowerCase(Locale.ROOT).endsWith(".yml"));

        if (files == null) {
            return found;
        }

        for (File f : files) {
            String name = f.getName().substring(0, f.getName().length() - 4);
            String lower = name.toLowerCase(Locale.ROOT);

            if (Main.BUILTIN_FEELINGS.contains(lower) || INACTIVE_BUILTIN_FEELINGS.contains(lower)) {
                continue; // already handled as a built-in (or intentionally inactive) feeling
            }

            FileConfiguration fc;
            try {
                fc = YamlConfiguration.loadConfiguration(new InputStreamReader(Files.newInputStream(f.toPath()), StandardCharsets.UTF_8));
            } catch (IOException e) {
                Main.log("Skipping custom feeling '" + name + "': couldn't read Feelings/" + f.getName() + " (" + e.getMessage() + ")", true, true);
                continue;
            }

            List<String> errors = validateFeelingConfig(name, fc);
            if (!errors.isEmpty()) {
                Main.log("Skipping invalid custom feeling '" + name + "' in Feelings/" + f.getName() + ":", true, true);
                for (String error : errors) {
                    Main.log("   - " + error, true, true);
                }
                continue;
            }

            found.add(lower);
            Main.log("Loaded custom feeling: /" + lower, false, false);
        }

        return found;
    }

    @SuppressWarnings("SpellCheckingInspection")
    static void enableFiles() {
        File folder = getFolder();

        File msgsfile = new File(folder, File.separator + "messages.yml");
        FileConfiguration msgs = YamlConfiguration.loadConfiguration(msgsfile);

        File emotesfile = new File(folder, File.separator + "emotes.yml");
        FileConfiguration emotes = YamlConfiguration.loadConfiguration(emotesfile);

        final int msgfilever = 13;
        if (!msgsfile.exists() || !msgs.contains("Version")) {

            List<String> confighead = new ArrayList<>();
            confighead.add("Looking for the messages used for feelings?");
            confighead.add("Check inside your emotes.yml!");

            try {
                msgs.options().setHeader(confighead);
            } catch (NoSuchMethodError e) {
                // Using less than Java 18 will use this method instead.
                try {
                    msgs.options().header("Looking for the messages used for feelings? Check the emotes.yml!");
                } catch (Exception giveup) { /* just skip this */ }
            }

            if (saveFile(msgs, msgsfile)) {
                if(!Main.reducemsgs) {
                    plugin.getLogger().info("Created new messages.yml file...");
                }
            }

        } else {
            final int currentmsgv = msgs.getInt("Version");
            if (currentmsgv != msgfilever) {
                Main.log("Updating your messages.yml with new additional messages...", false, false);
            }
            if (currentmsgv < 6) {
                forceMsgs("Reload", "&8&l> &#c3ff9b&l✓  &7Plugin reloaded in &f%time%");
            }

            if (currentmsgv < 7) {
                forceMsgs("Player-Is-Sleeping", null); // added in v3, removed in v7
                forceMsgs("No-Player-Ignore", null); // removed in v7
            }

            if (currentmsgv < 10) {
                forceMsgs("Prefix", msgs.getString("Prefix") + " &f"); // removed space in prefix internally in v10
            }

            if (currentmsgv < 12) {
                // Was also v11 but had auto correct causing upgrade issues, bump to v12 - 8/18/24
                // Typo in file, move old variables to correctly spelled one.
                // INTENTIONALLY MISTYPED AS INGORING TO CORRECT TO IGNORING 
                if (msgs.getString("Ingoring-On-Player") != null) {
                    setMsgs("Ignoring-On-Player", msgs.getString("Ingoring-On-Player"));
                    forceMsgs("Ingoring-On-Player", null);
                }
                if (msgs.getString("Ingoring-Off-Player") != null) {
                    setMsgs("Ignoring-Off-Player", msgs.getString("Ingoring-Off-Player"));
                    forceMsgs("Ingoring-Off-Player", null);
                }

                if (msgs.getString("Ingoring-On-All") != null) {
                    setMsgs("Ignoring-On-All", msgs.getString("Ingoring-Off-Player"));
                    forceMsgs("Ingoring-On-Player", null);
                }

                if (msgs.getString("Ingoring-Off-All") != null) {
                    setMsgs("Ignoring-Off-All", msgs.getString("Ingoring-Off-Player"));
                    forceMsgs("Ingoring-Off-Player", null);
                }
                // Wb -> Welcome Back
                if (msgs.getString("Command_Descriptions.Wb") != null) {
                    setMsgs("Command_Descriptions.Welcomeback", msgs.getString("Command_Descriptions.Wb"));
                    forceMsgs("Command_Descriptions.Wb", null);
                }
            }
            if (currentmsgv < 13) {
                Main.log("[New] Now supporting HEX color codes! We'll keep your old messages.yml as it is, but figured we'd tell you! :)", true, true);
            }
        }

        setMsgs("Prefix", "&#c3ff9b&lC&r&#c3ff9bhat&r&f&lF&r&feelings &8&l┃ &f");
        setMsgs("Prefix-Header", "&#c3ff9b&lC&r&#c3ff9bhat &r&f&lF&r&feelings");
        setMsgs("Reload", "&8&l> &#c3ff9b&l✓  &7Plugin reloaded in &f%time%"); // updated in version 5
        setMsgs("Console-Name", "The Server");
        setMsgs("No-Permission", "&#FF8C6BSorry. &fYou don't have permission for that.");
        setMsgs("Feelings-Help", "&#c3ff9bAvailable Feelings:");
        setMsgs("Feelings-Help-Page", "&r&#7aa35ePage &#c3ff9b%page%&8/&#7aa35e%pagemax%");
        setMsgs("Sending-World-Disabled", "&#FF8C6BSorry. &fYou can't use feelings in this world.");
        setMsgs("Disabled-Serverwide-Targets", "&#FF8C6BNot Allowed. &fThis server has disabled emoting everyone.");
        setMsgs("Receiving-World-Disabled", "&#FF8C6BSorry. &fYour target is in a world with feelings disabled.");
        setMsgs("Page-Not-Found", "&#FF8C6BOops. &fThat page doesn't exist, try &7/feelings 1");
        setMsgs("No-Player", "&#FF8C6BOops! &fYou need to provide a player to do that to."); // updated in version 2
        setMsgs("No-Player-Mute", "&#FF8C6BOops! &fYou must provide a player to mute."); // added in version 3
        setMsgs("No-Player-Unmute", "&#FF8C6BOops! &fYou must provide a player to unmute."); // added in version 3
        setMsgs("Player-Offline", "&#FF8C6BPlayer Offline. &fWe couldn't find &7&l%player% &fon the server.");
        setMsgs("Player-Never-Joined", "&#FF8C6BHmm. &fThat player has never joined before.");
        setMsgs("Outside-Of-Radius", "&#FF8C6BHmm. &fYou're too far away from &7%player% &fto use that.");
        setMsgs("Cooldown-Active", "&#FF8C6BSlow Down. &fWait &7%time% &fbefore doing that again.");
        setMsgs("Ignore-Cooldown", "&#FF8C6BSlow Down. &fPlease wait before ignoring again.");
        setMsgs("Console-Not-Player", "&#FF8C6BGoofball! &fThe &7CONSOLE&f is not a real player.");
        setMsgs("Sender-Is-Target", "&#FF8C6BYou Silly! &fYou can't %command% &fyourself.");
        setMsgs("Is-Muted", "&#FF8C6BYou're Muted. &fYou can no longer use feelings."); // added in version 3
        setMsgs("Folder-Not-Found", "&#FF8C6BHmm. &fThere is no data to display here."); // added in version 4
        setMsgs("Stats-Header-Own", "&#f4fcab&lYour Statistics:"); // added in version 6
        setMsgs("Stats-Header-Other", "&#f4fcab&l%player%'s Statistics:"); // added in version 6
        setMsgs("Ignore-List-Header", "&#FF8C6B&lIgnored Players:"); // added in version 7
        setMsgs("Ignore-List-None", "   &8&l> &fYou are currently not ignoring anyone!"); // added in version 7
        setMsgs("Ignore-List-All", "   &8&l> &fYou are ignoring all feelings."); // added in version 8
        setMsgs("Ignore-List-Cooldown", "&#FF8C6BPlease Wait. &fYou must wait before checking who you're ignoring.");
        setMsgs("Mute-List-Header", "&#f4fcabMuted Players:"); // added in version 4
        setMsgs("Mute-List-Player", "&r  &8&l> &f%player%"); // added in version 4
        setMsgs("Mute-List-Total-One", "&r  &7There is &f&l%total% &7muted player."); // added in version 4
        setMsgs("Mute-List-Total-Many", "&r  &7There are &f&l%total% &7muted players."); // added in version 4
        setMsgs("Mute-List-Total-Zero", "&r  &8&l> &#c3ff9b&lYay! &7No players are currently muted."); // added in version 4
        setMsgs("Player-Has-Been-Muted", "&#FF8C6BUser Muted. &7%player% &fcan no longer use feelings."); // added in version 3
        setMsgs("Player-Muted-Via-Essentials", "&#FF8C6BOops! &7%player&f is muted via Essentials, use /unmute!"); // added in version 5
        setMsgs("Player-Muted-Via-LiteBans", "&#FF8C6BOops! &7%player&f is muted via LiteBans, use /unmute!"); // added in version 5
        setMsgs("Player-Muted-Via-AdvancedBan", "&#FF8C6BOops! &7%player&f is muted via AdvancedBans, use /unmute!"); // added in version 5
        setMsgs("Extra-Mute-Present", "&r&7&oThey're already muted via your punishment system. &#f4fcab&oSee /cf mutelist"); // added in version 5
        setMsgs("Player-Has-Been-Unmuted", "&#c3ff9bUser Unmuted. &7%player% &fcan now use feelings again."); // added in version 3
        setMsgs("Cant-Mute-Self", "&#FF8C6BYou Silly! &fYou can't mute yourself."); // added in version 3
        setMsgs("Player-Already-Muted", "&#FF8C6BOops. &fThis player is already muted."); // added in version 3
        setMsgs("Player-Already-Unmuted", "&#FF8C6BOops. &fYou can't unmute a player who's not muted."); // added in version 3
        setMsgs("Already-Mute-Unmute-Suggestion", "&7&oCould you have meant &#f4fcab&o/cf unmute"); // added in version 3
        setMsgs("No-Perm-Mute-Suggestion", "&7&oCould you have meant &#f4fcab&o/cf ignore&7&o?");
        setMsgs("Emote-Disabled", "&#FF8C6BEmote Disabled. &fThis emotion has been disabled by the server.");
        setMsgs("Ignoring-On-Player", "&7You've now &#FF8C6B&lBLOCKED &r&7feelings from: &f%player%");
        setMsgs("Ignoring-Off-Player", "&7Now &#c3ff9b&lALLOWING &7feelings from: &f%player%");
        setMsgs("Ignoring-On-All", "&7You've now &#FF8C6B&lBLOCKED &r&7feelings from all players.");
        setMsgs("Ignoring-Off-All", "&7Now &#c3ff9b&lALLOWING &7feelings from all players.");
        setMsgs("Cant-Ignore-Self", "&#FF8C6BYou Silly! &fYou can't ignore yourself.");
        setMsgs("Target-Is-Ignoring", "&#FF8C6BBummer! &fThis player has blocked you.");
        setMsgs("Target-Is-Ignoring-All", "&#FF8C6BBummer! &fThis player is not accepting feelings.");
        setMsgs("Command-List-Page", "&7To go to the next page do &#c3ff9b/feelings %page%");
        setMsgs("Command-List-Player", "&r &f(player)");
        setMsgs("Command-List-NoPerm", "&7You aren't able to use this feeling.");

        setMsgs("Command-Help.Descriptions.Help", "&7Shows you this page.");
        setMsgs("Command-Help.Descriptions.Ignore", "&7Toggle ignoring feelings from players");
        setMsgs("Command-Help.Descriptions.Ignore-All", "&7Toggles everyone being able to use feelings.");
        setMsgs("Command-Help.Descriptions.Stats", "&7Shows how many feelings you've sent.");
        setMsgs("Command-Help.Descriptions.Stats-Others", "&7Shows another players total sent feelings.");
        setMsgs("Command-Help.Descriptions.Mute", "&7Prevents a player from using feelings.");
        setMsgs("Command-Help.Descriptions.Unmute", "&7Reallows feeling usage by a player.");
        setMsgs("Command-Help.Descriptions.Mute-List", "&7Lists players that are currently muted.");
        setMsgs("Command-Help.Descriptions.Plugin-Version", "&7Shows the current version info.");
        setMsgs("Command-Help.Descriptions.Plugin-Reload", "&7Reload all config and message files.");
        setMsgs("Command-Help.Descriptions.Feelings", "&7Show all feelings available for use.");

        setMsgs("Command_Descriptions.Hug", "Give someone a nice warm hug!");
        setMsgs("Command_Descriptions.Slap", "Slap some sense back into someone.");
        setMsgs("Command_Descriptions.Poke", "Poke someone to get their attention");
        setMsgs("Command_Descriptions.Highfive", "Show your support, and give a highfive!");
        setMsgs("Command_Descriptions.Facepalm", "Need to show some disapproval?");
        setMsgs("Command_Descriptions.Yell", "Yell at someone as loud as possible!");
        setMsgs("Command_Descriptions.Bite", "Bite a player right on the arm.");
        setMsgs("Command_Descriptions.Snuggle", "Snuggle up with the power of warm hugs!");
        setMsgs("Command_Descriptions.Shake", "Shake someone to their core.");
        setMsgs("Command_Descriptions.Stab", "Stab someone with a knife. Ouch!");
        setMsgs("Command_Descriptions.Kiss", "Give a kiss on the cheek. How sweet!");
        setMsgs("Command_Descriptions.Punch", "Punch someone back from insanity!");
        setMsgs("Command_Descriptions.Murder", "Finna kill someone here.");
        setMsgs("Command_Descriptions.Boi", "Still in iFunny hell? Here's this!");
        setMsgs("Command_Descriptions.Cry", "Down in the dumps? Let it all out.");
        setMsgs("Command_Descriptions.Dab", "Cringe never dies! Prove it to the world.");
        setMsgs("Command_Descriptions.Scorn", "Shame a player for what they've done.");
        setMsgs("Command_Descriptions.Lick", "Lick someone like an ice-cream sundae!");
        setMsgs("Command_Descriptions.Pat", "Pat a players head for being good.");
        setMsgs("Command_Descriptions.Stalk", "Stalk a player carefully... carefully.");
        setMsgs("Command_Descriptions.Sus", "Pure single-boned suspicion.");
        setMsgs("Command_Descriptions.Wave", "Say frewell, and wave aideu. How elegant!");
        setMsgs("Command_Descriptions.Welcomeback", "Give a warm welcome-back to returning players!");
        setMsgs("Command_Descriptions.Boop", "Boop someone right on their nose!");
        setMsgsVersion(13);

        if (!emotesfile.exists() || !emotes.contains("Version")) {
            if (saveFile(emotes, emotesfile)) {
                if(!Main.reducemsgs) {
                    plugin.getLogger().info("Created new emotes.yml file...");
                }
            }
        } else {
            if (emotes.getInt("Version") != 9) {
                plugin.getLogger().info("Updating your emotes.yml for the latest update...");
                if(emotes.getInt("Version") <= 4) {
                    if(!emotes.contains("Feelings.Welcomeback.Msgs.Sender") || Objects.requireNonNull(emotes.getString("Feelings.Welcomeback.Msgs.Sender")).equalsIgnoreCase("&7You told &#c3ff9b&l%player% welcome back!")) {
                        forceEmotes("Feelings.Welcomeback.Msgs.Sender", "&7You told &#c3ff9b&l%player%&r &7welcome back!");
                    }
                }
                if (emotes.getInt("Version") <= 3) {
                    if (Objects.requireNonNull(emotes.getString("Feelings.Bite.Msgs.Sender")).contains("info")) {
                        forceEmotes("Feelings.Bite.Msgs.Sender", "&7You sink your teeth into &#FF8C6B&l%player%&r&7's skin");
                    }
                }

                if (emotes.getInt("Version") <= 5) {
                   setEmotesBoolean("Feelings.Welcomeback.Enable", emotes.getBoolean("Feelings.Wb.Enable"));
                   setEmotes("Feelings.Welcomeback.Msgs.Sender", emotes.getString("Feelings.Wb.Msgs.Sender"));
                   setEmotes("Feelings.Welcomeback.Msgs.Target", emotes.getString("Feelings.Wb.Msgs.Target"));
                   setEmotes("Feelings.Welcomeback.Msgs.Global", emotes.getString("Feelings.Wb.Msgs.Global"));
                   setEmotes("Feelings.Welcomeback.Sounds.Sound1.Name", emotes.getString("Feelings.Wb.Sounds.Sound1.Name"));
                   setEmotesDouble("Feelings.Welcomeback.Sounds.Sound1.Volume", emotes.getDouble("Feelings.Wb.Sounds.Sound1.Volume"));
                   setEmotesDouble("Feelings.Welcomeback.Sounds.Sound1.Pitch", emotes.getDouble("Feelings.Wb.Sounds.Sound1.Pitch"));
                   setEmotes("Feelings.Welcomeback.Sounds.Sound2.Name", emotes.getString("Feelings.Wb.Sounds.Sound2.Name"));
                   setEmotesDouble("Feelings.Welcomeback.Sounds.Sound2.Volume", emotes.getDouble("Feelings.Wb.Sounds.Sound2.Volume"));
                   setEmotesDouble("Feelings.Welcomeback.Sounds.Sound2.Pitch", emotes.getDouble("Feelings.Wb.Sounds.Sound2.Pitch"));
                   
                   forceEmotes("Feelings.Wb", null);
                }
                if(emotes.getInt("Version") <= 6) {
                    final String path = "Feelings.Hug.Sounds.Sound1.Name";
                    if(emotes.contains(path)) {
                        if(Objects.requireNonNull(emotes.getString(path)).equalsIgnoreCase("ENTITY_CAT_PURREOW")) {
                            if(Supports.getMcMajorVersion() >= 1 && Supports.getMcMinorVersion() >= 20 && Supports.getMcPatchVersion() >= 6) {
                                // Sounds changed along the way somehow and I let a soft fail occur. This should fix it.
                                emotes.set(path, "ENTITY.CAT.PURREOW");
                            }
                        }
                    }
                }
                setEmotesVersion(9);
            }
        }

        // One-time move of any "Feelings.*" entries still sitting in emotes.yml into Feelings/*.yml.
        migrateLegacyFeelingsToFolder(emotes, emotesfile);

        writeFeelingDefaults("Hug", true,
                "&7You give &#c3ff9b&l%player% &r&7a warm hug. &#FF8C6BAwww &4❤",
                "&#c3ff9b&l%player% &r&7gives you a warm hug. &#FF8C6BAwww &4❤",
                "&#c3ff9b&l%sender% &r&7gave &2&l%target% &r&7a warm hug. &#FF8C6BAwww &4❤",
                "ENTITY.CAT.PURREOW", 2.0, 2.0, "None", 0.0, 0.0);

        writeFeelingDefaults("Bite", true,
                "&7You sink your teeth into &#FF8C6B&l%player%&r&7's skin.",
                "&#FF8C6B&l%player% &r&7sinks their teeth into your skin.",
                "&#FF8C6B&l%sender% &r&7sank their teeth into &4&l%target%&r&7's skin",
                "ENTITY.ZOMBIE.ATTACK_WOODEN_DOOR", 2.0, 2.0, "None", 0.0, 0.0);

        writeFeelingDefaults("Punch", true,
                "&7You strike &#FF8C6B&l%player% &r&7with a punch. Ouch!",
                "&#FF8C6B&l%player% &r&7strikes you with a punch. Ouch!",
                "&#FF8C6B&l%sender% &r&7punched &4&l%target% &r&7right in the face.",
                "ENTITY.IRON_GOLEM.ATTACK", 2.0, 0.6, "None", 0.0, 0.0);

        writeFeelingDefaults("Murder", true,
                "&7You murder &#FF8C6B&l%player% &r&7and have no regrets.",
                "&#FF8C6B&l%player% &r&7just murdered you. Bandaid anyone?",
                "&#FF8C6B&l%sender% &r&7just murdered &4&l%target%&r&7. &7&lRIP",
                "ENTITY.BLAZE.DEATH", 1.0, 0.7, "None", 0.0, 0.0);

        writeFeelingDefaults("Boi", true,
                "&7You inhale at &#f4fcab&l%player%&r&7... &6&lBOI",
                "&#f4fcab&l%player% &r&7inhales at you... &6&lBOI",
                "&#f4fcab&l%sender% &r&7inhales at &6&l%target%&r&7... &6&l&oBOI",
                "ENTITY.CHICKEN.EGG", 2.0, 0.1, "None", 0.0, 0.0);

        writeFeelingDefaults("Dab", true,
                "&7You freshly dab on &#c3ff9b&l%player%&r&7... &7&oGot'em.",
                "&#c3ff9b&l%player% &r&7freshly dabs on you... &7&oGot'em.",
                "&#c3ff9b&l%sender% &r&7freshly dabs on &2&l%target%&r&7... &7&oGot'em.",
                "ENTITY.CHICKEN.EGG", 2.0, 0.1, "None", 0.0, 0.0);

        writeFeelingDefaults("Cry", true,
                "&7You cry on &b&l%player%&r&7's shoulder.",
                "&b&l%player% &r&7cries on your shoulder.",
                "&b&l%sender% &r&7leans on &3&l%target%&r&7's shoulder and cries.",
                "ENTITY.GHAST.DEATH", 1.0, 0.8, "None", 0.0, 0.0);

        writeFeelingDefaults("Facepalm", true,
                "&7You facepalm at what &#f4fcab&l%player% &r&7just said.",
                "&#f4fcab&l%player% &r&7facepalmed at what you just said.",
                "&#f4fcab&l%sender% &r&7facepalms at &6&l%target%&r&7 for being dumb.",
                "ENTITY.VILLAGER.NO", 2.0, 1.0, "None", 0.0, 0.0);

        writeFeelingDefaults("Highfive", true,
                "&7You give a mighty highfive to &#c3ff9b&l%player%&7.",
                "&#c3ff9b&l%player% &7gives you a mighty highfive.",
                "&#c3ff9b&l%sender% &7gives &2&l%target% &r&7a mighty highfive.",
                "ENTITY.VILLAGER.YES", 2.0, 1.0, "None", 0.0, 0.0);

        writeFeelingDefaults("Kiss", true,
                "&7You give &#c3ff9b&l%player% &r&7a kiss. &#FF8C6BAwww &4❤",
                "&#c3ff9b&l%player% &r&7gives you a kiss. &#FF8C6BAwww &4❤",
                "&#c3ff9b&l%sender% &7gives &2&l%target% &7a kiss. &#FF8C6BAwww &4❤",
                "ENTITY.ARROW.HIT_PLAYER", 2.0, 1.0, "None", 0.0, 0.0);

        writeFeelingDefaults("Lick", true,
                "&7You lick &#f4fcab&l%player% &7like ice-cream. &6Gross!",
                "&#f4fcab&l%player% &r&7licks you like ice-cream. &6Gross!",
                "&#f4fcab&l%target% &r&7got licked by &6&l%sender%&r&7. &8Gross.",
                "ENTITY.GENERIC.DRINK", 2.0, 0.1, "None", 0.0, 0.0);

        writeFeelingDefaults("Shake", true,
                "&7You shake &#FF8C6B&l%player%&r&7's entire body.",
                "&#FF8C6B&l%player% &r&7shakes your entire body.",
                "&#FF8C6B&l%sender% &r&7picks up &4&l%target%&r&7's body, and shakes it.",
                "ENTITY.WOLF.SHAKE", 2.0, 0.7, "None", 0.0, 0.0);

        writeFeelingDefaults("Snuggle", true,
                "&7You snuggle &#c3ff9b&l%player% &r&7with love. &#FF8C6BAwww &4❤",
                "&#c3ff9b&l%player% &r&7snuggles you with love. &#FF8C6BAwww &4❤",
                "&#c3ff9b&l%sender% &r&7snuggles &2&l%target% &r&7them with hugs. &#FF8C6BAwww &4❤",
                "ENTITY.CAT.PURR", 2.0, 1.0, "None", 0.0, 0.0);

        writeFeelingDefaults("Yell", true,
                "&7You yell at &#FF8C6B&l%player%&r&7 at the top of your lungs.",
                "&#FF8C6B&l%player% &r&7yells at you from the top of their lungs.",
                "&#FF8C6B&l%sender% &r&7yells right at &4&l%target% &r&7from the top of their lungs.",
                "ENTITY.GHAST.SCREAM", 2.0, 1.0, "None", 0.0, 0.0);

        writeFeelingDefaults("Poke", true,
                "&7You poked &#f4fcab&l%player%&7. Maybe they're on vacation?",
                "&#f4fcab&l%player% &r&7has poked you. Anyone there?",
                "&#f4fcab&l%target% &r&7was poked by &6&l%sender%&r&7. &7&oAnyone home?",
                "ENTITY.CHICKEN.EGG", 2.0, 0.1, "None", 0.0, 0.0);

        writeFeelingDefaults("Slap", true,
                "&7You slap &#FF8C6B&l%player% &r&7with some spaghetti.",
                "&#FF8C6B&l%player% &r&7slaps you with some spaghetti.",
                "&#FF8C6B&l%target% &r&7was slapped by &4&l%sender%&r&7.",
                "ENTITY.BLAZE.HURT", 2.0, 0.7, "None", 0.0, 0.0);

        writeFeelingDefaults("Stab", true,
                "&7You stab &#FF8C6B&l%player% &r&7with a knife. Got Bandaids?",
                "&#FF8C6B&l%player% &r&7grabs a knife and stabs you. Got Bandaids?",
                "&#FF8C6B&l%sender% &r&7grabs a knife and stabs &4&l%target%&r&7.",
                "ENTITY.GENERIC.HURT", 2.0, 0.7, "None", 0.0, 0.0);

        writeFeelingDefaults("Pat", true,
                "&7You gently pat &#c3ff9b&l%player%&r&7's head for being good.",
                "&#c3ff9b&l%player% &r&7gently pats your head for being good.",
                "&#c3ff9b&l%sender% &r&7gently pats &2&l%target%&r&7's head for being good.",
                "ENTITY.WOLF.PANT", 2.0, 0.8, "None", 0.0, 0.0);

        writeFeelingDefaults("Scorn", true,
                "&7You scorn &#FF8C6B&l%player% &r&7for what they've done.",
                "&#FF8C6B&l%player% &r&7scorns you for what you've done.",
                "&#FF8C6B&l%sender% &r&7scorns &4&l%target% &r&7for what they've done.",
                "ENTITY.ENDERMAN.STARE", 2.0, 0.8, "None", 0.0, 0.0);

        writeFeelingDefaults("Stalk", true,
                "&7You carefully stalk &#f4fcab&l%player%&r&7, &7&oHeh Heh.",
                "&#f4fcab&l%player% &r&7stalks you from a nearby tree.",
                "&#f4fcab&l%sender% &r&7stalks &6&l%target% &r&7from a nearby tree.",
                "AMBIENT.CAVE", 2.0, 2.0, "None", 0.0, 0.0);

        writeFeelingDefaults("Sus", true,
                "&7You look at &#f4fcab&l%player%&r&7's single-boned body in suspicion.",
                "&#f4fcab&l%player% &r&7suspiciously looks at your single-boned body.",
                "&#f4fcab&l%sender% &r&7looks at &6&l%target% &r&7in single-boned suspicion.",
                validSound("AMBIENT_NETHER_WASTES_MOOD") ? "AMBIENT.NETHER_WASTES.MOOD" : "AMBIENT.CAVE", 2.0, 1.2,
                validSound("BLOCK_RESPAWN_ANCHOR_DEPLETE") ? "BLOCK.RESPAWN_ANCHOR.DEPLETE" : "None", 0.25, 0.1);

        writeFeelingDefaults("Wave", true,
                "&7You wave adieu to &#c3ff9b&l%player%&r&7!",
                "&#c3ff9b&l%player% &r&7waves adieu to you.",
                "&#c3ff9b&l%sender% &r&7waves adieu to &2&l%target%.",
                validSound("BLOCK_AMETHYST_BLOCK_RESONATE") ? "BLOCK.AMETHYST_BLOCK.RESONATE" : "BLOCK.NOTE_BLOCK.BELL", 2.0, 2.0,
                "None", 0.0, 0.0);

        writeFeelingDefaults("Welcomeback", true,
                "&7You told &#c3ff9b&l%player%&r &7welcome back!",
                "&#c3ff9b&l%player% &r&7gave you a warm welcome back!",
                "&#c3ff9b&l%sender% &r&7welcomed &2&l%target% &r&7back.",
                "BLOCK.BEACON.POWER_SELECT", 2.0, 2.0, "None", 0.0, 0.0);

        writeFeelingDefaults("Boop", true,
                "&7You boop &#c3ff9b&l%player%&7 right on their nose!",
                "&#c3ff9b&l%player% &r&7boops you right on your nose!",
                "&#c3ff9b&l%target% &r&7was booped on their nose by &#c3ff9b&l%sender%&r&7!",
                "ENTITY.CHICKEN.EGG", 2.0, 2.0, "None", 0.0, 0.0);

        // Still moot: scaffolded but intentionally left out of Main.BUILTIN_FEELINGS / plugin.yml.
        writeFeelingDefaults("Knock", true,
                "&7You knock on &#c3ff9b&l%player%&7's door!",
                "&#c3ff9b&l%player% &r&7has knocked at your door!",
                "&#c3ff9b&l%target% &r&7has knocked on &#c3ff9b&l%sender%&r&7 door.",
                "BLOCK.IRON.HIT", 2.0, 1.75, "None", 0.0, 0.0);

        setEmotesVersion(9);

        List<String> customFeelings = discoverCustomFeelings();
        Main.setCustomFeelings(customFeelings);
        CommandManager.updateCustomFeelingCommands(customFeelings);

        reloadFiles();
    }

    static void reloadFiles() {
        plugin.folder = getFolder();
        plugin.msgsfile = new File(plugin.folder, File.separator + "messages.yml");
        plugin.msg = YamlConfiguration.loadConfiguration(plugin.msgsfile);

        plugin.emotesfile = new File(plugin.folder, File.separator + "emotes.yml");
        plugin.emotes = YamlConfiguration.loadConfiguration(plugin.emotesfile);
    }
}