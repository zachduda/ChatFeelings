package com.zachduda.chatfeelings.other;

import com.zachduda.chatfeelings.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import space.arim.morepaperlib.MorePaperLib;

import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Supports {

    private final String support_v = "4_14_0";
    private final JavaPlugin javaPlugin;
    private final MorePaperLib morePaperLib;
    private static int mcMajorVersion = 0; // ex: the 1 in 1.20.5
    private static int mcMinorVersion = 0; // ex: the 20 in 1.20.5
    private static int mcPatchVersion = 0; // ex: the 5 in 1.20.5

    // 0 = Supports not run yet • 1 = Prior to 1.20.5 • 2 = 1.20.5 or later
    private static int particleVersion = 0;

    static boolean supported;

    public Supports(final JavaPlugin javaPlugin, final MorePaperLib morePaperLib) {
        this.javaPlugin = javaPlugin;
        this.morePaperLib = morePaperLib;
    }

    public void fetch() {
        morePaperLib.scheduling().asyncScheduler().run(() -> {
            try {
                Logger l = javaPlugin.getLogger();
                final String dottedver = getMCVersion();
                final String this_version = getMCVersion("_");
                JSONParser reader = new JSONParser();
                JSONObject json = new JSONObject((JSONObject) reader.parse(new InputStreamReader(new URL("https://raw.githubusercontent.com/zachduda/ChatFeelings/master/supports/"
                        + support_v + ".json").openStream(),
                        StandardCharsets.UTF_8)));

                if (!Main.reducemsgs || (json.get("Msg_Critical") != null && ((boolean) json.get("Msg_Critical")))) {
                    if (json.get("Console_Message") != null && json.get("Console_Message") != "") {
                        l.info((String) json.get("Console_Message"));
                    }
                }

                JSONObject versions = (JSONObject) json.get("Versions");

                if (versions.get(this_version) != null) {
                    final String support = versions.get(this_version).toString().toLowerCase();
                    switch (support) {
                        case "full": {
                            supported = true;
                            return;
                        }
                        case "partial": {
                            l.info(ChatColor.YELLOW + "[ChatFeelings] This plugin can work with " + dottedver + ", however it is not officially supported.");
                            return;
                        }
                        case "not_tested": {
                            l.info(ChatColor.YELLOW + "[ChatFeelings] Heads Up! This plugin hasn't been fully tested with " + dottedver + " yet!");
                            return;
                        }
                    }
                } // else this for any version not specifically listed
                if (!supported) {
                    if (Main.reducemsgs) {
                        l.info("This version of ChatFeelings is made for " + json.get("Latest") + "-" + json.get("Oldest") + " o");
                    } else {
                        l.info("---------------------------------------------------");
                        l.info("This version of ChatFeelings is only compatible with: " + json.get("Latest") + "-" + json.get("Oldest"));
                        l.info("While ChatFeelings may work with " + dottedver + ", it is not supported.");
                        l.info(" ");
                        l.info("If you continue, you understand that you will get no support, and");
                        l.info("that some features, such as sounds, may disable to continue working.");
                        l.info("");
                        l.info("");
                        l.info("[!] IF YOU GET BUGS/ERRORS, DO NOT REPORT THEM.");
                        l.info("---------------------------------------------------");
                    }
                }
                supported = false;
            } catch (final Exception e) {
                if (e instanceof FileNotFoundException) {
                    Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "[ChatFeelings] Couldn't find the support file for this version within the repository.");
                } else {
                    Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "[ChatFeelings] Unable to check repository for version support:");
                    e.printStackTrace();
                }
            } finally {
                // particle support logic
                if (getMcMajorVersion() >= 1 && getMcMinorVersion() >= 20 && getMcPatchVersion() >= 5) {
                    // If 1.20.5 or newer, set version 2.
                    particleVersion = 2;
                } else {
                    // If less than 1.20.5, version 1 (legacy)
                    particleVersion = 1;
                }
                // end of particle support logic
                Main.updateConfig(javaPlugin);
                Main.updateConfigHeaders(javaPlugin);
            }
        });
    }

    public static boolean isSupported() {
        return supported;
    }

    static boolean invalidver = false;

    public static String getMCVersion(String separator) {
        String this_ver = Bukkit.getBukkitVersion().toUpperCase();
        if (separator == null) {
            separator = ".";
        }
        final Pattern versionPattern = Pattern.compile("([1-9]\\d*)\\.(\\d+)\\.(\\d+)(?:-([a-zA-Z0-9]+))?");
        final Matcher version = versionPattern.matcher(this_ver);
        if (!version.find()) {
            if (!invalidver) {
                invalidver = true;
                Bukkit.getLogger().severe("[ChatFeelings] Unable to read Minecraft Version: " + this_ver);
                return this_ver;
            }
            return "X.XX";
        }
        mcMajorVersion = Integer.parseInt(version.group(1));
        mcMinorVersion = Integer.parseInt(version.group(2));
        mcPatchVersion = Integer.parseInt(version.group(3));
        Main.debug("Parsed Version -> M:" + mcMajorVersion + " MN: " + mcMinorVersion + " P:" + mcPatchVersion);
        return (version.group(1) + separator + version.group(2));
    }

    public static String getMCVersion() {
        return getMCVersion(".");
    }

    public static int getMcMajorVersion() {
        return mcMajorVersion;
    }

    public static int getMcMinorVersion() {
        return mcMinorVersion;
    }

    public static int getMcPatchVersion() {
        return mcPatchVersion;
    }

    public static int getParticleVersion() {
        return particleVersion;
    }
}
