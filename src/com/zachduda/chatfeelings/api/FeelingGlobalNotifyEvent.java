package com.zachduda.chatfeelings.api;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FeelingGlobalNotifyEvent extends Event implements Cancellable {
	
	private final Player p;
	private final CommandSender sender;
	private final Player target;
	private final String feeling;
    private boolean isCancelled;

    public FeelingGlobalNotifyEvent(Player p, CommandSender sender, Player target, String feeling) {
    	this.p = p;
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

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Player getPlayer() {
        return this.p;
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

}
