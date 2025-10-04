package com.zachduda.chatfeelings.other;

import com.zachduda.chatfeelings.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import space.arim.morepaperlib.MorePaperLib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

// ChatFeelings Async Check --> Based off of Benz56's update checker <3
// https://github.com/Benz56/Async-Update-Checker/blob/master/UpdateChecker.java

public class Updater {

    private final JavaPlugin javaPlugin;
    private final String localPluginVersion;
    private final MorePaperLib morePaperLib;
    
    static String posted_version = "???";
    static boolean outdated = false;

    private static final long CHECK_INTERVAL = 1_728_000; //In ticks.
    
    public Updater(final JavaPlugin javaPlugin, final MorePaperLib morePaperLib) {
        this.javaPlugin = javaPlugin;
        this.localPluginVersion = javaPlugin.getDescription().getVersion();
        this.morePaperLib = morePaperLib;
    }

    public void checkForUpdate() {
    	try {
        new BukkitRunnable() {
            @Override
            public void run() {
                morePaperLib.scheduling().asyncScheduler().run(() -> {
                    try {
                    	URL url = new URL("https://api.github.com/repos/zachduda/ChatFeelings/releases");
            			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            			String str = br.readLine();
                        JSONArray raw_json = (JSONArray) new JSONParser().parse(str);
                        for (JSONObject object : (Iterable<JSONObject>) raw_json) {
                            final String vs = ((String) object.get("tag_name")).replace("v", "");
                            final Boolean prerelease = ((Boolean) object.get("prerelease"));
                            if (!prerelease) {
                                if (!localPluginVersion.equalsIgnoreCase(vs) && localPluginVersion.equalsIgnoreCase("v4.14.5")) {
                                    outdated = true;
                                    posted_version = vs;
                                }
                                break;
                            }
                        }
                    } catch (final IOException | ParseException e) {
                    	e.printStackTrace();
                        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[ChatFeelings] Unable to check for updates. Is your server online?");
                        cancel();
                        return;
                    }
                    if(outdated) {
                        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&r[ChatFeelings] &e&l&nUpdate Available&r&e&l!&r You're running &7v" + localPluginVersion + "&r, while the latest is &av" + posted_version));
                        cancel(); //Cancel the runnable as an update has been found.
                    }
                });
            }
        }.runTaskTimer(javaPlugin, 0, CHECK_INTERVAL);
    	}catch(Exception err) {
            if(!Main.reducemsgs) {
                javaPlugin.getLogger().warning("Error. There was a problem checking for updates.");
            }
            if(Main.debug()) {
                err.printStackTrace();
            }
    	}
    }

	public static boolean isOutdated() {
		return outdated;
	}
	
	public static String getPostedVersion() {
		return posted_version;
	}
}