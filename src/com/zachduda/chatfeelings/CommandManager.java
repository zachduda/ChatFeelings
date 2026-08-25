package com.zachduda.chatfeelings;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.jetbrains.annotations.NotNull;
import space.arim.morepaperlib.MorePaperLib;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class CommandManager {
    private static Plugin plugin = null;
    private static CommandMap commandMap;
    private static Map<String, Command> knownCommands;
    private static boolean cfAliasRegistered = false;
    private static MorePaperLib mpl;

    /** Custom feeling commands currently registered with the server's CommandMap. */
    private static final Set<String> registeredCustomFeelings = new HashSet<>();

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
            public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] args) {
                // Forward to your main command
                return Bukkit.dispatchCommand(sender, "chatfeelings " + String.join(" ", args));
            }

            @Override
            public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args) {
                // Forward tab completion to your main command
                return  Objects.requireNonNull(Bukkit.getPluginCommand("chatfeelings")).tabComplete(sender, "chatfeelings", args);
            }
        };

        cfCommand.setDescription("Chatfeelings Primary (Alias) Command");
        cfCommand.setUsage("/cf");

        commandMap.register(plugin.getName(), cfCommand);
        cfAliasRegistered = true;
        Main.debug("Registered /cf command alias");
    }

    private static void unregisterCfAlias() {
        if (commandMap == null || knownCommands == null) return;

        // Remove all references to the command
        knownCommands.remove("cf");
        knownCommands.remove(plugin.getName().toLowerCase() + ":cf");

        cfAliasRegistered = false;
        Main.debug("Unregistered /cf command alias");
    }

    /**
     * Registers/unregisters custom feeling commands (from the Feelings folder) with the server's
     * CommandMap so /&lt;customfeeling&gt; works, the same way the /cf alias is registered above.
     * Called by FileSetup after every enableFiles()/reload, so it stays in sync with what's on disk.
     */
    public static void updateCustomFeelingCommands(List<String> customFeelings) {
        if (commandMap == null) return;

        Iterator<String> it = registeredCustomFeelings.iterator();
        while (it.hasNext()) {
            String name = it.next();
            if (!customFeelings.contains(name)) {
                unregisterFeelingCommand(name);
                it.remove();
            }
        }

        for (String name : customFeelings) {
            if (!registeredCustomFeelings.contains(name)) {
                registerFeelingCommand(name);
                registeredCustomFeelings.add(name);
            }
        }
    }

    private static void registerFeelingCommand(String name) {
        if (commandMap == null || !(plugin instanceof TabExecutor)) return;
        final TabExecutor executor = (TabExecutor) plugin;

        String description = FileSetup.getFeelingString(name, "Description");
        if (description == null || description.isBlank()) {
            description = "A custom feeling added via the Feelings folder.";
        }

        Command feelingCommand = new Command(name) {
            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] args) {
                return executor.onCommand(sender, this, label, args);
            }

            @Override
            public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args) {
                return executor.onTabComplete(sender, this, alias, args);
            }
        };

        feelingCommand.setDescription(description);
        feelingCommand.setUsage("/" + name + " <player>");

        String permissionNode = "chatfeelings." + name;
        if (Bukkit.getPluginManager().getPermission(permissionNode) == null) {
            Bukkit.getPluginManager().addPermission(new Permission(permissionNode, PermissionDefault.OP));
        }
        feelingCommand.setPermission(permissionNode);

        commandMap.register(plugin.getName(), feelingCommand);
        Main.log("Registered custom feeling command: /" + name, false, false);
    }

    private static void unregisterFeelingCommand(String name) {
        if (knownCommands == null) return;

        knownCommands.remove(name);
        knownCommands.remove(plugin.getName().toLowerCase() + ":" + name);

        Main.log("Unregistered custom feeling command: /" + name, false, false);
    }
}