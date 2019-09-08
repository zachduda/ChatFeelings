package com.zach_attack.chatfeelings;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Cooldowns {
	private static Main plugin = Main.getPlugin(Main.class);
	
	static HashMap<Player, Long> cooldown = new HashMap<Player, Long>();
	static HashMap<Player, String> ignorecooldown = new HashMap<Player, String>();
	static HashMap<Player, String> justjoined = new HashMap<Player, String>();
	
	static HashMap<String, Integer> spook = new HashMap<String, Integer>();
	
	static void removeAll(Player p) {
		cooldown.remove(p);
		ignorecooldown.remove(p);
	}
	
	static void putCooldown(Player p) {
		// Cooldown used for Feelings. Needs current mili time for active calc.
		cooldown.put(p, System.currentTimeMillis());
	}
	
	static int spookTimer(Player p) {
    int timerid = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable(){
        @Override
        public void run() {
        	if(!p.isOnline()) {
        		return;
        	}
		    Particles.spookDripParticle(p);
        }}, 5, 5);
	 return timerid;
	}
	
	static void spookHash(Player p) {
		spook.put(p.getName(), spookTimer(p));

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				Particles.spookStop(p);
				
				if(p.isOnline()) {
					plugin.pop(p);
					
					if(!plugin.getConfig().getBoolean("General.Multi-Version-Support")) {
	    	        	p.stopSound(Sound.MUSIC_DISC_13);	
	    	        }
					
					Msgs.send(p, "&e" + p.getName() + "&7, your spooky days are finally over.");
				}
			}
		}, 20 * 10);
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
