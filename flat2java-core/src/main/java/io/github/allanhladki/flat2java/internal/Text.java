package io.github.allanhladki.flat2java.internal;

/** String helpers shared by the codecs. */
final class Text {

    private Text() {}

    static boolean isDigits(String s) {
        return isDigits(s, 0, s.length());
    }

    /** Whether {@code s[start, end)} is a non-empty run of ASCII digits. */
    static boolean isDigits(String s, int start, int end) {
        if (start >= end) {
            return false;
        }
        for (int i = start; i < end; i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    static void appendRepeated(StringBuilder out, char c, int count) {
        for (int i = 0; i < count; i++) {
            out.append(c);
        }
    }

    /** Label plus the one-based column range, for read errors. */
    static String at(String label, int start, int length) {
        return label + " (columns " + (start + 1) + "-" + (start + length) + ")";
    }
}
