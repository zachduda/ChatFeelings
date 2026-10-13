package com.zachduda.chatfeelings.util;

import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Minecraft version such as 1.21.4 or 26.1. Comparisons are done on the whole version, so they keep
 * working with Mojang's year-based numbering (26.1 is newer than 1.21.11).
 */
public record McVersion(int major, int minor, int patch) implements Comparable<McVersion> {
    private static final Pattern VERSION = Pattern.compile("^(\\d+)\\.(\\d+)(?:\\.(\\d+))?");
    private static volatile McVersion current;

    /** Parses strings like "1.21.4-R0.1-SNAPSHOT" or "26.1". Returns null if there is no version at the start. */
    public static McVersion parse(String version) {
        if (version == null) {
            return null;
        }
        Matcher m = VERSION.matcher(version.trim());
        if (!m.find()) {
            return null;
        }
        try {
            int patch = m.group(3) == null ? 0 : Integer.parseInt(m.group(3));
            return new McVersion(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), patch);
        } catch (NumberFormatException tooLong) {
            return null;
        }
    }

    /** The running server's version, or 0.0.0 if it can't be read. */
    public static McVersion current() {
        McVersion v = current;
        if (v == null) {
            v = parse(Bukkit.getBukkitVersion());
            if (v == null) {
                Log.warn("Unable to read the Minecraft version from: " + Bukkit.getBukkitVersion());
                v = new McVersion(0, 0, 0);
            }
            current = v;
        }
        return v;
    }

    public boolean isAtLeast(int major, int minor, int patch) {
        return compareTo(new McVersion(major, minor, patch)) >= 0;
    }

    /** "1.21" style (or with another separator, ie: "1_21" for the support manifest). */
    public String majorMinor(String separator) {
        return major + separator + minor;
    }

    @Override
    public int compareTo(McVersion o) {
        if (major != o.major) {
            return Integer.compare(major, o.major);
        }
        if (minor != o.minor) {
            return Integer.compare(minor, o.minor);
        }
        return Integer.compare(patch, o.patch);
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch;
    }
}
