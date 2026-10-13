package com.zachduda.chatfeelings.feeling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Defaults for the feelings ChatFeelings ships with. They're written into Feelings/&lt;name&gt;.yml without
 * touching anything an admin has already changed. Each one also needs a command in plugin.yml.
 */
public final class BuiltinFeelings {

    /** @param particle preset name (see ParticleEffects), or "None" */
    public record Defaults(String name, String sender, String target, String global,
                           String sound1, double volume1, double pitch1,
                           String sound2, double volume2, double pitch2,
                           String particle, boolean violent, String statsLabel) {
    }

    /**
     * Files earlier versions created for feelings that aren't released yet. They're skipped silently so they
     * don't get loaded as (broken) custom feelings.
     */
    public static final Set<String> RESERVED_UNRELEASED = Set.of("knock");

    private static final Map<String, Defaults> BY_NAME = new LinkedHashMap<>();
    public static final List<String> NAMES;

    static {
        add("hug", "&7You give &#c3ff9b&l%player% &r&7a warm hug. &#FF8C6BAwww &4❤",
                "&#c3ff9b&l%player% &r&7gives you a warm hug. &#FF8C6BAwww &4❤",
                "&#c3ff9b&l%sender% &r&7gave &2&l%target% &r&7a warm hug. &#FF8C6BAwww &4❤",
                "ENTITY.CAT.PURREOW", 2.0, 2.0, "hug", false, "Hugs");
        add("slap", "&7You slap &#FF8C6B&l%player% &r&7with some spaghetti.",
                "&#FF8C6B&l%player% &r&7slaps you with some spaghetti.",
                "&#FF8C6B&l%target% &r&7was slapped by &4&l%sender%&r&7.",
                "ENTITY.BLAZE.HURT", 2.0, 0.7, "punch", true, "Slaps");
        add("poke", "&7You poked &#f4fcab&l%player%&7. Maybe they're on vacation?",
                "&#f4fcab&l%player% &r&7has poked you. Anyone there?",
                "&#f4fcab&l%target% &r&7was poked by &6&l%sender%&r&7. &7&oAnyone home?",
                "ENTITY.CHICKEN.EGG", 2.0, 0.1, "poke", false, "Pokes");
        add("highfive", "&7You give a mighty highfive to &#c3ff9b&l%player%&7.",
                "&#c3ff9b&l%player% &7gives you a mighty highfive.",
                "&#c3ff9b&l%sender% &7gives &2&l%target% &r&7a mighty highfive.",
                "ENTITY.VILLAGER.YES", 2.0, 1.0, "highfive", false, "Highfives");
        add("facepalm", "&7You facepalm at what &#f4fcab&l%player% &r&7just said.",
                "&#f4fcab&l%player% &r&7facepalmed at what you just said.",
                "&#f4fcab&l%sender% &r&7facepalms at &6&l%target%&r&7 for being dumb.",
                "ENTITY.VILLAGER.NO", 2.0, 1.0, "facepalm", false, "Facepalms");
        add("yell", "&7You yell at &#FF8C6B&l%player%&r&7 at the top of your lungs.",
                "&#FF8C6B&l%player% &r&7yells at you from the top of their lungs.",
                "&#FF8C6B&l%sender% &r&7yells right at &4&l%target% &r&7from the top of their lungs.",
                "ENTITY.GHAST.SCREAM", 2.0, 1.0, "yell", false, "Yells");
        add("bite", "&7You sink your teeth into &#FF8C6B&l%player%&r&7's skin.",
                "&#FF8C6B&l%player% &r&7sinks their teeth into your skin.",
                "&#FF8C6B&l%sender% &r&7sank their teeth into &4&l%target%&r&7's skin",
                "ENTITY.ZOMBIE.ATTACK_WOODEN_DOOR", 2.0, 2.0, "bite", true, "Bites");
        add("snuggle", "&7You snuggle &#c3ff9b&l%player% &r&7with love. &#FF8C6BAwww &4❤",
                "&#c3ff9b&l%player% &r&7snuggles you with love. &#FF8C6BAwww &4❤",
                "&#c3ff9b&l%sender% &r&7snuggles &2&l%target% &r&7them with hugs. &#FF8C6BAwww &4❤",
                "ENTITY.CAT.PURR", 2.0, 1.0, "hug", false, "Snuggles");
        add("shake", "&7You shake &#FF8C6B&l%player%&r&7's entire body.",
                "&#FF8C6B&l%player% &r&7shakes your entire body.",
                "&#FF8C6B&l%sender% &r&7picks up &4&l%target%&r&7's body, and shakes it.",
                "ENTITY.WOLF.SHAKE", 2.0, 0.7, "shake", true, "Shakes");
        add("stab", "&7You stab &#FF8C6B&l%player% &r&7with a knife. Got Bandaids?",
                "&#FF8C6B&l%player% &r&7grabs a knife and stabs you. Got Bandaids?",
                "&#FF8C6B&l%sender% &r&7grabs a knife and stabs &4&l%target%&r&7.",
                "ENTITY.GENERIC.HURT", 2.0, 0.7, "murder", true, "Stabs");
        add("kiss", "&7You give &#c3ff9b&l%player% &r&7a kiss. &#FF8C6BAwww &4❤",
                "&#c3ff9b&l%player% &r&7gives you a kiss. &#FF8C6BAwww &4❤",
                "&#c3ff9b&l%sender% &7gives &2&l%target% &7a kiss. &#FF8C6BAwww &4❤",
                "ENTITY.ARROW.HIT_PLAYER", 2.0, 1.0, "hug", false, "Kisses");
        add("punch", "&7You strike &#FF8C6B&l%player% &r&7with a punch. Ouch!",
                "&#FF8C6B&l%player% &r&7strikes you with a punch. Ouch!",
                "&#FF8C6B&l%sender% &r&7punched &4&l%target% &r&7right in the face.",
                "ENTITY.IRON_GOLEM.ATTACK", 2.0, 0.6, "punch", true, "Punches");
        add("murder", "&7You murder &#FF8C6B&l%player% &r&7and have no regrets.",
                "&#FF8C6B&l%player% &r&7just murdered you. Bandaid anyone?",
                "&#FF8C6B&l%sender% &r&7just murdered &4&l%target%&r&7. &7&lRIP",
                "ENTITY.BLAZE.DEATH", 1.0, 0.7, "murder", true, "Murders");
        add("cry", "&7You cry on &b&l%player%&r&7's shoulder.",
                "&b&l%player% &r&7cries on your shoulder.",
                "&b&l%sender% &r&7leans on &3&l%target%&r&7's shoulder and cries.",
                "ENTITY.GHAST.DEATH", 1.0, 0.8, "cry", false, "Cries");
        add("boi", "&7You inhale at &#f4fcab&l%player%&r&7... &6&lBOI",
                "&#f4fcab&l%player% &r&7inhales at you... &6&lBOI",
                "&#f4fcab&l%sender% &r&7inhales at &6&l%target%&r&7... &6&l&oBOI",
                "ENTITY.CHICKEN.EGG", 2.0, 0.1, "boi", false, "Bois");
        add("dab", "&7You freshly dab on &#c3ff9b&l%player%&r&7... &7&oGot'em.",
                "&#c3ff9b&l%player% &r&7freshly dabs on you... &7&oGot'em.",
                "&#c3ff9b&l%sender% &r&7freshly dabs on &2&l%target%&r&7... &7&oGot'em.",
                "ENTITY.CHICKEN.EGG", 2.0, 0.1, "dab", false, "Dabs");
        add("lick", "&7You lick &#f4fcab&l%player% &7like ice-cream. &6Gross!",
                "&#f4fcab&l%player% &r&7licks you like ice-cream. &6Gross!",
                "&#f4fcab&l%target% &r&7got licked by &6&l%sender%&r&7. &8Gross.",
                "ENTITY.GENERIC.DRINK", 2.0, 0.1, "lick", false, "Licks");
        add("scorn", "&7You scorn &#FF8C6B&l%player% &r&7for what they've done.",
                "&#FF8C6B&l%player% &r&7scorns you for what you've done.",
                "&#FF8C6B&l%sender% &r&7scorns &4&l%target% &r&7for what they've done.",
                "ENTITY.ENDERMAN.STARE", 2.0, 0.8, "None", false, "Scorns");
        add("pat", "&7You gently pat &#c3ff9b&l%player%&r&7's head for being good.",
                "&#c3ff9b&l%player% &r&7gently pats your head for being good.",
                "&#c3ff9b&l%sender% &r&7gently pats &2&l%target%&r&7's head for being good.",
                "ENTITY.WOLF.PANT", 2.0, 0.8, "hug", false, "Pats");
        add("stalk", "&7You carefully stalk &#f4fcab&l%player%&r&7, &7&oHeh Heh.",
                "&#f4fcab&l%player% &r&7stalks you from a nearby tree.",
                "&#f4fcab&l%sender% &r&7stalks &6&l%target% &r&7from a nearby tree.",
                "AMBIENT.CAVE", 2.0, 2.0, "None", false, "Stalks");
        // Sus and Wave use sounds that only exist on newer versions; FeelingRegistry swaps in the fallbacks
        // (AMBIENT.CAVE / None and BLOCK.NOTE_BLOCK.BELL) when writing a new file on an older server.
        add("sus", "&7You look at &#f4fcab&l%player%&r&7's single-boned body in suspicion.",
                "&#f4fcab&l%player% &r&7suspiciously looks at your single-boned body.",
                "&#f4fcab&l%sender% &r&7looks at &6&l%target% &r&7in single-boned suspicion.",
                "AMBIENT.NETHER_WASTES.MOOD", 2.0, 1.2, "BLOCK.RESPAWN_ANCHOR.DEPLETE", 0.25, 0.1,
                "None", false, "Sus");
        add("wave", "&7You wave adieu to &#c3ff9b&l%player%&r&7!",
                "&#c3ff9b&l%player% &r&7waves adieu to you.",
                "&#c3ff9b&l%sender% &r&7waves adieu to &2&l%target%.",
                "BLOCK.AMETHYST_BLOCK.RESONATE", 2.0, 2.0, "wave", false, "Waves");
        add("welcomeback", "&7You told &#c3ff9b&l%player%&r &7welcome back!",
                "&#c3ff9b&l%player% &r&7gave you a warm welcome back!",
                "&#c3ff9b&l%sender% &r&7welcomed &2&l%target% &r&7back.",
                "BLOCK.BEACON.POWER_SELECT", 2.0, 2.0, "welcomeback", false, "Welcomes");
        add("boop", "&7You boop &#c3ff9b&l%player%&7 right on their nose!",
                "&#c3ff9b&l%player% &r&7boops you right on your nose!",
                "&#c3ff9b&l%target% &r&7was booped on their nose by &#c3ff9b&l%sender%&r&7!",
                "ENTITY.CHICKEN.EGG", 2.0, 2.0, "dab", false, "Boops");
        NAMES = Collections.unmodifiableList(new ArrayList<>(BY_NAME.keySet()));
    }

    private BuiltinFeelings() {
    }

    public static boolean isBuiltin(String name) {
        return BY_NAME.containsKey(name);
    }

    public static Defaults get(String name) {
        return BY_NAME.get(name);
    }

    /** Fallback sounds for built-ins whose default sound is too new for older servers. */
    static String fallbackSound(String soundName) {
        return switch (soundName) {
            case "AMBIENT.NETHER_WASTES.MOOD" -> "AMBIENT.CAVE";
            case "BLOCK.RESPAWN_ANCHOR.DEPLETE" -> "None";
            case "BLOCK.AMETHYST_BLOCK.RESONATE" -> "BLOCK.NOTE_BLOCK.BELL";
            default -> null;
        };
    }

    private static void add(String name, String sender, String target, String global,
                            String sound1, double volume1, double pitch1, String particle, boolean violent,
                            String statsLabel) {
        add(name, sender, target, global, sound1, volume1, pitch1, "None", 0.0, 0.0, particle, violent, statsLabel);
    }

    private static void add(String name, String sender, String target, String global,
                            String sound1, double volume1, double pitch1, String sound2, double volume2, double pitch2,
                            String particle, boolean violent, String statsLabel) {
        BY_NAME.put(name, new Defaults(name, sender, target, global, sound1, volume1, pitch1,
                sound2, volume2, pitch2, particle, violent, statsLabel));
    }
}
