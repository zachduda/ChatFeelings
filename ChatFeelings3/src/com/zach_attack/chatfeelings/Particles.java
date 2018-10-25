package com.zach_attack.chatfeelings;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.zach_attack.chatfeelings.Main;

public class Particles implements Listener{
	static Main plugin = Main.getPlugin(Main.class);
	
	  @SuppressWarnings("unused")
	public static void hugParticle(Player p)
	  {
		  try {
		  if(plugin.getConfig().getBoolean("particles")) {
		    for (Player online : Bukkit.getServer().getOnlinePlayers())
		    {
		      World world = p.getLocation().getWorld();
		      world.spawnParticle(Particle.HEART, p.getLocation(), 9, 1.0D, 1.0D, 1.0D);
		    }
		  }
		  }catch(Exception e) {
			  plugin.getLogger().info("Error! Couldn't display hug particles. Are you using 1.13?");
			  if(plugin.getConfig().getBoolean("debug")) {
			System.out.print("ChatFeelings Debug: "); e.printStackTrace();	  
			  }
		  }
	  }
	  
	  @SuppressWarnings("unused")
	public static void biteParticle(Player p)
	  {
		  try {
		  if(plugin.getConfig().getBoolean("particles")) {
		    for (Player online : Bukkit.getServer().getOnlinePlayers())
		    {
		      World world = p.getLocation().getWorld();
		      world.spawnParticle(Particle.CRIT, p.getLocation().getX(), p.getLocation().getY()+1, p.getLocation().getZ(), 30, 0.4D, 0.4D, 0.4D);
		    }
		  }
		  }catch(Exception e) {
			  plugin.getLogger().info("Error! Couldn't display bite particles. Are you using 1.13?");
			  if(plugin.getConfig().getBoolean("debug")) {
			System.out.print("ChatFeelings Debug: "); e.printStackTrace();	  
			  }
		  }
	  }
	  
	  @SuppressWarnings("unused")
	public static void punchParticle(Player p)
	  {
		  try {
		  if(plugin.getConfig().getBoolean("particles")) {
		    for (Player online : Bukkit.getServer().getOnlinePlayers())
		    {
		      World world = p.getLocation().getWorld();
	    	    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
	    	    {
	    	        public void run()
	    	        {
		      world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY()+1, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D);
	    	        }
	    	      }, 0L);
	    	    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
	    	    {
	    	        public void run()
	    	        {
		      world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY()+1, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D);
	    	        }
	    	      }, 2L);
	    	    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
	    	    {
	    	        public void run()
	    	        {
		      world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY()+1, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D);
	    	        }
	    	      }, 4L);
	    	    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
	    	    {
	    	        public void run()
	    	        {
		      world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY()+1, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D);
	    	        }
	    	      }, 6L);
	    	    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
	    	    {
	    	        public void run()
	    	        {
		      world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY()+1, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D);
	    	        }
	    	      }, 8L);
	    	    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
	    	    {
	    	        public void run()
	    	        {
		      world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY()+1, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D);
	    	        }
	    	      }, 10L);
	    	    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
	    	    {
	    	        public void run()
	    	        {
		      world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY()+1, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D);
	    	        }
	    	      }, 12L);
	    	    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
	    	    {
	    	        public void run()
	    	        {
		      world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY()+1, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D);
	    	        }
	    	      }, 14L);
	    	    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
	    	    {
	    	        public void run()
	    	        {
		      world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().getX(), p.getLocation().getY()+1, p.getLocation().getZ(), 1, 0.5D, 1.0D, 0.5D);
	    	        }
	    	      }, 16L);
	    	  }
		  }
	  }catch(Exception e) {
		  plugin.getLogger().info("Error! Couldn't display punch particles. Are you using 1.13?");
		  if(plugin.getConfig().getBoolean("debug")) {
		System.out.print("ChatFeelings Debug: "); e.printStackTrace();	  
		  }
	  }
	  }
	  
	  @SuppressWarnings("unused")
	public static void murderParticle(Player p)
	  {
		  try {
		  if(plugin.getConfig().getBoolean("particles")) {
		    for (Player online : Bukkit.getServer().getOnlinePlayers())
		    {
		      World world = p.getLocation().getWorld();
  		      world.playEffect(p.getLocation().add(0.04D, 0.8D, 0.04D), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
	    	    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
	    	    {
  	        public void run()
  	        {
;		      world.spawnParticle(Particle.LAVA, p.getLocation(), 5, 0.0D, 0.6D, 0.0D);
  	        }
  	      }, 5L);

		    }
		  }
	     }catch(Exception e) {
		  plugin.getLogger().info("Error! Couldn't display murder particles. Are you using 1.13?");
		  if(plugin.getConfig().getBoolean("debug")) {
		System.out.print("ChatFeelings Debug: "); e.printStackTrace();	  
		  }
	     }
	  }
	  
	  @SuppressWarnings("unused")
	public static void boiParticle(Player p)
	  {
		  try {
		  if(plugin.getConfig().getBoolean("particles")) {
		    for (Player online : Bukkit.getServer().getOnlinePlayers())
		    {
		      World world = p.getLocation().getWorld();
		      world.spawnParticle(Particle.END_ROD, p.getLocation(), 40, 0.0D, 0.0D, 0.0D); // 1.9 particles
		    }
		  }
	     }catch(Exception e) {
		  plugin.getLogger().info("Error! Couldn't display boi particles. Are you using 1.13?");
		  if(plugin.getConfig().getBoolean("debug")) {
		System.out.print("ChatFeelings Debug: "); e.printStackTrace();	  
		  }
	     }
	  }
	  
	  @SuppressWarnings("unused")
	public static void dabParticle(Player p)
	  {
		  try {
		  if(plugin.getConfig().getBoolean("particles")) {
		    for (Player online : Bukkit.getServer().getOnlinePlayers())
		    {
		      World world = p.getLocation().getWorld();
		      world.spawnParticle(Particle.END_ROD, p.getLocation(), 20, 0.4D, 0.4D, 0.4D); // 1.9 particles
		    }
		  }
	     }catch(Exception e) {
		  plugin.getLogger().info("Error! Couldn't display dab particles. Are you using 1.13?");
		  if(plugin.getConfig().getBoolean("debug")) {
		System.out.print("ChatFeelings Debug: "); e.printStackTrace();	  
		  }
	     }
	  }
	  
	  @SuppressWarnings("unused")
	public static void cryParticle(Player p)
	  {
		  try {
		  if(plugin.getConfig().getBoolean("particles")) {
		    for (Player online : Bukkit.getServer().getOnlinePlayers())
		    {
		      World world = p.getLocation().getWorld();
		      world.spawnParticle(Particle.WATER_SPLASH, p.getLocation(), 100, 0.4D, 0.4D, 0.4D); // 1.9 particles
		    }
		  }
	     }catch(Exception e) {
		  plugin.getLogger().info("Error! Couldn't display cry particles. Are you using 1.13?");
		  if(plugin.getConfig().getBoolean("debug")) {
		System.out.print("ChatFeelings Debug: "); e.printStackTrace();	  
		  }
	     }
	  }
	  
	  @SuppressWarnings("unused")
	public static void facepalmParticle(Player p)
	  {
		  try {
		  if(plugin.getConfig().getBoolean("particles")) {
		    for (Player online : Bukkit.getServer().getOnlinePlayers())
		    {
		      World world = p.getLocation().getWorld();
		      world.spawnParticle(Particle.EXPLOSION_NORMAL, p.getLocation(), 3, 0.4D, 0.4D, 0.4D); // 1.9 particles
		    }
		  }
	     }catch(Exception e) {
		  plugin.getLogger().info("Error! Couldn't display facepalm particles. Are you using 1.13?");
		  if(plugin.getConfig().getBoolean("debug")) {
		System.out.print("ChatFeelings Debug: "); e.printStackTrace();	  
		  }
	     }
	  }
	  
	  @SuppressWarnings("unused")
	public static void highfiveParticle(Player p)
	  {
		  try {
		  if(plugin.getConfig().getBoolean("particles")) {
		    for (Player online : Bukkit.getServer().getOnlinePlayers())
		    {
		      World world = p.getLocation().getWorld();
		      world.spawnParticle(Particle.CRIT_MAGIC, p.getLocation(), 30, 0.4D, 0.4D, 0.4D); // 1.9 particles
		    }
		  }
	     }catch(Exception e) {
		  plugin.getLogger().info("Error! Couldn't display highfive particles. Are you using 1.13?");
		  if(plugin.getConfig().getBoolean("debug")) {
		System.out.print("ChatFeelings Debug: "); e.printStackTrace();	  
		  }
	     }
	  }
	  
	  @SuppressWarnings("unused")
	public static void kissParticle(Player p)
	  {
		  try {
		  if(plugin.getConfig().getBoolean("particles")) {
		    for (Player online : Bukkit.getServer().getOnlinePlayers())
		    {
		      World world = p.getLocation().getWorld();
		      world.spawnParticle(Particle.HEART, p.getLocation(), 9, 1.0D, 1.0D, 1.0D);// 1.9 particles
		    }
		  }
	     }catch(Exception e) {
		  plugin.getLogger().info("Error! Couldn't display kiss particles. Are you using 1.13?");
		  if(plugin.getConfig().getBoolean("debug")) {
		System.out.print("ChatFeelings Debug: "); e.printStackTrace();	  
		  }
	     }
	  }
	  
	  @SuppressWarnings("unused")
	public static void lickParticle(Player p)
	  {
		  try {
		  if(plugin.getConfig().getBoolean("particles")) {
		    for (Player online : Bukkit.getServer().getOnlinePlayers())
		    {
		      World world = p.getLocation().getWorld();
		      world.spawnParticle(Particle.DRIP_WATER, p.getLocation(), 30, 0.2D, 0.5D, 0.2D);// 1.9 particles
		      world.spawnParticle(Particle.WATER_DROP, p.getLocation(), 100, 1.0D, 1.0D, 1.0D);// 1.9 particles
		    }
		  }
	     }catch(Exception e) {
		  plugin.getLogger().info("Error! Couldn't display lick particles. Are you using 1.13?");
		  if(plugin.getConfig().getBoolean("debug")) {
		System.out.print("ChatFeelings Debug: "); e.printStackTrace();	  
		  }
	     }
	  }
	  
}
