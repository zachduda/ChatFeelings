package com.zachduda.chatfeelings.platform;

/** Which server software ChatFeelings is running on, for /cf version and metrics. */
public final class ServerPlatform {
    private ServerPlatform() {
    }

    /** "Folia", "Paper" (Paper and its forks), "Spigot" or "Bukkit / Other". */
    public static String name(Scheduler scheduler) {
        if (scheduler.isFolia()) {
            return "Folia";
        }
        // ServerBuildInfo is Paper API (1.20.6+); the other two are older Paper server classes.
        if (classExists("io.papermc.paper.ServerBuildInfo") || classExists("io.papermc.paper.configuration.Configuration")
                || classExists("com.destroystokyo.paper.PaperConfig")) {
            return "Paper";
        }
        if (classExists("org.spigotmc.SpigotConfig")) {
            return "Spigot";
        }
        return "Bukkit / Other";
    }

    private static boolean classExists(String name) {
        try {
            Class.forName(name, false, ServerPlatform.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }
}
