package com.zachduda.chatfeelings;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Msgs {	
	private static final Main plugin = Main.getPlugin(Main.class);

	static String color(String msg) {
		msg = msg.replaceAll("&#", "#");
		Pattern pattern = Pattern.compile("(&#|#|&)[a-fA-F0-9]{6}");
		Matcher matcher = pattern.matcher(msg);
		while (matcher.find()) {
			String hexCode = msg.substring(matcher.start(), matcher.end());
			String replaceAmp = hexCode.replaceAll("&#", "x");
			String replaceSharp = replaceAmp.replace('#', 'x');

			char[] ch = replaceSharp.toCharArray();
			StringBuilder builder = new StringBuilder();
			for (char c : ch) {
				builder.append("&" + c);
			}

			msg = msg.replace(hexCode, builder.toString());
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
