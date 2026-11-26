package com.zachduda.chatfeelings.util;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlFilesTest {
    @TempDir
    File dir;

    @Test
    void savesReplaceTheFileAndLeaveNothingBehind() throws Exception {
        final File file = new File(dir, "Data/player.yml");
        final YamlConfiguration yml = new YamlConfiguration();
        yml.set("Username", "Älice");
        assertTrue(YamlFiles.save(yml, file), "creates the missing folder too");
        yml.set("Username", "Bob");
        assertTrue(YamlFiles.save(yml, file));

        assertEquals("Bob", YamlFiles.load(file).config().getString("Username"));
        assertArrayEquals(new String[]{"player.yml"}, file.getParentFile().list(), "no temporary file is left");
    }

    @Test
    void aWriteThatFailsKeepsThePreviousFile() throws Exception {
        final File file = new File(dir, "player.yml");
        Files.writeString(file.toPath(), "Username: Alice\n", StandardCharsets.UTF_8);
        // A folder where the new data would be written makes the write fail, like a full disk would. Bukkit's
        // save() would have emptied player.yml by this point.
        assertTrue(new File(dir, "player.yml.tmp").mkdir());
        final YamlConfiguration yml = new YamlConfiguration();
        yml.set("Username", "Bob");

        assertThrows(IOException.class, () -> YamlFiles.writeAtomically(yml, file));
        assertEquals("Username: Alice\n", Files.readString(file.toPath(), StandardCharsets.UTF_8));
    }

    @Test
    void aSymlinkedFileIsWrittenThroughTheLink() throws Exception {
        final File shared = new File(dir, "shared-config.yml");
        Files.writeString(shared.toPath(), "Debug: false\n", StandardCharsets.UTF_8);
        final File link = new File(dir, "config.yml");
        Files.createSymbolicLink(link.toPath(), shared.toPath());
        final YamlConfiguration yml = new YamlConfiguration();
        yml.set("Debug", true);

        assertTrue(YamlFiles.save(yml, link));
        assertTrue(Files.isSymbolicLink(link.toPath()), "the link is kept");
        assertTrue(YamlFiles.load(shared).config().getBoolean("Debug"), "the shared file got the change");
    }
}
