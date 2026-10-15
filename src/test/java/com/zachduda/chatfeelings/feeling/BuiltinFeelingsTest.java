package com.zachduda.chatfeelings.feeling;

import com.zachduda.chatfeelings.effect.ParticlePresets;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuiltinFeelingsTest {
    /** A built-in's defaults laid out the way FeelingRegistry writes them to Feelings/&lt;name&gt;.yml. */
    private static YamlConfiguration asFile(BuiltinFeelings.Defaults d) {
        final YamlConfiguration c = new YamlConfiguration();
        c.set("Enable", true);
        c.set("Msgs.Sender", d.sender());
        c.set("Msgs.Target", d.target());
        c.set("Msgs.Global", d.global());
        c.set("Sounds.Sound1.Name", d.sound1());
        c.set("Sounds.Sound1.Volume", d.volume1());
        c.set("Sounds.Sound1.Pitch", d.pitch1());
        c.set("Sounds.Sound2.Name", d.sound2());
        c.set("Sounds.Sound2.Volume", d.volume2());
        c.set("Sounds.Sound2.Pitch", d.pitch2());
        c.set("Particle", d.particle());
        c.set("Violent", d.violent());
        return c;
    }

    @Test
    void defaultsPassTheSameChecksAsCustomFeelings() {
        for (String name : BuiltinFeelings.NAMES) {
            assertEquals(List.of(), FeelingValidator.validate(name, asFile(BuiltinFeelings.get(name)),
                    sound -> true, ParticlePresets::isValid), name);
        }
    }

    /** FeelingService fills in %player% for the sender's and target's messages, %sender%/%target% for the global one. */
    @Test
    void messagesSayWhoTheyAreAbout() {
        for (String name : BuiltinFeelings.NAMES) {
            final BuiltinFeelings.Defaults d = BuiltinFeelings.get(name);
            assertTrue(d.sender().contains("%player%"), name + " sender message");
            assertTrue(d.target().contains("%player%"), name + " target message");
            assertTrue(d.global().contains("%sender%") && d.global().contains("%target%"), name + " global message");
        }
    }

    /** Catches a typo in a default sound, or a sound that's gone from the API version being built against. */
    @Test
    void defaultSoundsExistInTheServerApi() throws Exception {
        final Set<String> apiSounds = new HashSet<>();
        // Only the field names are read: initializing Sound needs a running server.
        for (Field field : Class.forName("org.bukkit.Sound", false, getClass().getClassLoader()).getDeclaredFields()) {
            apiSounds.add(field.getName());
        }
        for (String name : BuiltinFeelings.NAMES) {
            final BuiltinFeelings.Defaults d = BuiltinFeelings.get(name);
            for (String sound : List.of(d.sound1(), d.sound2())) {
                if (SoundResolver.isSilent(sound)) {
                    continue;
                }
                assertTrue(apiSounds.contains(SoundResolver.normalize(sound).toUpperCase(Locale.ROOT)), name + ": " + sound);
                final String fallback = BuiltinFeelings.fallbackSound(sound);
                if (fallback != null && !SoundResolver.isSilent(fallback)) {
                    assertTrue(apiSounds.contains(SoundResolver.normalize(fallback).toUpperCase(Locale.ROOT)),
                            name + " fallback: " + fallback);
                }
            }
        }
    }
}
