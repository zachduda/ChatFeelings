package com.zachduda.chatfeelings.hook;

import com.zachduda.chatfeelings.util.Log;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * The punishment plugins found on startup. Mutes are checked in the order AdvancedBan, LiteBans, Essentials;
 * bans additionally check the vanilla ban list last. Some checks hit a database, so call these async.
 */
public final class Punishments {
    private final List<PunishmentHook> hooks;
    private final EssentialsHook essentials;
    private final boolean liteBans;
    private final boolean advancedBan;

    private Punishments(List<PunishmentHook> hooks, EssentialsHook essentials, boolean liteBans, boolean advancedBan) {
        this.hooks = Collections.unmodifiableList(hooks);
        this.essentials = essentials;
        this.liteBans = liteBans;
        this.advancedBan = advancedBan;
    }

    public static Punishments detect(PluginManager pm) {
        final List<PunishmentHook> hooks = new ArrayList<>();
        boolean liteBans = false;
        boolean advancedBan = false;
        EssentialsHook essentials = null;

        if (enabled(pm, "AdvancedBan")) {
            advancedBan = add(hooks, "AdvancedBan", AdvancedBanHook::new);
        }
        if (enabled(pm, "LiteBans")) {
            liteBans = add(hooks, "LiteBans", LiteBansHook::new);
        }
        final Plugin ess = pm.getPlugin("Essentials");
        if (ess != null && ess.isEnabled()) {
            try {
                essentials = new EssentialsHook(ess);
                hooks.add(essentials);
                Log.info("Hooking into Essentials...");
            } catch (LinkageError | ClassCastException err) {
                Log.warn("Found Essentials, but couldn't hook into it", err);
            }
        } else {
            Log.debug("Skipping hooks for Essentials (Not Found)");
        }
        hooks.add(new VanillaBanHook());
        return new Punishments(hooks, essentials, liteBans, advancedBan);
    }

    private static boolean enabled(PluginManager pm, String name) {
        final boolean found = pm.isPluginEnabled(name);
        if (!found) {
            Log.debug("Skipping hooks for " + name + " (Not Found)");
        }
        return found;
    }

    private interface HookFactory {
        PunishmentHook create();
    }

    private static boolean add(List<PunishmentHook> hooks, String name, HookFactory factory) {
        try {
            hooks.add(factory.create());
            Log.info("Hooking into " + name + "...");
            return true;
        } catch (LinkageError err) {
            Log.warn("Found " + name + ", but couldn't hook into it", err);
            return false;
        }
    }

    /** The first plugin that has the player muted, or null. Vanilla has no mutes. */
    public PunishmentHook findMute(UUID uuid, String ip) {
        for (PunishmentHook hook : hooks) {
            if (hook.isMuted(uuid, ip)) {
                return hook;
            }
        }
        return null;
    }

    /** The first plugin (or the vanilla ban list) that has the player banned, or null. */
    public PunishmentHook findBan(UUID uuid, String ip) {
        for (PunishmentHook hook : hooks) {
            if (hook.isBanned(uuid, ip)) {
                return hook;
            }
        }
        return null;
    }

    public boolean hasEssentials() {
        return essentials != null;
    }

    public boolean hasLiteBans() {
        return liteBans;
    }

    public boolean hasAdvancedBan() {
        return advancedBan;
    }

    /** True if Essentials lists the player as vanished. */
    public boolean isVanishedInEssentials(String playerName) {
        return essentials != null && essentials.isVanished(playerName);
    }
}
