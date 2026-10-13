package com.zachduda.chatfeelings.storage;

/**
 * Storage couldn't be read right now (ie: the database is unreachable). Distinct from a player having no record,
 * so callers can keep using data they already have instead of treating the player as unknown.
 */
public class StorageException extends RuntimeException {
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
