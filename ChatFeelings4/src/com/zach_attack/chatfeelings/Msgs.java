package com.zach_attack.chatfeelings;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Msgs {
	private static File folder = Bukkit.getServer().getPluginManager().getPlugin("ChatFeelings").getDataFolder();
	private static File msgsfile = new File(folder, File.separator + "messages.yml");
	private static FileConfiguration messages = YamlConfiguration.loadConfiguration(msgsfile);
	
	public static void sendPrefix(CommandSender sender, String msg) {
	    String prefix = ChatColor.translateAlternateColorCodes('&', messages.getString("Prefix"));
	    sender.sendMessage(prefix + " " + (ChatColor.translateAlternateColorCodes('&', msg)));	
	}
	
	public static void send(CommandSender sender, String msg) {
	    sender.sendMessage((ChatColor.translateAlternateColorCodes('&', msg)));	
	}
	
}
