package com.zachduda.chatfeelings.other;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zachduda.chatfeelings.Main;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class Supports {

    private final JavaPlugin javaPlugin;

    static boolean supported;
    public Supports(final JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
    }

    public void fetch() {
        Bukkit.getScheduler().runTaskAsynchronously(javaPlugin, () -> {
            try {
                final Pattern versionPattern = Pattern.compile("([1-9]\\d*)\\.(\\d+)\\.(\\d+)(?:-([a-zA-Z0-9]+))?");
                final Matcher version = versionPattern.matcher(Bukkit.getBukkitVersion().toString());

                JSONParser reader = new JSONParser();
                JSONObject json = new JSONObject((JSONObject)reader.parse(IOUtils.toString(new URL("https://raw.githubusercontent.com/zachduda/ChatFeelings/master/supports.json").openStream(), Charset.forName("UTF-8"))));
                if(json.get("Versions."+version.group(1)+"_"+version.group(2)) != null) {
                    if(json.get("Versions."+version.group(1)+"_"+version.group(2)) == "FULL") {
                        supported = true;
                        javaPlugin.getLogger().info("FULL SUPPORT");
                        return;
                    }
                }
                javaPlugin.getLogger().info("NO SUPPORT FOR ");
                supported = false;
            } catch (final Exception e) {
                e.printStackTrace();
                Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "[ChatFeelings] Unable to check repository for version support.");
                return;
            }
        });
    }

    public static boolean isSupported() {
        return supported;
    }
}