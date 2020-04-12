package com.zach_attack.chatfeelings.api;

import com.zach_attack.chatfeelings.Main;

public class ChatFeelingsAPI {
	private final static Main plugin = Main.getPlugin(Main.class);
	
	// All API events automatically check if a player has played before. If not, it will return "false" or "0".
	// Therefore, it is not needed to run hasPlayedBefore when checking ANY of these!
	
	// ALL API calls here need a player USERNAME. The only exception is ChatFeelingsAPI.hasPlayedBefore(), 
	//                                                     this can take a UUID (string) or Name.
	
	// Feel free to submit a pull request for an API feature you wish to have implemented!
	
	// MUST BE RUN ASYNC
	public static boolean isMuted(String name) {
		String player = plugin.hasPlayedNameGetUUID(name);
		
		if(player.equals("0")) {
			return false;
		} else {
		
		if(plugin.APIisMutedUUIDBoolean(player)) {
			return true;
		}}
		
		return false;
	}
	
	public static boolean hasPlayedBefore(String name) {
		if(plugin.hasPlayedNameGetUUID(name).equals("0") && plugin.hasPlayedUUIDGetName(name).equals("0")) {
			return false;
		} else {
			return true;
		}
	}
	
	// MUST BE RUN ASYNC
	public static boolean isBanned(String name) {
		String player = plugin.hasPlayedNameGetUUID(name);
		
		if(player.equals("0")) {
			return false;
		} else {
		
		if(plugin.APIisBannedUUIDBoolean(player)) {
			return true;
		}}
		
		return false;
	}
	
	public static int getSentStats(String name, String feeling) {
		return plugin.APIgetSentStat(name, feeling);
	}
	
	public static int getTotalFeelingsSent(String name) {
		return plugin.APIgetTotalSent(name);
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
}
