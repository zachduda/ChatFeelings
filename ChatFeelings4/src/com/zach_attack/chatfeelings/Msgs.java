package com.zach_attack.chatfeelings;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.zach_attack.chatfeelings.Main;

public class Msgs {
	static Main plugin = Main.getPlugin(Main.class);
	
	  static File folder = Bukkit.getServer().getPluginManager().getPlugin("ChatFeelings").getDataFolder();
	  static File msgsfile = new File(folder, File.separator + "messages.yml");
	  static FileConfiguration messages = YamlConfiguration.loadConfiguration(msgsfile);
	
	public static void sendPrefix(CommandSender sender, String msg) {
	    String prefix = ChatColor.translateAlternateColorCodes('&', messages.getString("Prefix"));
	    sender.sendMessage(prefix + " " + (ChatColor.translateAlternateColorCodes('&', msg)));	
	}
	
	public static void send(CommandSender sender, String msg) {
	    sender.sendMessage((ChatColor.translateAlternateColorCodes('&', msg)));	
	}
	
}
