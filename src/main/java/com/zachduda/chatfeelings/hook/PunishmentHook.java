package com.zachduda.chatfeelings.hook;

import java.util.UUID;

/** A punishment plugin (or vanilla's ban list) ChatFeelings checks before letting a player use feelings. */
public interface PunishmentHook {
    /** Shown in /cf mutelist and logs, ie: "LiteBans". */
    String name();

    /** messages.yml key shown when trying to /cf unmute someone muted through this plugin, or null. */
    String mutedMessageKey();

    /** @param ip may be null */
    boolean isMuted(UUID uuid, String ip);

    /** @param ip may be null */
    boolean isBanned(UUID uuid, String ip);
}
