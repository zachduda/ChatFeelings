package com.zachduda.chatfeelings.effect;

import com.zachduda.chatfeelings.feeling.BuiltinFeelings;

import java.util.Locale;
import java.util.Set;

/**
 * Names of the particle effects a feeling can use. They're named after the built-in feeling that uses them,
 * and any built-in feeling's name also works, so a custom feeling can say {@code Particle: kiss} to borrow
 * /kiss's hearts. Kept free of Bukkit classes so feeling files can be validated without touching the server.
 */
public final class ParticlePresets {
    public static final String NONE = "none";

    public static final Set<String> ALL = Set.of(
            "hug", "punch", "poke", "highfive", "facepalm", "yell", "bite", "shake", "murder",
            "boi", "dab", "cry", "lick", "wave", "welcomeback");

    private ParticlePresets() {
    }

    /**
     * @return the preset name, {@link #NONE} for no particles (also for a missing value), or null if the value
     * isn't recognized.
     */
    public static String normalize(String configured) {
        if (configured == null) {
            return NONE;
        }
        final String value = configured.trim().toLowerCase(Locale.ROOT);
        if (value.isEmpty() || value.equals(NONE) || value.equals("off")) {
            return NONE;
        }
        if (ALL.contains(value)) {
            return value;
        }
        final BuiltinFeelings.Defaults builtin = BuiltinFeelings.get(value);
        return builtin != null ? normalize(builtin.particle()) : null;
    }

    public static boolean isValid(String configured) {
        return normalize(configured) != null;
    }
}
