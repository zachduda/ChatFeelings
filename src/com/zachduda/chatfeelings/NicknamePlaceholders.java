package com.zachduda.chatfeelings;

import org.bukkit.configuration.file.FileConfiguration;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;

/**
 * Handles replacing placeholders with nicknames and playernames.
 */
public class NicknamePlaceholders {

	private static boolean use_nickname = false;
	private static String nickname_placeholder = "%essentials_nickname%";
	private static String console_name = "Console";

	/**
	 * Checks if the config is set to use nickname placeholders and if a nickname
	 * placeholder is set. If so then it enables placeholders
	 * 
	 * @param config
	 * @return boolean should nicknames be replaced
	 */
	static boolean enablePlaceholders(FileConfiguration config, FileConfiguration msgConfig, boolean papiExists) {
		if (msgConfig.getString("Console-Name") != null) {

			console_name = msgConfig.getString("Console-Name");
		} else {
			console_name = "The Server";
		}
		
		if(papiExists == false) {
			use_nickname = false;
			return false;
		}
		
		
		if (!config.getBoolean("General.Use-Nickname-Placeholder")) {
			use_nickname = false;
			return false;
		}

		if (config.getString("General.Nickname-Placeholder") == null) {
			use_nickname = false;
			return false;
		}

		nickname_placeholder = config.getString("General.Nickname-Placeholder");
		use_nickname = true;
		return true;

	}

	/**
	 * replaces %sender% and %target% with either nicknames or usernames as
	 * specified in the config
	 * 
	 * @param msg
	 * @param sender
	 * @param target
	 * @return string
	 */
	static String replacePlaceholders(String msg, CommandSender sender, Player target) {

		if (!use_nickname || nickname_placeholder == null) {

			return replaceUsernames(msg, sender, target);
		}

		String senderNick;
		if (sender instanceof Player) {
			senderNick = PlaceholderAPI.setPlaceholders((Player) sender, nickname_placeholder);
		} else {
			senderNick = console_name;
		}

		String targetNick = PlaceholderAPI.setPlaceholders(target, nickname_placeholder);

		if (senderNick == nickname_placeholder || targetNick == nickname_placeholder) {
			return replaceUsernames(msg, sender, target);
		}

		String out = msg.replace("%sender%", senderNick).replace("%target%", targetNick);

		return out;
	}

	/**
	 * replaces %player% with either nickname or username as specified in the config
	 * 
	 * @param msg
	 * @param player
	 * @return string
	 */
	static String replacePlaceholders(String msg, Player player) {

		if (!use_nickname || nickname_placeholder == null) {

			return replaceUsernames(msg, player);
		}

		String playerNick = PlaceholderAPI.setPlaceholders(player, nickname_placeholder);

		if (playerNick == nickname_placeholder) {
			return replaceUsernames(msg, player);
		}

		String out = msg.replace("%player%", playerNick);
		return out;
	}

	/**
	 * replaces %player% with either nickname or username as specified in the config
	 * 
	 * @param msg
	 * @param sender
	 * @return replaced string
	 */
	static String replacePlaceholders(String msg, CommandSender sender) {

		if (!use_nickname || nickname_placeholder == null) {

			return replaceUsernames(msg, sender);
		}

		String senderNick;
		if (sender instanceof Player) {
			senderNick = PlaceholderAPI.setPlaceholders((Player) sender, nickname_placeholder);
		} else {
			senderNick = console_name;
		}

		if (senderNick == nickname_placeholder) {
			return replaceUsernames(msg, sender);
		}

		return msg.replace("%player%", senderNick);
	}

	/**
	 * replaces %sender% and %target% with usernames
	 * 
	 * @param msg
	 * @param sender player sending the message
	 * @param target target of the message
	 * @return replaced string
	 */
	static String replaceUsernames(String msg, CommandSender sender, Player target) {

		String senderName;
		if (sender instanceof Player) {
			senderName = ((Player) sender).getName();
		} else {
			senderName = console_name;
		}

		return msg.replace("%sender%", senderName).replace("%target%", target.getName());
	}

	/**
	 * replaces %player% with username
	 * 
	 * @param msg
	 * @param player player who's username to use
	 * @return replaced string
	 */
	static String replaceUsernames(String msg, Player player) {

		return msg.replace("%player%", player.getName());
	}

	/**
	 * replaces %player% with username
	 * 
	 * @param msg
	 * @param CommandSender who's username to use
	 * @return replaced string
	 */
	static String replaceUsernames(String msg, CommandSender sender) {

		String playerName;
		if (sender instanceof Player) {
			playerName = ((Player) sender).getName();
		} else {
			playerName = console_name;
		}

		return msg.replace("%player%", playerName);
	}

}
