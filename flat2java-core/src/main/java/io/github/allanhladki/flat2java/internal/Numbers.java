package io.github.allanhladki.flat2java.internal;

import io.github.allanhladki.flat2java.exception.SerializationException;

import java.math.BigInteger;

/** Shared encoding of unsigned digits. */
final class Numbers {

    private Numbers() {}

    /** Appends {@code number} zero-filled to {@code length}; fails if negative or too wide. */
    static void appendUnsigned(String label, BigInteger number, int length, StringBuilder out) {
        if (number.signum() < 0) {
            throw new SerializationException(
                    label + ": negative values are not supported by the flat format");
        }
        String digits = number.toString();
        appendDigits(label, digits, length, out);
    }

    /** Appends {@code digits} zero-filled on the left; numbers are never truncated. */
    static void appendDigits(String label, String digits, int length, StringBuilder out) {
        if (digits.length() > length) {
            throw new SerializationException(
                    label
                            + ": value needs "
                            + digits.length()
                            + " characters, but length is "
                            + length
                            + "; numeric values are never truncated");
        }
        Text.appendRepeated(out, '0', length - digits.length());
        out.append(digits);
    }
}
