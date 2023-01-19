package com.zachduda.chatfeelings;

import me.dave.chatcolorhandler.ChatColorHandler;
import org.bukkit.command.CommandSender;

public class Msgs {	
	private static final Main plugin = Main.getPlugin(Main.class);

	public static String color(String str) {
		return ChatColorHandler.translateAlternateColorCodes(str);
	}
	static void sendPrefix(CommandSender sender, String msg) {
	    String prefix = color(plugin.msg.getString("Prefix"));
	    sender.sendMessage(prefix + color(msg));
	}
	
	static void send(CommandSender sender, String msg) {
	    sender.sendMessage(color(msg));
	}
	
}
