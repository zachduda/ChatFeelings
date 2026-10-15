package com.zachduda.chatfeelings.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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

    @Test
    void zeroSecondCooldownNeverBlocks() {
        final Cooldowns cooldowns = new Cooldowns();
        assertEquals(0, cooldowns.reserveFeeling(player, 1_000, 0));
        assertEquals(0, cooldowns.reserveFeeling(player, 1_000, 0));
    }

    @Test
    void cooldownsArePerPlayerAndPerCommand() {
        final Cooldowns cooldowns = new Cooldowns();
        assertEquals(0, cooldowns.reserveFeeling(player, 1_000, 10_000));
        assertEquals(0, cooldowns.reserveFeeling(UUID.randomUUID(), 1_000, 10_000));
        assertEquals(0, cooldowns.reserveIgnore(player, 1_000, 10_000), "a feeling cooldown doesn't block /cf ignore");
    }

    @Test
    void simultaneousReservationsLetExactlyOneThrough() throws Exception {
        final Cooldowns cooldowns = new Cooldowns();
        final int threads = 16;
        final ExecutorService pool = Executors.newFixedThreadPool(threads);
        final CountDownLatch start = new CountDownLatch(1);
        try {
            final List<Future<Long>> results = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                results.add(pool.submit(() -> {
                    start.await();
                    return cooldowns.reserveFeeling(player, 1_000, 10_000);
                }));
            }
            start.countDown();
            int granted = 0;
            for (Future<Long> result : results) {
                if (result.get(10, TimeUnit.SECONDS) == 0) {
                    granted++;
                }
            }
            assertEquals(1, granted);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void sweepingExpiredEntriesKeepsActiveOnes() {
        final Cooldowns cooldowns = new Cooldowns();
        final List<UUID> active = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            cooldowns.reserveFeeling(UUID.randomUUID(), 0, 1_000);
            final UUID uuid = UUID.randomUUID();
            cooldowns.reserveFeeling(uuid, 0, 60_000);
            active.add(uuid);
        }
        cooldowns.reserveFeeling(UUID.randomUUID(), 2_000, 1_000); // past the threshold, so expired entries are swept
        for (UUID uuid : active) {
            assertTrue(cooldowns.reserveFeeling(uuid, 2_000, 60_000) > 0);
        }
    }
}
