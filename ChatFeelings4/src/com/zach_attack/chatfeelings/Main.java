package com.zach_attack.chatfeelings;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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

import com.zach_attack.chatfeelings.other.Metrics;
import com.zach_attack.chatfeelings.other.Updater;
import com.zach_attack.puuids.api.PUUIDS;
import com.zach_attack.chatfeelings.api.ChatFeelingsAPI;

import litebans.api.Database;
import me.leoko.advancedban.manager.PunishmentManager;

import com.earth2me.essentials.Essentials;
import com.zach_attack.chatfeelings.Msgs;
import com.zach_attack.chatfeelings.api.FeelingGlobalNotifyEvent;
import com.zach_attack.chatfeelings.api.FeelingRecieveEvent;
import com.zach_attack.chatfeelings.api.FeelingSendEvent;

public class Main extends JavaPlugin implements Listener {

	// FOR GITHUB PRE-RELEASES -----------------
	
	boolean isPreRelease = false;
	
	// ----------------------------------
	
	public ChatFeelingsAPI api;
	
	private static boolean hasess = false;
	private static boolean haslitebans = false;
	private static boolean hasadvancedban = false;
	
	private static boolean usingpuuids = false;

	private static boolean usevanishcheck = false;
	
	static boolean particles = true;
	
	private static boolean useperms = false;
	
	static boolean multiversion = false;
    static boolean debug = false;
	
	private static boolean sounds = false;
	
	private long lastreload = 0;

	private ArrayList<String> disabledsendingworlds = (ArrayList<String>) getConfig()
			.getStringList("General.Disable-Sending-Worlds");
	private ArrayList<String> disabledreceivingworlds = (ArrayList<String>) getConfig()
			.getStringList("General.Disable-Receiving-Worlds");

	private void removeAll(Player p) {
		Cooldowns.removeAll(p);
	}

	public void onDisable() {
		disabledsendingworlds.clear();
		disabledreceivingworlds.clear();

		lastreload = 0;
		
		if (Bukkit.getOnlinePlayers().size() > 0) {
			// Remove all HashMaps to prevent memory leaks if the plugin is reloaded when players are on.
			for (final Player online : Bukkit.getServer().getOnlinePlayers()) {
				removeAll(online.getPlayer());
			}
		}
		
	}

	public void updateConfig() {
		boolean confdebug = getConfig().getBoolean("Other.Debug");
		
		if(confdebug) {
			debug = true;
		} else {
			debug = false;
		}
		
		String version = Bukkit.getBukkitVersion().replace("-SNAPSHOT", "");
		
		if(getConfig().getBoolean("General.Sounds", true)) {
			if(!version.contains("1.13") && !version.contains("1.14")) {
				getLogger().warning("Sounds were disabled as you are using " + version + " and not 1.13.X or higher.");
				sounds = false;
			} else {
				if(debug) {
					getLogger().info("[Debug] Using supported MC version for sounds: " + version);
				}
				sounds = true;
			}
		} else {
			sounds = false;
		}
		
		if(getConfig().getBoolean("General.Particles", true)) {
			if(!version.contains("1.14") && !version.contains("1.13") && !version.contains("1.12")) {
				getLogger().warning("Particles were disabled. You're using " + version + " and not 1.12.X or higher.");
				particles = false;
			} else {
				if(debug) {
					getLogger().info("[Debug] Using 1.12+, Particles have been enabled.");
				}
				particles = true;
			}
		} else {
			particles = false;
		}
		
		if(getConfig().getBoolean("Other.Vanished-Players.Check", true)) {
			usevanishcheck = true;
			} else {
				usevanishcheck = false;
			}
		
		if(getConfig().contains("General.Use-Feeling-Permissions", true)) {
		if(getConfig().getBoolean("General.Use-Feeling-Permissions")) {
			useperms = true;
		} else {
			useperms = false;
		}} else {
			useperms = false;
		}
		
		if(getConfig().contains("General.Multi-Version-Support", false)) {
		if(getConfig().getBoolean("General.Multi-Version-Support")) {
			multiversion = true;
		} else {
			multiversion = false;
		}} else {
			multiversion = false;
		}
	}
	
	public void addMetrics() {
		if (!getConfig().getBoolean("Other.Metrics", true)) {
			if(debug) {
				getLogger().info("[Debug] Metrics were disabled. Guess we won't support the developer today!");
			}
			return;
		}
		
		double version = Double.parseDouble(System.getProperty("java.specification.version"));
		if (version < 1.8) {
			getLogger().warning(
					"Java " + Double.toString(version).replace("1.", "") + " detected. ChatFeelings requires Java 8 or higher to fully function.");
			getLogger().info("TIP: Use version v2.0.1 or below for legacy Java support.");
			return;
		}
		
			Metrics metrics = new Metrics(this);
			metrics.addCustomChart(new Metrics.SimplePie("server_version", () -> {
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

			metrics.addCustomChart(new Metrics.SimplePie("update_notifications", () -> {
				if (getConfig().getBoolean("Other.Updates.Check")) {
					return "Enabled";
				} else {
					return "Disabled";
				}
			}));
	} // End Metrics
	
	public void pop(CommandSender sender) {
		if(!sounds) {
			return;
		}
		
		if (sender instanceof Player) {
			Player p = (Player) sender;
				p.playSound(p.getLocation(), Sound.ENTITY_CHICKEN_EGG, 2.0f, 2.0f);
		}
	}

	public void bass(CommandSender sender) {
		if(!sounds) {
			return;
		}
		
		if (sender instanceof Player) {
			Player p = (Player) sender;
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 2.0f, 1.3f);
		}
	}

	public void levelup(CommandSender sender) {
		if(!sounds) {
			return;
		}
		
		if (sender instanceof Player) {
			Player p = (Player) sender;
				p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2.0f, 2.0f);
		}
	}
	
	public void configChecks() {		
		
		if(getConfig().contains("Version")) {
			int ver = getConfig().getInt("Version");
			
			if(ver != 7) {
				
				if(ver <= 4 || getConfig().contains("Other.Bypass-Version-Block")) {
				getConfig().set("Other.Bypass-Version-Block", null);
				}
				
				if(ver <= 5) {
				getConfig().set("General.Use-Feeling-Permissions", true);
				getConfig().set("General.Multi-Version-Support", false);
				
				getConfig().set("Cooldowns.Ignore-List.Enabled", true);
				getConfig().set("Cooldowns.Ignore-List.Seconds", 10);
				
				getConfig().set("General.No-Violent-Cmds-When-Sleeping", null);
				}
				
				if(ver <= 6) {
					getConfig().set("Other.Player-Files", null);
				}
				
				getConfig().set("Version", 7);
				saveConfig();
				reloadConfig();
			}
		}
		
		if(getConfig().getBoolean("General.Radius.Enabled")) {
		if(getConfig().getInt("General.Radius.Radius-In-Blocks") == 0) {
			getLogger().warning("Feeling radius cannot be 0, disabling the radius.");
			getConfig().set("General.Radius.Radius-In-Blocks", 35);
			getConfig().set("General.Radius.Enabled", false);
			saveConfig();
			reloadConfig();
		}}
		
		if(usingpuuids) {
			if(!getConfig().getBoolean("Other.Hook-With-PUUIDs")) {
				usingpuuids = false;
			}
		} else {			
			usingpuuids = false;
		}
	}

	public void statsAdd(String uuid, String emotion) {
		if(!usingpuuids) {
			return;
		}
		
		int ftotal = PUUIDS.getInt(this, uuid, "Stats.Sent." + emotion);
		int total = PUUIDS.getInt(this, uuid, "Stats.Sent.Total");
		PUUIDS.set(this, uuid, "Stats.Sent." + emotion, ftotal+1);
		PUUIDS.set(this, uuid, "Stats.Sent.Total", total+1);
	}
	
	public String hasPlayedNameGetUUID(String inputsearch) {
		if(usingpuuids) {
			return PUUIDS.getUUID(inputsearch, false);
		}
		return "0";
	}

	public boolean isTargetIgnoringSender(Player target, Player sender) {
		if(!usingpuuids) {
			return false;
		}
		
		List<String> ignoredplayers = new ArrayList<String>();
		ignoredplayers.clear();
		ignoredplayers.addAll(PUUIDS.getStringList(this, target.getUniqueId().toString(), "Ignoring"));
		
		if(ignoredplayers.contains(sender.getUniqueId().toString())) {
			ignoredplayers.clear();
			return true;
		}
		
		ignoredplayers.clear();
		return false;
	}

	public String hasPlayedUUIDGetName(String inputsearch) {
		if(usingpuuids) {
			return PUUIDS.getName(inputsearch);
		}
		return "0";
	}

	@Override
	public void onEnable() {
		long start = System.currentTimeMillis();
		
		String version = Bukkit.getBukkitVersion().replace("-SNAPSHOT", "");
		
		if (!version.contains("1.14") && !version.contains("1.13")) {
				getLogger().info("---------------------------------------------------");
				getLogger().info("This version of ChatFeelings is only compatible with: 1.14 & 1.13");
				getLogger().info("While ChatFeelings may work with " + version + ", it is not supported.");
				getLogger().info(" ");
				getLogger().info("If you continue, you understand that you will get no support, and");
				getLogger().info("that some features, such as sounds, may disable to continue working.");
				getLogger().info("");
				getLogger().warning("[!] IF YOU GET BUGS/ERRORS, DO NOT REPORT THEM.");
				getLogger().info("---------------------------------------------------");
		}
		
		if(version.contains("1.8") || version.contains("1.7") || version.contains("1.6") || version.contains("1.5") || version.contains("1.4")) {
			getLogger().warning("1.8 or below may have severe issues with this version of ChatFeelings, please use this version:");
			getLogger().warning("https://www.spigotmc.org/resources/chatfeelings.12987/download?version=208840");
		}
		
		api = new ChatFeelingsAPI();
		
		if (this.getServer().getPluginManager().isPluginEnabled("PUUIDS")
				&& this.getServer().getPluginManager().getPlugin("PUUIDS") != null) {
			if(getConfig().getBoolean("Other.Hook-With-PUUIDs", true)) {
			usingpuuids = true;

			if(PUUIDS.connect(this)) { 
				getLogger().info("Hooking into PUUIDS. Built-in ignoring & muting system has been enabled...");
			} else {
				getLogger().warning("UNABLE TO HOOK INTO PUUIDS. Make sure you aren't using /rl or /restart.");
			}
			Bukkit.getServer().getPluginManager().registerEvents(new PUUIDs(), this);
			PUUIDS.addToAllWithout(this, "Allow-Feelings", true);
			PUUIDS.addToAllWithout(this, "Muted", false);
			}
		} else {
			if(getConfig().getBoolean("Other.Hook-With-PUUIDs", true)) {
				getLogger().info("Disabling built-in stats, muting, & ignoring. Missing Optional Depedency: PUUIDs");
			}
		}
		
		
		getConfig().options().copyDefaults(true);
		saveConfig();
		boolean debug = getConfig().getBoolean("Options.Debug");
		if (!getConfig().getBoolean("Other.Bypass-Version-Block") && (Bukkit.getVersion().contains("1.13") ||  Bukkit.getVersion().contains("1.14"))) {
		getConfig().options().header(
				"Thanks for downloading ChatFeelings!\nMessages for feelings can be found in the Emotes.yml, and other message in the Messages.yml.\n\nHaving trouble? Join our support discord: https://discord.gg/6ugXPfX");
		if(debug) {	
		getLogger().info("[Debug] Setting 'supported' header in the config. Using 1.13+");
		}
		} else {
			if(debug) {
				getLogger().info("[Debug] Setting 'unsupported' header in the config. Using below 1.13.");
			}
			getConfig().options().header(
					"Thanks for downloading ChatFeelings!\nMessages for feelings can be found in the Emotes.yml, and other message in the Messages.yml.\n\nDO NOT REPORT BUGS, YOU ARE USING AN UNSUPPORTED MIENCRAFT VERSION.");	
		}
		saveConfig();

		disabledsendingworlds.clear();
		disabledreceivingworlds.clear();
		disabledsendingworlds.addAll(getConfig().getStringList("General.Disabled-Sending-Worlds"));
		disabledreceivingworlds.addAll(getConfig().getStringList("General.Disabled-Receiving-Worlds"));
		
		Bukkit.getServer().getPluginManager().registerEvents(this, this);

		if (debug) {
			getLogger().info("[Debug] Disabled Sending Worlds: " + disabledsendingworlds.toString());
			getLogger().info("[Debug] Disabled Receiving Worlds: " + disabledreceivingworlds.toString());
		}

		if(isPreRelease) {
				getLogger().info("Using a PRE-RELEASE, skipped update checking & metrics.");
		} else {
			
			addMetrics();
			
		if (getConfig().getBoolean("Other.Updates.Check")) {
				try {
					new Updater(this).checkForUpdate();
				} catch (Exception e) {
					getLogger().warning("There was an issue while trying to check for updates.");
				}
			} else {
			getLogger().info("[!] Update checking has been disabled in the config.yml");
		}}

		FileSetup.enableFiles();
		
		configChecks();
		if ((Bukkit.getVersion().contains("1.13") ||  Bukkit.getVersion().contains("1.14"))) {
		getLogger().info("Having issues? Got a question? Join our support discord: https://discord.gg/6ugXPfX");
		} else {
			if(debug) {
				getLogger().info("[Debug] Not showing support discord link. They are using " + Bukkit.getVersion().toString() + " :(");
			}
		}
		
		if (this.getServer().getPluginManager().isPluginEnabled("LiteBans")
				&& this.getServer().getPluginManager().getPlugin("LiteBans") != null) {
			getLogger().info("Hooking into LiteBans...");
			haslitebans = true;
		}
		
		if (this.getServer().getPluginManager().isPluginEnabled("AdvancedBan")
				&& this.getServer().getPluginManager().getPlugin("AdvancedBan") != null) {
			getLogger().info("Hooking into AdvancedBans...");
			hasadvancedban = true;
		}
		
		if (this.getServer().getPluginManager().isPluginEnabled("Essentials")
				&& this.getServer().getPluginManager().getPlugin("Essentials") != null) {
			hasess = true;
			getLogger().info("Hooking into Essentials...");
		} 
		
		updateConfig();
		
		if(debug) {
			getLogger().info("Finished! ChatFeelings was loaded in " + Long.toString(System.currentTimeMillis()-start) + "ms");
		}
		
		start = 0;
		
	} // [!] End of OnEnable Event

	private int isBanned(UUID uuid, String IPAdd) {
		
		if(isABBanned(uuid)) {
			return 3;
		}
		
		if(isLiteBanBanned(uuid, IPAdd)) {
			return 2;
		}
		
		if(isEssBanned(uuid)) {
			return 1;
		}
		
		if(isVanillaBanned(uuid)) {
			return 4;
		}
		
		return 0;
	}
	
	private int isMuted(UUID uuid, String IPAdd) {
		if(isABMuted(uuid)) {
			return 3;
		}
		
		if(isLiteBanMuted(uuid, IPAdd)) {
			return 2;
		}
		
		if(isEssMuted(uuid)) {
			return 1;
		}
		
		return 0; // 0 in this case means no mute was found.
	}
	
	
	// FOR API ---------------------------------
	
    public boolean APIisMutedUUIDBoolean(String uuid) {
		if(isMuted(UUID.fromString(uuid), null) != 0) {
			return true;
		}
		return false;
	}
	
	public boolean APIisBannedUUIDBoolean(String uuid) {
		if(isBanned(UUID.fromString(uuid), null) != 0) {
			return true;
		}
		return false;
	}
	
	public int APIgetSentStat(String name, String feeling) {
		if(!usingpuuids) {
			return 0;
		}
		
		String uuid = PUUIDS.getUUID(name, false);
		return PUUIDS.getInt(this, uuid, "Stats.Sent." + StringUtils.capitalize(feeling.toLowerCase()));
	}
	
	// END OF API CALLS ------------------------------------
	
	private static boolean isEssMuted(UUID uuid) {
		try {
		if (hasess) {
			Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
			if(ess.getUser(uuid).isMuted()) {
				return true;
			}
		}
		return false;
			} catch(Exception err) {
				Bukkit.getLogger().warning("[ChatFeelings] Error when trying to check if a player was muted. (ES)");
			return false;
		}
	}
	
	private boolean isLiteBanMuted(UUID uuid, String IPAdd) {
		try {
		if (haslitebans) {
			if(Database.get().isPlayerMuted(uuid, IPAdd)) {
				return true;
			}
		}
		return false;
		} catch(Exception err) {
				Bukkit.getLogger().warning("[ChatFeelings] Error when trying to check if a player was muted. (LB)");
			return false;
		}
	}
	
	private boolean isABMuted(UUID uuid) {
		try {
		if (hasadvancedban) {
			if(PunishmentManager.get().isMuted(uuid.toString())) {
				return true;
			}
		}
		return false;
		} catch(Exception err) {
				Bukkit.getLogger().warning("[ChatFeelings] Error when trying to check if a player was muted. (AB)");
			return false;
		}
	}
	
	private boolean isVanillaBanned(UUID uuid) {
		if(Bukkit.getOfflinePlayer(uuid).isBanned()) {
			return true;
		}
		
		return false;
	}
	
	private boolean isEssBanned(UUID uuid) {
		try {
		if (hasess) {
			Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
			if(ess.getUser(uuid).getBase().isBanned()) {
				return true;
			}
		}
		return false;
			} catch(Exception err) {
			getLogger().warning("Error when trying to check if a player was banned. (ES)");
			return false;
		}
	}
	
	private boolean isLiteBanBanned(UUID uuid, String IPAdd) {
		try {
		if (haslitebans) {
			if(Database.get().isPlayerBanned(uuid, IPAdd)) {
				return true;
			}
		}
		return false;
		} catch(Exception err) {
			getLogger().warning("Error when trying to check if a player was banned. (LB)");
			return false;
		}
	}
	
	private boolean isABBanned(UUID uuid) {
		try {
		if (hasadvancedban) {
			if(PunishmentManager.get().isBanned(uuid.toString())) {
				return true;
			}
		}
		return false;
		} catch(Exception err) {
			getLogger().warning("Error when trying to check if a player was Banned. (AB)");
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

				for (MetadataValue meta : player.getMetadata("vanished")) {
					if (meta.asBoolean()) {
						return true;
					}
				}

			} catch (Exception err) {
				getLogger().warning("Couldn't check for vanished players. Disabling this check until next restart.");
				usevanishcheck = false;
			}

			if (getConfig().getBoolean("Other.Vanished-Players.Use-Legacy")) {
				if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
					getLogger().info("Player was legacy vanished.");
					return true;
				}
				return false;
			}

			return false;
		} else { // Vanish Check is Off
			getLogger().info("Vanished Checking is off.");
			return false;
		}
	}
	
	private void getStats(CommandSender p, String name, boolean isown) {
		if(!usingpuuids) {
			return;
		}
		
		File folder = Bukkit.getServer().getPluginManager().getPlugin("ChatFeelings").getDataFolder();
		File msgsfile = new File(folder, File.separator + "messages.yml");
		FileConfiguration msg = YamlConfiguration.loadConfiguration(msgsfile);
		
		String your = "";
		
		if(isown) {
			Msgs.send(p, msg.getString("Stats-Header-Own").replace("%player%", name));
			your = "&7Your ";
		} else {
			Msgs.send(p, msg.getString("Stats-Header-Other").replace("%player%", name));
			your = "&7";
		}
		String uuid = PUUIDS.getUUID(name, false);
		
		Msgs.send(p, "&f   &8&l> " + your + "Hugs: &f&l" + PUUIDS.getInt(this, uuid, "Stats.Sent.Hug"));
		Msgs.send(p, "&f   &8&l> " + your + "Slaps: &f&l" + PUUIDS.getInt(this, uuid, "Stats.Sent.Slap"));
		Msgs.send(p, "&f   &8&l> " + your + "Pokes: &f&l" + PUUIDS.getInt(this, uuid, "Stats.Sent.Poke"));
		Msgs.send(p, "&f   &8&l> " + your + "Highfives: &f&l" + PUUIDS.getInt(this, uuid, "Stats.Sent.Highfive"));
		Msgs.send(p, "&f   &8&l> " + your + "Facepalms: &f&l" + PUUIDS.getInt(this, uuid, "Stats.Sent.Facepalm"));
		Msgs.send(p, "&f   &8&l> " + your + "Yells: &f&l" + PUUIDS.getInt(this, uuid, "Stats.Sent.Yell"));
		Msgs.send(p, "&f   &8&l> " + your + "Bites: &f&l" + PUUIDS.getInt(this, uuid, "Stats.Sent.Bite"));
		Msgs.send(p, "&f   &8&l> " + your + "Snuggles: &f&l" + PUUIDS.getInt(this, uuid, "Stats.Sent.Snuggle"));
		Msgs.send(p, "&f   &8&l> " + your + "Shakes: &f&l" + PUUIDS.getInt(this, uuid, "Stats.Sent.Shake"));
		Msgs.send(p, "&f   &8&l> " + your + "Stabs: &f&l" + PUUIDS.getInt(this, uuid, "Stats.Sent.Stab"));
		Msgs.send(p, "&f   &8&l> " + your + "Kisses: &f&l" + PUUIDS.getInt(this, uuid, "Stats.Sent.Kiss"));
		Msgs.send(p, "&f   &8&l> " + your + "Punches: &f&l" + PUUIDS.getInt(this, uuid, "Stats.Sent.Punch"));
		Msgs.send(p, "&f   &8&l> " + your + "Murders: &f&l" + PUUIDS.getInt(this, uuid, "Stats.Sent.Murder"));
		Msgs.send(p, "&f   &8&l> " + your + "Boi: &f&l" + PUUIDS.getInt(this, uuid, "Stats.Sent.Boi"));
		Msgs.send(p, "&f   &8&l> " + your + "Cries: &f&l" + PUUIDS.getInt(this, uuid, "Stats.Sent.Cry"));
		Msgs.send(p, "&f   &8&l> " + your + "Dabs: &f&l" + PUUIDS.getInt(this, uuid, "Stats.Sent.Dab"));
		Msgs.send(p, "&f   &8&l> " + your + "Licks: &f&l" + PUUIDS.getInt(this, uuid, "Stats.Sent.Lick"));
		Msgs.send(p, "&f   &8&l> " + your + "Scorn: &f&l" + PUUIDS.getInt(this, uuid, "Stats.Sent.Scorn"));
		Msgs.send(p, "&f   &8&l> " + your + "Pats: &f&l" + PUUIDS.getInt(this, uuid, "Stats.Sent.Pat"));
		Msgs.send(p, "&f   &8&l> " + your + "Stalks: &f&l" + PUUIDS.getInt(this, uuid, "Stats.Sent.Stalk"));
		Msgs.send(p, "&f   &8&l> &eTotal Sent: &f&l" + PUUIDS.getInt(this, uuid, "Stats.Sent.Total"));
	}

	private void noPermission(CommandSender sender) {
		File folder = Bukkit.getServer().getPluginManager().getPlugin("ChatFeelings").getDataFolder();
		File msgsfile = new File(folder, File.separator + "messages.yml");
		FileConfiguration msg = YamlConfiguration.loadConfiguration(msgsfile);
		Msgs.sendPrefix(sender, msg.getString("No-Permission"));
		bass(sender);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		File folder = Bukkit.getServer().getPluginManager().getPlugin("ChatFeelings").getDataFolder();
		File msgsfile = new File(folder, File.separator + "messages.yml");
		FileConfiguration msg = YamlConfiguration.loadConfiguration(msgsfile);

		File emotesfile = new File(folder, File.separator + "emotes.yml");
		FileConfiguration emotes = YamlConfiguration.loadConfiguration(emotesfile);

		if (cmd.getName().equalsIgnoreCase("chatfeelings") && args.length == 0) {
			Msgs.send(sender, "");
			Msgs.send(sender, "&a&lC&r&ahat &f&lF&r&feelings");
			Msgs.send(sender, "&8&l> &7/cf help &7&ofor commands & settings.");
			Msgs.send(sender, "");
			pop(sender);
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("chatfeelings") && args.length >= 1 && args[0].equalsIgnoreCase("version")) {
			Msgs.send(sender, "");
			Msgs.send(sender, "&a&lC&r&ahat &f&lF&r&feelings");
			Msgs.send(sender, "&8&l> &7You are currently running &f&lv" + getDescription().getVersion());
			Msgs.send(sender, "");
			pop(sender);
			return true;
		}
		
		if (cmd.getName().equalsIgnoreCase("chatfeelings") && args.length >= 1 && args[0].equalsIgnoreCase("stats")) {
			if(!sender.hasPermission("chatfeelings.stats") && !sender.hasPermission("chatfeelings.stats.others") && !sender.isOp()) {
				noPermission(sender);
				return true;
			}
			
			if(!usingpuuids) {
				Msgs.sendPrefix(sender, msg.getString("Feature-Disabled"));
				bass(sender);
				return true;
			}
			
			if (args.length == 1) {
				if(!(sender instanceof Player)) {
					Msgs.sendPrefix(sender, msg.getString("No-Player"));
					return true;
				}
				
				getStats(sender, sender.getName(), true);
				pop(sender);
				return true;
			}
			
			if(!sender.hasPermission("chatfeelings.stats.others") && !sender.isOp()) {
				noPermission(sender);
				return true;
			}
			
			String getUUID = hasPlayedNameGetUUID(args[1]);
			if (getUUID == "0" || getUUID == null) {
				
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
			getStats(sender, getName, false);
			pop(sender);
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("chatfeelings") && args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
			if (!sender.hasPermission("chatfeelings.admin") && !sender.isOp()) {
				noPermission(sender);
				return true;
			}
			
			long secsLeft = ((lastreload / 1000) + 30) - (System.currentTimeMillis() / 1000);
			if(secsLeft > 0) {
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
				if(debug) {
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
						
			if(debug) {
				getLogger().info("[Debug] Sending Feelings is disabled in: " + disabledsendingworlds.toString());
				getLogger().info("[Debug] Receiving Feelings is disabled in: " + disabledreceivingworlds.toString());
			}
			
			try {
				long reloadtime = System.currentTimeMillis()-starttime;
				if(reloadtime >= 1000) {
					double reloadsec = reloadtime/1000;
					// Lets hope nobody's reload takes more than 1000ms (1s). However it's not unheard of .-.
					Msgs.send(sender, msg.getString("Reload").replace("%time%", Double.toString(reloadsec) + "s"));
					if(sender instanceof Player) {
						getLogger().info("Configuration & Files reloaded by " + sender.getName() + " in " + reloadsec + "s");
					}
				} else {
				    Msgs.send(sender, msg.getString("Reload").replace("%time%", Long.toString(reloadtime) + "ms"));
				    if(sender instanceof Player) {
				    	getLogger().info("Configuration & Files reloaded by " + sender.getName() + " in " + reloadtime + "ms");
				    }
				}
			} catch (Exception err) {
				Msgs.send(sender, "&8&l> &a&l✓  &7Configuration Reloaded. &c(1 file was regenerated)");
			}
			Msgs.send(sender, "");
			levelup(sender);
			
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("chatfeelings") && args.length >= 1 && args[0].equalsIgnoreCase("help")) {
			Msgs.send(sender, "");
			Msgs.send(sender, "&a&lC&r&ahat &f&lF&r&feelings");
			Msgs.send(sender, "&8&l> &e&l/cf help &7Shows you this page.");
			
			if(usingpuuids) {
			if (sender.hasPermission("chatfeelings.ignore") || sender.isOp()) {
				Msgs.send(sender, "&8&l> &e&l/cf ignore (player) &7Ignore/Unignore feelings from players.");
				Msgs.send(sender, "&8&l> &e&l/cf ignore all &7Toggles everyone being able to use feelings.");
			}
			if(sender.hasPermission("chatfeelings.stats") || sender.isOp()) {
				if(!sender.hasPermission("chatfeelings.stats.others") && !sender.isOp()) {
					Msgs.send(sender, "&8&l> &e&l/cf stats &7Shows your feeling statistics.");
				} else {
					Msgs.send(sender, "&8&l> &e&l/cf stats (player) &7Shows your a players statistics.");
				}
			}
			if (sender.hasPermission("chatfeelings.mute") || sender.isOp()) {
				Msgs.send(sender, "&8&l> &e&l/cf mute (player) &7Prevents a player from using feelings.");
				Msgs.send(sender, "&8&l> &e&l/cf unmute (player) &7Unmutes a muted player.");
				Msgs.send(sender, "&8&l> &e&l/cf mutelist &7Shows who's currently muted.");
			}}
			
			if (sender.hasPermission("chatfeelings.admin") || sender.isOp()) {
				Msgs.send(sender, "&8&l> &e&l/cf version &7Shows you the plugin version.");
				Msgs.send(sender, "&8&l> &e&l/cf reload &7Reloads the plugin.");
			}
			Msgs.send(sender, "&8&l> &6&l/feelings &7Shows a list of available feelings.");
			Msgs.send(sender, "");
			pop(sender);
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("chatfeelings") && args.length >=1 && args[0].equalsIgnoreCase("uuid")) {
			if(!sender.hasPermission("chatfeelings.admin") && !sender.isOp()) {
				noPermission(sender);
				return true;
			}
			
			if (args.length == 1) {
				Msgs.sendPrefix(sender, msg.getString("No-Player"));
				bass(sender);
				return true;
			}
			
			String getUUID = hasPlayedNameGetUUID(args[1]);
			if (getUUID == "0" || getUUID == null) {
				
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
		
		if (cmd.getName().equalsIgnoreCase("chatfeelings") && args.length >= 1 && args[0].equalsIgnoreCase("mutelist")) {
			if(!sender.hasPermission("chatfeelings.mute") && !sender.isOp()) {
				noPermission(sender);
				return true;
			}
			
			if(!usingpuuids) {
				Msgs.sendPrefix(sender, msg.getString("Feature-Disabled"));
				bass(sender);
				return true;
			}
			
			Msgs.send(sender, "");
			Msgs.send(sender, msg.getString("Mute-List-Header"));
			
			int totalmuted = 0;
			for (String name : PUUIDS.getAllWithBoolean(this, "Muted", true)) {
					String uuid = PUUIDS.getUUID(name, false);
					String IPAdd = PUUIDS.getIP(uuid);
					UUID puuid = UUID.fromString(uuid);
					
					int muteInt = isMuted(puuid, IPAdd);
						totalmuted++;
						
					if(muteInt == 3){
						Msgs.send(sender, msg.getString("Mute-List-Player").replace("%player%", name) + " &c(AdvancedBan & CF)");	
					} else if(muteInt == 2) {
						Msgs.send(sender, msg.getString("Mute-List-Player").replace("%player%", name) + " &c(LiteBans & CF)");	
					} else if(muteInt == 1) {
						Msgs.send(sender, msg.getString("Mute-List-Player").replace("%player%", name) + " &c(Essentials & CF)");	
					} else {
					    Msgs.send(sender, msg.getString("Mute-List-Player").replace("%player%", name));
					}
			}
				
				if(totalmuted == 1) {
				Msgs.send(sender, msg.getString("Mute-List-Total-One").replace("%total%", "1"));
				} else if(totalmuted == 0) {
				Msgs.send(sender, msg.getString("Mute-List-Total-Zero").replace("%total%", "0"));
				} else {
			    Msgs.send(sender, msg.getString("Mute-List-Total-Many").replace("%total%", Integer.toString(totalmuted)));	
				}
				Msgs.send(sender, "");
				pop(sender);
				return true;
		}
		
		if (cmd.getName().equalsIgnoreCase("chatfeelings") && args.length >= 1 && args[0].equalsIgnoreCase("unmute")) {
			if (!sender.hasPermission("chatfeelings.mute") && !sender.isOp()) {
				noPermission(sender);
				if(getConfig().contains("General.Extra-Help") && msg.contains("No-Perm-Mute-Suggestion")) {
				if(getConfig().getBoolean("General.Extra-Help")) {
				Msgs.sendPrefix(sender, msg.getString("No-Perm-Mute-Suggestion"));
				}}
				return true;
			}

			if(!usingpuuids) {
				Msgs.sendPrefix(sender, msg.getString("Feature-Disabled"));
				bass(sender);
				return true;
			}
			
			if (args.length == 1) {
				Msgs.sendPrefix(sender, msg.getString("No-Player-Unmute"));
				bass(sender);
				return true;
			}

			String muteUUID = hasPlayedNameGetUUID(args[1]);
			
			if (muteUUID == "0" || muteUUID == null) {
				bass(sender);
				Msgs.sendPrefix(sender, msg.getString("Player-Never-Joined").replace("%player%", args[1]));
				return true;
			}
			
			String IPAdd = PUUIDS.getIP(muteUUID);
			UUID puuid = UUID.fromString(muteUUID);
			
			int muteInt = isMuted(puuid, IPAdd);
			
			if(PUUIDS.getBoolean(this, muteUUID, "Muted")) {
				Msgs.sendPrefix(sender, msg.getString("Player-Has-Been-Unmuted").replace("%player%", args[1]));
				pop(sender);
				PUUIDS.set(this, muteUUID, "Muted", false);
			} else {
				bass(sender);
		    if(muteInt == 3) {
			   Msgs.sendPrefix(sender, msg.getString("Player-Muted-Via-AdvancedBan"));	
			} else
			if(muteInt == 2) {
			    Msgs.sendPrefix(sender, msg.getString("Player-Muted-Via-LiteBans"));	
			} else 
			if(muteInt == 1) {
				Msgs.sendPrefix(sender, msg.getString("Player-Muted-Via-Essentials"));		
			} else {
				Msgs.sendPrefix(sender, msg.getString("Player-Already-Unmuted"));	
			}}
			return true;
		}
		
		if (cmd.getName().equalsIgnoreCase("chatfeelings") && args.length >= 1 && args[0].equalsIgnoreCase("mute")) {
			if (!sender.hasPermission("chatfeelings.mute") && !sender.isOp()) {
				noPermission(sender);
				if(getConfig().contains("General.Extra-Help") && msg.contains("No-Perm-Mute-Suggestion")) {
				if(getConfig().getBoolean("General.Extra-Help")) {
				Msgs.sendPrefix(sender, msg.getString("No-Perm-Mute-Suggestion"));
				}}
				return true;
			}

			if(!usingpuuids) {
				Msgs.sendPrefix(sender, msg.getString("Feature-Disabled"));
				bass(sender);
				return true;
			}
			
			if (args.length == 1) {
				Msgs.sendPrefix(sender, msg.getString("No-Player-Mute"));
				bass(sender);
				return true;
			}

			String muteUUID = hasPlayedNameGetUUID(args[1]);
			
			if (muteUUID == "0" || muteUUID == null) {
				bass(sender);
				Msgs.sendPrefix(sender, msg.getString("Player-Never-Joined").replace("%player%", args[1]));
				return true;
			}
			
			String IPAdd = PUUIDS.getIP(muteUUID);
			UUID puuid = UUID.fromString(muteUUID);
			
			int muteInt = isMuted(puuid, IPAdd);
			
				if (args[1].equalsIgnoreCase(sender.getName())) {
					bass(sender);
					Msgs.sendPrefix(sender, msg.getString("Cant-Mute-Self"));
					return true;
				}
				
				if(!PUUIDS.getBoolean(this, muteUUID, "Muted")) {
				Msgs.sendPrefix(sender, msg.getString("Player-Has-Been-Muted").replace("%player%", args[1]));
				if(muteInt != 0) {
					Msgs.sendPrefix(sender, msg.getString("Extra-Mute-Present").replace("%player%", args[1]));	
				}
				pop(sender);
				PUUIDS.set(this, muteUUID, "Muted", true);
				
				} else {
					bass(sender);
					Msgs.sendPrefix(sender, msg.getString("Player-Already-Muted"));	
				if(getConfig().contains("General.Extra-Help") && msg.contains("Already-Mute-Unmute-Suggestion")) {
				if(getConfig().getBoolean("General.Extra-Help")) {
					Msgs.sendPrefix(sender, msg.getString("Already-Mute-Unmute-Suggestion"));
				}}}
			
			return true;
		}
		
		if (cmd.getName().equalsIgnoreCase("chatfeelings") && args.length >= 1 && args[0].equalsIgnoreCase("ignore")) {
			if (!sender.hasPermission("chatfeelings.ignore") && !sender.isOp()) {
				noPermission(sender);
				return true;
			}
			
			if(!usingpuuids) {
				Msgs.sendPrefix(sender, msg.getString("Feature-Disabled"));
				bass(sender);
				return true;
			}
			
			if (!(sender instanceof Player)) {
				Msgs.sendPrefix(sender, "&c&lSorry. &fOnly players can ignore other players.");
				return true;
			}

			Player p = (Player) sender;
			String uuid = p.getUniqueId().toString();
			
			if (args.length == 1) {
				if(Cooldowns.ignorelistcooldown.containsKey(p)) {
					Msgs.sendPrefix(sender, msg.getString("Ignore-List-Cooldown"));
					bass(sender);
					return true;
				}

				Cooldowns.ignoreListCooldown(p);

				List<String> ignoredplayers = PUUIDS.getStringList(this, uuid, "Ignoring");
				Msgs.send(sender, " ");
				Msgs.send(sender, msg.getString("Ignore-List-Header"));
				if(ignoredplayers.size() == 0) {
					Msgs.send(sender, msg.getString("Ignore-List-None"));
				} else {
					for (String ignoredUUID : ignoredplayers) {
						String name = hasPlayedUUIDGetName(ignoredUUID);
						if(name != null && name != "0") {
								Msgs.send(sender, "  &8&l> &f&l" + name);	
						}
					}}
				
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

			if (args[1].equalsIgnoreCase("all")) {
				if (PUUIDS.getBoolean(this, uuid, "Allow-Feelings")) {
					PUUIDS.set(this, uuid, "Allow-Feelings", false);
					Msgs.sendPrefix(sender, msg.getString("Ingoring-On-All"));
				} else {
					PUUIDS.set(this, uuid, "Allow-Feelings", true);
					Msgs.sendPrefix(sender, msg.getString("Ingoring-Off-All"));
				}

				pop(sender);
				return true;
			}

			List<String> ignoredplayers = new ArrayList<String>();
			ignoredplayers.clear();
			ignoredplayers.addAll(PUUIDS.getStringList(this, uuid, "Ignoring"));

			String ignoreUUID = hasPlayedNameGetUUID(args[1]);
			if (ignoreUUID == "0" || ignoreUUID == null) {
				
				if (args[1].equalsIgnoreCase("console")) {
					Msgs.sendPrefix(sender, msg.getString("Console-Not-Player"));
					bass(sender);
					return true;
				}
				
				bass(sender);
				Msgs.sendPrefix(sender, msg.getString("Player-Never-Joined").replace("%player%", args[1]));
				return true;
			}

			try {
				if (ignoredplayers.contains(ignoreUUID)) {
					Msgs.sendPrefix(sender, msg.getString("Ingoring-Off-Player").replace("%player%", args[1]));

					ignoredplayers.remove(ignoreUUID);
					PUUIDS.set(this, uuid, "Ignoring", ignoredplayers);

					pop(sender);
					ignoredplayers.clear();
					return true;
				}
			} catch (Exception searcherr) {
				getLogger().warning("Error trying to search for: " + args[1] + " in the Data folder.");
			}

			ignoredplayers.add(ignoreUUID);
			PUUIDS.set(this, uuid, "Ignoring", ignoredplayers);
			
			Msgs.sendPrefix(sender, msg.getString("Ingoring-On-Player").replace("%player%", args[1]));
			pop(sender);
			ignoredplayers.clear();
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("feelings")) {
			if ((args.length == 0)
					|| (args.length >= 1 && (args[0].equalsIgnoreCase("1") || args[0].equalsIgnoreCase("0")))) {
				Msgs.send(sender, "");
				Msgs.send(sender, msg.getString("Feelings-Help") + "                        "
						+ msg.getString("Feelings-Help-Page").replace("%page%", "1").replace("%pagemax%", "2"));
				Msgs.send(sender, "&8&l> &f&l/hug (player) &7Give someone a nice warm hug!");
				Msgs.send(sender, "&8&l> &f&l/slap (player) &7Slap some sense back into someone.");
				Msgs.send(sender, "&8&l> &f&l/poke (player) &7Poke someone to get their attention.");
				Msgs.send(sender, "&8&l> &f&l/highfive (player) &7Show your suopport, and give a highfive!");
				Msgs.send(sender, "&8&l> &f&l/facepalm (player) &7Need to show some disapproval?");
				Msgs.send(sender, "&8&l> &f&l/yell (player) &7Yell at someone as loud as possible!");
				Msgs.send(sender, "&8&l> &f&l/bite (player) &7Bite a player right on the arm.");
				Msgs.send(sender, "&8&l> &f&l/snuggle (player) &7Snuggle up with the power of warm hugs!");
				Msgs.send(sender, "&8&l> &f&l/shake (player) &7Shake a player to their feet.");
				Msgs.send(sender, "&8&l> &f&l/stab (player) &7Stab someone with a knife. Ouch!");
				Msgs.send(sender, "&7To go to the 2nd page do &a/feelings 2");
				pop(sender);
				Msgs.send(sender, "");
			} else if (args.length >= 1 && args[0].equalsIgnoreCase("2")) {
				Msgs.send(sender, "");
				Msgs.send(sender, msg.getString("Feelings-Help") + "                        "
						+ msg.getString("Feelings-Help-Page").replace("%page%", "2").replace("%pagemax%", "2"));
				Msgs.send(sender, "&8&l> &f&l/kiss (player) &7Make sweet sweet love. uwu");
				Msgs.send(sender, "&8&l> &f&l/punch (player) &7Punch the lights out of someone!");
				Msgs.send(sender, "&8&l> &f&l/murder (player) &7Finna kill someone here.");
				Msgs.send(sender, "&8&l> &f&l/boi (player) &7Living in 2016? Boi at a player.");
				Msgs.send(sender, "&8&l> &f&l/cry (player) &7Real sad hours? Cry at someone.");
				Msgs.send(sender, "&8&l> &f&l/dab (player) &7Freshly dab on someone.");
				Msgs.send(sender, "&8&l> &f&l/lick (player) &7Lick someone like an ice-cream sundae!");
				Msgs.send(sender, "&8&l> &f&l/pat (player) &7Pat a players head for being good.");
				Msgs.send(sender, "&8&l> &f&l/stalk (player) &7Stalk a player carefully... carefully.");
				
		 	    Date now = new Date();
			    SimpleDateFormat format = new SimpleDateFormat("MM");
			    
			 	if(format.format(now).equals("10") || format.format(now).equals("09")) {
			 		Msgs.send(sender, "&8&l> &6&l/spook (player) &7Give your friends some festive fright!");
			 	} else {
			 		Msgs.send(sender, "&8&l> &7&l/spook &7This command is exclusive to October only.");
			 	}
			 	
				pop(sender);
				Msgs.send(sender, "");
			} else {
				bass(sender);
				Msgs.sendPrefix(sender, msg.getString("Page-Not-Found"));
			}
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("hug") || cmd.getName().equalsIgnoreCase("poke")
				|| cmd.getName().equalsIgnoreCase("slap") || cmd.getName().equalsIgnoreCase("highfive")
				|| cmd.getName().equalsIgnoreCase("facepalm") || cmd.getName().equalsIgnoreCase("yell")
				|| cmd.getName().equalsIgnoreCase("bite") || cmd.getName().equalsIgnoreCase("snuggle")
				|| cmd.getName().equalsIgnoreCase("shake") || cmd.getName().equalsIgnoreCase("stab")
				|| cmd.getName().equalsIgnoreCase("stab") || cmd.getName().equalsIgnoreCase("kiss")
				|| cmd.getName().equalsIgnoreCase("punch") || cmd.getName().equalsIgnoreCase("murder")
				|| cmd.getName().equalsIgnoreCase("boi") || cmd.getName().equalsIgnoreCase("cry")
				|| cmd.getName().equalsIgnoreCase("dab") || cmd.getName().equalsIgnoreCase("lick")
				|| cmd.getName().equalsIgnoreCase("scorn") || cmd.getName().equalsIgnoreCase("pat")
				|| cmd.getName().equalsIgnoreCase("stalk") || cmd.getName().equalsIgnoreCase("spook")) {

			if(sender instanceof Player && useperms) {
			if(!sender.hasPermission("chatfeelings." + cmd.getName()) && !sender.hasPermission("chatfeelings.all") && !sender.isOp()) {
				noPermission(sender);
				return true;
			}}
			
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
							return true;
						}
					}
				}
			}

			if (args.length == 0) {
				Msgs.sendPrefix(sender, msg.getString("No-Player"));
				bass(sender);
				return true;
			}

			String cmdconfig = (StringUtils.capitalize(cmd.getName().toString()));

			if (sender instanceof Player) {
				Player p = (Player) sender;
				if (disabledsendingworlds.contains(p.getWorld().getName())) {
					bass(sender);
					Msgs.sendPrefix(sender, msg.getString("Sending-World-Disabled"));
					return true;
				}
			}

			if (!emotes.getBoolean("Feelings." + cmdconfig + ".Enable")) {
				bass(sender);
				Msgs.sendPrefix(sender, msg.getString("Emote-Disabled"));
				return true;
			}

			if (args.length < 1) {
				Msgs.sendPrefix(sender, msg.getString("No-Player"));
				bass(sender);
				return true;
			}

			if (args[0].equalsIgnoreCase("console")) {
				Msgs.sendPrefix(sender, msg.getString("Console-Not-Player"));
				bass(sender);
				return true;
			}

			Player target = Bukkit.getServer().getPlayer(args[0]);
			
			if (sender instanceof Player) {
				if (target == sender) {
					if (getConfig().getBoolean("General.Prevent-Self-Feelings")) {
						bass(sender);
						Msgs.sendPrefix(sender, msg.getString("Sender-Is-Target").replace("%command%", cmdconfig));
						return true;
					}
				}
			}
			
			if (target == null || isVanished(target)) {
				bass(sender);
				Msgs.sendPrefix(sender, msg.getString("Player-Offline").replace("%player%", args[0].toString()));
				return true;
			}

			if (disabledreceivingworlds.contains(target.getWorld().getName())) {
				bass(sender);
				Msgs.sendPrefix(sender, msg.getString("Receiving-World-Disabled"));
				return true;
			}
			
			// Radius & Sleeping Check ---------------------------
			if(sender instanceof Player) {
				Player p = (Player)sender;
			if(getConfig().getBoolean("General.Radius.Enabled")) {
				
				Double distance = p.getLocation().distance(target.getLocation());
				Double radius = getConfig().getDouble("General.Radius.Radius-In-Blocks");	
			if(distance > radius) {
				if(debug) {
					getLogger().info(sender.getName() + " was outside the radius of " + radius + ". (They're " + distance + ")");
				}
				Msgs.sendPrefix(sender, msg.getString("Outside-Of-Radius").replace("%player%", target.getName()).replace("%command%", cmd.getName().toString()));
				bass(sender);
				return true;
			}}
		}
			
			
			// Ignoring & Mute Check ----------------
			if (sender instanceof Player) {
				Player p = (Player) sender;

				int muteInt = isMuted(p.getUniqueId(), null);
				
				if(muteInt != 0) {
					if(debug) {
						if(muteInt == 3) {
						getLogger().info("[Debug] " + sender.getName() + " tried to use /" + cmdLabel + ", but is muted by AdvancedBan.");
					}
						if(muteInt == 2) {
						getLogger().info("[Debug] " + sender.getName() + " tried to use /" + cmdLabel + ", but is muted by LiteBans.");
					}
						if(muteInt == 1) {
						getLogger().info("[Debug] " + sender.getName() + " tried to use /" + cmdLabel + ", but is muted by Essentials.");
					}
					}
					bass(sender);
					Msgs.sendPrefix(sender, msg.getString("Is-Muted"));
					return true;
				}
				
				if(usingpuuids) {
					if(PUUIDS.getBoolean(this, p.getUniqueId().toString(), "Muted")) {
						if(debug) {
							getLogger().info("[Debug] " + sender.getName() + " tried to use /" + cmdLabel + ", but was muted (via CF).");
						}
						bass(sender);
						Msgs.sendPrefix(sender, msg.getString("Is-Muted"));
						return true;
					}
					
					if(!PUUIDS.getBoolean(this, target.getUniqueId().toString(), "Allow-Feelings")) {
						bass(sender);
						Msgs.sendPrefix(sender, msg.getString("Target-Is-Ignoring-All"));
						if(debug) {
							getLogger().info("[Debug] " + sender.getName() + " couldn't send feeling to " + target.getName() + " because they are ignoring ALL.");
						}
						return true;
					}

					if (isTargetIgnoringSender(target, p)) {
						bass(sender);
						Msgs.sendPrefix(sender,
								msg.getString("Target-Is-Ignoring").replace("%player%", target.getName()));
							if(debug) {
								getLogger().info("[Debug] Not sending feeling to " + target.getName() + " because they are ignoring " + p.getName());
							}
						return true;
					}
			} else {
				if(debug) {
					getLogger().info("[Debug] Not using PUUIDs user system, skipping allow-feelings, built-in mute and built-in ignoring checks.");
				}
			}} else if(usingpuuids){
				if (PUUIDS.getBoolean(this, target.getUniqueId().toString(), "Allow-Feelings")) {
					Msgs.sendPrefix(sender, msg.getString("Target-Is-Ignoring-All"));
					if(debug) {
						getLogger().info("[Debug] Blocking CONSOLE from sending feeling because " + target.getName() + " is blocking ALL.");
					}
					return true;
				} // Sender is Console however the player is still blocking ALL feelings.
			}
			// ------------------------------------------------
			
			// FEELING HANDLING IS ALL BELOW -------------------------------------------------------------------------------
			
			if(cmd.getName().equalsIgnoreCase("spook")) {
		 	    Date now = new Date();
			    SimpleDateFormat format = new SimpleDateFormat("MM");
			    
			 	if(!format.format(now).equals("10")) {
			 		Msgs.sendPrefix(sender, "&c&lSorry. &fSpook is an emote exclusive to &7&lOctober");
			 		bass(sender);
			 		return true;
			 	}
			 	
			 	if(Cooldowns.spook.containsKey(target.getName())) {
			 		Msgs.sendPrefix(sender, "&e&l&oToo Spooky! &fThis player is already being spooked.");
			 		bass(sender);
			 		return true;
			 	}
			 	
			 	if(!(target.getInventory().getHelmet() == (new ItemStack(Material.AIR)) || (target.getInventory().getHelmet() == null))) {
			 		Msgs.sendPrefix(sender, "&cSorry. &7" + target.getName() + "&f has a helmet on, and cannot be spooked.");
			 		bass(sender);
			 		return true;
			 	}
			 	
			 	Cooldowns.spookHash(target);
			}
			
			// API Events ----------------------------
			FeelingSendEvent fse = new FeelingSendEvent(sender, target, cmdconfig);
			Bukkit.getPluginManager().callEvent(fse);
			if (fse.isCancelled()) {
				return true;
			}
			
			FeelingRecieveEvent fre = new FeelingRecieveEvent(target, sender, cmdconfig);
			Bukkit.getPluginManager().callEvent(fre);
			if (fre.isCancelled()) {
				return true;
			}
			
			// End of API events (Except for Global event below ---------------------
			
			// Global Handler for PLAYER messages & Feelings ----------------------------
			if (getConfig().getBoolean("General.Global-Feelings.Enabled")) {
				
				for (final Player online : Bukkit.getServer().getOnlinePlayers()) {
					
					// Global Ignoring Checks -----------------
					if(usingpuuids && PUUIDS.getBoolean(this, online.getUniqueId().toString(), "Allow-Feelings") && (online.getName() != sender.getName())) {
						if(debug) {
							getLogger().info("[Debug] " + online.getName() + " is blocking all feelings. Skipping Global Msg!");
						}
					} else {
				// End of Global ignoring Checks -------------------
						
					if (sender.getName().equalsIgnoreCase("console") || !(sender instanceof Player)) {
						// ONLY for CONSOLE Global notify here.
						Msgs.send(online.getPlayer(),
								emotes.getString("Feelings." + cmdconfig + ".Msgs.Global")
										.replace("%sender%", msg.getString("Console-Name"))
										.replace("%target%", target.getName()));
					} else {
						// Global for PLAYER below
						if(sender instanceof Player) {
							Player p = (Player)sender;
							if(PUUIDS.getStringList(this, online.getUniqueId().toString(), "Ignoring").contains(p.getUniqueId().toString())) {
					
								FeelingGlobalNotifyEvent fgne = new FeelingGlobalNotifyEvent(online, sender, target, cmdconfig);
								Bukkit.getPluginManager().callEvent(fgne);
								if (!fgne.isCancelled()) {
									Msgs.send(online.getPlayer(), emotes.getString("Feelings." + cmdconfig + ".Msgs.Global")
											.replace("%sender%", sender.getName()).replace("%target%", target.getName()));
								}
						
							} // end of check to make sure message is sent to those NOT ignoring the player
						}// end of if player confirmation (just a safeguard)
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
				if (sender.getName().equalsIgnoreCase("console")) {
					Msgs.send(target.getPlayer(), emotes.getString("Feelings." + cmdconfig + ".Msgs.Target")
							.replace("%player%", msg.getString("Console-Name")));
				} else {
					Msgs.send(target.getPlayer(), emotes.getString("Feelings." + cmdconfig + ".Msgs.Target")
							.replace("%player%", sender.getName().toString()));
				}

				Msgs.send(sender, emotes.getString("Feelings." + cmdconfig + ".Msgs.Sender").replace("%player%",
						target.getName().toString())); // sender (not global)
			} // end of global else

			// Special Effect Command Handlers -----------------------------
			if (getConfig().getBoolean("General.Violent-Command-Harm")) {
				if (cmd.getName().equalsIgnoreCase("slap") || cmd.getName().equalsIgnoreCase("bite")
						|| cmd.getName().equalsIgnoreCase("shake") || cmd.getName().equalsIgnoreCase("stab")
						|| cmd.getName().equalsIgnoreCase("punch") || cmd.getName().equalsIgnoreCase("murder")) {
					try {
						if(!target.isSleeping()) {
						  target.damage(0.01D);
						  if(debug) {
							  getLogger().info("Skipped damage to " + target.getName() + ", as they were sleeping.");
						  }
						}
					} catch (Exception err) {
						getLogger().warning("Unable to damage player: " + target.getName());
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
					Particles.show(target, cmd.getName().toLowerCase());
				} catch (Exception parterr) {
					getLogger().warning("Couldn't display '" + cmd.getName().toUpperCase() + "' particles to " + target.getName() + ". Make sure you use 1.12 or higher.");
				}
			}
			// -----------------------------------------------------

			// Sound Handler ----------------------------------------
			if(sounds) {
			try {
				String sound1 = emotes.getString("Feelings." + cmdconfig + ".Sounds.Sound1.Name");
				if (!sound1.equalsIgnoreCase("none")
						&& !sound1.equalsIgnoreCase("off")
						&& sound1 != null
						&& sound1 != "null") {

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
				if (!sound2.equalsIgnoreCase("none")
						&& !sound2.equalsIgnoreCase("off")
						&& sound2 != null
						&& sound2 != "null") {
					
					if(sound2.contains("DISC") && multiversion) {
						// Check for SPOOK, that runs an ALT sound to prevent needing to stop it. (For Multi Version support)
						target.playSound(target.getPlayer().getLocation(),
								Sound.AMBIENT_CAVE,
								2.0F, 0.5F);
					} else {
						
					target.playSound(target.getPlayer().getLocation(),
							Sound.valueOf(sound2),
							(float) emotes.getDouble("Feelings." + cmdconfig + ".Sounds.Sound2.Volume"),
							(float) emotes.getDouble("Feelings." + cmdconfig + ".Sounds.Sound2.Pitch"));
					}
					
					if (sender instanceof Player) {
						if(sound2.contains("DISC")) {
							if(debug) {
								getLogger().info("[Debug] Skipping DISC sound for sender on /" + cmdconfig);
							}
						} else {
						Player p = (Player) sender;
						p.playSound(p.getLocation(),
								Sound.valueOf(sound2),
								(float) emotes.getDouble("Feelings." + cmdconfig + ".Sounds.Sound2.Volume"),
								(float) emotes.getDouble("Feelings." + cmdconfig + ".Sounds.Sound2.Pitch"));
					}}
		        }

			} catch (Exception sounderr) { // err test for sounds
				getLogger().info("One or more of your sounds for /" + cmdconfig + " is incorrect. See below:");
				sounderr.printStackTrace();
				getLogger().info("DO NOT report this error. This is a configuration related issue.");
				getLogger().info("---------------------------[End of Error]---------------------------");
			}
			} // end of config sound check
			// ---------- End of Sounds

			// Add Stats
			if(sender instanceof Player && usingpuuids) {
				if(!cmd.getName().equalsIgnoreCase("Spook")) {
				Player p = (Player)sender;
				statsAdd(p.getUniqueId().toString(), cmdconfig);
			}}
			// End Stats
			return true;
		}
			
		if (cmd.getName().equalsIgnoreCase("chatfeelings") && args.length >= 1) {
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
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		
		if(Cooldowns.spook.containsKey(p.getName())) {
			Cooldowns.spookStop(e.getPlayer());
		}
		
		removeAll(e.getPlayer());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		
		if(getConfig().getBoolean("Other.Updates.Check")) {
		if(p.hasPermission("chatfeelings.admin") || p.isOp()) {
		if (Updater.isOutdated()) {
			Msgs.sendPrefix(e.getPlayer(), "&c&lOutdated Plugin! &7Running v" + getDescription().getVersion()
					+ " while the latest is &f&l" + Updater.OutdatedVersion());
		}}}
		
		if (p.getUniqueId().toString().equals("6191ff85-e092-4e9a-94bd-63df409c2079")) {
			Msgs.send(e.getPlayer(), "&7This server is running &fChatFeelings &6v" + getDescription().getVersion()
					+ " &7for " + Bukkit.getBukkitVersion().replace("-SNAPSHOT", ""));
		}
	}
	
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
			
		 Player p = (Player)e.getPlayer();
		 
		 if(Cooldowns.spook.containsKey(p.getName())) {
			 e.setCancelled(true);
			 bass(p);
			 Msgs.sendPrefix(p, "&c&lSorry! &fYou can't teleport while being spooked.");
			 Msgs.sendPrefix(p, "&e&oTip: &7&oTo prevent the spooks, you can put a helmet on your head.");
		 }
	}
}
