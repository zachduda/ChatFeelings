package com.zachduda.chatfeelings.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextTest {
    @Test
    void translatesLegacyCodes() {
        assertEquals("§aHi §lthere", Text.color("&aHi &Lthere"));
    }

    @Test
    void leavesUnknownAmpersandsAlone() {
        assertEquals("Fish & Chips &z", Text.color("Fish & Chips &z"));
    }

    @Test
    void translatesHexForms() {
        final String expected = "§x§c§3§f§f§9§bHi";
        assertEquals(expected, Text.color("&#c3ff9bHi"));
        assertEquals(expected, Text.color("#c3ff9bHi"));
        assertEquals(expected, Text.color("&xc3ff9bHi"));
        assertEquals(expected, Text.color("&#C3FF9BHi"));
    }

    @Test
    void nullAndEmptyBecomeEmpty() {
        assertEquals("", Text.color(null));
        assertEquals("", Text.color(""));
        assertEquals("", Text.strip(null));
    }

    @Test
    void stripRemovesLegacyAndHexCodes() {
        assertEquals("Steve hugged Alex", Text.strip("&#c3ff9b&lSteve &r&7hugged &2Alex"));
        assertEquals("plain", Text.strip("§aplain"));
    }

    @Test
    void capitalizes() {
        assertEquals("Hug", Text.capitalize("hug"));
        assertEquals("Welcomeback", Text.capitalize("WELCOMEBACK"));
        assertEquals("High-five", Text.capitalize("high-five"));
        assertEquals("Two Words", Text.capitalize("two words"));
    }
}
