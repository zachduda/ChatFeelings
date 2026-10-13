package com.zachduda.chatfeelings.hook;

import litebans.api.Database;

import java.util.UUID;

/** Only loaded when LiteBans is installed. Its checks query a database, so call them asynchronously. */
final class LiteBansHook extends HookSupport {
    @Override
    public String name() {
        return "LiteBans";
    }

    @Override
    public String mutedMessageKey() {
        return "Player-Muted-Via-LiteBans";
    }

    @Override
    public boolean isMuted(UUID uuid, String ip) {
        return safely("mute", () -> Database.get().isPlayerMuted(uuid, ip));
    }

    @Override
    public boolean isBanned(UUID uuid, String ip) {
        return safely("ban", () -> Database.get().isPlayerBanned(uuid, ip));
    }
}
