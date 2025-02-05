package com.zachduda.chatfeelings;

import org.bukkit.configuration.file.FileConfiguration;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;

import java.util.Objects;

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
	 */
	static void enablePlaceholders(FileConfiguration config, FileConfiguration msgConfig, boolean papiExists) {
		if (msgConfig.getString("Console-Name") != null) {

			console_name = msgConfig.getString("Console-Name");
		} else {
			console_name = "The Server";
		}
		
		if(!papiExists) {
			use_nickname = false;
			return;
		}
		
		
		if (!config.getBoolean("General.Use-Nickname-Placeholder")) {
			use_nickname = false;
			return;
		}

		if (config.getString("General.Nickname-Placeholder") == null) {
			use_nickname = false;
			return;
		}

		nickname_placeholder = config.getString("General.Nickname-Placeholder");
		use_nickname = true;

	}

	/**
	 * replaces %sender% and %target% with either nicknames or usernames as
	 * specified in the config
	 * 
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

		if (Objects.equals(senderNick, nickname_placeholder) || targetNick.equals(nickname_placeholder)) {
			return replaceUsernames(msg, sender, target);
		}

        return msg.replace("%sender%", senderNick).replace("%target%", targetNick);
	}

	/**
	 * replaces %player% with either nickname or username as specified in the config
	 * 
	 * @return string
	 */
	static String replacePlaceholders(String msg, Player player) {

		if (!use_nickname || nickname_placeholder == null) {

			return replaceUsernames(msg, player);
		}

		String playerNick = PlaceholderAPI.setPlaceholders(player, nickname_placeholder);

		if (playerNick.equals(nickname_placeholder)) {
			return replaceUsernames(msg, player);
		}

        return msg.replace("%player%", playerNick);
	}

	/**
	 * replaces %player% with either nickname or username as specified in the config
	 * 
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

		if (Objects.equals(senderNick, nickname_placeholder)) {
			return replaceUsernames(msg, sender);
		}

		return msg.replace("%player%", senderNick);
	}

	/**
	 * replaces %sender% and %target% with usernames
	 * 
	 * @param sender player sending the message
	 * @param target target of the message
	 * @return replaced string
	 */
	static String replaceUsernames(String msg, CommandSender sender, Player target) {

		String senderName;
		if (sender instanceof Player) {
			senderName = sender.getName();
		} else {
			senderName = console_name;
		}

		return msg.replace("%sender%", senderName).replace("%target%", target.getName());
	}

	/**
	 * replaces %player% with username
	 * 
	 * @param player player who's username to use
	 * @return replaced string
	 */
	static String replaceUsernames(String msg, Player player) {

		return msg.replace("%player%", player.getName());
	}

	/**
	 * replaces %player% with username
	 * 
	 * @param sender who's username to use
	 * @return replaced string
	 */
	static String replaceUsernames(String msg, CommandSender sender) {

		String playerName;
		if (sender instanceof Player) {
			playerName = sender.getName();
		} else {
			playerName = console_name;
		}

		return msg.replace("%player%", playerName);
	}

}
