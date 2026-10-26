package io.github.allanhladki.flat2java.internal;

import io.github.allanhladki.flat2java.exception.DeserializationException;
import io.github.allanhladki.flat2java.exception.SerializationException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * Unsigned {@code BigDecimal} with a fixed number of decimal places, zero-filled on the left. The
 * decimal point is implied ({@code 12.34} with precision 2 is {@code 1234}) unless an explicit
 * separator is configured ({@code 12.34}).
 */
final class DecimalCodec implements FieldCodec {

    private final String label;
    private final int length;
    private final int precision;
    private final RoundingMode roundingMode;
    private final boolean useSeparator;
    private final char separator;

    DecimalCodec(
            String label,
            int length,
            int precision,
            RoundingMode roundingMode,
            boolean useDecimalSeparator,
            char separator) {
        this.label = label;
        this.length = length;
        this.precision = precision;
        this.roundingMode = roundingMode;
        this.useSeparator = useDecimalSeparator && precision > 0;
        this.separator = separator;
        if (useSeparator) {
            MappingRules.checkDecimalSeparator(label, length, precision);
        }
    }

    @Override
    public void encode(Object value, StringBuilder out) {
        if (value == null) {
            throw new SerializationException(label + ": null is not allowed on numeric fields");
        }
        BigDecimal scaled;
        try {
            scaled = ((BigDecimal) value).setScale(precision, roundingMode);
        } catch (ArithmeticException e) {
            throw new SerializationException(
                    label
                            + ": value has more than "
                            + precision
                            + " decimal places; set a roundingMode other than UNNECESSARY to"
                            + " round it",
                    e);
        }
        if (scaled.signum() < 0) {
            throw new SerializationException(
                    label + ": negative values are not supported by the flat format");
        }
        String digits = scaled.unscaledValue().toString();
        if (!useSeparator) {
            Numbers.appendDigits(label, digits, length, out);
            return;
        }
        if (digits.length() <= precision) {
            digits = "0".repeat(precision - digits.length() + 1) + digits;
        }
        int split = digits.length() - precision;
        String text = digits.substring(0, split) + separator + digits.substring(split);
        Numbers.appendDigits(label, text, length, out);
    }

    @Override
    public Object decode(String line, int start) {
        String slice = line.substring(start, start + length);
        String digits;
        if (useSeparator) {
            int split = length - precision - 1;
            String integerPart = slice.substring(0, split);
            String fraction = slice.substring(split + 1);
            if (slice.charAt(split) != separator
                    || !Text.isDigits(integerPart)
                    || !Text.isDigits(fraction)) {
                throw new DeserializationException(
                        Text.at(label, start, length)
                                + ": expected digits with '"
                                + separator
                                + "' followed by "
                                + precision
                                + " decimal places");
            }
            digits = integerPart + fraction;
        } else {
            if (!Text.isDigits(slice)) {
                throw new DeserializationException(
                        Text.at(label, start, length) + ": expected digits only");
            }
            digits = slice;
        }
        return new BigDecimal(new BigInteger(digits), precision);
    }

    @Override
    public int length() {
        return length;
    }
}
