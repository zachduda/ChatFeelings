package com.zachduda.chatfeelings.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ParticlePresetsTest {
    @Test
    void acceptsPresetsAndBuiltinFeelingNames() {
        assertEquals("hug", ParticlePresets.normalize("HUG"));
        assertEquals("hug", ParticlePresets.normalize("kiss"));   // /kiss uses /hug's hearts
        assertEquals("punch", ParticlePresets.normalize("slap"));
        assertEquals("murder", ParticlePresets.normalize("stab"));
        assertEquals(ParticlePresets.NONE, ParticlePresets.normalize("scorn"));
    }

    @Test
    void noneAndMissing() {
        assertEquals(ParticlePresets.NONE, ParticlePresets.normalize(null));
        assertEquals(ParticlePresets.NONE, ParticlePresets.normalize("None"));
        assertEquals(ParticlePresets.NONE, ParticlePresets.normalize("off"));
        assertNull(ParticlePresets.normalize("sparkles"));
    }
}
