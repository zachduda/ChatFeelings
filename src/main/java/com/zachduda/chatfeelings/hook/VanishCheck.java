package com.zachduda.chatfeelings.hook;

import com.zachduda.chatfeelings.config.Settings;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffectType;

import java.util.function.Supplier;

/** Treats vanished players as offline, so feelings can't target (and reveal) them. */
public final class VanishCheck {
    private final Supplier<Settings> settings;
    private final Punishments punishments;

    public VanishCheck(Supplier<Settings> settings, Punishments punishments) {
        this.settings = settings;
        this.punishments = punishments;
    }

    /**
     * Whether {@code target} is vanished and should be treated as offline. Only real vanish signals count
     * (Essentials, the "vanished" metadata vanish plugins set, optionally invisibility): players merely hidden
     * from someone, ie: by a lobby's "hide players" item, can still receive feelings. Reads the target's
     * metadata and potion effects, so call it on the target's thread.
     */
    @SuppressWarnings("deprecation") // the "vanished" metadata is what vanish plugins still set
    public boolean isVanished(Player target) {
        final Settings s = settings.get();
        if (!s.vanishCheck()) {
            return false;
        }
        if (punishments.isVanishedInEssentials(target.getName())) {
            return true;
        }
        for (MetadataValue meta : target.getMetadata("vanished")) {
            if (meta.asBoolean()) {
                return true;
            }
        }
        return s.vanishUseLegacy() && target.hasPotionEffect(PotionEffectType.INVISIBILITY);
    }

    /** Cheap check for tab-completion: hide players the viewer can't see. */
    public static boolean visibleTo(CommandSender viewer, Player player) {
        return !(viewer instanceof Player p) || p.canSee(player);
    }
}
