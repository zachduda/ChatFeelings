package com.zachduda.chatfeelings.feeling;

/**
 * Everything ChatFeelings needs to send one feeling, parsed once from Feelings/&lt;name&gt;.yml.
 *
 * @param name        lower-case command name, ie: "hug"
 * @param displayName capitalized name ("Hug"), used as the stats key and in API events
 * @param builtin     shipped with ChatFeelings (declared in plugin.yml) rather than added by an admin
 * @param description shown in /feelings when messages.yml has no description for it; may be null
 * @param particle    particle preset (a built-in feeling's name) or null for none
 * @param violent     whether it gives the target a tiny bit of damage (General.Violent-Command-Harm)
 * @param statsLabel  plural label used in /cf stats, ie: "Hugs"
 */
public record Feeling(
        String name,
        String displayName,
        boolean builtin,
        boolean enabled,
        String senderMessage,
        String targetMessage,
        String globalMessage,
        String description,
        FeelingSound sound1,
        FeelingSound sound2,
        String particle,
        boolean violent,
        String statsLabel) {
}
