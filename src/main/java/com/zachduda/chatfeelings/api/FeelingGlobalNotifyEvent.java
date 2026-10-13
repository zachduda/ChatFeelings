package com.zachduda.chatfeelings.api;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called for each player about to see a global feeling (General.Global-Feelings) sent by a player. Cancelling
 * it hides the message from that one player. Called on that player's thread (the main thread on Spigot/Paper).
 */
@SuppressWarnings("unused")
public class FeelingGlobalNotifyEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final CommandSender sender;
    private final Player target;
    private final String feeling;
    private boolean cancelled;

    public FeelingGlobalNotifyEvent(Player player, CommandSender sender, Player target, String feeling) {
        this.player = player;
        this.sender = sender;
        this.target = target;
        this.feeling = feeling;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /** The player who would see the message. */
    public Player getPlayer() {
        return player;
    }

    public CommandSender getSender() {
        return sender;
    }

    /** The feeling's capitalized name, ie: "Hug". */
    public String getFeeling() {
        return feeling;
    }

    public Player getTarget() {
        return target;
    }
}
