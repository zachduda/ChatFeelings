package com.zachduda.chatfeelings.storage;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * Player data access for the rest of the plugin: storage plus a cache in front of it. Methods documented as
 * blocking may read the disk or database, so call them from a background task.
 */
public final class PlayerDataService {
    /** With MySQL another server may change a player's mute/ignore state, so trust cached data briefly. */
    private static final long SHARED_STORAGE_MAX_AGE_MILLIS = 15_000L;
    private static final long MISSING_RETRY_MILLIS = 5 * 60_000L;
    /** After a storage error, background prefetches (placeholders) wait this long before trying storage again. */
    private static final long UNAVAILABLE_RETRY_MILLIS = 10_000L;

    private final PlayerStorage storage;
    private final PlayerDataCache cache;
    private final long unavailableRetryMillis;
    private final Map<String, Integer> globalSent = new ConcurrentHashMap<>();
    /** Players looked up recently who have no record, so placeholders don't re-read storage for them. */
    private final Map<UUID, Long> knownMissingUntil = new ConcurrentHashMap<>();
    private final Set<UUID> prefetching = ConcurrentHashMap.newKeySet();
    private volatile boolean closed = false;
    private volatile long unavailableUntil = 0L;

    public PlayerDataService(PlayerStorage storage) {
        this(storage, UNAVAILABLE_RETRY_MILLIS);
    }

    /** For tests: lets the back-off after a storage error be shortened. */
    PlayerDataService(PlayerStorage storage, long unavailableRetryMillis) {
        this.storage = storage;
        this.cache = new PlayerDataCache(storage instanceof MySQLStorage ? SHARED_STORAGE_MAX_AGE_MILLIS : 0L);
        this.unavailableRetryMillis = unavailableRetryMillis;
    }

    public PlayerStorage storage() {
        return storage;
    }

    public boolean isSharedDatabase() {
        return storage instanceof MySQLStorage;
    }

    /** Non-blocking: cached data or null. Safe from any thread, including for placeholders. */
    public PlayerData peek(UUID uuid) {
        return cache.peek(uuid);
    }

    /**
     * Blocking: the player's data, from the cache when it's fresh enough, or null if they have no record. If
     * storage can't be read right now, whatever is cached is returned (possibly old) rather than treating the
     * player as unknown.
     */
    public PlayerData get(UUID uuid) {
        if (!cache.needsRefresh(uuid)) {
            final PlayerData cached = cache.peek(uuid);
            if (cached != null) {
                return cached;
            }
        }
        final PlayerData data;
        try {
            data = storage.load(uuid);
        } catch (StorageException unavailable) {
            markUnavailable();
            return cache.peek(uuid);
        }
        cache.put(uuid, data);
        if (data == null) {
            rememberMissing(uuid);
        } else {
            knownMissingUntil.remove(uuid);
        }
        return data;
    }

    private void markUnavailable() {
        unavailableUntil = System.currentTimeMillis() + unavailableRetryMillis;
    }

    private void rememberMissing(UUID uuid) {
        final long now = System.currentTimeMillis();
        knownMissingUntil.put(uuid, now + MISSING_RETRY_MILLIS);
        if (knownMissingUntil.size() > 512) {
            knownMissingUntil.values().removeIf(until -> until <= now);
        }
    }

    /**
     * Non-blocking: starts loading a player into the cache in the background, unless they're already cached,
     * being loaded, were recently found to have no record, or storage failed in the last few seconds (so a
     * scoreboard refreshing every second doesn't hammer an unreachable database). Used by placeholders.
     */
    public void prefetch(UUID uuid, Executor executor) {
        if (cache.peek(uuid) != null || System.currentTimeMillis() < unavailableUntil) {
            return;
        }
        final Long missingUntil = knownMissingUntil.get(uuid);
        if (missingUntil != null && missingUntil > System.currentTimeMillis()) {
            return;
        }
        if (prefetching.add(uuid)) {
            executor.execute(() -> {
                try {
                    get(uuid);
                } finally {
                    prefetching.remove(uuid);
                }
            });
        }
    }

    /**
     * Blocking: always reads storage (for decisions like muting), refreshing the cache if the player is in it.
     * Falls back to cached data if storage can't be read right now.
     */
    public PlayerData load(UUID uuid) {
        final PlayerData data;
        try {
            data = storage.load(uuid);
        } catch (StorageException unavailable) {
            markUnavailable();
            return cache.peek(uuid);
        }
        cache.put(uuid, data);
        return data;
    }

    /** Non-blocking: call from the join event, before any background loading for that player. */
    public void markOnline(UUID uuid) {
        cache.markOnline(uuid);
        knownMissingUntil.remove(uuid);
    }

    /** Non-blocking: call from the quit event. Drops the player from the cache. */
    public void markOffline(UUID uuid) {
        cache.markOffline(uuid);
    }

    public boolean isOnline(UUID uuid) {
        return cache.isOnline(uuid);
    }

    /**
     * Blocking: reads a player into the cache after they join, where they stay until they quit. Nothing is cached
     * if they already left (ie: kicked during join) by the time the read finishes.
     */
    public PlayerData loadOnline(UUID uuid) {
        try {
            final PlayerData data = storage.load(uuid);
            cache.putIfOnline(uuid, data);
            return data;
        } catch (StorageException unavailable) {
            markUnavailable();
            return null; // loaded on demand later instead
        }
    }

    /** Blocking: an online player's UUID, or the most recently seen player with that name. */
    public UUID findUUID(String name) {
        final Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return online.getUniqueId();
        }
        return storage.findUUIDByName(name);
    }

    /** Blocking: the stored username, or null if the player has no record. */
    public String findName(UUID uuid) {
        final PlayerData data = get(uuid);
        return data == null ? null : data.getUsername();
    }

    /** Blocking. Creates the record for new players. */
    public void recordLogin(UUID uuid, String username, String ip, long lastOn) {
        storage.recordLogin(uuid, username, ip, lastOn);
        knownMissingUntil.remove(uuid);
        cache.update(uuid, d -> d.withLogin(username, ip, lastOn));
    }

    /** Blocking. @return false if the player has no record or it couldn't be saved. */
    public boolean setMuted(UUID uuid, boolean muted) {
        final boolean saved = storage.setMuted(uuid, muted);
        if (saved) {
            cache.update(uuid, d -> d.withMuted(muted));
        }
        return saved;
    }

    /** Blocking. @return the new value, or null if the player has no record. */
    public Boolean toggleAllowFeelings(UUID uuid) {
        final Boolean allow = storage.toggleAllowFeelings(uuid);
        if (allow != null) {
            cache.update(uuid, d -> d.withAllowingFeelings(allow));
        }
        return allow;
    }

    /** Blocking. @return true if now ignoring, false if not, null if the owner has no record. */
    public Boolean toggleIgnoring(UUID owner, UUID target) {
        final Boolean ignoring = storage.toggleIgnoring(owner, target);
        if (ignoring != null) {
            cache.update(owner, d -> d.withIgnoring(target, ignoring));
        }
        return ignoring;
    }

    /** Blocking. Counts a sent feeling for the server and, when a player sent it, for them. */
    public void recordFeelingSent(UUID sender, String displayName) {
        storage.incrementGlobalSent(displayName);
        globalSent.merge(displayName, 1, Integer::sum);
        if (sender != null) {
            storage.incrementSent(sender, displayName);
            cache.update(sender, d -> d.withSentIncremented(displayName));
        }
    }

    /** Blocking. Loads the server-wide counters once so metrics can read them without touching storage. */
    public void loadGlobalSent() {
        globalSent.putAll(storage.getGlobalSent());
    }

    /** Non-blocking snapshot of the server-wide counters. */
    public Map<String, Integer> globalSentSnapshot() {
        return Collections.unmodifiableMap(Map.copyOf(globalSent));
    }

    /** Blocking. May leave ignore lists and stats empty (see {@link PlayerStorage#listAll()}). */
    public List<PlayerData> listAll() {
        return storage.listAll();
    }

    /**
     * Blocking. Deletes a player's data only if it's still exactly as listed (same last-on time), so a player
     * who joined since the list was taken keeps their fresh record.
     *
     * @return true if it was deleted
     */
    public boolean deleteIfUnchanged(PlayerData listed) {
        if (cache.isOnline(listed.getUuid())) {
            return false;
        }
        final boolean deleted = storage.deleteIfUnchanged(listed.getUuid(), listed.getLastOn());
        if (deleted) {
            cache.invalidate(listed.getUuid());
        }
        return deleted;
    }

    /** Blocking. */
    public int cleanupInvalid() {
        return storage.cleanupInvalid();
    }

    /** True once {@link #close} has started; long-running loops check this to stop early on shutdown. */
    public boolean isClosed() {
        return closed;
    }

    public void close() {
        closed = true;
        cache.clear();
        storage.close();
    }
}
