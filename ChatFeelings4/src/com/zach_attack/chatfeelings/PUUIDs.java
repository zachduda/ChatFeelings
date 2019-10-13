package com.zach_attack.chatfeelings;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.zach_attack.puuids.api.OnNewFile;
import com.zach_attack.puuids.api.PUUIDS;

public class PUUIDs implements Listener {
	private static Main plugin = (Main)Main.getPlugin(Main.class);
	
	@EventHandler
	public void newFile(OnNewFile event) {
		Player p = event.getPlayer();
		String uuid = p.getUniqueId().toString();
		
		PUUIDS.set(plugin, uuid, "Allow-Feelings", true);
		PUUIDS.set(plugin, uuid, "Muted", false);
	}
}
