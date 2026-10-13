package com.zachduda.chatfeelings.command;

import com.zachduda.chatfeelings.feeling.FeelingService;
import com.zachduda.chatfeelings.hook.VanishCheck;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Handles every feeling command (/hug, /slap, ... and custom feelings), whose name is the feeling's name. */
public final class FeelingCommand implements TabExecutor {
    private final FeelingService feelings;

    public FeelingCommand(FeelingService feelings) {
        this.feelings = feelings;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        feelings.send(sender, command.getName(), args);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }
        return StringUtil.copyPartialMatches(args[0], visiblePlayerNames(sender), new ArrayList<>());
    }

    static List<String> visiblePlayerNames(CommandSender viewer) {
        final List<String> names = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (VanishCheck.visibleTo(viewer, player)) {
                names.add(player.getName());
            }
        }
        return names;
    }
}
