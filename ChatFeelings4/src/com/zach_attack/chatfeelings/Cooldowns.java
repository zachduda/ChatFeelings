package com.zach_attack.chatfeelings;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Cooldowns {
	private static Main plugin = Main.getPlugin(Main.class);
	
	static HashMap<Player, Long> cooldown = new HashMap<Player, Long>();
	static HashMap<Player, String> ignorecooldown = new HashMap<Player, String>();
	static HashMap<Player, String> ignorelistcooldown = new HashMap<Player, String>();
	
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
	
	static void ignoreListCooldown(Player p) {
		// Cooldown used when player ignores another player or all players. Helps prevent file cache spam.
		ignorelistcooldown.put(p, p.getName());

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				ignorelistcooldown.remove(p);
			}
		}, 20 * plugin.getConfig().getInt("General.Cooldowns.Ignore-List.Seconds"));
	}
}
