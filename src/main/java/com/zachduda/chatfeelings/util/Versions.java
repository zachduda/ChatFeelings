package com.zachduda.chatfeelings.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Compares plugin version strings such as "5.0.0", "v5.0.1" or "4.15.0b95". */
public final class Versions {
    private static final Pattern CORE = Pattern.compile("^[vV]?(\\d+(?:\\.\\d+)*)(.*)$");

    private Versions() {
    }

    /**
     * Negative if {@code a} is older than {@code b}, positive if newer, 0 if equal. The numeric part is
     * compared first; when it's equal, a version with a suffix (a beta/pre-release such as "5.0.0b3") is
     * older than the plain release. Unparseable versions compare as equal so they never trigger an update.
     */
    public static int compare(String a, String b) {
        Matcher ma = CORE.matcher(a == null ? "" : a.trim());
        Matcher mb = CORE.matcher(b == null ? "" : b.trim());
        if (!ma.matches() || !mb.matches()) {
            return 0;
        }
        String[] pa = ma.group(1).split("\\.");
        String[] pb = mb.group(1).split("\\.");
        for (int i = 0; i < Math.max(pa.length, pb.length); i++) {
            long va = i < pa.length ? parse(pa[i]) : 0;
            long vb = i < pb.length ? parse(pb[i]) : 0;
            if (va != vb) {
                return Long.compare(va, vb);
            }
        }
        boolean aPre = !ma.group(2).isBlank();
        boolean bPre = !mb.group(2).isBlank();
        if (aPre == bPre) {
            return 0;
        }
        return aPre ? -1 : 1;
    }

    private static long parse(String part) {
        try {
            return Long.parseLong(part);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
