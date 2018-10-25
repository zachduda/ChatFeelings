package com.zach_attack.chatfeelings;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import com.zach_attack.chatfeelings.Main;

public class Messages implements Listener {
	 private static Main plugin = Main.getPlugin(Main.class);  //Changed to statics 
	 
	    private static void setMessage(String name, String message) {
	        File f = new File(plugin.getDataFolder()+File.separator+"messages.yml");
	        FileConfiguration config = YamlConfiguration.loadConfiguration(f);
	        if (!config.isSet(name)) {
	            config.set(name, message);
	            try {
	                config.save(f);
	            } catch (IOException e) {
	            	Bukkit.getServer().getLogger().info("[ChatFeelings]  > Error when setting " + name + " message in messages.yml."); 
	            	e.printStackTrace();
	            	Bukkit.getServer().getLogger().info("[ChatFeelings]  ! Use the error above to figure out what's wrong.");
	            }
	        }
	    }
	    
	    private void setEmotes(String name, boolean status) {
	        File f = new File(plugin.getDataFolder()+File.separator+"emotes.yml");
	        FileConfiguration config1 = YamlConfiguration.loadConfiguration(f);
	        if (!config1.isSet(name)) {
	            config1.set(name, status);
	            try {
	                config1.save(f);
	            } catch (IOException e) {
	            	Bukkit.getServer().getLogger().info("[ChatFeelings]  > Error when setting " + name + " boolean in emotes.yml."); 
	            	e.printStackTrace();
	            	Bukkit.getServer().getLogger().info("[ChatFeelings]  ! Use only true/false statements in emotes.yml !");
	            }
	        }
	    }
	    
	    private void setSound(String name, String sound) {
	        File f = new File(plugin.getDataFolder()+File.separator+"sounds.yml");
	        FileConfiguration soundconfig = YamlConfiguration.loadConfiguration(f);
	        if (!soundconfig.isSet(name)) {
	            soundconfig.set(name, sound);
	            try {
	                soundconfig.save(f);
	            } catch (IOException e) {
	            	Bukkit.getServer().getLogger().info("[ChatFeelings]  > Error when setting " + name + " sound in sounds.yml."); 
	      		  if(plugin.getConfig().getBoolean("debug")) {
	      			  Bukkit.getServer().getLogger().info("ChatFeelings Debug: " + e.getMessage());
	      			  }
	            	Bukkit.getServer().getLogger().info("[ChatFeelings]  ! Make sure you use sounds that support your spigot version!");
	            }
	        }
	    }
	    
	    private void setConfig(String name, boolean value) {
	        File f = new File(plugin.getDataFolder()+File.separator+"config.yml");
	        FileConfiguration regconfig = YamlConfiguration.loadConfiguration(f);
	        if (!regconfig.isSet(name)) {
	            regconfig.set(name, value);
	            try {
	                regconfig.save(f);
	            } catch (IOException e) {
	            	Bukkit.getServer().getLogger().info("[ChatFeelings]  > Error when setting " + name + " sound in config.yml."); 
	      		  if(plugin.getConfig().getBoolean("debug")) {
	      			  Bukkit.getServer().getLogger().info("ChatFeelings Debug: " + e.getMessage());
	      			  }
	            }
	        }
	     }
		    private void setConfigNum(String name, int value) {
		        File f = new File(plugin.getDataFolder()+File.separator+"config.yml");
		        FileConfiguration regconfig = YamlConfiguration.loadConfiguration(f);
		        if (!regconfig.isSet(name)) {
		            regconfig.set(name, value);
		            try {
		                regconfig.save(f);
		            } catch (IOException e) {
		            	Bukkit.getServer().getLogger().info("[ChatFeelings]  > Error when setting " + name + " value in config.yml."); 
		      		  if(plugin.getConfig().getBoolean("debug")) {
		      			  Bukkit.getServer().getLogger().info("ChatFeelings Debug: " + e.getMessage());
		      			  }
		            }
		        }
	    }

	 public Messages() {
	    File file = new File(plugin.getDataFolder() + File.separator + "messages.yml");
	    File file1 = new File(plugin.getDataFolder() + File.separator + "emotes.yml");
	    File soundfile = new File(plugin.getDataFolder() + File.separator + "sounds.yml");
        File f = new File(plugin.getDataFolder(), "config.yml");
        
        if(!f.exists()) {
        	Bukkit.getServer().getLogger().info("[ChatFeelings]  > Config not detected. Creating new one...");
      	 
        	try {    
        	f.delete();
      	    } catch (Exception e1){
      		  if(plugin.getConfig().getBoolean("debug")) {
      			  Bukkit.getServer().getLogger().info("ChatFeelings Debug: " + e1.getMessage());
      			  }
      	    }
      	          plugin.saveDefaultConfig();
      	     	 plugin.reloadConfig();
      	     	Bukkit.getServer().getLogger().info("[ChatFeelings]  > Done! New configuration was created.");       
	}

     if(!soundfile.exists()) {
    	 if(Bukkit.getVersion().contains("1.13") || Bukkit.getVersion().contains("1.14")) {
    		 System.out.print("[ChatFeelings] Detected 1.13. Setting 1.13 sounds in the sounds.yml");
    	 }
    	 if(Bukkit.getVersion().contains("1.12")) {
    		 System.out.print("[ChatFeelings] Detected 1.12. Setting 1.12 sounds in the sounds.yml");
    	 }
    	 if(Bukkit.getVersion().contains("1.11")) {
    		 System.out.print("[ChatFeelings] Detected 1.11. Setting 1.11 sounds in the sounds.yml");
    	 }
    	 if(Bukkit.getVersion().contains("1.10")) {
    		 System.out.print("[ChatFeelings] Detected 1.10. Setting 1.10 sounds in the sounds.yml");
    	 }
    	 if(Bukkit.getVersion().contains("1.9")) {
    		 System.out.print("[ChatFeelings] Detected 1.9. Setting 1.9 sounds in the sounds.yml");
    	 }
    	 if(Bukkit.getVersion().contains("1.8")) {
    		 System.out.print("[ChatFeelings] Detected 1.8. Setting 1.8 sounds in the sounds.yml");
    	 }
    	 if(Bukkit.getVersion().contains("1.7") || Bukkit.getVersion().contains("1.6")) {
    		 System.out.print("[ChatFeelings] Detected 1.7 or below.");
    	 }
    	 Bukkit.getServer().getLogger().info("[ChatFeelings]  > Sounds.yml not detected. Creating a new one...");
    	    FileConfiguration sounds = YamlConfiguration.loadConfiguration(soundfile);
    	    try {
    	    soundfile.createNewFile();	
    	    sounds.save(soundfile);
    	    try {
    			sounds.load(soundfile);
    		} catch (InvalidConfigurationException e2) {
    		    System.out.print("[ChatFeelings] Unable to load Emotes.yml. Try again, and if it fails let us know.");
    			  if(plugin.getConfig().getBoolean("debug")) {
    			  Bukkit.getServer().getLogger().info("ChatFeelings Debug: "); e2.printStackTrace();
    			  }}
    	    } catch (IOException e1) {
    			  if(plugin.getConfig().getBoolean("debug")) {
    				  Bukkit.getServer().getLogger().info("ChatFeelings Debug: " + e1.getMessage());
    				  }
    	    System.out.print("[ChatFeelings] Something wen't wrong while trying to create the sounds.yml file.");
    	    }
       }
        
    if(!file.exists()){
    	Bukkit.getServer().getLogger().info("[ChatFeelings]  > Messages.yml not detected. Creating new one...");
    FileConfiguration config = YamlConfiguration.loadConfiguration(file);
    try {
    file.createNewFile();	
    config.save(file);
    try {
		config.load(file);
	} catch (InvalidConfigurationException e2) {
	    System.out.print("[ChatFeelings] Unable to load Emotes.yml. Try again, and if it fails let us know.");
		  if(plugin.getConfig().getBoolean("debug")) {
		  Bukkit.getServer().getLogger().info("ChatFeelings Debug: "); e2.printStackTrace();
		  }}
    } catch (IOException e1) {
		  if(plugin.getConfig().getBoolean("debug")) {
		  Bukkit.getServer().getLogger().info("ChatFeelings Debug: " + e1.getMessage());
		  }
    System.out.print("[ChatFeelings] Something wen't wrong while trying to create the messages.yml file.");
       }
     }
    
    if(!file1.exists()){  // Emotes Creation
    FileConfiguration config1 = YamlConfiguration.loadConfiguration(file1);
    try {
        Bukkit.getServer().getLogger().info("[ChatFeelings]  > Emotes.yml not detected. Creating new one...");
        file1.createNewFile();	
        config1.save(file1);
        try {
			config1.load(file1);
		} catch (InvalidConfigurationException e2) {
		    System.out.print("[ChatFeelings] Unable to load Emotes.yml. Try again, and if it fails let us know.");
			  if(plugin.getConfig().getBoolean("debug")) {
			  Bukkit.getServer().getLogger().info("ChatFeelings Debug: "); e2.printStackTrace();
			  }
		}
    } catch (IOException e1) {
		  if(plugin.getConfig().getBoolean("debug")) {
		  Bukkit.getServer().getLogger().info("ChatFeelings Debug: "); e1.printStackTrace();
		  }
    System.out.print("[ChatFeelings] Something wen't wrong while trying to create the emotes.yml file.");
       }
    }
    
	  try {
		  if(plugin.getConfig().getInt("config-version") == plugin.configVersion) {
			 plugin.getLogger().info("Config is up to date! Nothing needs to be changed."); 
		  } else {
			  System.out.print("-----------------------------------------------------");
			  if(plugin.getServer().getPluginManager().isPluginEnabled("PlugMan") && (plugin.getServer().getPluginManager().getPlugin("PlugMan") != null)) {			  
			  plugin.getLogger().info("Update Installed. Since you have PlugMan, we'll reload ChatFeelings.");
			  } else {
			plugin.getLogger().info("Update Installed. Please restart your server twice with ChatFeelings!");		  
			  }
			  plugin.getLogger().info("Now updating your Config...");
			  System.out.print("-----------------------------------------------------");
			  plugin.getConfig().set("config-version", plugin.configVersion);
			  plugin.getConfig().set("spook-helemet-check", null);
			  plugin.saveConfig();
			  plugin.reloadConfig();
			  plugin.saveConfig();
			  
	    	    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
	    	    {
	    	        public void run()
	    	        {
	    				  plugin.getLogger().info("Reloading ChatFeelings to ensure updates...");
	    	    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "plugman reload ChatFeelings");
	    	    plugin.getLogger().info("--------- Updates Completed! ----------");
	    	        }
	    	      }, 125L);
		  }
	  }catch(Exception e) {
		  plugin.getLogger().info("Config couldn't be updated. Please erase your config and try again!");   
	  }
	  
//Sounds
    setSound("NOTE", "Im working on adding pitch, and volume options soon.");
    if(Bukkit.getVersion().contains("1.13") || Bukkit.getVersion().contains("1.14")) { //Preparing for 1.14 should it come soon lol
    	setSound("command-sound", "ENTITY_CHICKEN_EGG");
        setSound("error-sound", "BLOCK_NOTE_BLOCK_BASS");
        setSound("hug-sound", "ENTITY_CAT_PURREOW");
        setSound("bite-sound", "ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR");
        setSound("punch-sound", "ENTITY_IRON_GOLEM_ATTACK");
        setSound("murder-sound", "ENTITY_BLAZE_DEATH");
        setSound("boi-sound", "ENTITY_CHICKEN_EGG");
        setSound("dab-sound", "ENTITY_CHICKEN_EGG");
        setSound("cry-sound", "ENTITY_GHAST_DEATH");
        setSound("facepalm-sound", "ENTITY_VILLAGER_NO");    
        setSound("highfive-sound", "ENTITY_VILLAGER_YES");  
        setSound("kiss-sound", "ENTITY_ARROW_HIT_PLAYER");
        setSound("lick-sound", "ENTITY_GENERIC_DRINK");
        setSound("shake-sound", "ENTITY_WOLF_SHAKE");
        setSound("snuggle-sound", "ENTITY_CAT_PURR");
        setSound("stab-sound", "ENTITY_GENERIC_HURT");
        setSound("yell-sound", "ENTITY_GHAST_SCREAM");
    	setSound("poke-sound", "ENTITY_CHICKEN_EGG");
    	setSound("slap-sound", "ENTITY_BLAZE_HURT");
    	setSound("pat-sound", "ENTITY_WOLF_PANT");
    	setSound("scorn-sound", "ENTITY_ENDERMAN_STARE");
    	setSound("stalk-sound", "AMBIENT_CAVE");
    	//setSound("spook-sound", "MUSIC_DISC_13");
    }
   
    if(Bukkit.getVersion().contains("1.12") || Bukkit.getVersion().contains("1.11")
    || Bukkit.getVersion().contains("1.10") || Bukkit.getVersion().contains("1.9")) {
    	setSound("command-sound", "ENTITY_CHICKEN_EGG");
        setSound("error-sound", "BLOCK_NOTE_BASS");
        setSound("hug-sound", "ENTITY_CAT_PURREOW");
        setSound("bite-sound", "ENTITY_ZOMBIE_ATTACK_DOOR_WOOD");
        setSound("punch-sound", "ENTITY_IRONGOLEM_ATTACK");
        setSound("murder-sound", "ENTITY_BLAZE_DEATH");
        setSound("boi-sound", "ENTITY_CHICKEN_EGG");
        setSound("dab-sound", "ENTITY_CHICKEN_EGG");
        setSound("cry-sound", "ENTITY_GHAST_DEATH");
        setSound("facepalm-sound", "ENTITY_VILLAGER_NO");    
        setSound("highfive-sound", "ENTITY_VILLAGER_YES");  
        setSound("kiss-sound", "ENTITY_ARROW_HIT_PLAYER");
        setSound("lick-sound", "ENTITY_GENERIC_DRINK");
        setSound("shake-sound", "ENTITY_WOLF_SHAKE");
        setSound("snuggle-sound", "ENTITY_CAT_PURR");
        setSound("yell-sound", "ENTITY_GHAST_SCREAM");
    	setSound("poke-sound", "ENTITY_CHICKEN_EGG");
    	setSound("slap-sound", "ENTITY_BLAZE_HURT");
    	setSound("pat-sound", "ENTITY_WOLF_PANT");
    	setSound("scorn-sound", "ENTITY_ENDERMEN_STARE");
        setSound("stab-sound", "ENTITY_GENERIC_HURT");
    	setSound("stalk-sound", "AMBIENT_CAVE");
    }
    
    if(Bukkit.getVersion().contains("1.8") || Bukkit.getVersion().contains("1.7") || Bukkit.getVersion().contains("1.6") || Bukkit.getVersion().contains("1.5") || Bukkit.getVersion().contains("1.4")) {
    	setSound("command-sound", "CHICKEN_EGG_POP");
        setSound("error-sound", "NOTE_BASS");
        setSound("hug-sound", "CAT_PURREOW");
        setSound("bite-sound", "ZOMBIE_WOODBREAK");
        setSound("punch-sound", "IRONGOLEM_HIT");
        setSound("murder-sound", "BLAZE_DEATH");
        setSound("boi-sound", "CHICKEN_EGG_POP");
        setSound("dab-sound", "CHICKEN_EGG_POP");
        setSound("cry-sound", "GHAST_DEATH");
        setSound("facepalm-sound", "VILLAGER_NO");    
        setSound("highfive-sound", "VILLAGER_YES");  
        setSound("kiss-sound", "ARROW_HIT");
        setSound("lick-sound", "DRINK");
        setSound("shake-sound", "WOLF_SHAKE");
        setSound("snuggle-sound", "CAT_PURR");
        setSound("yell-sound", "GHAST_SCREAM");
    	setSound("poke-sound", "CHICKEN_EGG_POP");
    	setSound("slap-sound", "BLAZE_HIT");
    	setSound("pat-sound", "WOLF_PANT");
    	setSound("scorn-sound", "ENDERMAN_STARE");
        setSound("stab-sound", "HURT_FLESH");
    	setSound("stalk-sound", "AMBIENCE_CAVE");
    }
    
// Other messages (prefix)    
    setMessage("prefix", "&aFeelings &8&l▏");
    setMessage("self-feeling", "&c&lHmm! &fYou can't &7%cmd% &fyourself!");
    setMessage("player-not-online", "&c&lOops! &7%target% &fisn't online to do that.");
    setMessage("no-permission-msg", "&c&lWhoops! &fYou don't have permission for this.");
    setMessage("no-feeling-permission-msg", "&c&lWhoops! &fYou're not able to &7%cmd%");
    setMessage("extra-help-command", "&fTry doing this &7/%cmd% &7(player)");
    setMessage("disabled-feeling-message", "&c&lSorry! &7/%cmd% &fhas been disabled.");
    setMessage("too-many-args-message", "&c&lOops! &fYou added too many arguments for that.");
    setMessage("no-player-provided", "&c&lOops! &fYou need to provide a player to &7%cmd%&f.");
    setMessage("feelings-list-help", "&c&lOops! &fJust try &7/feelings &ffor a list.");
    setMessage("invalid-page", "&c&lOops! &fCan't find page &7#%arg%");
    setMessage("cooldown-active", "&c&lSlow Down! &fYou need to wait to &7%cmd% &fsomeone.");
    setMessage("player-is-console", "&c&lYou Silly! &fYou can't &7%cmd% &fthe console.");
// EMOTION MESSAGES (no prefix)
    setMessage("hug-target", "&a%sender% &7gives you a warm hug. &cAwww &4❤");
    setMessage("hug-sender", "&7You give &a%target% &7a warm hug. &cAwww &4❤");
    setMessage("hug-global", "&a%sender% &7gave &2%target% &7a warm hug. &cAwww &4❤");
    
    setMessage("bite-target", "&c%sender% &7sinks their teeth into your skin.");
    setMessage("bite-sender", "&7You sink your teeth info &c%target%&7's skin.");
    setMessage("bite-global", "&c%sender% &7sank their teeth into &4%target%'s &7skin.");
    
    setMessage("punch-target", "&c%sender% &7strikes you with a punch. Ouch!");
    setMessage("punch-sender", "&7You strike &c%target% &7with a punch. Ouch!");
    setMessage("punch-global", "&c%sender% &7punched &4%target% &7right in the face.");
    
    setMessage("murder-target", "&c%sender% &7has just murdered you. Bandaid anyone?");
    setMessage("murder-sender", "&7You murder &c%target% &7and have no regrets.");
    setMessage("murder-global", "&c%sender% &7just murdered &4%target%&7. &8RIP.");
    
    setMessage("boi-target", "&e%sender% &7inhales at you... &6&lBOI");
    setMessage("boi-sender", "&7You inhales at &e%target%&7... &6&lBOI");
    setMessage("boi-global", "&e%sender% &7inahles at &6%target%&7... &6&lBOI");
    
    setMessage("dab-target", "&a%sender% &7freshly dabs on you... Nailed it.");
    setMessage("dab-sender", "&7You freshly dab on &a%target%&7... Nailed it.");
    setMessage("dab-global", "&a%sender% &7freshly dabs on &2%target%&7. Nailed it.");
    
    setMessage("cry-target", "&b%sender% &7cries on your shoulder.");
    setMessage("cry-sender", "&7You cry on &b%target%'s &7shoulder.");
    setMessage("cry-global", "&b%sender% &7leans on &3%target% &7and cries.");
    
    setMessage("facepalm-target", "&e%sender% &7facepalms at your stupidity.");
    setMessage("facepalm-sender", "&7You facepalm at &e%target%'s &7stupidity.");
    setMessage("facepalm-global", "&e%sender% &7facepalms at &6%target%'s &7stupidity.");
    
    setMessage("highfive-target", "&a%sender% &7gives you a mighty highfive.");
    setMessage("highfive-sender", "&7You give a mighty highfive to &a%target%&7.");
    setMessage("highfive-global", "&a%sender% &7gives &2%target% &7a mighty highfive.");
    
    setMessage("kiss-target", "&a%sender% &7gives you a kiss. &cAwww &4❤");
    setMessage("kiss-sender", "&7You give &a%target% &7a kiss. &cAwww &4❤");
    setMessage("kiss-global", "&a%sender% &7gives &2%target% &7a kiss. &cAwww &4❤");
    
    setMessage("lick-target", "&e%sender% &7licks you like ice-cream. &6Gross!");
    setMessage("lick-sender", "&7You lick &e%target% &7like ice-cream. &6Gross!");
    setMessage("lick-global", "&e%target% &7got licked by &6%sender%&7. &8Gross.");
    
    setMessage("shake-target", "&c%sender% &7shakes your entire body.");
    setMessage("shake-sender", "&7You shake &c%target%'s &7entire body.");
    setMessage("shake-global", "&c%sender% &7picks up &4%target%'s &7body, and shakes it.");
    
    setMessage("snuggle-target", "&a%sender% &7snuggles you with love. &cAwww &4❤");
    setMessage("snuggle-sender", "&7You snuggle &a%target% &7with love. &cAwww &4❤");
    setMessage("snuggle-global", "&a%sender% &7snuggles &2%target% &7them with hugs. &cAwww &4❤");
    
    setMessage("yell-target", "&c%sender% &7yells at you from the top of their lungs.");
    setMessage("yell-sender", "&7You yell at &c%target%'s &7at the top of your lungs.");
    setMessage("yell-global", "&c%sender% &7yells right at &4%target% &7from the top of their lungs.");
    
    setMessage("poke-target", "&e%sender% &7has poked you. Anyone there?");
    setMessage("poke-sender", "&7You poked &e%target%&7. Maybe they're on vacation?");
    setMessage("poke-global", "&e%target% &7was poked by &6%sender%&7. &7&oHelooo?");
    
    setMessage("slap-target", "&c%sender% &7slaps you with some spaghetti.");
    setMessage("slap-sender", "&7You slap &c%target% &7with some spaghetti.");
    setMessage("slap-global", "&c%target% &7was slapped by &4%sender%&7.");
    
    setMessage("stab-target", "&c%sender% &7grabs a knife and stabs you. Got Bandaids?");
    setMessage("stab-sender", "&7You stab &c%target% &7with a knife. Got Bandaids?");
    setMessage("stab-global", "&c%sender% &7grabes a knife and stabs &4%target%&7.");
    
    setMessage("pat-target", "&a%sender% &7gently pats your head for being good.");
    setMessage("pat-sender", "&7You gently pat &a%target%'s &7head for being good.");
    setMessage("pat-global", "&a%sender% &7gently pats &2%target%'s &7head for being good.");
    
    setMessage("scorn-target", "&c%sender% &7scorns you for what you've done.");
    setMessage("scorn-sender", "&7You scorn &c%target% &7for what they've done.");
    setMessage("scorn-global", "&c%sender% &7scorns &4%target% &7for what they've done.");
    
    setMessage("stalk-target", "&e%sender% &7stalks you from a nearby tree.");
    setMessage("stalk-sender", "&7You carefully stalk &e%target%&7, &7&oHeh Heh.");
    setMessage("stalk-global", "&e%sender% &7stalks &6%target% &7from a nearby tree.");

    setEmotes("hug-active", true);
    setEmotes("bite-active", true);
    setEmotes("punch-active", true);
    setEmotes("murder-active", true);
    setEmotes("boi-active", true);
    setEmotes("dab-active", true);
    setEmotes("facepalm-active", true);
    setEmotes("cry-active", true);
    setEmotes("highfive-active", true);
    setEmotes("kiss-active", true);
    setEmotes("lick-active", true);
    setEmotes("shake-active", true);
    setEmotes("snuggle-active", true);
    setEmotes("yell-active", true);
    setEmotes("poke-active", true);
    setEmotes("slap-active", true);
    setEmotes("stab-active", true);
    setEmotes("pat-active", true);
    setEmotes("scorn-active", true);
    setEmotes("stalk-active", true);
    // Config.yml sets
    setConfig("sounds", true);
    setConfig("violent-commands-damage", true);
    setConfig("Murder-Command-Kills-Player", false);
    setConfig("Update-Notify", true);
    setConfig("Developer-Join", true);
    setConfig("reload-notify-console", false);
    setConfigNum("Cooldown-Delay-Seconds", Integer.valueOf(3));
    setConfig("extra-help", true);
    setConfig("particles", true);
    setConfig("other-effects", true);
    setConfig("debug", false);
    setConfig("Metrics", true);
    setConfigNum("config-version", plugin.configVersion);
  }	
}
	 
