package io.github.allanhladki.flat2java.internal;

import io.github.allanhladki.flat2java.exception.DeserializationException;
import io.github.allanhladki.flat2java.exception.SerializationException;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/** {@code LocalDate}/{@code LocalDateTime} written with a fixed-width pattern. */
final class TemporalCodec implements FieldCodec {

    private final String label;
    private final int length;
    private final FieldKind kind;
    private final String pattern;
    private final DateTimeFormatter formatter;

    TemporalCodec(String label, int length, FieldKind kind, String pattern) {
        this.label = label;
        this.length = length;
        this.kind = kind;
        this.pattern = pattern;
        this.formatter = MappingRules.formatter(pattern);
    }

    @Override
    public void encode(Object value, StringBuilder out) {
        if (value == null) {
            throw new SerializationException(label + ": null is not allowed on date/time fields");
        }
        String text;
        try {
            text = formatter.format((TemporalAccessor) value);
        } catch (DateTimeException e) {
            throw new SerializationException(
                    label + ": value cannot be formatted with '" + pattern + "'", e);
        }
        if (text.length() != length) {
            throw new SerializationException(
                    label
                            + ": formatted value has "
                            + text.length()
                            + " characters, but length is "
                            + length);
        }
        out.append(text);
    }

    @Override
    public Object decode(String line, int start) {
        String slice = line.substring(start, start + length);
        try {
            return kind == FieldKind.LOCAL_DATE
                    ? LocalDate.parse(slice, formatter)
                    : LocalDateTime.parse(slice, formatter);
        } catch (DateTimeException e) {
            throw new DeserializationException(
                    Text.at(label, start, length)
                            + ": not a valid date/time for pattern '"
                            + pattern
                            + "'",
                    e);
        }
    }

    @Override
    public int length() {
        return length;
    }
}
