package com.zachduda.chatfeelings.command;

import com.zachduda.chatfeelings.config.Messages;
import com.zachduda.chatfeelings.config.Settings;
import com.zachduda.chatfeelings.feeling.Feeling;
import com.zachduda.chatfeelings.feeling.FeelingRegistry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** /feelings [page]: lists the enabled feelings, marking the ones the sender can't use. */
public final class FeelingsListCommand implements TabExecutor {
    private final Supplier<Settings> settings;
    private final FeelingRegistry registry;
    private final Permissions permissions;
    private final Feedback feedback;

    public FeelingsListCommand(Supplier<Settings> settings, FeelingRegistry registry, Permissions permissions, Feedback feedback) {
        this.settings = settings;
        this.registry = registry;
        this.permissions = permissions;
        this.feedback = feedback;
    }

    private List<Feeling> enabledFeelings() {
        return registry.all().stream().filter(Feeling::enabled).toList();
    }

    private int totalPages(int feelingCount) {
        return Math.max(1, (int) Math.ceil((double) feelingCount / settings.get().helpPageLength()));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        final Messages m = feedback.messages();
        int page = 1;
        if (args.length >= 1) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException notANumber) {
                feedback.bass(sender);
                feedback.message(sender, "Page-Not-Found");
                return true;
            }
        }
        page = Math.max(1, page);

        final List<Feeling> enabled = enabledFeelings();
        if (enabled.isEmpty()) {
            feedback.bass(sender);
            feedback.prefixed(sender, "&7There are no feelings currently enabled.");
            return true;
        }
        final int pageLength = settings.get().helpPageLength();
        final int totalPages = totalPages(enabled.size());
        if (page > totalPages) {
            feedback.bass(sender);
            feedback.message(sender, "Page-Not-Found");
            return true;
        }

        final String playerHint = m.get("Command-List-Player");
        feedback.send(sender, "");
        feedback.send(sender, m.get("Feelings-Help") + "                            " + m.get("Feelings-Help-Page")
                .replace("%page%", Integer.toString(page)).replace("%pagemax%", Integer.toString(totalPages)));
        final int start = (page - 1) * pageLength;
        for (int i = start; i < Math.min(start + pageLength, enabled.size()); i++) {
            final Feeling f = enabled.get(i);
            if (permissions.canUse(sender, f)) {
                feedback.send(sender, "&8&l> &f&l/" + f.name() + playerHint + "&7 " + description(m, f));
            } else {
                feedback.send(sender, "&8&l> &#FF8C6B/" + f.name() + playerHint + "&7 " + m.get("Command-List-NoPerm"));
            }
        }
        if (page < totalPages) {
            feedback.send(sender, m.get("Command-List-Page").replace("%page%", Integer.toString(page + 1)));
        }
        feedback.pop(sender);
        feedback.send(sender, "");
        return true;
    }

    /** messages.yml's description first (so admins' edits win), then the feeling file's, then a generic one. */
    private static String description(Messages m, Feeling f) {
        final String fromMessages = m.getIfSet("Command_Descriptions." + f.displayName());
        if (fromMessages != null) {
            return fromMessages;
        }
        return f.description() != null ? f.description() : "&7A custom feeling.";
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        if (args.length != 1) {
            return List.of();
        }
        final List<String> pages = new ArrayList<>();
        for (int i = 1; i <= totalPages(enabledFeelings().size()); i++) {
            pages.add(Integer.toString(i));
        }
        return StringUtil.copyPartialMatches(args[0], pages, new ArrayList<>());
    }
}
