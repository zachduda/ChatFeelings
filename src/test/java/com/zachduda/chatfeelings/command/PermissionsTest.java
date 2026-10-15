package com.zachduda.chatfeelings.command;

import com.zachduda.chatfeelings.config.Settings;
import com.zachduda.chatfeelings.feeling.FeelingRegistry;
import com.zachduda.chatfeelings.feeling.SoundResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.lang.reflect.Proxy;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermissionsTest {
    @TempDir
    File dir;

    private Permissions permissions(boolean useFeelingPermissions) {
        final YamlConfiguration c = new YamlConfiguration();
        c.set("General.Use-Feeling-Permissions", useFeelingPermissions);
        final Settings settings = Settings.from(c);
        return new Permissions(() -> settings, new FeelingRegistry(dir, new SoundResolver()));
    }

    /** A sender that only answers isOp() and hasPermission(String). */
    private static <T extends CommandSender> T sender(Class<T> type, boolean op, String... granted) {
        final Set<String> nodes = Set.of(granted);
        return type.cast(Proxy.newProxyInstance(PermissionsTest.class.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> switch (method.getName()) {
                    case "isOp" -> op;
                    case "hasPermission" -> args[0] instanceof String node && nodes.contains(node);
                    case "toString" -> type.getSimpleName();
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(method.getName());
                }));
    }

    private static Player player(String... granted) {
        return sender(Player.class, false, granted);
    }

    @Test
    void consoleAndOpsCanDoEverything() {
        final Permissions permissions = permissions(true);
        assertTrue(permissions.has(sender(CommandSender.class, false), "chatfeelings.admin", true));
        assertTrue(permissions.has(sender(Player.class, true), "chatfeelings.admin", true));
    }

    /** With Use-Feeling-Permissions off (the default) anyone may hug, but never mute, reload or skip cooldowns. */
    @Test
    void adminNodesAreNeverGivenOutByDefault() {
        final Permissions permissions = permissions(false);
        assertTrue(permissions.has(player(), "chatfeelings.hug"));
        assertTrue(permissions.has(player(), "chatfeelings.stats"));
        assertFalse(permissions.has(player(), "chatfeelings.admin", true));
        assertFalse(permissions.has(player(), "chatfeelings.mute", true));
        assertFalse(permissions.has(player(), "chatfeelings.bypasscooldowns", true));
        assertTrue(permissions.has(player("chatfeelings.mute"), "chatfeelings.mute", true));
    }

    @Test
    void feelingPermissionsCanBeRequired() {
        final Permissions permissions = permissions(true);
        assertFalse(permissions.has(player(), "chatfeelings.hug"));
        assertTrue(permissions.has(player("chatfeelings.hug"), "chatfeelings.hug"));
        assertFalse(permissions.has(player("chatfeelings.all"), "chatfeelings.stats"),
                "chatfeelings.all only covers feelings");
    }
}
