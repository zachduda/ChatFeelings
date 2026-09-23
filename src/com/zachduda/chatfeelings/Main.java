package com.zachduda.chatfeelings;

import com.earth2me.essentials.Essentials;
import com.zachduda.chatfeelings.api.*;
import com.zachduda.chatfeelings.other.Supports;
import com.zachduda.chatfeelings.other.Updater;
import com.zachduda.chatfeelings.storage.PlayerData;
import com.zachduda.chatfeelings.storage.PlayerStorage;
import com.zachduda.chatfeelings.storage.StorageFactory;
import litebans.api.Database;
import me.leoko.advancedban.manager.PunishmentManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimpleBarChart;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import space.arim.morepaperlib.MorePaperLib;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@SuppressWarnings({"CatchMayIgnoreException", "CallToPrintStackTrace"})
public class Main extends JavaPlugin implements Listener, TabExecutor {
    /* If true, metrics & update checking are skipped. */
    final public static boolean beta = false;

    public ChatFeelingsAPI api;

    public MorePaperLib morePaperLib = new MorePaperLib(this);

    /** The feelings ChatFeelings ships with. Defaults for each are written to Feelings/&lt;name&gt;.yml by FileSetup. */
    public final static List<String> BUILTIN_FEELINGS = Collections.unmodifiableList(Arrays.asList(
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
            "boop"
    ));

    /**
     * Every usable feeling: the built-ins above plus any valid custom feelings discovered in the
     * Feelings folder. Kept in sync by FileSetup.enableFiles() via setCustomFeelings(); the List
     * instance itself never changes so other classes can keep a reference to it.
     */
    public final static List<String> feelings = new ArrayList<>(BUILTIN_FEELINGS);

    static void setCustomFeelings(List<String> customFeelings) {
        feelings.clear();
        feelings.addAll(BUILTIN_FEELINGS);
        for (String custom : customFeelings) {
            if (!feelings.contains(custom)) {
                feelings.add(custom);
            }
        }
    }

    private boolean hasess = false;
    private boolean haslitebans = false;
    private boolean hasadvancedban = false;
    private static boolean usevanishcheck = false;

    protected static volatile boolean particles = true;

    private static volatile boolean useperms = false;

    protected static volatile boolean multiversion = false;
    public static volatile boolean reducemsgs = false;
    protected static volatile boolean debug = false;

    private static volatile boolean sounds = false;
    private static volatile boolean punishmentError = false;
    private Metrics metrics;

    /** Player data backend (YAML Data folder or MySQL). Only touch it from async tasks. */
    private volatile PlayerStorage storage;
    private String storageType;

    private long lastreload = 0;
    private long lastmutelist = 0;

    private final List<String> disabledsendingworlds = new ArrayList<>();
    private final List<String> disabledreceivingworlds = new ArrayList<>();

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
        if(msg.isBlank()) {
            return;
        }

        if (critical || !reducemsgs) {
            if(warning) {
                log.warning("[!] " + logtag + msg);
                return;
            }
            log.info( logtag + msg);
        }
    }

    public static void debug(String msg) {
        if(msg.isBlank()) {
            return;
        }

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

        if (storage != null) {
            storage.close();
            storage = null;
        }
    }

    public PlayerStorage getStorage() {
        return storage;
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
        final boolean useclean = getConfig().getBoolean("Other.Player-Files.Cleanup");
        final boolean eraseBanned = getConfig().getBoolean("Other.Player-Files.Erase-If-Banned");
        final int maxDays = getConfig().getInt("Other.Player-Files.Cleanup-After-Days");
        final PlayerStorage store = storage;

        morePaperLib.scheduling().asyncScheduler().run(() -> {
            store.cleanupInvalid();

            final long today = System.currentTimeMillis() / 86400000;
            for (PlayerData data : store.loadAll()) {
                try {
                    final String playername = data.getUsername();
                    final long daysAgo = Math.abs((data.getLastOn() / 86400000) - today);

                    final int banInt = eraseBanned ? isBanned(data.getUuid(), data.getIp()) : 0;
                    if (banInt != 0) {
                        store.delete(data.getUuid());
                        debug("Deleted " + playername + "'s data. They were banned! (" + banSource(banInt) + ")");
                    } else if (useclean && daysAgo >= maxDays) {
                        store.delete(data.getUuid());
                        debug("Deleted " + playername + "'s data because it's " + daysAgo +
                                " days old. (Max is " + maxDays + " Days)");
                    } else if (useclean) {
                        debug("Keeping " + playername + "'s data. (" + daysAgo + "/" + maxDays + " days)");
                    } else {
                        debug("Found " + playername + "'s data. (" + daysAgo + " days)");
                    }
                } catch (Exception err) {
                    if (debug) {
                        debug("Error when trying to purge player data for " + data.getUuid() + ", see below:");
                        err.printStackTrace();
                    }
                }
            }
        });
    }

    private static String banSource(int banInt) {
        return switch (banInt) {
            case 1 -> "Essentials";
            case 2 -> "LiteBans";
            case 3 -> "AdvancedBan";
            case 4 -> "Vanilla";
            default -> "Unknown";
        };
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
    }

    public static void updateConfig(JavaPlugin pl) {
        debug = pl.getConfig().getBoolean("Other.Debug");
        sounds = pl.getConfig().getBoolean("General.Sounds");
        final String lvu = pl.getConfig().getString("LVU");

        if(lvu == null || lvu.isEmpty()) {
            pl.getConfig().set("LVU", Supports.getMCVersion());
        } else {
            if(!lvu.equals(Supports.getMCVersion())) {
                log("--------------------[ Server Version Updated ]----------------------", false, false);
                log("Your server was running " + lvu + " and is now running " + Supports.getMCVersion() + ".", true, true);
                log("Sound values may need to be changed in ChatFeeling's emotes.yml, or erasing this file altogether.", true, true);
                log("------------------------------------------------------------------", false, false);
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
            CommandManager.updateCommands(pl.getConfig());
        }
    }

    public boolean hasPerm(CommandSender p, String node, Boolean admin_cmd) {
        if(!(p instanceof Player)) {
            return true;
        }
        if(!node.equalsIgnoreCase("none") && p.hasPermission(node)) {
            return true;
        }
        if(p.isOp()) {
            return true;
        }
        if(!admin_cmd && !useperms) {
            return true;
        }
        return node.startsWith("chatfeelings.") && feelings.contains(node.substring("chatfeelings.".length())) && p.hasPermission("chatfeelings.all");
    }

    public boolean hasPerm(CommandSender p, String node) {
        return hasPerm(p, node, false);
    }
    @SuppressWarnings("unused")
    public boolean hasPerm(CommandSender p, Boolean admin_cmd) {
        return hasPerm(p, "none", admin_cmd);
    }
    @SuppressWarnings("unused")
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
            final PlayerStorage store = storage;
            final Map<String, Integer> sent = store == null ? Collections.emptyMap() : store.getGlobalSent();

            Map<String, Integer> map = new HashMap<>();
            for (String fl : feelings) {
                final String flc = capitalizeString(fl);
                map.put(flc, sent.getOrDefault(flc, 1));
            }
            return map;
        }));

        return metrics;

    } // End Metrics

    protected void pop(CommandSender sender) {
        if (!sounds) {
            return;
        }

        if (sender instanceof Player p) {
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

        if (sender instanceof Player p) {
            try {
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

        if (sender instanceof Player p) {
            try {
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

    // Only for debug, internal use and timing. Available if debug mode is
    // Enabled in the config.yml and player has OP or admin permissions.
    private void generatePlayerTestFile(CommandSender sender) {
        final long start = System.currentTimeMillis();
        morePaperLib.scheduling().asyncScheduler().run(() -> {
            final UUID uuid = UUID.randomUUID();
            try {
                storage.recordLogin(uuid, "User-" + uuid, "127.0.0.1", System.currentTimeMillis());
                Msgs.sendPrefix(sender, "&a&lDone! &7Player test data (" + storage.getName() + ") created in &f"
                        + (System.currentTimeMillis() - start) + "ms");
                pop(sender);
            } catch (Exception err) {
                if (debug) {
                    err.printStackTrace();
                }
                Msgs.sendPrefix(sender, "&7See console. Test data creation failed.");
                bass(sender);
            }
        });
    }

    private static String getIp(Player p) {
        final java.net.InetSocketAddress address = p.getAddress();
        if (address == null || address.getAddress() == null) {
            return null;
        }
        return address.getAddress().getHostAddress();
    }

    private void updateLastOn(Player p) {
        final UUID uuid = p.getUniqueId();
        final String name = p.getName();
        final String ip = getIp(p);
        final PlayerStorage store = storage;
        morePaperLib.scheduling().asyncScheduler().run(() -> store.recordLogin(uuid, name, ip, System.currentTimeMillis()));
    }

    private void statsAdd(Player p, String emotion) {
        final UUID uuid = p.getUniqueId();
        final PlayerStorage store = storage;
        morePaperLib.scheduling().asyncScheduler().run(() -> {
            store.incrementGlobalSent(emotion);
            store.incrementSent(uuid, emotion);
        });
    }

    /** Finds the UUID of a player who has joined before. Checks online players first, then storage (slow, use async). */
    public UUID hasPlayedNameGetUUID(String inputsearch) {
        final Player online = Bukkit.getPlayerExact(inputsearch);
        if (online != null) {
            return online.getUniqueId();
        }
        return storage.findUUIDByName(inputsearch);
    }

    public String hasPlayedUUIDGetName(UUID uuid) {
        final PlayerData data = storage.load(uuid);
        return data == null ? "0" : data.getUsername();
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

        storageType = StorageFactory.configuredType(getConfig());
        storage = StorageFactory.create(getConfig(), getDataFolder());
        debug("Using " + storage.getName() + " for player data storage.");

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
            log("Check for updates daily at https://ci.zachduda.com/job/ChatFeelings/", false, true);
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
        final PlayerData data = storage.load(u);
        return data == null ? 0 : data.getSent(capitalizeString(feeling.toLowerCase()));
    }

    public List < String > APIgetFeelings() {
        return feelings;
    }

    public int APIgetTotalSent(UUID u) {
        final PlayerData data = storage.load(u);
        return data == null ? 0 : data.getTotalSent();
    }

    public boolean APIisAcceptingFeelings(UUID u) {
        final PlayerData data = storage.load(u);
        return data == null || data.isAllowingFeelings();
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

    /** Must be called async: loads player data from storage. */
    private void getStats(CommandSender p, UUID uuid, boolean isown) {
        final PlayerData data = storage.load(uuid);
        if (data == null) {
            bass(p);
            Msgs.sendPrefix(p, msg.getString("Folder-Not-Found"));
            return;
        }
        final String name = data.getUsername();
        if (isown) {
            Msgs.send(p, Objects.requireNonNull(msg.getString("Stats-Header-Own")).replace("%player%", name));
        } else {
            Msgs.send(p, Objects.requireNonNull(msg.getString("Stats-Header-Other")).replace("%player%", name));
        }

        final int totalsent = data.getTotalSent();

        if(totalsent == 0) {
            if(isown) {
                Msgs.send(p, "&f   &8&l> &7You haven't sent anyone feelings yet!");
            } else {
                Msgs.send(p, "&f   &8&l> &f" + name + " &7hasn't sent feelings yet!");
            }
        } else {
            for (String fl : feelings) {
                String flcap = capitalizeString(fl);

                final int fsent = data.getSent(flcap);

                if(fsent > 0) {
                    // grammatical adjustment logic
                    switch (fl.toLowerCase()) {
                        case "kiss" -> flcap = "Kisse";
                        case "cry" -> flcap = "Crie";
                        case "welcomeback" -> flcap = "Welcome";
                        case "punch" -> flcap = "Punche";
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
        pop(p);
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
        if(command.getName().equalsIgnoreCase("feelings")) {
            completions.add("");
            completions.add("1");
            completions.add("2");
            completions.add("3");
            completions.add("4");
            return StringUtil.copyPartialMatches(args[0].toLowerCase(), completions, new ArrayList<>());
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

    @SuppressWarnings("ReplaceAllNonRegex")
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String cmdLabel, String[] args) {
        final String cmdlr = cmd.getName().toLowerCase();
        if (cmdlr.equals("chatfeelings") && args.length == 0) {
            Msgs.send(sender, "");
            Msgs.send(sender, msg.getString("Prefix-Header"));
            Msgs.send(sender, "&8&l> &#f4fcab/cf help &7&ofor commands & settings.");
            Msgs.send(sender, "");
            pop(sender);
            return true;
        }

        if (cmdlr.equals("chatfeelings") && args[0].equalsIgnoreCase("version")) {
            Msgs.send(sender, "");
            Msgs.send(sender, msg.getString("Prefix-Header"));
            Msgs.send(sender, "&8&l> &7You're running &fv" + getDescription().getVersion());
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
                if (!(sender instanceof Player p)) {
                    Msgs.sendPrefix(sender, msg.getString("No-Player"));
                    return true;
                }
                final UUID own = p.getUniqueId();
                morePaperLib.scheduling().asyncScheduler().run(() -> getStats(sender, own, true));
                return true;
            }

            if (!hasPerm(sender,"chatfeelings.stats.others", true)) {
                noPermission(sender);
                return true;
            }

            if (args[1].equalsIgnoreCase("console")) {
                Msgs.sendPrefix(sender, msg.getString("Console-Not-Player"));
                bass(sender);
                return true;
            }

            final String lookup = args[1];
            morePaperLib.scheduling().asyncScheduler().run(() -> {
                final UUID getUUID = hasPlayedNameGetUUID(lookup);
                if (getUUID == null) {
                    bass(sender);
                    Msgs.sendPrefix(sender, Objects.requireNonNull(msg.getString("Player-Never-Joined")).replace("%player%", lookup));
                    return;
                }
                getStats(sender, getUUID, false);
            });
            return true;
        }

        if (cmdlr.equals("chatfeelings") && args[0].equalsIgnoreCase("debug-generate-file")) {
            if (!hasPerm(sender, "chatfeelings.admin", true)) {
                noPermission(sender);
                return true;
            }
            if (!debug) {
                Msgs.sendPrefix(sender, "&7Enabled debug mode in your config to use this.");
                bass(sender);
                return true;
            }
            generatePlayerTestFile(sender);
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

                if (!StorageFactory.configuredType(getConfig()).equals(storageType)) {
                    Msgs.send(sender, "&8&l> &#FF8C6BHeads up! &7Changing the storage type requires a full server restart.");
                }
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
                Msgs.send(sender, "&8&l> &a&l✓  &7Plugin Reloaded. &#FF8C6B(1 file was regenerated)");
            }
            Msgs.send(sender, "");
            levelup(sender);

            return true;
        }

        if (cmdlr.equals("chatfeelings") && args[0].equalsIgnoreCase("help")) {
            Msgs.send(sender, "");
            Msgs.send(sender, msg.getString("Prefix-Header"));
            Msgs.send(sender, "&8&l> &#f4fcab/cf help &7" + msg.getString("Command-Help.Descriptions.Help"));
            if (hasPerm(sender, "chatfeelings.ignore")) {
                Msgs.send(sender, "&8&l> &#f4fcab/cf ignore (player) &7" + msg.getString("Command-Help.Descriptions.Ignore"));
                Msgs.send(sender, "&8&l> &#f4fcab/cf ignore all &7" + msg.getString("Command-Help.Descriptions.Ignore-All"));
            }
            if (hasPerm(sender, "chatfeelings.stats")) {
                Msgs.send(sender, "&8&l> &#f4fcab/cf stats &7" + msg.getString("Command-Help.Descriptions.Stats"));
            }
            if (hasPerm(sender, "chatfeelings.stats.others", true)) {
                Msgs.send(sender, "&8&l> &#f4fcab/cf stats (player) &7" + msg.getString("Command-Help.Descriptions.Stats-Others"));
            }
            if (hasPerm(sender, "chatfeelings.mute", true)) {
                Msgs.send(sender, "&8&l> &#f4fcab/cf mute (player) &7" + msg.getString("Command-Help.Descriptions.Mute"));
                Msgs.send(sender, "&8&l> &#f4fcab/cf unmute (player) &7" + msg.getString("Command-Help.Descriptions.Unmute"));
                Msgs.send(sender, "&8&l> &#f4fcab/cf mutelist &7" + msg.getString("Command-Help.Descriptions.Mute-List"));
            }
            if (hasPerm(sender, "chatfeelings.admin", true)) {
                Msgs.send(sender, "&8&l> &#f4fcab/cf version &7" + msg.getString("Command-Help.Descriptions.Plugin-Version"));
                Msgs.send(sender, "&8&l> &#f4fcab/cf reload &7" + msg.getString("Command-Help.Descriptions.Plugin-Reload"));
            }
            Msgs.send(sender, "&8&l> &#fcdcab&l/feelings &7" + msg.getString("Command-Help.Descriptions.Feelings"));
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

            if (args[1].equalsIgnoreCase("console")) {
                Msgs.sendPrefix(sender, msg.getString("Console-Not-Player"));
                bass(sender);
                return true;
            }

            final String lookup = args[1];
            morePaperLib.scheduling().asyncScheduler().run(() -> {
                final UUID getUUID = hasPlayedNameGetUUID(lookup);
                if (getUUID == null) {
                    bass(sender);
                    Msgs.sendPrefix(sender, Objects.requireNonNull(msg.getString("Player-Never-Joined")).replace("%player%", lookup));
                    return;
                }

                String getName = hasPlayedUUIDGetName(getUUID);
                if (getName.equals("0")) {
                    getName = lookup;
                }
                Msgs.sendPrefix(sender, "&fThe UUID of " + getName + " is &7" + getUUID);
                pop(sender);
            });
            return true;
        }

        if (cmdlr.equals("chatfeelings") && args[0].equalsIgnoreCase("mutelist")) {
            if (!hasPerm(sender, "chatfeelings.mute", true)) {
                noPermission(sender);
                return true;
            }

            final long secsLeft = ((lastmutelist / 1000) + 60) - (System.currentTimeMillis() / 1000);
            if (secsLeft > 0) {
                Msgs.sendPrefix(sender, "&7Please wait &f&l" + secsLeft + "s &7before checking the mute list.");
                return true;
            }
            // We need a 60s global cooldown in case they use MySQL. Doing this command w/ MySQL can suck up LOTS of CPU.
            if (haslitebans || hasadvancedban || storage instanceof com.zachduda.chatfeelings.storage.MySQLStorage) {
                lastmutelist = System.currentTimeMillis();
            }
            morePaperLib.scheduling().asyncScheduler().run(() -> {
                final List<PlayerData> all = storage.loadAll();
                if (all.isEmpty()) {
                    Msgs.sendPrefix(sender, msg.getString("Folder-Not-Found"));
                    bass(sender);
                    return;
                }

                Msgs.send(sender, "");
                Msgs.send(sender, msg.getString("Mute-List-Header"));

                final String listFormat = Objects.requireNonNull(msg.getString("Mute-List-Player"));
                int totalmuted = 0;

                for (PlayerData data : all) {
                    final int muteInt = isMuted(data.getUuid(), data.getIp());
                    final boolean cfMuted = data.isMuted();
                    if (!cfMuted && muteInt == 0) {
                        continue;
                    }

                    totalmuted++;
                    String line = listFormat.replace("%player%", data.getUsername());
                    if (muteInt != 0) {
                        line += " &#FF8C6B(" + banSource(muteInt) + (cfMuted ? " & CF)" : ")");
                    }
                    Msgs.send(sender, line);
                }

                if (totalmuted == 1) {
                    Msgs.send(sender, Objects.requireNonNull(msg.getString("Mute-List-Total-One")).replace("%total%", "1"));
                } else if (totalmuted == 0) {
                    Msgs.send(sender, Objects.requireNonNull(msg.getString("Mute-List-Total-Zero")).replace("%total%", "0"));
                } else {
                    Msgs.send(sender, Objects.requireNonNull(msg.getString("Mute-List-Total-Many")).replace("%total%", Integer.toString(totalmuted)));
                }
                Msgs.send(sender, "");
                pop(sender);
            });
            return true;
        }

        if (cmdlr.equals("chatfeelings") && (args[0].equalsIgnoreCase("mute") || args[0].equalsIgnoreCase("unmute"))) {
            final boolean muting = args[0].equalsIgnoreCase("mute");
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
                Msgs.sendPrefix(sender, msg.getString(muting ? "No-Player-Mute" : "No-Player-Unmute"));
                bass(sender);
                return true;
            }

            if (muting && args[1].equalsIgnoreCase(sender.getName())) {
                bass(sender);
                Msgs.sendPrefix(sender, msg.getString("Cant-Mute-Self"));
                return true;
            }

            final String lookup = args[1];
            morePaperLib.scheduling().asyncScheduler().run(() -> {
                final UUID muteUUID = hasPlayedNameGetUUID(lookup);
                final PlayerData data = muteUUID == null ? null : storage.load(muteUUID);

                if (data == null) {
                    bass(sender);
                    Msgs.sendPrefix(sender, Objects.requireNonNull(msg.getString("Player-Never-Joined")).replace("%player%", lookup));
                    return;
                }

                final String playername = data.getUsername();

                if (muting) {
                    if (data.isMuted()) {
                        bass(sender);
                        Msgs.sendPrefix(sender, msg.getString("Player-Already-Muted"));
                        if (getConfig().getBoolean("General.Extra-Help") && msg.contains("Already-Mute-Unmute-Suggestion")) {
                            Msgs.sendPrefix(sender, msg.getString("Already-Mute-Unmute-Suggestion"));
                        }
                        return;
                    }

                    if (!storage.setMuted(data.getUuid(), true)) {
                        bass(sender);
                        Msgs.sendPrefix(sender, "&#FF8C6BError. &fWe couldn't save that player's mute status. Check console.");
                        return;
                    }
                    Msgs.sendPrefix(sender, Objects.requireNonNull(msg.getString("Player-Has-Been-Muted")).replace("%player%", playername));
                    pop(sender);
                    if (isMuted(data.getUuid(), data.getIp()) != 0) {
                        Msgs.sendPrefix(sender, Objects.requireNonNull(msg.getString("Extra-Mute-Present")).replace("%player%", playername));
                    }
                    return;
                }

                // Unmuting
                if (data.isMuted()) {
                    if (!storage.setMuted(data.getUuid(), false)) {
                        bass(sender);
                        Msgs.sendPrefix(sender, "&#FF8C6BError. &fWe couldn't save that player's mute status. Check console.");
                        return;
                    }
                    Msgs.sendPrefix(sender, Objects.requireNonNull(msg.getString("Player-Has-Been-Unmuted")).replace("%player%", playername));
                    pop(sender);
                    return;
                }

                bass(sender);
                final int muteInt = isMuted(data.getUuid(), data.getIp());
                final String key = switch (muteInt) {
                    case 3 -> "Player-Muted-Via-AdvancedBan";
                    case 2 -> "Player-Muted-Via-LiteBans";
                    case 1 -> "Player-Muted-Via-Essentials";
                    default -> "Player-Already-Unmuted";
                };
                // Older messages.yml defaults used "%player" without the closing %, so handle both.
                Msgs.sendPrefix(sender, Objects.requireNonNull(msg.getString(key)).replace("%player%", playername).replace("%player", playername));
            });
            return true;
        }

        if (cmdlr.equals("chatfeelings") && (args[0].equalsIgnoreCase("ignore") || args[0].equalsIgnoreCase("unignore"))) {
            if (!hasPerm(sender, "chatfeelings.ignore")) {
                noPermission(sender);
                return true;
            }

            if (!(sender instanceof Player p)) {
                Msgs.sendPrefix(sender, "&#FF8C6B&lSorry. &fOnly players can ignore other players.");
                return true;
            }

            final UUID self = p.getUniqueId();

            if (args.length == 1) {
                if (getConfig().getBoolean("General.Cooldowns.Ignore-List.Enabled", true)) {
                    if (Cooldowns.isIgnoreListCooldown(self)) {
                        Msgs.sendPrefix(sender, msg.getString("Ignore-List-Cooldown"));
                        bass(sender);
                        return true;
                    }

                    Cooldowns.ignoreListCooldown(p);
                }

                morePaperLib.scheduling().asyncScheduler().run(() -> {
                    final PlayerData data = storage.load(self);
                    if (data == null) {
                        Msgs.sendPrefix(sender, msg.getString("Folder-Not-Found"));
                        bass(sender);
                        return;
                    }

                    Msgs.send(sender, " ");
                    Msgs.send(sender, msg.getString("Ignore-List-Header"));
                    if (data.getIgnoring().isEmpty()) {
                        if (data.isAllowingFeelings()) {
                            Msgs.send(sender, msg.getString("Ignore-List-None"));
                        } else {
                            Msgs.send(sender, msg.getString("Ignore-List-All"));
                        }
                    } else {
                        for (String ignoredUUID : data.getIgnoring()) {
                            try {
                                String name = hasPlayedUUIDGetName(UUID.fromString(ignoredUUID));
                                if (!name.equals("0")) {
                                    Msgs.send(sender, "  &8&l> &f&l" + name);
                                }
                            } catch (IllegalArgumentException badUuid) {
                                debug("Skipping invalid UUID in " + p.getName() + "'s ignore list: " + ignoredUUID);
                            }
                        }
                    }

                    Msgs.send(sender, " ");
                    pop(sender);
                });
                return true;
            }

            if (args[1].equalsIgnoreCase(sender.getName())) {
                bass(sender);
                Msgs.sendPrefix(sender, msg.getString("Cant-Ignore-Self"));
                return true;
            }

            if (getConfig().getBoolean("General.Cooldowns.Ignoring.Enabled") && !sender.isOp() && !hasPerm(sender, "chatfeelings.bypasscooldowns", true)) {
                if (Cooldowns.isIgnoreCooldown(self)) {
                    bass(sender);
                    Msgs.sendPrefix(sender, msg.getString("Ignore-Cooldown"));
                    return true;
                }

                Cooldowns.ignoreCooldown(p);
            }

            if (args[1].equalsIgnoreCase("console")) {
                Msgs.sendPrefix(sender, msg.getString("Console-Not-Player"));
                bass(sender);
                return true;
            }

            final String lookup = args[1];
            morePaperLib.scheduling().asyncScheduler().run(() -> {
                if (lookup.equalsIgnoreCase("all")) {
                    final Boolean allow = storage.toggleAllowFeelings(self);
                    if (allow == null) {
                        Msgs.sendPrefix(sender, "&#FF8C6BSorry!&f We couldn't find your player data.");
                        bass(sender);
                        return;
                    }
                    Msgs.sendPrefix(sender, msg.getString(allow ? "Ignoring-Off-All" : "Ignoring-On-All"));
                    pop(sender);
                    return;
                }

                final UUID ignoreUUID = hasPlayedNameGetUUID(lookup);
                if (ignoreUUID == null) {
                    bass(sender);
                    Msgs.sendPrefix(sender, Objects.requireNonNull(msg.getString("Player-Never-Joined")).replace("%player%", lookup));
                    return;
                }

                if (ignoreUUID.equals(self)) {
                    bass(sender);
                    Msgs.sendPrefix(sender, msg.getString("Cant-Ignore-Self"));
                    return;
                }

                final Boolean nowIgnoring = storage.toggleIgnoring(self, ignoreUUID);
                if (nowIgnoring == null) {
                    Msgs.sendPrefix(sender, "&#FF8C6BSorry!&f We couldn't find your player data.");
                    bass(sender);
                    return;
                }

                Msgs.sendPrefix(sender, Objects.requireNonNull(msg.getString(nowIgnoring ? "Ignoring-On-Player" : "Ignoring-Off-Player")).replace("%player%", lookup));
                pop(sender);
            });
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
                if (FileSetup.getFeelingBoolean(fl, "Enable")) {
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
            Msgs.send(sender, msg.getString("Feelings-Help") + "                            " +
                    Objects.requireNonNull(msg.getString("Feelings-Help-Page")).replace("%page%", Integer.toString(page)).replace("%pagemax%", Integer.toString(totalpages)));
            for (int i = start; i < end; i++) {
                if(i < enabledfeelings.size()) {
                    final String flcap = capitalizeString(enabledfeelings.get(i));
                    final String cfl = enabledfeelings.get(i).toLowerCase();
                    if (FileSetup.getFeelingBoolean(cfl, "Enable")) {
                        String description = msg.getString(path + flcap);
                        if (description == null) {
                            description = FileSetup.getFeelingString(cfl, "Description");
                        }
                        if (description == null) {
                            description = "&7A custom feeling.";
                        }
                        if (hasPerm(sender, "chatfeelings." + cfl) || hasPerm(sender, "chatfeelings.all")) {
                            Msgs.send(sender, "&8&l> &f&l/" + cfl + plyr + "&7 " + description);
                        } else {
                            Msgs.send(sender, "&8&l> &#FF8C6B/" + cfl + plyr + "&7 " + msg.getString("Command-List-NoPerm"));
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
                    if (sender instanceof Player p) {
                        final Long lastSent = Cooldowns.cooldown.get(p.getUniqueId());
                        if (lastSent != null) {
                            int cooldownTime = getConfig().getInt("General.Cooldowns.Feelings.Seconds");
                            long secondsLeft = ((lastSent / 1000) + cooldownTime) - (System.currentTimeMillis() / 1000);
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
                final FileConfiguration feelingConfig = FileSetup.loadFeelingConfig(cmdlr);

                if (sender instanceof Player p) {
                    if (disabledsendingworlds.contains(p.getWorld().getName())) {
                        bass(sender);
                        Msgs.sendPrefix(sender, msg.getString("Sending-World-Disabled"));
                        return;
                    }
                }

                if (!feelingConfig.getBoolean("Enable")) {
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
                        Msgs.sendPrefix(sender, Objects.requireNonNull(msg.getString("Sender-Is-Target")).replace("%command%", cmd.getName().toLowerCase()).replace("welcomeback", "welcome-back").replace("yell", "yell at").replace("cry", "cry on"));
                        return;
                    }
                }

                if (disabledreceivingworlds.contains(target.getWorld().getName())) {
                    bass(sender);
                    Msgs.sendPrefix(sender, msg.getString("Receiving-World-Disabled"));
                    return;
                }

                // Radius & Sleeping Check ---------------------------
                if (sender instanceof Player p) {
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

                if (sender instanceof Player p) {
                    final int muteInt = isMuted(p.getUniqueId(), null);

                    if (muteInt != 0) {
                        debug(sender.getName() + " tried to use /" + cmdLabel + ", but is muted by " + banSource(muteInt) + ".");
                        bass(sender);
                        Msgs.sendPrefix(sender, msg.getString("Is-Muted"));
                        return;
                    }

                    final PlayerData me = storage.load(p.getUniqueId());
                    if (me != null && me.isMuted()) {
                        debug(sender.getName() + " tried to use /" + cmdLabel + ", but was muted (via CF).");
                        bass(sender);
                        Msgs.sendPrefix(sender, msg.getString("Is-Muted"));
                        return;
                    }
                }

                final PlayerData targetData = storage.load(target.getUniqueId());
                if (targetData != null) {
                    if (sender instanceof Player p && targetData.isIgnoring(p.getUniqueId())) {
                        bass(sender);
                        Msgs.sendPrefix(sender,
                                Objects.requireNonNull(msg.getString("Target-Is-Ignoring")).replace("%player%", target.getName()));

                        debug("Not sending feeling to " + target.getName() + " because they are ignoring " + p.getName());
                        return;
                    }

                    if (!targetData.isAllowingFeelings()) {
                        bass(sender);
                        Msgs.sendPrefix(sender, msg.getString("Target-Is-Ignoring-All"));
                        debug("Blocking feeling because " + target.getName() + " is blocking ALL.");
                        return;
                    }
                }
                // ------------------------------------------------

                // FEELING HANDLING IS ALL BELOW -------------------------------------------------------------------------------

                // API Events ----------------------------
                // Events must be fired on the main/global thread, so wait (briefly) for listeners to decide if this is cancelled.
                final Player finalTarget = target;
                final FeelingSendEvent fse = new FeelingSendEvent(sender, finalTarget, cmdconfig);
                final FeelingRecieveEvent fre = new FeelingRecieveEvent(finalTarget, sender, cmdconfig);
                final CompletableFuture<Boolean> eventCancelled = new CompletableFuture<>();

                morePaperLib.scheduling().globalRegionalScheduler().run(() -> {
                    try {
                        Bukkit.getPluginManager().callEvent(fse);
                        if (!fse.isCancelled()) {
                            Bukkit.getPluginManager().callEvent(fre);
                        }
                        eventCancelled.complete(fse.isCancelled());
                    } catch (Throwable t) {
                        eventCancelled.completeExceptionally(t);
                    }
                });

                try {
                    if (eventCancelled.get(5, TimeUnit.SECONDS)) {
                        debug("A plugin cancelled " + sender.getName() + "'s /" + cmdlr + " via FeelingSendEvent.");
                        return;
                    }
                } catch (Exception eventErr) {
                    debug("Unable to wait for FeelingSendEvent listeners, continuing anyway: " + eventErr);
                }

                // End of API events (Except for Global event below ---------------------

                // Global Handler for PLAYER messages & Feelings ----------------------------
                if (getConfig().getBoolean("General.Global-Feelings.Enabled")) {

                    for (final Player online: Bukkit.getServer().getOnlinePlayers()) {

                        // Global Ignoring Checks -----------------
                        final PlayerData onlineData = online.getUniqueId().equals(target.getUniqueId())
                                ? targetData : storage.load(online.getUniqueId());
                        final boolean isSender = online.getName().equals(sender.getName());

                        if (onlineData != null && !onlineData.isAllowingFeelings() && !isSender) {
                            debug(online.getName() + " is blocking all feelings. Skipping Global Msg!");
                            continue;
                        }
                        // End of Global ignoring Checks -------------------

                        if (!(sender instanceof Player p)) {
                            // ONLY for CONSOLE Global notify here.
                            Msgs.send(online, NicknamePlaceholders.replacePlaceholders(feelingConfig.getString("Msgs.Global"), sender, target));
                        } else if (onlineData == null || !onlineData.isIgnoring(p.getUniqueId())) {
                            // Global for PLAYER below (only sent to those NOT ignoring the sender)
                            morePaperLib.scheduling().globalRegionalScheduler().run(() -> {
                                FeelingGlobalNotifyEvent fgne = new FeelingGlobalNotifyEvent(online, sender, finalTarget, cmdconfig);
                                Bukkit.getPluginManager().callEvent(fgne);

                                if (!fgne.isCancelled()) {
                                    Msgs.send(online, NicknamePlaceholders.replacePlaceholders(feelingConfig.getString("Msgs.Global"), sender, finalTarget));
                                }
                            });
                        } else {
                            debug(online.getName() + " is blocking feelings from " + p.getName() + ". Skipping global msg!");
                        }
                    } // end of for(online)
                    // End --------------------------------------------------

                    // Global Console Broadcast Msg ------------------------------------------------
                    if (getConfig().getBoolean("General.Global-Feelings.Broadcast-To-Console")) {
                        Msgs.send(getServer().getConsoleSender(), NicknamePlaceholders.replacePlaceholders(feelingConfig.getString("Msgs.Global"), sender, target));

                    }
                    // Global Console End --------------------------------------------------

                } else {
                    // if not global (normal)
                    // send to target
                    Msgs.send(Objects.requireNonNull(target.getPlayer()), NicknamePlaceholders.replacePlaceholders(feelingConfig.getString("Msgs.Target"), sender));
                    // send to cmd sender
                    Msgs.send(sender, NicknamePlaceholders.replacePlaceholders(feelingConfig.getString("Msgs.Sender"), target));
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
                    if (sender instanceof Player p) {
                        Cooldowns.putCooldown(p);
                    }
                }
                // -----------------------------------------------------

                // Particle Handler -------------------------------------
                if (particles) {
                    try {
                        morePaperLib.scheduling().globalRegionalScheduler().run(() -> Particles.show(target, cmdlr));
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
                        String sound1 = feelingConfig.getString("Sounds.Sound1.Name");
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
                                    (float) feelingConfig.getDouble("Sounds.Sound1.Volume"),
                                    (float) feelingConfig.getDouble("Sounds.Sound1.Pitch"));
                            if (sender instanceof Player p) {
                                p.playSound(p.getLocation(),
                                        sound1var,
                                        (float) feelingConfig.getDouble("Sounds.Sound1.Volume"),
                                        (float) feelingConfig.getDouble("Sounds.Sound1.Pitch"));
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
                        String sound2 = feelingConfig.getString("Sounds.Sound2.Name");
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
                                        (float) feelingConfig.getDouble("Sounds.Sound2.Volume"),
                                        (float) feelingConfig.getDouble("Sounds.Sound2.Pitch"));

                                if (sender instanceof Player p && !sound2.contains("DISC")) {
                                    p.playSound(p.getLocation(),
                                            sound2var,
                                            (float) feelingConfig.getDouble("Sounds.Sound2.Volume"),
                                            (float) feelingConfig.getDouble("Sounds.Sound2.Pitch"));
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
                if (sender instanceof Player p) {
                    statsAdd(p, cmdconfig);
                }
            });
            // End Stats
            return true;
        }

        if (cmdlr.equals("chatfeelings")) {
            Msgs.send(sender, "");
            Msgs.send(sender, msg.getString("Prefix-Header"));
            Msgs.send(sender, "&8&l> &#FF8C6B&lHmm. &7That command does not exist.");
            Msgs.send(sender, "");
            if (sender instanceof Player p) {
                bass(p.getPlayer());
            }
        }

        return true;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        String name = p.getName();
        if (!Cooldowns.recentlyUpdated(name)) {
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
                            Msgs.sendPrefix(p, "&#f4fcabUpdate Available &8› &7Download @ &fzachduda.com/chatfeelings &7("
                                    + getDescription().getVersion() + "→ &f"
                                    + Updater.getPostedVersion()
                                    + "&r&7)");
                        }
                    }
                }
            } catch (Exception err) {
                Main.debug("onJoin Update Err: " + err.getMessage());
                if (debug) {
                    err.printStackTrace();
                }
            }

            if (!Cooldowns.recentlyUpdated(name)) {
                updateLastOn(p);
                Cooldowns.justJoined(name);
            }

            if (p.getUniqueId().toString().equals("6191ff85-e092-4e9a-94bd-63df409c2079")) {
                Msgs.send(p, "&7This server is running &fChatFeelings &6v" + getDescription().getVersion() +
                        " &7for " + Supports.getMCVersion() + "." + Supports.getMcPatchVersion());
            }
        });
    }
}
