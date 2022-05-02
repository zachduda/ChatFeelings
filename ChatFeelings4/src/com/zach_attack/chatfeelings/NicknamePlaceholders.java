package com.zach_attack.chatfeelings;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;

/**
 * Handles replacing placeholders with nicknames and playernames.
 */
public class NicknamePlaceholders {
	
	private static boolean use_nickname = null;
	private static String nickname_placeholder = null;
	private static String console_name = "Console";
	
	/**
	 * Checks if the config is set to use nickname placeholders and if a nickname placeholder is set.
	 * If so then it enables placeholders
	 * @param config
	 * @return boolean should nicknames be replaced
	 */
	static boolean enablePlaceholders(FileConfiguration config, FileConfiguration msgConfig) {
		if(!config.getBoolean("General.Use-Nickname-Placeholder")) {
			use_nickname = false;
			return false;
		}
		
		if(config.getString("General.Nickname-Placeholder") == null) {
			use_nickname = false;
			return false;
		}

		if(msgConfig.getString("Console-Name") != null) {
			console_name = msgConfig.getString("Console-Name");
		} else {
			console_name = "Console";
		}
		
		nickname_placeholder = config.getString("General.Nickname-Placeholder");
		use_nickname = true;
		return true;
		
	}
	
	/**
	 * replaces %sender% and %target% with either nicknames or usernames as specified in the config
	 * @param msg
	 * @param sender 
	 * @param target
	 * @return string
	 */
	static String replacePlaceholders(String msg, CommandSender sender, Player target) {
		if(use_nickname == null || !use_nickname || nickname_placeholder == null) {
			return replaceUsernames();
		}
		
		String senderNick;
		if(sender instanceof Player) {
			senderNick = PlaceholderAPI.setPlaceholders((Player) sender, nickname_placeholder);
		} else {
			senderNick = console_name;
		}
		
		String targetNick = PlaceholderAPI.setPlaceholders(target, nickname_placeholder);
		
		if(senderNick == nickname_placeholder || targetNick == nickname_placeholder) {
			return replaceUsernames();
		}
		
		return msg.replace("%sender%", senderNick).replace("%target%", targetNick);
	}
	
	/**
	 * replaces %sender% and %target% with usernames
	 * @param msg
	 * @param sender player sending the message
	 * @param target target of the message
	 * @return
	 */
	static String replaceUsernames(String msg, CommandSender sender, Player target) {
		
		String senderName;
		if(sender instanceof Player) {
			senderName = ((Player) sender).getName();
		} else {
			senderName = console_name;
		}
		
		return msg.replace("%sender%", senderName).replace("%target%", target.getName());
	}
	
}
