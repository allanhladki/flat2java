package io.github.allanhladki.flat2java.internal;

import io.github.allanhladki.flat2java.exception.DeserializationException;
import io.github.allanhladki.flat2java.exception.SerializationException;

import java.math.BigInteger;

/** Unsigned integer ({@code int}, {@code long}, {@code BigInteger}), zero-filled on the left. */
final class WholeNumberCodec implements FieldCodec {

    private static final int MAX_LONG_DIGITS = 18;

    private final String label;
    private final int length;
    private final FieldKind kind;

    WholeNumberCodec(String label, int length, FieldKind kind) {
        this.label = label;
        this.length = length;
        this.kind = kind;
    }

    @Override
    public void encode(Object value, StringBuilder out) {
        if (value == null) {
            throw new SerializationException(label + ": null is not allowed on numeric fields");
        }
        if (value instanceof BigInteger big) {
            Numbers.appendUnsigned(label, big, length, out);
            return;
        }
        long number = ((Number) value).longValue();
        Numbers.checkNotNegative(label, number < 0);
        Numbers.appendDigits(label, Long.toString(number), length, out);
    }

    @Override
    public Object decode(String line, int start) {
        int end = start + length;
        if (!Text.isDigits(line, start, end)) {
            throw new DeserializationException(
                    Text.at(label, start, length) + ": expected digits only");
        }
        if (kind == FieldKind.BIG_INTEGER) {
            return new BigInteger(line.substring(start, end));
        }
        try {
            // 18 digits always fit in a long; wider columns may still hold a small value.
            long number =
                    length <= MAX_LONG_DIGITS
                            ? Long.parseLong(line, start, end, 10)
                            : new BigInteger(line.substring(start, end)).longValueExact();
            if (kind == FieldKind.INT) {
                return Math.toIntExact(number);
            }
            return number;
        } catch (ArithmeticException e) {
            throw new DeserializationException(
                    Text.at(label, start, length)
                            + ": value is out of range for "
                            + (kind == FieldKind.INT ? "int" : "long"),
                    e);
        }
    }

    @Override
    public int length() {
        return length;
    }
}
