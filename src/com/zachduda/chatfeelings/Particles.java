package com.zachduda.chatfeelings;

import org.bukkit.*;
import org.bukkit.entity.Player;

public class Particles {
	
	private static final Main plugin = Main.getPlugin(Main.class);

		static void show(Player p, String label) {
            if (!Main.particles) {
                return;
            }

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
                dabParticle(p);
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
            } else if (label.equalsIgnoreCase("stalk")) {
                // No particle
            } else if (label.equalsIgnoreCase("sus")) {
                // No particle
            } else if (label.equalsIgnoreCase("wave")) {
                pokeParticle(p);
            } else {
                Main.debug("Couldn't find Particle for: /" + label.toLowerCase());
            }
        }

	private static void hugParticle(Player p) {
        World world = p.getLocation().getWorld();
        world.spawnParticle(Particle.HEART, p.getLocation().add(0, 1, 0), 9, 1.0D, 0.5D, 1.0D);
    }

	private static void biteParticle(Player p) {
        World world = p.getLocation().getWorld();
        world.spawnParticle(Particle.CRIT, p.getLocation().getX(), p.getLocation().getY() + 1.0D, p.getLocation().getZ(), 30, 0.4D, 0.4D, 0.4D);
	}

	private static void slapParticle(final Player p) {
        World world = p.getLocation().getWorld();
        world.spawnParticle(Particle.VILLAGER_ANGRY, p.getLocation().getX(), p.getLocation().getY() + 1.0D, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D);

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY() + 1.0D, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D);
            }
        }, 2L);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                world.spawnParticle(Particle.VILLAGER_ANGRY, p.getLocation().getX(), p.getLocation().getY() + 1.0D, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D);
            }
        }, 4L);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY() + 1.0D, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D);
            }
        }, 6L);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                world.spawnParticle(Particle.VILLAGER_ANGRY, p.getLocation().getX(), p.getLocation().getY() + 1.0D, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D);
            }
        }, 8L);
    }
	
	private static void punchParticle(final Player p) {
        World world = p.getLocation().getWorld();

        world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY() + 1.0D, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D);

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY() + 1.0D, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D);
            }
        }, 2L);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY() + 1.0D, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D);
            }
        }, 4L);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY() + 1.0D, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D);
            }
        }, 6L);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY() + 1.0D, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D);
            }
        }, 8L);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY() + 1.0D, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D);
            }
        }, 10L);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY() + 1.0D, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D);
            }
        }, 12L);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY() + 1.0D, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D);
            }
        }, 14L);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY() + 1.0D, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D);
            }
        }, 16L);
    }

	private static void murderParticle(final Player p) {
        World world = p.getLocation().getWorld();
        world.playEffect(p.getLocation().add(0.04D, 0.8D, 0.04D), Effect.STEP_SOUND, Material.RED_CONCRETE);

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                world.spawnParticle(Particle.LAVA, p.getLocation().add(0, 1, 0), 5, 0.0D, 0.6D, 0.0D);
            }
        }, 5L);
    }

	private static void dabParticle(Player p) {
        World world = p.getLocation().getWorld();
        world.spawnParticle(Particle.END_ROD, p.getLocation().add(0, 1, 0), 20, 0.4D, 0.4D, 0.4D);
    }

	private static void cryParticle(Player p) {
        World world = p.getLocation().getWorld();
        world.spawnParticle(Particle.WATER_SPLASH, p.getLocation().add(0, 1.5D, 0), 100, 0.4D, 0.4D, 0.4D);
    }

	private static void facepalmParticle(Player p) {
        World world = p.getLocation().getWorld();
        world.spawnParticle(Particle.CLOUD, p.getLocation().add(0, 1, 0), 3, 0.4D, 0.4D, 0.4D, 0.0001D);
    }

	private static void highfiveParticle(Player p) {
        World world = p.getLocation().getWorld();
        world.spawnParticle(Particle.CRIT_MAGIC, p.getLocation().add(0, 1, 0), 30, 0.4D, 0.4D, 0.4D);
    }

	private static void pokeParticle(Player p) {
        World world = p.getLocation().getWorld();
        world.spawnParticle(Particle.VILLAGER_HAPPY, p.getLocation().add(0, 1, 0), 15, 0.4D, 0.4D, 0.4D);
    }


	private static void lickParticle(Player p) {
        World world = p.getLocation().getWorld();
        world.spawnParticle(Particle.DRIP_WATER, p.getLocation().add(0, 2, 0), 10, 0.2D, 0.5D, 0.2D);
        world.spawnParticle(Particle.WATER_DROP, p.getLocation().add(0, 1, 0), 24, 1.0D, 0.5D, 1.0D);
    }

	private static void yellParticle(Player p) {
        World world = p.getLocation().getWorld();
        world.spawnParticle(Particle.EXPLOSION_HUGE, p.getLocation().add(0, 1, 0), 1, 0.2D, 0.5D, 0.2D);
    }

}
