package com.zach_attack.chatfeelings.api;

import java.util.UUID;

import org.bukkit.Bukkit;

import com.zach_attack.chatfeelings.Main;

public class ChatFeelingsAPI {
	private final static Main plugin = Main.getPlugin(Main.class);
	
	//    All API events automatically check if a player has played before. If not, it will return "false" or "0".
	//    Therefore, it is not needed to run hasPlayedBefore when checking ANY of these!
	
	// ALL API calls should use a UUID if possible. If not, ChatFeelings will except a username as a string and search that way. 
	//                             ** NOTE: Searching for usernames can be intensive. Try to do them asynchronously if possible.
	
	//           Feel free to submit a pull request for an API feature you wish to have implemented!
	
	//        ****     Highly recommended to use ASYNC when accessing anything from the ChatFeelings API   ****
	
	// ** REQUIRED TO BE ASYNC
	public static boolean isMuted(String name) throws IllegalAccessException {
		if(Bukkit.isPrimaryThread()) {
			throw new IllegalAccessException("ChatFeelings API isBanned() must be accessed Asynchronously.");
		}
		
		final UUID uuid = plugin.hasPlayedNameGetUUID(name);
		
		if(uuid == null) {
			return false;
		}
		
		if(plugin.APIisMutedUUIDBoolean(uuid)) {
			return true;
		}
		return false;
	}
	
	// ** REQUIRED TO BE ASYNC
	public static boolean isMuted(UUID uuid) throws IllegalAccessException {
		if(Bukkit.isPrimaryThread()) {
			throw new IllegalAccessException("ChatFeelings API isMuted() must be accessed Asynchronously.");
		}
		
		if(plugin.APIisMutedUUIDBoolean(uuid)) {
			return true;
		}
		return false;
	}
	
	public static boolean hasPlayedBefore(UUID uuid) {
		final String name = plugin.hasPlayedUUIDGetName(uuid);
		if(name == null || name.equals("0")) {
			return false;
		}
		return true;
	}
	
	// If using a username, it's HIGHLY recommended (not required) that you do this async.
	public static boolean hasPlayedBefore(String username) {
		final UUID u = plugin.hasPlayedNameGetUUID(username);
		if(u == null) {
			return false;
		}
		return true;
	}
	
	// ** REQUIRED TO BE ASYNC
	public static boolean isBanned(String name) throws IllegalAccessException {
		if(Bukkit.isPrimaryThread()) {
			throw new IllegalAccessException("ChatFeelings API isBanned() must be accessed Asynchronously.");
		}
		
		final UUID uuid = plugin.hasPlayedNameGetUUID(name);
		
		if(uuid == null) {
			return false;
		}
		
		if(plugin.APIisBannedUUIDBoolean(uuid)) {
			return true;
		}
		
		return false;
	}
	
	// ** REQUIRED TO BE ASYNC
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
	
	public static boolean isAcceptingFeelings(UUID u) {
		return plugin.APIisAcceptingFeelings(u);
	}
}
