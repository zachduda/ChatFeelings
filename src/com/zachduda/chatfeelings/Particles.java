package com.zachduda.chatfeelings;

import com.zachduda.chatfeelings.other.Supports;
import org.bukkit.*;
import org.bukkit.entity.Player;

@SuppressWarnings("StatementWithEmptyBody")
public class Particles {

	private static final Main plugin = Main.getPlugin(Main.class);
    private static final int particleVersion = Supports.getParticleVersion();

		static void show(Player p, String label) {
            if (!Main.particles) {
                return;
            }

            if(Main.debug && particleVersion <= 0) {
                Main.debug("Particle version returned 0. Using legacy (1) as fallback.");
            }

            try {
                if (label.equalsIgnoreCase("hug")) {
                    hugParticle(p);
                } else if (label.equalsIgnoreCase("slap")) {
                    punchParticle(p);
                } else if (label.equalsIgnoreCase("poke")) {
                    pokeParticle(p);
                } else if (label.equalsIgnoreCase("highfive")) {
                    highfiveParticle(p);
                } else if (label.equalsIgnoreCase("facepalm")) {
                    facepalmParticle(p);
                } else if (label.equalsIgnoreCase("yell")) {
                    yellParticle(p);
                } else if (label.equalsIgnoreCase("bite")) {
                    biteParticle(p);
                } else if (label.equalsIgnoreCase("snuggle")) {
                    hugParticle(p);
                } else if (label.equalsIgnoreCase("shake")) {
                    slapParticle(p);
                } else if (label.equalsIgnoreCase("stab")) {
                    murderParticle(p);
                } else if (label.equalsIgnoreCase("kiss")) {
                    hugParticle(p);
                } else if (label.equalsIgnoreCase("punch")) {
                    punchParticle(p);
                } else if (label.equalsIgnoreCase("murder")) {
                    murderParticle(p);
                } else if (label.equalsIgnoreCase("boi")) {
                    boiParticle(p);
                } else if (label.equalsIgnoreCase("cry")) {
                    cryParticle(p);
                } else if (label.equalsIgnoreCase("dab")) {
                    dabParticle(p);
                } else if (label.equalsIgnoreCase("lick")) {
                    lickParticle(p);
                } else if (label.equalsIgnoreCase("scorn")) {
                    // No particle
                } else if (label.equalsIgnoreCase("pat")) {
                    // Use hug particle
                    hugParticle(p);
                } else if (label.equalsIgnoreCase("wb")) {
                    // use boi
                    boiParticle(p);
                } else if (label.equalsIgnoreCase("stalk")) {
                    // No particle
                } else if (label.equalsIgnoreCase("sus")) {
                    // No particle
                } else {
                    plugin.getLogger().warning("Couldn't find Particle for: /" + label);
                }
            } catch(Exception err) {
                Main.particles = false;
                plugin.getLogger().warning("Error trying to display /" + label + " particles.");
                if(Main.debug) {
                    Main.debug("Particle Error Thrown:");
                    err.printStackTrace();
                }
            }
        }

	private static void hugParticle(Player p) {
        World world = p.getLocation().getWorld();
        assert world != null;
        world.spawnParticle(Particle.HEART, p.getLocation().add(0, 1, 0), 9, 1.0D, 0.5D, 1.0D);
    }

	private static void biteParticle(Player p) {
        World world = p.getLocation().getWorld();
        assert world != null;
        world.spawnParticle(Particle.CRIT, p.getLocation().getX(), p.getLocation().getY() + 1.0D, p.getLocation().getZ(), 30, 0.4D, 0.4D, 0.4D);
	}

	private static void slapParticle(final Player p) {
        World world = p.getLocation().getWorld();
        assert world != null;

        Particle happy;
        if(particleVersion >= 2) {
            happy = Particle.HAPPY_VILLAGER;
        } else {
            happy = Particle.valueOf("VILLAGER_ANGRY");
        }

        world.spawnParticle(happy, p.getLocation().getX(), p.getLocation().getY() + 1.0D, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D);
        plugin.morePaperLib.scheduling().globalRegionalScheduler().runDelayed(() -> world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY() + 1.0D, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D), 2L);
        plugin.morePaperLib.scheduling().globalRegionalScheduler().runDelayed(() -> world.spawnParticle(happy, p.getLocation().getX(), p.getLocation().getY() + 1.0D, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D), 4L);
        plugin.morePaperLib.scheduling().globalRegionalScheduler().runDelayed(() -> world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY() + 1.0D, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D), 6L);
        plugin.morePaperLib.scheduling().globalRegionalScheduler().runDelayed(() -> world.spawnParticle(happy, p.getLocation().getX(), p.getLocation().getY() + 1.0D, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D), 8L);
    }
	
	private static void punchParticle(final Player p) {
        World world = p.getLocation().getWorld();
        assert world != null;
        world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY() + 1.0D, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D);

        plugin.morePaperLib.scheduling().globalRegionalScheduler().runDelayed(() -> world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY() + 1.0D, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D), 2L);
        plugin.morePaperLib.scheduling().globalRegionalScheduler().runDelayed(() -> world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY() + 1.0D, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D), 4L);
        plugin.morePaperLib.scheduling().globalRegionalScheduler().runDelayed(() -> world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY() + 1.0D, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D), 6L);
        plugin.morePaperLib.scheduling().globalRegionalScheduler().runDelayed(() -> world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY() + 1.0D, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D), 8L);
        plugin.morePaperLib.scheduling().globalRegionalScheduler().runDelayed(() -> world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY() + 1.0D, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D), 10L);
        plugin.morePaperLib.scheduling().globalRegionalScheduler().runDelayed(() -> world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY() + 1.0D, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D), 12L);
        plugin.morePaperLib.scheduling().globalRegionalScheduler().runDelayed(() -> world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY() + 1.0D, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D), 14L);
        plugin.morePaperLib.scheduling().globalRegionalScheduler().runDelayed(() -> world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY() + 1.0D, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D), 16L);
    }

	private static void murderParticle(final Player p) {
        World world = p.getLocation().getWorld();
        assert world != null;
        world.playEffect(p.getLocation().add(0.04D, 0.8D, 0.04D), Effect.STEP_SOUND, Material.RED_CONCRETE);

        plugin.morePaperLib.scheduling().globalRegionalScheduler().runDelayed(() -> world.spawnParticle(Particle.LAVA, p.getLocation().add(0, 1, 0), 5, 0.0D, 0.6D, 0.0D), 5L);
    }

	private static void boiParticle(Player p) {
        World world = p.getLocation().getWorld();
        assert world != null;
        world.spawnParticle(Particle.END_ROD, p.getLocation().add(0, 1, 0), 40, 0.0D, 0.0D, 0.0D);
    }

	private static void dabParticle(Player p) {
        World world = p.getLocation().getWorld();
        assert world != null;
        world.spawnParticle(Particle.END_ROD, p.getLocation().add(0, 1, 0), 20, 0.4D, 0.4D, 0.4D);
    }

	private static void cryParticle(Player p) {
        World world = p.getLocation().getWorld();
        assert world != null;

        Particle splash;
        if(particleVersion >= 2) {
            splash = Particle.SPLASH;
        } else {
            splash = Particle.valueOf("WATER_SPLASH");
        }

        world.spawnParticle(splash, p.getLocation().add(0, 1.5D, 0), 100, 0.4D, 0.4D, 0.4D);
    }

	private static void facepalmParticle(Player p) {
        World world = p.getLocation().getWorld();
        assert world != null;
        world.spawnParticle(Particle.CLOUD, p.getLocation().add(0, 1, 0), 3, 0.4D, 0.4D, 0.4D, 0.0001D);
    }

	private static void highfiveParticle(Player p) {
        World world = p.getLocation().getWorld();
        assert world != null;

        Particle critm;
        if(particleVersion >= 2) {
            critm = Particle.CRIT;
        } else {
            critm = Particle.valueOf("CRIT_MAGIC");
        }

        world.spawnParticle(critm, p.getLocation().add(0, 1, 0), 30, 0.4D, 0.4D, 0.4D);
    }

	private static void pokeParticle(Player p) {
        World world = p.getLocation().getWorld();
        assert world != null;

        Particle happy;
        if(particleVersion >= 2) {
            happy = Particle.HAPPY_VILLAGER;
        } else {
            happy = Particle.valueOf("VILLAGER_HAPPY");
        }

        world.spawnParticle(happy, p.getLocation().add(0, 1, 0), 15, 0.4D, 0.4D, 0.4D);
    }


	private static void lickParticle(Player p) {
        World world = p.getLocation().getWorld();
        assert world != null;

        Particle drip;
        if(particleVersion >= 2) {
            drip = Particle.DRIPPING_WATER;
        } else {
            drip = Particle.valueOf("DRIP_WATER");
        }

        Particle drop;
        if(particleVersion >= 2) {
            drop = Particle.FALLING_WATER;
        } else {
            drop = Particle.valueOf("WATER_DROP");
        }

        world.spawnParticle(drip, p.getLocation().add(0, 2, 0), 10, 0.2D, 0.5D, 0.2D);
        world.spawnParticle(drop, p.getLocation().add(0, 1, 0), 24, 1.0D, 0.5D, 1.0D);
    }

	private static void yellParticle(Player p) {
        World world = p.getLocation().getWorld();
        assert world != null;

        Particle explode;
        if(particleVersion >= 2) {
            explode = Particle.EXPLOSION;
        } else {
            explode = Particle.valueOf("EXPLOSION_HUGE");
        }

        world.spawnParticle(explode, p.getLocation().add(0, 1, 0), 1, 0.2D, 0.5D, 0.2D);
    }

}
