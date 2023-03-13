package com.zachduda.chatfeelings.other;

import com.zachduda.chatfeelings.Main;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.commands.PluginSlashCommand;
import github.scarsz.discordsrv.api.commands.SlashCommand;
import github.scarsz.discordsrv.api.commands.SlashCommandProvider;
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.CommandData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.zachduda.chatfeelings.Main.capitalizeString;
import static com.zachduda.chatfeelings.Main.feelings;

public class DiscordSRVHooks implements Listener, SlashCommandProvider {

    private final static Main plugin = Main.getPlugin(Main.class);

    public DiscordSRVHooks() {
        plugin.getLogger().info("Initializing DiscordSRV features....");
        DiscordSRV.api.addSlashCommandProvider(this);
    }

    @Override
    public Set<PluginSlashCommand> getSlashCommands() {
        Set<PluginSlashCommand> plc = new HashSet<>();
        for(String fl : feelings) {
            plc.add(new PluginSlashCommand(plugin, new CommandData(fl, Objects.requireNonNull(plugin.msg.getString("Command_Descriptions." + capitalizeString(fl))))));
        }
        return new HashSet<>(plc);
    }

    static String getEmoji(String feeling) {
        return switch (feeling) {
            case ("hug") -> ":heart:";
            case ("dab") -> ":muscle:";
            default -> ":sparkles:";
        };
    }


    @SlashCommand(path = "*")
    public void feelingCommand(SlashCommandEvent e) {
        final String cmd = e.getName().toLowerCase();
        if(feelings.contains(cmd)) {
            Main.debug("Got DiscordSRV Feeling: " + e +  " • " + e.getCommandPath());

            e.reply("> **" +
                    ChatColor.stripColor(
                            ChatColor.translateAlternateColorCodes('&',
                                            plugin.emotes.getString("Feelings."+capitalizeString(cmd)+".Msgs.Global"
                                                    , "Attempted to do the **" + cmd + "** emote but the `ChatFeelings` messages folder is **missing** or **corrupted**."
                                            )))
                            .replaceAll("%sender%", e.getUser().getAsMention())
                            .replaceAll("%target%", "Everyone")
                            .replaceAll("❤", "") // remove symbol to avoid double
                    + "**  " + getEmoji(cmd)
            ).queue();
        }
    }

    public static void broadcast(final String feeling, final String msg) {
        // i should probably find the API for this, but idk how to find the default channel id through DiscordSRV's api cuz its not documented.
        Bukkit.getScheduler().runTask(plugin, () -> {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "discordsrv:discordsrv broadcast **" +
                    ChatColor.stripColor(
                    ChatColor.translateAlternateColorCodes('&',
                            msg.replaceAll("❤", "") + " " + getEmoji(feeling))) + "**");
        });
    }
}
