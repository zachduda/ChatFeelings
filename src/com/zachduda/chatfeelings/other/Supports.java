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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Supports {

    private final String support_v = "4_15_0";
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
                final String dottedver = getMCVersion();
                final String this_version = getMCVersion("_");
                JSONParser reader = new JSONParser();
                JSONObject json = new JSONObject((JSONObject) reader.parse(new InputStreamReader(new URL("https://raw.githubusercontent.com/zachduda/ChatFeelings/master/supports/"
                        + support_v + ".json").openStream(),
                        StandardCharsets.UTF_8)));

                if (!Main.reducemsgs || (json.get("Msg_Critical") != null && ((boolean) json.get("Msg_Critical")))) {
                    if (json.get("Console_Message") != null && json.get("Console_Message") != "") {
                        Main.log((String) json.get("Console_Message"), false, false);
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
                            Main.log(ChatColor.YELLOW + "This plugin can work with " + dottedver + ", however it is not officially supported.", true, true);
                            return;
                        }
                        case "not_tested": {
                            Main.log(ChatColor.YELLOW + "Heads Up! This plugin hasn't been fully tested with " + dottedver + " yet!", true, true);
                            return;
                        }
                    }
                } else {
                    supported = false;
                    // Version wasn't found in supports -> json file. Likely very old or very new. Use longer message to warn
                    Main.log("---------------------------------------------------", false, false);
                    Main.log("This version is designed to work with " + json.get("Latest") + "-" + json.get("Oldest") + ". Expect some issues!", true, true);
                    Main.log("While ChatFeelings may work with " + dottedver + ", it is not supported.", false, true);
                    Main.log("If you continue, you understand that you will get no support, and", false, false);
                    Main.log("that some features, such as sounds, may disable to continue working.", false, false);
                    Main.log("", false, false);
                    Main.log("[!] IF YOU GET BUGS/ERRORS, DO NOT REPORT THEM.", true, false);
                    Main.log("---------------------------------------------------", false, false);
                }
            } catch (final Exception e) {
                if (e instanceof FileNotFoundException) {
                    Main.log(ChatColor.YELLOW + "Unable to find support information on server at (missing " + support_v + ".json)", false, true);
                } else {
                    Main.log(ChatColor.YELLOW + "[ChatFeelings] Unable to check repository for version support.", true, true);
                    if(Main.debug()) {
                        e.printStackTrace();
                    }
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
        String this_ver = Bukkit.getBukkitVersion().toUpperCase().replaceAll("-.+$", "");

        if (!this_ver.matches("\\d+\\.\\d+\\.\\d+")) { // ex: if 1.21, will be 1.21.0
            this_ver += ".0";
        }

        if (separator == null) {
            separator = ".";
        }
        final Pattern versionPattern = Pattern.compile("([1-9]\\d*)\\.(\\d+)\\.(\\d+)(?:-([a-zA-Z0-9]+))?");
        final Matcher version = versionPattern.matcher(this_ver);
        if (!version.find()) {
            if (!invalidver) {
                invalidver = true;
                Main.log(ChatColor.RED + "Unable to read Minecraft Version: " + this_ver, true ,true);
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
