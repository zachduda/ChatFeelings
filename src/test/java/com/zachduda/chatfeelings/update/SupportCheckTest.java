package com.zachduda.chatfeelings.update;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SupportCheckTest {
    @Test
    void manifestNamesMatchTheSupportsFolder() {
        assertEquals("5_0_0", SupportCheck.manifestName("5.0.0"));
        assertEquals("4_15_0b95", SupportCheck.manifestName("4.15.0b95"));
    }
}
