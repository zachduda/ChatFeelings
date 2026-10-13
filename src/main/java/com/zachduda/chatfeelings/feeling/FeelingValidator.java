package com.zachduda.chatfeelings.feeling;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/** Checks that a custom feeling file is complete and safe to register as a command. */
public final class FeelingValidator {
    /** A feeling's file name (minus ".yml") is its command name, so it has to be a sane command name. */
    static final Pattern VALID_NAME = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_-]{0,31}$");

    /**
     * Names a custom feeling can never take: ChatFeelings' own commands and permission nodes, plus vanilla and
     * server commands.
     * (Vanilla commands are registered after plugins on Spigot, so they can't be detected at startup.)
     * Commands from other plugins are detected when registering instead.
     */
    static final Set<String> RESERVED_NAMES = Set.of(
            // ChatFeelings commands, and its permission nodes (a feeling's node is chatfeelings.<name>)
            "chatfeelings", "cf", "feelings", "emotions",
            "all", "admin", "mute", "ignore", "stats", "bypasscooldowns",
            // Bukkit / Spigot / Paper
            "plugins", "pl", "version", "ver", "about", "icanhasbukkit", "reload", "rl", "timings", "restart",
            "spigot", "tps", "paper", "mspt", "help",
            // Vanilla
            "advancement", "attribute", "ban", "ban-ip", "banlist", "bossbar", "clear", "clone", "damage", "data",
            "datapack", "debug", "defaultgamemode", "deop", "dialog", "difficulty", "effect", "enchant", "execute",
            "experience", "fill", "fillbiome", "forceload", "function", "gamemode", "gamerule", "give", "item",
            "jfr", "kick", "kill", "list", "locate", "loot", "me", "msg", "op", "pardon", "pardon-ip", "particle",
            "perf", "place", "playsound", "publish", "random", "recipe", "return", "ride", "rotate", "save-all",
            "save-off", "save-on", "say", "schedule", "scoreboard", "seed", "setblock", "setidletimeout",
            "setworldspawn", "spawnpoint", "spectate", "spreadplayers", "stop", "stopsound", "summon", "tag",
            "team", "teammsg", "teleport", "tell", "tellraw", "test", "tick", "time", "title", "tm", "tp",
            "transfer", "trigger", "w", "waypoint", "weather", "whitelist", "worldborder", "xp");

    private FeelingValidator() {
    }

    public static List<String> validateName(String name) {
        List<String> errors = new ArrayList<>();
        if (!VALID_NAME.matcher(name).matches()) {
            errors.add("Invalid name: must start with a letter and contain only letters, numbers, '-' or '_' (max 32 characters).");
        }
        if (RESERVED_NAMES.contains(name.toLowerCase(Locale.ROOT))) {
            errors.add("'" + name + "' is already used by the server or ChatFeelings, so it can't be used as a feeling name.");
        }
        return errors;
    }

    /**
     * @param soundExists    whether a (non-silent) sound name exists on this server
     * @param particleExists whether a particle preset name is valid
     */
    public static List<String> validate(String name, ConfigurationSection fc,
                                        Predicate<String> soundExists, Predicate<String> particleExists) {
        List<String> errors = new ArrayList<>(validateName(name));

        if (!fc.isBoolean("Enable")) {
            errors.add("Missing or invalid 'Enable' (must be true or false).");
        }
        for (String path : List.of("Msgs.Sender", "Msgs.Target", "Msgs.Global")) {
            final String value = fc.getString(path);
            if (value == null || value.isBlank()) {
                errors.add("Missing or empty '" + path + "'.");
            }
        }
        validateSound(fc, "Sounds.Sound1", soundExists, errors);
        validateSound(fc, "Sounds.Sound2", soundExists, errors);

        if (fc.contains("Particle") && !(fc.isString("Particle") && particleExists.test(fc.getString("Particle")))) {
            errors.add("'Particle' must be None or the name of a built-in feeling whose particles to use (ie: hug).");
        }
        if (fc.contains("Violent") && !fc.isBoolean("Violent")) {
            errors.add("'Violent' must be true or false.");
        }
        if (fc.contains("Description") && !fc.isString("Description")) {
            errors.add("'Description' must be text.");
        }
        if (fc.contains("Stats-Label") && !fc.isString("Stats-Label")) {
            errors.add("'Stats-Label' must be text.");
        }
        return errors;
    }

    private static void validateSound(ConfigurationSection fc, String base, Predicate<String> soundExists,
                                      List<String> errors) {
        final String soundName = fc.getString(base + ".Name");
        if (soundName == null || soundName.isBlank()) {
            errors.add("Missing '" + base + ".Name' (use 'None' if you don't want this sound).");
            return;
        }
        if (SoundResolver.isSilent(soundName)) {
            return; // volume & pitch don't matter for a sound that never plays
        }
        if (!soundExists.test(soundName)) {
            errors.add("'" + base + ".Name' isn't a recognized sound: " + soundName);
        }
        checkRange(fc, base + ".Volume", 0, 10, errors);
        checkRange(fc, base + ".Pitch", 0, 2, errors);
    }

    private static void checkRange(ConfigurationSection fc, String path, double min, double max, List<String> errors) {
        if (!(fc.get(path) instanceof Number)) {
            errors.add("Missing or invalid '" + path + "' (must be a number).");
        } else if (fc.getDouble(path) < min || fc.getDouble(path) > max) {
            errors.add("'" + path + "' must be between " + (int) min + " and " + (int) max + ".");
        }
    }
}
