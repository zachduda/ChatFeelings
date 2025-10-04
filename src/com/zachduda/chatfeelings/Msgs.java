package com.zachduda.chatfeelings;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Msgs {	
	private static final Main plugin = Main.getPlugin(Main.class);

	static String color(String msg) {
		msg = msg.replaceAll("&#", "#");
		Pattern pattern = Pattern.compile("(?<!\\w)(?:#|&x)([A-Fa-f0-9]{6})(?!\\w)");
		Matcher matcher = pattern.matcher(msg);
		while (matcher.find()) {
			String hexCode = matcher.group(0);
			String hex = matcher.group(1);
			String replace = "&x&" + hex.charAt(0) + "&" + hex.charAt(1) + "&" + hex.charAt(2)
					+ "&" + hex.charAt(3) + "&" + hex.charAt(4) + "&" + hex.charAt(5);
			msg = msg.replace(hexCode, replace);
			matcher = pattern.matcher(msg);
		}
		return ChatColor.translateAlternateColorCodes('&', msg);
	}

	static void sendPrefix(CommandSender sender, String msg) {
	    String prefix = color(plugin.msg.getString("Prefix"));
	    sender.sendMessage(prefix + color(msg));
	}
	
	static void send(CommandSender sender, String msg) {
	    sender.sendMessage(color(msg));
	}
	
}
