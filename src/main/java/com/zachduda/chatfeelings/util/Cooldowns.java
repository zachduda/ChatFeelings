package com.zachduda.chatfeelings.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player cooldowns. Each map holds the time (epoch millis) a cooldown ends. Entries are not cleared on
 * quit, so relogging doesn't skip a cooldown; expired entries are swept lazily as the maps grow.
 */
public final class Cooldowns {
    private static final int SWEEP_THRESHOLD = 256;
    private static final long LAST_ON_THROTTLE_MILLIS = 60_000L;

    private final Map<UUID, Long> feelingUntil = new ConcurrentHashMap<>();
    private final Map<UUID, Long> ignoreUntil = new ConcurrentHashMap<>();
    private final Map<UUID, Long> ignoreListUntil = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastOnUpdateUntil = new ConcurrentHashMap<>();

    /**
     * Atomically checks and starts the feeling cooldown, so spamming a command can't slip several feelings
     * through before the first one finishes.
     *
     * @return 0 if the cooldown was free and is now reserved until {@code now + cooldownMillis}, otherwise
     * how many milliseconds are left on the active cooldown.
     */
    public long reserveFeeling(UUID uuid, long now, long cooldownMillis) {
        return reserve(feelingUntil, uuid, now, cooldownMillis);
    }

    /** Gives back a reservation made by {@link #reserveFeeling} when the feeling didn't go through. */
    public void cancelFeeling(UUID uuid, long reservedAt, long cooldownMillis) {
        feelingUntil.remove(uuid, reservedAt + cooldownMillis);
    }

    /** Like {@link #reserveFeeling}, for /cf ignore. */
    public long reserveIgnore(UUID uuid, long now, long cooldownMillis) {
        return reserve(ignoreUntil, uuid, now, cooldownMillis);
    }

    /** Gives back a reservation made by {@link #reserveIgnore}, ie: when the player name was a typo. */
    public void cancelIgnore(UUID uuid, long reservedAt, long cooldownMillis) {
        ignoreUntil.remove(uuid, reservedAt + cooldownMillis);
    }

    public boolean isIgnoreListCooldown(UUID uuid) {
        return active(ignoreListUntil, uuid);
    }

    public void startIgnoreListCooldown(UUID uuid, int seconds) {
        start(ignoreListUntil, uuid, seconds * 1000L);
    }

    /**
     * True if the player's last-on time was written less than a minute ago. Joining and quitting within a
     * minute only writes once.
     */
    public boolean lastOnRecentlyUpdated(UUID uuid) {
        return active(lastOnUpdateUntil, uuid);
    }

    public void markLastOnUpdated(UUID uuid) {
        start(lastOnUpdateUntil, uuid, LAST_ON_THROTTLE_MILLIS);
    }

    public void clear() {
        feelingUntil.clear();
        ignoreUntil.clear();
        ignoreListUntil.clear();
        lastOnUpdateUntil.clear();
    }

    private static long reserve(Map<UUID, Long> map, UUID uuid, long now, long cooldownMillis) {
        final long[] remaining = {0L};
        map.compute(uuid, (k, until) -> {
            if (until != null && until > now) {
                remaining[0] = until - now;
                return until;
            }
            return now + cooldownMillis;
        });
        sweep(map, now);
        return remaining[0];
    }

    private static boolean active(Map<UUID, Long> map, UUID uuid) {
        final Long until = map.get(uuid);
        if (until == null) {
            return false;
        }
        if (until <= System.currentTimeMillis()) {
            map.remove(uuid, until);
            return false;
        }
        return true;
    }

    private static void start(Map<UUID, Long> map, UUID uuid, long millis) {
        final long now = System.currentTimeMillis();
        map.put(uuid, now + millis);
        sweep(map, now);
    }

    private static void sweep(Map<UUID, Long> map, long now) {
        if (map.size() > SWEEP_THRESHOLD) {
            map.values().removeIf(until -> until <= now);
        }
    }
}
