package com.zachduda.chatfeelings.storage;

import com.zachduda.chatfeelings.util.Log;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.Locale;

public final class StorageFactory {
    private StorageFactory() {
    }

    /** The backend configured under Other.Storage.Type, normalized to "YAML" or "MYSQL". */
    public static String configuredType(FileConfiguration config) {
        final String type = config.getString("Other.Storage.Type", "YAML").trim().toUpperCase(Locale.ROOT);
        return (type.equals("MYSQL") || type.equals("MARIADB")) ? "MYSQL" : "YAML";
    }

    /**
     * Builds the configured storage backend. If MySQL is selected but can't be reached, this logs
     * why and falls back to YAML so the plugin keeps working.
     */
    public static PlayerStorage create(FileConfiguration config, File dataFolder) {
        if (!configuredType(config).equals("MYSQL")) {
            return new YamlStorage(dataFolder);
        }

        final String base = "Other.Storage.MySQL.";
        MySQLStorage mysql = null;
        try {
            mysql = new MySQLStorage(
                    config.getString(base + "Host", "localhost"),
                    config.getInt(base + "Port", 3306),
                    config.getString(base + "Database", "chatfeelings"),
                    config.getString(base + "Username", "root"),
                    config.getString(base + "Password", ""),
                    config.getBoolean(base + "Use-SSL", false),
                    config.getString(base + "Table-Prefix", "cf_"),
                    config.getString(base + "Properties", ""));
            mysql.init();
            Log.info("Connected to MySQL for player data storage.");

            if (config.getBoolean(base + "Import-From-YAML", true)) {
                final int imported = mysql.importFromYaml(dataFolder);
                if (imported > 0) {
                    Log.notice("Imported " + imported + " player file(s) from the Data folder into MySQL. "
                            + "The old files were left in place as a backup.");
                }
            }
            return mysql;
        } catch (Exception err) {
            if (mysql != null) {
                mysql.close();
            }
            Log.warn("Unable to connect to MySQL", err);
            Log.warn("Falling back to YAML files in the Data folder until this is fixed. Check Other.Storage in config.yml.");
            return new YamlStorage(dataFolder);
        }
    }
}
