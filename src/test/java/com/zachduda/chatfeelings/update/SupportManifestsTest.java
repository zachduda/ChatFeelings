package com.zachduda.chatfeelings.update;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Released jars download supports/&lt;version&gt;.json from master, so a broken one affects old releases too. */
class SupportManifestsTest {
    private static final Path FOLDER = Path.of(System.getProperty("basedir", "."), "supports");
    private static final Set<String> STATUSES = Set.of("FULL", "PARTIAL", "NONE", "NOT_TESTED");

    @Test
    void everyManifestIsWellFormed() throws Exception {
        final List<Path> manifests;
        try (Stream<Path> files = Files.list(FOLDER)) {
            manifests = files.filter(p -> p.getFileName().toString().endsWith(".json")).toList();
        }
        assertFalse(manifests.isEmpty(), "nothing found in " + FOLDER.toAbsolutePath());

        for (Path manifest : manifests) {
            final String file = manifest.getFileName().toString();
            final JsonObject json = JsonParser.parseString(Files.readString(manifest, StandardCharsets.UTF_8)).getAsJsonObject();
            final JsonObject versions = json.getAsJsonObject("Versions");
            assertNotNull(versions, file + " has no Versions");
            for (Map.Entry<String, JsonElement> entry : versions.entrySet()) {
                // SupportCheck looks the server up as McVersion.majorMinor("_"), ie: "1_21" or "26_1".
                assertTrue(entry.getKey().matches("\\d+_\\d+"), file + ": '" + entry.getKey() + "' must be written like 1_21");
                assertTrue(STATUSES.contains(entry.getValue().getAsString().toUpperCase(Locale.ROOT)),
                        file + ": unknown status for " + entry.getKey() + ": " + entry.getValue());
            }
            for (String key : List.of("Latest", "Oldest")) {
                assertTrue(json.has(key) && json.get(key).getAsString().matches("\\d+\\.\\d+(\\.\\d+)?"), file + ": " + key);
            }
            if (json.has("Msg_Critical")) {
                assertTrue(json.get("Msg_Critical").getAsJsonPrimitive().isBoolean(), file + ": Msg_Critical must be true/false");
            }
        }
    }

    @Test
    void thisVersionHasAManifest() throws Exception {
        final YamlConfiguration pluginYml = new YamlConfiguration();
        try (InputStream in = getClass().getResourceAsStream("/plugin.yml")) {
            assertNotNull(in);
            pluginYml.loadFromString(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
        final String version = pluginYml.getString("version");
        final Path manifest = FOLDER.resolve(SupportCheck.manifestName(version) + ".json");
        assertTrue(Files.exists(manifest), "add supports/" + manifest.getFileName() + " for v" + version);
    }
}
