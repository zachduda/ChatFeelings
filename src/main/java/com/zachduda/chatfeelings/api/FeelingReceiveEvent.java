package com.zachduda.chatfeelings.api;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player is about to receive a feeling, after {@link FeelingSendEvent} wasn't cancelled.
 * Cancelling it stops the feeling. Called on the receiving player's thread (the main thread on Spigot/Paper).
 * <p>
 * ChatFeelings fires this as {@link FeelingRecieveEvent} (the old, misspelled class) so plugins listening to
 * either one keep working. New code should listen to this class.
 */
@SuppressWarnings("unused")
public class FeelingReceiveEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final CommandSender sender;
    private final Player target;
    private final String feeling;
    private boolean cancelled;

    public FeelingReceiveEvent(Player target, CommandSender sender, String feeling) {
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

    public CommandSender getSender() {
        return sender;
    }

    /** The feeling's capitalized name, ie: "Hug". */
    public String getFeeling() {
        return feeling;
    }

    /** The player receiving the feeling. */
    public Player getPlayer() {
        return target;
    }
}
