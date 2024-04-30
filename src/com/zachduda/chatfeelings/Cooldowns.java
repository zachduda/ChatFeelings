package com.zachduda.chatfeelings;

import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.HashMap;


public class Cooldowns {
	private static final Main plugin = Main.getPlugin(Main.class);

	static HashMap<Player, Long> cooldown = new HashMap<>();
	static HashMap<Player, String> ignorecooldown = new HashMap<>();
	static HashMap<Player, String> ignorelistcooldown = new HashMap<>();
	
	static ArrayList<String> playerFileUpdate = new ArrayList<>();
	
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

}
