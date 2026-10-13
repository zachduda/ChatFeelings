package com.zachduda.chatfeelings.feeling;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SoundResolverTest {
    @Test
    void normalizesEveryAcceptedSpelling() {
        assertEquals("entity_iron_golem_attack", SoundResolver.normalize("ENTITY_IRON_GOLEM_ATTACK"));
        assertEquals("entity_iron_golem_attack", SoundResolver.normalize("entity.iron_golem.attack"));
        assertEquals("entity_iron_golem_attack", SoundResolver.normalize("minecraft:entity.iron_golem.attack"));
        assertEquals("entity_cat_purreow", SoundResolver.normalize("ENTITY.CAT.PURREOW"));
    }

    @Test
    void silentNames() {
        assertTrue(SoundResolver.isSilent("None"));
        assertTrue(SoundResolver.isSilent("OFF"));
        assertTrue(SoundResolver.isSilent("null"));
        assertTrue(SoundResolver.isSilent(" "));
        assertTrue(SoundResolver.isSilent(null));
        assertFalse(SoundResolver.isSilent("ENTITY.CAT.PURR"));
    }
}
