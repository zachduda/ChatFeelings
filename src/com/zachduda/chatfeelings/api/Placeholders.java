package com.zachduda.chatfeelings.api;

import org.bukkit.OfflinePlayer;

import com.zachduda.chatfeelings.Main;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jetbrains.annotations.NotNull;

public class Placeholders extends PlaceholderExpansion {
	private final Main plugin;
	private String yes = "true";
	private String no = "false";
    
	public Placeholders(Main plugin){
        this.plugin = plugin;
        try {
        	yes = PlaceholderAPIPlugin.booleanTrue();
        	no = PlaceholderAPIPlugin.booleanFalse();
        } catch (Exception err) {
        	plugin.getLogger().info("Unable to hook into PAPI API for boolean results. Defaulting...");
        }
    }
    
    @Override
    public boolean canRegister(){
    	return true;
    }

    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public @NotNull String getAuthor(){
        return plugin.getDescription().getAuthors().get(0);
    }

    @Override
    public @NotNull String getIdentifier(){
        return plugin.getDescription().getName().toLowerCase();
    }

    @Override
    public @NotNull String getVersion(){
        return plugin.getDescription().getVersion();
    }
  
    @Override
    public String onRequest(OfflinePlayer player, String identifier){

        // %chatfeelings_total_sent%
        if(identifier.equalsIgnoreCase("total_sent")){
            return Integer.toString(plugin.APIgetTotalSent(player.getUniqueId()));
        }

        if(identifier.equalsIgnoreCase("incoming_allow")) {
        	return plugin.APIisAcceptingFeelings(player.getUniqueId()) ?yes :no;
        }
        
        for (String fl : Main.fmap.keySet()) {
        	if(identifier.equalsIgnoreCase("total_"+fl) || identifier.equalsIgnoreCase("total_"+fl+"s") || identifier.equalsIgnoreCase("total_"+fl+"es")) {
        		return Integer.toString(plugin.APIgetSentStat(player.getUniqueId(), fl));
        	}
        }

        return null;
    }

}
