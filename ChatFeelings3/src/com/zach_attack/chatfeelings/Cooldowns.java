package com.zach_attack.chatfeelings;

import java.util.HashMap;
import com.zach_attack.chatfeelings.Main;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class Cooldowns implements Listener {

	static Main plugin = Main.getPlugin(Main.class);


	public static HashMap<Player, String> cooldown = new HashMap<Player, String>();
	public static HashMap<Player, String> spook = new HashMap<Player, String>();
	  
	  public static void startCooldown(final Player p)
	  {
		  try{
	    if (plugin.getConfig().getString("Cooldown-Delay-Seconds").equalsIgnoreCase("none"))
	    {
	      cooldown.remove(p);
	      return;
	    }
	    
	    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
	    {
	      public void run()
	      {
	        Cooldowns.cooldown.remove(p.getPlayer());
	      }
	    }, plugin.getConfig().getInt("Cooldown-Delay-Seconds") * 22);
		  }catch(Exception e){
			  System.out.print("[ChatFeelings] Error! Couldn't exceute the cooldown timer properly.");
			  if(plugin.getConfig().getBoolean("debug")) {
				  System.out.print("ChatFeelings Debug: " + e.getMessage());
			  }
			  }
		  }
}
