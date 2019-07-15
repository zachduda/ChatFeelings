package com.zach_attack.chatfeelings;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;


public class FileSetup {
	static Main plugin = Main.getPlugin(Main.class);
	
	public static void enableFiles() {
	  File folder = Bukkit.getServer().getPluginManager().getPlugin("ChatFeelings").getDataFolder();
	  
	  File msgsfile = new File(folder, File.separator + "messages.yml");
	  FileConfiguration msgs = YamlConfiguration.loadConfiguration(msgsfile);
	  
	  File emotesfile = new File(folder, File.separator + "emotes.yml");
	  FileConfiguration emotes = YamlConfiguration.loadConfiguration(emotesfile);
	  
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
	  
	  if(emotesfile.exists() && !msgs.contains("Version")) {
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
		  
	  try {
	    if (!msgsfile.exists() || !msgs.contains("Version")) {
	    	msgs.save(msgsfile);
	    	msgs.options().header("Looking for messages for the feelings?\nThose can now be found in the emotes.yml!");
	    	msgs.set("Prefix", "&a&lC&r&ahat&f&lF&r&feelings &8&l┃");
	    	msgs.set("Reload", "&2&l✓ &aConfiguration Reloaded.");
	    	msgs.set("Console-Name", "The Server");
	    	msgs.set("No-Permission", "&cSorry. &fYou don't have permission for that.");
	    	msgs.set("Feelings-Help", "&a&lFeelings:");
	    	msgs.set("Feelings-Help-Page", "&7(Page &f%page%&8&l/&r&f%pagemax%&7)");
	    	msgs.set("Sending-World-Disabled", "&cSorry. &fYou can't use feelings in this world.");
	    	msgs.set("Receiving-World-Disabled", "&cSorry. &fYour target is in a world with feelings disabled.");
	    	msgs.set("Page-Not-Found", "&cOops. &fThat page doesn't exist, try &7/feelings 1");
	    	msgs.set("No-Player", "&cOops! &fYou need to provide a player to do that to.");
	    	msgs.set("Player-Offline", "&cPlayer Offline. &fWe couldn't find &7&l%player% &fon the server.");
	    	msgs.set("Player-Never-Joined", "&cHmm. &fThat player has never joined before.");
	    	msgs.set("Cooldown-Active", "&cSlow Down. &fWait &7%time% &fbefore doing that again.");
	    	msgs.set("Ignore-Cooldown", "&cSlow Down. &fPlease wait before ignoring another player.");
	    	msgs.set("Console-Not-Player", "&cGoofball! &fThe &7CONSOLE&f is not a real player.");
	    	msgs.set("Sender-Is-Target", "&cYou Silly! &fYou can't %command% &fyourself.");
	    	msgs.set("Emote-Disabled", "&cEmote Disabled. &fThis emotion has been disabled by the server.");
	    	msgs.set("No-Player-Ignore", "&cOops! &fYou must provide a player to ignore.");
	    	msgs.set("Ingoring-On-Player", "&7You've now &c&lBLOCKED &r&7feelings from: &f%player%");
	    	msgs.set("Ingoring-Off-Player", "&7Now &a&lALLOWING &7feelings from: &f%player%");
	    	msgs.set("Ingoring-On-All", "&7You've now &c&lBLOCKED &r&7feelings from all players.");
	    	msgs.set("Ingoring-Off-All", "&7Now &a&lALLOWING &7feelings from all players.");
	    	msgs.set("Cant-Ignore-Self", "&cYou Silly! &fYou can't ignore yourself.");
	    	msgs.set("Target-Is-Ignoring", "&cBummer! &fThis player has blocked you.");
	    	msgs.set("Target-Is-Ignoring-All", "&cBummer! &fThis player is not accepting feelings.");
	    	msgs.set("Version", 2);
	    	msgs.save(msgsfile);
	    	
	    	plugin.getLogger().info("Created new messages.yml file...");
	    		    	  
	    }}catch(Exception noerr) { plugin.getLogger().warning("Couldn't create new messages.yml file."); }
	  
	  try {
	  if(!emotesfile.exists() || !emotes.contains("Version")) {
		  emotes.save(emotesfile);
    emotes.set("Feelings.Hug.Enable", true);	  
	emotes.set("Feelings.Hug.Msgs.Sender", "&7You give &a&l%player% &r&7a warm hug. &cAwww &4❤");
	emotes.set("Feelings.Hug.Msgs.Target", "&a&l%player% &r&7gives you a warm hug. &cAwww &4❤");
	emotes.set("Feelings.Hug.Msgs.Global", "&a&l%sender% &r&7gave &2&l%target% &r&7a warm hug. &cAwww &4❤");
	emotes.set("Feelings.Hug.Sounds.Sound1.Name", "ENTITY_CAT_PURREOW");
	emotes.set("Feelings.Hug.Sounds.Sound1.Volume", 2.0);
	emotes.set("Feelings.Hug.Sounds.Sound1.Pitch", 2.0);
	emotes.set("Feelings.Hug.Sounds.Sound2.Name", "None");
	emotes.set("Feelings.Hug.Sounds.Sound2.Volume", 0.0);
	emotes.set("Feelings.Hug.Sounds.Sound2.Pitch", 0.0);

	emotes.set("Feelings.Bite.Enable", true);
	emotes.set("Feelings.Bite.Msgs.Sender", "&7You sink your teeth info &c&l%player%&r&7's skin.");
	emotes.set("Feelings.Bite.Msgs.Target", "&c&l%player% &r&7sinks their teeth into your skin.");
	emotes.set("Feelings.Bite.Msgs.Global", "&c&l%sender% &r&7sank their teeth into &4&l%target%&r&7's skin");
	emotes.set("Feelings.Bite.Sounds.Sound1.Name", "ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR");
	emotes.set("Feelings.Bite.Sounds.Sound1.Volume", 2.0);
	emotes.set("Feelings.Bite.Sounds.Sound1.Pitch", 2.0);
	emotes.set("Feelings.Bite.Sounds.Sound2.Name", "None");
	emotes.set("Feelings.Bite.Sounds.Sound2.Volume", 0.0);
	emotes.set("Feelings.Bite.Sounds.Sound2.Pitch", 0.0);
	
	emotes.set("Feelings.Punch.Enable", true);
	emotes.set("Feelings.Punch.Msgs.Sender", "&7You strike &c&l%player% &r&7with a punch. Ouch!");
	emotes.set("Feelings.Punch.Msgs.Target", "&c&l%player% &r&7strikes you with a punch. Ouch!");
	emotes.set("Feelings.Punch.Msgs.Global", "&c&l%sender% &r&7punched &4&l%target% &r&7right in the face.");
	emotes.set("Feelings.Punch.Sounds.Sound1.Name", "ENTITY_IRON_GOLEM_ATTACK");
	emotes.set("Feelings.Punch.Sounds.Sound1.Volume", 2.0);
	emotes.set("Feelings.Punch.Sounds.Sound1.Pitch", 0.6);
	emotes.set("Feelings.Punch.Sounds.Sound2.Name", "None");
	emotes.set("Feelings.Punch.Sounds.Sound2.Volume", 0.0);
	emotes.set("Feelings.Punch.Sounds.Sound2.Pitch", 0.0);
	
	emotes.set("Feelings.Murder.Enable", true);
	emotes.set("Feelings.Murder.Msgs.Sender", "&7You murder &c&l%player% &r&7and have no regrets.");
	emotes.set("Feelings.Murder.Msgs.Target", "&c&l%player% &r&7just murdered you. Bandaid anyone?");
	emotes.set("Feelings.Murder.Msgs.Global", "&c&l%sender% &r&7just murdered &4&l%target%&r&7. &7&lRIP");
	emotes.set("Feelings.Murder.Sounds.Sound1.Name", "ENTITY_BLAZE_DEATH");
	emotes.set("Feelings.Murder.Sounds.Sound1.Volume", 1.0);
	emotes.set("Feelings.Murder.Sounds.Sound1.Pitch", 0.7);
	emotes.set("Feelings.Murder.Sounds.Sound2.Name", "None");
	emotes.set("Feelings.Murder.Sounds.Sound2.Volume", 0.0);
	emotes.set("Feelings.Murder.Sounds.Sound2.Pitch", 0.0);
	
	emotes.set("Feelings.Boi.Enable", true);
	emotes.set("Feelings.Boi.Msgs.Sender", "&7You inhale at &e&l%player%&r&7... &6&lBOI");
	emotes.set("Feelings.Boi.Msgs.Target", "&e&l%player% &r&7inhales at you... &6&lBOI");
	emotes.set("Feelings.Boi.Msgs.Global", "&e&l%sender% &r&7inhales at &6&l%target%&r&7... &6&l&oBOI");
	emotes.set("Feelings.Boi.Sounds.Sound1.Name", "ENTITY_CHICKEN_EGG");
	emotes.set("Feelings.Boi.Sounds.Sound1.Volume", 2.0);
	emotes.set("Feelings.Boi.Sounds.Sound1.Pitch", 0.1);
	emotes.set("Feelings.Boi.Sounds.Sound2.Name", "None");
	emotes.set("Feelings.Boi.Sounds.Sound2.Volume", 0.0);
	emotes.set("Feelings.Boi.Sounds.Sound2.Pitch", 0.0);
	
	emotes.set("Feelings.Dab.Enable", true);
	emotes.set("Feelings.Dab.Msgs.Sender", "&7You freshly dab on &a&l%player%&r&7... &7&oGot'em.");
	emotes.set("Feelings.Dab.Msgs.Target", "&a&l%player% &r&7freshly dabs on you... &7&oGot'em.");
	emotes.set("Feelings.Dab.Msgs.Global", "&a&l%sender% &r&7freshly dabs on &2&l%target%&r&7... &7&oGot'em.");
	emotes.set("Feelings.Dab.Sounds.Sound1.Name", "ENTITY_CHICKEN_EGG");
	emotes.set("Feelings.Dab.Sounds.Sound1.Volume", 2.0);
	emotes.set("Feelings.Dab.Sounds.Sound1.Pitch", 0.1);
	emotes.set("Feelings.Dab.Sounds.Sound2.Name", "None");
	emotes.set("Feelings.Dab.Sounds.Sound2.Volume", 0.0);
	emotes.set("Feelings.Dab.Sounds.Sound2.Pitch", 0.0);
	
	emotes.set("Feelings.Cry.Enable", true);
	emotes.set("Feelings.Cry.Msgs.Sender", "&7You cry on &b&l%player%&r&7's shoulder.");
	emotes.set("Feelings.Cry.Msgs.Target", "&b&l%player% &r&7cries on your shoulder.");
	emotes.set("Feelings.Cry.Msgs.Global", "&b&l%sender% &r&7leans on &3&l%target%&r&7's shoulder and cries.");
	emotes.set("Feelings.Cry.Sounds.Sound1.Name", "ENTITY_GHAST_DEATH");
	emotes.set("Feelings.Cry.Sounds.Sound1.Volume", 1.0);
	emotes.set("Feelings.Cry.Sounds.Sound1.Pitch", 0.8);
	emotes.set("Feelings.Cry.Sounds.Sound2.Name", "None");
	emotes.set("Feelings.Cry.Sounds.Sound2.Volume", 0.0);
	emotes.set("Feelings.Cry.Sounds.Sound2.Pitch", 0.0);
	
	emotes.set("Feelings.Facepalm.Enable", true);
	emotes.set("Feelings.Facepalm.Msgs.Sender", "&7You facepalm at what &e&l%player% &r&7just said.");
	emotes.set("Feelings.Facepalm.Msgs.Target", "&e&l%player% &r&7facepalmed at what you just said.");
	emotes.set("Feelings.Facepalm.Msgs.Global", "&e&l%sender% &r&7facepalms at &6&l%target%&r&7 for being dumb.");
	emotes.set("Feelings.Facepalm.Sounds.Sound1.Name", "ENTITY_VILLAGER_NO");
	emotes.set("Feelings.Facepalm.Sounds.Sound1.Volume", 2.0);
	emotes.set("Feelings.Facepalm.Sounds.Sound1.Pitch", 1.0);
	emotes.set("Feelings.Facepalm.Sounds.Sound2.Name", "None");
	emotes.set("Feelings.Facepalm.Sounds.Sound2.Volume", 0.0);
	emotes.set("Feelings.Facepalm.Sounds.Sound2.Pitch", 0.0);
	
	// need pitch & volume values:
	
	emotes.set("Feelings.Highfive.Enable", true);
	emotes.set("Feelings.Highfive.Msgs.Sender", "&7You give a mighty highfive to &a&l%player%&7.");
	emotes.set("Feelings.Highfive.Msgs.Target", "&a&l%player% &7gives you a mighty highfive.");
	emotes.set("Feelings.Highfive.Msgs.Global", "&a&l%sender% &7gives &2&l%target% &r&7a mighty highfive.");
	emotes.set("Feelings.Highfive.Sounds.Sound1.Name", "ENTITY_VILLAGER_YES");
	emotes.set("Feelings.Highfive.Sounds.Sound1.Volume", 2.0);
	emotes.set("Feelings.Highfive.Sounds.Sound1.Pitch", 1.0);
	emotes.set("Feelings.Highfive.Sounds.Sound2.Name", "None");
	emotes.set("Feelings.Highfive.Sounds.Sound2.Volume", 0.0);
	emotes.set("Feelings.Highfive.Sounds.Sound2.Pitch", 0.0);
	
	emotes.set("Feelings.Kiss.Enable", true);
	emotes.set("Feelings.Kiss.Msgs.Sender", "&7You give &a&l%player% &r&7a kiss. &cAwww &4❤");
	emotes.set("Feelings.Kiss.Msgs.Target", "&a&l%player% &r&7gives you a kiss. &cAwww &4❤");
	emotes.set("Feelings.Kiss.Msgs.Global", "&a&l%sender% &7gives &2&l%target% &7a kiss. &cAwww &4❤");
	emotes.set("Feelings.Kiss.Sounds.Sound1.Name", "ENTITY_ARROW_HIT_PLAYER");
	emotes.set("Feelings.Kiss.Sounds.Sound1.Volume", 2.0);
	emotes.set("Feelings.Kiss.Sounds.Sound1.Pitch", 1.0);
	emotes.set("Feelings.Kiss.Sounds.Sound2.Name", "None");
	emotes.set("Feelings.Kiss.Sounds.Sound2.Volume", 0.0);
	emotes.set("Feelings.Kiss.Sounds.Sound2.Pitch", 0.0);
	
	emotes.set("Feelings.Lick.Enable", true);
	emotes.set("Feelings.Lick.Msgs.Sender", "&7You lick &e&l%player% &7like ice-cream. &6Gross!");
	emotes.set("Feelings.Lick.Msgs.Target", "&e&l%player% &r&7licks you like ice-cream. &6Gross!");
	emotes.set("Feelings.Lick.Msgs.Global", "&e&l%target% &r&7got licked by &6&l%sender%&r&7. &8Gross.");
	emotes.set("Feelings.Lick.Sounds.Sound1.Name", "ENTITY_GENERIC_DRINK");
	emotes.set("Feelings.Lick.Sounds.Sound1.Volume", 2.0);
	emotes.set("Feelings.Lick.Sounds.Sound1.Pitch", 0.1); // not sure
	emotes.set("Feelings.Lick.Sounds.Sound2.Name", "None");
	emotes.set("Feelings.Lick.Sounds.Sound2.Volume", 0.0);
	emotes.set("Feelings.Lick.Sounds.Sound2.Pitch", 0.0);
	
	emotes.set("Feelings.Shake.Enable", true);
	emotes.set("Feelings.Shake.Msgs.Sender", "&7You shake &c&l%player%&r&7's entire body.");
	emotes.set("Feelings.Shake.Msgs.Target", "&c&l%player% &r&7shakes your entire body.");
	emotes.set("Feelings.Shake.Msgs.Global", "&c&l%sender% &r&7picks up &4&l%target%&r&7's body, and shakes it.");
	emotes.set("Feelings.Shake.Sounds.Sound1.Name", "ENTITY_WOLF_SHAKE");
	emotes.set("Feelings.Shake.Sounds.Sound1.Volume", 2.0);
	emotes.set("Feelings.Shake.Sounds.Sound1.Pitch", 0.7); // not sure
	emotes.set("Feelings.Shake.Sounds.Sound2.Name", "None");
	emotes.set("Feelings.Shake.Sounds.Sound2.Volume", 0.0);
	emotes.set("Feelings.Shake.Sounds.Sound2.Pitch", 0.0);
	
	emotes.set("Feelings.Snuggle.Enable", true);
	emotes.set("Feelings.Snuggle.Msgs.Sender", "&7You snuggle &a&l%player% &r&7with love. &cAwww &4❤");
	emotes.set("Feelings.Snuggle.Msgs.Target", "&a&l%player% &r&7snuggles you with love. &cAwww &4❤");
	emotes.set("Feelings.Snuggle.Msgs.Global", "&a&l%sender% &r&7snuggles &2&l%target% &r&7them with hugs. &cAwww &4❤");
	emotes.set("Feelings.Snuggle.Sounds.Sound1.Name", "ENTITY_CAT_PURR");
	emotes.set("Feelings.Snuggle.Sounds.Sound1.Volume", 2.0);
	emotes.set("Feelings.Snuggle.Sounds.Sound1.Pitch", 1.0); // not sure
	emotes.set("Feelings.Snuggle.Sounds.Sound2.Name", "None");
	emotes.set("Feelings.Snuggle.Sounds.Sound2.Volume", 0.0);
	emotes.set("Feelings.Snuggle.Sounds.Sound2.Pitch", 0.0);
	
	emotes.set("Feelings.Yell.Enable", true);
	emotes.set("Feelings.Yell.Msgs.Sender", "&7You yell at &c&l%player%&r&7 at the top of your lungs.");
	emotes.set("Feelings.Yell.Msgs.Target", "&c&l%player% &r&7yells at you from the top of their lungs.");
	emotes.set("Feelings.Yell.Msgs.Global", "&c&l%sender% &r&7yells right at &4&l%target% &r&7from the top of their lungs.");
	emotes.set("Feelings.Yell.Sounds.Sound1.Name", "ENTITY_GHAST_SCREAM");
	emotes.set("Feelings.Yell.Sounds.Sound1.Volume", 2.0);
	emotes.set("Feelings.Yell.Sounds.Sound1.Pitch", 1.0); // not sure
	emotes.set("Feelings.Yell.Sounds.Sound2.Name", "None");
	emotes.set("Feelings.Yell.Sounds.Sound2.Volume", 0.0);
	emotes.set("Feelings.Yell.Sounds.Sound2.Pitch", 0.0);
	
	emotes.set("Feelings.Poke.Enable", true);
	emotes.set("Feelings.Poke.Msgs.Sender", "&7You poked &e%player%&7. Maybe they're on vacation?.");
	emotes.set("Feelings.Poke.Msgs.Target", "&e&l%player% &r&7has poked you. Anyone there?");
	emotes.set("Feelings.Poke.Msgs.Global", "&e&l%target% &r&7was poked by &6&l%sender%&r&7. &7&oAnyone home?");
	emotes.set("Feelings.Poke.Sounds.Sound1.Name", "ENTITY_CHICKEN_EGG");
	emotes.set("Feelings.Poke.Sounds.Sound1.Volume", 2.0);
	emotes.set("Feelings.Poke.Sounds.Sound1.Pitch", 0.1); // not sure
	emotes.set("Feelings.Poke.Sounds.Sound2.Name", "None");
	emotes.set("Feelings.Poke.Sounds.Sound2.Volume", 0.0);
	emotes.set("Feelings.Poke.Sounds.Sound2.Pitch", 0.0);
	
	emotes.set("Feelings.Slap.Enable", true);
	emotes.set("Feelings.Slap.Msgs.Sender", "&7You slap &c&l%player% &r&7with some spaghetti.");
	emotes.set("Feelings.Slap.Msgs.Target", "&c&l%player% &r&7slaps you with some spaghetti.");
	emotes.set("Feelings.Slap.Msgs.Global", "&c&l%target% &r&7was slapped by &4&l%sender%&r&7.");
	emotes.set("Feelings.Slap.Sounds.Sound1.Name", "ENTITY_BLAZE_HURT");
	emotes.set("Feelings.Slap.Sounds.Sound1.Volume", 2.0);
	emotes.set("Feelings.Slap.Sounds.Sound1.Pitch", 0.7); // not sure
	emotes.set("Feelings.Slap.Sounds.Sound2.Name", "None");
	emotes.set("Feelings.Slap.Sounds.Sound2.Volume", 0.0);
	emotes.set("Feelings.Slap.Sounds.Sound2.Pitch", 0.0);
	
	emotes.set("Feelings.Stab.Enable", true);
	emotes.set("Feelings.Stab.Msgs.Sender", "&7You stab &c&l%player% &r&7with a knife. Got Bandaids?");
	emotes.set("Feelings.Stab.Msgs.Target", "&c&l%player% &r&7grabs a knife and stabs you. Got Bandaids?");
	emotes.set("Feelings.Stab.Msgs.Global", "&c&l%sender% &r&7grabs a knife and stabs &4&l%target%&r&7.");
	emotes.set("Feelings.Stab.Sounds.Sound1.Name", "ENTITY_GENERIC_HURT");
	emotes.set("Feelings.Stab.Sounds.Sound1.Volume", 2.0);
	emotes.set("Feelings.Stab.Sounds.Sound1.Pitch", 0.7); // not sure
	emotes.set("Feelings.Stab.Sounds.Sound2.Name", "None");
	emotes.set("Feelings.Stab.Sounds.Sound2.Volume", 0.0);
	emotes.set("Feelings.Stab.Sounds.Sound2.Pitch", 0.0);
	
	
	emotes.set("Feelings.Pat.Enable", true);
	emotes.set("Feelings.Pat.Msgs.Sender", "&7You gently pat &a&l%player%&r&7's head for being good.");
	emotes.set("Feelings.Pat.Msgs.Target", "&a&l%player% &r&7gently pats your head for being good.");
	emotes.set("Feelings.Pat.Msgs.Global", "&a&l%sender% &r&7gently pats &2&l%target%&r&7's head for being good.");
	emotes.set("Feelings.Pat.Sounds.Sound1.Name", "ENTITY_WOLF_PANT");
	emotes.set("Feelings.Pat.Sounds.Sound1.Volume", 2.0);
	emotes.set("Feelings.Pat.Sounds.Sound1.Pitch", 0.8); // not sure
	emotes.set("Feelings.Pat.Sounds.Sound2.Name", "None");
	emotes.set("Feelings.Pat.Sounds.Sound2.Volume", 0.0);
	emotes.set("Feelings.Pat.Sounds.Sound2.Pitch", 0.0);
	
	
	emotes.set("Feelings.Scorn.Enable", true);
	emotes.set("Feelings.Scorn.Msgs.Sender", "&7You scorn &c&l%player% &r&7for what they've done.");
	emotes.set("Feelings.Scorn.Msgs.Target", "&c&l%player% &r&7scorns you for what you've done.");
	emotes.set("Feelings.Scorn.Msgs.Global", "&c&l%sender% &r&7scorns &4&l%target% &r&7for what they've done.");
	emotes.set("Feelings.Scorn.Sounds.Sound1.Name", "ENTITY_ENDERMAN_STARE");
	emotes.set("Feelings.Scorn.Sounds.Sound1.Volume", 2.0);
	emotes.set("Feelings.Scorn.Sounds.Sound1.Pitch", 0.8); // not sure
	emotes.set("Feelings.Scorn.Sounds.Sound2.Name", "None");
	emotes.set("Feelings.Scorn.Sounds.Sound2.Volume", 0.0);
	emotes.set("Feelings.Scorn.Sounds.Sound2.Pitch", 0.0);
	
	
	emotes.set("Feelings.Stalk.Enable", true);
	emotes.set("Feelings.Stalk.Msgs.Sender", "&7You carefully stalk &e&l%player%&r&7, &7&oHeh Heh.");
	emotes.set("Feelings.Stalk.Msgs.Target", "&e&l%player% &r&7stalks you from a nearby tree.");
	emotes.set("Feelings.Stalk.Msgs.Global", "&e&l%sender% &r&7stalks &6&l%target% &r&7from a nearby tree.");
	emotes.set("Feelings.Stalk.Sounds.Sound1.Name", "AMBIENT_CAVE");
	emotes.set("Feelings.Stalk.Sounds.Sound1.Volume", 2.0);
	emotes.set("Feelings.Stalk.Sounds.Sound1.Pitch", 2.0); // not sure
	emotes.set("Feelings.Stalk.Sounds.Sound2.Name", "None");
	emotes.set("Feelings.Stalk.Sounds.Sound2.Volume", 0.0);
	emotes.set("Feelings.Stalk.Sounds.Sound2.Pitch", 0.0);
	
	
	
	emotes.set("Version", 1);
	emotes.save(emotesfile);
	plugin.getLogger().info("Created new emotes.yml file...");
	  }}catch(Exception noerr) { plugin.getLogger().warning("Couldn't create new emotes.yml file.");}
	}
}
