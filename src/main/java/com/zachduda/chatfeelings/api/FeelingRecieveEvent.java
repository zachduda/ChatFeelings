package com.zachduda.chatfeelings.api;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Misspelled in v4. not used.
 */
@Deprecated(since = "5.0.0")
public class FeelingRecieveEvent extends FeelingReceiveEvent {
    public FeelingRecieveEvent(Player target, CommandSender sender, String feeling) {
        super(target, sender, feeling);
    }
}
