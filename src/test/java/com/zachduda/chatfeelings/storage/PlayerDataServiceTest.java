package com.zachduda.chatfeelings.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerDataServiceTest {
    @TempDir
    File dir;

    private final UUID alice = UUID.randomUUID();
    private final UUID bob = UUID.randomUUID();

    @Test
    void cachedDataFollowsWrites() {
        final YamlStorage storage = new YamlStorage(dir);
        final PlayerDataService service = new PlayerDataService(storage);
        service.recordLogin(alice, "Alice", null, 1000L);
        assertNull(service.peek(alice), "nothing cached until loaded");
        service.markOnline(alice);
        assertNotNull(service.loadOnline(alice));

        assertTrue(service.setMuted(alice, true));
        assertTrue(service.peek(alice).isMuted());
        assertEquals(Boolean.TRUE, service.toggleIgnoring(alice, bob));
        assertTrue(service.peek(alice).isIgnoring(bob));
        assertEquals(Boolean.FALSE, service.toggleAllowFeelings(alice));
        assertFalse(service.peek(alice).isAllowingFeelings());

        service.recordFeelingSent(alice, "Hug");
        assertEquals(1, service.peek(alice).getSent("Hug"));
        assertEquals(1, service.peek(alice).getTotalSent());
        assertEquals(1, service.globalSentSnapshot().get("Hug"));

        // The cache agrees with what's on disk.
        final PlayerData stored = storage.load(alice);
        assertTrue(stored.isMuted());
        assertTrue(stored.isIgnoring(bob));
        assertFalse(stored.isAllowingFeelings());
        assertEquals(1, stored.getSent("Hug"));

        service.markOffline(alice);
        assertNull(service.peek(alice));
    }

    @Test
    void lateJoinLoadDoesNotKeepAQuitPlayerCachedAsOnline() {
        final PlayerDataService service = new PlayerDataService(new YamlStorage(dir));
        service.recordLogin(alice, "Alice", null, 1000L);
        service.markOnline(alice);
        service.markOffline(alice);   // quit before the join's background load finished
        service.loadOnline(alice);    // ...which finishes now
        assertFalse(service.isOnline(alice));
        assertNull(service.peek(alice), "nothing is left behind for a player who already left");
    }

    /** A storage that throws StorageException while down[0] is true. */
    private PlayerStorage flaky(YamlStorage real, boolean[] down) {
        return new PlayerStorage() {
            public String getName() { return "flaky"; }
            public PlayerData load(UUID uuid) {
                if (down[0]) {
                    throw new StorageException("database unreachable", null);
                }
                return real.load(uuid);
            }
            public UUID findUUIDByName(String username) { return real.findUUIDByName(username); }
            public void recordLogin(UUID uuid, String username, String ip, long lastOn) { real.recordLogin(uuid, username, ip, lastOn); }
            public boolean setMuted(UUID uuid, boolean muted) { return real.setMuted(uuid, muted); }
            public Boolean toggleAllowFeelings(UUID uuid) { return real.toggleAllowFeelings(uuid); }
            public Boolean toggleIgnoring(UUID owner, UUID target) { return real.toggleIgnoring(owner, target); }
            public void incrementSent(UUID uuid, String feeling) { real.incrementSent(uuid, feeling); }
            public void incrementGlobalSent(String feeling) { real.incrementGlobalSent(feeling); }
            public java.util.Map<String, Integer> getGlobalSent() { return real.getGlobalSent(); }
            public java.util.List<PlayerData> loadAll() { return real.loadAll(); }
            public void delete(UUID uuid) { real.delete(uuid); }
        };
    }

    @Test
    void storageErrorsKeepTheCachedDataInsteadOfForgettingThePlayer() {
        final YamlStorage real = new YamlStorage(dir);
        real.recordLogin(alice, "Alice", null, 1000L);
        real.setMuted(alice, true);
        final boolean[] down = {false};
        final PlayerDataService service = new PlayerDataService(flaky(real, down));
        service.markOnline(alice);
        service.loadOnline(alice);

        down[0] = true;
        final PlayerData duringOutage = service.load(alice);
        assertNotNull(duringOutage, "the cached record is used while storage is down");
        assertTrue(duringOutage.isMuted(), "a muted player stays muted during an outage");
        assertNotNull(service.peek(alice), "the cache isn't cleared by a failed read");
        assertNull(service.get(bob), "an unknown player is still unknown");

        down[0] = false;
        final AtomicInteger runs = new AtomicInteger();
        service.prefetch(bob, task -> runs.incrementAndGet());
        assertEquals(0, runs.get(), "placeholders back off for a few seconds after a storage error");
        assertNotNull(service.load(alice), "direct reads work again as soon as storage is back");
    }

    @Test
    void failedReadIsNotRememberedAsAMissingPlayer() {
        final YamlStorage real = new YamlStorage(dir);
        final boolean[] down = {true};
        final PlayerDataService service = new PlayerDataService(flaky(real, down), 0L);
        assertNull(service.get(bob));

        down[0] = false;
        real.recordLogin(bob, "Bob", null, 1000L);
        final AtomicInteger runs = new AtomicInteger();
        service.prefetch(bob, task -> {
            runs.incrementAndGet();
            task.run();
        });
        assertEquals(1, runs.get(), "once storage is back, placeholders load the player again");
        assertNotNull(service.peek(bob));
    }

    @Test
    void purgeDeleteSkipsPlayersWhoJoinedSinceTheListWasTaken() {
        final YamlStorage storage = new YamlStorage(dir);
        final PlayerDataService service = new PlayerDataService(storage);
        storage.recordLogin(alice, "Alice", null, 1000L);
        storage.recordLogin(bob, "Bob", null, 1000L);
        final PlayerData aliceListed = storage.load(alice);
        final PlayerData bobListed = storage.load(bob);

        storage.recordLogin(alice, "Alice", null, 5000L); // Alice joins mid-purge
        assertFalse(service.deleteIfUnchanged(aliceListed));
        assertNotNull(storage.load(alice));

        service.markOnline(bob); // online players are never purged
        assertFalse(service.deleteIfUnchanged(bobListed));
        service.markOffline(bob);
        assertTrue(service.deleteIfUnchanged(bobListed));
        assertNull(storage.load(bob));
    }

    @Test
    void prefetchLoadsOnceAndRemembersMissingPlayers() {
        final PlayerDataService service = new PlayerDataService(new YamlStorage(dir));
        service.recordLogin(alice, "Alice", null, 1000L);
        final AtomicInteger runs = new AtomicInteger();

        service.prefetch(alice, task -> {
            runs.incrementAndGet();
            task.run();
        });
        assertNotNull(service.peek(alice));
        service.prefetch(alice, task -> runs.incrementAndGet()); // already cached
        assertEquals(1, runs.get());

        service.prefetch(bob, Runnable::run); // no record
        service.prefetch(bob, task -> runs.incrementAndGet()); // recently missing: not retried
        assertEquals(1, runs.get());
        assertNull(service.peek(bob));
    }

    @Test
    void playerDataCopies() {
        final PlayerData base = new PlayerData(alice, "Alice", null, 1L, true, false,
                java.util.Set.of(), java.util.Map.of("Hug", 2), 2);
        assertTrue(base.withMuted(true).isMuted());
        assertFalse(base.withAllowingFeelings(false).isAllowingFeelings());
        assertTrue(base.withIgnoring(bob, true).isIgnoring(bob));
        assertFalse(base.withIgnoring(bob, true).withIgnoring(bob, false).isIgnoring(bob));
        final PlayerData sent = base.withSentIncremented("Hug").withSentIncremented("Slap");
        assertEquals(3, sent.getSent("Hug"));
        assertEquals(1, sent.getSent("Slap"));
        assertEquals(4, sent.getTotalSent());
        assertEquals(2, base.getSent("Hug"), "copies don't change the original");
    }
}
