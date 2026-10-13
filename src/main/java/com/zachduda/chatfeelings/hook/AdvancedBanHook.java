package com.zachduda.chatfeelings.hook;

import me.leoko.advancedban.manager.PunishmentManager;

import java.util.UUID;

/** Only loaded when AdvancedBan is installed. AdvancedBan identifies players by their UUID as a string. */
final class AdvancedBanHook extends HookSupport {
    @Override
    public String name() {
        return "AdvancedBan";
    }

    @Override
    public String mutedMessageKey() {
        return "Player-Muted-Via-AdvancedBan";
    }

    @Override
    public boolean isMuted(UUID uuid, String ip) {
        return safely("mute", () -> PunishmentManager.get().isMuted(uuid.toString()));
    }

    @Override
    public boolean isBanned(UUID uuid, String ip) {
        return safely("ban", () -> PunishmentManager.get().isBanned(uuid.toString()));
    }
}
