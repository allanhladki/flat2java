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
    private final Instantiator instantiator;
    private final List<Column> columns;
    private final int length;

    RecordLayout(Class<?> type, Instantiator instantiator, List<Column> columns) {
        this.type = type;
        this.instantiator = instantiator;
        this.columns = List.copyOf(columns);
        this.length = columns.stream().mapToInt(c -> c.codec().length()).sum();
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
                value = column.field().get(record);
            } catch (IllegalAccessException e) {
                throw new SerializationException(column.label() + ": field is not accessible", e);
            }
            column.codec().encode(value, out);
        }
    }

    Object decode(String line, int start) {
        Object[] values = new Object[columns.size()];
        int offset = start;
        for (int i = 0; i < values.length; i++) {
            FieldCodec codec = columns.get(i).codec();
            values[i] = codec.decode(line, offset);
            offset += codec.length();
        }
        return instantiator.create(values);
    }

    /** One mapped field. */
    record Column(Field field, String label, FieldCodec codec) {}

    /** Creates an instance of the mapped type from the decoded column values, in column order. */
    interface Instantiator {
        Object create(Object[] values);
    }

    /** A class with a no-arg constructor; the fields are assigned after construction. */
    static final class NoArgInstantiator implements Instantiator {
        private final Class<?> type;
        private final Constructor<?> constructor;
        private final List<Column> columns;

        NoArgInstantiator(Class<?> type, Constructor<?> constructor, List<Column> columns) {
            this.type = type;
            this.constructor = constructor;
            this.columns = List.copyOf(columns);
        }

        @Override
        public Object create(Object[] values) {
            Object instance = invoke(type, constructor, "no-arg constructor");
            for (int i = 0; i < values.length; i++) {
                Column column = columns.get(i);
                try {
                    column.field().set(instance, values[i]);
                } catch (IllegalAccessException e) {
                    throw new DeserializationException(
                            column.label() + ": field cannot be assigned", e);
                }
            }
            return instance;
        }
    }

    /**
     * A Java record, created through its canonical constructor. Components without
     * {@code @FlatField} receive {@code null}, {@code 0} or {@code false}.
     */
    static final class CanonicalInstantiator implements Instantiator {
        private final Class<?> type;
        private final Constructor<?> constructor;
        private final int[] argumentOfColumn;
        private final Object[] defaults;

        /**
         * @param argumentOfColumn for each column, the index of its record component
         * @param defaults the argument values of the components that are not mapped
         */
        CanonicalInstantiator(
                Class<?> type,
                Constructor<?> constructor,
                int[] argumentOfColumn,
                Object[] defaults) {
            this.type = type;
            this.constructor = constructor;
            this.argumentOfColumn = argumentOfColumn;
            this.defaults = defaults;
        }

        @Override
        public Object create(Object[] values) {
            Object[] arguments = defaults.clone();
            for (int i = 0; i < values.length; i++) {
                arguments[argumentOfColumn[i]] = values[i];
            }
            return invoke(type, constructor, "canonical constructor", arguments);
        }
    }

    private static Object invoke(
            Class<?> type, Constructor<?> constructor, String description, Object... arguments) {
        try {
            return constructor.newInstance(arguments);
        } catch (InvocationTargetException e) {
            throw new DeserializationException(
                    type.getSimpleName() + ": " + description + " threw an exception",
                    e.getCause());
        } catch (ReflectiveOperationException | IllegalArgumentException e) {
            throw new DeserializationException(
                    type.getSimpleName() + ": cannot create an instance", e);
        }
    }
}
