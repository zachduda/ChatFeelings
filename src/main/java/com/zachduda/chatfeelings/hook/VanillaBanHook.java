package com.zachduda.chatfeelings.hook;

import org.bukkit.Bukkit;

import java.util.UUID;

/** The server's own ban list. Only used for data cleanup; vanilla has no mutes. */
final class VanillaBanHook extends HookSupport {
    @Override
    public String name() {
        return "Vanilla";
    }

    @Override
    public String mutedMessageKey() {
        return null;
    }

    @Override
    public boolean isMuted(UUID uuid, String ip) {
        return false;
    }

    @Override
    public boolean isBanned(UUID uuid, String ip) {
        return safely("ban", () -> Bukkit.getOfflinePlayer(uuid).isBanned());
    }
}
