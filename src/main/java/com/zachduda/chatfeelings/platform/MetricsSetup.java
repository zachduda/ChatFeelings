package com.zachduda.chatfeelings.platform;

import com.zachduda.chatfeelings.Main;
import com.zachduda.chatfeelings.feeling.BuiltinFeelings;
import com.zachduda.chatfeelings.util.Text;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimpleBarChart;
import org.bstats.charts.SimplePie;

import java.util.HashMap;
import java.util.Map;

/** bStats (https://bstats.org/plugin/bukkit/ChatFeelings/1376). Charts only read in-memory values. */
public final class MetricsSetup {
    private static final int PLUGIN_ID = 1376;

    private MetricsSetup() {
    }

    public static Metrics start(Main plugin) {
        final Metrics metrics = new Metrics(plugin, PLUGIN_ID);
        final String platform = platform(plugin.getScheduler());
        metrics.addCustomChart(new SimplePie("server_version", () -> platform));
        metrics.addCustomChart(new SimplePie("update_notifications",
                () -> plugin.getSettings().checkForUpdates() ? "Enabled" : "Disabled"));
        // Only built-in feelings are reported: custom feeling names are admin-chosen text.
        metrics.addCustomChart(new SimpleBarChart("feeling_usage", () -> {
            final Map<String, Integer> sent = plugin.getPlayerData().globalSentSnapshot();
            final Map<String, Integer> usage = new HashMap<>();
            for (String name : BuiltinFeelings.NAMES) {
                final String key = Text.capitalize(name);
                usage.put(key, sent.getOrDefault(key, 0));
            }
            return usage;
        }));
        return metrics;
    }

    private static String platform(Scheduler scheduler) {
        if (scheduler.isFolia()) {
            return "Folia";
        }
        // Paper moved its config class in 1.19; check both so modern Paper isn't counted as Spigot.
        if (classExists("io.papermc.paper.configuration.Configuration") || classExists("com.destroystokyo.paper.PaperConfig")) {
            return "Paper";
        }
        if (classExists("org.spigotmc.SpigotConfig")) {
            return "Spigot";
        }
        return "Bukkit / Other";
    }

    private static boolean classExists(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }
}
