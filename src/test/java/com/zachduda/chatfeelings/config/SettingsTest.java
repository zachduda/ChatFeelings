package com.zachduda.chatfeelings.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class SettingsTest {
    @Test
    void negativeAndZeroNumbersAreClamped() {
        final YamlConfiguration c = new YamlConfiguration();
        c.set("General.Cooldowns.Feelings.Seconds", -5);
        c.set("General.Cooldowns.Ignoring.Seconds", -1);
        c.set("General.Cooldowns.Ignore-List.Seconds", -10);
        c.set("General.Help-Page-Length", 0);
        final Settings s = Settings.from(c);
        assertEquals(0, s.feelingCooldownSeconds());
        assertEquals(0, s.ignoreCooldownSeconds());
        assertEquals(0, s.ignoreListCooldownSeconds());
        assertEquals(1, s.helpPageLength(), "/feelings divides by the page length");
    }

    /**
     * Flips every true/false and changes every other value in the shipped config.yml, then checks that every
     * setting changed too. One that didn't is read from a key that isn't in config.yml (a typo, or a key renamed
     * on only one side), so admins have no way to change it.
     */
    @Test
    void everySettingIsReadFromAKeyInConfigYml() throws Exception {
        final YamlConfiguration changed = ConfigUpdaterTest.shippedConfig();
        for (String key : changed.getKeys(true)) {
            final Object value = changed.get(key);
            if (value instanceof Boolean b) {
                changed.set(key, !b);
            } else if (value instanceof Number n) {
                changed.set(key, n.intValue() + 3);
            } else if (value instanceof String s) {
                changed.set(key, s + "-changed");
            } else if (value instanceof List<?>) {
                changed.set(key, List.of("changed"));
            }
        }

        final Settings shipped = Settings.from(ConfigUpdaterTest.shippedConfig());
        final Settings edited = Settings.from(changed);
        for (RecordComponent component : Settings.class.getRecordComponents()) {
            assertNotEquals(component.getAccessor().invoke(shipped), component.getAccessor().invoke(edited),
                    component.getName() + " doesn't follow config.yml");
        }
    }
}
