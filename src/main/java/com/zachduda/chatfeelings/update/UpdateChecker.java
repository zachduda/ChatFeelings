package com.zachduda.chatfeelings.update;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zachduda.chatfeelings.platform.Scheduler;
import com.zachduda.chatfeelings.util.Log;
import com.zachduda.chatfeelings.util.Versions;
import space.arim.morepaperlib.scheduling.ScheduledTask;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/** Checks GitHub once a day for a newer release, and remembers it for the admin join message. */
public final class UpdateChecker {
    private static final URI LATEST_RELEASE = URI.create("https://api.github.com/repos/zachduda/ChatFeelings/releases/latest");

    private final String localVersion;
    private final Scheduler scheduler;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private volatile String postedVersion = "???";
    private volatile boolean outdated = false;
    private volatile boolean reportedFailure = false;
    private volatile ScheduledTask task;

    public UpdateChecker(String localVersion, Scheduler scheduler) {
        this.localVersion = localVersion;
        this.scheduler = scheduler;
    }

    public void start() {
        try {
            task = scheduler.asyncRepeating(this::check, Duration.ofSeconds(1), Duration.ofHours(24));
        } catch (RuntimeException err) {
            Log.warn("Update Check Failed", err);
        }
    }

    public void stop() {
        final ScheduledTask t = task;
        if (t != null) {
            t.cancel();
        }
    }

    public boolean isOutdated() {
        return outdated;
    }

    public String getPostedVersion() {
        return postedVersion;
    }

    private void check() {
        if (outdated) {
            return;
        }
        try {
            final HttpRequest request = HttpRequest.newBuilder(LATEST_RELEASE)
                    .timeout(Duration.ofSeconds(15))
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "ChatFeelings/" + localVersion)
                    .GET()
                    .build();
            final HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                failed("GitHub answered HTTP " + response.statusCode());
                return;
            }
            final JsonObject release = JsonParser.parseString(response.body()).getAsJsonObject();
            final JsonElement tag = release.get("tag_name");
            if (tag == null || tag.isJsonNull()) {
                failed("unexpected response");
                return;
            }
            final String latest = tag.getAsString().replaceFirst("^[vV]", "");
            if (Versions.compare(localVersion, latest) < 0) {
                postedVersion = latest;
                outdated = true;
                Log.notice("Update Available: Download @ zduda.dev/cf  (" + localVersion + " —> " + latest + ")");
                stop();
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        } catch (IOException | RuntimeException err) {
            failed(err.getClass().getSimpleName() + ": " + err.getMessage());
        }
    }

    /** Logged once; the check keeps retrying daily in case the server was just offline. */
    private void failed(String reason) {
        Log.debug("Update check failed: " + reason);
        if (!reportedFailure) {
            reportedFailure = true;
            Log.warn("Unable to check for updates. Is your server online? (" + reason + ")");
        }
    }
}
