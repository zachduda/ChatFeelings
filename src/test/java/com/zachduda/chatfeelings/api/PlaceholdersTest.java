package com.zachduda.chatfeelings.api;

import com.zachduda.chatfeelings.feeling.Feeling;
import com.zachduda.chatfeelings.feeling.FeelingSound;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PlaceholdersTest {
    private static Feeling feeling(String name) {
        return new Feeling(name, name.substring(0, 1).toUpperCase() + name.substring(1), true, true, "", "", "",
                null, FeelingSound.NONE, FeelingSound.NONE, null, false, name + "s");
    }

    private static final Map<String, Feeling> FEELINGS = Map.of(
            "hug", feeling("hug"), "punch", feeling("punch"), "kiss", feeling("kiss"), "sus", feeling("sus"));
    private static final Function<String, Feeling> BY_NAME = FEELINGS::get;

    private static String name(String id) {
        final Feeling f = Placeholders.feelingFor(id, BY_NAME);
        return f == null ? null : f.name();
    }

    @Test
    void acceptsTheNameAndItsPlurals() {
        assertEquals("hug", name("total_hug"));
        assertEquals("hug", name("total_hugs"));
        assertEquals("punch", name("total_punches"));
        assertEquals("kiss", name("total_kisses"));
        assertEquals("sus", name("total_sus"), "a name ending in s is matched as-is first");
    }

    @Test
    void ignoresAnythingElse() {
        assertNull(name("total_"));
        assertNull(name("total_nope"));
        assertNull(name("hug"));
        assertNull(name("sent_hug"));
    }
}
