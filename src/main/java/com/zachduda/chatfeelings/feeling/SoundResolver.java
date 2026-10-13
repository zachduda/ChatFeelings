package com.zachduda.chatfeelings.feeling;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Turns a sound name from a feeling file into a {@link Sound}. Accepts the namespaced key in any case
 * ("entity.cat.purreow", "ENTITY.CAT.PURREOW", "minecraft:entity.cat.purreow") as well as the enum-style
 * name from the Bukkit Javadocs ("ENTITY_CAT_PURREOW"), including multi-word names such as
 * "ENTITY_IRON_GOLEM_ATTACK".
 */
public final class SoundResolver {
    private volatile Map<String, Sound> byNormalizedName;

    public static boolean isSilent(String name) {
        return name == null || name.isBlank() || name.equalsIgnoreCase("none")
                || name.equalsIgnoreCase("off") || name.equalsIgnoreCase("null");
    }

    /** @return the sound, or null if the name is "None"/blank or isn't a sound on this server. */
    public Sound resolve(String name) {
        if (isSilent(name)) {
            return null;
        }
        final String trimmed = name.trim().toLowerCase(Locale.ROOT);
        final NamespacedKey key = NamespacedKey.fromString(trimmed);
        if (key != null) {
            final Sound exact = Registry.SOUNDS.get(key);
            if (exact != null) {
                return exact;
            }
        }
        return index().get(normalize(trimmed));
    }

    public boolean exists(String name) {
        return resolve(name) != null;
    }

    /** "minecraft:entity.cat.purreow", "ENTITY_CAT_PURREOW" and "Entity.Cat.Purreow" all become "entity_cat_purreow". */
    static String normalize(String name) {
        String n = name.trim().toLowerCase(Locale.ROOT);
        if (n.startsWith("minecraft:")) {
            n = n.substring("minecraft:".length());
        }
        return n.replace('.', '_').replace(' ', '_');
    }

    private Map<String, Sound> index() {
        Map<String, Sound> map = byNormalizedName;
        if (map == null) {
            map = new HashMap<>();
            for (Sound sound : Registry.SOUNDS) {
                // Call getKey() through Keyed: Sound is an enum on older servers and an interface on newer
                // ones, and only the Keyed call site links on both.
                final Keyed keyed = sound;
                map.putIfAbsent(normalize(keyed.getKey().getKey()), sound);
            }
            byNormalizedName = map;
        }
        return map;
    }
}
