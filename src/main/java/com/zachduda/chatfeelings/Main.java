package com.zachduda.chatfeelings;

import com.zachduda.chatfeelings.api.Placeholders;
import com.zachduda.chatfeelings.command.ChatFeelingsCommand;
import com.zachduda.chatfeelings.command.CommandRegistrar;
import com.zachduda.chatfeelings.command.Feedback;
import com.zachduda.chatfeelings.command.FeelingCommand;
import com.zachduda.chatfeelings.command.FeelingsListCommand;
import com.zachduda.chatfeelings.command.Permissions;
import com.zachduda.chatfeelings.config.ConfigUpdater;
import com.zachduda.chatfeelings.config.Messages;
import com.zachduda.chatfeelings.config.Settings;
import com.zachduda.chatfeelings.effect.ParticleEffects;
import com.zachduda.chatfeelings.feeling.BuiltinFeelings;
import com.zachduda.chatfeelings.feeling.FeelingRegistry;
import com.zachduda.chatfeelings.feeling.FeelingService;
import com.zachduda.chatfeelings.feeling.SoundResolver;
import com.zachduda.chatfeelings.hook.NicknameResolver;
import com.zachduda.chatfeelings.hook.PunishmentHook;
import com.zachduda.chatfeelings.hook.Punishments;
import com.zachduda.chatfeelings.hook.VanishCheck;
import com.zachduda.chatfeelings.platform.MetricsSetup;
import com.zachduda.chatfeelings.platform.Scheduler;
import com.zachduda.chatfeelings.storage.PlayerData;
import com.zachduda.chatfeelings.storage.PlayerDataService;
import com.zachduda.chatfeelings.storage.StorageFactory;
import com.zachduda.chatfeelings.update.SupportCheck;
import com.zachduda.chatfeelings.update.UpdateChecker;
import com.zachduda.chatfeelings.util.Cooldowns;
import com.zachduda.chatfeelings.util.Log;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import space.arim.morepaperlib.MorePaperLib;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class Main extends JavaPlugin {
    /** Skip metrics and update checks. */
    static final boolean BETA = false;

    private MorePaperLib morePaperLib;

    private volatile Settings settings;
    private volatile Messages messages = Messages.defaults();

    private Scheduler scheduler;
    private Cooldowns cooldowns;
    private FeelingRegistry feelings;
    private PlayerDataService playerData;
    private Punishments punishments;
    private NicknameResolver nicknames;
    private Permissions permissions;
    private Feedback feedback;
    private CommandRegistrar registrar;
    private ChatFeelingsCommand chatFeelingsCommand;
    private FeelingCommand feelingCommand;
    private UpdateChecker updateChecker;
    private Metrics metrics;
    private Placeholders placeholders;
    private String storageType;
    private volatile boolean shuttingDown = false;

    @Override
    public void onEnable() {
        final long start = System.currentTimeMillis();
        Log.init(getLogger());
        morePaperLib = new MorePaperLib(this);

        // Settings are applied right away, before anything that depends on them.
        ConfigUpdater.update(this);
        settings = Settings.from(getConfig());
        Log.configure(settings.debug(), settings.reduceConsoleMessages());

        scheduler = new Scheduler(this, morePaperLib);
        cooldowns = new Cooldowns();
        messages = Messages.load(getDataFolder());
        punishments = Punishments.detect(getServer().getPluginManager());

        final boolean papiInstalled = getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
        nicknames = new NicknameResolver(papiInstalled);
        nicknames.configure(settings, messages);

        feelings = new FeelingRegistry(getDataFolder(), new SoundResolver());
        feelings.reload();

        storageType = StorageFactory.configuredType(getConfig());
        playerData = new PlayerDataService(StorageFactory.create(getConfig(), getDataFolder()));
        Log.debug("Using " + playerData.storage().getName() + " for player data storage.");

        final Supplier<Settings> settingsSupplier = this::getSettings;
        final Supplier<Messages> messagesSupplier = this::getMessages;
        feedback = new Feedback(settingsSupplier, messagesSupplier, scheduler);
        permissions = new Permissions(settingsSupplier, feelings);
        final FeelingService feelingService = new FeelingService(settingsSupplier, messagesSupplier, scheduler, feelings,
                playerData, punishments, new VanishCheck(settingsSupplier, punishments), nicknames, permissions, cooldowns,
                feedback, new ParticleEffects(scheduler));

        feelingCommand = new FeelingCommand(feelingService);
        chatFeelingsCommand = new ChatFeelingsCommand(this, feedback, permissions);
        bind("chatfeelings", chatFeelingsCommand);
        bind("feelings", new FeelingsListCommand(settingsSupplier, feelings, permissions, feedback));
        for (String name : BuiltinFeelings.NAMES) {
            bind(name, feelingCommand);
        }
        registrar = new CommandRegistrar(this, morePaperLib, scheduler);

        registrar.sync(List.of(), feelingCommand, settings.cfAlias(), chatFeelingsCommand, getCommand("chatfeelings"), false);
        scheduler.globalNextTick(() -> registrar.sync(feelings.custom(), feelingCommand, settings.cfAlias(),
                chatFeelingsCommand, null, true));

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        scheduler.async(playerData::loadGlobalSent);
        final Collection<? extends Player> online = getServer().getOnlinePlayers();
        if (online.isEmpty()) {
            purgeOldData();
        } else {
            online.forEach(this::recordJoin);
            Log.debug("Reloaded with " + online.size() + " players online... Skipping purge.");
        }

        if (papiInstalled) {
            Log.info("Hooking into PlaceholderAPI...");
            placeholders = new Placeholders(this);
            placeholders.register();
        }

        if (!BETA) {
            if (settings.metrics()) {
                metrics = MetricsSetup.start(this);
            } else {
                Log.debug("Metrics was disabled. Guess we won't support Zach today. :(");
            }
            if (settings.checkForUpdates()) {
                updateChecker = new UpdateChecker(getPluginVersion(), scheduler);
                updateChecker.start();
            }
        } else {
            Log.warn("[!] This is a BETA version. Check for updates manually on Github/Discord!");
            Log.warn("Check for updates daily at https://ci.zachduda.com/job/ChatFeelings/");
        }
        new SupportCheck(getPluginVersion(), scheduler).run();

        Log.debug("Finished! ChatFeelings was loaded in " + (System.currentTimeMillis() - start) + "ms");
    }

    @Override
    public void onDisable() {
        shuttingDown = true;
        if (registrar != null) {
            registrar.unregisterAll();
        }
        if (placeholders != null) {
            placeholders.unregister();
        }
        if (metrics != null) {
            metrics.shutdown();
        }
        if (updateChecker != null) {
            updateChecker.stop();
        }
        if (playerData != null && scheduler != null) {
            for (Player player : getServer().getOnlinePlayers()) {
                final UUID uuid = player.getUniqueId();
                final String name = player.getName();
                final String ip = ipOf(player);
                scheduler.async(() -> playerData.recordLogin(uuid, name, ip, System.currentTimeMillis()));
            }
        }
        if (scheduler != null) {
            scheduler.shutdown(10);
        }
        if (playerData != null) {
            playerData.close();
        }
        if (cooldowns != null) {
            cooldowns.clear();
        }
    }

    /**
     * Reloads config.yml, messages.yml and the Feelings folder. Blocking (file I/O), so it's called from a
     * background task; command changes are applied on the main/global thread.
     *
     * @return true if the storage type changed, which only takes effect after a restart
     */
    public synchronized boolean reloadAll() {
        reloadConfig();
        ConfigUpdater.update(this);
        final Settings newSettings = Settings.from(getConfig());
        Log.configure(newSettings.debug(), newSettings.reduceConsoleMessages());
        final Messages newMessages = Messages.load(getDataFolder());
        feelings.reload();

        settings = newSettings;
        messages = newMessages;
        nicknames.configure(newSettings, newMessages);
        scheduler.global(this::syncCommands);

        if (getServer().getOnlinePlayers().isEmpty()) {
            Log.debug("Purging old data files since nobody is currently online...");
            purgeOldData();
        }
        if (!newSettings.disabledSendingWorlds().isEmpty()) {
            Log.debug("Sending Feelings is disabled in: " + newSettings.disabledSendingWorlds());
        }
        if (!newSettings.disabledReceivingWorlds().isEmpty()) {
            Log.debug("Receiving Feelings is disabled in: " + newSettings.disabledReceivingWorlds());
        }
        return !StorageFactory.configuredType(getConfig()).equals(storageType);
    }

    private void syncCommands() {
        registrar.sync(feelings.custom(), feelingCommand, settings.cfAlias(), chatFeelingsCommand,
                getCommand("chatfeelings"), true);
    }

    private void bind(String name, TabExecutor executor) {
        final PluginCommand command = getCommand(name);
        if (command == null) {
            Log.warn("The /" + name + " command is missing from plugin.yml.");
            return;
        }
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    /** Saves a joining player's name, IP and last-on time, and caches their data while they're online. */
    void recordJoin(Player player) {
        final UUID uuid = player.getUniqueId();
        final String name = player.getName();
        final String ip = ipOf(player);
        final boolean write = !cooldowns.lastOnRecentlyUpdated(uuid);
        if (write) {
            cooldowns.markLastOnUpdated(uuid);
        }
        playerData.markOnline(uuid);
        scheduler.async(() -> {
            if (write) {
                playerData.recordLogin(uuid, name, ip, System.currentTimeMillis());
            }
            playerData.loadOnline(uuid);
        });
    }

    /** Saves a leaving player's last-on time (unless it was saved under a minute ago) and drops them from the cache. */
    void recordQuit(Player player) {
        final UUID uuid = player.getUniqueId();
        final String name = player.getName();
        final String ip = ipOf(player);
        playerData.markOffline(uuid);
        if (cooldowns.lastOnRecentlyUpdated(uuid)) {
            Log.debug("Skipped updating " + name + "'s data, they joined less than 60s ago.");
            return;
        }
        cooldowns.markLastOnUpdated(uuid);
        scheduler.async(() -> playerData.recordLogin(uuid, name, ip, System.currentTimeMillis()));
    }

    /** Deletes data for banned players and players who haven't joined in a while, per Other.Player-Files. */
    private void purgeOldData() {
        final Settings s = settings;
        scheduler.async(() -> {
            playerData.cleanupInvalid();
            final long today = System.currentTimeMillis() / 86_400_000L;
            for (PlayerData data : playerData.listAll()) {
                if (shuttingDown || playerData.isClosed()) {
                    Log.debug("Stopped purging old player data because ChatFeelings is shutting down.");
                    return;
                }
                try {
                    final String name = data.getUsername();
                    final long daysAgo = Math.abs(data.getLastOn() / 86_400_000L - today);
                    // Only the player's own UUID is checked: an IP ban could also match innocent players on the same IP.
                    final PunishmentHook bannedBy = s.eraseIfBanned() ? punishments.findBan(data.getUuid(), null) : null;
                    if (bannedBy != null) {
                        if (playerData.deleteIfUnchanged(data)) {
                            Log.debug("Deleted " + name + "'s data. They were banned! (" + bannedBy.name() + ")");
                        }
                    } else if (s.cleanupPlayerData() && daysAgo >= s.cleanupAfterDays()) {
                        // Skipped if they joined since the list was taken (deleteIfUnchanged compares last-on).
                        if (playerData.deleteIfUnchanged(data)) {
                            Log.debug("Deleted " + name + "'s data because it's " + daysAgo + " days old. (Max is "
                                    + s.cleanupAfterDays() + " Days)");
                        }
                    } else if (s.cleanupPlayerData()) {
                        Log.debug("Keeping " + name + "'s data. (" + daysAgo + "/" + s.cleanupAfterDays() + " days)");
                    } else {
                        Log.debug("Found " + name + "'s data. (" + daysAgo + " days)");
                    }
                } catch (RuntimeException err) {
                    Log.debug("Error when trying to purge player data for " + data.getUuid(), err);
                }
            }
        });
    }

    private static String ipOf(Player player) {
        final InetSocketAddress address = player.getAddress();
        if (address == null || address.getAddress() == null) {
            return null;
        }
        return address.getAddress().getHostAddress();
    }

    // ------------------------------------------------------------------------------------------
    // Accessors for the command, API and placeholder classes.

    public Settings getSettings() {
        return settings;
    }

    public Messages getMessages() {
        return messages;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public Cooldowns getCooldowns() {
        return cooldowns;
    }

    public FeelingRegistry getFeelings() {
        return feelings;
    }

    public PlayerDataService getPlayerData() {
        return playerData;
    }

    public Punishments getPunishments() {
        return punishments;
    }

    public Permissions getPermissions() {
        return permissions;
    }

    public Feedback getFeedback() {
        return feedback;
    }

    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }

    @SuppressWarnings("deprecation") // getPluginMeta() is Paper-only; this works everywhere
    public String getPluginVersion() {
        return getDescription().getVersion();
    }

    @SuppressWarnings("deprecation")
    public String getAuthor() {
        final List<String> authors = getDescription().getAuthors();
        return authors.isEmpty() ? "zach_attack" : authors.get(0);
    }
}
