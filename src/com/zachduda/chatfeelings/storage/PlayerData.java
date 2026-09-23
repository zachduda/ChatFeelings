package com.zachduda.chatfeelings.storage;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A snapshot of everything ChatFeelings stores about a single player, independent of the
 * storage backend (YAML files in the Data folder or MySQL tables).
 */
public final class PlayerData {
    private final UUID uuid;
    private final String username;
    private final String ip;
    private final long lastOn;
    private final boolean allowFeelings;
    private final boolean muted;
    private final Set<String> ignoring;
    private final Map<String, Integer> sentStats;
    private final int totalSent;

    public PlayerData(UUID uuid, String username, String ip, long lastOn, boolean allowFeelings, boolean muted,
                      Set<String> ignoring, Map<String, Integer> sentStats, int totalSent) {
        this.uuid = uuid;
        this.username = username;
        this.ip = ip;
        this.lastOn = lastOn;
        this.allowFeelings = allowFeelings;
        this.muted = muted;
        this.ignoring = Collections.unmodifiableSet(new LinkedHashSet<>(ignoring));
        this.sentStats = Collections.unmodifiableMap(new HashMap<>(sentStats));
        this.totalSent = totalSent;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    /** May be null if it was never recorded. */
    public String getIp() {
        return ip;
    }

    /** Epoch millis of the last time this player's data was refreshed on join/quit. */
    public long getLastOn() {
        return lastOn;
    }

    /** False when the player has toggled "/cf ignore all". */
    public boolean isAllowingFeelings() {
        return allowFeelings;
    }

    /** True when muted through ChatFeelings itself (not an external punishment plugin). */
    public boolean isMuted() {
        return muted;
    }

    /** UUID strings of the players this player is ignoring. */
    public Set<String> getIgnoring() {
        return ignoring;
    }

    public boolean isIgnoring(UUID other) {
        return ignoring.contains(other.toString());
    }

    /** Keys are capitalized feeling names (ie: "Hug"), matching the historic YAML layout. */
    public Map<String, Integer> getSentStats() {
        return sentStats;
    }

    public int getSent(String capitalizedFeeling) {
        return sentStats.getOrDefault(capitalizedFeeling, 0);
    }

    public int getTotalSent() {
        return totalSent;
    }
}
