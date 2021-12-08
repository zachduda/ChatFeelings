package com.zach_attack.chatfeelings.other;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

// ChatFeelings Async Check --> Based off of Benz56's update checker <3
// https://github.com/Benz56/Async-Update-Checker/blob/master/UpdateChecker.java

public class Updater {

    private final JavaPlugin javaPlugin;
    private final String localPluginVersion;
    
    static String outdatedversion = "???";
    static boolean outdated = false;

    private static final int ID = 12987;
    private static final long CHECK_INTERVAL = 1_728_000; //In ticks.
    
    public Updater(final JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
        this.localPluginVersion = javaPlugin.getDescription().getVersion();
    }

    public void checkForUpdate() {
    	try {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getScheduler().runTaskAsynchronously(javaPlugin, () -> {
                    try {
                    	URL url = new URL("https://api.spigotmc.org/simple/0.1/index.php?action=getResource&id="+ID);
            			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            			String str = br.readLine();
                    	outdatedversion = (String) ((JSONObject) new JSONParser().parse(str)).get("current_version");
                    } catch (final IOException | ParseException e) {
                    	e.printStackTrace();
                        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[ChatFeelings] Unable to check for updates. Is your server online?");
                        cancel();
                        return;
                    }

                    if (("v" + localPluginVersion).equalsIgnoreCase(outdatedversion)) {
                    	return;
                    }
                    
                    if(outdatedversion.equalsIgnoreCase("v4.7")) {
                    	return;
                    }
                    
                    outdated = true;
                    Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&r[ChatFeelings] &e&l&nUpdate Available&r&e&l! &rYou're running &7v" + localPluginVersion + "&r, while the latest is &a" + outdatedversion));
                    cancel(); //Cancel the runnable as an update has been found.
                });
            }
        }.runTaskTimer(javaPlugin, 0, CHECK_INTERVAL);
    	}catch(Exception err) {
    		javaPlugin.getLogger().warning("Error. There was a problem checking for updates.");
    	}
    }

	public static boolean isOutdated() {
		return outdated;
	}
	
	public static String getOutdatedVersion() {
		return outdatedversion;
	}
}