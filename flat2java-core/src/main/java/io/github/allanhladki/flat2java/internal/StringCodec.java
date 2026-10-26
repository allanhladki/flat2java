package io.github.allanhladki.flat2java.internal;

import io.github.allanhladki.flat2java.exception.SerializationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Left-aligned text, filled on the right with the padding character. */
final class StringCodec implements FieldCodec {

    private static final Logger LOG = LoggerFactory.getLogger(StringCodec.class);

    private final String label;
    private final int length;
    private final char padding;
    private final char nullFill;
    private final boolean allowNull;
    private final boolean failOnTruncation;

    StringCodec(
            String label,
            int length,
            char padding,
            char nullFill,
            boolean allowNull,
            boolean failOnTruncation) {
        this.label = label;
        this.length = length;
        this.padding = padding;
        this.nullFill = nullFill;
        this.allowNull = allowNull;
        this.failOnTruncation = failOnTruncation;
    }

    @Override
    public void encode(Object value, StringBuilder out) {
        if (value == null) {
            if (!allowNull) {
                throw new SerializationException(
                        label + ": null is not allowed because allowNullStrings is false");
            }
            Text.appendRepeated(out, nullFill, length);
            return;
        }
        String text = (String) value;
        if (text.length() > length) {
            if (failOnTruncation) {
                throw new SerializationException(
                        label
                                + ": value has "
                                + text.length()
                                + " characters, but length is "
                                + length
                                + " and failOnStringTruncation is true");
            }
            LOG.warn("{}: value truncated from {} to {} characters", label, text.length(), length);
            // Never split a surrogate pair: drop the whole character and pad instead.
            int end = Character.isHighSurrogate(text.charAt(length - 1)) ? length - 1 : length;
            out.append(text, 0, end);
            Text.appendRepeated(out, padding, length - end);
            return;
        }
        out.append(text);
        Text.appendRepeated(out, padding, length - text.length());
    }

    @Override
    public Object decode(String line, int start) {
        int end = start + length;
        boolean onlyNullFill = true;
        for (int i = start; i < end && onlyNullFill; i++) {
            onlyNullFill = line.charAt(i) == nullFill;
        }
        if (onlyNullFill) {
            return "";
        }
        while (end > start && line.charAt(end - 1) == padding) {
            end--;
        }
        return line.substring(start, end);
    }

    @Override
    public int length() {
        return length;
    }
}
