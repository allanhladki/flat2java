package io.github.allanhladki.flat2java.internal;

/** String helpers shared by the codecs. */
final class Text {

    private Text() {}

    static boolean isAll(String s, char c) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != c) {
                return false;
            }
        }
        return true;
    }

    static boolean isDigits(String s) {
        if (s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
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

    static String trimRight(String s, char c) {
        int end = s.length();
        while (end > 0 && s.charAt(end - 1) == c) {
            end--;
        }
        return s.substring(0, end);
    }

    /** Label plus the one-based column range, for read errors. */
    static String at(String label, int start, int length) {
        return label + " (columns " + (start + 1) + "-" + (start + length) + ")";
    }
}
