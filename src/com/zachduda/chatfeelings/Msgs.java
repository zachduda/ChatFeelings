package com.zachduda.chatfeelings;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Msgs {	
	private static final Main plugin = Main.getPlugin(Main.class);

	static String color(String msg) {
		final Pattern HEX = Pattern.compile("(?i)(?:&#|#|&x)([0-9a-f]{6})");
		Matcher matcher = HEX.matcher(msg);
		StringBuilder sb = new StringBuilder();

		while (matcher.find()) {
			String hex = matcher.group(1);
			StringBuilder replacement = new StringBuilder("&x");
			for (char c : hex.toCharArray()) {
				replacement.append('&').append(c);
			}
			matcher.appendReplacement(sb, replacement.toString());
		}
		matcher.appendTail(sb);

		return ChatColor.translateAlternateColorCodes('&', sb.toString());
	}

	static void sendPrefix(CommandSender sender, String msg) {
	    String prefix = color(plugin.msg.getString("Prefix"));
	    sender.sendMessage(prefix + color(msg));
	}
	
	static void send(CommandSender sender, String msg) {
	    sender.sendMessage(color(msg));
	}
	
}
