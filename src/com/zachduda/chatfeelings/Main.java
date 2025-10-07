package com.zachduda.chatfeelings;

import com.earth2me.essentials.Essentials;
import com.zachduda.chatfeelings.api.*;
import com.zachduda.chatfeelings.other.Supports;
import com.zachduda.chatfeelings.other.Updater;
import litebans.api.Database;
import me.leoko.advancedban.manager.PunishmentManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimpleBarChart;
import org.bstats.charts.SimplePie;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import space.arim.morepaperlib.MorePaperLib;
import space.arim.morepaperlib.scheduling.FoliaDetection;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

public class Main extends JavaPlugin implements Listener, TabExecutor {
    /* If true, metrics & update checking are skipped. */
    final public static boolean beta = false;
    final public static boolean folia = isFolia();

    public ChatFeelingsAPI api;

    public MorePaperLib morePaperLib = new MorePaperLib(this);

    public final static List< String > feelings = Arrays.asList(
            "hug",
            "slap",
            "poke",
            "highfive",
            "facepalm",
            "yell",
            "bite",
            "snuggle",
            "shake",
            "stab",
            "kiss",
            "punch",
            "murder",
            "cry",
            "boi",
            "dab",
            "lick",
            "scorn",
            "pat",
            "stalk",
            "sus",
            "wave",
            "welcomeback",
            "boop",
            "spook"
        );

    private boolean hasess = false;
    private boolean haslitebans = false;
    private boolean hasadvancedban = false;
    private static boolean usevanishcheck = false;

    protected static boolean particles = true;

    private static boolean useperms = false;

    protected static boolean multiversion = false;
    public static boolean reducemsgs = false;
    protected static boolean debug = false;

    private static boolean sounds = false;
    private static boolean punishmentError = false;
    private Metrics metrics;

    private long lastreload = 0;
    private long lastmutelist = 0;
    private final List <String> disabledsendingworlds = getConfig().getStringList("General.Disable-Sending-Worlds");
    private final List <String> disabledreceivingworlds = getConfig().getStringList("General.Disable-Receiving-Worlds");

    final static String discord_link = "zachduda.com/discord";
    File folder;
    File msgsfile;
    public FileConfiguration msg;

    File emotesfile;
    public FileConfiguration emotes;


    private void removeAll(Player p) {
        Cooldowns.removeAll(p);
    }

    static Logger log = Bukkit.getLogger();
    private static final String logtag = "[ChatFeelings] ";

    public static void log(String msg, Boolean critical, Boolean warning) {
        if (critical || !reducemsgs) {
            if(warning) {
                log.warning("[!] " + logtag + msg);
                return;
            }
            log.info( logtag + msg);
        }
    }

    public static void debug(String msg) {
        if (debug) {
            log("[Debug] " + msg, true, false);
        }
    }

    public static boolean debug() {
        return debug;
    }

    public void onDisable() {
        disabledsendingworlds.clear();
        disabledreceivingworlds.clear();

        lastreload = 0;
        lastmutelist = 0;

        if (!Bukkit.getOnlinePlayers().isEmpty()) {
            // Remove all HashMaps to prevent memory leaks if the plugin is reloaded when players are on.
            for (final Player online: Bukkit.getServer().getOnlinePlayers()) {
                removeAll(online.getPlayer());
            }
        }
        if(metrics != null) {
            metrics.shutdown();
        } else {
            debug("Metrics were disabled or are NULL, skipping Metric shutdown call...");
        }

        morePaperLib.scheduling().cancelGlobalTasks();
    }

    public static String capitalizeString(String string) {
        char[] chars = string.toLowerCase().toCharArray();
        boolean found = false;
        for (int i = 0; i < chars.length; i++) {
            if (!found && Character.isLetter(chars[i])) {
                chars[i] = Character.toUpperCase(chars[i]);
                found = true;
            } else if (Character.isWhitespace(chars[i]) || chars[i]=='.' || chars[i]=='\'') { // You can add other chars here
                found = false;
            }
        }
        return String.valueOf(chars);
    }

    private void purgeOldFiles() {
        boolean useclean = getConfig().getBoolean("Other.Player-Files.Cleanup");

        morePaperLib.scheduling().asyncScheduler().run(() -> {

            File folder = new File(this.getDataFolder(), File.separator + "Data");
            if (!folder.exists()) {
                return;
            }

            int maxDays = getConfig().getInt("Other.Player-Files.Cleanup-After-Days");

            for (File cachefile: Objects.requireNonNull(folder.listFiles())) {
                File f = new File(cachefile.getPath());

                if(f.getName().toLowerCase().contains(".ds_store")) {
                    // Ignore MAC OS created files in the DATA folder.
                    return;
                }

                if (!f.getName().equalsIgnoreCase("global.yml")) {
                    try {
                        FileConfiguration setcache = YamlConfiguration.loadConfiguration(f);

                        if (!setcache.contains("Last-On") || (!setcache.contains("Username")) || (!setcache.contains("UUID"))) {
                            if(f.delete()) {
                                debug("Deleted file: " + f.getName() + "... It was invalid!");
                            } else {
                                debug("Unable to delete invalid player file: " + f.getName());
                            }
                        } else {

                            long daysAgo = Math
                                    .abs(((setcache.getLong("Last-On")) / 86400000) - (System.currentTimeMillis() / 86400000));

                            String playername = setcache.getString("Username");
                            String uuid = setcache.getString("UUID");
                            String IPAdd = setcache.getString("IP");
                            assert uuid != null;
                            UUID puuid = UUID.fromString(uuid);

                            int banInt;

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
                                        } catch (Exception err) {
                                            if(debug) {
                                                log("Unable to update file:", true, true);
                                                err.printStackTrace();
                                            }
                                        }
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

    public static void updateConfigHeaders(JavaPlugin pl) {
        final boolean supported = Supports.isSupported();
        final String confgreeting = "Thanks for downloading ChatFeelings!\n# Messages for feelings can be found in the Emotes.yml, and other message in the Messages.yml.\n";
        final String nosupport = "# DO NOT REPORT BUGS, YOU ARE USING AN UNSUPPORTED MINECRAFT VERSION.\n";
        try {
            List < String > confighead = new ArrayList<>();
            confighead.add(confgreeting);
            if (supported) {
                confighead.add("# Having trouble? Join our support discord: " + discord_link);
                pl.getConfig().options().setHeader(confighead);
                debug("Setting 'supported' header in the config. Using 1.13+");
            } else {
                confighead.add(nosupport);
                debug("Setting 'unsupported' header in the config. Using below 1.13.");
                pl.getConfig().options().setHeader(confighead);
            }
        } catch (NoSuchMethodError e) {
            // Using less than Java 18 will use this method instead.
            try {
                if (supported) {
                    //noinspection deprecation
                    pl.getConfig().options().header(confgreeting);
                } else {
                    //noinspection deprecation
                    pl.getConfig().options().header(confgreeting + nosupport);
                }
                debug("Using older java that doesn't support non deprecated method. Use old file method.");
            } catch (Exception giveup) {
                debug("Unable to set configuration greeting. Method removed: " + giveup.getMessage());
            }
        }
        pl.saveConfig();
        configChecks(pl);
        if (supported && !reducemsgs) {
            log("Having issues? Got a question? Join our support discord: " + discord_link, false, false);
        } else if(!supported) {
            debug("Not showing support discord link. They are using a version that's not supported :(");
        }
    }

    public static void updateConfig(JavaPlugin pl) {
        debug = pl.getConfig().getBoolean("Other.Debug");
        sounds = pl.getConfig().getBoolean("General.Sounds");
        final String lvu = pl.getConfig().getString("LVU");

        if(lvu == null || lvu.isEmpty()) {
            pl.getConfig().set("LVU", Supports.getMCVersion());
        } else {
            if(!lvu.equals(Supports.getMCVersion())) {
                log.warning("------------ CHECK YOUR EMOTES.YML SOUNDS, RESET IT IF NECESSARY TO PREVENT ERRORS ------------ ");
                log.warning("Your server was running " + lvu + " and is now running " + Supports.getMCVersion() + ".");
                log.warning("Sound values may need to be changed in ChatFeeling's emotes.yml, or erasing this file altogether.");
                log.warning("------------------------------------------------------------------");
                pl.getConfig().set("LVU", Supports.getMCVersion());
            }
        }

        if (pl.getConfig().getBoolean("General.Particles")) {
            if (!Supports.isSupported()) {
                log("Particles were disabled. You're using " + Supports.getMCVersion() + " and not 1.12 or higher.", false, true);
                particles = false;
            } else {
                debug("Using 1.12+, Particles have been enabled.");
                particles = true;
            }
        } else {
            particles = false;
        }

        usevanishcheck = pl.getConfig().getBoolean("Other.Vanished-Players.Check");

        if (pl.getConfig().contains("General.Use-Feeling-Permissions")) {
            useperms = pl.getConfig().getBoolean("General.Use-Feeling-Permissions");
        } else {
            useperms = false;
        }

        if (pl.getConfig().contains("General.Multi-Version-Support")) {
            multiversion = pl.getConfig().getBoolean("General.Multi-Version-Support");
        } else {
            multiversion = false;
        }
        if (pl.getConfig().contains("Other.CF-Alias")) {
            boolean cfalias = pl.getConfig().getBoolean("Other.CF-Alias", true);
        }
    }

    public boolean hasPerm(CommandSender p, String node, Boolean admin_cmd) {
        return (!(p instanceof Player)) || (!node.equalsIgnoreCase("none") && p.hasPermission(node)) || p.isOp() || (!admin_cmd && !useperms) || (feelings.contains(node.replaceAll("chatfeelings.", "")) && p.hasPermission("chatfeelings.all"));
    }

    public boolean hasPerm(CommandSender p, String node) {
        return hasPerm(p, node, false);
    }
    public boolean hasPerm(CommandSender p, Boolean admin_cmd) {
        return hasPerm(p, "none", admin_cmd);
    }
    public boolean hasPerm(CommandSender p) {
        return hasPerm(p, "none", false);
    }

    private static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    private Metrics addMetrics() {
        if(beta) {
            debug("Metrics were not enabled as this is a pre-release version.");
            return null;
        }
        if (!getConfig().getBoolean("Other.Metrics")) {
            debug("Metrics was disabled. Guess we won't support Zach today. :(");
            return null;
        }

        double version = Double.parseDouble(System.getProperty("java.specification.version"));
        if (version < 1.8) {
            log("Java " + Double.toString(version).replace("1.", "") + " detected. ChatFeelings requires Java 8 or higher to fully function.", true, true);
            log("TIP: Use version v2.0.1 or below for legacy Java support.", false, false);
            return null;
        }

        metrics = new Metrics(this, 1376);
        metrics.addCustomChart(new SimplePie("server_version", () -> {
            if(isFolia()) {
                return "Folia";
            } else {
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
            }
        }));

        metrics.addCustomChart(new SimplePie("update_notifications", () -> {
            if (getConfig().getBoolean("Other.Updates.Check")) {
                return "Enabled";
            } else {
                return "Disabled";
            }
        }));

        metrics.addCustomChart(new SimpleBarChart("feeling_usage", () -> {
            File folder = new File(getDataFolder(), File.separator + "Data");
            File fstats = new File(folder, File.separator + "global.yml");
            FileConfiguration setstats = YamlConfiguration.loadConfiguration(fstats);

            Map<String, Integer> map = new HashMap<>();
            for (String fl : feelings) {
                final String flc = capitalizeString(fl);
                map.put(flc, setstats.getInt("Feelings.Sent." + flc, setstats.getInt("Feelings.Sent." + flc) + 1));
            }
            return map;
        }));

        return metrics;

    } // End Metrics

    protected void pop(CommandSender sender) {
        if (!sounds) {
            return;
        }

        if (sender instanceof Player) {
            final Player p = (Player)sender;
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
            try {
                final Player p = (Player)sender;
                morePaperLib.scheduling().globalRegionalScheduler().run(() -> p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 2.0F, 1.3F));
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
            try {
                final Player p = (Player)sender;
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2.0F, 2.0F);
            } catch (Exception err) {
                sounds = false;
            }
        }
    }

        public static void configChecks(JavaPlugin pl) {
        if (pl.getConfig().getBoolean("General.Radius.Enabled")) {
            if (pl.getConfig().getInt("General.Radius.Radius-In-Blocks") == 0) {
                log("Feeling radius cannot be 0, disabling the radius.", true, true);
                pl.getConfig().set("General.Radius.Radius-In-Blocks", 35);
                pl.getConfig().set("General.Radius.Enabled", false);
                pl.saveConfig();
                pl.reloadConfig();
            }
        }

        if (pl.getConfig().contains("Version")) {
            int ver = pl.getConfig().getInt("Version");

            if (ver != 8) {

                if (ver <= 4)
                    if (pl.getConfig().contains("Other.Bypass-Version-Block")) {
                        pl.getConfig().set("Other.Bypass-Version-Block", null);
                    }

                pl.getConfig().set("General.Use-Feeling-Permissions", true);
                pl.getConfig().set("General.Multi-Version-Support", false);

                if (ver < 6) {
                    pl.getConfig().set("General.No-Violent-Cmds-When-Sleeping", null);
                    pl.getConfig().set("General.Use-Feeling-Permissions", true);
                    pl.getConfig().set("General.Multi-Version-Support", false);
                    pl.getConfig().set("General.Cooldowns.Ignore-List.Enabled", true);
                    pl.getConfig().set("General.Cooldowns.Ignore-List.Seconds", 10);
                }

                if (ver < 7) {
                    pl.getConfig().set("Cooldowns.Ignore-List.Enabled", null);
                    pl.getConfig().set("Cooldowns.Ignore-List.Seconds", null);
                }

                if(ver < 8) {
                    pl.getConfig().set("Other.CF-Alias", true);
                }

                pl.getConfig().set("Version", 8);
                pl.saveConfig();
                pl.reloadConfig();
            }
        }
    }

    private void updateLastOn(Player p) {
        morePaperLib.scheduling().asyncScheduler().run(() -> {
            final String UUID = p.getUniqueId().toString();

            File cache = new File(this.getDataFolder(), File.separator + "Data");
            File f = new File(cache, File.separator + UUID + ".yml");
            FileConfiguration setcache = YamlConfiguration.loadConfiguration(f);

            if (!f.exists()) {
                try {
                    setcache.save(f);
                } catch (Exception err) {
                    if(debug) {
                        log("Unable to update last seen var in player file:",true,true);
                        err.printStackTrace();
                    }
                }
            }

            String IPAdd = Objects.requireNonNull(p.getAddress()).getAddress().toString().replace(p.getAddress().getHostString() + "/", "").replace("/", "");
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
            } catch (Exception err) {
                if(debug) {
                    log("Unable to update player file:", true, true);
                    err.printStackTrace();
                }
            }
        });
    }

    private void statsAdd(Player p, String emotion) {
        morePaperLib.scheduling().asyncScheduler().run(() -> {

            // Global Stats -------------------------------------
            File folder = new File(getDataFolder(), File.separator + "Data");
            File fstats = new File(folder, File.separator + "global.yml");
            FileConfiguration setstats = YamlConfiguration.loadConfiguration(fstats);

            if (!fstats.exists()) {
                debug("Global stats file didn't exist, creating one now!");
                try {
                    setstats.save(fstats);
                } catch (Exception err) {
                    if(debug) {
                        log("Unable to create or save global stats file:", true, true);
                        err.printStackTrace();
                    }
                }
            }

            setstats.set("Feelings.Sent." + emotion, setstats.getInt("Feelings.Sent." + emotion) + 1);
            try {
                setstats.save(fstats);
            } catch (Exception err) {}

            // Global Stats ----------------------------------

            final String UUID = p.getUniqueId().toString();

            File f = new File(folder, File.separator + UUID + ".yml");
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
        if(!folder.exists()) {
            return null;
        }
        try {
            for (File AllData : Objects.requireNonNull(folder.listFiles())) {
                File f = new File(AllData.getPath());

                if (!f.getName().equalsIgnoreCase("global.yml")) {

                    FileConfiguration setcache = YamlConfiguration.loadConfiguration(f);

                    String playername = setcache.getString("Username");
                    String u = setcache.getString("UUID");

                    if (inputsearch.equalsIgnoreCase(playername)) {
                        return UUID.fromString(Objects.requireNonNull(u));
                    }
                }
            }
        } catch (NullPointerException err) {
            // No match found, data files are missing.
            return null;
        }
        // No Match Found
        return null;
    }

    private boolean isTargetIgnoringSender(Player target, Player sender) {
        File cache = new File(this.getDataFolder(), File.separator + "Data");
        if(!cache.exists()) {
            return false;
        }
        File f = new File(cache, File.separator + target.getUniqueId() + ".yml");
        FileConfiguration setcache = YamlConfiguration.loadConfiguration(f);

        List<String> ignoredplayers = new ArrayList<>(setcache.getStringList("Ignoring"));

        if (ignoredplayers.contains(sender.getUniqueId().toString())) {
            ignoredplayers.clear();
            return true;
        }

        ignoredplayers.clear();
        return false;
    }

    public String hasPlayedUUIDGetName(UUID uuid) {
        File cache = new File(this.getDataFolder(), File.separator + "Data");
        if(!cache.exists()) {
            return "0";
        }
        File f = new File(cache, File.separator + uuid + ".yml");
        if (!f.exists()) {
            return "0";
        }
        FileConfiguration setcache = YamlConfiguration.loadConfiguration(f);
        return setcache.getString("Username", "0");
    }

    public boolean hasPlugin(String plugin) {
        try {
            if (this.getServer().getPluginManager().isPluginEnabled(plugin) &&
                    this.getServer().getPluginManager().getPlugin(plugin) != null) {
                    log("Hooking into " + plugin + "...", false, false);
                return true;
            }
            debug("Skipping hooks for " + plugin + " (Not Found)");
            return false;
        } catch (Exception err) {
            debug("Unable to check for " + plugin + ":");
            err.printStackTrace();
        }
        return false;
    }


    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();

        // Required to do these first even if they are called in updateCheck()
        if (getConfig().contains("Other.Reduce-Console-Msgs")) {
            reducemsgs = getConfig().getBoolean("Other.Reduce-Console-Msgs");
        }

        if (getConfig().contains("Other.Debug")) {
            debug = getConfig().getBoolean("Other.Debug");
        }

        log("Checking repository to maximize support...", false, false);

        api = new ChatFeelingsAPI();
        new CommandManager(this, morePaperLib);

        getConfig().options().copyDefaults(true);
        saveConfig();

        disabledsendingworlds.clear();
        disabledreceivingworlds.clear();
        disabledsendingworlds.addAll(getConfig().getStringList("General.Disabled-Sending-Worlds"));
        disabledreceivingworlds.addAll(getConfig().getStringList("General.Disabled-Receiving-Worlds"));

        Bukkit.getServer().getPluginManager().registerEvents(this, this);

        debug("Disabled Sending Worlds: " + disabledsendingworlds);
        debug("Disabled Receiving Worlds: " + disabledreceivingworlds);

        new Supports(this, morePaperLib).fetch();

        if(!beta) {
            metrics = addMetrics();

            if (getConfig().getBoolean("Other.Updates.Check")) {
                try {
                    new Updater(this, morePaperLib).checkForUpdate();
                } catch (Exception e) {
                    log("There was an issue while trying to check for updates.", false, true);
                    if(debug) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            updateConfig(this);
            updateConfigHeaders(this);
            debug("Using a pre-release of ChatFeelings. Update/Support checking & metrics have been disabled!");
        }

        CommandManager.updateCommands(getConfig());

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

        if (hasPlugin("LiteBans")) {
            haslitebans = true;
        }

        if (hasPlugin("AdvancedBan")) {
            hasadvancedban = true;
        }

        if (hasPlugin("Essentials") || hasPlugin("EssentialsX")) {
            hasess = true;
        }

        if (hasPlugin("PlaceholderAPI")) {
            new Placeholders(this).register();

            // enable nickname placeholders if placeholder api is present
            NicknamePlaceholders.enablePlaceholders(getConfig(), msg, true);
        } else {
            NicknamePlaceholders.enablePlaceholders(getConfig(), msg, false);
        }

        if(beta) {
                log("[!] This is a BETA version. Check for updates manually on Github/Discord!", true, true);
                log("Check for updates daily at https://github.com/zachduda/ChatFeelings/releases", false, true);
        }
        debug("Finished! ChatFeelings was loaded in " + (System.currentTimeMillis() - start) + "ms");

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
        return isMuted(uuid, null) != 0;
    }

    public boolean APIisBannedUUIDBoolean(UUID uuid) {
        return isBanned(uuid, null) != 0;
    }

    public int APIgetSentStat(UUID u, String feeling) {
        if (!feelings.contains(feeling.toLowerCase())) {
            return 0;
        }
        File cache = new File(this.getDataFolder(), File.separator + "Data");
        File f = new File(cache, File.separator + u + ".yml");
        if (!f.exists()) {
            return 0;
        }
        FileConfiguration setcache = YamlConfiguration.loadConfiguration(f);

        return setcache.getInt("Stats.Sent." + capitalizeString(feeling.toLowerCase()));
    }

    public List < String > APIgetFeelings() {
        return feelings;
    }

    public int APIgetTotalSent(UUID u) {
        File cache = new File(this.getDataFolder(), File.separator + "Data");
        File f = new File(cache, File.separator + u + ".yml");
        if (!f.exists()) {
            return 0;
        }
        FileConfiguration setcache = YamlConfiguration.loadConfiguration(f);
        return setcache.getInt("Stats.Sent.Total");
    }

    public boolean APIisAcceptingFeelings(UUID u) {
        File cache = new File(this.getDataFolder(), File.separator + "Data");
        File f = new File(cache, File.separator + u + ".yml");
        FileConfiguration setcache = YamlConfiguration.loadConfiguration(f);
        return setcache.getBoolean("Allow-Feelings");
    }

    // END OF API CALLS ------------------------------------

    private boolean isEssMuted(UUID uuid) {
        try {
            if (hasess) {
                Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
                return ess != null && ess.getUser(uuid).isMuted();
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
                return Database.get().isPlayerMuted(uuid, IPAdd);
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
                return PunishmentManager.get().isMuted(uuid.toString());
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
        return Bukkit.getOfflinePlayer(uuid).isBanned();
    }

    private boolean isEssBanned(UUID uuid) {
        try {
            if (hasess) {
                Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
                assert ess != null;
                return ess.getUser(uuid).getBase().isBanned();
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
                return Database.get().isPlayerBanned(uuid, IPAdd);
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
            if (hasadvancedban) { // Requires UUID as string.
                return PunishmentManager.get().isBanned(uuid.toString());
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
                    assert ess != null;
                    if (ess.getVanishedPlayers().contains(player.getName())) {
                        return true;
                    }
                }

                for (MetadataValue meta: player.getMetadata("vanished")) {
                    if (meta.asBoolean())
                        return true;
                }

            } catch (Exception err) {
                log("Couldn't check for vanished players. Disabling this check until next restart.", false, true);
                usevanishcheck = false;
            }

            if (getConfig().getBoolean("Other.Vanished-Players.Use-Legacy")) {
                return player.hasPotionEffect(PotionEffectType.INVISIBILITY);
            }

        }
        return false;
    }

    private void getStats(CommandSender p, UUID uuid, boolean isown) {
        File cache = new File(folder, File.separator + "Data");
        File f = new File(cache, File.separator + uuid + ".yml");
        FileConfiguration setcache = YamlConfiguration.loadConfiguration(f);
        final String name = setcache.getString("Username");
        if (isown) {
            Msgs.send(p, Objects.requireNonNull(msg.getString("Stats-Header-Own")).replace("%player%", Objects.requireNonNull(name)));
        } else {
            Msgs.send(p, Objects.requireNonNull(msg.getString("Stats-Header-Other")).replace("%player%", Objects.requireNonNull(name)));
        }

        final int totalsent = setcache.getInt("Stats.Sent.Total", 0);

        if(totalsent == 0) {
            if(isown) {
                Msgs.send(p, "&f   &8&l> &7You haven't sent anyone feelings yet!");
            } else {
                Msgs.send(p, "&f   &8&l> &f" + name + " &7hasn't sent feelings yet!");
            }
        } else {
            for (String fl : feelings) {
                String flcap;
                flcap = capitalizeString(fl);

                final int fsent = setcache.getInt("Stats.Sent." + flcap);

                if(fsent > 0) {
                    // grammatical adjustment logic
                    if (fl.equalsIgnoreCase("kiss")) {
                        flcap = "Kisse";
                    }

                    if (fl.equalsIgnoreCase("cry")) {
                        flcap = "Crie";
                    }

                    if (fl.equalsIgnoreCase("welcomeback")) {
                        flcap = "Welcome";
                    }

                    if (fl.equalsIgnoreCase("punch")) {
                        flcap = "Punche";
                    }

                    Msgs.send(p, "&f   &8&l> &7" + flcap + "s: &f&l" + fsent);
                }
            }
            String you = "You've";
            if (!isown) {
                you = "They've";
            }
            Msgs.send(p, "&f   &8&l> &e" + you + " Sent: &f&l" + totalsent);
        }
    }

    private void noPermission(CommandSender sender) {
        Msgs.sendPrefix(sender, msg.getString("No-Permission"));
        bass(sender);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if(feelings.contains(command.getName().toLowerCase())) {
            if(args.length == 1) {
                Bukkit.getOnlinePlayers().forEach(player -> completions.add(player.getName()));
                return StringUtil.copyPartialMatches(args[0].toLowerCase(), completions, new ArrayList<>());
            }
            return Collections.emptyList();
        }
        if (args.length == 1) {
            completions.add("help");

            if (hasPerm(sender, "chatfeelings.stats")) {
                completions.add("stats");
            }
            if (hasPerm(sender, "chatfeelings.mute", true)) {
                completions.add("mute");
                completions.add("unmute");
                completions.add("mutelist");
            }
            if (hasPerm(sender, "chatfeelings.ignore")) {
                completions.add("ignore");
                completions.add("ignorelist");
            }
            if (hasPerm(sender, "chatfeelings.admin", true)) {
                completions.add("reload");
                completions.add("version");
            }

            return StringUtil.copyPartialMatches(args[0].toLowerCase(), completions, new ArrayList<>());
        }
        else if (args.length == 2) {
            // Second argument completions
            if (args[0].equalsIgnoreCase("ignore") || args[0].equalsIgnoreCase("mute")) {
                if (hasPerm(sender, "chatfeelings.ignore")) {
                    completions.add("all");
                    Bukkit.getOnlinePlayers().forEach(player -> completions.add(player.getName()));
                }
            }

            return StringUtil.copyPartialMatches(args[1].toLowerCase(), completions, new ArrayList<>());
        }

        return completions;
    }

    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String cmdLabel, String[] args) {
        final String cmdlr = cmd.getName().toLowerCase();
        if (cmdlr.equals("chatfeelings") && args.length == 0) {
            Msgs.send(sender, "");
            Msgs.send(sender, msg.getString("Prefix-Header"));
            Msgs.send(sender, "&8&l> &7/cf help &7&ofor commands & settings.");
            Msgs.send(sender, "");
            pop(sender);
            return true;
        }

        if (cmdlr.equals("chatfeelings") && args[0].equalsIgnoreCase("version")) {
            Msgs.send(sender, "");
            Msgs.send(sender, msg.getString("Prefix-Header"));
            Msgs.send(sender, "&8&l> &7You are currently running &f&lv" + getDescription().getVersion());
            Msgs.send(sender, "");
            pop(sender);
            return true;
        }

        if (cmdlr.equals("chatfeelings") && args[0].equalsIgnoreCase("stats")) {
            if (!hasPerm(sender,"chatfeelings.stats")) {
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

            if (!hasPerm(sender,"chatfeelings.stats.others", true)) {
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
                Msgs.sendPrefix(sender, Objects.requireNonNull(msg.getString("Player-Never-Joined")).replace("%player%", args[1]));
                return true;
            }

            getStats(sender, getUUID, false);
            pop(sender);
            return true;
        }

        if (cmdlr.equals("chatfeelings") && args[0].equalsIgnoreCase("reload")) {
            if (!hasPerm(sender, "chatfeelings.admin", true)) {
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
            Msgs.send(sender, msg.getString("Prefix-Header"));

            try {
                reloadConfig();

                disabledsendingworlds.clear();
                disabledreceivingworlds.clear();
                disabledsendingworlds.addAll(getConfig().getStringList("General.Disabled-Sending-Worlds"));
                disabledreceivingworlds.addAll(getConfig().getStringList("General.Disabled-Receiving-Worlds"));

                FileSetup.enableFiles();
                configChecks(this);
                CommandManager.updateCommands(getConfig());
            } catch (Exception err2) {
                if (debug) {
                    log("Error occurred when trying to reload your config: ----------", false, false);
                    err2.printStackTrace();
                    log("-----------------------[End of Error]-----------------------", false, false);
                    Msgs.send(sender, "&8&l> &4&lError! &fSomething in your config isn't right. Check console!");
                } else {
                    Msgs.send(sender, "&8&l> &4&lError! &fSomething in your ChatFeelings files is wrong.");
                }
                bass(sender);
                usevanishcheck = true;
                return true;
            }

            updateConfig(this);

            int onlinecount = Bukkit.getServer().getOnlinePlayers().size();
            if (onlinecount == 0) {
                debug("Purging old data files since nobody is currently online...");
                purgeOldFiles();
            }
            if (!disabledsendingworlds.isEmpty()) {
                debug("Sending Feelings is disabled in: " + disabledsendingworlds);
            }
            if (!disabledreceivingworlds.isEmpty()) {
                debug("Receiving Feelings is disabled in: " + disabledreceivingworlds);
            }

            try {
                long reloadtime = System.currentTimeMillis() - starttime;
                if (reloadtime >= 1000) {
                    double reloadsec = (double) reloadtime / 1000;
                    // Let's hope nobody's reload takes more than 1000ms (1s). However, it's not unheard of .-.
                    Msgs.send(sender, Objects.requireNonNull(msg.getString("Reload")).replace("%time%", reloadsec + "s"));
                    if (sender instanceof Player) {
                        log("Configuration & Files reloaded by " + sender.getName() + " in " + reloadsec + "s", false, false);
                    }
                } else {
                    Msgs.send(sender, Objects.requireNonNull(msg.getString("Reload")).replace("%time%", reloadtime + "ms"));
                    if (sender instanceof Player) {
                        log("Configuration & Files reloaded by " + sender.getName() + " in " + reloadtime + "ms", false, false);
                    }
                }
            } catch (Exception err) {
                Msgs.send(sender, "&8&l> &a&l✓  &7Plugin Reloaded. &c(1 file was regenerated)");
            }
            Msgs.send(sender, "");
            levelup(sender);

            return true;
        }

        if (cmdlr.equals("chatfeelings") && args[0].equalsIgnoreCase("help")) {
            Msgs.send(sender, "");
            Msgs.send(sender, msg.getString("Prefix-Header"));
            Msgs.send(sender, "&8&l> &e&l/cf help &7" + msg.getString("Command-Help.Descriptions.Help"));
            if (hasPerm(sender, "chatfeelings.ignore")) {
                Msgs.send(sender, "&8&l> &e&l/cf ignore (player) &7" + msg.getString("Command-Help.Descriptions.Ignore"));
                Msgs.send(sender, "&8&l> &e&l/cf ignore all &7" + msg.getString("Command-Help.Descriptions.Ignore-All"));
            }
            if (hasPerm(sender, "chatfeelings.stats")) {
                Msgs.send(sender, "&8&l> &e&l/cf stats &7" + msg.getString("Command-Help.Descriptions.Stats"));
            }
            if (hasPerm(sender, "chatfeelings.stats.others", true)) {
                Msgs.send(sender, "&8&l> &e&l/cf stats (player) &7" + msg.getString("Command-Help.Descriptions.Stats-Others"));
            }
            if (hasPerm(sender, "chatfeelings.mute", true)) {
                Msgs.send(sender, "&8&l> &e&l/cf mute (player) &7" + msg.getString("Command-Help.Descriptions.Mute"));
                Msgs.send(sender, "&8&l> &e&l/cf unmute (player) &7" + msg.getString("Command-Help.Descriptions.Unmute"));
                Msgs.send(sender, "&8&l> &e&l/cf mutelist &7" + msg.getString("Command-Help.Descriptions.Mute-List"));
            }
            if (hasPerm(sender, "chatfeelings.admin", true)) {
                Msgs.send(sender, "&8&l> &e&l/cf version &7" + msg.getString("Command-Help.Descriptions.Plugin-Version"));
                Msgs.send(sender, "&8&l> &e&l/cf reload &7" + msg.getString("Command-Help.Descriptions.Plugin-Reload"));
            }
            Msgs.send(sender, "&8&l> &6&l/feelings &7" + msg.getString("Command-Help.Descriptions.Feelings"));
            Msgs.send(sender, "");
            pop(sender);
            return true;
        }

        if (cmdlr.equals("chatfeelings") && args[0].equalsIgnoreCase("uuid")) {
            if (!hasPerm(sender, "chatfeelings.admin", true)) {
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
                Msgs.sendPrefix(sender, Objects.requireNonNull(msg.getString("Player-Never-Joined")).replace("%player%", args[1]));
                return true;
            }

            String getName = hasPlayedUUIDGetName(getUUID);
            Msgs.sendPrefix(sender, "&fThe UUID of " + getName + " is &7" + getUUID);
            pop(sender);
            return true;
        }

        if (cmdlr.equals("chatfeelings") && args[0].equalsIgnoreCase("mutelist")) {
            if (!hasPerm(sender, "chatfeelings.mute", true)) {
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
            // We need a 60s global cooldown in case they use MySQL. Doing this command w/ MySQL can suck up LOTS of CPU.
            if (haslitebans || hasadvancedban) {
                lastmutelist = System.currentTimeMillis();
            }
            morePaperLib.scheduling().asyncScheduler().run(() -> {
                Msgs.send(sender, "");
                Msgs.send(sender, msg.getString("Mute-List-Header"));

                int totalmuted = 0;

                for (File cachefile: Objects.requireNonNull(datafolder.listFiles())) {
                    File f = new File(cachefile.getPath());

                    if (!f.getName().equalsIgnoreCase("global.yml")) {
                        FileConfiguration setcache = YamlConfiguration.loadConfiguration(f);

                        String uuid = setcache.getString("UUID");
                        String IPAdd = setcache.getString("IP");
                        UUID puuid = UUID.fromString(Objects.requireNonNull(uuid));

                        int muteInt = isMuted(puuid, IPAdd);

                        if (setcache.contains("Muted") && setcache.contains("Username")) {
                            if (setcache.getBoolean("Muted")) {
                                totalmuted++;
                                if (muteInt == 3) {
                                    Msgs.send(sender, Objects.requireNonNull(msg.getString("Mute-List-Player")).replace("%player%", (String) Objects.requireNonNull(setcache.get("Username"))) + " &c(AdvancedBan & CF)");
                                } else if (muteInt == 2) {
                                    Msgs.send(sender, Objects.requireNonNull(msg.getString("Mute-List-Player")).replace("%player%", (String) Objects.requireNonNull(setcache.get("Username"))) + " &c(LiteBans & CF)");
                                } else if (muteInt == 1) {
                                    Msgs.send(sender, Objects.requireNonNull(msg.getString("Mute-List-Player")).replace("%player%", (String) Objects.requireNonNull(setcache.get("Username"))) + " &c(Essentials & CF)");
                                } else {
                                    Msgs.send(sender, Objects.requireNonNull(msg.getString("Mute-List-Player")).replace("%player%", (String) Objects.requireNonNull(setcache.get("Username"))));
                                }
                            } else {
                                if (muteInt == 3) {
                                    totalmuted++;
                                    Msgs.send(sender, Objects.requireNonNull(msg.getString("Mute-List-Player")).replace("%player%", (String) Objects.requireNonNull(setcache.get("Username"))) + " &c(AdvancedBan)");
                                } else
                                if (muteInt == 2) {
                                    totalmuted++;
                                    Msgs.send(sender, Objects.requireNonNull(msg.getString("Mute-List-Player")).replace("%player%", (String) Objects.requireNonNull(setcache.get("Username"))) + " &c(LiteBans)");
                                } else
                                if (muteInt == 1) {
                                    totalmuted++;
                                    Msgs.send(sender, Objects.requireNonNull(msg.getString("Mute-List-Player")).replace("%player%", (String) Objects.requireNonNull(setcache.get("Username"))) + " &c(Essentials)");
                                }
                            }
                        }
                    }
                }

                if (totalmuted == 1) {
                    Msgs.send(sender, Objects.requireNonNull(msg.getString("Mute-List-Total-One")).replace("%total%", "1"));
                } else if (totalmuted == 0) {
                    Msgs.send(sender, Objects.requireNonNull(msg.getString("Mute-List-Total-Zero")).replace("%total%", "0"));
                } else {
                    Msgs.send(sender, Objects.requireNonNull(msg.getString("Mute-List-Total-Many")).replace("%total%", Integer.toString(totalmuted)));
                }
                Msgs.send(sender, "");
            });
            pop(sender);
            return true;
        }

        if (cmdlr.equals("chatfeelings") && args[0].equalsIgnoreCase("unignore")) {
            Msgs.sendPrefix(sender, "&c&lOops! &fRetype to unignore as &7/cf ignore (player)");
            bass(sender);
            return true;
        }

        if (cmdlr.equals("chatfeelings") && args[0].equalsIgnoreCase("unmute")) {
            if (!hasPerm(sender, "chatfeelings.mute", true)) {
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
                Msgs.sendPrefix(sender, Objects.requireNonNull(msg.getString("Player-Never-Joined")).replace("%player%", args[1]));
                return true;
            }

            File datafolder = new File(this.getDataFolder(), File.separator + "Data");

            if (!datafolder.exists()) {
                Msgs.sendPrefix(sender, msg.getString("Folder-Not-Found"));
                bass(sender);
                return true;
            }

            File f = new File(datafolder, File.separator + muteUUID + ".yml");
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
            final UUID puuid = UUID.fromString(Objects.requireNonNull(uuid));

            if (setcache.getBoolean("Muted")) {
                setcache.set("Muted", false);

                try {
                    setcache.save(f);
                } catch (Exception err) {
                    log("Unable to save " + playername + "'s data file:", true, true);
                    err.printStackTrace();
                    log("-----------------------------------------------------",false, true);
                    log("Please message us on discord or spigot about this error.", false, true);
                }

                Msgs.sendPrefix(sender, Objects.requireNonNull(msg.getString("Player-Has-Been-Unmuted")).replace("%player%", Objects.requireNonNull(playername)));
                pop(sender);
            } else if (!setcache.getBoolean("Muted")) {
                bass(sender);
                morePaperLib.scheduling().asyncScheduler().run(() -> {
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
                log("Something went wrong when trying to get " + sender.getName() + "'s (un)mute status in the player file.", false, true);
            }

            return true;
        }

        if (cmdlr.equals("chatfeelings") && args[0].equalsIgnoreCase("mute")) {
            if (!hasPerm(sender, "chatfeelings.mute", true)) {
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
                Msgs.sendPrefix(sender, Objects.requireNonNull(msg.getString("Player-Never-Joined")).replace("%player%", args[1]));
                return true;
            }

            File datafolder = new File(this.getDataFolder(), File.separator + "Data");

            if (!datafolder.exists()) {
                Msgs.sendPrefix(sender, msg.getString("Folder-Not-Found"));
                bass(sender);
                return true;
            }

            File f = new File(datafolder, File.separator + hasPlayedNameGetUUID(args[1]).toString() + ".yml");
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
            final UUID puuid = UUID.fromString(Objects.requireNonNull(uuid));

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
                    log("Unable to save " + playername + "'s data file:", true, true);
                    err.printStackTrace();
                    log("-----------------------------------------------------", false, true);
                    log("Please message us on discord or spigot about this error.", false, true);
                }
                Msgs.sendPrefix(sender, Objects.requireNonNull(msg.getString("Player-Has-Been-Muted")).replace("%player%", Objects.requireNonNull(playername)));
                morePaperLib.scheduling().asyncScheduler().run(() -> {
                    final int muteInt = isMuted(puuid, IPAdd);
                    if (muteInt != 0) {
                        Msgs.sendPrefix(sender, Objects.requireNonNull(msg.getString("Extra-Mute-Present")).replace("%player%", playername));
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
                log("Something went wrong when trying to get " + sender.getName() + "'s mute status in the player file.", false, true);
            }

            return true;
        }

        if (cmdlr.equals("chatfeelings") && args[0].equalsIgnoreCase("ignore")) {
            if (!hasPerm(sender, "chatfeelings.ignore")) {
                noPermission(sender);
                return true;
            }

            if (!(sender instanceof Player)) {
                Msgs.sendPrefix(sender, "&c&lSorry. &fOnly players can ignore other players.");
                return true;
            }

            final Player p = (Player)sender;

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

                File f = new File(datafolder, File.separator + p.getUniqueId() + ".yml");
                FileConfiguration setcache = YamlConfiguration.loadConfiguration(f);

                List<String> ignoredplayers = new ArrayList<>(setcache.getStringList("Ignoring"));

                Msgs.send(sender, " ");
                Msgs.send(sender, msg.getString("Ignore-List-Header"));
                if (ignoredplayers.isEmpty()) {
                    if (setcache.getBoolean("Allow-Feelings")) {
                        Msgs.send(sender, msg.getString("Ignore-List-None"));
                    } else {
                        Msgs.send(sender, msg.getString("Ignore-List-All"));
                    }
                } else {
                    for (String ignoredUUID: ignoredplayers) {
                        String name = hasPlayedUUIDGetName(UUID.fromString(ignoredUUID));
                        if (name != null && !name.equals("0")) {
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

            if (getConfig().getBoolean("General.Cooldowns.Ignoring.Enabled") && !sender.isOp() && !hasPerm(sender, "chatfeelings.bypasscooldowns", true)) {
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

            File f = new File(datafolder, File.separator + p.getUniqueId() + ".yml");
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
                    Msgs.sendPrefix(sender, msg.getString("Ignoring-On-All"));
                } else {
                    setcache.set("Allow-Feelings", true);
                    Msgs.sendPrefix(sender, msg.getString("Ignoring-Off-All"));
                }

                pop(sender);

                try {
                    setcache.save(f);
                } catch (Exception err) {}
                return true;
            }

            List<String> ignoredplayers = new ArrayList<>(setcache.getStringList("Ignoring"));

            final UUID ignoreUUID = hasPlayedNameGetUUID(args[1]);
            if (ignoreUUID == null) {

                if (args[1].equalsIgnoreCase("console")) {
                    Msgs.sendPrefix(sender, msg.getString("Console-Not-Player"));
                    bass(sender);
                    return true;
                }

                bass(sender);
                Msgs.sendPrefix(sender, Objects.requireNonNull(msg.getString("Player-Never-Joined")).replace("%player%", args[1]));
                return true;
            }

            final String iuuids = ignoreUUID.toString();

            try {
                if (ignoredplayers.contains(iuuids)) {
                    Msgs.sendPrefix(sender, Objects.requireNonNull(msg.getString("Ignoring-Off-Player")).replace("%player%", args[1]));

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
                log("Error trying to search for: " + args[1] + " in the Data folder.", false, true);
            }

            ignoredplayers.add(ignoreUUID.toString());
            setcache.set("Ignoring", ignoredplayers);
            try {
                setcache.save(f);
            } catch (Exception err) {}
            Msgs.sendPrefix(sender, Objects.requireNonNull(msg.getString("Ignoring-On-Player")).replace("%player%", args[1]));
            pop(sender);
            ignoredplayers.clear();
            return true;
        }

        if (cmdlr.equals("feelings")) {
            final String path = "Command_Descriptions.";
            final String plyr = msg.getString("Command-List-Player");
            int page = 1;
            if (args.length >= 1) {
                try {
                    page = Integer.parseInt(args[0]);
                } catch(NumberFormatException e) {
                    bass(sender);
                    Msgs.sendPrefix(sender, msg.getString("Page-Not-Found"));
                    return true;
                }
            }

            if(page <= 0) {
                page = 1;
            }

            final int page_length = Math.max(1, getConfig().getInt("General.Help-Page-Length"));

            List<String> enabledfeelings = new ArrayList<>();
            for(String fl : feelings) {
                if (emotes.getBoolean("Feelings." + capitalizeString(fl) + ".Enable")) {
                    enabledfeelings.add(fl);
                }
            }

            if(enabledfeelings.isEmpty()) {
                bass(sender);
                Msgs.sendPrefix(sender,"&7There are no feelings are currently enabled.");
                return true;
            }

            final int totalpages = Math.max(1, (int)Math.ceil((double) enabledfeelings.size() /page_length));

            if(page > totalpages) {
                bass(sender);
                Msgs.sendPrefix(sender, msg.getString("Page-Not-Found"));
                return true;
            }

            final int start = (page-1) * page_length;
            final int end = start + page_length;

            Msgs.send(sender, "");
            Msgs.send(sender, msg.getString("Feelings-Help") + "                        " +
                    Objects.requireNonNull(msg.getString("Feelings-Help-Page")).replace("%page%", Integer.toString(page)).replace("%pagemax%", Integer.toString(totalpages)));
            for (int i = start; i < end; i++) {
                if(i < enabledfeelings.size()) {
                    final String flcap = capitalizeString(enabledfeelings.get(i));
                    final String cfl = enabledfeelings.get(i).toLowerCase();
                    if (emotes.getBoolean("Feelings." + flcap + ".Enable")) {
                        if(enabledfeelings.get(i).equalsIgnoreCase("spook")) { // test if spook
                            Date now = new Date();
                            SimpleDateFormat format = new SimpleDateFormat("MM");

                            if (format.format(now).equals("10") || format.format(now).equals("09")) {
                                if (hasPerm(sender, "chatfeelings." + cmdlr)) {
                                    Msgs.send(sender, "&8&l> &6&l/spook (player) &7Give your friends some festive fright!");
                                } else {
                                    Msgs.send(sender, "&8&l> &c/" + enabledfeelings.get(i).toLowerCase() + plyr + "&7 " + msg.getString("Command-List-NoPerm"));
                                }
                            } else {
                                Msgs.send(sender, "&8&l> &7&l/spook &7This command is exclusive to October only.");
                            }
                        } else {
                            if (hasPerm(sender, "chatfeelings." + cfl) || hasPerm(sender, "chatfeelings.all")) {
                                Msgs.send(sender, "&8&l> &f&l/" + cfl + plyr + "&7 " + msg.getString(path + flcap));
                            } else {
                                Msgs.send(sender, "&8&l> &c/" + cfl + plyr + "&7 " + msg.getString("Command-List-NoPerm"));
                            }
                        }
                    }
                }
            }
            if(totalpages > 1 && ((page+1) <= totalpages)) {
                Msgs.send(sender, Objects.requireNonNull(msg.getString("Command-List-Page")).replaceAll("%page%", Integer.toString(page + 1)));
            }
            pop(sender);
            Msgs.send(sender, "");
            return true;
        }

        if (feelings.contains(cmdlr)) {

            morePaperLib.scheduling().asyncScheduler().run(() -> {
                if (sender instanceof Player && useperms) {
                    if(!hasPerm(sender, "chatfeelings." + cmdlr) && !hasPerm(sender, "chatfeelings.all")) {
                        noPermission(sender);
                        return;
                    }
                }

                if (getConfig().getBoolean("General.Cooldowns.Feelings.Enabled") && !hasPerm(sender,"chatfeelings.bypasscooldowns", true)) {
                    if (sender instanceof Player) {
                        final Player p = (Player)sender;
                        if (Cooldowns.cooldown.containsKey(p.getPlayer())) {
                            int cooldownTime = getConfig().getInt("General.Cooldowns.Feelings.Seconds");
                            long secondsLeft = ((Cooldowns.cooldown.get(p.getPlayer()) / 1000) + cooldownTime) - (System.currentTimeMillis() / 1000);
                            if (secondsLeft > 0) {
                                Msgs.sendPrefix(sender, Objects.requireNonNull(msg.getString("Cooldown-Active")).replace("%time%",
                                        secondsLeft + "s"));
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

                final String cmdconfig = (capitalizeString(cmd.getName()));

                if (sender instanceof Player) {
                    final Player p = (Player)sender;
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

                if (args[0].equalsIgnoreCase("console")) {
                    Msgs.sendPrefix(sender, msg.getString("Console-Not-Player"));
                    bass(sender);
                    return;
                }

                Player target = Bukkit.getServer().getPlayer(args[0]);

                if (target == null || isVanished(target)) {
                    //if(Cooldowns.nicknames.containsKey(args[0])) {
                    //    target = Cooldowns.nicknames.get(args[0]);
                    //} else {
                        bass(sender);
                        Msgs.sendPrefix(sender, Objects.requireNonNull(msg.getString("Player-Offline")).replace("%player%", args[0]));
                        return;
                    //}
                }

                if (target.getName().equalsIgnoreCase(sender.getName())) {
                    if (getConfig().getBoolean("General.Prevent-Self-Feelings")) {
                        bass(sender);
                        Msgs.sendPrefix(sender, Objects.requireNonNull(msg.getString("Sender-Is-Target")).replace("%command%", cmdconfig));
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
                    final Player p = (Player)sender;
                    if (getConfig().getBoolean("General.Radius.Enabled")) {
                        final String omsg = Objects.requireNonNull(msg.getString("Outside-Of-Radius")).replace("%player%", target.getName()).replace("%command%", cmd.getName());
                        if (target.getWorld() != p.getWorld()) {
                            Msgs.sendPrefix(sender, omsg);
                            bass(sender);
                            return;
                        }
                        double distance = p.getLocation().distance(target.getLocation());
                        double radius = getConfig().getDouble("General.Radius.Radius-In-Blocks");
                        if (distance > radius) {
                            debug(sender.getName() + " was outside the radius of " + radius + ". (They're " + distance + ")");
                            Msgs.sendPrefix(sender, omsg);
                            bass(sender);
                            return;
                        }
                    }
                }

                // Ignoring & Mute Check ----------------

                File playerfiles = new File(this.getDataFolder(), File.separator + "Data");

                if (sender instanceof Player) {
                    final Player p = (Player)sender;
                    File myf = new File(playerfiles, File.separator + p.getUniqueId() + ".yml");
                    FileConfiguration me = YamlConfiguration.loadConfiguration(myf);

                    final int muteInt = isMuted(p.getUniqueId(), null);

                    if (muteInt != 0) {
                        if (muteInt == 3) {
                            debug(sender.getName() + " tried to use /" + cmdLabel + ", but is muted by AdvancedBan.");
                        }
                        if (muteInt == 2) {
                            debug(sender.getName() + " tried to use /" + cmdLabel + ", but is muted by LiteBans.");
                        }
                        if (muteInt == 1) {
                            debug(sender.getName() + " tried to use /" + cmdLabel + ", but is muted by Essentials.");
                        }
                        bass(sender);
                        Msgs.sendPrefix(sender, msg.getString("Is-Muted"));
                        return;
                    }

                    if (myf.exists()) {
                        if (me.getBoolean("Muted")) {
                            debug(sender.getName() + " tried to use /" + cmdLabel + ", but was muted (via CF).");
                            bass(sender);
                            Msgs.sendPrefix(sender, msg.getString("Is-Muted"));
                            return;
                        }

                        if (isTargetIgnoringSender(target, p)) {
                            bass(sender);
                            Msgs.sendPrefix(sender,
                                    Objects.requireNonNull(msg.getString("Target-Is-Ignoring")).replace("%player%", target.getName()));

                            debug("Not sending feeling to " + target.getName() + " because they are ignoring " + p.getName());
                            return;
                        }
                    }
                }

                File tfraw = new File(playerfiles, File.separator + target.getUniqueId() + ".yml");
                try {
                    FileConfiguration targetfile = YamlConfiguration.loadConfiguration(tfraw);

                    if (tfraw.exists()) {
                        if (!targetfile.getBoolean("Allow-Feelings")) {
                            bass(sender);
                            Msgs.sendPrefix(sender, msg.getString("Target-Is-Ignoring-All"));
                            debug("Blocking feeling because " + target.getName() + " is blocking ALL.");
                            return;
                        }
                    }
                } catch (Exception tfe) {
                    if(tfraw.delete()) {
                        log.warning("Corruption in " + target.getName() + "'s data file. Deleted it!");
                    } else {
                        log.warning("Corruption in " + target.getName() + "'s data file. You should delete it!");
                    }
                }
                // ------------------------------------------------

                // FEELING HANDLING IS ALL BELOW -------------------------------------------------------------------------------


                // SPOOK INSERT
                if(cmdlr.equals("spook")) {
                    Date now = new Date();
                    SimpleDateFormat format = new SimpleDateFormat("MM");

                    if(!format.format(now).equals("10") && !format.format(now).equals("09")) {
                        Msgs.sendPrefix(sender, "&c&lSorry. &fSpook is an emote exclusive to &7&lOctober");
                        bass(sender);
                        return;
                    }

                    if(Cooldowns.spook.containsKey(target.getName())) {
                        Msgs.sendPrefix(sender, "&e&l&oToo Spooky! &fThis player is already being spooked.");
                        bass(sender);
                        return;
                    }

                    if(!(Objects.equals(target.getInventory().getHelmet(), new ItemStack(Material.AIR)) || (target.getInventory().getHelmet() == null))) {
                        Msgs.sendPrefix(sender, "&cSorry. &7" + target.getName() + "&f has a helmet on, and cannot be spooked.");
                        bass(sender);
                        return;
                    }

                    Cooldowns.spookHash(target);
                }
                // END OF SPOOK

                // API Events ----------------------------
                final Player finalTarget = target;
                FeelingSendEvent fse = new FeelingSendEvent(sender, finalTarget, cmdconfig);
                FeelingRecieveEvent fre = new FeelingRecieveEvent(finalTarget, sender, cmdconfig);

                morePaperLib.scheduling().globalRegionalScheduler().run(() -> {
                    Bukkit.getPluginManager().callEvent(fse);
                    if (fse.isCancelled()) {
                        return;
                    }

                    Bukkit.getPluginManager().callEvent(fre);
                });

                // End of API events (Except for Global event below ---------------------

                // Global Handler for PLAYER messages & Feelings ----------------------------
                if (getConfig().getBoolean("General.Global-Feelings.Enabled")) {

                    for (final Player online: Bukkit.getServer().getOnlinePlayers()) {

                        // Global Ignoring Checks -----------------
                        File cache = new File(this.getDataFolder(), File.separator + "Data");
                        File f = new File(cache, File.separator + online.getUniqueId() + ".yml");
                        FileConfiguration setcache = YamlConfiguration.loadConfiguration(f);

                        if (!setcache.getBoolean("Allow-Feelings") && (!online.getName().equals(sender.getName()))) {
                            debug(online.getName() + " is blocking all feelings. Skipping Global Msg!");
                        } else { // else NOT ignoring ALL
                            if (sender instanceof Player) {
                                final Player p = (Player)sender;
                                if (isTargetIgnoringSender(target, p)) {
                                    // Player is Ignoring from sender but is not target. (GlobaL)

                                    // This works but is unused. Need to remove later.
                                    debug(online.getName() + " is blocking feelings from " + p.getName() + ". Skipping global msg!");
                                }
                            }
                            // End of Global ignoring Checks -------------------

                            if (sender.getName().equalsIgnoreCase("console") || !(sender instanceof Player)) {
                                // ONLY for CONSOLE Global notify here.
                                Msgs.send(Objects.requireNonNull(online.getPlayer()), NicknamePlaceholders.replacePlaceholders(emotes.getString("Feelings." + cmdconfig + ".Msgs.Global"), sender, target));
                            } else {
                                // Global for PLAYER below
                                Player p = (Player) sender;
                                if (!setcache.getStringList("Ignoring").contains(p.getUniqueId().toString())) {
                                    morePaperLib.scheduling().globalRegionalScheduler().run(() -> {
                                        FeelingGlobalNotifyEvent fgne = new FeelingGlobalNotifyEvent(online, sender, finalTarget, cmdconfig);
                                        Bukkit.getPluginManager().callEvent(fgne);

                                        if (!fgne.isCancelled()) {
                                            Msgs.send(Objects.requireNonNull(online.getPlayer()), NicknamePlaceholders.replacePlaceholders(emotes.getString("Feelings." + cmdconfig + ".Msgs.Global"), sender, finalTarget));
                                        }
                                    });

                                } // end of check to make sure message is sent to those NOT ignoring the player
                            }
                        } // end of else for ignore global check
                    } // end of for(online)
                    // End --------------------------------------------------

                    // Global Console Broadcast Msg ------------------------------------------------
                    if (getConfig().getBoolean("General.Global-Feelings.Broadcast-To-Console")) {
                        Msgs.send(getServer().getConsoleSender(), NicknamePlaceholders.replacePlaceholders(emotes.getString("Feelings." + cmdconfig + ".Msgs.Global"), sender, target));

                    }
                    // Global Console End --------------------------------------------------

                } else {
                    // if not global (normal)
                    // send to target
                    Msgs.send(Objects.requireNonNull(target.getPlayer()), NicknamePlaceholders.replacePlaceholders(emotes.getString("Feelings." + cmdconfig + ".Msgs.Target"), sender));
                    // send to cmd sender
                    Msgs.send(sender, NicknamePlaceholders.replacePlaceholders(emotes.getString("Feelings." + cmdconfig + ".Msgs.Sender"), target));
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
                        final Player p = (Player)sender;
                        Cooldowns.putCooldown(p);
                    }
                }
                // -----------------------------------------------------

                // Particle Handler -------------------------------------
                if (particles) {
                    try {
                        morePaperLib.scheduling().globalRegionalScheduler().run(() ->  {
                            Particles.show(target, cmdlr);
                        });
                    } catch (Exception parterr) {
                        if (debug) {
                            parterr.printStackTrace();
                        }
                        particles = false;
                        log("Couldn't display '" + cmd.getName().toUpperCase() + "' particles to " + target.getName() + ". Make sure you use 1.12 or higher.", false, true);
                    }
                }
                // -----------------------------------------------------

                // Sound Handler ----------------------------------------
                if (sounds) {
                    try {
                        String sound1 = emotes.getString("Feelings." + cmdconfig + ".Sounds.Sound1.Name");
                        if (!Objects.requireNonNull(sound1).equalsIgnoreCase("none") && !sound1.equalsIgnoreCase("off") && !sound1.equals("null")) {
                            Sound sound1var;
                            try {
                                sound1var = Objects.requireNonNull(Registry.SOUNDS.get(Objects.requireNonNull(NamespacedKey.fromString(sound1.toLowerCase()))));
                            } catch (Exception preerr1) {
                                debug("[Sound Soft-Fail] Attempting sound regex (replacing _ with .) for sound: " + sound1.toUpperCase());
                                sound1var = Objects.requireNonNull(Registry.SOUNDS.get(Objects.requireNonNull(NamespacedKey.fromString(sound1.toLowerCase().replaceAll("_", ".")))));
                            }
                            target.playSound(Objects.requireNonNull(target.getPlayer()).getLocation(),
                                    sound1var,
                                    (float) emotes.getDouble("Feelings." + cmdconfig + ".Sounds.Sound1.Volume"),
                                    (float) emotes.getDouble("Feelings." + cmdconfig + ".Sounds.Sound1.Pitch"));
                            if (sender instanceof Player) {
                                final Player p = (Player)sender;
                                p.playSound(p.getLocation(),
                                        Objects.requireNonNull(Registry.SOUNDS.get(Objects.requireNonNull(NamespacedKey.fromString(sound1.toLowerCase())))),
                                        (float) emotes.getDouble("Feelings." + cmdconfig + ".Sounds.Sound1.Volume"),
                                        (float) emotes.getDouble("Feelings." + cmdconfig + ".Sounds.Sound1.Pitch"));
                            }
                        }
                    } catch (Exception sounderr1) { // err test for sounds
                        log("Primary feeling values for /" + cmdconfig + " are incorrect! Sounds will disable...", true, true);
                        if(debug) {
                            sounderr1.printStackTrace();
                        }
                        sounds = false;
                    }
                    try {
                        String sound2 = emotes.getString("Feelings." + cmdconfig + ".Sounds.Sound2.Name");
                        if (!Objects.requireNonNull(sound2).equalsIgnoreCase("none") && !sound2.equalsIgnoreCase("off") && !sound2.equals("null")) {
                            Sound sound2var;
                            try {
                                sound2var = Objects.requireNonNull(Registry.SOUNDS.get(Objects.requireNonNull(NamespacedKey.fromString(sound2.toLowerCase()))));
                            } catch (Exception preerr1) {
                                debug("[Sound Soft-Fail] Attempting sound regex (replacing _ with .) for sound: " + sound2.toUpperCase());
                                sound2var = Objects.requireNonNull(Registry.SOUNDS.get(Objects.requireNonNull(NamespacedKey.fromString(sound2.toLowerCase().replaceAll("_", ".")))));
                            }

                            if (sound2.contains("DISC") && multiversion) {
                                // Check for SPOOK, that runs an ALT sound to prevent needing to stop it. (For Multi Version support)
                                target.playSound(Objects.requireNonNull(target.getPlayer()).getLocation(),
                                        Sound.AMBIENT_CAVE,
                                        2.0F, 0.5F);
                            } else {
                                target.playSound(Objects.requireNonNull(target.getPlayer()).getLocation(),
                                        sound2var,
                                        (float) emotes.getDouble("Feelings." + cmdconfig + ".Sounds.Sound2.Volume"),
                                        (float) emotes.getDouble("Feelings." + cmdconfig + ".Sounds.Sound2.Pitch"));

                                if (sender instanceof Player && !sound2.contains("DISC")) {
                                    final Player p = (Player)sender;
                                    p.playSound(p.getLocation(),
                                            sound2var,
                                            (float) emotes.getDouble("Feelings." + cmdconfig + ".Sounds.Sound2.Volume"),
                                            (float) emotes.getDouble("Feelings." + cmdconfig + ".Sounds.Sound2.Pitch"));
                                }
                            }
                        }
                    } catch (Exception sounderr) { // err test for sounds
                        log("Secondary feeling values for /" + cmdconfig + " are incorrect! Sounds will disable..", true, true);
                        if(debug) {
                            sounderr.printStackTrace();
                        }
                        sounds = false;
                    }
                } // end of config sound check
                // ---------- End of Sounds

                // Add Stats
                if (sender instanceof Player) {
                    final Player p = (Player) sender;
                    statsAdd(p, cmdconfig);
                }
            });
            // End Stats
            return true;
        }

        if (cmdlr.equals("chatfeelings")) {
            Msgs.send(sender, "");
            Msgs.send(sender, msg.getString("Prefix-Header"));
            Msgs.send(sender, "&8&l> &c&lHmm. &7That command does not exist.");
            Msgs.send(sender, "");
            if (sender instanceof Player) {
                final Player p = (Player)sender;
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
        morePaperLib.scheduling().asyncScheduler().run(() -> {
            Player p = e.getPlayer();
            String name = p.getName();

            //Cooldowns.saveNickname(p);

            try {
                if (getConfig().getBoolean("Other.Updates.Check")) {
                    if (hasPerm(p, "chatfeelings.admin", true)) {
                        if (Updater.isOutdated()) {
                            Msgs.sendPrefix(p, "&c&lOutdated Plugin! &7Running v" + getDescription().getVersion() +
                                    " while the latest is &f&l" + Updater.getPostedVersion());
                        }
                    }
                }
            } catch (Exception err) {}

            if (!Cooldowns.playerFileUpdate.contains(name)) {
                updateLastOn(p);
                morePaperLib.scheduling().globalRegionalScheduler().run(() -> Cooldowns.justJoined(name));
            }

            if (p.getUniqueId().toString().equals("6191ff85-e092-4e9a-94bd-63df409c2079")) {
                Msgs.send(p, "&7This server is running &fChatFeelings &6v" + getDescription().getVersion() +
                        " &7for " + Supports.getMCVersion() + "." + Supports.getMcPatchVersion());
            }
        });
    }

    // spook even listeners
    @EventHandler
    public void onChestEvent(InventoryClickEvent event) {
        Player p = (Player)event.getWhoClicked();
        if(Cooldowns.spook.containsKey(p.getName())) {
            event.setCancelled(true);
        }
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onTP(PlayerTeleportEvent e) {
        if(e.isCancelled()) {
            return;
        }
        Player p = e.getPlayer();
        if(Cooldowns.spook.containsKey(p.getName())) {
            e.setCancelled(true);
            bass(p);
            Msgs.sendPrefix(p, "&c&lSorry! &fYou can't teleport while being spooked.");
            Msgs.sendPrefix(p, "&e&oTip: &7&oTo prevent the spooks, you can put a helmet on your head.");
        }
    }
}
