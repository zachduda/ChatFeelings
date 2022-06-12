package com.zachduda.chatfeelings;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Msgs {	
	private static Main plugin = Main.getPlugin(Main.class);
	
	static void sendPrefix(CommandSender sender, String msg) {
	    String prefix = ChatColor.translateAlternateColorCodes('&', plugin.msg.getString("Prefix"));
	    sender.sendMessage(prefix + " " + (ChatColor.translateAlternateColorCodes('&', msg)));	
	}
	
	static void send(CommandSender sender, String msg) {
	    sender.sendMessage((ChatColor.translateAlternateColorCodes('&', msg)));	
	}
	
}
