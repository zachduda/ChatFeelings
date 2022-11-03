package com.zachduda.chatfeelings.other;

import com.zachduda.chatfeelings.Main;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileNotFoundException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Supports {

    private final JavaPlugin javaPlugin;

    static boolean supported;

    public Supports(final JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
    }

    public void fetch() {
        Bukkit.getScheduler().runTaskAsynchronously(javaPlugin, () -> {
            try {
                Logger l = javaPlugin.getLogger();
                final String dottedver = getMCVersion();
                final String this_version = getMCVersion("_");
                JSONParser reader = new JSONParser();
                JSONObject json = new JSONObject((JSONObject)reader.parse(IOUtils.toString(new URL("https://raw.githubusercontent.com/zachduda/ChatFeelings/master/supports/"+javaPlugin.getDescription().getVersion().replaceAll("\\.", "_")+".json").openStream(), StandardCharsets.UTF_8)));
                JSONObject versions = (JSONObject) json.get("Versions");
                if(versions.get(this_version) != null) {
                    final String support = versions.get(this_version).toString();
                    if(support.equalsIgnoreCase("full")) {
                        supported = true;
                        return;
                    } else if(support.equalsIgnoreCase("partial")) {
                        l.info(ChatColor.YELLOW + "[ChatFeelings] This plugin can work with " + dottedver + ", however it is not officially supported.");
                        return;
                    } else if(support.equalsIgnoreCase("not_tested")) {
                        l.info(ChatColor.YELLOW + "[ChatFeelings] Heads Up! This plugin hasn't been fully tested with " + dottedver + " yet!");
                        return;
                    }
                }
                if (!supported) {
                    l.info("---------------------------------------------------");
                    l.info("This version of ChatFeelings is only compatible with: "+json.get("Latest")+"-"+json.get("Oldest"));
                    l.info("While ChatFeelings may work with " + dottedver + ", it is not supported.");
                    l.info(" ");
                    l.info("If you continue, you understand that you will get no support, and");
                    l.info("that some features, such as sounds, may disable to continue working.");
                    l.info("");
                    l.info("");
                    l.info("[!] IF YOU GET BUGS/ERRORS, DO NOT REPORT THEM.");
                    l.info("---------------------------------------------------");
                }
                supported = false;
            } catch (final Exception e) {
                if(e instanceof FileNotFoundException) {
                    Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "[ChatFeelings] Couldn't find the support file for this version within the repository.");
                } else {
                    Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "[ChatFeelings] Unable to check repository for version support:");
                    e.printStackTrace();
                }
            } finally {
                Main.updateConfig(javaPlugin, supported);
                Main.updateConfigHeaders(javaPlugin, supported);
            }
        });
    }

    public static boolean isSupported() {
        return supported;
    }

    static boolean invalidver = false;
    public static String getMCVersion(String separator) {
        String this_ver = Bukkit.getBukkitVersion().toUpperCase();
        if(separator == null) { separator = "."; }
        final Pattern versionPattern = Pattern.compile("([1-9]\\d*)\\.(\\d+)\\.(\\d+)(?:-([a-zA-Z0-9]+))?");
        final Matcher version = versionPattern.matcher(this_ver);
        if(!version.find()) {
            if(!invalidver) {
                invalidver = true;
                Bukkit.getLogger().severe("[ChatFeelings] Unable to read Minecraft Version: " + this_ver);
                return this_ver;
            }
            return "X.XX";
        }
        return (version.group(1)+separator+version.group(2));
    }

    public static String getMCVersion() {
        return getMCVersion(".");
    }
}