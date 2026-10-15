package com.zachduda.chatfeelings.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigUpdaterTest {
    /** The config.yml packaged in the jar. */
    static YamlConfiguration shippedConfig() throws Exception {
        try (InputStream in = ConfigUpdaterTest.class.getResourceAsStream("/config.yml")) {
            assertNotNull(in, "config.yml isn't packaged");
            final YamlConfiguration c = new YamlConfiguration();
            c.loadFromString(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            return c;
        }
    }

    /** If config.yml's Version fell behind, fresh installs would run old migrations (which turn on feeling permissions). */
    @Test
    void shippedConfigNeedsNoMigration() throws Exception {
        final YamlConfiguration c = shippedConfig();
        assertEquals(ConfigUpdater.VERSION, c.getInt("Version"));
        final String before = c.saveToString();
        ConfigUpdater.migrate(c);
        assertEquals(before, c.saveToString());
    }

    @Test
    void migratesOldVersions() {
        final YamlConfiguration c = new YamlConfiguration();
        c.set("Version", 4);
        c.set("Other.Bypass-Version-Block", true);
        c.set("General.No-Violent-Cmds-When-Sleeping", true);
        ConfigUpdater.migrate(c);
        assertEquals(ConfigUpdater.VERSION, c.getInt("Version"));
        assertNull(c.get("Other.Bypass-Version-Block"));
        assertNull(c.get("General.No-Violent-Cmds-When-Sleeping"));
        assertTrue(c.getBoolean("Other.CF-Alias"));
        assertEquals(10, c.getInt("General.Cooldowns.Ignore-List.Seconds"));
    }

    @Test
    void currentVersionIsLeftAlone() {
        final YamlConfiguration c = new YamlConfiguration();
        c.set("Version", ConfigUpdater.VERSION);
        c.set("General.Use-Feeling-Permissions", false);
        ConfigUpdater.migrate(c);
        assertFalse(c.getBoolean("General.Use-Feeling-Permissions"));
    }

    @Test
    void zeroRadiusIsDisabled() {
        final YamlConfiguration c = new YamlConfiguration();
        c.set("General.Radius.Enabled", true);
        c.set("General.Radius.Radius-In-Blocks", 0);
        ConfigUpdater.migrate(c);
        assertFalse(c.getBoolean("General.Radius.Enabled"));
        assertEquals(35, c.getInt("General.Radius.Radius-In-Blocks"));
    }
}
