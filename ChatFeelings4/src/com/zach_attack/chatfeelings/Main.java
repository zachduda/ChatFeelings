package com.zach_attack.chatfeelings;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import com.earth2me.essentials.Essentials;
import com.zach_attack.chatfeelings.api.ChatFeelingsAPI;
import com.zach_attack.chatfeelings.api.FeelingGlobalNotifyEvent;
import com.zach_attack.chatfeelings.api.FeelingRecieveEvent;
import com.zach_attack.chatfeelings.api.FeelingSendEvent;
import com.zach_attack.chatfeelings.api.Placeholders;

import com.zach_attack.chatfeelings.other.Updater;

import litebans.api.Database;
import me.leoko.advancedban.manager.PunishmentManager;

public class Main extends JavaPlugin implements Listener {

    public ChatFeelingsAPI api;
    
    final static List <String> feelings = Arrays.asList(new String[] {
    		"hug", "slap", "poke", "highfive", "facepalm", "yell",
    		"bite", "snuggle", "shake", "stab", "kiss", "punch", "murder",
    		"cry", "boi", "dab", "lick", "scorn", "pat", "stalk"
    });

    private boolean hasess = false;
    private boolean haslitebans = false;
    private boolean hasadvancedban = false;

    private boolean usevanishcheck = false;

    protected static boolean particles = true;

    private boolean useperms = false;

    protected static boolean multiversion = false;
    protected static boolean debug = false;

    private static boolean sounds = false;
    private static boolean punishmentError = false;

    private long lastreload = 0;
    private long lastmutelist = 0;
    
    private final String version = Bukkit.getBukkitVersion().toString().replace("-SNAPSHOT", "");
    private final boolean supported = (version.contains("1.18") || version.contains("1.17") || version.contains("1.16") || version.contains("1.13") || version.contains("1.14") || version.contains("1.15")) ?true :false;
    
    private List <String> disabledsendingworlds = getConfig().getStringList("General.Disable-Sending-Worlds");
    private List <String> disabledreceivingworlds = getConfig().getStringList("General.Disable-Receiving-Worlds");

    protected File folder;
    protected File msgsfile;
    protected FileConfiguration msg;

    protected File emotesfile;
    protected FileConfiguration emotes;

    private void removeAll(Player p) {
        Cooldowns.removeAll(p);
    }

    static Logger log = Bukkit.getLogger();

    static void debug(String msg) {
        if (debug) {
            log.info("[ChatFeelings] [Debug] " + msg);
        }
    }

    public void onDisable() {
        disabledsendingworlds.clear();
        disabledreceivingworlds.clear();

        lastreload = 0;
        lastmutelist = 0;

        if (Bukkit.getOnlinePlayers().size() > 0) {
            // Remove all HashMaps to prevent memory leaks if the plugin is reloaded when players are on.
            for (final Player online: Bukkit.getServer().getOnlinePlayers()) {
                removeAll(online.getPlayer());
            }
        }
    }

    private void purgeOldFiles() {
        boolean useclean = getConfig().getBoolean("Other.Player-Files.Cleanup");

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {

            File folder = new File(this.getDataFolder(), File.separator + "Data");
            if (!folder.exists()) {
                return;
            }

            int maxDays = getConfig().getInt("Other.Player-Files.Cleanup-After-Days");

            for (File cachefile: folder.listFiles()) {
                File f = new File(cachefile.getPath());

                if (f.getName().equalsIgnoreCase("global.yml")) {

                } else {

                    try {
                        FileConfiguration setcache = YamlConfiguration.loadConfiguration(f);

                        if (!setcache.contains("Last-On") || (!setcache.contains("Username")) || (!setcache.contains("UUID"))) {
                            f.delete();
                            debug("Deleted file: " + f.getName() + "... It was invalid!");
                        } else {

                            long daysAgo = Math
                                .abs(((setcache.getLong("Last-On")) / 86400000) - (System.currentTimeMillis() / 86400000));

                            String playername = setcache.getString("Username");
                            String uuid = setcache.getString("UUID");
                            String IPAdd = setcache.getString("IP");
                            UUID puuid = UUID.fromString(uuid);

                            int banInt = 0;

                            if (getConfig().getBoolean("Other.Player-Files.Erase-If-Banned")) {
                                banInt = isBanned(puuid, IPAdd);
                            } else {
                                banInt = 0;
                            }


                            if (banInt == 1) {
                                debug("Deleted " + playername + "'s data file. They were banned! (Essentials)");
                                f.delete();
                            } else if (banInt == 2) {
                                debug("Deleted " + playername + "'s data file. They were banned! (LiteBans)");
                                f.delete();
                            } else if (banInt == 3) {
                                debug("Deleted " + playername + "'s data file. They were banned! (AdvancedBan)");
                                f.delete();
                            } else if (banInt == 4) {
                                debug("Deleted " + playername + "'s data file. They were banned! (Vanilla)");
                                f.delete();
                            } else { // Ban int = 0 means not banned.

                                if (daysAgo >= maxDays && useclean) {
                                    f.delete();
                                    debug("Deleted " + playername + "'s data file because it's " + daysAgo +
                                        "s old. (Max is " + maxDays + " Days)");
                                } else {

                                    if (!setcache.contains("Muted") || !setcache.contains("Version")) {
                                        setcache.set("Muted", false);
                                        setcache.set("Version", 2);
                                        debug("Updated " + playername + "'s data file to work with new mute system.");

                                        try {
                                            setcache.save(f);
                                        } catch (Exception err) {}
                                    }

                                    if (useclean) {
                                        debug("Keeping " + playername + "'s data file. (" + daysAgo + "/" + maxDays +
                                            " days left)");
                                    } else {
                                        debug("Found " + playername + "'s data file. (" + daysAgo + " days");
                                    }

                                } // end of not too old check.
                            } // end of not banned check.
                        } // end of contains variables check.
                    } catch (Exception err) {
                        if (debug) {
                            debug("Error when trying to work with player file: " + f.getName() + ", see below:");
                            err.printStackTrace();
                        }
                    }
                } // end of if not global check
            } // end of For loop

        }); // End of Async;
    }

    private void updateConfig() {
        boolean confdebug = getConfig().getBoolean("Other.Debug");

        if (confdebug) {
            debug = true;
        } else {
            debug = false;
        }

        String version = Bukkit.getBukkitVersion().replace("-SNAPSHOT", "");

        if (getConfig().getBoolean("General.Sounds")) {
            sounds = true;
        } else {
            sounds = false;
        }

        if (getConfig().getBoolean("General.Particles")) {
            if (!supported && !version.contains("1.12")) {
                getLogger().warning("Particles were disabled. You're using " + version + " and not 1.12.X or higher.");
                particles = false;
            } else {
                debug("Using 1.12+, Particles have been enabled.");
                particles = true;
            }
        } else {
            particles = false;
        }

        if (getConfig().getBoolean("Other.Vanished-Players.Check")) {
            usevanishcheck = true;
        } else {
            usevanishcheck = false;
        }

        if (getConfig().contains("General.Use-Feeling-Permissions")) {
            if (getConfig().getBoolean("General.Use-Feeling-Permissions")) {
                useperms = true;
            } else {
                useperms = false;
            }
        } else {
            useperms = false;
        }

        if (getConfig().contains("General.Multi-Version-Support")) {
            if (getConfig().getBoolean("General.Multi-Version-Support")) {
                multiversion = true;
            } else {
                multiversion = false;
            }
        } else {
            multiversion = false;
        }
    }

    private void addMetrics() {
        if (!getConfig().getBoolean("Other.Metrics")) {
            debug("Metrics was disabled. Guess we won't support the developer today!");
            return;
        }

        double version = Double.parseDouble(System.getProperty("java.specification.version"));
        if (version < 1.8) {
            getLogger().warning(
                "Java " + Double.toString(version).replace("1.", "") + " detected. ChatFeelings requires Java 8 or higher to fully function.");
            getLogger().info("TIP: Use version v2.0.1 or below for legacy Java support.");
            return;
        }

        Metrics metrics = new Metrics(this, 1376);
        metrics.addCustomChart(new SimplePie("server_version", () -> {
            try {
                Class.forName("com.destroystokyo.paper.PaperConfig");
                return "Paper";
            } catch (Exception NotPaper) {
                try {
                    Class.forName("org.spigotmc.SpigotConfig");
                    return "Spigot";
                } catch (Exception Other) {
                    return "Bukkit / Other";
                }
            }
        }));

        metrics.addCustomChart(new SimplePie("update_notifications", () -> {
            if (getConfig().getBoolean("Other.Updates.Check")) {
                return "Enabled";
            } else {
                return "Disabled";
            }
        }));
    } // End Metrics

    protected void pop(CommandSender sender) {
        if (!sounds) {
            return;
        }

        if (sender instanceof Player) {
            Player p = (Player) sender;
            try {
            	p.playSound(p.getLocation(), Sound.ENTITY_CHICKEN_EGG, 2.0F, 2.0F);
            } catch (Exception err) {
            	sounds = false;
            }
        }
    }

    private void bass(CommandSender sender) {
        if (!sounds) {
            return;
        }

        if (sender instanceof Player) {
            Player p = (Player) sender;
            try {
            	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 2.0F, 1.3F);
            } catch (Exception err) {
            	sounds = false;
            }
        }
    }

    private void levelup(CommandSender sender) {
        if (!sounds) {
            return;
        }

        if (sender instanceof Player) {
            Player p = (Player) sender;
            try {
            	p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2.0F, 2.0F);
            } catch (Exception err) {
            	sounds = false;
            }
        }
    }

    public void configChecks() {
        if (getConfig().getBoolean("General.Radius.Enabled")) {
            if (getConfig().getInt("General.Radius.Radius-In-Blocks") == 0) {
                getLogger().warning("Feeling radius cannot be 0, disabling the radius.");
                getConfig().set("General.Radius.Radius-In-Blocks", 35);
                getConfig().set("General.Radius.Enabled", false);
                saveConfig();
                reloadConfig();
            }
        }

        if (getConfig().contains("Version")) {
            int ver = getConfig().getInt("Version");

            if (ver != 7) {

                if (ver <= 4)
                    if (getConfig().contains("Other.Bypass-Version-Block")) {
                        getConfig().set("Other.Bypass-Version-Block", null);
                    }

                getConfig().set("General.Use-Feeling-Permissions", true);
                getConfig().set("General.Multi-Version-Support", false);

                if (ver < 6) {
                    getConfig().set("General.No-Violent-Cmds-When-Sleeping", null);
                    getConfig().set("General.Use-Feeling-Permissions", true);
                    getConfig().set("General.Multi-Version-Support", false);
                    getConfig().set("General.Cooldowns.Ignore-List.Enabled", true);
                    getConfig().set("General.Cooldowns.Ignore-List.Seconds", 10);
                }

                if (ver < 7) {
                    getConfig().set("Cooldowns.Ignore-List.Enabled", null);
                    getConfig().set("Cooldowns.Ignore-List.Seconds", null);
                }

                getConfig().set("Version", 7);
                saveConfig();
                reloadConfig();
            }
        }
    }

    private void updateLastOn(Player p) {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            final String UUID = p.getUniqueId().toString();

            File cache = new File(this.getDataFolder(), File.separator + "Data");
            File f = new File(cache, File.separator + "" + UUID + ".yml");
            FileConfiguration setcache = YamlConfiguration.loadConfiguration(f);

            if (!f.exists()) {
                try {
                    setcache.save(f);
                } catch (Exception err) {}
            }

            String IPAdd = p.getAddress().getAddress().toString().replace(p.getAddress().getHostString() + "/", "").replace("/", "");
            int fileversion = setcache.getInt("Version");
            int currentfileversion = 2; // <--------------------- CHANGE when UPDATING

            if (!setcache.contains(UUID)) {
                setcache.set("UUID", UUID);
                setcache.set("Allow-Feelings", true);
                setcache.set("Muted", false);
            }

            if (fileversion != currentfileversion || !setcache.contains("Version")) {
                setcache.set("Version", currentfileversion);
            }

            setcache.set("IP", IPAdd);
            setcache.set("Username", p.getName());
            setcache.set("Last-On", System.currentTimeMillis());
            try {
                setcache.save(f);
            } catch (Exception err) {}
        });
    }

    private void statsAdd(Player p, String emotion) {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {

            // Global Stats -------------------------------------
            File folder = new File(this.getDataFolder(), File.separator + "Data");
            File fstats = new File(folder, File.separator + "global.yml");
            FileConfiguration setstats = YamlConfiguration.loadConfiguration(fstats);

            if (!fstats.exists()) {
                debug("Global stats file didn't exist, creating one now!");
                try {
                    setstats.save(fstats);
                } catch (Exception err) {}
            }

            setstats.set("Feelings.Sent." + emotion, setstats.getInt("Feelings.Sent." + emotion) + 1);
            try {
                setstats.save(fstats);
            } catch (Exception err) {}

            // Global Stats ----------------------------------

            final String UUID = p.getUniqueId().toString();

            File f = new File(folder, File.separator + "" + UUID + ".yml");
            FileConfiguration setcache = YamlConfiguration.loadConfiguration(f);

            if (!f.exists()) {
                return;
            }

            int ftotal = setcache.getInt("Stats.Sent." + emotion);
            int total = setcache.getInt("Stats.Sent.Total");

            setcache.set("Stats.Sent." + emotion, ftotal + 1);
            setcache.set("Stats.Sent.Total", total + 1);

            try {
                setcache.save(f);
            } catch (Exception err) {}
        });
    }

    public UUID hasPlayedNameGetUUID(String inputsearch) {
        File folder = new File(this.getDataFolder(), File.separator + "Data");

        for (File AllData: folder.listFiles()) {
            File f = new File(AllData.getPath());

            if (!f.getName().equalsIgnoreCase("global.yml")) {

                FileConfiguration setcache = YamlConfiguration.loadConfiguration(f);

                String playername = setcache.getString("Username");
                String u = setcache.getString("UUID");

                if (inputsearch.equalsIgnoreCase(playername)) {
                    return UUID.fromString(u);
                }
            }
        }
        // No Match Found
        return null;
    }

    private boolean isTargetIgnoringSender(Player target, Player sender) {
        File cache = new File(this.getDataFolder(), File.separator + "Data");
        File f = new File(cache, File.separator + "" + target.getUniqueId() + ".yml");
        FileConfiguration setcache = YamlConfiguration.loadConfiguration(f);

        List <String> ignoredplayers = new ArrayList <String> ();
        ignoredplayers.clear();
        ignoredplayers.addAll(setcache.getStringList("Ignoring"));

        if (ignoredplayers.contains(sender.getUniqueId().toString())) {
            ignoredplayers.clear();
            return true;
        }

        ignoredplayers.clear();
        return false;
    }

    public String hasPlayedUUIDGetName(UUID uuid) {
        File cache = new File(this.getDataFolder(), File.separator + "Data");
        File f = new File(cache, File.separator + "" + uuid + ".yml");
        if(!f.exists()) {
        	return "0";
        }
        FileConfiguration setcache = YamlConfiguration.loadConfiguration(f);
        return setcache.getString("Username", "0");
    }

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        
        if (!supported) {
            getLogger().info("---------------------------------------------------");
            getLogger().info("This version of ChatFeelings is only compatible with: 1.18-1.13");
            getLogger().info("While ChatFeelings may work with " + version + ", it is not supported.");
            getLogger().info(" ");
            getLogger().info("If you continue, you understand that you will get no support, and");
            getLogger().info("that some features, such as sounds, may disable to continue working.");
            getLogger().info("");
            getLogger().warning("[!] IF YOU GET BUGS/ERRORS, DO NOT REPORT THEM.");
            getLogger().info("---------------------------------------------------");
        }

        if (version.contains("1.8") || version.contains("1.7") || version.contains("1.6") || version.contains("1.5") || version.contains("1.4")) {
            getLogger().warning("1.8 or below may have severe issues with this version of ChatFeelings, please use this version:");
            getLogger().warning("https://www.spigotmc.org/resources/chatfeelings.12987/download?version=208840");
        }

        api = new ChatFeelingsAPI();

        getConfig().options().copyDefaults(true);
        saveConfig();

        if (supported) {
            getConfig().options().header(
                "Thanks for downloading ChatFeelings!\nMessages for feelings can be found in the Emotes.yml, and other message in the Messages.yml.\n\nHaving trouble? Join our support discord: https://discord.gg/6ugXPfX");
            debug("Setting 'supported' header in the config. Using 1.13+");
        } else {
            debug("Setting 'unsupported' header in the config. Using below 1.13.");
            getConfig().options().header(
                "Thanks for downloading ChatFeelings!\nMessages for feelings can be found in the Emotes.yml, and other message in the Messages.yml.\n\nDO NOT REPORT BUGS, YOU ARE USING AN UNSUPPORTED MIENCRAFT VERSION.");
        }
        saveConfig();

        disabledsendingworlds.clear();
        disabledreceivingworlds.clear();
        disabledsendingworlds.addAll(getConfig().getStringList("General.Disabled-Sending-Worlds"));
        disabledreceivingworlds.addAll(getConfig().getStringList("General.Disabled-Receiving-Worlds"));

        Bukkit.getServer().getPluginManager().registerEvents(this, this);

        debug("Disabled Sending Worlds: " + disabledsendingworlds.toString());
        debug("Disabled Receiving Worlds: " + disabledreceivingworlds.toString());

        addMetrics();

        if (getConfig().getBoolean("Other.Updates.Check")) {
            try {
                new Updater(this).checkForUpdate();
            } catch (Exception e) {
                getLogger().warning("There was an issue while trying to check for updates.");
            }
        } else {
            getLogger().info("[!] Update checking has been disabled in the config.yml");
        }

        FileSetup.enableFiles();

        int onlinecount = Bukkit.getOnlinePlayers().size();
        if (onlinecount >= 1) {
            for (Player online: Bukkit.getOnlinePlayers()) {
                removeAll(online);
                updateLastOn(online); // Generates files for players who are on during restart that didn't join
                // normally.
            }
            debug("Reloaded with " + onlinecount + " players online... Skipping purge.");
        } else {
            purgeOldFiles();
        }

        configChecks();
        if (supported) {
            getLogger().info("Having issues? Got a question? Join our support discord: https://discord.gg/6ugXPfX");
        } else {
            debug("Not showing support discord link. They are using " + version + " :(");
        }

        if (this.getServer().getPluginManager().isPluginEnabled("LiteBans") &&
            this.getServer().getPluginManager().getPlugin("LiteBans") != null) {
            getLogger().info("Hooking into LiteBans...");
            haslitebans = true;
        }

        if (this.getServer().getPluginManager().isPluginEnabled("AdvancedBan") &&
            this.getServer().getPluginManager().getPlugin("AdvancedBan") != null) {
            getLogger().info("Hooking into AdvancedBans...");
            hasadvancedban = true;
        }

        if (this.getServer().getPluginManager().isPluginEnabled("Essentials") &&
            this.getServer().getPluginManager().getPlugin("Essentials") != null) {
            hasess = true;
            getLogger().info("Hooking into Essentials...");
        }
        
        if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI") &&
                this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
                getLogger().info("Hooking into PlaceholderAPI...");
                new Placeholders(this).register();
                NicknamePlaceholders.enablePlaceholders(getConfig(), msg);
            }

        updateConfig();

        debug("Finished! ChatFeelings was loaded in " + Long.toString(System.currentTimeMillis() - start) + "ms");

        start = 0;
        lastreload = System.currentTimeMillis();

    } // [!] End of OnEnable Event

    private int isBanned(UUID uuid, String IPAdd) {

        if (isABBanned(uuid)) {
            return 3;
        }

        if (isLiteBanBanned(uuid, IPAdd)) {
            return 2;
        }

        if (isEssBanned(uuid)) {
            return 1;
        }

        if (isVanillaBanned(uuid)) {
            return 4;
        }

        return 0;
    }

    private int isMuted(UUID uuid, String IPAdd) {
        if (isABMuted(uuid)) {
            return 3;
        }

        if (isLiteBanMuted(uuid, IPAdd)) {
            return 2;
        }

        if (isEssMuted(uuid)) {
            return 1;
        }

        return 0; // 0 in this case means no mute was found.
    }


    // FOR API ---------------------------------
    public boolean APIhasAB() {
        return hasadvancedban;
    }
    public boolean APIhasLB() {
        return haslitebans;
    }
    public boolean APIhasEss() {
        return hasess;
    }

    public boolean APIisMutedUUIDBoolean(UUID uuid) {
        if (isMuted(uuid, null) != 0) {
            return true;
        }
        return false;
    }

    public boolean APIisBannedUUIDBoolean(UUID uuid) {
        if (isBanned(uuid, null) != 0) {
            return true;
        }
        return false;
    }

    public int APIgetSentStat(UUID u, String feeling) {
    	if(!feelings.contains(feeling.toLowerCase())) {
    		return 0;
    	}
        File cache = new File(this.getDataFolder(), File.separator + "Data");
        File f = new File(cache, File.separator + "" + u + ".yml");
        if(!f.exists()) {
        	return 0;
        }
        FileConfiguration setcache = YamlConfiguration.loadConfiguration(f);

        return setcache.getInt("Stats.Sent." + StringUtils.capitalize(feeling.toLowerCase()));
    }
    
    public List<String> APIgetFeelings() {
        return feelings;
    }

    public int APIgetTotalSent(UUID u) {
    	File cache = new File(this.getDataFolder(), File.separator + "Data");
        File f = new File(cache, File.separator + "" + u + ".yml");
        if(!f.exists()) {
        	return 0;
        }
        FileConfiguration setcache = YamlConfiguration.loadConfiguration(f);
        return setcache.getInt("Stats.Sent.Total");
    }
    
    public boolean APIisAcceptingFeelings(UUID u) {
    	File cache = new File(this.getDataFolder(), File.separator + "Data");
        File f = new File(cache, File.separator + "" + u + ".yml");
        FileConfiguration setcache = YamlConfiguration.loadConfiguration(f);
        return setcache.getBoolean("Allow-Feelings");
    }

    // END OF API CALLS ------------------------------------

    private boolean isEssMuted(UUID uuid) {
        try {
            if (hasess) {
                Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
                if (ess.getUser(uuid).isMuted()) {
                    return true;
                }
            }
            return false;
        } catch (Exception err) {
            if (debug && !punishmentError) {
            	punishmentError = true;
            	debug("Essentials isMuted Error:");
                err.printStackTrace();
            }
            return false;
        }
    }

    private boolean isLiteBanMuted(UUID uuid, String IPAdd) {
        try {
            if (haslitebans) {
                if (Database.get().isPlayerMuted(uuid, IPAdd)) {
                    return true;
                }
            }
            return false;
        } catch (Exception err) {
            if (debug && !punishmentError) {
            	punishmentError = true;
            	debug("LiteBan isMuted Error:");
                err.printStackTrace();
            }
            return false;
        }
    }

    private boolean isABMuted(UUID uuid) {
        try {
            if (hasadvancedban) {
                if (PunishmentManager.get().isMuted(uuid.toString())) {
                    return true;
                }
            }
            return false;
        } catch (Exception err) {
            if (debug && !punishmentError) {
            	punishmentError = true;
            	debug("AdvancedBan isMuted Error:");
                err.printStackTrace();
            }
            return false;
        }
    }

    private boolean isVanillaBanned(UUID uuid) {
        if (Bukkit.getOfflinePlayer(uuid).isBanned()) {
            return true;
        }

        return false;
    }

    private boolean isEssBanned(UUID uuid) {
        try {
            if (hasess) {
                Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
                if (ess.getUser(uuid).getBase().isBanned()) {
                    return true;
                }
            }
            return false;
        } catch (Exception err) {
            if (debug && !punishmentError) {
            	punishmentError = true;
            	debug("Essentials isBanned Error:");
                err.printStackTrace();
            }
            return false;
        }
    }

    private boolean isLiteBanBanned(UUID uuid, String IPAdd) {
        try {
            if (haslitebans) {
                if (Database.get().isPlayerBanned(uuid, IPAdd)) {
                    return true;
                }
            }
            return false;
        } catch (Exception err) {
        	if (debug && !punishmentError) {
            	punishmentError = true;
            	debug("LiteBans isBanned Error:");
                err.printStackTrace();
            }
            return false;
        }
    }

    private boolean isABBanned(UUID uuid) {
        try {
            if (hasadvancedban) {						// Requires UUID as string.
                if (PunishmentManager.get().isBanned(uuid.toString())) {
                    return true;
                }
            }
            return false;
        } catch (Exception err) {
        	if (debug && !punishmentError) {
            	punishmentError = true;
            	debug("AdvancedBan isBanned Error:");
                err.printStackTrace();
            }
            return false;
        }
    }

    private boolean isVanished(Player player) {
        if (usevanishcheck) {
            try {

                if (hasess) {
                    Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
                    if (ess.getVanishedPlayers().contains(player.getName())) {
                        return true;
                    }
                }

                for (MetadataValue meta: player.getMetadata("vanished")) {
                    if (meta.asBoolean())
                        return true;
                }

            } catch (Exception err) {
                getLogger().warning("Couldn't check for vanished players. Disabling this check until next restart.");
                usevanishcheck = false;
            }

            if (getConfig().getBoolean("Other.Vanished-Players.Use-Legacy")) {
                if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    return true;
                }

                return false;
            }

            return false;
        } else { // Vanish Check is Off
            return false;
        }
    }

    private void getStats(CommandSender p, UUID uuid, boolean isown) {
        String your = "";

        File cache = new File(folder, File.separator + "Data");
        File f = new File(cache, File.separator + "" + uuid + ".yml");
        FileConfiguration setcache = YamlConfiguration.loadConfiguration(f);
        final String name = setcache.getString("Username");
        if (isown) {
            Msgs.send(p, msg.getString("Stats-Header-Own").replace("%player%", name));
            your = "&7Your ";
        } else {
            Msgs.send(p, msg.getString("Stats-Header-Other").replace("%player%", name));
            your = "&7";
        }
        for (String fl : feelings) {
        	final String flcap = fl.substring(0,1).toUpperCase() + fl.substring(1).toLowerCase();
        	Msgs.send(p, "&f   &8&l> " + your + flcap +"s: &f&l" + setcache.getInt("Stats.Sent." + flcap));
        }
    	Msgs.send(p, "&f   &8&l> &eTotal Sent: &f&l" + setcache.getInt("Stats.Sent.Total"));
    }

    private void noPermission(CommandSender sender) {
        Msgs.sendPrefix(sender, msg.getString("No-Permission"));
        bass(sender);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
    	final String cmdlr = cmd.getName().toLowerCase();
        if (cmdlr.equals("chatfeelings") && args.length == 0) {
            Msgs.send(sender, "");
            Msgs.send(sender, "&a&lC&r&ahat &f&lF&r&feelings");
            Msgs.send(sender, "&8&l> &7/cf help &7&ofor commands & settings.");
            Msgs.send(sender, "");
            pop(sender);
            return true;
        }

        if (cmdlr.equals("chatfeelings") && args.length >= 1 && args[0].equalsIgnoreCase("version")) {
            Msgs.send(sender, "");
            Msgs.send(sender, "&a&lC&r&ahat &f&lF&r&feelings");
            Msgs.send(sender, "&8&l> &7You are currently running &f&lv" + getDescription().getVersion());
            Msgs.send(sender, "");
            pop(sender);
            return true;
        }

        if (cmdlr.equals("chatfeelings") && args.length >= 1 && args[0].equalsIgnoreCase("stats")) {
            if (!sender.hasPermission("chatfeelings.stats") && !sender.hasPermission("chatfeelings.stats.others") && !sender.isOp()) {
                noPermission(sender);
                return true;
            }

            if (args.length == 1) {
                if (!(sender instanceof Player)) {
                    Msgs.sendPrefix(sender, msg.getString("No-Player"));
                    return true;
                }

                final Player p = (Player)sender;
                getStats(sender, p.getUniqueId(), true);
                pop(sender);
                return true;
            }

            if (!sender.hasPermission("chatfeelings.stats.others") && !sender.isOp()) {
                noPermission(sender);
                return true;
            }

            final UUID getUUID = hasPlayedNameGetUUID(args[1]);
            if (getUUID == null) {

                if (args[1].equalsIgnoreCase("console")) {
                    Msgs.sendPrefix(sender, msg.getString("Console-Not-Player"));
                    bass(sender);
                    return true;
                }

                bass(sender);
                Msgs.sendPrefix(sender, msg.getString("Player-Never-Joined").replace("%player%", args[1]));
                return true;
            }

            getStats(sender, getUUID, false);
            pop(sender);
            return true;
        }

        if (cmdlr.equals("chatfeelings") && args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("chatfeelings.admin") && !sender.isOp()) {
                noPermission(sender);
                return true;
            }

            long secsLeft = ((lastreload / 1000) + 10) - (System.currentTimeMillis() / 1000);
            if (secsLeft > 0) {
                Msgs.sendPrefix(sender, "&7Please wait &f&l" + secsLeft + "s &7until reloading again.");
                bass(sender);
                return true;
            }

            lastreload = System.currentTimeMillis();
            final long starttime = System.currentTimeMillis();

            Msgs.send(sender, "");
            Msgs.send(sender, "&a&lC&r&ahat &f&lF&r&feelings");

            try {
                reloadConfig();

                disabledsendingworlds.clear();
                disabledreceivingworlds.clear();
                disabledsendingworlds.addAll(getConfig().getStringList("General.Disabled-Sending-Worlds"));
                disabledreceivingworlds.addAll(getConfig().getStringList("General.Disabled-Receiving-Worlds"));

                FileSetup.enableFiles();
                configChecks();

            } catch (Exception err2) {
                if (debug) {
                    getLogger().info("Error occured when trying to reload your config: ----------");
                    err2.printStackTrace();
                    getLogger().info("-----------------------[End of Error]-----------------------");
                    Msgs.send(sender, "&8&l> &4&lError! &fSomething in your config isn't right. Check console!");
                } else {
                    Msgs.send(sender, "&8&l> &4&lError! &fSomething in your ChatFeelings files is wrong.");
                }
                bass(sender);
                usevanishcheck = true;
                return true;
            }

            updateConfig();

            int onlinecount = Bukkit.getServer().getOnlinePlayers().size();
            if (onlinecount == 0) {
                debug("Purging old data files since nobody is currently online...");
                purgeOldFiles();
            }
            if (!disabledsendingworlds.isEmpty()) {
                debug("Sending Feelings is disabled in: " + disabledsendingworlds.toString());
            }
            if (!disabledreceivingworlds.isEmpty()) {
                debug("Receiving Feelings is disabled in: " + disabledreceivingworlds.toString());
            }

            try {
                long reloadtime = System.currentTimeMillis() - starttime;
                if (reloadtime >= 1000) {
                    double reloadsec = reloadtime / 1000;
                    // Lets hope nobody's reload takes more than 1000ms (1s). However it's not unheard of .-.
                    Msgs.send(sender, msg.getString("Reload").replace("%time%", Double.toString(reloadsec) + "s"));
                    if (sender instanceof Player) {
                        getLogger().info("Configuration & Files reloaded by " + sender.getName() + " in " + reloadsec + "s");
                    }
                } else {
                    Msgs.send(sender, msg.getString("Reload").replace("%time%", Long.toString(reloadtime) + "ms"));
                    if (sender instanceof Player) {
                        getLogger().info("Configuration & Files reloaded by " + sender.getName() + " in " + reloadtime + "ms");
                    }
                }
            } catch (Exception err) {
                Msgs.send(sender, "&8&l> &a&l✓  &7Plugin Reloaded. &c(1 file was regenerated)");
            }
            Msgs.send(sender, "");
            levelup(sender);

            return true;
        }

        if (cmdlr.equals("chatfeelings") && args.length >= 1 && args[0].equalsIgnoreCase("help")) {
            Msgs.send(sender, "");
            Msgs.send(sender, "&a&lC&r&ahat &f&lF&r&feelings");
            Msgs.send(sender, "&8&l> &e&l/cf help &7Shows you this page.");
            if (sender.hasPermission("chatfeelings.ignore") || sender.isOp()) {
                Msgs.send(sender, "&8&l> &e&l/cf ignore (player) &7Ignore/Unignore feelings from players.");
                Msgs.send(sender, "&8&l> &e&l/cf ignore all &7Toggles everyone being able to use feelings.");
            }
            if (sender.hasPermission("chatfeelings.stats") || sender.isOp()) {
                if (!sender.hasPermission("chatfeelings.stats.others") && !sender.isOp()) {
                    Msgs.send(sender, "&8&l> &e&l/cf stats &7Shows your feeling statistics.");
                } else {
                    Msgs.send(sender, "&8&l> &e&l/cf stats (player) &7Shows your a players statistics.");
                }
            }
            if (sender.hasPermission("chatfeelings.mute") || sender.isOp()) {
                Msgs.send(sender, "&8&l> &e&l/cf mute (player) &7Prevents a player from using feelings.");
                Msgs.send(sender, "&8&l> &e&l/cf unmute (player) &7Unmutes a muted player.");
                Msgs.send(sender, "&8&l> &e&l/cf mutelist &7Shows who's currently muted.");
            }
            if (sender.hasPermission("chatfeelings.admin") || sender.isOp()) {
                Msgs.send(sender, "&8&l> &e&l/cf version &7Shows you the plugin version.");
                Msgs.send(sender, "&8&l> &e&l/cf reload &7Reloads the plugin.");
            }
            Msgs.send(sender, "&8&l> &6&l/feelings &7Shows a list of available feelings.");
            Msgs.send(sender, "");
            pop(sender);
            return true;
        }

        if (cmdlr.equals("chatfeelings") && args.length >= 1 && args[0].equalsIgnoreCase("uuid")) {
            if (!sender.hasPermission("chatfeelings.admin") && !sender.isOp()) {
                noPermission(sender);
                return true;
            }

            if (args.length == 1) {
                Msgs.sendPrefix(sender, msg.getString("No-Player"));
                bass(sender);
                return true;
            }

            final UUID getUUID = hasPlayedNameGetUUID(args[1]);
            if (getUUID == null) {

                if (args[1].equalsIgnoreCase("console")) {
                    Msgs.sendPrefix(sender, msg.getString("Console-Not-Player"));
                    bass(sender);
                    return true;
                }

                bass(sender);
                Msgs.sendPrefix(sender, msg.getString("Player-Never-Joined").replace("%player%", args[1]));
                return true;
            }

            String getName = hasPlayedUUIDGetName(getUUID);
            Msgs.sendPrefix(sender, "&fThe UUID of " + getName + " is &7" + getUUID);
            pop(sender);
            return true;
        }

        if (cmdlr.equals("chatfeelings") && args.length >= 1 && args[0].equalsIgnoreCase("mutelist")) {
            if (!sender.hasPermission("chatfeelings.mute") && !sender.isOp()) {
                noPermission(sender);
                return true;
            }

            File datafolder = new File(this.getDataFolder(), File.separator + "Data");

            if (!datafolder.exists()) {
                Msgs.sendPrefix(sender, msg.getString("Folder-Not-Found"));
                bass(sender);
                return true;
            }
            final long secsLeft = ((lastmutelist / 1000) + 60) - (System.currentTimeMillis() / 1000);
            if (secsLeft > 0) {
                Msgs.sendPrefix(sender, "&7Please wait &f&l" + secsLeft + "s &7before checking the mute list.");
                return true;
            }
            // We need a 60s global cooldown incase they use MySQL. Doing this command w/ MySQL can suck up LOTS of CPU.
            if (haslitebans || hasadvancedban) {
                lastmutelist = System.currentTimeMillis();
            }
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                Msgs.send(sender, "");
                Msgs.send(sender, msg.getString("Mute-List-Header"));

                int totalmuted = 0;

                for (File cachefile: datafolder.listFiles()) {
                    File f = new File(cachefile.getPath());

                    if (!f.getName().equalsIgnoreCase("global.yml")) {
                        FileConfiguration setcache = YamlConfiguration.loadConfiguration(f);

                        String uuid = setcache.getString("UUID");
                        String IPAdd = setcache.getString("IP");
                        UUID puuid = UUID.fromString(uuid);

                        int muteInt = isMuted(puuid, IPAdd);

                        if (setcache.contains("Muted") && setcache.contains("Username")) {
                            if (setcache.getBoolean("Muted")) {
                                totalmuted++;
                                if (muteInt == 3) {
                                    Msgs.send(sender, msg.getString("Mute-List-Player").replace("%player%", (String) setcache.get("Username")) + " &c(AdvancedBan & CF)");
                                } else if (muteInt == 2) {
                                    Msgs.send(sender, msg.getString("Mute-List-Player").replace("%player%", (String) setcache.get("Username")) + " &c(LiteBans & CF)");
                                } else if (muteInt == 1) {
                                    Msgs.send(sender, msg.getString("Mute-List-Player").replace("%player%", (String) setcache.get("Username")) + " &c(Essentials & CF)");
                                } else {
                                    Msgs.send(sender, msg.getString("Mute-List-Player").replace("%player%", (String) setcache.get("Username")));
                                }
                            } else {
                                if (muteInt == 3) {
                                    totalmuted++;
                                    Msgs.send(sender, msg.getString("Mute-List-Player").replace("%player%", (String) setcache.get("Username")) + " &c(AdvancedBan)");
                                } else
                                if (muteInt == 2) {
                                    totalmuted++;
                                    Msgs.send(sender, msg.getString("Mute-List-Player").replace("%player%", (String) setcache.get("Username")) + " &c(LiteBans)");
                                } else
                                if (muteInt == 1) {
                                    totalmuted++;
                                    Msgs.send(sender, msg.getString("Mute-List-Player").replace("%player%", (String) setcache.get("Username")) + " &c(Essentials)");
                                }
                            }
                        }
                    }
                }

                if (totalmuted == 1) {
                    Msgs.send(sender, msg.getString("Mute-List-Total-One").replace("%total%", "1"));
                } else if (totalmuted == 0) {
                    Msgs.send(sender, msg.getString("Mute-List-Total-Zero").replace("%total%", "0"));
                } else {
                    Msgs.send(sender, msg.getString("Mute-List-Total-Many").replace("%total%", Integer.toString(totalmuted)));
                }
                Msgs.send(sender, "");
            });
            pop(sender);
            return true;
        }

        if (cmdlr.equals("chatfeelings") && args.length >= 1 && args[0].equalsIgnoreCase("unmute")) {
            if (!sender.hasPermission("chatfeelings.mute") && !sender.isOp()) {
                noPermission(sender);
                if (getConfig().contains("General.Extra-Help") && msg.contains("No-Perm-Mute-Suggestion")) {
                    if (getConfig().getBoolean("General.Extra-Help")) {
                        Msgs.sendPrefix(sender, msg.getString("No-Perm-Mute-Suggestion"));
                    }
                }
                return true;
            }

            if (args.length == 1) {
                Msgs.sendPrefix(sender, msg.getString("No-Player-Unmute"));
                bass(sender);
                return true;
            }

            final UUID muteUUID = hasPlayedNameGetUUID(args[1]);

            if (muteUUID == null) {
                bass(sender);
                Msgs.sendPrefix(sender, msg.getString("Player-Never-Joined").replace("%player%", args[1]));
                return true;
            }


            File datafolder = new File(this.getDataFolder(), File.separator + "Data");

            if (!datafolder.exists()) {
                Msgs.sendPrefix(sender, msg.getString("Folder-Not-Found"));
                bass(sender);
                return true;
            }

            File f = new File(datafolder, File.separator + "" + muteUUID + ".yml");
            FileConfiguration setcache = YamlConfiguration.loadConfiguration(f);

            if (!f.exists()) {
                try {
                    Msgs.sendPrefix(sender, "&cSorry!&f We couldn't find that player's file.");
                    bass(sender);
                    return true;
                } catch (Exception err) {}
            }

            if (!setcache.contains("Muted")) {
                Msgs.sendPrefix(sender, "&cOutdated Data. &fPlease erase your ChatFeeling's &7&lData &ffolder & try again.");
            }

            final String playername = setcache.getString("Username");
            final String uuid = setcache.getString("UUID");
            final String IPAdd = setcache.getString("IP");
            final UUID puuid = UUID.fromString(uuid);

            if (setcache.getBoolean("Muted")) {
                setcache.set("Muted", false);

                try {
                    setcache.save(f);
                } catch (Exception err) {
                    getLogger().warning("Unable to save " + playername + "'s data file:");
                    err.printStackTrace();
                    getLogger().warning("-----------------------------------------------------");
                    getLogger().warning("Please message us on discord or spigot about this error.");
                }

                Msgs.sendPrefix(sender, msg.getString("Player-Has-Been-Unmuted").replace("%player%", playername));
                pop(sender);
            } else if (!setcache.getBoolean("Muted")) {
                bass(sender);
                Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                    final int muteInt = isMuted(puuid, IPAdd);
                    if (muteInt == 3) {
                        Msgs.sendPrefix(sender, msg.getString("Player-Muted-Via-AdvancedBan"));
                    } else
                    if (muteInt == 2) {
                        Msgs.sendPrefix(sender, msg.getString("Player-Muted-Via-LiteBans"));
                    } else
                    if (muteInt == 1) {
                        Msgs.sendPrefix(sender, msg.getString("Player-Muted-Via-Essentials"));
                    } else {
                        Msgs.sendPrefix(sender, msg.getString("Player-Already-Unmuted"));
                    }
                });
            } else {
                bass(sender);
                Msgs.sendPrefix(sender, "&c&lError. &fWe couldn't find mute status in your data files.");
                getLogger().warning("Something went wrong when trying to get " + sender.getName() + "'s (un)mute status in the player file.");
            }

            return true;
        }

        if (cmdlr.equals("chatfeelings") && args.length >= 1 && args[0].equalsIgnoreCase("mute")) {
            if (!sender.hasPermission("chatfeelings.mute") && !sender.isOp()) {
                noPermission(sender);
                if (getConfig().contains("General.Extra-Help") && msg.contains("No-Perm-Mute-Suggestion")) {
                    if (getConfig().getBoolean("General.Extra-Help")) {
                        Msgs.sendPrefix(sender, msg.getString("No-Perm-Mute-Suggestion"));
                    }
                }
                return true;
            }

            if (args.length == 1) {
                Msgs.sendPrefix(sender, msg.getString("No-Player-Mute"));
                bass(sender);
                return true;
            }

            final UUID muteUUID = hasPlayedNameGetUUID(args[1]);

            if (muteUUID == null) {
                bass(sender);
                Msgs.sendPrefix(sender, msg.getString("Player-Never-Joined").replace("%player%", args[1]));
                return true;
            }

            File datafolder = new File(this.getDataFolder(), File.separator + "Data");

            if (!datafolder.exists()) {
                Msgs.sendPrefix(sender, msg.getString("Folder-Not-Found"));
                bass(sender);
                return true;
            }

            File f = new File(datafolder, File.separator + "" + hasPlayedNameGetUUID(args[1]).toString() + ".yml");
            FileConfiguration setcache = YamlConfiguration.loadConfiguration(f);

            if (!f.exists()) {
                Msgs.sendPrefix(sender, "&cSorry!&f We couldn't find that player's file.");
                bass(sender);
                return true;
            }

            if (!setcache.contains("Muted")) {
                Msgs.sendPrefix(sender, "&cOutdated Data. &fPlease erase your ChatFeeling's &7&lData &ffolder & try again.");
            }

            final String playername = setcache.getString("Username");
            final String uuid = setcache.getString("UUID");
            final String IPAdd = setcache.getString("IP");
            final UUID puuid = UUID.fromString(uuid);

            if (args[1].equalsIgnoreCase(sender.getName())) {
                bass(sender);
                Msgs.sendPrefix(sender, msg.getString("Cant-Mute-Self"));
                return true;
            }

            if (!setcache.getBoolean("Muted")) {
                setcache.set("Muted", true);
                try {
                    setcache.save(f);
                } catch (Exception err) {
                    getLogger().warning("Unable to save " + playername + "'s data file:");
                    err.printStackTrace();
                    getLogger().warning("-----------------------------------------------------");
                    getLogger().warning("Please message us on discord or spigot about this error.");
                }
                Msgs.sendPrefix(sender, msg.getString("Player-Has-Been-Muted").replace("%player%", playername));
                Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                    final int muteInt = isMuted(puuid, IPAdd);
                    if (muteInt != 0) {
                        Msgs.sendPrefix(sender, msg.getString("Extra-Mute-Present").replace("%player%", playername));
                    }
                });
                pop(sender);
            } else if (setcache.getBoolean("Muted")) {
                bass(sender);
                Msgs.sendPrefix(sender, msg.getString("Player-Already-Muted"));
                if (getConfig().contains("General.Extra-Help") && msg.contains("Already-Mute-Unmute-Suggestion")) {
                    if (getConfig().getBoolean("General.Extra-Help")) {
                        Msgs.sendPrefix(sender, msg.getString("Already-Mute-Unmute-Suggestion"));
                    }
                }
            } else {
                bass(sender);
                Msgs.sendPrefix(sender, "&cError. &fWe couldn't find your mute status in your data file.");
                getLogger().warning("Something went wrong when trying to get " + sender.getName() + "'s mute status in the player file.");
            }

            return true;
        }

        if (cmdlr.equals("chatfeelings") && args.length >= 1 && args[0].equalsIgnoreCase("ignore")) {
            if (!sender.hasPermission("chatfeelings.ignore") && !sender.isOp()) {
                noPermission(sender);
                return true;
            }

            if (!(sender instanceof Player)) {
                Msgs.sendPrefix(sender, "&c&lSorry. &fOnly players can ignore other players.");
                return true;
            }

            Player p = (Player) sender;

            if (args.length == 1) {
                if (Cooldowns.ignorelistcooldown.containsKey(p)) {
                    Msgs.sendPrefix(sender, msg.getString("Ignore-List-Cooldown"));
                    bass(sender);
                    return true;
                }

                Cooldowns.ignoreListCooldown(p);

                File datafolder = new File(this.getDataFolder(), File.separator + "Data");

                if (!datafolder.exists()) {
                    Msgs.sendPrefix(sender, msg.getString("Folder-Not-Found"));
                    bass(sender);
                    return true;
                }

                File f = new File(datafolder, File.separator + "" + p.getUniqueId().toString() + ".yml");
                FileConfiguration setcache = YamlConfiguration.loadConfiguration(f);

                List <String> ignoredplayers = new ArrayList <String> ();
                ignoredplayers.addAll(setcache.getStringList("Ignoring"));

                Msgs.send(sender, " ");
                Msgs.send(sender, msg.getString("Ignore-List-Header"));
                if (ignoredplayers.size() == 0) {
                    if (setcache.getBoolean("Allow-Feelings")) {
                        Msgs.send(sender, msg.getString("Ignore-List-None"));
                    } else {
                        Msgs.send(sender, msg.getString("Ignore-List-All"));
                    }
                } else {
                    for (String ignoredUUID: ignoredplayers) {
                        String name = hasPlayedUUIDGetName(UUID.fromString(ignoredUUID));
                        if (name != null && name != "0") {
                            Msgs.send(sender, "  &8&l> &f&l" + name);
                        }
                    }
                }

                Msgs.send(sender, " ");
                ignoredplayers.clear();
                pop(sender);
                return true;
            }

            if (args[1].equalsIgnoreCase(sender.getName())) {
                bass(sender);
                Msgs.sendPrefix(sender, msg.getString("Cant-Ignore-Self"));
                return true;
            }

            if (getConfig().getBoolean("General.Cooldowns.Ignoring.Enabled") && !sender.isOp() && !sender.hasPermission("chatfeelings.bypasscooldowns")) {
                if (Cooldowns.ignorecooldown.containsKey(p)) {
                    bass(sender);
                    Msgs.sendPrefix(sender, msg.getString("Ignore-Cooldown"));
                    return true;
                }

                Cooldowns.ignoreCooldown(p);
            }

            File datafolder = new File(this.getDataFolder(), File.separator + "Data");

            if (!datafolder.exists()) {
                Msgs.sendPrefix(sender, msg.getString("Folder-Not-Found"));
                bass(sender);
                return true;
            }

            File f = new File(datafolder, File.separator + "" + p.getUniqueId().toString() + ".yml");
            FileConfiguration setcache = YamlConfiguration.loadConfiguration(f);

            if (!f.exists()) {
                try {
                    Msgs.sendPrefix(sender, "&cSorry!&f We couldn't find your player file.");
                    bass(sender);
                    return true;
                } catch (Exception err) {}
            }

            if (args[1].equalsIgnoreCase("all")) {
                if (setcache.getBoolean("Allow-Feelings")) {
                    setcache.set("Allow-Feelings", false);
                    Msgs.sendPrefix(sender, msg.getString("Ingoring-On-All"));
                } else {
                    setcache.set("Allow-Feelings", true);
                    Msgs.sendPrefix(sender, msg.getString("Ingoring-Off-All"));
                }

                pop(sender);

                try {
                    setcache.save(f);
                } catch (Exception err) {}
                return true;
            }

            List <String> ignoredplayers = new ArrayList  <String> ();
            ignoredplayers.clear();
            ignoredplayers.addAll(setcache.getStringList("Ignoring"));

            final UUID ignoreUUID = hasPlayedNameGetUUID(args[1]);
            if (ignoreUUID == null) {

                if (args[1].equalsIgnoreCase("console")) {
                    Msgs.sendPrefix(sender, msg.getString("Console-Not-Player"));
                    bass(sender);
                    return true;
                }

                bass(sender);
                Msgs.sendPrefix(sender, msg.getString("Player-Never-Joined").replace("%player%", args[1]));
                return true;
            }
            
            final String iuuids = ignoreUUID.toString();
            
            try {
                if (ignoredplayers.contains(iuuids)) {
                    Msgs.sendPrefix(sender, msg.getString("Ingoring-Off-Player").replace("%player%", args[1]));

                    ignoredplayers.remove(iuuids);
                    setcache.set("Ignoring", ignoredplayers);
                    try {
                        setcache.save(f);
                    } catch (Exception err) {}

                    pop(sender);
                    ignoredplayers.clear();
                    return true;
                }
            } catch (Exception searcherr) {
                getLogger().warning("Error trying to search for: " + args[1] + " in the Data folder.");
            }

            ignoredplayers.add(ignoreUUID.toString());
            setcache.set("Ignoring", ignoredplayers);
            try {
                setcache.save(f);
            } catch (Exception err) {}
            Msgs.sendPrefix(sender, msg.getString("Ingoring-On-Player").replace("%player%", args[1]));
            pop(sender);
            ignoredplayers.clear();
            return true;
        }

        if (cmdlr.equals("feelings")) {
            final String path = "Command_Descriptions.";
            if ((args.length == 0) ||
                (args.length >= 1 && (args[0].equalsIgnoreCase("1") || args[0].equalsIgnoreCase("0")))) {
                Msgs.send(sender, "");
                Msgs.send(sender, msg.getString("Feelings-Help") + "                        " +
                    msg.getString("Feelings-Help-Page").replace("%page%", "1").replace("%pagemax%", "2"));
                Msgs.send(sender, "&8&l> &f&l/hug (player) &7 " + msg.getString(path + "Hug"));
                Msgs.send(sender, "&8&l> &f&l/slap (player) &7 " + msg.getString(path + "Slap"));
                Msgs.send(sender, "&8&l> &f&l/poke (player) &7 " + msg.getString(path + "Poke"));
                Msgs.send(sender, "&8&l> &f&l/highfive (player) &7 " + msg.getString(path + "Highfive"));
                Msgs.send(sender, "&8&l> &f&l/facepalm (player) &7 " + msg.getString(path + "Facepalm"));
                Msgs.send(sender, "&8&l> &f&l/yell (player) &7 " + msg.getString(path + "Yell"));
                Msgs.send(sender, "&8&l> &f&l/bite (player) &7 " + msg.getString(path + "Bite"));
                Msgs.send(sender, "&8&l> &f&l/snuggle (player) &7 " + msg.getString(path + "Snuggle"));
                Msgs.send(sender, "&8&l> &f&l/shake (player) &7 " + msg.getString(path + "Shake"));
                Msgs.send(sender, "&8&l> &f&l/stab (player) &7 " + msg.getString(path + "Stab"));
                Msgs.send(sender, "&7To go to the 2nd page do &a/feelings 2");
                pop(sender);
                Msgs.send(sender, "");
            } else if (args.length >= 1 && args[0].equalsIgnoreCase("2")) {
                Msgs.send(sender, "");
                Msgs.send(sender, msg.getString("Feelings-Help") + "                        " +
                    msg.getString("Feelings-Help-Page").replace("%page%", "2").replace("%pagemax%", "2"));
                Msgs.send(sender, "&8&l> &f&l/kiss (player) &7 " + msg.getString(path + "Kiss"));
                Msgs.send(sender, "&8&l> &f&l/punch (player) &7 " + msg.getString(path + "Punch"));
                Msgs.send(sender, "&8&l> &f&l/murder (player) &7 " + msg.getString(path + "Murder"));
                Msgs.send(sender, "&8&l> &f&l/boi (player) &7 " + msg.getString(path + "Boi"));
                Msgs.send(sender, "&8&l> &f&l/cry (player) &7 " + msg.getString(path + "Cry"));
                Msgs.send(sender, "&8&l> &f&l/dab (player) &7 " + msg.getString(path + "Dab"));
                Msgs.send(sender, "&8&l> &f&l/lick (player) &7 " + msg.getString(path + "Lick"));
                Msgs.send(sender, "&8&l> &f&l/pat (player) &7 " + msg.getString(path + "Pat"));
                Msgs.send(sender, "&8&l> &f&l/stalk (player) &7 " + msg.getString(path + "Stalk"));	 	
                pop(sender);
                Msgs.send(sender, "");
            } else {
                bass(sender);
                Msgs.sendPrefix(sender, msg.getString("Page-Not-Found"));
            }
            return true;
        }

        if (feelings.contains(cmdlr)) {

            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                if (sender instanceof Player && useperms) {
                    if (!sender.hasPermission("chatfeelings." + cmdlr) && !sender.hasPermission("chatfeelings.all") && !sender.isOp()) {
                        noPermission(sender);
                        return;
                    }
                }

                if (getConfig().getBoolean("General.Cooldowns.Feelings.Enabled") && !sender.isOp() && !sender.hasPermission("chatfeelings.bypasscooldowns")) {
                    if (sender instanceof Player) {
                        Player p = (Player) sender;
                        if (Cooldowns.cooldown.containsKey(p.getPlayer())) {
                            int cooldownTime = getConfig().getInt("General.Cooldowns.Feelings.Seconds");
                            long secondsLeft = ((Cooldowns.cooldown.get(p.getPlayer()) / 1000) + cooldownTime) - (System.currentTimeMillis() / 1000);
                            if (secondsLeft > 0) {
                                Msgs.sendPrefix(sender, msg.getString("Cooldown-Active").replace("%time%",
                                    Long.toString(secondsLeft) + "s"));
                                bass(sender);
                                return;
                            }
                        }
                    }
                }

                if (args.length == 0) {
                    Msgs.sendPrefix(sender, msg.getString("No-Player"));
                    bass(sender);
                    return;
                }

                final String cmdconfig = (StringUtils.capitalize(cmd.getName()));

                if (sender instanceof Player) {
                    Player p = (Player) sender;
                    if (disabledsendingworlds.contains(p.getWorld().getName())) {
                        bass(sender);
                        Msgs.sendPrefix(sender, msg.getString("Sending-World-Disabled"));
                        return;
                    }
                }

                if (!emotes.getBoolean("Feelings." + cmdconfig + ".Enable")) {
                    bass(sender);
                    Msgs.sendPrefix(sender, msg.getString("Emote-Disabled"));
                    return;
                }

                if (args.length < 1) {
                    Msgs.sendPrefix(sender, msg.getString("No-Player"));
                    bass(sender);
                    return;
                }

                if (args[0].equalsIgnoreCase("console")) {
                    Msgs.sendPrefix(sender, msg.getString("Console-Not-Player"));
                    bass(sender);
                    return;
                }
                
                final Player target = Bukkit.getServer().getPlayer(args[0]);

                if (target == null || isVanished(target)) {
                    bass(sender);
                    Msgs.sendPrefix(sender, msg.getString("Player-Offline").replace("%player%", args[0]));
                    return;
                }

                if (target.getName().equalsIgnoreCase(sender.getName())) {
                    if (getConfig().getBoolean("General.Prevent-Self-Feelings")) {
                        bass(sender);
                        Msgs.sendPrefix(sender, msg.getString("Sender-Is-Target").replace("%command%", cmdconfig));
                        return;
                    }
                }

                if (disabledreceivingworlds.contains(target.getWorld().getName())) {
                    bass(sender);
                    Msgs.sendPrefix(sender, msg.getString("Receiving-World-Disabled"));
                    return;
                }

                // Radius & Sleeping Check ---------------------------
                if (sender instanceof Player) {
                    final Player p = (Player) sender;
                    if (getConfig().getBoolean("General.Radius.Enabled")) {
                        final String omsg = msg.getString("Outside-Of-Radius").replace("%player%", target.getName()).replace("%command%", cmd.getName());
                        if (target.getWorld() != p.getWorld()) {
                            Msgs.sendPrefix(sender, omsg);
                            bass(sender);
                            return;
                        }
                        Double distance = p.getLocation().distance(target.getLocation());
                        Double radius = getConfig().getDouble("General.Radius.Radius-In-Blocks");
                        if (distance > radius) {
                            debug(sender.getName() + " was outside the radius of " + radius + ". (They're " + distance + ")");
                            Msgs.sendPrefix(sender, omsg);
                            bass(sender);
                            return;
                        }
                    }
                }


                // Ignoring & Mute Check ----------------
                if (sender instanceof Player) {
                    final Player p = (Player) sender;

                    File cache = new File(this.getDataFolder(), File.separator + "Data");
                    File f = new File(cache, File.separator + "" + p.getUniqueId() + ".yml");
                    FileConfiguration setcache = YamlConfiguration.loadConfiguration(f);

                    final int muteInt = isMuted(p.getUniqueId(), null);

                    if (muteInt != 0) {
                        if (muteInt == 3) {
                            debug("" + sender.getName() + " tried to use /" + cmdLabel + ", but is muted by AdvancedBan.");
                        }
                        if (muteInt == 2) {
                            debug("" + sender.getName() + " tried to use /" + cmdLabel + ", but is muted by LiteBans.");
                        }
                        if (muteInt == 1) {
                            debug("" + sender.getName() + " tried to use /" + cmdLabel + ", but is muted by Essentials.");
                        }
                        bass(sender);
                        Msgs.sendPrefix(sender, msg.getString("Is-Muted"));
                        return;
                    }


                    if (f.exists()) {
                        if (setcache.getBoolean("Muted")) {
                            debug("" + sender.getName() + " tried to use /" + cmdLabel + ", but was muted (via CF).");
                            bass(sender);
                            Msgs.sendPrefix(sender, msg.getString("Is-Muted"));
                            return;
                        }

                        if (!setcache.getBoolean("Allow-Feelings")) {
                            bass(sender);
                            Msgs.sendPrefix(sender, msg.getString("Target-Is-Ignoring-All"));
                            debug(sender.getName() + " couldn't send feeling to " + target.getName() + " because they are ignoring ALL.");
                            return;
                        }

                        if (isTargetIgnoringSender(target, p)) {
                            bass(sender);
                            Msgs.sendPrefix(sender,
                                msg.getString("Target-Is-Ignoring").replace("%player%", target.getName()));

                            debug("Not sending feeling to " + target.getName() + " because they are ignoring " + p.getName());
                            return;
                        }
                    }
                } else {
                    File cache = new File(this.getDataFolder(), File.separator + "Data");
                    File f = new File(cache, File.separator + "" + target.getUniqueId() + ".yml");
                    FileConfiguration setcache = YamlConfiguration.loadConfiguration(f);

                    if (f.exists()) {
                        if (!setcache.getBoolean("Allow-Feelings")) {
                            Msgs.sendPrefix(sender, msg.getString("Target-Is-Ignoring-All"));
                            debug("Blocking CONSOLE from sending feeling because " + target.getName() + " is blocking ALL.");
                            return;
                        } // Sender is Console however the player is still blocking ALL feelings.
                    }
                }
                // ------------------------------------------------


                // FEELING HANDLING IS ALL BELOW -------------------------------------------------------------------------------
                
                // API Events ----------------------------
                Bukkit.getScheduler().runTask(this, () -> {
                	FeelingSendEvent fse = new FeelingSendEvent(sender, target, cmdconfig);
                	Bukkit.getPluginManager().callEvent(fse);
                	if (fse.isCancelled()) {
                		return;
                	}

                	FeelingRecieveEvent fre = new FeelingRecieveEvent(target, sender, cmdconfig);
                	Bukkit.getPluginManager().callEvent(fre);
                	if (fre.isCancelled()) {
                		return;
                	}
                });

                // End of API events (Except for Global event below ---------------------

                // Global Handler for PLAYER messages & Feelings ----------------------------
                if (getConfig().getBoolean("General.Global-Feelings.Enabled")) {

                    for (final Player online: Bukkit.getServer().getOnlinePlayers()) {

                        // Global Ignoring Checks -----------------
                        File cache = new File(this.getDataFolder(), File.separator + "Data");
                        File f = new File(cache, File.separator + "" + online.getUniqueId() + ".yml");
                        FileConfiguration setcache = YamlConfiguration.loadConfiguration(f);

                        if (!setcache.getBoolean("Allow-Feelings") && (online.getName() != sender.getName())) {
                            debug("" + online.getName() + " is blocking all feelings. Skipping Global Msg!");
                        } else { // else NOT ignoring ALL
                            if (sender instanceof Player) {
                                Player p = (Player) sender;
                                if (isTargetIgnoringSender(target, p)) {
                                    // Player is Ignoring from sender but is not target. (GlobaL)

                                    // This works but is unused. Need to remove later.
                                    debug("" + online.getName() + " is blocking feelings from " + p.getName() + ". Skipping global msg!");
                                }
                            }
                            // End of Global ignoring Checks -------------------



                            if (sender.getName().equalsIgnoreCase("console") || !(sender instanceof Player)) {
                                // ONLY for CONSOLE Global notify here.
                                Msgs.send(online.getPlayer(),NicknamePlaceholders.replacePlaceholders(emotes.getString("Feelings." + cmdconfig + ".Msgs.Global"),sender,target));
                            } else {
                                // Global for PLAYER below
                                if (sender instanceof Player) {
                                    Player p = (Player) sender;
                                    if (!setcache.getStringList("Ignoring").contains(p.getUniqueId().toString())) {
                                    	Bukkit.getScheduler().runTask(this, () -> {
                                    		FeelingGlobalNotifyEvent fgne = new FeelingGlobalNotifyEvent(online, sender, target, cmdconfig);
                                    		Bukkit.getPluginManager().callEvent(fgne);
                                    		
                                        if (!fgne.isCancelled()) {
                                        	Msgs.send(online.getPlayer(),NicknamePlaceholders.replacePlaceholders(emotes.getString("Feelings." + cmdconfig + ".Msgs.Global"),sender,target));
                                        }
                                      });

                                    } // end of check to make sure message is sent to those NOT ignoring the player
                                } // end of if player confirmation (just a safeguard)
                            }
                        } // end of else for ignore global check
                    } // end of for(online)
                    // End --------------------------------------------------

                    // Global Console Broadcast Msg ------------------------------------------------
                    if (getConfig().getBoolean("General.Global-Feelings.Broadcast-To-Console")) {
                        if (sender.getName().equalsIgnoreCase("console")) {
                            Msgs.send(getServer().getConsoleSender(),
                                emotes.getString("Feelings." + cmdconfig + ".Msgs.Global")
                                .replace("%sender%", msg.getString("Console-Name"))
                                .replace("%target%", target.getName()));
                        } else {
                            Msgs.send(getServer().getConsoleSender(),
                                emotes.getString("Feelings." + cmdconfig + ".Msgs.Global")
                                .replace("%sender%", sender.getName()).replace("%target%", target.getName()));
                        }
                    }
                    // Global Console End --------------------------------------------------

                } else {
                    // if not global (normal)
                
                    Msgs.send(target.getPlayer(), NicknamePlaceholders.replacePlaceholders(emotes.getString("Feelings." + cmdconfig + ".Msgs.Target"),sender,target));

                    Msgs.send(sender, NicknamePlaceholders.replacePlaceholders(emotes.getString("Feelings." + cmdconfig + ".Msgs.Sender"),sender,target)); // sender (not global)
                } // end of global else

                // Special Effect Command Handlers -----------------------------
                if (getConfig().getBoolean("General.Violent-Command-Harm")) {
                    if (cmdlr.equals("slap") || cmdlr.equals("bite") ||
                        cmdlr.equals("shake") || cmdlr.equals("stab") ||
                        cmdlr.equals("punch") || cmdlr.equals("murder")) {
                        try {
                            if (!target.isSleeping()) {
                                target.damage(0.01D);
                            } else {
                                debug("Skipped damage to " + target.getName() + ", as they were sleeping.");
                            }
                        } catch (Exception err) {
                            debug("Unable to damage player: " + target.getName());
                        }
                    }
                }

                // ------------------------------------------------------

                // Cooldown Handler ------------------------------------
                if (getConfig().getBoolean("General.Cooldowns.Feelings.Enabled")) {
                    if (sender instanceof Player) {
                        Player p = (Player) sender;
                        Cooldowns.putCooldown(p);
                    }
                }
                // -----------------------------------------------------

                // Particle Handler -------------------------------------
                if (particles) {
                    try {
                        Particles.show(target, cmdlr);
                    } catch (Exception parterr) {
                    	if(debug) {
                    		parterr.printStackTrace();
                    	}
                    	particles = false;
                        getLogger().warning("Couldn't display '" + cmd.getName().toUpperCase() + "' particles to " + target.getName() + ". Make sure you use 1.12 or higher.");
                    }
                }
                // -----------------------------------------------------

                // Sound Handler ----------------------------------------
                if (sounds) {
                    try {
                        String sound1 = emotes.getString("Feelings." + cmdconfig + ".Sounds.Sound1.Name");
                        if (!sound1.equalsIgnoreCase("none") &&
                            !sound1.equalsIgnoreCase("off") &&
                            sound1 != null &&
                            sound1 != "null") {

                            target.playSound(target.getPlayer().getLocation(),
                                Sound.valueOf(sound1),
                                (float) emotes.getDouble("Feelings." + cmdconfig + ".Sounds.Sound1.Volume"),
                                (float) emotes.getDouble("Feelings." + cmdconfig + ".Sounds.Sound1.Pitch"));
                            if (sender instanceof Player) {
                                Player p = (Player) sender;
                                p.playSound(p.getLocation(),
                                    Sound.valueOf(sound1),
                                    (float) emotes.getDouble("Feelings." + cmdconfig + ".Sounds.Sound1.Volume"),
                                    (float) emotes.getDouble("Feelings." + cmdconfig + ".Sounds.Sound1.Pitch"));
                            }
                        }


                        String sound2 = emotes.getString("Feelings." + cmdconfig + ".Sounds.Sound2.Name");
                        if (!sound2.equalsIgnoreCase("none") &&
                            !sound2.equalsIgnoreCase("off") &&
                            sound2 != null &&
                            sound2 != "null") {

                        	if(sound2.contains("DISC") && !multiversion) {
        						// Check for SPOOK, that runs an ALT sound to prevent needing to stop it. (For Multi Version support)
        						target.playSound(target.getPlayer().getLocation(),
        								Sound.AMBIENT_CAVE,
        								2.0F, 0.5F);
        					} else {
                            target.playSound(target.getPlayer().getLocation(),
                                Sound.valueOf(sound2),
                                (float) emotes.getDouble("Feelings." + cmdconfig + ".Sounds.Sound2.Volume"),
                                (float) emotes.getDouble("Feelings." + cmdconfig + ".Sounds.Sound2.Pitch"));

                            if (sender instanceof Player && !sound2.contains("DISC")) {
                                Player p = (Player) sender;
                                p.playSound(p.getLocation(),
                                    Sound.valueOf(sound2),
                                    (float) emotes.getDouble("Feelings." + cmdconfig + ".Sounds.Sound2.Volume"),
                                    (float) emotes.getDouble("Feelings." + cmdconfig + ".Sounds.Sound2.Pitch"));
                            }
                        }
                        }
                    } catch (Exception sounderr) { // err test for sounds
                        getLogger().warning("One or more of your sounds for /" + cmdconfig + " are incorrect. See below:");
                        sounderr.printStackTrace();
                        getLogger().info("This happens when the sound values don't match the version of MC. This is not a bug. Sounds will now disable.");
                        sounds = false;
                    }
                } // end of config sound check
                // ---------- End of Sounds

                // Add Stats
                if (sender instanceof Player) {
                    Player p = (Player) sender;
                    statsAdd(p, cmdconfig);
                }
            });
            // End Stats
            return true;
        }

        if (cmdlr.equals("chatfeelings") && args.length >= 1) {
            Msgs.send(sender, "");
            Msgs.send(sender, "&a&lC&r&ahat &f&lF&r&feelings");
            Msgs.send(sender, "&8&l> &c&lHmm. &7That command does not exist.");
            Msgs.send(sender, "");
            if (sender instanceof Player) {
                Player p = (Player) sender;
                bass(p.getPlayer());
            }
        }

        return true;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        String name = p.getName();        
        if (!Cooldowns.playerFileUpdate.contains(name)) {
            updateLastOn(p);
            Cooldowns.justJoined(name);
        } else {
            debug("Skipped updating " + name + "'s file, they joined less than 60s ago.");
        }

        removeAll(p);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            Player p = e.getPlayer();
            String name = p.getName();

            try {
                if (getConfig().getBoolean("Other.Updates.Check")) {
                    if (p.hasPermission("chatfeelings.admin") || p.isOp()) {
                        if (Updater.isOutdated()) {
                            Msgs.sendPrefix(p, "&c&lOutdated Plugin! &7Running v" + getDescription().getVersion() +
                                " while the latest is &f&l" + Updater.getOutdatedVersion());
                        }
                    }
                }
            } catch (Exception err) {}

            if (!Cooldowns.playerFileUpdate.contains(name)) {
                updateLastOn(p);
                Cooldowns.justJoined(name);
            }

            if (p.getUniqueId().toString().equals("6191ff85-e092-4e9a-94bd-63df409c2079")) {
                Msgs.send(p, "&7This server is running &fChatFeelings &6v" + getDescription().getVersion() +
                    " &7for " + version);
            }
        });
    }
}