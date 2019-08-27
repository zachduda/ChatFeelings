package com.zach_attack.chatfeelings;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Cooldowns {
	private static Main plugin = Main.getPlugin(Main.class);
	
	static HashMap<Player, Long> cooldown = new HashMap<Player, Long>();
	static HashMap<Player, String> ignorecooldown = new HashMap<Player, String>();
	static HashMap<Player, String> justjoined = new HashMap<Player, String>();
	
	static void removeAll(Player p) {
		cooldown.remove(p);
		ignorecooldown.remove(p);
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
		}, 20 * plugin.getConfig().getInt("General.Cooldowns.Ignoring.Seconds"));
	}
	
	static void justJoined(Player p) {
		// Cooldown used to not update the player file if it's been less than 60 seconds since the join update.
		justjoined.put(p, p.getName());

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				justjoined.remove(p);
			}
		}, 1200); // 1 minute
	}
}
