package com.zachduda.chatfeelings.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlStorageTest {
    @TempDir
    File dir;

    private final UUID alice = UUID.randomUUID();
    private final UUID bob = UUID.randomUUID();

    @Test
    void recordsAndUpdatesPlayers() {
        final YamlStorage storage = new YamlStorage(dir);
        assertNull(storage.load(alice));
        storage.recordLogin(alice, "Alice", "1.2.3.4", 1000L);

        PlayerData data = storage.load(alice);
        assertEquals("Alice", data.getUsername());
        assertEquals("1.2.3.4", data.getIp());
        assertTrue(data.isAllowingFeelings());
        assertFalse(data.isMuted());

        assertTrue(storage.setMuted(alice, true));
        assertEquals(Boolean.FALSE, storage.toggleAllowFeelings(alice));
        // A later login refreshes name/IP but must not reset mutes or "ignore all".
        storage.recordLogin(alice, "AliceRenamed", "5.6.7.8", 2000L);
        data = storage.load(alice);
        assertTrue(data.isMuted());
        assertFalse(data.isAllowingFeelings());
        assertEquals("AliceRenamed", data.getUsername());
        assertEquals(alice, storage.findUUIDByName("alicerenamed"));
    }

    @Test
    void ignoringAndStats() {
        final YamlStorage storage = new YamlStorage(dir);
        storage.recordLogin(alice, "Alice", null, 1000L);
        assertEquals(Boolean.TRUE, storage.toggleIgnoring(alice, bob));
        assertTrue(storage.load(alice).isIgnoring(bob));
        assertEquals(Boolean.FALSE, storage.toggleIgnoring(alice, bob));
        assertFalse(storage.load(alice).isIgnoring(bob));
        assertNull(storage.toggleIgnoring(bob, alice), "no record for bob");

        storage.incrementSent(alice, "Hug");
        storage.incrementSent(alice, "Hug");
        storage.incrementSent(alice, "Slap");
        storage.incrementGlobalSent("Hug");
        final PlayerData data = storage.load(alice);
        assertEquals(2, data.getSent("Hug"));
        assertEquals(3, data.getTotalSent());
        assertEquals(1, storage.getGlobalSent().get("Hug"));
    }

    @Test
    void invalidFilesAreMovedNotDeleted() throws Exception {
        final YamlStorage storage = new YamlStorage(dir);
        storage.recordLogin(alice, "Alice", null, 1000L);
        final File data = new File(dir, "Data");
        Files.writeString(new File(data, "broken.yml").toPath(), "Username: nobody\n", StandardCharsets.UTF_8);
        Files.writeString(new File(data, ".DS_Store").toPath(), "junk", StandardCharsets.UTF_8);

        assertEquals(1, storage.cleanupInvalid());
        assertTrue(new File(data, "invalid/broken.yml").exists());
        assertFalse(new File(data, "broken.yml").exists());
        assertEquals(1, storage.loadAll().size());
        assertEquals(1, storage.listAll().size());
    }

    @Test
    void unreadableFilesAreMovedAsideInsteadOfOverwritten() throws Exception {
        final YamlStorage storage = new YamlStorage(dir);
        storage.recordLogin(alice, "Alice", null, 1000L);
        storage.setMuted(alice, true);
        final File file = new File(dir, "Data/" + alice + ".yml");
        final String broken = Files.readString(file.toPath(), StandardCharsets.UTF_8) + "\tbroken: [yaml\n";
        Files.writeString(file.toPath(), broken, StandardCharsets.UTF_8);

        assertFalse(storage.setMuted(alice, false), "a write to an unreadable file is refused");
        assertTrue(new File(dir, "Data/invalid/" + alice + ".yml").exists(), "the damaged file is kept for recovery");
        assertEquals(broken, Files.readString(new File(dir, "Data/invalid/" + alice + ".yml").toPath(), StandardCharsets.UTF_8));

        storage.recordLogin(alice, "Alice", null, 2000L); // next join starts a fresh record
        assertEquals(2000L, storage.load(alice).getLastOn());
    }

    @Test
    void aStrayNonUtf8ByteDoesNotCountAsUnreadable() throws Exception {
        final YamlStorage storage = new YamlStorage(dir);
        storage.recordLogin(alice, "Alice", null, 1000L);
        storage.setMuted(alice, true);
        final File file = new File(dir, "Data/" + alice + ".yml");
        // "# muted by Jos\u00e9" saved by a Windows-1252 editor: 0xE9 is not valid UTF-8.
        final byte[] original = Files.readAllBytes(file.toPath());
        final byte[] comment = "# muted by Jos\u00e9\n".getBytes(java.nio.charset.Charset.forName("windows-1252"));
        final byte[] edited = new byte[comment.length + original.length];
        System.arraycopy(comment, 0, edited, 0, comment.length);
        System.arraycopy(original, 0, edited, comment.length, original.length);
        Files.write(file.toPath(), edited);

        storage.incrementSent(alice, "Hug");
        assertFalse(new File(dir, "Data/invalid").exists(), "the file wasn't moved aside");
        final PlayerData data = storage.load(alice);
        assertTrue(data.isMuted(), "the player's data survived");
        assertEquals(1, data.getSent("Hug"));
    }

    @Test
    void aFileThatCannotBeMovedAsideIsNeverOverwritten() throws Exception {
        final YamlStorage storage = new YamlStorage(dir);
        storage.recordLogin(alice, "Alice", null, 1000L);
        final File file = new File(dir, "Data/" + alice + ".yml");
        final String broken = "UUID: x\n\tbroken: [yaml\n";
        Files.writeString(file.toPath(), broken, StandardCharsets.UTF_8);
        // A plain file where the Data/invalid folder should be makes the move fail.
        Files.writeString(new File(dir, "Data/invalid").toPath(), "in the way", StandardCharsets.UTF_8);

        storage.recordLogin(alice, "Alice", null, 2000L);
        storage.incrementGlobalSent("Hug");
        assertEquals(broken, Files.readString(file.toPath(), StandardCharsets.UTF_8));
    }

    @Test
    void deleteRemovesRecordAndNameIndex() {
        final YamlStorage storage = new YamlStorage(dir);
        storage.recordLogin(alice, "Alice", null, 1000L);
        assertEquals(alice, storage.findUUIDByName("Alice"));
        storage.delete(alice);
        assertNull(storage.load(alice));
        assertNull(storage.findUUIDByName("Alice"));
    }

    @Test
    void globalStatsFileIsNeverTreatedAsAPlayer() {
        final YamlStorage storage = new YamlStorage(dir);
        storage.recordLogin(alice, "Alice", null, 1000L);
        storage.incrementGlobalSent("Hug");
        storage.flush();

        assertEquals(0, storage.cleanupInvalid(), "global.yml isn't a broken player file");
        assertTrue(new File(dir, "Data/global.yml").exists());
        assertEquals(1, storage.loadAll().size());
        assertEquals(1, storage.getGlobalSent().get("Hug"));
    }

    @Test
    void globalCountsAreWrittenTogetherWhenFlushedOrClosed() {
        final YamlStorage storage = new YamlStorage(dir);
        storage.incrementGlobalSent("Hug");
        storage.incrementGlobalSent("Hug");
        storage.incrementGlobalSent("Slap");
        assertFalse(new File(dir, "Data/global.yml").exists(), "sending a feeling doesn't write the file");
        assertEquals(Map.of("Hug", 2, "Slap", 1), storage.getGlobalSent(), "but the counts are already visible");

        storage.flush();
        assertEquals(Map.of("Hug", 2, "Slap", 1), new YamlStorage(dir).getGlobalSent());
        storage.flush(); // nothing new: must not double count
        storage.incrementGlobalSent("Hug");
        storage.close();
        assertEquals(Map.of("Hug", 3, "Slap", 1), new YamlStorage(dir).getGlobalSent(), "close saves the rest");
    }

    @Test
    void globalCountsThatFailToSaveAreKeptForTheNextFlush() throws Exception {
        final YamlStorage storage = new YamlStorage(dir);
        storage.incrementGlobalSent("Hug");
        storage.flush();
        // A folder where the new data would be written makes the next write fail, like a full disk would.
        final File blocker = new File(dir, "Data/global.yml.tmp");
        assertTrue(blocker.mkdir());
        storage.incrementGlobalSent("Hug");
        storage.flush();
        assertEquals(1, new YamlStorage(dir).getGlobalSent().get("Hug"), "the failed write left the old file alone");
        assertEquals(2, storage.getGlobalSent().get("Hug"), "the unsaved count wasn't dropped");

        Files.deleteIfExists(blocker.toPath());
        storage.flush();
        assertEquals(2, new YamlStorage(dir).getGlobalSent().get("Hug"));
    }

    @Test
    void nameLookupFindsTheMostRecentOwnerOfAName() {
        final YamlStorage storage = new YamlStorage(dir);
        storage.recordLogin(alice, "Steve", null, 1000L);
        storage.recordLogin(bob, "steve", null, 2000L); // the name passed from Alice to Bob
        assertEquals(bob, new YamlStorage(dir).findUUIDByName("STEVE"), "when the index is built from the files");

        assertEquals(bob, storage.findUUIDByName("Steve"));
        storage.recordLogin(alice, "Steve", null, 3000L);
        assertEquals(alice, storage.findUUIDByName("steve"), "when a login updates the index");
    }

    @Test
    void filesFromOlderVersionsLoadWithSafeDefaults() throws Exception {
        final File folder = new File(dir, "Data");
        assertTrue(folder.mkdirs());
        // Written before mutes and "/cf ignore all" existed.
        Files.writeString(new File(folder, alice + ".yml").toPath(), "UUID: " + alice
                + "\nUsername: Alice\nLast-On: 1000\nStats:\n  Sent:\n    Hug: 2\n    Total: 2\n", StandardCharsets.UTF_8);
        final YamlStorage storage = new YamlStorage(dir);

        final PlayerData data = storage.load(alice);
        assertTrue(data.isAllowingFeelings());
        assertFalse(data.isMuted());
        assertEquals(Map.of("Hug", 2), data.getSentStats(), "Total isn't a feeling");
        assertEquals(2, data.getTotalSent());

        storage.recordLogin(alice, "Alice", null, 2000L);
        assertEquals(2, storage.load(alice).getSent("Hug"), "a login keeps existing stats");
    }

    @Test
    void movingAsideNeverOverwritesAnEarlierBrokenFile() throws Exception {
        final YamlStorage storage = new YamlStorage(dir);
        final File folder = new File(dir, "Data");
        assertTrue(folder.mkdirs());
        final File broken = new File(folder, "broken.yml");
        Files.writeString(broken.toPath(), "Username: first\n", StandardCharsets.UTF_8);
        assertEquals(1, storage.cleanupInvalid());
        Files.writeString(broken.toPath(), "Username: second\n", StandardCharsets.UTF_8);
        assertEquals(1, storage.cleanupInvalid());

        final Set<String> kept = new HashSet<>();
        for (File f : new File(folder, "invalid").listFiles()) {
            kept.add(Files.readString(f.toPath(), StandardCharsets.UTF_8));
        }
        assertEquals(Set.of("Username: first\n", "Username: second\n"), kept);
    }

    /** File access is synchronized so concurrent async tasks (and the timed save) can't write a file over each other. */
    @Test
    void concurrentUpdatesAreNotLost() throws Exception {
        final YamlStorage storage = new YamlStorage(dir);
        storage.recordLogin(alice, "Alice", null, 1000L);
        final int threads = 8;
        final int perThread = 25;
        final ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            final List<Future<?>> tasks = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                tasks.add(pool.submit(() -> {
                    for (int i = 0; i < perThread; i++) {
                        storage.incrementSent(alice, "Hug");
                        storage.incrementGlobalSent("Hug");
                        if (i % 5 == 0) {
                            storage.flush(); // the timed save running while feelings are being counted
                        }
                    }
                }));
            }
            for (Future<?> task : tasks) {
                task.get(60, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }
        final PlayerData data = storage.load(alice);
        assertEquals(threads * perThread, data.getSent("Hug"));
        assertEquals(threads * perThread, data.getTotalSent());
        assertEquals(threads * perThread, storage.getGlobalSent().get("Hug"));
        storage.flush();
        assertEquals(threads * perThread, new YamlStorage(dir).getGlobalSent().get("Hug"), "and in the file");
    }
}
