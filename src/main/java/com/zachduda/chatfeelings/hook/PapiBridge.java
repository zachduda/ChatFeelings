package com.zachduda.chatfeelings.hook;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

/** The only class that calls PlaceholderAPI directly, so it's only loaded when PlaceholderAPI is installed. */
final class PapiBridge {
    private PapiBridge() {
    }

    static String apply(Player player, String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }
}
