package com.zachduda.chatfeelings;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;


public class FileSetup {
	private static Main plugin = Main.getPlugin(Main.class);
	
	private static void setMsgs(String configpath, String msg) {
		  File folder = Bukkit.getServer().getPluginManager().getPlugin("ChatFeelings").getDataFolder();
		  
		  File msgsfile = new File(folder, File.separator + "messages.yml");
		  FileConfiguration msgs = YamlConfiguration.loadConfiguration(msgsfile);
		  if(!msgsfile.exists()) {
			  try {
			  msgs.save(msgsfile);
		    	plugin.getLogger().info("Created new messages.yml file on the fly...");
			  } catch(Exception err) {}
		  }
		  
		if(!msgs.contains(configpath)) {
			msgs.set(configpath, msg);
	    	try { 
	    	msgs.save(msgsfile);
	    	} catch(Exception err) {}
		} else if(msgs.getString(configpath) == null) {
			plugin.getLogger().warning("Replacing '" + configpath + " in messages.yml, it was left blank.");
			msgs.set(configpath, msg);
	    	try { 
	    	msgs.save(msgsfile);
	    	} catch(Exception err) {}
		}
	}
	
    private static void forceMsgs(String configpath, String msg) {
		  File folder = Bukkit.getServer().getPluginManager().getPlugin("ChatFeelings").getDataFolder();
		  
		  File msgsfile = new File(folder, File.separator + "messages.yml");
		  FileConfiguration msgs = YamlConfiguration.loadConfiguration(msgsfile);
		  if(!msgsfile.exists()) {
			  try {
			  msgs.save(msgsfile);
		    	plugin.getLogger().info("Created new messages.yml file on the fly...");
			  } catch(Exception err) {}
		  }
		  
			msgs.set(configpath, msg);
	    	try { 
	    	msgs.save(msgsfile);
	    	} catch(Exception err) {}
	}
	
    private static void setMsgsVersion(int vers) {
		  File folder = Bukkit.getServer().getPluginManager().getPlugin("ChatFeelings").getDataFolder();
		  
		  File msgsfile = new File(folder, File.separator + "messages.yml");
		  FileConfiguration msgs = YamlConfiguration.loadConfiguration(msgsfile);

		  if(!msgs.contains("Version") || msgs.getInt("Version") != vers) {
	    	try { 
	    	msgs.set("Version", vers);
	    	msgs.save(msgsfile);
	    	} catch(Exception err) {}}
	}
    
	private static void forceEmotes(String configpath, String msg) {
		  File folder = Bukkit.getServer().getPluginManager().getPlugin("ChatFeelings").getDataFolder();
		  
		  File emotesfile = new File(folder, File.separator + "emotes.yml");
		  FileConfiguration emotes = YamlConfiguration.loadConfiguration(emotesfile);
		  
		  if(!emotesfile.exists()) {
			  try {
			  emotes.save(emotesfile);
		    	plugin.getLogger().info("Created new emotes.yml file on the fly...");
			  } catch(Exception err) {}
		  }
		  
			emotes.set(configpath, msg);
	    	try { 
	    	emotes.save(emotesfile);
	    	} catch(Exception err) {}
	}
	
	private static void setEmotes(String configpath, String msg) {
		  File folder = Bukkit.getServer().getPluginManager().getPlugin("ChatFeelings").getDataFolder();
		  
		  File emotesfile = new File(folder, File.separator + "emotes.yml");
		  FileConfiguration emotes = YamlConfiguration.loadConfiguration(emotesfile);
		  
		  if(!emotesfile.exists()) {
			  try {
			  emotes.save(emotesfile);
		    	plugin.getLogger().info("Created new emotes.yml file on the fly...");
			  } catch(Exception err) {}
		  }
		  
		if(!emotes.contains(configpath)) {
			emotes.set(configpath, msg);
	    	try { 
	    	emotes.save(emotesfile);
	    	} catch(Exception err) {}
		} else {
		if(emotes.getString(configpath) == null) {
			plugin.getLogger().warning("Replacing '" + configpath + " in emotes.yml, it was left blank.");
			emotes.set(configpath, msg);
	    	try { 
	    	emotes.save(emotesfile);
	    	} catch(Exception err) {}
		}
		}
	}
	
	private static void setEmotesVersion(int vers) {
		  File folder = Bukkit.getServer().getPluginManager().getPlugin("ChatFeelings").getDataFolder();
		  
		  File emotesfile = new File(folder, File.separator + "emotes.yml");
		  FileConfiguration emotes = YamlConfiguration.loadConfiguration(emotesfile);

		if(!emotes.contains("Version") || emotes.getInt("Version") != vers) {
	    	try { 
	    	emotes.set("Version", vers);
	    	emotes.save(emotesfile);
	    	} catch(Exception err) {}}
	}
	
	private static void setEmotesDouble(String configpath, Double dubdub) {
		  File folder = Bukkit.getServer().getPluginManager().getPlugin("ChatFeelings").getDataFolder();
		  
		  File emotesfile = new File(folder, File.separator + "emotes.yml");
		  FileConfiguration emotes = YamlConfiguration.loadConfiguration(emotesfile);

		if(!emotes.contains(configpath)) {
	    	try { 
	    	emotes.set(configpath, dubdub);
	    	emotes.save(emotesfile);
	    	} catch(Exception err) {}
	} else if(emotes.getString(configpath) == null) {
		plugin.getLogger().warning("Replacing '" + configpath + " (double) in emotes.yml, it was left blank.");
    	try { 
    	emotes.set(configpath, dubdub);
    	emotes.save(emotesfile);
    	} catch(Exception err) {}
	}}
	
	private static void setEmotesBoolean(String configpath, boolean siono) {
		  File folder = Bukkit.getServer().getPluginManager().getPlugin("ChatFeelings").getDataFolder();
		  
		  File emotesfile = new File(folder, File.separator + "emotes.yml");
		  FileConfiguration emotes = YamlConfiguration.loadConfiguration(emotesfile);

		if(!emotes.contains(configpath)) {
	    	try { 
	    	emotes.set(configpath, siono);
	    	emotes.save(emotesfile);
	    	} catch(Exception err) {}
	} else if(emotes.getString(configpath) == null) {
		plugin.getLogger().warning("Replacing '" + configpath + " (boolean) in emotes.yml, it was left blank.");
  	try { 
  	emotes.set(configpath, siono);
  	emotes.save(emotesfile);
  	} catch(Exception err) {}
	}}
	
	static void enableFiles() {
		File folder = Bukkit.getServer().getPluginManager().getPlugin("ChatFeelings").getDataFolder();
		  File msgsfile = new File(folder, File.separator + "messages.yml");
		  FileConfiguration msgs = YamlConfiguration.loadConfiguration(msgsfile);
		  
	  File emotesfile = new File(folder, File.separator + "emotes.yml");
	  FileConfiguration emotes = YamlConfiguration.loadConfiguration(emotesfile);
	  
	  
	  /// ---------------------------------- LEGACY NO TOUCHY OR IT BREAKY ---------------------------------------	  
	  File soundsfile = new File(folder, File.separator + "sounds.yml");
	  FileConfiguration sounds = YamlConfiguration.loadConfiguration(soundsfile);  // Sounds.yml moved to emotes.yml,  this is here for Legacy reasons.
		  
	  if(msgsfile.exists() && !msgs.contains("Version")) {
		  File legacyfolder = new File(plugin.getDataFolder(), File.separator + "Legacy_Files");
		  File legacymsgsfile = new File(legacyfolder, File.separator + "legacy_messages.yml");
		  
		  plugin.getLogger().warning("Legacy messages.yml from v3.X detected. Renaming to 'legacy_messages.yml' & starting anew.");
		  try {
			msgs.save(legacymsgsfile);
			msgsfile.delete();
		} catch (Exception noerr) {}
	  }
	  
	  if(emotesfile.exists() && !emotes.contains("Version")) {
		  File legacyfolder = new File(plugin.getDataFolder(), File.separator + "Legacy_Files");
		  File legacyemotesfile = new File(legacyfolder, File.separator + "legacy_emotes.yml");

		  plugin.getLogger().warning("Legacy emotes.yml from v3.X detected. Renaming to 'legacy_emotes.yml' & starting anew.");
		  try {
			emotes.save(legacyemotesfile);
			emotesfile.delete();
		} catch (Exception noerr) {}
	  }
	  
	  if(soundsfile.exists()) {
		  File legacyfolder = new File(plugin.getDataFolder(), File.separator + "Legacy_Files");
		  File legacysoundsfile = new File(legacyfolder, File.separator + "legacy_sounds.yml");
		  
		  plugin.getLogger().warning("Legacy sounds.yml from v3.X detected. Renaming to 'legacy_sounds.yml' & starting anew.");
		  
		  try {
			sounds.save(legacysoundsfile);
			soundsfile.delete();
		} catch (Exception noerr) {}
	  }
	//------------------------------------------ END OF LEGACY SOUND.YML CHECK -------------------------------------------
	  
	  
	    if (!msgsfile.exists() || !msgs.contains("Version")) {
	    	try {
	    	msgs.save(msgsfile);
	    	plugin.getLogger().info("Created new messages.yml file...");
	    	msgs.options().header("Looking for messages for the feelings?\nThose can now be found in the emotes.yml!");
		    }catch(Exception noerr) { plugin.getLogger().warning("Couldn't create new messages.yml file."); }
	    	
	    } else if(msgs.getInt("Version") != 9) {
			plugin.getLogger().info("Updating your messages.yml with new additional messages...");  
			
	    	if(msgs.getInt("Version") < 6 ) {
	    		forceMsgs("Reload", "&8&l> &a&l✓  &7Plugin reloaded in &f%time%");
	    	}
	    	
	    	if(msgs.getInt("Version") < 7) {
	    		forceMsgs("Player-Is-Sleeping", null); // added in v3, removed in v7
	    		forceMsgs("No-Player-Ignore", null); // removed in v7
	    	}
		}
		  
	    	setMsgs("Prefix", "&a&lC&r&ahat&f&lF&r&feelings &8&l┃");			
	    	setMsgs("Reload", "&8&l> &a&l✓  &7Plugin reloaded in &f%time%"); // updated in version 5
	    	setMsgs("Console-Name", "The Server");
	    	setMsgs("No-Permission", "&cSorry. &fYou don't have permission for that.");
	    	setMsgs("Feelings-Help", "&a&lFeelings:");
	    	setMsgs("Feelings-Help-Page", "&7(Page &f%page%&8&l/&r&f%pagemax%&7)");
	    	setMsgs("Sending-World-Disabled", "&cSorry. &fYou can't use feelings in this world.");
	    	setMsgs("Disabled-Serverwide-Targets", "&cNot Allowed. &fThis server has disabled emoting everyone.");
	    	setMsgs("Receiving-World-Disabled", "&cSorry. &fYour target is in a world with feelings disabled.");
	    	setMsgs("Page-Not-Found", "&cOops. &fThat page doesn't exist, try &7/feelings 1");
	    	setMsgs("No-Player", "&cOops! &fYou need to provide a player to do that to."); // updated in version 2
	    	setMsgs("No-Player-Mute", "&cOops! &fYou must provide a player to mute."); // added in version 3
	    	setMsgs("No-Player-Unmute", "&cOops! &fYou must provide a player to unmute."); // added in version 3
	    	setMsgs("Player-Offline", "&cPlayer Offline. &fWe couldn't find &7&l%player% &fon the server.");
	    	setMsgs("Player-Never-Joined", "&cHmm. &fThat player has never joined before.");
	    	setMsgs("Outside-Of-Radius", "&cHmm. &fYou're too far away from &7%player% &fto use that.");
	    	setMsgs("Cooldown-Active", "&cSlow Down. &fWait &7%time% &fbefore doing that again.");
	    	setMsgs("Ignore-Cooldown", "&cSlow Down. &fPlease wait before ignoring again.");
	    	setMsgs("Console-Not-Player", "&cGoofball! &fThe &7CONSOLE&f is not a real player.");
	    	setMsgs("Sender-Is-Target", "&cYou Silly! &fYou can't %command% &fyourself.");
	    	setMsgs("Is-Muted", "&cYou're Muted. &fYou can no longer use feelings."); // added in version 3
	    	setMsgs("Folder-Not-Found", "&cHmm. &fThere is no data to display here."); // added in version 4
	    	setMsgs("Stats-Header-Own", "&e&lYour Statistics:"); // added in version 6
	    	setMsgs("Stats-Header-Other", "&e&l%player%'s Statistics:"); // added in version 6
	    	setMsgs("Ignore-List-Header", "&c&lIgnored Players:"); // added in version 7
	    	setMsgs("Ignore-List-None", "   &8&l> &fYou are currently not ignoring anyone!"); // added in version 7
	    	setMsgs("Ignore-List-All", "   &8&l> &fYou are ignoring all feelings."); // added in version 8
	    	setMsgs("Ignore-List-Cooldown", "&cPlease Wait. &fYou must wait before checking who you're ignoring.");
	    	setMsgs("Mute-List-Header", "&e&lMuted Players:"); // added in version 4
	    	setMsgs("Mute-List-Player", "&r  &8&l> &f%player%"); // added in version 4
	    	setMsgs("Mute-List-Total-One", "&r  &7There is &f&l%total% &7muted player."); // added in version 4
	    	setMsgs("Mute-List-Total-Many", "&r  &7There are &f&l%total% &7muted players."); // added in version 4
	    	setMsgs("Mute-List-Total-Zero", "&r  &8&l> &a&lYay! &7No players are currently muted."); // added in version 4
	    	setMsgs("Player-Has-Been-Muted", "&cUser Muted. &7%player% &fcan no longer use feelings."); // added in version 3
	    	setMsgs("Player-Muted-Via-Essentials", "&cOops! &7%player&f is muted via Essentials, use /unmute!"); // added in version 5
	    	setMsgs("Player-Muted-Via-LiteBans", "&cOops! &7%player&f is muted via LiteBans, use /unmute!"); // added in version 5
	    	setMsgs("Player-Muted-Via-AdvancedBan", "&cOops! &7%player&f is muted via AdvancedBans, use /unmute!"); // added in version 5
	    	setMsgs("Extra-Mute-Present", "&r&7&oThey're already muted via your punishment system. &e&oSee /cf mutelist"); // added in version 5
	    	setMsgs("Player-Has-Been-Unmuted", "&aUser Unmuted. &7%player% &fcan now use feelings again."); // added in version 3
	    	setMsgs("Cant-Mute-Self", "&cYou Silly! &fYou can't mute yourself."); // added in version 3
	    	setMsgs("Player-Already-Muted", "&cOops. &fThis player is already muted."); // added in version 3
	    	setMsgs("Player-Already-Unmuted", "&cOops. &fYou can't unmute a player who's not muted."); // added in version 3
	    	setMsgs("Already-Mute-Unmute-Suggestion", "&7&oCould you have meant &e&o/cf unmute"); // added in version 3
	    	setMsgs("No-Perm-Mute-Suggestion", "&7&oCould you have meant &e&o/cf ignore&7&o?");
	    	setMsgs("Emote-Disabled", "&cEmote Disabled. &fThis emotion has been disabled by the server.");
	    	setMsgs("Ingoring-On-Player", "&7You've now &c&lBLOCKED &r&7feelings from: &f%player%");
	    	setMsgs("Ingoring-Off-Player", "&7Now &a&lALLOWING &7feelings from: &f%player%");
	    	setMsgs("Ingoring-On-All", "&7You've now &c&lBLOCKED &r&7feelings from all players.");
	    	setMsgs("Ingoring-Off-All", "&7Now &a&lALLOWING &7feelings from all players.");
	    	setMsgs("Cant-Ignore-Self", "&cYou Silly! &fYou can't ignore yourself.");
	    	setMsgs("Target-Is-Ignoring", "&cBummer! &fThis player has blocked you.");
	    	setMsgs("Target-Is-Ignoring-All", "&cBummer! &fThis player is not accepting feelings.");
	    	setMsgs("Command_Descriptions.Hug", "Give someone a nice warm hug!");
	    	setMsgs("Command_Descriptions.Slap", "Slap some sense back into someone.");
	    	setMsgs("Command_Descriptions.Poke", "Poke someone to get their attention");
	    	setMsgs("Command_Descriptions.Highfive", "Show your support, and give a highfive!");
	    	setMsgs("Command_Descriptions.Facepalm", "Need to show some disapproval?");
	    	setMsgs("Command_Descriptions.Yell", "Yell at someone as loud as possible!");
	    	setMsgs("Command_Descriptions.Bite", "Bite a player right on the arm.");
	    	setMsgs("Command_Descriptions.Snuggle", "Snuggle up with the power of warm hugs!");
	    	setMsgs("Command_Descriptions.Shake", "Shake a player to their feet.");
	    	setMsgs("Command_Descriptions.Stab", "Stab someone with a knife. Ouch!");
	    	setMsgs("Command_Descriptions.Kiss", "Give a kiss on the cheek. How sweet!");
	    	setMsgs("Command_Descriptions.Punch", "Punch someone back from insanity!");
	    	setMsgs("Command_Descriptions.Murder", "Finna kill someone here.");
	    	setMsgs("Command_Descriptions.Boi", "Living in 2016? Boi at a player.");
	    	setMsgs("Command_Descriptions.Cry", "Real sad hours? Cry at someone.");
	    	setMsgs("Command_Descriptions.Dab", "Freshly dab on someone.");
	    	setMsgs("Command_Descriptions.Lick", "Lick someone like an ice-cream sundae!");
	    	setMsgs("Command_Descriptions.Pat", "Pat a players head for being good.");
	    	setMsgs("Command_Descriptions.Stalk", "Stalk a player carefully... carefully.");
	    	setMsgsVersion(9);
	  
	    	
		    if (!emotesfile.exists() || !emotes.contains("Version")) {
		    	try {
		    	emotes.save(emotesfile);
		    	plugin.getLogger().info("Created new emotes.yml file...");
			    }catch(Exception noerr) { plugin.getLogger().warning("Couldn't create new emotes.yml file."); }
		    } else if(emotes.get("Feelings.Spook") != null) {
		    	forceEmotes("Feelings.Spook", null);
		    } else if(emotes.getInt("Version") != 4) {
				plugin.getLogger().info("Updating your emotes.yml for the latest update...");  
		    	if(emotes.getInt("Version") <= 3) {
		    		if(emotes.getString("Feelings.Bite.Msgs.Sender").contains("info")) {
		    			forceEmotes("Feelings.Bite.Msgs.Sender", "&7You sink your teeth into &c&l%player%&r&7's skin");
		    			plugin.getLogger().info("Fixing a typo in the the '/bite' command for sender...");
		    		}
		    	}
		    	
	    		setEmotesVersion(4);
			}
		    
    setEmotesBoolean("Feelings.Hug.Enable", true);	  
	setEmotes("Feelings.Hug.Msgs.Sender", "&7You give &a&l%player% &r&7a warm hug. &cAwww &4❤");
	setEmotes("Feelings.Hug.Msgs.Target", "&a&l%player% &r&7gives you a warm hug. &cAwww &4❤");
	setEmotes("Feelings.Hug.Msgs.Global", "&a&l%sender% &r&7gave &2&l%target% &r&7a warm hug. &cAwww &4❤");
//	setEmotes("Feelings.Hug.Msgs.Everyone", "&a&l%player% &r&7gives everyone a warm hug. &cAwww &4❤");
	setEmotes("Feelings.Hug.Sounds.Sound1.Name", "ENTITY_CAT_PURREOW");
	setEmotesDouble("Feelings.Hug.Sounds.Sound1.Volume", 2.0);
	setEmotesDouble("Feelings.Hug.Sounds.Sound1.Pitch", 2.0);
	setEmotes("Feelings.Hug.Sounds.Sound2.Name", "None");
	setEmotesDouble("Feelings.Hug.Sounds.Sound2.Volume", 0.0);
	setEmotesDouble("Feelings.Hug.Sounds.Sound2.Pitch", 0.0);

	setEmotesBoolean("Feelings.Bite.Enable", true);
	setEmotes("Feelings.Bite.Msgs.Sender", "&7You sink your teeth into &c&l%player%&r&7's skin.");
	setEmotes("Feelings.Bite.Msgs.Target", "&c&l%player% &r&7sinks their teeth into your skin.");
	setEmotes("Feelings.Bite.Msgs.Global", "&c&l%sender% &r&7sank their teeth into &4&l%target%&r&7's skin");
//	setEmotes("Feelings.Bite.Msgs.Everyone", "&c&l%player% &r&7sinks their teeth into everyone's skin.");
	setEmotes("Feelings.Bite.Sounds.Sound1.Name", "ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR");
	setEmotesDouble("Feelings.Bite.Sounds.Sound1.Volume", 2.0);
	setEmotesDouble("Feelings.Bite.Sounds.Sound1.Pitch", 2.0);
	setEmotes("Feelings.Bite.Sounds.Sound2.Name", "None");
	setEmotesDouble("Feelings.Bite.Sounds.Sound2.Volume", 0.0);
	setEmotesDouble("Feelings.Bite.Sounds.Sound2.Pitch", 0.0);
	
	setEmotesBoolean("Feelings.Punch.Enable", true);
	setEmotes("Feelings.Punch.Msgs.Sender", "&7You strike &c&l%player% &r&7with a punch. Ouch!");
	setEmotes("Feelings.Punch.Msgs.Target", "&c&l%player% &r&7strikes you with a punch. Ouch!");
	setEmotes("Feelings.Punch.Msgs.Global", "&c&l%sender% &r&7punched &4&l%target% &r&7right in the face.");
//	setEmotes("Feelings.Punch.Msgs.Everyone", "&c&l%player% &r&7punches everyone in the face.");
	setEmotes("Feelings.Punch.Sounds.Sound1.Name", "ENTITY_IRON_GOLEM_ATTACK");
	setEmotesDouble("Feelings.Punch.Sounds.Sound1.Volume", 2.0);
	setEmotesDouble("Feelings.Punch.Sounds.Sound1.Pitch", 0.6);
	setEmotes("Feelings.Punch.Sounds.Sound2.Name", "None");
	setEmotesDouble("Feelings.Punch.Sounds.Sound2.Volume", 0.0);
	setEmotesDouble("Feelings.Punch.Sounds.Sound2.Pitch", 0.0);
	
	setEmotesBoolean("Feelings.Murder.Enable", true);
	setEmotes("Feelings.Murder.Msgs.Sender", "&7You murder &c&l%player% &r&7and have no regrets.");
	setEmotes("Feelings.Murder.Msgs.Target", "&c&l%player% &r&7just murdered you. Bandaid anyone?");
	setEmotes("Feelings.Murder.Msgs.Global", "&c&l%sender% &r&7just murdered &4&l%target%&r&7. &7&lRIP");
	setEmotes("Feelings.Murder.Sounds.Sound1.Name", "ENTITY_BLAZE_DEATH");
	setEmotesDouble("Feelings.Murder.Sounds.Sound1.Volume", 1.0);
	setEmotesDouble("Feelings.Murder.Sounds.Sound1.Pitch", 0.7);
	setEmotes("Feelings.Murder.Sounds.Sound2.Name", "None");
	setEmotesDouble("Feelings.Murder.Sounds.Sound2.Volume", 0.0);
	setEmotesDouble("Feelings.Murder.Sounds.Sound2.Pitch", 0.0);
	
	setEmotesBoolean("Feelings.Boi.Enable", true);
	setEmotes("Feelings.Boi.Msgs.Sender", "&7You inhale at &e&l%player%&r&7... &6&lBOI");
	setEmotes("Feelings.Boi.Msgs.Target", "&e&l%player% &r&7inhales at you... &6&lBOI");
	setEmotes("Feelings.Boi.Msgs.Global", "&e&l%sender% &r&7inhales at &6&l%target%&r&7... &6&l&oBOI");
	setEmotes("Feelings.Boi.Sounds.Sound1.Name", "ENTITY_CHICKEN_EGG");
	setEmotesDouble("Feelings.Boi.Sounds.Sound1.Volume", 2.0);
	setEmotesDouble("Feelings.Boi.Sounds.Sound1.Pitch", 0.1);
	setEmotes("Feelings.Boi.Sounds.Sound2.Name", "None");
	setEmotesDouble("Feelings.Boi.Sounds.Sound2.Volume", 0.0);
	setEmotesDouble("Feelings.Boi.Sounds.Sound2.Pitch", 0.0);
	
	setEmotesBoolean("Feelings.Dab.Enable", true);
	setEmotes("Feelings.Dab.Msgs.Sender", "&7You freshly dab on &a&l%player%&r&7... &7&oGot'em.");
	setEmotes("Feelings.Dab.Msgs.Target", "&a&l%player% &r&7freshly dabs on you... &7&oGot'em.");
	setEmotes("Feelings.Dab.Msgs.Global", "&a&l%sender% &r&7freshly dabs on &2&l%target%&r&7... &7&oGot'em.");
	setEmotes("Feelings.Dab.Sounds.Sound1.Name", "ENTITY_CHICKEN_EGG");
	setEmotesDouble("Feelings.Dab.Sounds.Sound1.Volume", 2.0);
	setEmotesDouble("Feelings.Dab.Sounds.Sound1.Pitch", 0.1);
	setEmotes("Feelings.Dab.Sounds.Sound2.Name", "None");
	setEmotesDouble("Feelings.Dab.Sounds.Sound2.Volume", 0.0);
	setEmotesDouble("Feelings.Dab.Sounds.Sound2.Pitch", 0.0);
	
	setEmotesBoolean("Feelings.Cry.Enable", true);
	setEmotes("Feelings.Cry.Msgs.Sender", "&7You cry on &b&l%player%&r&7's shoulder.");
	setEmotes("Feelings.Cry.Msgs.Target", "&b&l%player% &r&7cries on your shoulder.");
	setEmotes("Feelings.Cry.Msgs.Global", "&b&l%sender% &r&7leans on &3&l%target%&r&7's shoulder and cries.");
	setEmotes("Feelings.Cry.Sounds.Sound1.Name", "ENTITY_GHAST_DEATH");
	setEmotesDouble("Feelings.Cry.Sounds.Sound1.Volume", 1.0);
	setEmotesDouble("Feelings.Cry.Sounds.Sound1.Pitch", 0.8);
	setEmotes("Feelings.Cry.Sounds.Sound2.Name", "None");
	setEmotesDouble("Feelings.Cry.Sounds.Sound2.Volume", 0.0);
	setEmotesDouble("Feelings.Cry.Sounds.Sound2.Pitch", 0.0);
	
	setEmotesBoolean("Feelings.Facepalm.Enable", true);
	setEmotes("Feelings.Facepalm.Msgs.Sender", "&7You facepalm at what &e&l%player% &r&7just said.");
	setEmotes("Feelings.Facepalm.Msgs.Target", "&e&l%player% &r&7facepalmed at what you just said.");
	setEmotes("Feelings.Facepalm.Msgs.Global", "&e&l%sender% &r&7facepalms at &6&l%target%&r&7 for being dumb.");
	setEmotes("Feelings.Facepalm.Sounds.Sound1.Name", "ENTITY_VILLAGER_NO");
	setEmotesDouble("Feelings.Facepalm.Sounds.Sound1.Volume", 2.0);
	setEmotesDouble("Feelings.Facepalm.Sounds.Sound1.Pitch", 1.0);
	setEmotes("Feelings.Facepalm.Sounds.Sound2.Name", "None");
	setEmotesDouble("Feelings.Facepalm.Sounds.Sound2.Volume", 0.0);
	setEmotesDouble("Feelings.Facepalm.Sounds.Sound2.Pitch", 0.0);
	
	// need pitch & volume values:
	
	setEmotesBoolean("Feelings.Highfive.Enable", true);
	setEmotes("Feelings.Highfive.Msgs.Sender", "&7You give a mighty highfive to &a&l%player%&7.");
	setEmotes("Feelings.Highfive.Msgs.Target", "&a&l%player% &7gives you a mighty highfive.");
	setEmotes("Feelings.Highfive.Msgs.Global", "&a&l%sender% &7gives &2&l%target% &r&7a mighty highfive.");
	setEmotes("Feelings.Highfive.Sounds.Sound1.Name", "ENTITY_VILLAGER_YES");
	setEmotesDouble("Feelings.Highfive.Sounds.Sound1.Volume", 2.0);
	setEmotesDouble("Feelings.Highfive.Sounds.Sound1.Pitch", 1.0);
	setEmotes("Feelings.Highfive.Sounds.Sound2.Name", "None");
	setEmotesDouble("Feelings.Highfive.Sounds.Sound2.Volume", 0.0);
	setEmotesDouble("Feelings.Highfive.Sounds.Sound2.Pitch", 0.0);
	
	setEmotesBoolean("Feelings.Kiss.Enable", true);
	setEmotes("Feelings.Kiss.Msgs.Sender", "&7You give &a&l%player% &r&7a kiss. &cAwww &4❤");
	setEmotes("Feelings.Kiss.Msgs.Target", "&a&l%player% &r&7gives you a kiss. &cAwww &4❤");
	setEmotes("Feelings.Kiss.Msgs.Global", "&a&l%sender% &7gives &2&l%target% &7a kiss. &cAwww &4❤");
	setEmotes("Feelings.Kiss.Sounds.Sound1.Name", "ENTITY_ARROW_HIT_PLAYER");
	setEmotesDouble("Feelings.Kiss.Sounds.Sound1.Volume", 2.0);
	setEmotesDouble("Feelings.Kiss.Sounds.Sound1.Pitch", 1.0);
	setEmotes("Feelings.Kiss.Sounds.Sound2.Name", "None");
	setEmotesDouble("Feelings.Kiss.Sounds.Sound2.Volume", 0.0);
	setEmotesDouble("Feelings.Kiss.Sounds.Sound2.Pitch", 0.0);
	
	setEmotesBoolean("Feelings.Lick.Enable", true);
	setEmotes("Feelings.Lick.Msgs.Sender", "&7You lick &e&l%player% &7like ice-cream. &6Gross!");
	setEmotes("Feelings.Lick.Msgs.Target", "&e&l%player% &r&7licks you like ice-cream. &6Gross!");
	setEmotes("Feelings.Lick.Msgs.Global", "&e&l%target% &r&7got licked by &6&l%sender%&r&7. &8Gross.");
	setEmotes("Feelings.Lick.Sounds.Sound1.Name", "ENTITY_GENERIC_DRINK");
	setEmotesDouble("Feelings.Lick.Sounds.Sound1.Volume", 2.0);
	setEmotesDouble("Feelings.Lick.Sounds.Sound1.Pitch", 0.1); // not sure
	setEmotes("Feelings.Lick.Sounds.Sound2.Name", "None");
	setEmotesDouble("Feelings.Lick.Sounds.Sound2.Volume", 0.0);
	setEmotesDouble("Feelings.Lick.Sounds.Sound2.Pitch", 0.0);
	
	setEmotesBoolean("Feelings.Shake.Enable", true);
	setEmotes("Feelings.Shake.Msgs.Sender", "&7You shake &c&l%player%&r&7's entire body.");
	setEmotes("Feelings.Shake.Msgs.Target", "&c&l%player% &r&7shakes your entire body.");
	setEmotes("Feelings.Shake.Msgs.Global", "&c&l%sender% &r&7picks up &4&l%target%&r&7's body, and shakes it.");
	setEmotes("Feelings.Shake.Sounds.Sound1.Name", "ENTITY_WOLF_SHAKE");
	setEmotesDouble("Feelings.Shake.Sounds.Sound1.Volume", 2.0);
	setEmotesDouble("Feelings.Shake.Sounds.Sound1.Pitch", 0.7); // not sure
	setEmotes("Feelings.Shake.Sounds.Sound2.Name", "None");
	setEmotesDouble("Feelings.Shake.Sounds.Sound2.Volume", 0.0);
	setEmotesDouble("Feelings.Shake.Sounds.Sound2.Pitch", 0.0);
	
	setEmotesBoolean("Feelings.Snuggle.Enable", true);
	setEmotes("Feelings.Snuggle.Msgs.Sender", "&7You snuggle &a&l%player% &r&7with love. &cAwww &4❤");
	setEmotes("Feelings.Snuggle.Msgs.Target", "&a&l%player% &r&7snuggles you with love. &cAwww &4❤");
	setEmotes("Feelings.Snuggle.Msgs.Global", "&a&l%sender% &r&7snuggles &2&l%target% &r&7them with hugs. &cAwww &4❤");
	setEmotes("Feelings.Snuggle.Sounds.Sound1.Name", "ENTITY_CAT_PURR");
	setEmotesDouble("Feelings.Snuggle.Sounds.Sound1.Volume", 2.0);
	setEmotesDouble("Feelings.Snuggle.Sounds.Sound1.Pitch", 1.0); // not sure
	setEmotes("Feelings.Snuggle.Sounds.Sound2.Name", "None");
	setEmotesDouble("Feelings.Snuggle.Sounds.Sound2.Volume", 0.0);
	setEmotesDouble("Feelings.Snuggle.Sounds.Sound2.Pitch", 0.0);
	
	setEmotesBoolean("Feelings.Yell.Enable", true);
	setEmotes("Feelings.Yell.Msgs.Sender", "&7You yell at &c&l%player%&r&7 at the top of your lungs.");
	setEmotes("Feelings.Yell.Msgs.Target", "&c&l%player% &r&7yells at you from the top of their lungs.");
	setEmotes("Feelings.Yell.Msgs.Global", "&c&l%sender% &r&7yells right at &4&l%target% &r&7from the top of their lungs.");
	setEmotes("Feelings.Yell.Sounds.Sound1.Name", "ENTITY_GHAST_SCREAM");
	setEmotesDouble("Feelings.Yell.Sounds.Sound1.Volume", 2.0);
	setEmotesDouble("Feelings.Yell.Sounds.Sound1.Pitch", 1.0); // not sure
	setEmotes("Feelings.Yell.Sounds.Sound2.Name", "None");
	setEmotesDouble("Feelings.Yell.Sounds.Sound2.Volume", 0.0);
	setEmotesDouble("Feelings.Yell.Sounds.Sound2.Pitch", 0.0);
	
	setEmotesBoolean("Feelings.Poke.Enable", true);
	setEmotes("Feelings.Poke.Msgs.Sender", "&7You poked &e&l%player%&7. Maybe they're on vacation?");
	setEmotes("Feelings.Poke.Msgs.Target", "&e&l%player% &r&7has poked you. Anyone there?");
	setEmotes("Feelings.Poke.Msgs.Global", "&e&l%target% &r&7was poked by &6&l%sender%&r&7. &7&oAnyone home?");
	setEmotes("Feelings.Poke.Sounds.Sound1.Name", "ENTITY_CHICKEN_EGG");
	setEmotesDouble("Feelings.Poke.Sounds.Sound1.Volume", 2.0);
	setEmotesDouble("Feelings.Poke.Sounds.Sound1.Pitch", 0.1); // not sure
	setEmotes("Feelings.Poke.Sounds.Sound2.Name", "None");
	setEmotesDouble("Feelings.Poke.Sounds.Sound2.Volume", 0.0);
	setEmotesDouble("Feelings.Poke.Sounds.Sound2.Pitch", 0.0);
	
	setEmotesBoolean("Feelings.Slap.Enable", true);
	setEmotes("Feelings.Slap.Msgs.Sender", "&7You slap &c&l%player% &r&7with some spaghetti.");
	setEmotes("Feelings.Slap.Msgs.Target", "&c&l%player% &r&7slaps you with some spaghetti.");
	setEmotes("Feelings.Slap.Msgs.Global", "&c&l%target% &r&7was slapped by &4&l%sender%&r&7.");
	setEmotes("Feelings.Slap.Sounds.Sound1.Name", "ENTITY_BLAZE_HURT");
	setEmotesDouble("Feelings.Slap.Sounds.Sound1.Volume", 2.0);
	setEmotesDouble("Feelings.Slap.Sounds.Sound1.Pitch", 0.7); // not sure
	setEmotes("Feelings.Slap.Sounds.Sound2.Name", "None");
	setEmotesDouble("Feelings.Slap.Sounds.Sound2.Volume", 0.0);
	setEmotesDouble("Feelings.Slap.Sounds.Sound2.Pitch", 0.0);
	
	setEmotesBoolean("Feelings.Stab.Enable", true);
	setEmotes("Feelings.Stab.Msgs.Sender", "&7You stab &c&l%player% &r&7with a knife. Got Bandaids?");
	setEmotes("Feelings.Stab.Msgs.Target", "&c&l%player% &r&7grabs a knife and stabs you. Got Bandaids?");
	setEmotes("Feelings.Stab.Msgs.Global", "&c&l%sender% &r&7grabs a knife and stabs &4&l%target%&r&7.");
	setEmotes("Feelings.Stab.Sounds.Sound1.Name", "ENTITY_GENERIC_HURT");
	setEmotesDouble("Feelings.Stab.Sounds.Sound1.Volume", 2.0);
	setEmotesDouble("Feelings.Stab.Sounds.Sound1.Pitch", 0.7); // not sure
	setEmotes("Feelings.Stab.Sounds.Sound2.Name", "None");
	setEmotesDouble("Feelings.Stab.Sounds.Sound2.Volume", 0.0);
	setEmotesDouble("Feelings.Stab.Sounds.Sound2.Pitch", 0.0);
	
	
	setEmotesBoolean("Feelings.Pat.Enable", true);
	setEmotes("Feelings.Pat.Msgs.Sender", "&7You gently pat &a&l%player%&r&7's head for being good.");
	setEmotes("Feelings.Pat.Msgs.Target", "&a&l%player% &r&7gently pats your head for being good.");
	setEmotes("Feelings.Pat.Msgs.Global", "&a&l%sender% &r&7gently pats &2&l%target%&r&7's head for being good.");
	setEmotes("Feelings.Pat.Sounds.Sound1.Name", "ENTITY_WOLF_PANT");
	setEmotesDouble("Feelings.Pat.Sounds.Sound1.Volume", 2.0);
	setEmotesDouble("Feelings.Pat.Sounds.Sound1.Pitch", 0.8); // not sure
	setEmotes("Feelings.Pat.Sounds.Sound2.Name", "None");
	setEmotesDouble("Feelings.Pat.Sounds.Sound2.Volume", 0.0);
	setEmotesDouble("Feelings.Pat.Sounds.Sound2.Pitch", 0.0);
	
	
	setEmotesBoolean("Feelings.Scorn.Enable", true);
	setEmotes("Feelings.Scorn.Msgs.Sender", "&7You scorn &c&l%player% &r&7for what they've done.");
	setEmotes("Feelings.Scorn.Msgs.Target", "&c&l%player% &r&7scorns you for what you've done.");
	setEmotes("Feelings.Scorn.Msgs.Global", "&c&l%sender% &r&7scorns &4&l%target% &r&7for what they've done.");
	setEmotes("Feelings.Scorn.Sounds.Sound1.Name", "ENTITY_ENDERMAN_STARE");
	setEmotesDouble("Feelings.Scorn.Sounds.Sound1.Volume", 2.0);
	setEmotesDouble("Feelings.Scorn.Sounds.Sound1.Pitch", 0.8); // not sure
	setEmotes("Feelings.Scorn.Sounds.Sound2.Name", "None");
	setEmotesDouble("Feelings.Scorn.Sounds.Sound2.Volume", 0.0);
	setEmotesDouble("Feelings.Scorn.Sounds.Sound2.Pitch", 0.0);
	
	
	setEmotesBoolean("Feelings.Stalk.Enable", true);
	setEmotes("Feelings.Stalk.Msgs.Sender", "&7You carefully stalk &e&l%player%&r&7, &7&oHeh Heh.");
	setEmotes("Feelings.Stalk.Msgs.Target", "&e&l%player% &r&7stalks you from a nearby tree.");
	setEmotes("Feelings.Stalk.Msgs.Global", "&e&l%sender% &r&7stalks &6&l%target% &r&7from a nearby tree.");
	setEmotes("Feelings.Stalk.Sounds.Sound1.Name", "AMBIENT_CAVE");
	setEmotesDouble("Feelings.Stalk.Sounds.Sound1.Volume", 2.0);
	setEmotesDouble("Feelings.Stalk.Sounds.Sound1.Pitch", 2.0); // not sure
	setEmotes("Feelings.Stalk.Sounds.Sound2.Name", "None");
	setEmotesDouble("Feelings.Stalk.Sounds.Sound2.Volume", 0.0);
	setEmotesDouble("Feelings.Stalk.Sounds.Sound2.Pitch", 0.0);

	setEmotesVersion(4);
	reloadFiles();
	}
	
	static void reloadFiles() {
		plugin.folder = Bukkit.getServer().getPluginManager().getPlugin("ChatFeelings").getDataFolder();
		plugin.msgsfile = new File(plugin.folder, File.separator + "messages.yml");
		plugin.msg = YamlConfiguration.loadConfiguration(plugin.msgsfile);

		plugin.emotesfile = new File(plugin.folder, File.separator + "emotes.yml");
		plugin.emotes = YamlConfiguration.loadConfiguration(plugin.emotesfile);
	}
}
