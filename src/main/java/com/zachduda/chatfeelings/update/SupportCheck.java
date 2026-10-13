package com.zachduda.chatfeelings.update;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zachduda.chatfeelings.platform.Scheduler;
import com.zachduda.chatfeelings.util.Log;
import com.zachduda.chatfeelings.util.McVersion;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;

/**
 * Tells admins whether their Minecraft version is supported by this ChatFeelings version, using the
 * supports/&lt;version&gt;.json file in the GitHub repository. It only logs: nothing about how the plugin
 * behaves depends on it, so an offline or firewalled server works exactly the same.
 * <p>
 * Released jars read these files from the master branch, so the supports/ folder must not be moved.
 */
public final class SupportCheck {
    private static final String BASE_URL = "https://raw.githubusercontent.com/zachduda/ChatFeelings/master/supports/";

    private final String pluginVersion;
    private final Scheduler scheduler;

    public SupportCheck(String pluginVersion, Scheduler scheduler) {
        this.pluginVersion = pluginVersion;
        this.scheduler = scheduler;
    }

    /** "5.0.0" -> "5_0_0", "4.15.0b95" -> "4_15_0b95". */
    static String manifestName(String pluginVersion) {
        return pluginVersion.replaceAll("[^0-9A-Za-z]", "_");
    }

    public void run() {
        scheduler.asyncServer(this::check);
    }

    private void check() {
        final String manifest = manifestName(pluginVersion);
        try {
            final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            final HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + manifest + ".json"))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "ChatFeelings/" + pluginVersion)
                    .GET()
                    .build();
            final HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 404) {
                Log.debug("No version support information is published for v" + pluginVersion + " (" + manifest + ".json).");
                return;
            }
            if (response.statusCode() != 200) {
                Log.debug("Version support check answered HTTP " + response.statusCode());
                return;
            }
            report(JsonParser.parseString(response.body()).getAsJsonObject());
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        } catch (IOException | RuntimeException err) {
            Log.debug("Unable to check the repository for version support: " + err);
        }
    }

    private void report(JsonObject json) {
        final boolean critical = json.has("Msg_Critical") && json.get("Msg_Critical").getAsBoolean();
        final String consoleMessage = string(json, "Console_Message");
        if (consoleMessage != null && !consoleMessage.isBlank() && (!Log.isReduced() || critical)) {
            Log.notice(consoleMessage);
        }

        final McVersion version = McVersion.current();
        final String dotted = version.majorMinor(".");
        final JsonObject versions = json.has("Versions") ? json.getAsJsonObject("Versions") : new JsonObject();
        final String status = string(versions, version.majorMinor("_"));
        switch (status == null ? "none" : status.toLowerCase(Locale.ROOT)) {
            case "full" -> Log.debug("Minecraft " + dotted + " is fully supported.");
            case "partial" -> Log.warn("This plugin can work with " + dotted + ", however it is not officially supported.");
            case "not_tested" -> Log.warn("Heads Up! This plugin hasn't been fully tested with " + dotted + " yet!");
            default -> {
                Log.info("---------------------------------------------------");
                Log.warn("This version is designed to work with " + string(json, "Latest") + "-" + string(json, "Oldest")
                        + ". Expect some issues!");
                Log.warn("While ChatFeelings may work with " + dotted + ", it is not supported.");
                Log.info("If you continue, you understand that you will get no support, and");
                Log.info("that some features, such as sounds, may not work.");
                Log.info("");
                Log.notice("[!] IF YOU GET BUGS/ERRORS, DO NOT REPORT THEM.");
                Log.info("---------------------------------------------------");
            }
        }
    }

    private static String string(JsonObject json, String key) {
        final JsonElement element = json.get(key);
        return element == null || element.isJsonNull() ? null : element.getAsString();
    }
}
