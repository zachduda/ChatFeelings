package com.zachduda.chatfeelings.api;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;

import com.zachduda.chatfeelings.Main;

public class ChatFeelingsAPI {

	/**
	 *    v        [-------------------------------------------------------------------------------------]
	 *  [ ! ]      [  Most if not all functions require using Async methods to access the API.           ]
	 *    v        [  It is HIGHLY recommended you use async as data lookups can block the main thread!  ]
	 *             [-------------------------------------------------------------------------------------]
	 */

	private final static Main plugin = Main.getPlugin(Main.class);

	/**
	 * MUST USE ASYNC *
	 * Checks to see if a player has been muted by ChatFeelings.
	 *
	 * @param username The username to check.
	 * @return boolean True = Muted / False = Not Muted
	 */
	public static boolean isMuted(String username) throws IllegalAccessException {
		if(Bukkit.isPrimaryThread()) {
			throw new IllegalAccessException("ChatFeelings API isBanned() must be accessed Asynchronously.");
		}

		// Username --> UUID
		final UUID uuid = plugin.hasPlayedNameGetUUID(username);
		
		if(uuid == null) {
			return false;
		}
		
		if(plugin.APIisMutedUUIDBoolean(uuid)) {
			return true;
		}
		return false;
	}

	/**
	 * MUST USE ASYNC *
	 * Checks to see if a player has been muted by ChatFeelings.
	 *
	 * @param uuid The UUID to check.
	 * @return boolean True = Muted / False = Not Muted
	 */
	public static boolean isMuted(UUID uuid) throws IllegalAccessException {
		if(Bukkit.isPrimaryThread()) {
			throw new IllegalAccessException("ChatFeelings API isMuted() must be accessed Asynchronously.");
		}
		
		if(plugin.APIisMutedUUIDBoolean(uuid)) {
			return true;
		}
		return false;
	}

	/**
	 * Recommended to use Async *
	 * Checks to see if ChatFeelings has a data file for a specific player.
	 *
	 * @param uuid The UUID to check.
	 * @return boolean Result
	 */
	public static boolean hasPlayedBefore(UUID uuid) {
		final String name = plugin.hasPlayedUUIDGetName(uuid);
		if(name == null || name.equals("0")) {
			return false;
		}
		return true;
	}

	/**
	 * Recommended to use Async especially if searching with a username as the UUID search can take hold up main thread. *
	 * Checks to see if ChatFeelings has a data file for a specific player.
	 *
	 * @param username The username to check.
	 * @return boolean Result
	 */
	public static boolean hasPlayedBefore(String username) {
		final UUID u = plugin.hasPlayedNameGetUUID(username);
		if(u == null) {
			return false;
		}
		return true;
	}

	/**
	 * MUST USE ASYNC *
	 * Checks to see if a specific player is banned.
	 *
	 * @param username The username to check.
	 * @return boolean True = Banned / False = Not Banned
	 */
	public static boolean isBanned(String username) throws IllegalAccessException {
		if(Bukkit.isPrimaryThread()) {
			throw new IllegalAccessException("ChatFeelings API isBanned() must be accessed Asynchronously.");
		}

		// Convert to UUID from username.
		final UUID uuid = plugin.hasPlayedNameGetUUID(username);
		
		if(uuid == null) {
			return false;
		}
		
		if(plugin.APIisBannedUUIDBoolean(uuid)) {
			return true;
		}
		
		return false;
	}

	/**
	 * MUST USE ASYNC *
	 * Checks to see if a specific player is banned.
	 *
	 * @param uuid The UUID to check and see if they're banned.
	 * @return boolean True = Banned / False = Not Banned
	 */
	public static boolean isBanned(UUID uuid) throws IllegalAccessException {
		if(Bukkit.isPrimaryThread()) {
			throw new IllegalAccessException("ChatFeelings API isBanned() must be accessed Asynchronously.");
		}

		if(plugin.APIisBannedUUIDBoolean(uuid)) {
			return true;
		}
		
		return false;
	}
	
	public static int getSentStats(String name, String feeling) {
		final UUID uuid = plugin.hasPlayedNameGetUUID(name);
		if(uuid == null) {
			return 0;
		}
		return plugin.APIgetSentStat(uuid, feeling);
	}
	
	public static int getSentStats(UUID uuid, String feeling) {
		return plugin.APIgetSentStat(uuid, feeling);
	}
	
	public static int getTotalFeelingsSent(String name) {
		final UUID uuid = plugin.hasPlayedNameGetUUID(name);
		if(uuid == null) {
			return 0;
		}
		return plugin.APIgetTotalSent(uuid);
	}
	
	public static int getTotalFeelingsSent(UUID uuid) {
		return plugin.APIgetTotalSent(uuid);
	}
	
	public static boolean usingAdvancedBans() {
		return plugin.APIhasAB();
	}
	
	public static boolean usingLiteBans() {
		return plugin.APIhasLB();
	}
	
	public static boolean usingEssentials() {
		return plugin.APIhasEss();
	}
	
	public static boolean hookedToPunishments() {
		if(plugin.APIhasAB() || plugin.APIhasEss() || plugin.APIhasLB()) {
			return true;
		}
		return false;
	}
	
	public static List<String> getFeelingsList() {
		return plugin.APIgetFeelings();
	}
	
	public static boolean isAcceptingFeelings(UUID u) {
		return plugin.APIisAcceptingFeelings(u);
	}
}
