package com.zachduda.chatfeelings;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import space.arim.morepaperlib.scheduling.ScheduledTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Cooldowns {
	private static final Main plugin = Main.getPlugin(Main.class);

	static HashMap<Player, Long> cooldown = new HashMap<>();
	static HashMap<Player, String> ignorecooldown = new HashMap<>();
	static HashMap<Player, String> ignorelistcooldown = new HashMap<>();
	static HashMap<String, ScheduledTask> spook = new HashMap<String, ScheduledTask>();

	static ArrayList<String> playerFileUpdate = new ArrayList<>();
	
	static void removeAll(Player p) {
		cooldown.remove(p);
		ignorecooldown.remove(p);
		spookStop(p);
	}
	
	static void putCooldown(Player p) {
		// Cooldown used for Feelings. Needs current mili time for active calc.
		cooldown.put(p, System.currentTimeMillis());
	}
	
	static void ignoreCooldown(Player p) {
		// Cooldown used when player ignores another player or all players. Helps prevent file cache spam.
		ignorecooldown.put(p, p.getName());

		plugin.morePaperLib.scheduling().globalRegionalScheduler().runDelayed(() -> ignorecooldown.remove(p), 20L * plugin.getConfig().getInt("General.Cooldowns.Ignoring.Seconds"));
	}
	
	static void ignoreListCooldown(Player p) {
		// Cooldown used when player ignores another player or all players. Helps prevent file cache spam.
		ignorelistcooldown.put(p, p.getName());

		plugin.morePaperLib.scheduling().globalRegionalScheduler().runDelayed(() -> ignorelistcooldown.remove(p), 20L * plugin.getConfig().getInt("General.Cooldowns.Ignore-List.Seconds"));
	}
	
	static void justJoined(String p) {
		// ArrayList used to not update the player file if it's been less than 60 seconds since the join update.
		
		if(!playerFileUpdate.contains(p)) {
			playerFileUpdate.add(p);
		}

		plugin.morePaperLib.scheduling().globalRegionalScheduler().runDelayed(() -> playerFileUpdate.remove(p), 1200L); // 1 minute
	}

	static void spookStop(Player p) {
		if(Cooldowns.spook.containsKey(p.getName())) {
			plugin.morePaperLib.scheduling().globalRegionalScheduler().run(() -> {
				p.getInventory().setHelmet(new ItemStack(Material.AIR, 1));
				p.removePotionEffect(PotionEffectType.SLOWNESS);
				p.removePotionEffect(PotionEffectType.BLINDNESS);
				p.removePotionEffect(PotionEffectType.SATURATION);
				p.removePotionEffect(PotionEffectType.NAUSEA);
			});

			// idk man, this might not work --zach 10/18/24
			Cooldowns.spook.get(p.getName()).cancel();
			Cooldowns.spook.remove(p.getName());
		}
	}

	@SuppressWarnings("UnnecessaryLocalVariable")
	static ScheduledTask spookTimer(Player p) {
		if(!Main.particles) {
			return null;
		}
		ScheduledTask timer = plugin.morePaperLib.scheduling().globalRegionalScheduler().runAtFixedRate(() -> {
			if(!p.isOnline()) {
				if(spook.containsKey(p.getName())) {
					spookStop(p);
				}
				return;
			}
			Particles.spookDripParticle(p);
		}, 5, 5);
		return timer;
	}

	static void spookHash(Player p) {
		spook.put(p.getName(), spookTimer(p));

		plugin.morePaperLib.scheduling().globalRegionalScheduler().runDelayed(() -> {
			spookStop(p);

			if(p.isOnline()) {
				plugin.pop(p);

				if (!Main.multiversion) {
					p.stopSound(Sound.MUSIC_DISC_13);
				}

				Msgs.sendPrefix(p, Objects.requireNonNull(plugin.emotes.getString("Feelings.Spook.Finished")).replaceAll("%player%", p.getName()));

			}
		}, 20 * 10);
	}

}
