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
            out.append(text, 0, length);
            return;
        }
        out.append(text);
        Text.appendRepeated(out, padding, length - text.length());
    }

    @Override
    public Object decode(String line, int start) {
        String slice = line.substring(start, start + length);
        if (Text.isAll(slice, nullFill)) {
            return "";
        }
        return Text.trimRight(slice, padding);
    }

    @Override
    public int length() {
        return length;
    }
}
