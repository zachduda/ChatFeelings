package com.zachduda.chatfeelings.api;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * The original, misspelled name of {@link FeelingReceiveEvent}. ChatFeelings still fires this class, so existing
 * listeners keep working, and because it extends {@link FeelingReceiveEvent} (sharing its handler list),
 * listeners of the new name receive it too.
 *
 * @deprecated listen to {@link FeelingReceiveEvent} instead.
 */
@Deprecated(since = "5.0.0")
public class FeelingRecieveEvent extends FeelingReceiveEvent {
    public FeelingRecieveEvent(Player target, CommandSender sender, String feeling) {
        super(target, sender, feeling);
    }
}
