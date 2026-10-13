package com.zachduda.chatfeelings.api;

import com.zachduda.chatfeelings.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called before a feeling is sent. Cancelling it stops the feeling (and doesn't start the sender's cooldown).
 * Called on the sender's thread (the main thread on Spigot/Paper).
 */
@SuppressWarnings("unused")
public class FeelingSendEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final CommandSender sender;
    private final Player target;
    private final String feeling;
    private boolean cancelled;

    public FeelingSendEvent(CommandSender sender, Player target, String feeling) {
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

    public Player getTarget() {
        return target;
    }

    /** The message the sender sees, without colour codes. */
    public String getSendersMessage() {
        return fill(ChatFeelingsAPI.getSenderEmoteMessage(feeling), target.getName());
    }

    /** The message the target sees, without colour codes. */
    public String getTargetsMessage() {
        return fill(ChatFeelingsAPI.getTargetEmoteMessage(feeling), sender.getName());
    }

    /** The message everyone sees when global feelings are on, without colour codes. */
    public String getGlobalEmoteMessage() {
        return fill(ChatFeelingsAPI.getGlobalEmoteMessage(feeling), target.getName());
    }

    private String fill(String message, String playerPlaceholder) {
        final String template = message != null ? message : "%sender% used " + feeling + " on %target%";
        return Text.strip(template
                .replace("%player%", playerPlaceholder)
                .replace("%sender%", sender.getName())
                .replace("%target%", target.getName()));
    }
}
