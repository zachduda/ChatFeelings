package com.zachduda.chatfeelings.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessagesTest {
    @TempDir
    File dir;

    @Test
    void createsFileWithEveryDefault() {
        final Messages messages = Messages.load(dir);
        final YamlConfiguration saved = YamlConfiguration.loadConfiguration(new File(dir, "messages.yml"));
        assertEquals(Messages.VERSION, saved.getInt("Version"));
        for (String key : DefaultMessages.VALUES.keySet()) {
            assertTrue(saved.contains(key), "missing " + key);
        }
        assertEquals(DefaultMessages.VALUES.get("No-Permission"), messages.get("No-Permission"));
    }

    @Test
    void keepsCustomizedValuesAndFillsBlanks() throws Exception {
        Files.writeString(new File(dir, "messages.yml").toPath(),
                "Version: 13\nNo-Permission: \"Nope!\"\nIs-Muted:\n", StandardCharsets.UTF_8);
        final Messages messages = Messages.load(dir);
        assertEquals("Nope!", messages.get("No-Permission"));
        assertEquals(DefaultMessages.VALUES.get("Is-Muted"), messages.get("Is-Muted"));
    }

    @Test
    void migratesMisspelledKeysFromOldVersions() throws Exception {
        Files.writeString(new File(dir, "messages.yml").toPath(),
                "Version: 9\nPrefix: \"[CF]\"\nIngoring-On-Player: \"custom on %player%\"\nCommand_Descriptions:\n  Wb: \"old wb\"\n",
                StandardCharsets.UTF_8);
        final Messages messages = Messages.load(dir);
        assertEquals("custom on %player%", messages.get("Ignoring-On-Player"));
        assertEquals("old wb", messages.get("Command_Descriptions.Welcomeback"));
        assertEquals("[CF] &f", messages.get("Prefix"));
        assertNull(messages.getIfSet("Ingoring-On-Player"));
        assertNull(messages.getIfSet("Command_Descriptions.Wb"));
    }

    @Test
    void brokenFileIsNotOverwritten() throws Exception {
        final File file = new File(dir, "messages.yml");
        final String broken = "Version: 13\nPrefix: \"unclosed\n  - [nope\n";
        Files.writeString(file.toPath(), broken, StandardCharsets.UTF_8);
        final Messages messages = Messages.load(dir);
        assertEquals(broken, Files.readString(file.toPath(), StandardCharsets.UTF_8));
        assertEquals(DefaultMessages.VALUES.get("Prefix"), messages.get("Prefix"));
    }

    @Test
    void fileSavedInAnotherEncodingStillLoads() throws Exception {
        final File file = new File(dir, "messages.yml");
        final byte[] windows1252 = "Version: 13\nConsole-Name: \"Le Serveur\"\nNo-Permission: \"&cDésolé\"\n"
                .getBytes(java.nio.charset.Charset.forName("windows-1252"));
        Files.write(file.toPath(), windows1252);
        final Messages messages = Messages.load(dir);
        assertEquals("Le Serveur", messages.get("Console-Name"), "the rest of the file isn't thrown away");
        assertEquals("&cD\uFFFDsol\uFFFD", messages.get("No-Permission"));
        assertTrue(java.util.Arrays.equals(windows1252,
                Files.readAllBytes(new File(dir, "messages.yml.not-utf8.bak").toPath())), "the original is kept");
    }

    @Test
    void unknownKeysAreEmptyNotNull() {
        final Messages messages = Messages.defaults();
        assertEquals("", messages.get("Does-Not-Exist"));
        assertFalse(messages.get("Prefix").isEmpty());
    }
}
