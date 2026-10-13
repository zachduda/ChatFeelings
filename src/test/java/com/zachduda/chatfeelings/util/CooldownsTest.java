package com.zachduda.chatfeelings.util;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CooldownsTest {
    private final UUID player = UUID.randomUUID();

    @Test
    void secondReservationIsRejectedWhileActive() {
        final Cooldowns cooldowns = new Cooldowns();
        assertEquals(0, cooldowns.reserveFeeling(player, 1_000, 10_000));
        assertEquals(9_000, cooldowns.reserveFeeling(player, 2_000, 10_000));
    }

    @Test
    void reservationExpires() {
        final Cooldowns cooldowns = new Cooldowns();
        assertEquals(0, cooldowns.reserveFeeling(player, 1_000, 10_000));
        assertEquals(0, cooldowns.reserveFeeling(player, 11_000, 10_000));
    }

    @Test
    void cancelledReservationFreesTheCooldown() {
        final Cooldowns cooldowns = new Cooldowns();
        assertEquals(0, cooldowns.reserveFeeling(player, 1_000, 10_000));
        cooldowns.cancelFeeling(player, 1_000, 10_000);
        assertEquals(0, cooldowns.reserveFeeling(player, 1_500, 10_000));
    }

    @Test
    void cancellingAStaleReservationDoesNotClearANewerOne() {
        final Cooldowns cooldowns = new Cooldowns();
        assertEquals(0, cooldowns.reserveFeeling(player, 1_000, 10_000));
        assertEquals(0, cooldowns.reserveFeeling(player, 20_000, 10_000));
        cooldowns.cancelFeeling(player, 1_000, 10_000); // the old one
        assertTrue(cooldowns.reserveFeeling(player, 21_000, 10_000) > 0);
    }

    @Test
    void ignoreCooldowns() {
        final Cooldowns cooldowns = new Cooldowns();
        assertEquals(0, cooldowns.reserveIgnore(player, 1_000, 5_000));
        assertTrue(cooldowns.reserveIgnore(player, 2_000, 5_000) > 0, "a second /cf ignore is rejected");
        cooldowns.cancelIgnore(player, 1_000, 5_000);
        assertEquals(0, cooldowns.reserveIgnore(player, 2_000, 5_000), "a typo gives the cooldown back");

        assertFalse(cooldowns.isIgnoreListCooldown(player));
        cooldowns.startIgnoreListCooldown(player, 5);
        assertTrue(cooldowns.isIgnoreListCooldown(player));
    }

    @Test
    void lastOnThrottle() {
        final Cooldowns cooldowns = new Cooldowns();
        assertFalse(cooldowns.lastOnRecentlyUpdated(player));
        cooldowns.markLastOnUpdated(player);
        assertTrue(cooldowns.lastOnRecentlyUpdated(player));
    }
}
