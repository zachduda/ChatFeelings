package com.zachduda.chatfeelings.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McVersionTest {
    @Test
    void parsesBukkitVersions() {
        assertEquals(new McVersion(1, 21, 4), McVersion.parse("1.21.4-R0.1-SNAPSHOT"));
        assertEquals(new McVersion(26, 1, 0), McVersion.parse("26.1-R0.1-SNAPSHOT"));
        assertEquals(new McVersion(1, 20, 0), McVersion.parse("1.20"));
        assertNull(McVersion.parse("unknown"));
        assertNull(McVersion.parse(null));
    }

    @Test
    void comparesWholeVersions() {
        // The old per-part checks got these wrong: 1.21.0 failed "patch >= 5" and 26.1 failed "minor >= 20".
        assertTrue(new McVersion(1, 21, 0).isAtLeast(1, 20, 5));
        assertTrue(new McVersion(26, 1, 0).isAtLeast(1, 20, 5));
        assertTrue(new McVersion(26, 1, 0).isAtLeast(1, 17, 0));
        assertTrue(new McVersion(1, 20, 5).isAtLeast(1, 20, 5));
        assertFalse(new McVersion(1, 20, 4).isAtLeast(1, 20, 5));
    }

    @Test
    void majorMinor() {
        assertEquals("1_21", new McVersion(1, 21, 4).majorMinor("_"));
        assertEquals("26.1", new McVersion(26, 1, 0).majorMinor("."));
    }
}
