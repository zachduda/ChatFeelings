package com.zach_attack.chatfeelings;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public class Cooldowns {
	private static Main plugin = Main.getPlugin(Main.class);
	
	static HashMap<Player, Long> cooldown = new HashMap<Player, Long>();
	static HashMap<Player, String> ignorecooldown = new HashMap<Player, String>();
	static HashMap<Player, String> ignorelistcooldown = new HashMap<Player, String>();
	
	static HashMap<String, Integer> spook = new HashMap<String, Integer>();
	
	static void removeAll(Player p) {
		cooldown.remove(p);
		ignorecooldown.remove(p);
		spook.remove(p.getName());
	}
	
	static void putCooldown(Player p) {
		// Cooldown used for Feelings. Needs current mili time for active calc.
		cooldown.put(p, System.currentTimeMillis());
	}
	
	static void spookStop(Player p) {
       	p.getInventory().setHelmet(new ItemStack(Material.AIR, 1));
       	
        if(Cooldowns.spook.containsKey(p.getName())) {
        p.removePotionEffect(PotionEffectType.SLOW);
        p.removePotionEffect(PotionEffectType.BLINDNESS);
        p.removePotionEffect(PotionEffectType.SATURATION);
        p.removePotionEffect(PotionEffectType.CONFUSION);
        Bukkit.getScheduler().cancelTask(Cooldowns.spook.get(p.getName()));
		Cooldowns.spook.remove(p.getName());
       }
	}
	
	static int spookTimer(Player p) {
    int timerid = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable(){
        @Override
        public void run() {
        	if(!p.isOnline()) {
        		if(spook.containsKey(p.getName())) {
        			spookStop(p);
        		}
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
				spookStop(p);
				
				if(p.isOnline()) {
					plugin.pop(p);
					
					if(!Main.multiversion) {
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
