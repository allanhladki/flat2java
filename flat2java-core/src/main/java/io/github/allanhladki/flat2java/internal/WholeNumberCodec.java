package io.github.allanhladki.flat2java.internal;

import io.github.allanhladki.flat2java.exception.DeserializationException;
import io.github.allanhladki.flat2java.exception.SerializationException;

import java.math.BigInteger;

/** Unsigned integer ({@code int}, {@code long}, {@code BigInteger}), zero-filled on the left. */
final class WholeNumberCodec implements FieldCodec {

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
        BigInteger number =
                value instanceof BigInteger
                        ? (BigInteger) value
                        : BigInteger.valueOf(((Number) value).longValue());
        Numbers.appendUnsigned(label, number, length, out);
    }

    @Override
    public Object decode(String line, int start) {
        String slice = line.substring(start, start + length);
        if (!Text.isDigits(slice)) {
            throw new DeserializationException(
                    Text.at(label, start, length) + ": expected digits only");
        }
        BigInteger number = new BigInteger(slice);
        try {
            switch (kind) {
                case INT:
                    return number.intValueExact();
                case LONG:
                    return number.longValueExact();
                default:
                    return number;
            }
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
