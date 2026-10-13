package com.zachduda.chatfeelings.command;

import com.zachduda.chatfeelings.config.Settings;
import com.zachduda.chatfeelings.feeling.Feeling;
import com.zachduda.chatfeelings.feeling.FeelingRegistry;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.Supplier;

/**
 * ChatFeelings' permission rules. The console and ops can do everything. When General.Use-Feeling-Permissions
 * is off, everyone may use the non-admin nodes (feelings, /cf stats, /cf ignore). chatfeelings.all covers
 * every feeling, custom ones included.
 */
public final class Permissions {
    private static final String PREFIX = "chatfeelings.";

    private final Supplier<Settings> settings;
    private final FeelingRegistry registry;

    public Permissions(Supplier<Settings> settings, FeelingRegistry registry) {
        this.settings = settings;
        this.registry = registry;
    }

    public boolean has(CommandSender sender, String node, boolean adminNode) {
        if (!(sender instanceof Player)) {
            return true;
        }
        if (sender.hasPermission(node) || sender.isOp()) {
            return true;
        }
        if (adminNode) {
            return false;
        }
        if (!settings.get().useFeelingPermissions()) {
            return true;
        }
        // chatfeelings.all covers every feeling node (never admin nodes), custom feelings included.
        return node.startsWith(PREFIX) && registry.isFeeling(node.substring(PREFIX.length()))
                && sender.hasPermission(PREFIX + "all");
    }

    public boolean has(CommandSender sender, String node) {
        return has(sender, node, false);
    }

    public boolean canUse(CommandSender sender, Feeling feeling) {
        return has(sender, PREFIX + feeling.name(), false);
    }
}
