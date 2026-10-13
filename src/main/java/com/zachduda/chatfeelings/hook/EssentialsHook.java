package com.zachduda.chatfeelings.hook;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;

/** Only loaded when Essentials is installed. */
final class EssentialsHook extends HookSupport {
    private final Essentials essentials;

    EssentialsHook(Plugin plugin) {
        this.essentials = (Essentials) plugin;
    }

    @Override
    public String name() {
        return "Essentials";
    }

    @Override
    public String mutedMessageKey() {
        return "Player-Muted-Via-Essentials";
    }

    @Override
    public boolean isMuted(UUID uuid, String ip) {
        return safely("mute", () -> {
            final User user = essentials.getUser(uuid);
            return user != null && user.isMuted();
        });
    }

    @Override
    public boolean isBanned(UUID uuid, String ip) {
        return safely("ban", () -> {
            final User user = essentials.getUser(uuid);
            return user != null && user.getBase() != null && user.getBase().isBanned();
        });
    }

    boolean isVanished(String playerName) {
        return safely("vanish", () -> {
            final List<String> vanished = essentials.getVanishedPlayers();
            return vanished != null && vanished.contains(playerName);
        });
    }
}
