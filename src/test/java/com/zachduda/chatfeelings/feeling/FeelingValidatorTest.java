package com.zachduda.chatfeelings.feeling;

import com.zachduda.chatfeelings.effect.ParticlePresets;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeelingValidatorTest {
    private static final Predicate<String> SOUNDS = Set.of("ENTITY_CAT_PURREOW", "entity.cat.purreow")::contains;

    private static YamlConfiguration valid() {
        final YamlConfiguration c = new YamlConfiguration();
        c.set("Enable", true);
        c.set("Msgs.Sender", "s");
        c.set("Msgs.Target", "t");
        c.set("Msgs.Global", "g");
        c.set("Sounds.Sound1.Name", "ENTITY_CAT_PURREOW");
        c.set("Sounds.Sound1.Volume", 2.0);
        c.set("Sounds.Sound1.Pitch", 1);
        c.set("Sounds.Sound2.Name", "None");
        return c;
    }

    private static List<String> validate(String name, YamlConfiguration c) {
        return FeelingValidator.validate(name, c, SOUNDS, ParticlePresets::isValid);
    }

    @Test
    void acceptsACompleteFeeling() {
        assertEquals(List.of(), validate("Tickle", valid()));
    }

    @Test
    void silentSoundNeedsNoVolumeOrPitch() {
        final YamlConfiguration c = valid();
        c.set("Sounds.Sound1.Name", "off");
        c.set("Sounds.Sound1.Volume", null);
        assertEquals(List.of(), validate("tickle", c));
    }

    @Test
    void rejectsBadNames() {
        assertFalse(FeelingValidator.validateName("1hug").isEmpty());
        assertFalse(FeelingValidator.validateName("has space").isEmpty());
        assertFalse(FeelingValidator.validateName("a".repeat(33)).isEmpty());
        assertTrue(FeelingValidator.validateName("high-five_2").isEmpty());
    }

    @Test
    void rejectsServerCommandsAndChatFeelingsPermissionNodes() {
        for (String name : List.of("cf", "Feelings", "kill", "tp", "msg", "help", "plugins",
                "all", "admin", "mute", "ignore", "stats", "bypasscooldowns")) {
            assertFalse(FeelingValidator.validateName(name).isEmpty(), name);
        }
    }

    @Test
    void reportsEveryProblem() {
        final YamlConfiguration c = new YamlConfiguration();
        c.set("Enable", "yes");
        c.set("Msgs.Sender", " ");
        c.set("Sounds.Sound1.Name", "NOT_A_SOUND");
        c.set("Sounds.Sound1.Volume", 99);
        c.set("Particle", "sparkles");
        c.set("Violent", "very");
        final List<String> errors = validate("tickle", c);
        assertTrue(errors.stream().anyMatch(e -> e.contains("'Enable'")), errors.toString());
        assertTrue(errors.stream().anyMatch(e -> e.contains("Msgs.Sender")), errors.toString());
        assertTrue(errors.stream().anyMatch(e -> e.contains("Msgs.Target")), errors.toString());
        assertTrue(errors.stream().anyMatch(e -> e.contains("isn't a recognized sound")), errors.toString());
        assertTrue(errors.stream().anyMatch(e -> e.contains("Volume")), errors.toString());
        assertTrue(errors.stream().anyMatch(e -> e.contains("Pitch")), errors.toString());
        assertTrue(errors.stream().anyMatch(e -> e.contains("Sound2.Name")), errors.toString());
        assertTrue(errors.stream().anyMatch(e -> e.contains("'Particle'")), errors.toString());
        assertTrue(errors.stream().anyMatch(e -> e.contains("'Violent'")), errors.toString());
    }

    @Test
    void nameEdgeCases() {
        assertTrue(FeelingValidator.validateName("a").isEmpty());
        assertTrue(FeelingValidator.validateName("a".repeat(32)).isEmpty());
        assertFalse(FeelingValidator.validateName("").isEmpty());
        assertFalse(FeelingValidator.validateName("caf\u00e9").isEmpty());
        // A '.' would split the feeling's stats key (Stats.Sent.<Name>) into nested YAML sections.
        assertFalse(FeelingValidator.validateName("high.five").isEmpty());
        assertFalse(FeelingValidator.validateName("KILL").isEmpty(), "reserved names ignore case");
    }

    @Test
    void totalIsReserved() {
        // Its stats would land on Stats.Sent.Total, every player's overall count, and count each use twice.
        assertFalse(FeelingValidator.validateName("Total").isEmpty());
    }

    @Test
    void volumeAndPitchLimitsAreInclusive() {
        YamlConfiguration c = valid();
        c.set("Sounds.Sound1.Volume", 0);
        c.set("Sounds.Sound1.Pitch", 2.0);
        assertEquals(List.of(), validate("tickle", c));
        c.set("Sounds.Sound1.Volume", 10);
        c.set("Sounds.Sound1.Pitch", 0);
        assertEquals(List.of(), validate("tickle", c));

        for (Object[] bad : new Object[][]{{"Volume", 10.5}, {"Volume", -0.1}, {"Pitch", 2.01}, {"Volume", "2"}}) {
            c = valid();
            c.set("Sounds.Sound1." + bad[0], bad[1]);
            assertEquals(1, validate("tickle", c).size(), bad[0] + ": " + bad[1]);
        }
    }

    @Test
    void particleCanBeABuiltinFeelingsName() {
        final YamlConfiguration c = valid();
        c.set("Particle", "kiss");
        assertEquals(List.of(), validate("tickle", c));
        c.set("Particle", 5);
        assertEquals(1, validate("tickle", c).size());
    }
}
