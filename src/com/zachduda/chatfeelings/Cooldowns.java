package com.zachduda.chatfeelings;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cooldowns are read & written from async tasks (feelings are handled off the main thread), so
 * everything here is a concurrent map keyed by UUID holding the time the cooldown expires.
 * Expired entries are simply ignored/overwritten, so no scheduled removal tasks are needed.
 */
public class Cooldowns {
	private static final Main plugin = Main.getPlugin(Main.class);

	/** Last time (millis) a player sent a feeling. */
	static final Map<UUID, Long> cooldown = new ConcurrentHashMap<>();
	private static final Map<UUID, Long> ignoreUntil = new ConcurrentHashMap<>();
	private static final Map<UUID, Long> ignoreListUntil = new ConcurrentHashMap<>();
	private static final Map<String, Long> playerFileUpdateUntil = new ConcurrentHashMap<>();

	private static boolean active(Map<?, Long> map, Object key) {
		final Long until = map.get(key);
		if (until == null) {
			return false;
		}
		if (until <= System.currentTimeMillis()) {
			map.remove(key, until);
			return false;
		}
		return true;
	}

	static void removeAll(Player p) {
		final UUID u = p.getUniqueId();
		cooldown.remove(u);
		ignoreUntil.remove(u);
		ignoreListUntil.remove(u);
	}

	static void putCooldown(Player p) {
		// Cooldown used for Feelings. Needs current mili time for active calc.
		cooldown.put(p.getUniqueId(), System.currentTimeMillis());
	}

	static boolean isIgnoreCooldown(UUID u) {
		return active(ignoreUntil, u);
	}

	static void ignoreCooldown(Player p) {
		// Cooldown used when player ignores another player or all players. Helps prevent file cache spam.
		ignoreUntil.put(p.getUniqueId(), System.currentTimeMillis() + 1000L * plugin.getConfig().getInt("General.Cooldowns.Ignoring.Seconds"));
	}

	static boolean isIgnoreListCooldown(UUID u) {
		return active(ignoreListUntil, u);
	}

	static void ignoreListCooldown(Player p) {
		ignoreListUntil.put(p.getUniqueId(), System.currentTimeMillis() + 1000L * plugin.getConfig().getInt("General.Cooldowns.Ignore-List.Seconds"));
	}

	/** True if the player's data was refreshed less than 60 seconds ago. */
	static boolean recentlyUpdated(String name) {
		return active(playerFileUpdateUntil, name);
	}

	static void justJoined(String name) {
		// Don't update the player's data again if it's been less than 60 seconds since the last update.
		playerFileUpdateUntil.put(name, System.currentTimeMillis() + 60_000L);
	}
}
