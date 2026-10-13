package com.zachduda.chatfeelings.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionsTest {
    @Test
    void numericComparison() {
        assertTrue(Versions.compare("5.0.0", "5.0.1") < 0);
        assertTrue(Versions.compare("5.0.10", "5.0.9") > 0);
        assertTrue(Versions.compare("5.1", "5.0.9") > 0);
        assertEquals(0, Versions.compare("5.0", "5.0.0"));
        assertEquals(0, Versions.compare("v5.0.0", "5.0.0"));
    }

    @Test
    void preReleasesAreOlderThanTheirRelease() {
        assertTrue(Versions.compare("5.0.0b3", "5.0.0") < 0);
        assertTrue(Versions.compare("5.0.0", "5.0.0b3") > 0);
        // a newer beta isn't "outdated" compared to an older release
        assertTrue(Versions.compare("5.1.0b1", "5.0.0") > 0);
    }

    @Test
    void garbageNeverCountsAsNewer() {
        assertEquals(0, Versions.compare("???", "5.0.0"));
        assertEquals(0, Versions.compare(null, "5.0.0"));
    }
}
