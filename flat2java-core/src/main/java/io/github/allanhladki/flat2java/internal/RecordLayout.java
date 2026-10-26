package io.github.allanhladki.flat2java.internal;

import io.github.allanhladki.flat2java.exception.DeserializationException;
import io.github.allanhladki.flat2java.exception.SerializationException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Validated, immutable description of how one class maps to a record: its fields in order, their
 * codecs, and the total record length. Built by {@link Layouts}.
 */
public final class RecordLayout {

    private final Class<?> type;
    private final Constructor<?> constructor;
    private final List<Column> columns;
    private final int length;

    RecordLayout(Class<?> type, Constructor<?> constructor, List<Column> columns) {
        this.type = type;
        this.constructor = constructor;
        this.columns = List.copyOf(columns);
        this.length = columns.stream().mapToInt(c -> c.codec.length()).sum();
    }

    /**
     * @return the total record length in characters
     */
    public int length() {
        return length;
    }

    /**
     * @return the number of mapped fields
     */
    public int fieldCount() {
        return columns.size();
    }

    /**
     * Writes {@code record} as a flat record.
     *
     * @param record an instance of the mapped type
     * @return the record, exactly {@link #length()} characters long
     */
    public String write(Object record) {
        StringBuilder out = new StringBuilder(length);
        encode(record, out);
        return out.toString();
    }

    /**
     * Reads a flat record.
     *
     * @param line the record
     * @return a new instance of the mapped type
     */
    public Object read(String line) {
        if (line.length() != length) {
            throw new DeserializationException(
                    type.getSimpleName()
                            + ": expected a record of "
                            + length
                            + " characters, got "
                            + line.length());
        }
        return decode(line, 0);
    }

    void encode(Object record, StringBuilder out) {
        for (Column column : columns) {
            Object value;
            try {
                value = column.field.get(record);
            } catch (IllegalAccessException e) {
                throw new SerializationException(column.label + ": field is not accessible", e);
            }
            column.codec.encode(value, out);
        }
    }

    Object decode(String line, int start) {
        Object instance = newInstance();
        int offset = start;
        for (Column column : columns) {
            Object value = column.codec.decode(line, offset);
            try {
                column.field.set(instance, value);
            } catch (IllegalAccessException e) {
                throw new DeserializationException(column.label + ": field cannot be assigned", e);
            }
            offset += column.codec.length();
        }
        return instance;
    }

    private Object newInstance() {
        try {
            return constructor.newInstance();
        } catch (InvocationTargetException e) {
            throw new DeserializationException(
                    type.getSimpleName() + ": no-arg constructor threw an exception", e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new DeserializationException(
                    type.getSimpleName() + ": cannot create an instance", e);
        }
    }

    /** One mapped field. */
    static final class Column {
        final Field field;
        final String label;
        final FieldCodec codec;

        Column(Field field, String label, FieldCodec codec) {
            this.field = field;
            this.label = label;
            this.codec = codec;
        }
    }
}
