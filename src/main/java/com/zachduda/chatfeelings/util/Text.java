package com.zachduda.chatfeelings.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Colour-code handling for messages: legacy {@code &} codes plus {@code &#rrggbb} hex colours. */
public final class Text {
    /** Accepts &#rrggbb, #rrggbb and &xrrggbb, as earlier versions did. */
    private static final Pattern HEX = Pattern.compile("(?i)(?:&#|#|&x)([0-9a-f]{6})");
    private static final Pattern SECTION_CODE = Pattern.compile("(?i)§[0-9a-fk-orx]");
    private static final String CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";

    private Text() {
    }

    /** Translates colour codes. A null message becomes an empty string rather than throwing. */
    public static String color(String msg) {
        if (msg == null || msg.isEmpty()) {
            return "";
        }
        Matcher matcher = HEX.matcher(msg);
        StringBuilder sb = new StringBuilder(msg.length() + 16);
        while (matcher.find()) {
            StringBuilder replacement = new StringBuilder("&x");
            for (char c : matcher.group(1).toCharArray()) {
                replacement.append('&').append(c);
            }
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);
        return translateAmpersands(sb.toString());
    }

    /** Removes every colour/format code (legacy, hex and already-translated ones). */
    public static String strip(String msg) {
        if (msg == null || msg.isEmpty()) {
            return "";
        }
        return SECTION_CODE.matcher(color(msg)).replaceAll("");
    }

    /** Same behaviour as Bukkit's ChatColor.translateAlternateColorCodes('&', ...), without the deprecated class. */
    static String translateAmpersands(String text) {
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length - 1; i++) {
            if (chars[i] == '&' && CODES.indexOf(chars[i + 1]) > -1) {
                chars[i] = '§';
                chars[i + 1] = Character.toLowerCase(chars[i + 1]);
            }
        }
        return new String(chars);
    }

    /** "hug" -> "Hug", "welcome back" -> "Welcome Back". Used for stat keys and event feeling names. */
    public static String capitalize(String string) {
        char[] chars = string.toLowerCase(Locale.ROOT).toCharArray();
        boolean found = false;
        for (int i = 0; i < chars.length; i++) {
            if (!found && Character.isLetter(chars[i])) {
                chars[i] = Character.toUpperCase(chars[i]);
                found = true;
            } else if (Character.isWhitespace(chars[i]) || chars[i] == '.' || chars[i] == '\'') {
                found = false;
            }
        }
        return String.valueOf(chars);
    }
}
