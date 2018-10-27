package com.zach_attack.chatfeelings;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.zach_attack.cf.other.Metrics;
import com.zach_attack.cf.other.Updater;
import com.zach_attack.chatfeelings.Messages;

public class Main
  extends JavaPlugin
  implements Listener
{
  FileConfiguration config;
  File cFile;

  File file = new File(this.getDataFolder() + File.separator + "messages.yml");
  File file1 = new File(this.getDataFolder() + File.separator + "emotes.yml");
  File soundfile = new File(this.getDataFolder() + File.separator + "sounds.yml");
  FileConfiguration messages = YamlConfiguration.loadConfiguration(file);
  FileConfiguration emotes = YamlConfiguration.loadConfiguration(file1);
  FileConfiguration sounds = YamlConfiguration.loadConfiguration(soundfile);
  
  String vv = "v" + getDescription().getVersion();
  Server server = Bukkit.getServer();
  
  //       Please please please read the READ_ME.txt before doing anything. Thanks.  //
  
  int configVersion = 4;
  
  public void addMetric() {
	  double version = Double.parseDouble(System.getProperty("java.specification.version"));
	  if(version < 1.8) {
		  if(getConfig().getBoolean("debug")) {
			  System.out.print("ChatFeelings Debug: Found Java Verion: " + version);
			  System.out.print("ChatFeelings Debug: Java version is below 8.");
		  }
		  getLogger().info("Java " + version+ " detected. ChatFeelings requires Java 8 or higher.");
		  getLogger().info("For old java support disable Metrics. ChatFeeling's will continue, but you've been warned.");
		  getLogger().info("If you believe this is an error, enable debug mode and see..");
	  } else { 
	  if(getConfig().getBoolean("Metrics")) {
		  if(getConfig().getBoolean("debug")) {
			  System.out.print("ChatFeelings Debug: Found Java Verion: " + version);
			  System.out.print("ChatFeelings Debug: Java version is above 8. Continuing plugin....");
		  System.out.print("ChatFeelings Debug: Injecting metrics from onEnable() method.");
		  }
	    Metrics metrics = new Metrics(this);
        metrics.addCustomChart(new Metrics.SimplePie("server_version", () -> {
            try {
                Class.forName("com.destroystokyo.paper.PaperConfig");
                return "Paper";
            }
            catch (Exception NotPaper) {
                try {
                    Class.forName("org.spigotmc.SpigotConfig");
                    return "Spigot";
                }
                catch (Exception Other) {
                    return "Bukkit / Other";
                }
            }
        }));
        
        metrics.addCustomChart(new Metrics.SimplePie("developer_join", () -> {
            if(getConfig().getBoolean("Developer-Join")) {
                return "Enabled";
            } else {
            	return "Disabled";
            }
        }));
        
        metrics.addCustomChart(new Metrics.SimplePie("update_notifications", () -> {
            if(getConfig().getBoolean("Update-Notify")) {
                return "Enabled";
            } else {
            	return "Disabled";
            }
        }));
        
        metrics.addCustomChart(new Metrics.SimplePie("sound_effects", () -> {
            if(getConfig().getBoolean("sounds")) {
                return "Enabled";
            } else {
            	return "Disabled";
            }
        }));
        
        metrics.addCustomChart(new Metrics.SimplePie("debug", () -> {
            if(getConfig().getBoolean("debug")) {
                return "Enabled";
            } else {
            	return "Disabled";
            }
        }));
        
        metrics.addCustomChart(new Metrics.SimplePie("murder_command_kills", () -> {
            if(getConfig().getBoolean("Murder-Command-Kills-Player")) {
                return "Yes";
            } else {
            	return "No";
            }
        }));
        
        metrics.addCustomChart(new Metrics.SimplePie("violent_commands_hurt", () -> {
            if(getConfig().getBoolean("violent-commands-damage")) {
                return "Yes";
            } else {
            	return "No";
            }
        }));
        
        metrics.addCustomChart(new Metrics.SimplePie("reload_notify_console", () -> {
            if(getConfig().getBoolean("reload-notify-console")) {
                return "Yes";
            } else {
            	return "No";
            }
        }));
        
        metrics.addCustomChart(new Metrics.SimplePie("extra_help", () -> {
            if(getConfig().getBoolean("extra-help")) {
                return "Enabled";
            } else {
            	return "Disabled";
            }
        }));
        
        metrics.addCustomChart(new Metrics.SimplePie("PlugMan", () -> {
       if(getServer().getPluginManager().isPluginEnabled("PlugMan") && (getServer().getPluginManager().getPlugin("PlugMan") != null)) {
                return "Found";
            } else {
            	return "Not Found";
            }
        }));
        
        metrics.addCustomChart(new Metrics.SimplePie("cooldown", () -> {
                return Integer.toString(getConfig().getInt("Cooldown-Delay-Seconds"));
        }));
        
	  
	  }}} // End Metric / End Else Statement // end boolean
  
  @Override
  public void onEnable() {
	  if(!(messages.getString("prefix") == null)) {
      try {
          Class.forName("com.destroystokyo.paper.PaperConfig");
          getLogger().info("NOTE: ChatFeeling's hasn't been completely tested on Paper Spigot yet.");
      }catch (Exception e) {
      }
      
	  if(getConfig().getBoolean("debug")) {
		  System.out.print("ChatFeelings Debug: Server Enviroment = " + Bukkit.getBukkitVersion());  
	  }
      	getLogger().info("NOTE: This is a BETA and is not inteded for production usage..");
	    //______________ M E T R I C S ______________________
               addMetric();
	    //__________________________________________________
	  } // end of if null check
	    
	  Bukkit.getPluginManager().registerEvents(new Messages(), this);  //REGISTER MESSAGES.CLASS
	  Bukkit.getPluginManager().registerEvents(new Cooldowns(), this);

  saveDefaultConfig();
  saveConfig();
  Bukkit.getServer().getPluginManager().registerEvents(this, this);
  if(!(messages.getString("prefix") == null)) { 
  if (!getConfig().getBoolean("Update-Notify"))
  {
    getLogger().info("Update Notifications have been turned off in the config.yml :(");
  }

  if(getServer().getPluginManager().isPluginEnabled("PlugMan") && (getServer().getPluginManager().getPlugin("PlugMan") != null)) {
	  getLogger().info("Hooking with PlugMan for reload commands...");
  }
  if (!getConfig().getBoolean("Developer-Join"))
  {
	  getLogger().info("Dev-Join has been disabled in the config.yml.");
  }
  if (!getConfig().getBoolean("Metrics"))
  {
	  getLogger().info("Metrics have been disabled in the config.yml.");
	  getLogger().info("NOTE: Metrics have little to no affect on your server.");
  }
  if (getConfig().getBoolean("global-feelings"))
  {
	  getLogger().info("Feeling commands have been set in the config.yml to be broadcasted.");
  }else {
	  getLogger().info("Feeling commands have been set in the config.yml to be sent localy.");	  
  }
  if(getConfig().getBoolean("particles")) {
	  getLogger().info("Particles have been enabled.");
  } else {
	  getLogger().info("Particles have been disabled.");
  }
	  }// end of if null
  try {
  YamlConfiguration.loadConfiguration(this.file);
  YamlConfiguration.loadConfiguration(this.file1);
  YamlConfiguration.loadConfiguration(this.soundfile);
  }catch(Exception e) {getLogger().info("Issue when attempting to load files.");}
  try {
	  if(messages.getString("prefix") == null) {
		  if(getServer().getPluginManager().isPluginEnabled("PlugMan") && (getServer().getPluginManager().getPlugin("PlugMan") != null)) {
			  getLogger().info("First time? Plugin will reload to properly load files.");
	    	    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
	    	    {
	    	        public void run()
	    	        {
	    				  getLogger().info("Waiting for complete startup of ChatFeelings before reloading...");
	    	        }
	    	      }, 10L);
	    	    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
	    	    {
	    	        public void run()
	    	        {
	    				  getLogger().info("Reloading ChatFeelings in 3 seconds.");
	    	        }
	    	      }, 50L);
	    	    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
	    	    {
	    	        public void run()
	    	        {
	    				  getLogger().info("Reloading ChatFeelings in 2 seconds.");
	    	        }
	    	      }, 82L);
	    	    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
	    	    {
	    	        public void run()
	    	        {
	    				  getLogger().info("Reloading ChatFeelings in 1 second.");
	    	        }
	    	      }, 104L);
	    	    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
	    	    {
	    	        public void run()
	    	        {
	    	    getServer().dispatchCommand(getServer().getConsoleSender(), "plugman reload ChatFeelings");
	    	        }
	    	      }, 125L);
		  } else {
		  System.out.print("########################################################");
		  getLogger().info("SORRY! We had trouble loading the messages.yml.");
		  getLogger().info("(This is a known BETA bug, we are working on a fix.)");
		  getLogger().info("A server restart normally fixes this issue asap.");
		  System.out.print("########################################################");  
          for(final Player online:Bukkit.getServer().getOnlinePlayers())
          {
        	  if(online.hasPermission("chatfeelings.admin") || online.isOp()){
        	      online.sendMessage("§a§lChatFeelings §r§8§m-------------------------------------");
        	      online.sendMessage("§fThanks for installing ChatFeelings, sadly, there is a");
        	      online.sendMessage("§fknown bug that needs your server to be completely");
        	      online.sendMessage("§frestarted with ChatFeelings' files already in place to load.");
        	      online.sendMessage("§c§lSorry! §7We will fix this bug as soon as possible.");
        	      online.sendMessage("§r§8§m------------------------------------------------");
        	      if(Bukkit.getBukkitVersion().contains("1.12") || Bukkit.getBukkitVersion().contains("1.13")) {
        	    	  online.playSound(online.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.5F, 2.0F);
        	      }
        	  }
		}
		  }
	  } else {
	  getLogger().info("Prefix set to: "+messages.getString("prefix"));
	  }
  } catch (Exception e) {
	  System.out.print("########################################################");
	  getLogger().info("SORRY! We had trouble loading the messages.yml.");
	  getLogger().info("(This is a known BETA bug, we are working on a fix.)");
	  getLogger().info("A server restart normally fixes this issue asap.");
	  System.out.print("########################################################");
      for(final Player online:Bukkit.getServer().getOnlinePlayers())
      {
    	  if(online.hasPermission("chatfeelings.admin") || online.isOp()){
    	      online.sendMessage("§a§lChatFeelings §r§8§m-------------------------------------");
    	      online.sendMessage("§fThanks for installing ChatFeelings, sadly, there is a");
    	      online.sendMessage("§fknown bug that needs your server to be completely");
    	      online.sendMessage("§frestarted with ChatFeelings' files already in place to load.");
    	      online.sendMessage("§c§lSorry! §7We will fix this bug as soon as possible.");
    	      online.sendMessage("§r§8§m------------------------------------------------");
    	      if(Bukkit.getBukkitVersion().contains("1.12") || Bukkit.getBukkitVersion().contains("1.13")) {
    	    	  online.playSound(online.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.5F, 2.0F);
    	      }
    	  }
	}
  }
  if(Bukkit.getBukkitVersion().contains("1.8") || Bukkit.getBukkitVersion().contains("1.7")) {
		 if(getConfig().getBoolean("particles")) {
	 getLogger().info("NOTE: Particles & Other effects are for 1.9+ ONLY. These have been disabled.");
	 getConfig().set("particles", false);
	 getConfig().set("other-effects", false);
		 }
  }
  
  if (getConfig().getBoolean("Update-Notify")) {
	    Updater updater = new Updater(this, 12987);
	    try
	    {
	      if (updater.checkForUpdates()) {
	        getLogger().info("[!] Update Available: You are using an outdated version of ChatFeelings. [Latest is " + updater.getLatestVersion() + "]");
	      } else {
	    	getLogger().info("✓ Hip Hip Hooray! You're using the latest version of ChatFeelings.");	
	      }
	    }catch(Exception e) {
	    	getLogger().warning("There was an issue while trying to check for updates.");
	    }}
  
  getLogger().info("------------- Finished --------------");	  
}
  
  public void reloadmessages() {
	  try {
      File file = new File(this.getDataFolder(), "messages.yml");
      this.saveResource("messages.yml", true);
      YamlConfiguration.loadConfiguration(file);
	  } catch (Exception e){
		  getLogger().info("Error. The messages.yml couldn't be reloaded correctly.");  
		  if(this.getConfig().getBoolean("debug")) {
		  System.out.print("ChatFeelings Debug: "); e.printStackTrace();
		  }
	  }
  }
  
  public void reloademotes() {
	  try{ 
      File file = new File(this.getDataFolder(), "emotes.yml");
      this.saveResource("messages.yml", true);
      YamlConfiguration.loadConfiguration(file);
	  } catch (Exception e){
		  getLogger().info("Error. The emotes.yml couldn't be reloaded correctly.");  
		  if(this.getConfig().getBoolean("debug")) {
		  System.out.print("ChatFeelings Debug: "); e.printStackTrace();
		  }
	  }
  }
  
  public void reloadsounds() {
	  try {
      File file = new File(this.getDataFolder(), "sounds.yml");
      this.saveResource("messages.yml", true);
      YamlConfiguration.loadConfiguration(file);
	  } catch (Exception e){
		  getLogger().info("Error. The sounds.yml couldn't be reloaded correctly.");  
		  if(this.getConfig().getBoolean("debug")) {
		  System.out.print("ChatFeelings Debug: "); e.printStackTrace();
		  }
	  }
  }
  
  public void debugParticles(Player p) {
	//  Particles.biteParticle(p);
	  Particles.boiParticle(p);
	//  Particles.cryParticle(p);
	//  Particles.dabParticle(p);
	//  Particles.hugParticle(p);
	//  Particles.murderParticle(p);
	//  Particles.punchParticle(p);
  }
  
  public void noPermission(CommandSender sender)
  {
    Player p = (Player)sender;
    sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + messages.getString("no-permission-msg").replace("&", "§"));
       bass(p);
  }
  
  public void noFeelingPermission(CommandSender sender, String label) {
    Player p = (Player)sender;
    sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + messages.getString("no-feeling-permission-msg").replace("%cmd%", label).replace("&", "§"));
       bass(p);
  }
  
  public void click(CommandSender sender) {
    Player p = (Player)sender;
    if (getConfig().getBoolean("sounds")) {
    	try{
        p.playSound(p.getLocation(), Sound.valueOf(sounds.getString("command-sound")), 5.0F, 2.0F);
    	} catch(IllegalArgumentException|NullPointerException e) {
    	getLogger().info("ERROR: Invalid sound for COMMAND in sounds.yml");	
		  if(this.getConfig().getBoolean("debug")) {
		  System.out.print("ChatFeelings Debug: "); e.printStackTrace();
		  }
    	}
      }
    }
  
  public void bass(CommandSender sender) {
	    Player p = (Player)sender;
	    if (getConfig().getBoolean("sounds")) {
	    	try{
	        p.playSound(p.getLocation(), Sound.valueOf(sounds.getString("error-sound")), 5.0F, 1.3F);
	    	} catch(IllegalArgumentException|NullPointerException e) {
	        	getLogger().info("ERROR: Invalid sound for ERROR-SOUND in sounds.yml");	
	  		  if(this.getConfig().getBoolean("debug")) {
	  			  System.out.print("ChatFeelings Debug: "); e.printStackTrace();
	  			  }
	        	}
	   	}
	  }
  
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
  {
    if ((sender instanceof Player))
    {     Player p = (Player)sender;
 //     if (((label.equalsIgnoreCase("chatfeelings")) || (label.equalsIgnoreCase("cf")))  (
 //       (args.length == 0) || ((args.length == 1) && (args[0].equalsIgnoreCase("help"))))) {
    	if(label.equalsIgnoreCase("chatfeelings") || (label.equalsIgnoreCase("cf"))) {
        	if(args.length == 0) {
        if (!sender.hasPermission("chatfeelings.help"))
        {
          noPermission(sender);
        }
        else
        {
            sender.sendMessage(" ");
            sender.sendMessage("§e/feelings §f- Lists all of the emotion commands.");
            sender.sendMessage("§7/cf source §f- Displays the plugins Spigot Page.");
            if (sender.hasPermission("chatfeelings.admin") || sender.isOp()) {
            sender.sendMessage("§7/cf version §f- Shows your version of ChatFeelings.");
            sender.sendMessage("§7/cf reload §f- Reloads the configuration file.");
            sender.sendMessage("§7/cf reset §f- Reset the config.yml to their default settings.");
            if(sender instanceof Player) {
            if(p.getUniqueId().toString().equals("6191ff85-e092-4e9a-94bd-63df409c2079")) {
            sender.sendMessage("§c/cf debugparticles §f- Command for developer to test Particles.");
            }}
          }
          sender.sendMessage(" ");
          click(sender);
        }}
      }
    	
    	if(label.equalsIgnoreCase("chatfeelings") || (label.equalsIgnoreCase("cf"))) {
        	if(args.length == 1 && args[0].equalsIgnoreCase("help")) {
        if (!sender.hasPermission("chatfeelings.help"))
        {
          noPermission(sender);
        }
        else
        {
            sender.sendMessage(" ");
            sender.sendMessage("§e/feelings §f- Lists all of the emotion commands.");
            sender.sendMessage("§7/cf source §f- Displays the plugins Spigot Page.");
            if (sender.hasPermission("chatfeelings.admin") || sender.isOp()) {
            sender.sendMessage("§7/cf version §f- Shows your version of ChatFeelings.");
            sender.sendMessage("§7/cf reload §f- Reloads the configuration file.");
            sender.sendMessage("§7/cf reset §f- Reset the config.yml to their default settings.");
            if(sender instanceof Player) {
            if(p.getUniqueId().toString().equals("6191ff85-e092-4e9a-94bd-63df409c2079")) {
                sender.sendMessage("§c/cf debugparticles §f- Command for developer to test Particles.");
                }}
          }
          sender.sendMessage(" ");
          click(sender);
        }}
      }
      
      if (((label.equalsIgnoreCase("chatfeelings")) || (label.equalsIgnoreCase("cf"))) && 
        (args.length == 1) && 
        (args[0].equalsIgnoreCase("version"))) {
        if (!sender.hasPermission("chatfeelings.admin"))
        {
          noPermission(sender);
        }
        else
        {
          click(sender);
        	  try {
              sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + "§fYou are running §6ChatFeelings " + vv);
        	  } catch (Exception e) {
        		  getLogger().info("Error! Prefix not found. Please reload the server or check your messages.yml");
        		  sender.sendMessage("§c§lError! §fCouldn't find your prefix. This usually just means you need to reload!");  
        	  }
      }}
      
      if (((label.equalsIgnoreCase("chatfeelings")) || (label.equalsIgnoreCase("cf"))) && 
        (args.length == 1) && 
        (args[0].equalsIgnoreCase("reload"))) {
        if (!sender.hasPermission("chatfeelings.admin"))
        {
          noPermission(sender);
        }
        else
        {
 		       if(getServer().getPluginManager().isPluginEnabled("PlugMan") && (getServer().getPluginManager().getPlugin("PlugMan") != null)) {
 			    	try {
 			            getServer().dispatchCommand(getServer().getConsoleSender(), "plugman reload ChatFeelings");
 			           sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + "§7ChatFeelings' §fhas been reloaded with PlugMan successfuly.");   
 				    	}catch(Exception plugman) {
 				    	getLogger().info("Error when using PlugMan to reload ChatFeelings. :(");	
 				    	sender.sendMessage("§c§lError! §fCouldn't reload §7ChatFeelings§f.");
 				    	}
 		       }else {
 		    	   reloadConfig();
 		       }
		       if(!getServer().getPluginManager().isPluginEnabled("PlugMan") && (getServer().getPluginManager().getPlugin("PlugMan") == null)) {
		           sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + "§7Config.yml §fhas been reloaded successfuly.");   
         sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + "§c§lNOTE: §7Messages.yml, Emotes.yml & Sounds.yml require a reload. Sorry!!");
		       }
        //  reloadmessages();
        //  reloadsounds();
        //  reloademotes();
          click(sender);   
          
          if (getConfig().getBoolean("reload-notify-console")) {
            getLogger().info("The plugin has been reloaded by " + sender.getName());
                }
          
        }
      }
      
      if (((label.equalsIgnoreCase("chatfeelings")) || (label.equalsIgnoreCase("cf"))) && 
        (args.length == 1) && 
        (args[0].equalsIgnoreCase("source"))) {
        if (!sender.hasPermission("chatfeelings.help"))
        {
          noPermission(sender);
        }
        else
        {
          click(sender);
          sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + "§fCheck for updates on Spigot at");
          sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + "§7spigotmc.org/resources/chatfeelings.12987");
        }
      }
      
      if (((label.equalsIgnoreCase("chatfeelings")) || (label.equalsIgnoreCase("cf"))) && 
    	        (args.length == 1) && 
    	        (args[0].equalsIgnoreCase("debugparticles"))) {
    	        if (!sender.hasPermission("chatfeelings.admin"))
    	        {
    	          noPermission(sender);
    	        }
    	        else
    	        {
    	          click(sender);
    	          sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + "§7Displaying all particles...");
    	          debugParticles(((Player) sender).getPlayer());
    	        }
    	      }
      
      if (((label.equalsIgnoreCase("chatfeelings")) || (label.equalsIgnoreCase("cf"))) && 
        (args.length == 1) && 
        (args[0].equalsIgnoreCase("reset"))) {
        if (!sender.hasPermission("chatfeelings.admin"))
        {
          noPermission(sender);
        }
        else
        {
          click(sender);
          sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + "§fThe §7Config §fhas been reset to their defaults.");
          File f = new File(this.getDataFolder(), "config.yml");
          f.delete();
          saveDefaultConfig();
          if(Bukkit.getBukkitVersion().contains("1.8") || Bukkit.getBukkitVersion().contains("1.7")) {
     		 if(getConfig().getBoolean("particles")) {
     	 getLogger().info("NOTE: Particles & Other effects are for 1.9+ ONLY. These have been disabled.");
     	 getConfig().set("particles", false);
     	 getConfig().set("other-effects", false);
     		 }
       }
     	 reloadConfig();
        }
      }
      if (((label.equalsIgnoreCase("chatfeelings")) || (label.equalsIgnoreCase("cf"))) && 
        (args.length >= 2))
      {
          sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + "§fToo many arguments for §7/" + cmd.getName());
        click(sender);
        return true;
      }
      
      if (((label.equalsIgnoreCase("chatfeelings")) || (label.equalsIgnoreCase("cf"))) && 
        (args.length == 1) && (!args[0].equalsIgnoreCase("reload")) && (!args[0].equalsIgnoreCase("reset")) && (!args[0].equalsIgnoreCase("version")) && (!args[0].equalsIgnoreCase("help")) && (!args[0].equalsIgnoreCase("source")) && (!args[0].equalsIgnoreCase("update")) && (!args[0].equalsIgnoreCase("debugparticles")))
      {
        sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + "§fCommand not found, try §7/cf §ffor more.");
        click(sender);
      }
      
      if ((label.equalsIgnoreCase("feelings")) && 
        (args.length == 0))
      {
          if (!sender.hasPermission("chatfeelings.help"))
          {
            noPermission(sender);
          }
          else
          {
        sender.sendMessage(" ");
        sender.sendMessage(ChatColor.DARK_GRAY + "§m-------------" + ChatColor.RESET + ChatColor.GREEN + " Feelings Help " + ChatColor.GRAY + "(1/4) " + ChatColor.DARK_GRAY + "§m------------");
        sender.sendMessage(ChatColor.YELLOW + "/hug " + ChatColor.WHITE + "- Gives somebody a nice warm hug!");
        sender.sendMessage(ChatColor.YELLOW + "/slap " + ChatColor.WHITE + "- Gives someone a piece of your mind!");
        sender.sendMessage(ChatColor.YELLOW + "/poke " + ChatColor.WHITE + "- Poke someone to get their attention!");
        sender.sendMessage(ChatColor.YELLOW + "/highfive " + ChatColor.WHITE + "- Shows your support and highfives a player!");
        sender.sendMessage(ChatColor.YELLOW + "/yell " + ChatColor.WHITE + "- Yells at a player, inside voices please.");
        sender.sendMessage(ChatColor.GRAY + "Do " + ChatColor.GREEN + "/feelings 2" + ChatColor.GRAY + " for the next page.");
        click(sender);
      }}
      
      if ((label.equalsIgnoreCase("feelings")) && 
        (args.length == 1) && 
        (args[0].equalsIgnoreCase("1"))) 
      {
          if (!sender.hasPermission("chatfeelings.help"))
          {
            noPermission(sender);
          }
          else
          {
        sender.sendMessage(" ");
        sender.sendMessage(ChatColor.DARK_GRAY + "§m-------------" + ChatColor.RESET + ChatColor.GREEN + " Feelings Help " + ChatColor.GRAY + "(1/4) " + ChatColor.DARK_GRAY + "§m------------");
        sender.sendMessage(ChatColor.YELLOW + "/hug " + ChatColor.WHITE + "- Gives somebody a nice warm hug!");
        sender.sendMessage(ChatColor.YELLOW + "/slap " + ChatColor.WHITE + "- Gives someone a piece of your mind!");
        sender.sendMessage(ChatColor.YELLOW + "/poke " + ChatColor.WHITE + "- Poke someone to get their attention!");
        sender.sendMessage(ChatColor.YELLOW + "/highfive " + ChatColor.WHITE + "- Shows your support and highfives a player!");
        sender.sendMessage(ChatColor.YELLOW + "/yell " + ChatColor.WHITE + "- Yells at a player, inside voices please.");
        sender.sendMessage(ChatColor.GRAY + "Do " + ChatColor.GREEN + "/feelings 2" + ChatColor.GRAY + " for the next page.");
        click(sender);
      }}
      
      if ((label.equalsIgnoreCase("feelings")) && 
        (args.length == 1) && 
        (args[0].equalsIgnoreCase("2")))
      {        
    if (!sender.hasPermission("chatfeelings.help"))
        {
          noPermission(sender);
        }
        else
        {
        sender.sendMessage(" ");
        sender.sendMessage(ChatColor.DARK_GRAY + "§m-------------" + ChatColor.RESET + ChatColor.GREEN + " Feelings Help " + ChatColor.GRAY + "(2/4) " + ChatColor.DARK_GRAY + "§m------------");
        sender.sendMessage(ChatColor.YELLOW + "/shake " + ChatColor.WHITE + "- Shakes a player to their feet.");
        sender.sendMessage(ChatColor.YELLOW + "/bite " + ChatColor.WHITE + "- Bites a player, OUCH!");
        sender.sendMessage(ChatColor.YELLOW + "/stab " + ChatColor.WHITE + "- Stabs a player, need a bandaid?");
        sender.sendMessage(ChatColor.YELLOW + "/snuggle " + ChatColor.WHITE + "- Snuggles a player with hugs.");
        sender.sendMessage(ChatColor.YELLOW + "/kiss " + ChatColor.WHITE + "- Give a player a squishy kiss!");
        sender.sendMessage(ChatColor.GRAY + "Do " + ChatColor.GREEN + "/feelings 3" + ChatColor.GRAY + " for the next page.");
        click(sender);
      }}
      
      if ((label.equalsIgnoreCase("feelings")) && 
        (args.length == 1) && 
        (args[0].equalsIgnoreCase("3")))
      {
          if (!sender.hasPermission("chatfeelings.help"))
          {
            noPermission(sender);
          }
          else
          {
        sender.sendMessage(" ");
        sender.sendMessage(ChatColor.DARK_GRAY + "§m-------------" + ChatColor.RESET + ChatColor.GREEN + " Feelings Help " + ChatColor.GRAY + "(3/4) " + ChatColor.DARK_GRAY + "§m------------");
        sender.sendMessage(ChatColor.YELLOW + "/punch " + ChatColor.WHITE + "- Punches a player, ouch!");
        sender.sendMessage(ChatColor.YELLOW + "/lick " + ChatColor.WHITE + "- Lick someone like an ice-cream sundae!");
        sender.sendMessage(ChatColor.YELLOW + "/murder " + ChatColor.WHITE + "- Murders a player, Muhaha!");
        sender.sendMessage(ChatColor.YELLOW + "/cry " + ChatColor.WHITE + "- Cry at a player, how sad.");
        sender.sendMessage(ChatColor.YELLOW + "/scorn " + ChatColor.WHITE + "- Scorn a player for what they've done.");
        sender.sendMessage(ChatColor.GRAY + "Do " + ChatColor.GREEN + "/feelings 4" + ChatColor.GRAY + " for the next page.");
        click(sender);
      }}
      
      if ((label.equalsIgnoreCase("feelings")) && 
        (args.length == 1) && 
        (args[0].equalsIgnoreCase("4")))
      {
          if (!sender.hasPermission("chatfeelings.help"))
          {
            noPermission(sender);
          }
          else
          {
        sender.sendMessage(" ");
        sender.sendMessage(ChatColor.DARK_GRAY + "§m-------------" + ChatColor.RESET + ChatColor.GREEN + " Feelings Help " + ChatColor.GRAY + "(4/4) " + ChatColor.DARK_GRAY + "§m------------");
        sender.sendMessage(ChatColor.YELLOW + "/pat " + ChatColor.WHITE + "- Pat your friends head, softly.");
          sender.sendMessage(ChatColor.YELLOW + "/boi " + ChatColor.WHITE + "- Here comes dat boi. How dank!");
          sender.sendMessage(ChatColor.YELLOW + "/dab " + ChatColor.WHITE + "- Cash me outside. How bout dab?");
          sender.sendMessage(ChatColor.YELLOW + "/stalk " + ChatColor.WHITE + "- Carefully stalk your pals... carefully.");
        sender.sendMessage(" ");
        click(sender);
      }}
      
      if ((label.equalsIgnoreCase("feelings")) && 
        (args.length == 1) && (!args[0].equalsIgnoreCase("1")) && (!args[0].equalsIgnoreCase("2")) && (!args[0].equalsIgnoreCase("3")) && (!args[0].equalsIgnoreCase("4")) && (!args[0].equalsIgnoreCase("list")))
      {
    	  try {
        sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + messages.getString("invalid-page").replace("%arg%", args[0]).replace("&", "§").replace("%,%","'"));
      	  } catch(Exception e) {
       		 getLogger().info("Error when trying to display 'invalid-page' message in messages.yml. Check your file!");
   		  sender.sendMessage("§c§lOops! §fSomething went wrong. §7Try that again!");
       		 if(getConfig().getBoolean("debug")) { System.out.print("ChatFeelings Debug: "); e.printStackTrace();
       	  }}
       		 
        bass(sender);
      }
    
    if ((label.equalsIgnoreCase("feelings")) && (args.length == 1) &&
            (args[0].equalsIgnoreCase("list")))
          {
    	try {
        sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + messages.getString("feelings-list-help").replace("%cmd%", label).replace("&", "§").replace("%,%","'"));
  	  } catch(Exception e) {
	  		 getLogger().info("Error when trying to display 'feelings-list-help' message in messages.yml. Check your file!");
   		  sender.sendMessage("§c§lOops! §fSomething went wrong. §7Try that again!");
	  		 if(getConfig().getBoolean("debug")) { System.out.print("ChatFeelings Debug: "); e.printStackTrace();
	  	  }}
             bass(sender);
           }
    
      if (((label.equalsIgnoreCase("bite")) || (label.equalsIgnoreCase("hug")) || (label.equalsIgnoreCase("boi")) || (label.equalsIgnoreCase("dab")) || 
        (label.equalsIgnoreCase("shake")) || (label.equalsIgnoreCase("snuggle")) || (label.equalsIgnoreCase("kiss")) || (label.equalsIgnoreCase("stab")) || 
        (label.equalsIgnoreCase("punch")) || (label.equalsIgnoreCase("slap")) || (label.equalsIgnoreCase("poke")) || (label.equalsIgnoreCase("highfive")) || 
        (label.equalsIgnoreCase("facepalm")) || (label.equalsIgnoreCase("yell")) || (label.equalsIgnoreCase("cry")) || (label.equalsIgnoreCase("lick")) || (label.equalsIgnoreCase("murder")) || 
        (label.equalsIgnoreCase("scorn")) || (label.equalsIgnoreCase("pat")) || (label.equalsIgnoreCase("stalk"))) && 
        (args.length == 0))
      {
      	if(emotes.getBoolean(label.toLowerCase() + "-active") && 
         	   !Cooldowns.cooldown.containsKey(p)) {
       bass(sender);
       try {

       sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + messages.getString("no-player-provided").replace("%cmd%", label).replace("&", "§"));
    	   
    	   } catch(Exception e) {
   		 getLogger().info("Error when trying to display 'no-player-provided' message in messages.yml. Check your file!");
		  sender.sendMessage("§c§lOops! §fSomething went wrong. §7Try that again!");
   		 if(getConfig().getBoolean("debug")) { System.out.print("ChatFeelings Debug: "); e.printStackTrace();
   	  }}
       
        if (getConfig().getBoolean("extra-help")) {
        	try {
	       sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + messages.getString("extra-help-command").replace("%cmd%", label).replace("&", "§"));
        	  } catch(Exception e) {
        	  		 getLogger().info("Error when trying to display 'extrap-help-message' message in messages.yml. Check your file!");
   	    		  sender.sendMessage("§c§lOops! §fSomething went wrong. §7Try that again!");
        	  		 if(getConfig().getBoolean("debug")) { System.out.print("ChatFeelings Debug: "); e.printStackTrace();
        	  	  }}
        }}
      }
      
      if (((label.equalsIgnoreCase("bite")) || (label.equalsIgnoreCase("hug")) || (label.equalsIgnoreCase("boi")) || (label.equalsIgnoreCase("dab")) || 
        (label.equalsIgnoreCase("shake")) || (label.equalsIgnoreCase("snuggle")) || (label.equalsIgnoreCase("kiss")) || (label.equalsIgnoreCase("stab")) || 
        (label.equalsIgnoreCase("punch")) || (label.equalsIgnoreCase("slap")) || (label.equalsIgnoreCase("poke")) || (label.equalsIgnoreCase("highfive")) || 
        (label.equalsIgnoreCase("facepalm")) || (label.equalsIgnoreCase("yell")) || (label.equalsIgnoreCase("cry")) || (label.equalsIgnoreCase("lick")) || (label.equalsIgnoreCase("murder")) || 
        (label.equalsIgnoreCase("scorn")) || (label.equalsIgnoreCase("pat")) || (label.equalsIgnoreCase("stalk"))) && 
        (args.length >= 2))
      {
      	if(emotes.getBoolean(label.toLowerCase() + "-active") && 
         	   !Cooldowns.cooldown.containsKey(p)) {
        bass(sender);
        try {
        sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + messages.getString("too-many-args-message").replace("%cmd%", label).replace("&", "§"));
  	  } catch(Exception e) {
	  		 getLogger().info("Error when trying to display 'too-many-args-message' message in messages.yml. Check your file!");
   		  sender.sendMessage("§c§lOops! §fSomething went wrong. §7Try that again!");
	  		 if(getConfig().getBoolean("debug")) { System.out.print("ChatFeelings Debug: "); e.printStackTrace();
	  	  }}
        if (getConfig().getBoolean("extra-help")) {
        	try {
	         sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + messages.getString("extra-help-command").replace("%cmd%", label).replace("&", "§"));
      	  } catch(Exception e) {
 	  		 getLogger().info("Error when trying to display 'extra-help-command' message in messages.yml. Check your file!");
   		  sender.sendMessage("§c§lOops! §fSomething went wrong. §7Try that again!");
 	  		 if(getConfig().getBoolean("debug")) { System.out.print("ChatFeelings Debug: "); e.printStackTrace();
 	  	  }}
        }}
      }

      if (((label.equalsIgnoreCase("bite")) || (label.equalsIgnoreCase("hug")) || (label.equalsIgnoreCase("boi")) || (label.equalsIgnoreCase("dab")) || 
    	        (label.equalsIgnoreCase("shake")) || (label.equalsIgnoreCase("snuggle")) || (label.equalsIgnoreCase("kiss")) || (label.equalsIgnoreCase("stab")) || 
    	        (label.equalsIgnoreCase("punch")) || (label.equalsIgnoreCase("slap")) || (label.equalsIgnoreCase("poke")) || (label.equalsIgnoreCase("highfive")) || 
    	        (label.equalsIgnoreCase("facepalm")) || (label.equalsIgnoreCase("yell")) || (label.equalsIgnoreCase("cry")) || (label.equalsIgnoreCase("lick")) || (label.equalsIgnoreCase("murder")) || 
    	        (label.equalsIgnoreCase("scorn")) || (label.equalsIgnoreCase("pat")) || (label.equalsIgnoreCase("stalk"))))
    	      {
    	    	  if(!emotes.getBoolean(label.toLowerCase() + "-active")){
    	        bass(sender);
    	        try {
    	        sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + messages.getString("disabled-feeling-message").replace("%cmd%", label).replace("&", "§"));
          	  } catch(Exception e) {
     	  		 getLogger().info("Error when trying to display 'disabled-feeling-message' message in messages.yml. Check your file!");
	    		  sender.sendMessage("§c§lOops! §fSomething went wrong. §7Try that again!");
     	  		 if(getConfig().getBoolean("debug")) { System.out.print("ChatFeelings Debug: "); e.printStackTrace();
     	  	  }}
    	        }
    	      }
      
      if (((label.equalsIgnoreCase("bite")) || (label.equalsIgnoreCase("hug")) || (label.equalsIgnoreCase("boi")) || (label.equalsIgnoreCase("dab")) || 
        (label.equalsIgnoreCase("shake")) || (label.equalsIgnoreCase("snuggle")) || (label.equalsIgnoreCase("kiss")) || (label.equalsIgnoreCase("stab")) || 
        (label.equalsIgnoreCase("punch")) || (label.equalsIgnoreCase("slap")) || (label.equalsIgnoreCase("poke")) || (label.equalsIgnoreCase("highfive")) || 
        (label.equalsIgnoreCase("facepalm")) || (label.equalsIgnoreCase("yell")) || (label.equalsIgnoreCase("cry")) || (label.equalsIgnoreCase("lick")) || (label.equalsIgnoreCase("murder")) || 
        (label.equalsIgnoreCase("scorn")) || (label.equalsIgnoreCase("pat")) || (label.equalsIgnoreCase("stalk"))) && 
        (args.length == 1))
      {
      	if(emotes.getBoolean(label.toLowerCase() + "-active") && 
         	   !Cooldowns.cooldown.containsKey(p)) {
        Player target = Bukkit.getServer().getPlayer(args[0]);
        if (target == null)
        {
         bass(sender);
         try {
        	if(args[0].equalsIgnoreCase("console")) {
         sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + messages.getString("player-is-console").replace("%target%", args[0]).replace("%cmd%", label).replace("&", "§"));
        	}else {
         sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + messages.getString("player-not-online").replace("%target%", args[0]).replace("%cmd%", label).replace("&", "§"));
        	}
   	  } catch(Exception e) {
	  		 getLogger().info("Error when trying to display 'player-not-online' message in messages.yml. Check your file!");
   		  sender.sendMessage("§c§lOops! §fSomething went wrong. §7Try that again!");
	  		 if(getConfig().getBoolean("debug")) { System.out.print("ChatFeelings Debug: "); e.printStackTrace();
	  	  }}
          return false;
        }}
      }
      
      if (((label.equalsIgnoreCase("bite")) || (label.equalsIgnoreCase("hug")) || (label.equalsIgnoreCase("boi")) || (label.equalsIgnoreCase("dab")) || 
    	        (label.equalsIgnoreCase("shake")) || (label.equalsIgnoreCase("snuggle")) || (label.equalsIgnoreCase("kiss")) || (label.equalsIgnoreCase("stab")) || 
    	        (label.equalsIgnoreCase("punch")) || (label.equalsIgnoreCase("slap")) || (label.equalsIgnoreCase("poke")) || (label.equalsIgnoreCase("highfive")) || 
    	        (label.equalsIgnoreCase("facepalm")) || (label.equalsIgnoreCase("yell")) || (label.equalsIgnoreCase("cry")) || (label.equalsIgnoreCase("lick")) || (label.equalsIgnoreCase("murder")) || 
    	        (label.equalsIgnoreCase("scorn")) || (label.equalsIgnoreCase("pat")) || (label.equalsIgnoreCase("stalk"))))
    	      {
    	    	  if(emotes.getBoolean(label.toLowerCase() + "-active")){
    	    	      if (Cooldowns.cooldown.containsKey(p.getPlayer()))  // COOLDOWN EVENT
    	    	      {
    	    	    	  try  {
    	    	          sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + messages.getString("cooldown-active").replace("%target%", args[0]).replace("%cmd%", label).replace("&", "§"));  //ERROR HERE. Basicly when you activate the cooldown then do /hug with out arguments it throws this error.
    	    	    	  } catch (Exception e) {
    	    	    		  sender.sendMessage("§c§lOops! §fSomething went wrong. §7Try that again!");
    	    	    		    getLogger().info("Error when trying to show 'cooldown-active' message. Enable 'debug' for errors.");
    	    	    		  if(getConfig().getBoolean("debug")) {
    	    	    			  System.out.print("ChatFeelings Error: "); e.printStackTrace();
    	    	    		  }
    	    	    	  }
    	    	          bass(sender);
    	         return true;
    	        }}
    	      }
      
      if (((label.equalsIgnoreCase("bite")) || (label.equalsIgnoreCase("hug")) || (label.equalsIgnoreCase("boi")) || (label.equalsIgnoreCase("dab")) || 
        (label.equalsIgnoreCase("shake")) || (label.equalsIgnoreCase("snuggle")) || (label.equalsIgnoreCase("kiss")) || (label.equalsIgnoreCase("stab")) || 
        (label.equalsIgnoreCase("punch")) || (label.equalsIgnoreCase("slap")) || (label.equalsIgnoreCase("poke")) || (label.equalsIgnoreCase("highfive")) || 
        (label.equalsIgnoreCase("facepalm")) || (label.equalsIgnoreCase("yell")) || (label.equalsIgnoreCase("cry")) || (label.equalsIgnoreCase("lick")) || (label.equalsIgnoreCase("murder")) || 
        (label.equalsIgnoreCase("scorn")) || (label.equalsIgnoreCase("pat")) || (label.equalsIgnoreCase("stalk"))) && 
        (args.length == 1))
      {
    	if(emotes.getBoolean(label.toLowerCase() + "-active") && 
    	     !Cooldowns.cooldown.containsKey(p)) {

        Player target = Bukkit.getServer().getPlayer(args[0]);
        if (sender.equals(target))
        {
          bass(sender);
          try {
          sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + messages.getString("self-feeling").replace("%cmd%", label).replace("&", "§"));
       	  } catch(Exception e) {
 	  		 getLogger().info("Error when trying to display 'self-feeling' message in messages.yml. Check your file!");
    		  sender.sendMessage("§c§lOops! §fSomething went wrong. §7Try that again!");
 	  		 if(getConfig().getBoolean("debug")) { System.out.print("ChatFeelings Debug: "); e.printStackTrace();
 	  	  }}
          return false;
        }}
     
    	        if (args.length == 1 && (emotes.getBoolean(label.toLowerCase() + "-active"))) {
    	        if ((!sender.hasPermission("chatfeelings." + label)) && (!sender.hasPermission("chatfeelings.all")) && (!p.isOp()))
    	        {
    	          noFeelingPermission(sender, label);
    	        }
    	        else  // TODO: Need to add particles.yml
    	        {
    	          Player target = Bukkit.getServer().getPlayer(args[0]);
    	          if (!sender.equals(target))
    	          {
    	            if (getConfig().getBoolean("sounds"))
    	            {
    	              click(sender);

    	            	try{
    	                    p.playSound(p.getLocation(), Sound.valueOf(sounds.getString(label + "-sound")), 2.0F, 1.4F);
    	                    target.playSound(target.getLocation(), Sound.valueOf(sounds.getString(label + "-sound")), 1.0F, 1.4F);
    	                	} catch(NullPointerException e) {
    	                	getLogger().info("ERROR: Invalid sound for " + label + "-sound in sounds.yml");	
    	          		  if(this.getConfig().getBoolean("debug")) {
    	          			  System.out.print("ChatFeelings Debug: "); e.printStackTrace();
    	          			  }
    	                	}
    	            	
    	            }
    	            try {
    	            if(this.getConfig().getBoolean("global-feelings")) {
    	                                for(final Player online:Bukkit.getServer().getOnlinePlayers())
                {
online.sendMessage(messages.getString(label+"-global").replace("&", "§").replace("%target%", target.getName()).replace("%sender%", sender.getName()));
                	}
    	                } else {
    	                sender.sendMessage(messages.getString(label+"-sender").replace("%target%", target.getName()).replace("&", "§"));
    	                target.sendMessage(messages.getString(label+"-target").replace("%sender%", sender.getName()).replace("&", "§"));
    	                }
    	          }catch (Exception e){
    	        	  sender.sendMessage("§c§lSorry! §fSomething happend, try again later. §7(See Console)");
    	        	  getLogger().info("Error when trying to send /" + label + " messages to " + p.getName());
    	      		  if(this.getConfig().getBoolean("debug")) {
    	      			  System.out.print("ChatFeelings Debug: "); e.printStackTrace();
    	      			  }
    	          }
    Cooldowns.cooldown.put(p, p.getName());
    Cooldowns.startCooldown(p);  
    	          }
    	        }
    	      }
      }// end of long label equals case from above.
      
      
  }else{
      if (((label.equalsIgnoreCase("bite")) || (label.equalsIgnoreCase("hug")) || (label.equalsIgnoreCase("boi")) || (label.equalsIgnoreCase("dab")) || 
    	        (label.equalsIgnoreCase("shake")) || (label.equalsIgnoreCase("snuggle")) || (label.equalsIgnoreCase("kiss")) || (label.equalsIgnoreCase("stab")) || 
    	        (label.equalsIgnoreCase("punch")) || (label.equalsIgnoreCase("slap")) || (label.equalsIgnoreCase("poke")) || (label.equalsIgnoreCase("highfive")) || 
    	        (label.equalsIgnoreCase("facepalm")) || (label.equalsIgnoreCase("yell")) || (label.equalsIgnoreCase("cry")) || (label.equalsIgnoreCase("lick")) || (label.equalsIgnoreCase("murder")) || 
    	        (label.equalsIgnoreCase("scorn")) || (label.equalsIgnoreCase("pat")) || (label.equalsIgnoreCase("stalk"))))
    	      {
	  sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + "§cSorry, only players can do feeling commands!");
    	      }
      
  	if(label.equalsIgnoreCase("chatfeelings") || (label.equalsIgnoreCase("cf"))) {
    	if(args.length == 0) {
        sender.sendMessage("§e/cf source §r- Displays the plugins Spigot Page.");
        sender.sendMessage("§e/cf version §r- Shows your version of ChatFeelings.");
        sender.sendMessage("§e/cf reload §r- Reloads the configuration file.");
        sender.sendMessage("§e/cf reset §r- Reset the config.yml to their default settings.");
    }
  }
	
	if(label.equalsIgnoreCase("chatfeelings") || (label.equalsIgnoreCase("cf"))) {
    	if(args.length == 1 && args[0].equalsIgnoreCase("help")) {
        sender.sendMessage("§e/cf source §r- Displays the plugins Spigot Page.");
        sender.sendMessage("§e/cf version §r- Shows your version of ChatFeelings.");
        sender.sendMessage("§e/cf reload §r- Reloads the configuration file.");
        sender.sendMessage("§e/cf reset §r- Reset the config.yml to their default settings.");
    }
  }
	
	if(label.equalsIgnoreCase("chatfeelings") || (label.equalsIgnoreCase("cf"))) {
    	if(args.length == 1 && args[0].equalsIgnoreCase("reload")) {
    		try {
    		       if(getServer().getPluginManager().isPluginEnabled("PlugMan") && (getServer().getPluginManager().getPlugin("PlugMan") != null)) {
    			    	try {
    			            getServer().dispatchCommand(getServer().getConsoleSender(), "plugman reload ChatFeelings");
    			            sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + "§7ChatFeelings §fhas used plugman to completely reload.");
    				    	}catch(Exception plugman) {
    				    	getLogger().info("Error when using PlugMan to reload ChatFeelings. :(");	
    				    	}
    		       }else {
    		    	   reloadConfig();
    		       }
		       if(!getServer().getPluginManager().isPluginEnabled("PlugMan") && (getServer().getPluginManager().getPlugin("PlugMan") == null)) {
		    	   try {
		            sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + "§7Config.yml §fhas been reloaded.");
            sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + "§c§lNOTE: §7Messages.yml, Emotes.yml & Sounds.yml require a reload. Sorry!!");
		    		}catch(Exception e) {
		      		  System.out.print("ERROR! Your prefix in the config.yml isn't correct.");
		      		  sender.sendMessage("§aFeelings §8§l▏§fConfiguration was reloaded with §c§l1§f error.");
		      		  sender.sendMessage("      §8 > §c§lCommon Msg Bug: §fRequires Server Restart.");
		      		}
		       }
         //   reloadmessages();
         //   reloadsounds();
         //   reloademotes();
    		} catch (Exception e) {
    	    	  getLogger().info("Error reloading config. Check your config and try again.");
    	    	  if(getConfig().getBoolean("debug")) {
    	    		  System.out.print("ChatFeelings Debug: "); e.printStackTrace();
    	    	  }
    		}
    }
  }
	
	if(label.equalsIgnoreCase("chatfeelings") || (label.equalsIgnoreCase("cf"))) {
    	if(args.length == 1 && args[0].equalsIgnoreCase("version")) {
    		try {
                sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + "§fYou are running §7ChatFeelings " + vv);
    		}catch(Exception e) {
      		  getLogger().info("ERROR! Your prefix in the config.yml isn't correct.");
      		System.out.print("      > NOTE: A server restart normally fixes this common issue.");
      		  sender.sendMessage("§c§lSorry! §fThere was an error with your prefix.");
      		}
    }
  }
	
	if(label.equalsIgnoreCase("feelings")) {
            sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + "§cSorry, console can't see the feelings list.");
  }
	
	if(label.equalsIgnoreCase("chatfeelings") || (label.equalsIgnoreCase("cf"))) {
    	if(args.length == 1 && args[0].equalsIgnoreCase("source")) {
    		try {
    sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + "§8Check for updates on Spigot at");
    sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + "§espigotmc.org/resources/chatfeelings.12987");
    		}catch(Exception e) {
        		  getLogger().info("ERROR! Your prefix in the config.yml isn't correct.");
            		System.out.print("      > NOTE: A server restart normally fixes this common issue.");
    		  sender.sendMessage("§c§lSorry! §fThere was an error with your prefix.");
    		}
    	}}
	
	if(label.equalsIgnoreCase("chatfeelings") || (label.equalsIgnoreCase("cf"))) {
    	if(args.length == 1 && args[0].equalsIgnoreCase("reset")) {
    		try {
    		File f = new File(this.getDataFolder(), "config.yml");
    		f.delete();
    		saveDefaultConfig();
    		  if(Bukkit.getBukkitVersion().contains("1.8") || Bukkit.getBukkitVersion().contains("1.7")) {
    				 if(getConfig().getBoolean("particles")) {
    			 sender.sendMessage(messages.getString("prefix").replace("&", "§") + "§cNOTE:§f Particles & Other effects are for 1.9+ ONLY. These have been disabled.");
    			 getConfig().set("particles", false);
    			 getConfig().set("other-effects", false);
    				 }
    		  }
    		sender.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" + "§bConfiguration has been restored to default values.");
    		reloadConfig();
    		}catch(Exception e) {
      		  getLogger().info("ERROR! Your prefix in the config.yml isn't correct.");
          		System.out.print("      > NOTE: A server restart normally fixes this common issue.");
  		  sender.sendMessage("§c§lSorry! §fThere was an error with your prefix.");
  		}
    	}}
	
	//_______________________
  }// end of else if not player
      return false;
    } // command end
  
  @EventHandler
  public void onPlayerJoinGameEvent(PlayerJoinEvent e)
  {
    final Player player = e.getPlayer();
    
    if (player.getUniqueId().toString().equals("6191ff85-e092-4e9a-94bd-63df409c2079")) {
	  try {
        player.sendMessage(ChatColor.GRAY + "This server is running " + ChatColor.WHITE + "ChatFeelings " + ChatColor.GOLD + "v" + getDescription().getVersion());
	  }catch(Exception error){
		  if(getConfig().getBoolean("debug")) {
		 System.out.print("ChatFeelings Debug: Error when casting Dev-Join message to Zach.");
		 System.out.print("ChatFeelings Debug: "); error.printStackTrace();
		  }
	  }}
    
    		  if (getConfig().getBoolean("Update-Notify") && (player.isOp() || player.hasPermission("chatfeelings.admin"))) {
    			  try {  
    			  Updater updater = new Updater(this, 12987);
    			if (updater.checkForUpdates()) {

    		    	  try {
    		      	    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
    		      	    {
    		      	        public void run()
    		      	        {
    		      	      	  try {
    		      	        	click(player);
    		      	            player.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" +"§eAn update for §fChatFeelings§e is now available.");
    		      	            player.sendMessage(messages.getString("prefix").replace("&", "§") + " §r" +"§fYou're using §7v" + getDescription().getVersion().replace("-BETA", "") + " §fwhile the latest is §7" + updater.getLatestVersion().replace("-BETA", ""));
    		      	      	  }catch(Exception e) {
    		      	        	  getLogger().info("Couldn't send update message. If this is your first time seeing this, restart your server;");    	    	
    		      	    }
    		      	        }
    		      	      }, 90L);
    		      	  }catch(Exception e2) {
    		      	  getLogger().info("Couldn't send update message. If this is your first time seeing this, restart your server.");
    		      	  if(getConfig().getBoolean("debug")) {
    		      		  System.out.print("ChatFeelings Debug: "); e2.printStackTrace();
    		      	  }
    		      	  }
    				
    			  }
    			  }catch(Exception updatefailed) { System.out.print("ChatFeelings Debug: Error on checking for updates on join:"); updatefailed.printStackTrace();
    			  }
    		  }
    		  

  
  if (!getConfig().getBoolean("Metrics"))
	      {
	  if (player.isOp() || player.hasPermission("chatfeelings.admin")) {
      	if(getConfig().getBoolean("ignore-metrics-join-message") && getConfig().contains("ignore-metrics-join-message")) {
      		System.out.print("ChatFeelings Debug: Found 'ignore-metrics-join-message' in config and is true.");
      		System.out.print("ChatFeelings Debug: Message will not be sent to player " + player.getName());
      	}else {
	    	    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
	    	    {
	    	        public void run()
	    	        {
	    	        	try {
	    	      	  if(getConfig().getBoolean("debug")) {
	    	      		  System.out.print("ChatFeelings Debug: Sent metrics disabled message to " + player.getName());
	    	      	  }
	    	        	bass(player);
	    	            player.sendMessage("§aFeelings §8§l▏ §c§lMetrics Disabled. §fPlease reconsider enabling!");
	    	            player.sendMessage("§aFeelings §8§l▏ §7Metrics help us address usage & don't effect preformance.");
	    	        	} catch(Exception e) {
	  	    	      	  if(getConfig().getBoolean("debug")) {
		    	      		  System.out.print("ChatFeelings Debug: ERROR! Couldn't send message for metrics off to " + player.getName());
		    	      		  System.out.print("ChatFeelings Debug: "); e.printStackTrace();
		    	      	  }
	    	        	}
	    	        }
	    	      }, 120L);
      	}
	    }}
	  } // END OF ON JOIN EVENT

  public void onDisable() {
      FileConfiguration soundconfig = YamlConfiguration.loadConfiguration(soundfile);
		sounds.set("Version", Bukkit.getBukkitVersion().replace("-SNAPSHOT", ""));
		try {
			soundconfig.save(soundfile);
		} catch (IOException e) { }
	    getLogger().info("Having trouble? Join our Support Discord: https://discord.gg/6ugXPfX");
	    getConfig().set("config-version", configVersion);
	    if (getConfig().getBoolean("debug")) {
		    System.out.print("ChatFeelings Debug: Shutting down and running onDisable events.");
	    System.out.print("ChatFeelings Debug: Saving config...");
	    }
	 //
  }
}
