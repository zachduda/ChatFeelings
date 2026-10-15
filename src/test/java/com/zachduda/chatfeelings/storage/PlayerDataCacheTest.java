package com.zachduda.chatfeelings.storage;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerDataCacheTest {
    private final UUID alice = UUID.randomUUID();
    private final PlayerData aliceData = new PlayerData(alice, "Alice", null, 1L, true, false, Set.of(), Map.of(), 0);

    @Test
    void storingNullForgetsThePlayer() {
        final PlayerDataCache cache = new PlayerDataCache(0L);
        assertTrue(cache.needsRefresh(alice));
        cache.put(alice, aliceData);
        assertFalse(cache.needsRefresh(alice));
        cache.put(alice, null); // their record was deleted (ie: purged)
        assertNull(cache.peek(alice));
        assertTrue(cache.needsRefresh(alice));
    }

    @Test
    void updatesOnlyApplyToCachedPlayers() {
        final PlayerDataCache cache = new PlayerDataCache(0L);
        cache.update(alice, d -> d.withMuted(true));
        assertNull(cache.peek(alice), "an update doesn't create an entry from nothing");
        cache.put(alice, aliceData);
        cache.update(alice, d -> d.withMuted(true));
        assertTrue(cache.peek(alice).isMuted());
    }

    @Test
    void sharedDatabaseEntriesAreRecheckedButStillServed() throws InterruptedException {
        final PlayerDataCache cache = new PlayerDataCache(1L);
        cache.markOnline(alice);
        cache.put(alice, aliceData);
        Thread.sleep(5);
        assertTrue(cache.needsRefresh(alice), "blocking reads go back to the database for changes from other servers");
        assertNotNull(cache.peek(alice), "non-blocking reads (placeholders) keep using the cached copy");
    }
}
