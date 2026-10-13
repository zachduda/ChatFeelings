package com.zachduda.chatfeelings.feeling;

import org.bukkit.Sound;

/**
 * One of a feeling's two sounds. {@code sound} is null when the sound is turned off ("None") or the
 * configured name couldn't be resolved on this server.
 */
public record FeelingSound(String name, Sound sound, float volume, float pitch) {
    public static final FeelingSound NONE = new FeelingSound("None", null, 0f, 0f);

    public boolean isPlayable() {
        return sound != null;
    }
}
