package com.zachduda.chatfeelings;

import me.dave.chatcolorhandler.ChatColorHandler;
import org.bukkit.command.CommandSender;

public class Msgs {	
	private static final Main plugin = Main.getPlugin(Main.class);
	
	static void sendPrefix(CommandSender sender, String msg) {
	    String prefix = ChatColorHandler.translateAlternateColorCodes(plugin.msg.getString("Prefix"));
	    sender.sendMessage(prefix + (ChatColorHandler.translateAlternateColorCodes(msg)));
	}
	
	static void send(CommandSender sender, String msg) {
	    sender.sendMessage((ChatColorHandler.translateAlternateColorCodes(msg)));
	}
	
}
