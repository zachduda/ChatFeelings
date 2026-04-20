package com.zachduda.chatfeelings.api;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("unused")
public class FeelingSendEvent extends Event implements Cancellable {
	
	private final CommandSender sender;
	private final Player target;
	private final String feeling;
    private boolean isCancelled;

    public FeelingSendEvent(CommandSender sender, Player target, String feeling) {
        this.sender = sender;
        this.target = target;
        this.feeling = feeling;
        this.isCancelled = false;
    }

    public boolean isCancelled() {
        return this.isCancelled;
    }

    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public CommandSender getSender() {
        return this.sender;
    }
    
    public String getFeeling() {
    	return this.feeling;
    }
    
    public Player getTarget() {
    	return this.target;
    }

    private static String stripColor(String s) { return s.replaceAll("(?i)&[0-9a-fk-or]", ""); }
    public String getSendersMessage() { String msg = ChatFeelingsAPI.getSenderEmoteMessage(this.feeling); return stripColor((msg != null ? msg : "%sender% used " + this.feeling + " on %target%").replaceAll("%player%", this.target.getName()).replaceAll("%sender%", this.sender.getName()).replaceAll("%target%", this.target.getName())); }
    public String getTargetsMessage() { String msg = ChatFeelingsAPI.getTargetEmoteMessage(this.feeling); return stripColor((msg != null ? msg : "%sender% used " + this.feeling + " on %target%").replaceAll("%player%", this.sender.getName()).replaceAll("%sender%", this.sender.getName()).replaceAll("%target%", this.target.getName())); }
    public String getGlobalEmoteMessage() { String msg = ChatFeelingsAPI.getGlobalEmoteMessage(this.feeling); return stripColor((msg != null ? msg : "%sender% used " + this.feeling + " on %target%").replaceAll("%player%", this.target.getName()).replaceAll("%sender%", this.sender.getName()).replaceAll("%target%", this.target.getName())); }
}
