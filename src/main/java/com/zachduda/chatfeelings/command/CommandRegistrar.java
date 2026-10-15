package com.zachduda.chatfeelings.command;

import com.zachduda.chatfeelings.feeling.Feeling;
import com.zachduda.chatfeelings.platform.Scheduler;
import com.zachduda.chatfeelings.util.Log;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.help.HelpMap;
import org.bukkit.help.HelpTopic;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import space.arim.morepaperlib.MorePaperLib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Registers the commands that aren't in plugin.yml: one per custom feeling, plus the optional /cf alias.
 * <ul>
 *     <li>A name another plugin already uses is skipped with a warning, never taken over.</li>
 *     <li>Unregistering only removes commands this class registered.</li>
 *     <li>No permission is set on the commands themselves: Paper hides commands from players lacking it,
 *     which would make custom feelings op-only. Permissions are checked when the feeling runs, like the
 *     built-in ones.</li>
 * </ul>
 * All methods must be called on the main / global region thread.
 */
public final class CommandRegistrar {
    private static final String CF_ALIAS = "cf";

    private final Plugin plugin;
    private final MorePaperLib morePaperLib;
    private final Scheduler scheduler;
    private final String fallbackPrefix;
    private final Map<String, Command> ours = new HashMap<>();
    private final Set<String> addedPermissions = new HashSet<>();

    private CommandMap commandMap;
    private Map<String, Command> knownCommands;

    public CommandRegistrar(Plugin plugin, MorePaperLib morePaperLib, Scheduler scheduler) {
        this.plugin = plugin;
        this.morePaperLib = morePaperLib;
        this.scheduler = scheduler;
        this.fallbackPrefix = plugin.getName().toLowerCase(Locale.ROOT);
    }

    /** A command ChatFeelings registered at runtime. Identifiable so servers know which plugin owns it. */
    private final class DynamicCommand extends Command implements PluginIdentifiableCommand {
        private final TabExecutor executor;
        private final Command executeAs;

        /** @param executeAs the command passed to the executor (lets /cf behave exactly like /chatfeelings) */
        DynamicCommand(String name, String description, String usage, TabExecutor executor, Command executeAs) {
            super(name, description, usage, List.of());
            this.executor = executor;
            this.executeAs = executeAs;
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String label, String @NotNull [] args) {
            return executor.onCommand(sender, executeAs != null ? executeAs : this, label, args);
        }

        @Override
        public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String @NotNull [] args) {
            final List<String> result = executor.onTabComplete(sender, executeAs != null ? executeAs : this, alias, args);
            return result != null ? result : List.of();
        }

        @Override
        public @NotNull Plugin getPlugin() {
            return plugin;
        }
    }

    private boolean ready() {
        if (commandMap != null) {
            return true;
        }
        try {
            commandMap = morePaperLib.commandRegistration().getServerCommandMap();
            if (commandMap instanceof SimpleCommandMap simple) {
                knownCommands = morePaperLib.commandRegistration().getCommandMapKnownCommands(simple);
            }
            return commandMap != null;
        } catch (RuntimeException err) {
            Log.warn("Unable to access the server's command map, so /cf and custom feelings can't be registered", err);
            return false;
        }
    }

    /**
     * Makes the registered commands match the given custom feelings and /cf setting.
     *
     * @param cfCommand        the /chatfeelings command, which /cf forwards to. Null leaves /cf as it is if it
     *                         isn't registered yet (it's only removed if cfAlias is false).
     * @param notifyPlayers    true after startup, to refresh online players' command lists
     */
    public void sync(Collection<Feeling> customFeelings, TabExecutor feelingExecutor, boolean cfAlias,
                     TabExecutor cfExecutor, Command cfCommand, boolean notifyPlayers) {
        if (!ready()) {
            return;
        }
        final Map<String, Feeling> wanted = new HashMap<>();
        for (Feeling feeling : customFeelings) {
            wanted.put(feeling.name(), feeling);
        }
        boolean changed = false;
        final List<String> added = new ArrayList<>();

        for (String label : List.copyOf(ours.keySet())) {
            final boolean stillWanted = label.equals(CF_ALIAS) ? cfAlias : wanted.containsKey(label);
            if (!stillWanted) {
                unregister(label);
                changed = true;
            }
        }
        for (Feeling feeling : wanted.values()) {
            if (!ours.containsKey(feeling.name())) {
                final String description = feeling.description() != null
                        ? feeling.description() : "A custom feeling added via the Feelings folder.";
                if (register(new DynamicCommand(feeling.name(), description, "/" + feeling.name() + " <player>",
                        feelingExecutor, null))) {
                    addPermission(feeling.name());
                    added.add(feeling.name());
                    changed = true;
                }
            }
        }
        if (cfAlias && !ours.containsKey(CF_ALIAS) && cfCommand != null
                && register(new DynamicCommand(CF_ALIAS, "ChatFeelings primary (alias) command", "/cf", cfExecutor, cfCommand))) {
            added.add(CF_ALIAS);
            changed = true;
        }

        if (changed && notifyPlayers) {
            reapplyServerAliases(added);
            refreshPlayers();
        }
    }

    /**
     * Reread/Build the Alias Map
     */
    private void reapplyServerAliases(Collection<String> added) {
        if (added.isEmpty() || !(commandMap instanceof SimpleCommandMap simple)) {
            return;
        }
        try {
            for (String[] targets : Bukkit.getCommandAliases().values()) {
                for (String target : targets) {
                    String label = target.split(" ", 2)[0].toLowerCase(Locale.ROOT);
                    if (label.startsWith(fallbackPrefix + ":")) {
                        label = label.substring(fallbackPrefix.length() + 1);
                    }
                    if (added.contains(label)) {
                        simple.registerServerAliases();
                        return;
                    }
                }
            }
        } catch (RuntimeException err) {
            Log.debug("Couldn't rebuild the commands.yml aliases: " + err);
        }
    }

    private boolean register(DynamicCommand command) {
        final String label = command.getName();
        final Command existing = commandMap.getCommand(label);
        if (existing != null && !isOurs(existing)) {
            final String owner = existing instanceof PluginIdentifiableCommand pic ? pic.getPlugin().getName() : "the server";
            Log.warn("Couldn't register /" + label + ": that command already belongs to " + owner + ". "
                    + (label.equals(CF_ALIAS) ? "Use /chatfeelings instead." : "Rename Feelings/" + label + ".yml to use it."));
            return false;
        }
        if (existing != null) {
            removeFromMap(label, existing); // left over from an earlier load of ChatFeelings (ie: a plugin reload)
        }
        commandMap.register(fallbackPrefix, command);
        ours.put(label, command);
        addHelpTopic(command);
        Log.debug("Registered /" + label);
        return true;
    }

    /** True for commands registered by any load of ChatFeelings, including a previous one before a reload. */
    private boolean isOurs(Command command) {
        if (ours.containsValue(command)) {
            return true;
        }
        return command instanceof PluginIdentifiableCommand pic && pic.getPlugin().getName().equals(plugin.getName())
                && !(command instanceof org.bukkit.command.PluginCommand);
    }

    private void unregister(String label) {
        final Command command = ours.remove(label);
        if (command == null) {
            return;
        }
        removeFromMap(label, command);
        command.unregister(commandMap);
        removeStaleHelpTopic(label);
        removePermission(label);
        Log.debug("Unregistered /" + label);
    }

    private void addHelpTopic(Command command) {
        try {
            final HelpMap help = Bukkit.getHelpMap();
            final List<String> ignored = help.getIgnoredPlugins();
            if (ignored.contains("All") || ignored.contains(plugin.getName())) {
                return;
            }
            removeStaleHelpTopic(command.getName());
            if (help.getHelpTopic("/" + command.getName()) == null) {
                help.addTopic(new GenericCommandHelpTopic(command));
            }
        } catch (RuntimeException err) {
            Log.debug("Couldn't add a /help entry for /" + command.getName() + ": " + err);
        }
    }

    private void removeStaleHelpTopic(String label) {
        try {
            final HelpMap help = Bukkit.getHelpMap();
            final HelpTopic existing = help.getHelpTopic("/" + label);
            if (existing instanceof GenericCommandHelpTopic && !existing.canSee(Bukkit.getConsoleSender())) {
                help.getHelpTopics().remove(existing);
            }
        } catch (RuntimeException err) {
            Log.debug("Couldn't remove the old /help entry for /" + label + ": " + err);
        }
    }

    private void removeFromMap(String label, Command command) {
        if (knownCommands != null) {
            knownCommands.remove(label, command);
            knownCommands.remove(fallbackPrefix + ":" + label, command);
        }
    }

    /** Removes everything this class registered. Called on disable. */
    public void unregisterAll() {
        if (commandMap == null) {
            return;
        }
        for (String label : List.copyOf(ours.keySet())) {
            unregister(label);
        }
    }

    private void addPermission(String feeling) {
        final PluginManager pm = Bukkit.getPluginManager();
        final String node = "chatfeelings." + feeling;
        if (pm.getPermission(node) != null) {
            return;
        }
        pm.addPermission(new Permission(node, "Allows using the custom /" + feeling + " feeling.", PermissionDefault.OP));
        addedPermissions.add(node);
        final Permission all = pm.getPermission("chatfeelings.all");
        if (all != null) {
            all.getChildren().put(node, true);
            all.recalculatePermissibles();
        }
    }

    private void removePermission(String feeling) {
        final PluginManager pm = Bukkit.getPluginManager();
        final String node = "chatfeelings." + feeling;
        if (!addedPermissions.remove(node)) {
            return;
        }
        final Permission all = pm.getPermission("chatfeelings.all");
        if (all != null && all.getChildren().remove(node) != null) {
            all.recalculatePermissibles();
        }
        pm.removePermission(node);
    }

    private void refreshPlayers() {
        try {
            Bukkit.getServer().getClass().getMethod("syncCommands").invoke(Bukkit.getServer());
        } catch (ReflectiveOperationException | RuntimeException err) {
            Log.debug("Couldn't re-sync the server's command tree: " + err);
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            scheduler.entity(player, player::updateCommands);
        }
    }
}
