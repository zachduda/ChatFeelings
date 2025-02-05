package com.zachduda.chatfeelings;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileSetup {
    private static final Main plugin = Main.getPlugin(Main.class);


    private static void saveFile(FileConfiguration fc, File f) {
        try {
            fc.save(f);
        } catch (Exception err) {
            plugin.getLogger().severe("[!] Failed to save file changes: " + f.getName());
            if(Main.debug) {
                err.getMessage();
            }
        }
    }

    private static File getFolder() {
        return Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("ChatFeelings")).getDataFolder();
    }

    private static File emfolder = updateEmoteFolder();
    static List<Path> emoteFiles = new ArrayList<>();

    // Returns folder and updates variable
    private static File updateEmoteFolder() {
        if(emfolder == null) {
                emfolder = new File(plugin.getDataFolder(), File.separator + "Emotes");
                try (Stream<Path> stream = Files.list(emfolder.toPath())) {
                    emoteFiles = stream
                            .filter(Files::isRegularFile)
                            .filter(path -> path.getFileName().toString().endsWith(".yml") || path.getFileName().toString().endsWith(".yaml"))
                            .collect(Collectors.toList());
                } catch (IOException e) {
                    throw new UncheckedIOException("Error loading command files: " + e.getMessage(), e);
                }
        }
        return emfolder;
    }

    static void generateDefaultEmotes() {
        if (emfolder.exists()) {
            // Only generates on 1st run.
            return;
        }

        File f = new File(emfolder, File.separator + "hug.yml");
        FileConfiguration sethug = YamlConfiguration.loadConfiguration(f);

        sethug.options().header("\nThis is a default emote generated on your first run. You can delete this file if you don't want to use this emote!\n");
        sethug.set("Name", "hug");
        sethug.set("Enabled", true);
        sethug.set("Messages.Sender", "You give &a&l%player% &r&7a warm hug. &cAwww &4❤");
        sethug.set("Messages.Target", "&a&l%player% &r&7gives you a warm hug. &cAwww &4❤");
        sethug.set("Messages.Global", "&a&l%sender% &r&7gave &2&l%target% &r&7a warm hug. &cAwww &4❤");

        sethug.set("Sounds.Enabled", true);
        sethug.set("Sounds.Primary.Name", "ENTITY_CAT_PURREOW");
        sethug.set("Sounds.Primary.Volume", 2.0);
        sethug.set("Sounds.Primary.Pitch", 2.0);
        sethug.set("Sounds.Secondary.Name", "None");
        sethug.set("Sounds.Secondary.Volume", 0.0);
        sethug.set("Sounds.Secondary.Pitch", 0.0);

        sethug.set("Is-Harmful", false);
        sethug.set("Permission-Node:", "chatfeelings.hug");

        try {
            sethug.save(f);
        } catch (IOException e) {
            plugin.getLogger().info("Error trying to create hug.yml: ");
            e.printStackTrace();
        }
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
            plugin.getLogger().warning("Replacing '" + configpath + " in messages.yml, it was left blank.");
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
    static void emotesFromFolder() {
        int i = Main.feelings.size();
        String lf;
        for (File emotefile: Objects.requireNonNull(emfolder.listFiles())) {
            String path = emotefile.getPath();

            if (com.google.common.io.Files.getFileExtension(path).equalsIgnoreCase("yml")) {
                File f = new File(path);
                final String fn = com.google.common.io.Files.getNameWithoutExtension(f.getName().toLowerCase().trim()); // File Name
                final FileConfiguration fc = YamlConfiguration.loadConfiguration(f);
                Main.feelings.add(new Emotion(fc.getString("Name"),
                        fc.getString("Messages.Sender"),
                        fc.getString("Messages.Target"),
                        fc.getString("Messages.Global"),
                        fc.getString("Permission-Node"),
                        Sound.valueOf(fc.getString("Sounds.Primary.Name")),
                        Sound.valueOf(fc.getString("Sounds.Secondary.Name")),
                        (float) fc.getDouble("Sounds.Primary.Volume"),
                        (float) fc.getDouble("Sounds.Secondary.Volume"),
                        (float) fc.getDouble("Sounds.Primary.Pitch"),
                        (float) fc.getDouble("Sounds.Secondary.Pitch"),
                        fc.getBoolean("Sounds.Enabled"),
                        fc.getBoolean("Is-Harmful")
                        )
                );
                Main.fmap.put(Objects.requireNonNull(fc.getString("Name")).toLowerCase(), i);
                i++;
            }
        }
        Main.debug("Loaded " + Main.feelings.size() + " emotions from file: " + Main.feelings);
    }

    static void enableFiles() {
        File folder = getFolder();

        File msgsfile = new File(folder, File.separator + "messages.yml");
        FileConfiguration msgs = YamlConfiguration.loadConfiguration(msgsfile);

        generateDefaultEmotes();
        updateEmoteFolder();
        emotesFromFolder();

        setMsgs("Prefix", "&a&lC&r&ahat&f&lF&r&feelings &8&l┃ &f");
        setMsgs("Prefix-Header", "&a&lC&r&ahat &f&lF&r&feelings");
        setMsgs("Reload", "&8&l> &a&l✓  &7Plugin reloaded in &f%time%"); // updated in version 5
        setMsgs("Console-Name", "The Server");
        setMsgs("No-Permission", "&cSorry. &fYou don't have permission for that.");
        setMsgs("Feelings-Help", "&a&lFeelings:");
        setMsgs("Feelings-Help-Page", "&7(Page &f%page%&8&l/&r&f%pagemax%&7)");
        setMsgs("Sending-World-Disabled", "&cSorry. &fYou can't use feelings in this world.");
        setMsgs("Disabled-Serverwide-Targets", "&cNot Allowed. &fThis server has disabled emoting everyone.");
        setMsgs("Receiving-World-Disabled", "&cSorry. &fYour target is in a world with feelings disabled.");
        setMsgs("Page-Not-Found", "&cOops. &fThat page doesn't exist, try &7/feelings 1");
        setMsgs("No-Player", "&cOops! &fYou need to provide a player to do that to."); // updated in version 2
        setMsgs("No-Player-Mute", "&cOops! &fYou must provide a player to mute."); // added in version 3
        setMsgs("No-Player-Unmute", "&cOops! &fYou must provide a player to unmute."); // added in version 3
        setMsgs("Player-Offline", "&cPlayer Offline. &fWe couldn't find &7&l%player% &fon the server.");
        setMsgs("Player-Never-Joined", "&cHmm. &fThat player has never joined before.");
        setMsgs("Outside-Of-Radius", "&cHmm. &fYou're too far away from &7%player% &fto use that.");
        setMsgs("Cooldown-Active", "&cSlow Down. &fWait &7%time% &fbefore doing that again.");
        setMsgs("Ignore-Cooldown", "&cSlow Down. &fPlease wait before ignoring again.");
        setMsgs("Console-Not-Player", "&cGoofball! &fThe &7CONSOLE&f is not a real player.");
        setMsgs("Sender-Is-Target", "&cYou Silly! &fYou can't %command% &fyourself.");
        setMsgs("Is-Muted", "&cYou're Muted. &fYou can no longer use feelings."); // added in version 3
        setMsgs("Folder-Not-Found", "&cHmm. &fThere is no data to display here."); // added in version 4
        setMsgs("Stats-Header-Own", "&e&lYour Statistics:"); // added in version 6
        setMsgs("Stats-Header-Other", "&e&l%player%'s Statistics:"); // added in version 6
        setMsgs("Ignore-List-Header", "&c&lIgnored Players:"); // added in version 7
        setMsgs("Ignore-List-None", "   &8&l> &fYou are currently not ignoring anyone!"); // added in version 7
        setMsgs("Ignore-List-All", "   &8&l> &fYou are ignoring all feelings."); // added in version 8
        setMsgs("Ignore-List-Cooldown", "&cPlease Wait. &fYou must wait before checking who you're ignoring.");
        setMsgs("Mute-List-Header", "&e&lMuted Players:"); // added in version 4
        setMsgs("Mute-List-Player", "&r  &8&l> &f%player%"); // added in version 4
        setMsgs("Mute-List-Total-One", "&r  &7There is &f&l%total% &7muted player."); // added in version 4
        setMsgs("Mute-List-Total-Many", "&r  &7There are &f&l%total% &7muted players."); // added in version 4
        setMsgs("Mute-List-Total-Zero", "&r  &8&l> &a&lYay! &7No players are currently muted."); // added in version 4
        setMsgs("Player-Has-Been-Muted", "&cUser Muted. &7%player% &fcan no longer use feelings."); // added in version 3
        setMsgs("Player-Muted-Via-Essentials", "&cOops! &7%player&f is muted via Essentials, use /unmute!"); // added in version 5
        setMsgs("Player-Muted-Via-LiteBans", "&cOops! &7%player&f is muted via LiteBans, use /unmute!"); // added in version 5
        setMsgs("Player-Muted-Via-AdvancedBan", "&cOops! &7%player&f is muted via AdvancedBans, use /unmute!"); // added in version 5
        setMsgs("Extra-Mute-Present", "&r&7&oThey're already muted via your punishment system. &e&oSee /cf mutelist"); // added in version 5
        setMsgs("Player-Has-Been-Unmuted", "&aUser Unmuted. &7%player% &fcan now use feelings again."); // added in version 3
        setMsgs("Cant-Mute-Self", "&cYou Silly! &fYou can't mute yourself."); // added in version 3
        setMsgs("Player-Already-Muted", "&cOops. &fThis player is already muted."); // added in version 3
        setMsgs("Player-Already-Unmuted", "&cOops. &fYou can't unmute a player who's not muted."); // added in version 3
        setMsgs("Already-Mute-Unmute-Suggestion", "&7&oCould you have meant &e&o/cf unmute"); // added in version 3
        setMsgs("No-Perm-Mute-Suggestion", "&7&oCould you have meant &e&o/cf ignore&7&o?");
        setMsgs("Emote-Disabled", "&cEmote Disabled. &fThis emotion has been disabled by the server.");
        setMsgs("Ignoring-On-Player", "&7You've now &c&lBLOCKED &r&7feelings from: &f%player%");
        setMsgs("Ignoring-Off-Player", "&7Now &a&lALLOWING &7feelings from: &f%player%");
        setMsgs("Ignoring-On-All", "&7You've now &c&lBLOCKED &r&7feelings from all players.");
        setMsgs("Ignoring-Off-All", "&7Now &a&lALLOWING &7feelings from all players.");
        setMsgs("Cant-Ignore-Self", "&cYou Silly! &fYou can't ignore yourself.");
        setMsgs("Target-Is-Ignoring", "&cBummer! &fThis player has blocked you.");
        setMsgs("Target-Is-Ignoring-All", "&cBummer! &fThis player is not accepting feelings.");
        setMsgs("Command-List-Page", "&7To go to the next page do &a/feelings %page%");
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
        setMsgs("Command_Descriptions.Shake", "Shake a player to their feet.");
        setMsgs("Command_Descriptions.Stab", "Stab someone with a knife. Ouch!");
        setMsgs("Command_Descriptions.Kiss", "Give a kiss on the cheek. How sweet!");
        setMsgs("Command_Descriptions.Punch", "Punch someone back from insanity!");
        setMsgs("Command_Descriptions.Murder", "Finna kill someone here.");
        setMsgs("Command_Descriptions.Boi", "Living in 2016? Boi at a player.");
        setMsgs("Command_Descriptions.Cry", "Real sad hours? Cry at someone.");
        setMsgs("Command_Descriptions.Dab", "Freshly dab on someone.");
        setMsgs("Command_Descriptions.Scorn", "Shame a player for what they've done.");
        setMsgs("Command_Descriptions.Lick", "Lick someone like an ice-cream sundae!");
        setMsgs("Command_Descriptions.Pat", "Pat a players head for being good.");
        setMsgs("Command_Descriptions.Stalk", "Stalk a player carefully... carefully.");
        setMsgs("Command_Descriptions.Sus", "Pure single-boned suspicion.");
        setMsgs("Command_Descriptions.Wave", "Say frewell, and wave aideu. How elegant!");
        setMsgs("Command_Descriptions.Welcomeback", "Give a warm welcome-back to returning players!");
        setMsgs("Command_Descriptions.Boop", "Boop someone right on their nose!");
        setMsgsVersion(1);

        reloadFiles();
    }

    static void reloadFiles() {
        plugin.folder = getFolder();
        plugin.msgsfile = new File(plugin.folder, File.separator + "messages.yml");
        plugin.msg = YamlConfiguration.loadConfiguration(plugin.msgsfile);

        updateEmoteFolder();
    }
}