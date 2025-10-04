package com.zachduda.chatfeelings;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import space.arim.morepaperlib.MorePaperLib;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class CommandManager {
    private static Plugin plugin = null;
    private static CommandMap commandMap;
    private static Map<String, Command> knownCommands;
    private static boolean cfAliasRegistered = false;
    private static MorePaperLib mpl;

    public CommandManager(Plugin plugin, MorePaperLib morePaperLib) {
        CommandManager.plugin = plugin;
        CommandManager.mpl = morePaperLib;
        setupCommandMap();
    }

    private void setupCommandMap() {
        try {
            if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
                Field f = SimplePluginManager.class.getDeclaredField("commandMap");
                f.setAccessible(true);

                commandMap = mpl.commandRegistration().getServerCommandMap();
                // Get knownCommands map for unregistering
                if (commandMap instanceof SimpleCommandMap) {
                    knownCommands = mpl.commandRegistration().getCommandMapKnownCommands((SimpleCommandMap) commandMap);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to setup CommandMap: " + e.getMessage());
        }
    }

    public static void updateCommands(FileConfiguration config) {
        boolean cfAliasEnabled = config.getBoolean("Other.CF-Alias", true);

        if (cfAliasEnabled && !cfAliasRegistered) {
            registerCfAlias();
        } else if (!cfAliasEnabled && cfAliasRegistered) {
            unregisterCfAlias();
        }
    }

    private static void registerCfAlias() {
        if (commandMap == null) return;

        // Create your command instance
        Command cfCommand = new Command("cf") {
            @Override
            public boolean execute(CommandSender sender, String label, String[] args) {
                // Forward to your main command
                return Bukkit.dispatchCommand(sender, "chatfeelings " + String.join(" ", args));
            }

            @Override
            public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
                // Forward tab completion to your main command
                return  Bukkit.getPluginCommand("chatfeelings").tabComplete(sender, "chatfeelings", args);
            }
        };

        cfCommand.setDescription("Alias for yourmaincommand");
        cfCommand.setUsage("/cf");

        commandMap.register(plugin.getName(), cfCommand);
        cfAliasRegistered = true;
        plugin.getLogger().info("Registered /cf command alias");
    }

    private static void unregisterCfAlias() {
        if (commandMap == null || knownCommands == null) return;

        // Remove all references to the command
        knownCommands.remove("cf");
        knownCommands.remove(plugin.getName().toLowerCase() + ":cf");

        cfAliasRegistered = false;
        plugin.getLogger().info("Unregistered /cf command alias");
    }
}