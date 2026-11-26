package com.zachduda.chatfeelings.util;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/** UTF-8 YAML loading/saving that reports parse errors instead of silently returning an empty config. */
public final class YamlFiles {
    private YamlFiles() {
    }

    /**
     * @param config  the parsed file (empty if missing or unreadable)
     * @param ok      false if the file exists but couldn't be parsed. Callers must not save over such a file,
     *                or an admin's typo would wipe their customizations.
     * @param existed whether the file was there at all
     */
    public record Loaded(YamlConfiguration config, boolean ok, boolean existed) {
    }

    public static Loaded load(File file) {
        if (!file.exists()) {
            return new Loaded(new YamlConfiguration(), true, false);
        }
        YamlConfiguration yml = new YamlConfiguration();
        try {
            yml.loadFromString(decode(file, Files.readAllBytes(file.toPath())));
            return new Loaded(yml, true, true);
        } catch (IOException | InvalidConfigurationException err) {
            Log.warn("Couldn't read " + file.getName() + ": " + err.getMessage());
            return new Loaded(new YamlConfiguration(), false, true);
        }
    }

    /**
     * Reads the file as UTF-8. A file saved in another encoding (ie: "ANSI" in a Windows editor) still loads, like it
     * does in Bukkit, with the characters that aren't valid UTF-8 replaced. A copy of the original is kept since
     * saving the file would write the replacements.
     */
    private static String decode(File file, byte[] bytes) {
        try {
            return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException notUtf8) {
            final File copy = new File(file.getParentFile(), file.getName() + ".not-utf8.bak");
            try {
                Files.copy(file.toPath(), copy.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException err) {
                Log.debug("Couldn't back up " + file.getName() + ": " + err);
            }
            Log.warn(file.getName() + " isn't saved as UTF-8, so some characters (like accents) were replaced with '?'. "
                    + "Re-save it as UTF-8 to fix them. A copy of the original is at " + copy.getName());
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    /** Sets a file header, falling back to the pre-1.18.1 API on older servers. */
    @SuppressWarnings("deprecation")
    public static void setHeader(FileConfiguration config, List<String> lines) {
        try {
            config.options().setHeader(lines);
        } catch (NoSuchMethodError oldServer) {
            config.options().header(String.join("\n", lines));
        }
    }

    public static boolean save(FileConfiguration yml, File file) {
        final File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            Log.warn("Unable to create the folder " + parent.getPath());
            return false;
        }
        try {
            writeAtomically(yml, file);
            return true;
        } catch (IOException err) {
            Log.warn("Failed to save " + file.getName(), err);
            return false;
        }
    }

    /**
     * Writes to a temporary file beside {@code file} and then moves it into place, so a crash or a full disk
     * mid-save leaves the previous file intact. Bukkit's own save() empties the file before writing it. A symlinked
     * file (ie: a config shared between servers) is written where the link points, so the link itself survives.
     */
    public static void writeAtomically(FileConfiguration yml, File file) throws IOException {
        final Path target = Files.isSymbolicLink(file.toPath()) ? file.toPath().toRealPath() : file.toPath();
        final Path temp = target.resolveSibling(target.getFileName() + ".tmp");
        try {
            Files.writeString(temp, yml.saveToString(), StandardCharsets.UTF_8);
            try {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException notSupported) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }
}
