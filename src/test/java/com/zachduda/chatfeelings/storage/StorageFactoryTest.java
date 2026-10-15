package com.zachduda.chatfeelings.storage;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StorageFactoryTest {
    @TempDir
    File dir;

    private static String type(String configured) {
        final YamlConfiguration c = new YamlConfiguration();
        c.set("Other.Storage.Type", configured);
        return StorageFactory.configuredType(c);
    }

    @Test
    void storageTypeIsNormalized() {
        assertEquals("MYSQL", type(" mysql "));
        assertEquals("MYSQL", type("MariaDB"));
        assertEquals("YAML", type("postgres"), "unknown types keep the default");
        assertEquals("YAML", StorageFactory.configuredType(new YamlConfiguration()));
    }

    private static MySQLStorage mysql(String tablePrefix) {
        return new MySQLStorage("127.0.0.1", 1, "chatfeelings", "root", "", false, tablePrefix, "");
    }

    /** The prefix is pasted into SQL statements, so only letters, digits and '_' may get through. */
    @Test
    void unsafeTablePrefixesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> mysql("cf_; DROP TABLE cf_players; --"));
        assertThrows(IllegalArgumentException.class, () -> mysql("cf-"));
        assertThrows(IllegalArgumentException.class, () -> mysql("a".repeat(33)));
        assertThrows(IllegalArgumentException.class, () -> mysql(null));
        assertDoesNotThrow(() -> mysql(""));
        assertDoesNotThrow(() -> mysql("Survival_1_"));
    }

    @Test
    void brokenMySQLSettingsFallBackToYaml() {
        final YamlConfiguration c = new YamlConfiguration();
        c.set("Other.Storage.Type", "MySQL");
        c.set("Other.Storage.MySQL.Host", "127.0.0.1");
        c.set("Other.Storage.MySQL.Port", 1); // nothing listens here
        c.set("Other.Storage.MySQL.Table-Prefix", "bad prefix!");
        assertInstanceOf(YamlStorage.class, StorageFactory.create(c, dir));

        c.set("Other.Storage.MySQL.Table-Prefix", "cf_");
        assertInstanceOf(YamlStorage.class, StorageFactory.create(c, dir), "an unreachable database doesn't stop startup");
    }
}
