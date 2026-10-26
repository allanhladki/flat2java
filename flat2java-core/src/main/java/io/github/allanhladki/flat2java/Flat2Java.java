package io.github.allanhladki.flat2java;

import io.github.allanhladki.flat2java.internal.Layouts;

import java.util.Objects;

/**
 * Writes objects as fixed-width flat records and reads them back.
 *
 * <pre>{@code
 * Flat2Java flat = Flat2Java.create();
 * String line = flat.write(order);
 * Order copy = flat.read(line, Order.class);
 * }</pre>
 *
 * <p>Instances are immutable and thread-safe. The mapping of each class is validated on first use
 * and cached, so create one instance per set of options and reuse it.
 *
 * <p>Errors are reported with subclasses of {@link
 * io.github.allanhladki.flat2java.exception.Flat2JavaException}: {@link
 * io.github.allanhladki.flat2java.exception.MappingException} when a class has invalid {@code
 * FlatField} declarations, {@link io.github.allanhladki.flat2java.exception.SerializationException}
 * when a value cannot be written and {@link
 * io.github.allanhladki.flat2java.exception.DeserializationException} when a record cannot be read.
 */
public final class Flat2Java {

    private final Flat2JavaOptions options;
    private final Layouts layouts;

    private Flat2Java(Flat2JavaOptions options) {
        this.options = options;
        this.layouts = new Layouts(options);
    }

    /**
     * Creates an instance with {@link Flat2JavaOptions#defaults()}.
     *
     * @return a new instance
     */
    public static Flat2Java create() {
        return new Flat2Java(Flat2JavaOptions.defaults());
    }

    /**
     * Creates an instance with the given options.
     *
     * @param options the options
     * @return a new instance
     */
    public static Flat2Java create(Flat2JavaOptions options) {
        return new Flat2Java(Objects.requireNonNull(options, "options"));
    }

    /**
     * Writes {@code record} as a flat record, including the fields of its superclasses.
     *
     * @param record the object to write
     * @return the record, exactly {@link #recordLength(Class)} characters long
     * @throws io.github.allanhladki.flat2java.exception.MappingException if the class cannot be
     *     mapped
     * @throws io.github.allanhladki.flat2java.exception.SerializationException if a value cannot be
     *     written
     */
    public String write(Object record) {
        Objects.requireNonNull(record, "record");
        return layouts.of(record.getClass()).write(record);
    }

    /**
     * Reads a flat record into a new instance of {@code type}.
     *
     * @param line the record; its length must be {@link #recordLength(Class)}
     * @param type the class to create, which needs a no-arg constructor unless it is a record
     * @param <T> the type to create
     * @return the new instance
     * @throws io.github.allanhladki.flat2java.exception.MappingException if the class cannot be
     *     mapped
     * @throws io.github.allanhladki.flat2java.exception.DeserializationException if the record
     *     cannot be read
     */
    public <T> T read(String line, Class<T> type) {
        Objects.requireNonNull(line, "line");
        Objects.requireNonNull(type, "type");
        return type.cast(layouts.of(type).read(line));
    }

    /**
     * Returns the length of the records of {@code type}.
     *
     * @param type the mapped class
     * @return the record length in characters
     * @throws io.github.allanhladki.flat2java.exception.MappingException if the class cannot be
     *     mapped
     */
    public int recordLength(Class<?> type) {
        return layouts.of(Objects.requireNonNull(type, "type")).length();
    }

    /**
     * Returns the options of this instance.
     *
     * @return the options
     */
    public Flat2JavaOptions options() {
        return options;
    }
}
