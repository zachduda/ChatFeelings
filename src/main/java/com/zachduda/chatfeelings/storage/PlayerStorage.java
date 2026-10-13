package com.zachduda.chatfeelings.storage;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Backend for per-player data (mute status, ignore lists, sent stats) and global feeling stats.
 * <p>
 * Implementations may perform blocking disk or network I/O, so callers should use them
 * from an async task and never from the main/region thread.
 */
public interface PlayerStorage {

    /** Human-readable backend name for logs, ie: "YAML" or "MySQL". */
    String getName();

    /**
     * Loads a player's data, or returns null if they've never been recorded.
     *
     * @throws StorageException if storage can't be read right now (as opposed to the player having no record)
     */
    PlayerData load(UUID uuid);

    /** Case-insensitive username lookup. Returns the most recently seen match, or null. */
    UUID findUUIDByName(String username);

    /**
     * Creates the player's record if it doesn't exist yet (allowing feelings, not muted),
     * otherwise refreshes their username, IP and last-on time without touching anything else.
     */
    void recordLogin(UUID uuid, String username, String ip, long lastOn);

    /** @return false if the player has no record. */
    boolean setMuted(UUID uuid, boolean muted);

    /**
     * Flips the player's "Allow-Feelings" flag.
     *
     * @return the new value, or null if the player has no record.
     */
    Boolean toggleAllowFeelings(UUID uuid);

    /**
     * Adds {@code target} to {@code owner}'s ignore list, or removes it if it's already there.
     *
     * @return true if owner is now ignoring target, false if no longer ignoring, null if owner has no record.
     */
    Boolean toggleIgnoring(UUID owner, UUID target);

    /** Adds one to the player's per-feeling and total sent counters. No-op if the player has no record. */
    void incrementSent(UUID uuid, String capitalizedFeeling);

    /** Adds one to the server-wide counter for a feeling. */
    void incrementGlobalSent(String capitalizedFeeling);

    /** Server-wide sent counters, keyed by capitalized feeling name. */
    Map<String, Integer> getGlobalSent();

    /** Every valid player record, complete with ignore lists and stats. Can be slow. */
    List<PlayerData> loadAll();

    /**
     * Every valid player record, but backends may leave ignore lists and stats empty to save work. Enough for
     * purging old data and listing mutes, which only need the name, IP, last-on time and mute flag.
     */
    default List<PlayerData> listAll() {
        return loadAll();
    }

    /** Removes everything stored for a player. */
    void delete(UUID uuid);

    /**
     * Removes a player only if their last-on time is still {@code expectedLastOn}, so a player who joined after
     * a purge listed them keeps their record. Backends should do the check and delete atomically.
     *
     * @return true if the player was removed
     */
    default boolean deleteIfUnchanged(UUID uuid, long expectedLastOn) {
        final PlayerData current = load(uuid);
        if (current == null || current.getLastOn() != expectedLastOn) {
            return false;
        }
        delete(uuid);
        return true;
    }

    /** Removes records that are unreadable or missing required fields. @return how many were removed. */
    default int cleanupInvalid() {
        return 0;
    }

    /** Releases connections/handles. The storage must not be used afterwards. */
    default void close() {
    }
}
