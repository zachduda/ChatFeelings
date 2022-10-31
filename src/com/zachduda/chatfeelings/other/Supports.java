package com.zachduda.chatfeelings.other;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import com.zachduda.chatfeelings.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Supports {

    private final JavaPlugin javaPlugin;

    static boolean supported = false;
    public Supports(final JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
    }

    public void fetch() {
        try {
            Bukkit.getScheduler().runTaskAsynchronously(javaPlugin, () -> {
                try {
                    URL url = new URL("https://raw.githubusercontent.com/zachduda/ChatFeelings/master/supports.json");
                    BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
                    String str = br.readLine();
                    JSONArray rawja = (JSONArray) new JSONParser().parse(str);
                    Iterator iterator = rawja.iterator();
                    while (iterator.hasNext()) {
                        JSONObject jsonObject = (JSONObject) iterator.next();
                        final String vs = ((String) jsonObject.get("Versions." + Main.version));
                        if(vs != null) {
                            javaPlugin.getLogger().info("Found supports.json match: " + vs);
                            break;
                        }
                    }
                    javaPlugin.getLogger().info("LOG: "+ rawja.toString());
                } catch (final IOException | ParseException e) {
                    e.printStackTrace();
                    Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[ChatFeelings] Unable to check repository for version support.");
                    return;
                }
            });
        } catch(Exception err) {
            javaPlugin.getLogger().warning("Error. There was a problem checking for supported versions.");
        }
    }

    public static boolean isSupported() {
        return supported;
    }
}