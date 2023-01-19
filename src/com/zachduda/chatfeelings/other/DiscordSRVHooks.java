package com.zachduda.chatfeelings.other;

import com.zachduda.chatfeelings.Main;
import com.zachduda.chatfeelings.Msgs;
import com.zachduda.chatfeelings.api.ChatFeelingsAPI;
import github.scarsz.discordsrv.api.commands.PluginSlashCommand;
import github.scarsz.discordsrv.api.commands.SlashCommand;
import github.scarsz.discordsrv.api.commands.SlashCommandProvider;
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent;
import github.scarsz.discordsrv.dependencies.jda.api.hooks.EventListener;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.CommandData;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

import static com.zachduda.chatfeelings.Main.capitalizeString;
import static com.zachduda.chatfeelings.Main.feelings;

public class DiscordSRVHooks extends JavaPlugin implements Listener, SlashCommandProvider {


    private final static Main plugin = Main.getPlugin(Main.class);

    public Set<PluginSlashCommand> getSlashCommands() {
        Set<PluginSlashCommand> plc = new HashSet<>();
        for(String fl : feelings) {
            plc.add(new PluginSlashCommand(plugin, new CommandData(fl, plugin.msg.getString("Command_Descriptions."+capitalizeString(fl)))));
        }
        return new HashSet<>(plc);
    }


    @SlashCommand(path = "*")
    public void feelingCommand(SlashCommandEvent e) {
        final String cmd = e.getName().toLowerCase();
        if(feelings.contains(cmd)) {
            Main.debug("Got DiscordSRV Feeling: " + e +  " • " + e.getCommandPath());
            String emoji = "";
            switch(cmd) {
                case("hug") : emoji = ":heart:"; break;
                case("dab") : emoji = ":muscle:"; break;
                default : emoji = ":sparkles:"; break;
            }
            e.reply(">  :white_check_mark:  You sent `/" + cmd + "` to the server!").queue();
            e.getChannel().sendMessage(">   **" +
                    ChatColor.stripColor(
                                    Msgs.color(
                                            plugin.emotes.getString("Feelings."+capitalizeString(cmd)+".Msgs.Global"
                                                    , "You did the **" + cmd + "** emote but the `ChatFeelings` messages folder is **missing** or **corrupted**."
                                            )))
                            .replaceAll("%sender%", e.getUser().getAsMention())
                            .replaceAll("%target%", "everyone")
                            .replaceAll("❤", "") // remove symbol to avoid double
                    + "**  " + emoji
            ).queue();
        }
    }
}
