package com.zachduda.chatfeelings.storage;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

/**
 * In-memory copies of player data, so feelings, placeholders and stats don't hit the disk or database each
 * time. Online players are cached from join until quit. Anyone else looked up (ie: by a placeholder) is
 * cached briefly. With a shared database, entries older than {@code maxAgeMillis} are re-read before being
 * trusted, so changes made on another server show up quickly.
 * <p>
 * Who is online is tracked here (set synchronously from the join/quit events) and checked inside the same
 * atomic map update that stores an entry, so a slow load finishing after the player already quit can't leave
 * them cached as online forever.
 */
public final class PlayerDataCache {
    private static final long OFFLINE_EXPIRY_MILLIS = 5 * 60_000L;
    /** Expired offline entries are swept once the cache grows past this many entries. */
    private static final int SWEEP_THRESHOLD = 512;

    private record Entry(PlayerData data, long loadedAt, boolean online) {
    }

    private final Map<UUID, Entry> entries = new ConcurrentHashMap<>();
    private final Set<UUID> online = ConcurrentHashMap.newKeySet();
    private final long maxAgeMillis;

    /** @param maxAgeMillis how long an entry is trusted before blocking reads refresh it; 0 means forever */
    public PlayerDataCache(long maxAgeMillis) {
        this.maxAgeMillis = maxAgeMillis;
    }

    /** Call from the join event (or for players already online on startup). */
    void markOnline(UUID uuid) {
        online.add(uuid);
    }

    /** Call from the quit event: the player stops being online and their entry is dropped. */
    void markOffline(UUID uuid) {
        online.remove(uuid);
        entries.remove(uuid);
    }

    boolean isOnline(UUID uuid) {
        return online.contains(uuid);
    }

    /** Never blocks: the cached data (possibly slightly old), or null if there's nothing usable. */
    public PlayerData peek(UUID uuid) {
        final Entry e = entries.get(uuid);
        if (e == null) {
            return null;
        }
        if (!e.online() && System.currentTimeMillis() - e.loadedAt() > OFFLINE_EXPIRY_MILLIS) {
            entries.remove(uuid, e);
            return null;
        }
        return e.data();
    }

    /** Whether a blocking read should go back to storage for this player. */
    boolean needsRefresh(UUID uuid) {
        final Entry e = entries.get(uuid);
        if (e == null) {
            return true;
        }
        final long age = System.currentTimeMillis() - e.loadedAt();
        return (maxAgeMillis > 0 && age > maxAgeMillis) || (!e.online() && age > OFFLINE_EXPIRY_MILLIS);
    }

    /** Stores freshly loaded data (null removes the entry). Online players are kept until they quit. */
    void put(UUID uuid, PlayerData data) {
        final long now = System.currentTimeMillis();
        // The online check happens inside compute(), so it's ordered against markOffline's remove().
        entries.compute(uuid, (k, old) -> data == null ? null : new Entry(data, now, online.contains(k)));
        sweep(now);
    }

    /**
     * Stores data loaded because the player joined, but only if they're still online: a load that finishes after
     * a quick quit (ie: a bot kicked on join) leaves nothing behind.
     */
    void putIfOnline(UUID uuid, PlayerData data) {
        final long now = System.currentTimeMillis();
        entries.compute(uuid, (k, old) -> !online.contains(k) ? old : (data == null ? null : new Entry(data, now, true)));
    }

    private void sweep(long now) {
        if (entries.size() > SWEEP_THRESHOLD) {
            entries.values().removeIf(e -> !e.online() && now - e.loadedAt() > OFFLINE_EXPIRY_MILLIS);
        }
    }

    /** Applies a change we just wrote to storage, if the player is cached. */
    void update(UUID uuid, UnaryOperator<PlayerData> change) {
        entries.computeIfPresent(uuid, (k, e) -> new Entry(change.apply(e.data()), e.loadedAt(), e.online()));
    }

    public void invalidate(UUID uuid) {
        entries.remove(uuid);
    }

    public void clear() {
        entries.clear();
        online.clear();
    }
}
