package com.zachduda.chatfeelings;

import com.zachduda.chatfeelings.feeling.BuiltinFeelings;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Checks the plugin.yml and bungee.yml that go into the jar, after Maven's resource filtering. */
class PluginYmlTest {
    /** Loaded with '/' as the path separator, since permission names contain dots. */
    static YamlConfiguration resource(String name) throws Exception {
        try (InputStream in = PluginYmlTest.class.getResourceAsStream("/" + name)) {
            assertNotNull(in, name + " isn't packaged");
            final YamlConfiguration yml = new YamlConfiguration();
            yml.options().pathSeparator('/');
            yml.loadFromString(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            return yml;
        }
    }

    @Test
    void versionIsFilledInByMaven() throws Exception {
        final String version = resource("plugin.yml").getString("version");
        assertNotNull(version);
        assertFalse(version.contains("${"), "plugin.yml wasn't filtered: " + version);
        assertTrue(version.matches("\\d+\\.\\d+\\.\\d+.*"), version);
        assertEquals(version, resource("bungee.yml").getString("version"));
    }

    @Test
    void mainClassesExist() throws Exception {
        final ClassLoader loader = getClass().getClassLoader();
        final Class<?> main = Class.forName(resource("plugin.yml").getString("main"), false, loader);
        assertTrue(JavaPlugin.class.isAssignableFrom(main));
        assertNotNull(Class.forName(resource("bungee.yml").getString("main"), false, loader));
    }

    /** Main binds each built-in feeling to its plugin.yml command; a missing one just logs a warning at runtime. */
    @Test
    void everyBuiltinFeelingHasACommandAndPermission() throws Exception {
        final YamlConfiguration yml = resource("plugin.yml");
        final ConfigurationSection permissions = yml.getConfigurationSection("permissions");
        final Set<String> allChildren = yml.getConfigurationSection("permissions/chatfeelings.all/children").getKeys(false);

        final Set<String> expectedCommands = new HashSet<>(Set.of("chatfeelings", "feelings"));
        final Set<String> expectedChildren = new HashSet<>();
        for (String name : BuiltinFeelings.NAMES) {
            assertTrue(permissions.contains("chatfeelings." + name), "chatfeelings." + name + " isn't declared");
            expectedCommands.add(name);
            expectedChildren.add("chatfeelings." + name);
        }
        assertEquals(expectedCommands, yml.getConfigurationSection("commands").getKeys(false));
        assertEquals(expectedChildren, allChildren);
    }

    /** Undeclared permissions are op-only, and chatfeelings.* is how admins hand these out. */
    @Test
    void permissionsTheCodeChecksAreDeclared() throws Exception {
        final YamlConfiguration yml = resource("plugin.yml");
        final ConfigurationSection permissions = yml.getConfigurationSection("permissions");
        final ConfigurationSection star = yml.getConfigurationSection("permissions/chatfeelings.*/children");
        for (String node : List.of("chatfeelings.all", "chatfeelings.admin", "chatfeelings.stats",
                "chatfeelings.stats.others", "chatfeelings.mute", "chatfeelings.ignore", "chatfeelings.bypasscooldowns")) {
            assertTrue(permissions.contains(node), node + " isn't declared");
            assertTrue(star.getBoolean(node), node + " isn't granted by chatfeelings.*");
        }
    }
}
