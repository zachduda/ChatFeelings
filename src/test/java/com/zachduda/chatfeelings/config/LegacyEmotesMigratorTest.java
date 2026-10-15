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

class LegacyEmotesMigratorTest {
    @TempDir
    File dir;

    @Test
    void oldEmotesFileIsUpgradedThenSplitIntoTheFeelingsFolder() throws Exception {
        Files.writeString(new File(dir, "emotes.yml").toPath(), """
                Version: 3
                Feelings:
                  Bite:
                    Enable: true
                    Msgs:
                      Sender: "&7old info text"
                  Hug:
                    Enable: false
                    Sounds:
                      Sound1:
                        Name: ENTITY_CAT_PURREOW
                  Wb:
                    Enable: false
                    Msgs:
                      Target: "custom wb target"
                """, StandardCharsets.UTF_8);
        final File feelings = new File(dir, "Feelings");

        LegacyEmotesMigrator.migrate(dir, feelings);

        // The Wb -> Welcomeback rename survives (an older version saved a stale copy over it and created wb.yml).
        assertFalse(new File(feelings, "wb.yml").exists());
        final YamlConfiguration welcome = YamlConfiguration.loadConfiguration(new File(feelings, "welcomeback.yml"));
        assertEquals("custom wb target", welcome.getString("Msgs.Target"));
        assertFalse(welcome.getBoolean("Enable", true));
        assertEquals("&7You told &#c3ff9b&l%player%&r &7welcome back!", welcome.getString("Msgs.Sender"));

        final YamlConfiguration bite = YamlConfiguration.loadConfiguration(new File(feelings, "bite.yml"));
        assertEquals("&7You sink your teeth into &#FF8C6B&l%player%&r&7's skin", bite.getString("Msgs.Sender"));

        final YamlConfiguration hug = YamlConfiguration.loadConfiguration(new File(feelings, "hug.yml"));
        assertEquals("ENTITY.CAT.PURREOW", hug.getString("Sounds.Sound1.Name"));
        assertFalse(hug.getBoolean("Enable", true));

        final YamlConfiguration emotes = YamlConfiguration.loadConfiguration(new File(dir, "emotes.yml"));
        assertNull(emotes.get("Feelings"));
        assertEquals(LegacyEmotesMigrator.EMOTES_VERSION, emotes.getInt("Version"));
    }

    @Test
    void existingFeelingFilesAreNotOverwritten() throws Exception {
        final File feelings = new File(dir, "Feelings");
        assertTrue(feelings.mkdirs());
        Files.writeString(new File(feelings, "hug.yml").toPath(), "Enable: true\nMsgs:\n  Sender: kept\n", StandardCharsets.UTF_8);
        Files.writeString(new File(dir, "emotes.yml").toPath(), """
                Version: 9
                Feelings:
                  Hug:
                    Msgs:
                      Sender: from emotes
                """, StandardCharsets.UTF_8);

        LegacyEmotesMigrator.migrate(dir, feelings);

        assertEquals("kept", YamlConfiguration.loadConfiguration(new File(feelings, "hug.yml")).getString("Msgs.Sender"));
    }

    @Test
    void noEmotesFileDoesNothing() {
        LegacyEmotesMigrator.migrate(dir, new File(dir, "Feelings"));
        assertFalse(new File(dir, "emotes.yml").exists());
    }

    @Test
    void unreadableEmotesFileIsLeftAsIs() throws Exception {
        final File emotes = new File(dir, "emotes.yml");
        final String broken = "Version: 3\nFeelings:\n  Hug: [unclosed\n";
        Files.writeString(emotes.toPath(), broken, StandardCharsets.UTF_8);
        final File feelings = new File(dir, "Feelings");

        LegacyEmotesMigrator.migrate(dir, feelings);

        assertEquals(broken, Files.readString(emotes.toPath(), StandardCharsets.UTF_8));
        assertFalse(feelings.exists(), "nothing is half-migrated");
    }
}
