package com.zachduda.chatfeelings;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;


public class Cooldowns {
	private static final Main plugin = Main.getPlugin(Main.class);

	static HashMap<String, Player> nicknames = new HashMap<String, Player>();
	static HashMap<Player, Long> cooldown = new HashMap<Player, Long>();
	static HashMap<Player, String> ignorecooldown = new HashMap<Player, String>();
	static HashMap<Player, String> ignorelistcooldown = new HashMap<Player, String>();
	
	static ArrayList<String> playerFileUpdate = new ArrayList<String>();
	
	static void removeAll(Player p) {
		cooldown.remove(p);
		ignorecooldown.remove(p);
		nicknames.remove(ChatColor.stripColor(Main.getEssNick(p.getUniqueId())));
	}
	
	static void putCooldown(Player p) {
		// Cooldown used for Feelings. Needs current mili time for active calc.
		cooldown.put(p, System.currentTimeMillis());
	}
	
	static void ignoreCooldown(Player p) {
		// Cooldown used when player ignores another player or all players. Helps prevent file cache spam.
		ignorecooldown.put(p, p.getName());

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				ignorecooldown.remove(p);
			}
		}, 20L * plugin.getConfig().getInt("General.Cooldowns.Ignoring.Seconds"));
	}
	
	static void ignoreListCooldown(Player p) {
		// Cooldown used when player ignores another player or all players. Helps prevent file cache spam.
		ignorelistcooldown.put(p, p.getName());

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				ignorelistcooldown.remove(p);
			}
		}, 20L * plugin.getConfig().getInt("General.Cooldowns.Ignore-List.Seconds"));
	}
	
	static void justJoined(String p) {
		// ArrayList used to not update the player file if it's been less than 60 seconds since the join update.
		
		if(!playerFileUpdate.contains(p)) {
			playerFileUpdate.add(p);
		}

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				playerFileUpdate.remove(p);
			}
		}, 1200L); // 1 minute
	}

	static void saveNickname(Player p) {
		nicknames.put(ChatColor.stripColor(Main.getEssNick(p.getUniqueId())), p);
	}

}
