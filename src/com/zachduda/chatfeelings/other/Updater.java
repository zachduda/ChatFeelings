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
import space.arim.morepaperlib.scheduling.ScheduledTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

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

    protected ScheduledTask updatetimer;

    public void checkForUpdate() {
        try {
            updatetimer = morePaperLib.scheduling().globalRegionalScheduler().runAtFixedRate(() -> {
                // Run network I/O off the main thread
                morePaperLib.scheduling().asyncScheduler().run(() -> {
                    AtomicBoolean foundOutdated = new AtomicBoolean(false);
                    String foundVersion = null;

                    try {
                        URL url = new URL("https://api.github.com/repos/zachduda/ChatFeelings/releases");
                        try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
                            String str = br.readLine();
                            JSONArray raw_json = (JSONArray) new JSONParser().parse(str);
                            for (JSONObject object : (Iterable<JSONObject>) raw_json) {
                                final String vs = ((String) object.get("tag_name")).replace("v", "");
                                final Boolean prerelease = ((Boolean) object.get("prerelease"));
                                if (!prerelease) {
                                    if (!localPluginVersion.equalsIgnoreCase(vs) && localPluginVersion.equalsIgnoreCase("v4.14.5")) {
                                        foundOutdated.set(true);
                                        foundVersion = vs;
                                    }
                                    break;
                                }
                            }
                        }
                    } catch (final IOException | ParseException e) {
                        if(Main.debug()) {
                            e.printStackTrace();
                        }
                        // Send console message on main thread and cancel repeating task
                        morePaperLib.scheduling().globalRegionalScheduler().run(() -> {
                            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[ChatFeelings] Unable to check for updates. Is your server online?");
                            if(updatetimer != null) {
                                updatetimer.cancel();
                            }
                        });
                        return;
                    }

                    if (foundOutdated.get()) {
                        final String posted = foundVersion;
                        morePaperLib.scheduling().globalRegionalScheduler().run(() -> {
                            Bukkit.getServer().getConsoleSender().sendMessage(
                                    ChatColor.translateAlternateColorCodes('&',
                                            "&r[ChatFeelings] &e&l&nUpdate Available&r&e&l!&r You're running &7v" + localPluginVersion +
                                                    "&r, while the latest is &av" + posted)
                            );
                            if(updatetimer != null) {
                                updatetimer.cancel();
                            }
                        });
                    }
                });
            }, 100000, 100000);
        } catch (Exception err) {
            if (!Main.reducemsgs) {
                javaPlugin.getLogger().warning("Error. There was a problem checking for updates.");
            }
            if (Main.debug()) {
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