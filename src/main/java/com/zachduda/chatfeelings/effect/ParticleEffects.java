package com.zachduda.chatfeelings.effect;

import com.zachduda.chatfeelings.platform.Scheduler;
import com.zachduda.chatfeelings.util.Log;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shows the particle effects named in {@link ParticlePresets} around a feeling's target.
 * <p>
 * Particles are looked up by name, trying the current name first and the pre-1.20.5 name second, so the
 * same jar works across server versions. Must be called on the target's own thread (see {@link Scheduler}).
 */
public final class ParticleEffects {
    private static final Particle HEART = find("HEART");
    private static final Particle CRIT = find("CRIT");
    private static final Particle ENCHANTED_HIT = find("ENCHANTED_HIT", "CRIT_MAGIC");
    private static final Particle HAPPY_VILLAGER = find("HAPPY_VILLAGER", "VILLAGER_HAPPY");
    private static final Particle ANGRY_VILLAGER = find("ANGRY_VILLAGER", "VILLAGER_ANGRY");
    private static final Particle SWEEP_ATTACK = find("SWEEP_ATTACK");
    private static final Particle LAVA = find("LAVA");
    private static final Particle END_ROD = find("END_ROD");
    private static final Particle SPLASH = find("SPLASH", "WATER_SPLASH");
    private static final Particle CLOUD = find("CLOUD");
    private static final Particle DRIPPING_WATER = find("DRIPPING_WATER", "DRIP_WATER");
    private static final Particle FALLING_WATER = find("FALLING_WATER", "WATER_DROP");
    private static final Particle EXPLOSION = find("EXPLOSION", "EXPLOSION_LARGE");
    private static final Particle GLOW_SQUID_INK = find("GLOW_SQUID_INK");
    private static final Particle BLOCK = find("BLOCK", "BLOCK_CRACK");

    private final Scheduler scheduler;
    private final Map<String, Boolean> failedPresets = new ConcurrentHashMap<>();

    public ParticleEffects(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /** Shows a preset around {@code target}. Errors are logged once per preset rather than disabling particles. */
    public void play(Player target, String preset) {
        if (preset == null || preset.equals(ParticlePresets.NONE)) {
            return;
        }
        try {
            switch (preset) {
                case "hug" -> at(target, HEART, 1, 9, 1.0, 0.5, 1.0);
                case "punch" -> repeat(target, SWEEP_ATTACK, SWEEP_ATTACK, 9);
                case "shake" -> repeat(target, ANGRY_VILLAGER, SWEEP_ATTACK, 5);
                case "wave" -> repeat(target, HAPPY_VILLAGER, SWEEP_ATTACK, 5);
                case "poke" -> at(target, HAPPY_VILLAGER, 1, 15, 0.4, 0.4, 0.4);
                case "highfive" -> at(target, ENCHANTED_HIT, 1, 30, 0.4, 0.4, 0.4);
                case "facepalm" -> spawn(target, CLOUD, target.getLocation().add(0, 1, 0), 3, 0.4, 0.4, 0.4, 0.0001);
                case "yell" -> at(target, EXPLOSION, 1, 1, 0.2, 0.5, 0.2);
                case "bite" -> at(target, CRIT, 1, 30, 0.4, 0.4, 0.4);
                case "murder" -> murder(target);
                case "boi" -> at(target, END_ROD, 1, 40, 0, 0, 0);
                case "dab" -> at(target, END_ROD, 1, 20, 0.4, 0.4, 0.4);
                case "cry" -> at(target, SPLASH, 1.5, 100, 0.4, 0.4, 0.4);
                case "lick" -> {
                    at(target, DRIPPING_WATER, 2, 10, 0.2, 0.5, 0.2);
                    at(target, FALLING_WATER, 1, 24, 1.0, 0.5, 1.0);
                }
                case "welcomeback" -> {
                    if (GLOW_SQUID_INK != null) {
                        spawn(target, GLOW_SQUID_INK, target.getLocation().add(0, 1.3, 0), 20, 2.0, 1.0, 2.0, 0.001);
                    } else {
                        at(target, END_ROD, 1, 40, 0, 0, 0);
                    }
                }
                default -> Log.debug("Unknown particle preset: " + preset);
            }
        } catch (RuntimeException err) {
            if (failedPresets.putIfAbsent(preset, Boolean.TRUE) == null) {
                Log.warn("Couldn't show the '" + preset + "' particles; they'll be skipped.", err);
            }
        }
    }

    /** {@code first} now, then alternating {@code second}/{@code first} every 2 ticks, {@code steps} times in total. */
    private void repeat(Player target, Particle first, Particle second, int steps) {
        single(target, first);
        for (int i = 1; i < steps; i++) {
            final Particle particle = (i % 2 == 1) ? second : first;
            scheduler.entityLater(target, () -> single(target, particle), 2L * i);
        }
    }

    private void single(Player target, Particle particle) {
        if (target.isOnline()) {
            at(target, particle, 1, 1, 0.5, 1.0, 0.5);
        }
    }

    /**
     * Red concrete breaking, then lava. Earlier versions used Effect.STEP_SOUND for the first part, which Paper
     * has deprecated for removal, so the break particles and their sound are played separately.
     */
    private void murder(Player target) {
        final Location spot = target.getLocation().add(0.04, 0.8, 0.04);
        if (BLOCK != null) {
            final BlockData blood = Material.RED_CONCRETE.createBlockData();
            spawn(target, BLOCK, spot, 30, 0.2, 0.3, 0.2, 0.0, blood);
        }
        target.getWorld().playSound(spot, Sound.BLOCK_STONE_BREAK, 1.0F, 0.8F);
        scheduler.entityLater(target, () -> {
            if (target.isOnline()) {
                at(target, LAVA, 1, 5, 0.0, 0.6, 0.0);
            }
        }, 5L);
    }

    /** Spawns above the target's feet with speed 1, which is what Bukkit's shorter spawnParticle overloads use. */
    private static void at(Player target, Particle particle, double yOffset, int count, double dx, double dy, double dz) {
        spawn(target, particle, target.getLocation().add(0, yOffset, 0), count, dx, dy, dz, 1.0);
    }

    private static void spawn(Player target, Particle particle, Location loc, int count,
                              double dx, double dy, double dz, double extra) {
        spawn(target, particle, loc, count, dx, dy, dz, extra, null);
    }

    private static <T> void spawn(Player target, Particle particle, Location loc, int count,
                                  double dx, double dy, double dz, double extra, T data) {
        final World world = target.getWorld();
        if (particle == null) {
            return; // doesn't exist on this server version
        }
        if (data == null) {
            world.spawnParticle(particle, loc, count, dx, dy, dz, extra);
        } else {
            world.spawnParticle(particle, loc, count, dx, dy, dz, extra, data);
        }
    }

    /** Reads a Particle constant by name, so a renamed or missing constant can't stop the class from loading. */
    private static Particle find(String... names) {
        for (String name : names) {
            try {
                final Object value = Particle.class.getField(name).get(null);
                if (value instanceof Particle particle) {
                    return particle;
                }
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                // try the next name
            }
        }
        return null;
    }
}
