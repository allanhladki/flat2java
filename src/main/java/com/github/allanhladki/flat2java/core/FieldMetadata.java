package com.github.allanhladki.flat2java.core;

import com.github.allanhladki.flat2java.markers.FlatField;
import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Represents the metadata of a field annotated with {@link FlatField}.
 * This class encapsulates all the information needed to process a field from a
 * flat file.
 */
public class FieldMetadata {
    private final Field field;
    private final String name;
    private final int position;
    private final int length;
    private final int precision;
    private final String dateTimeFormat;
    private final char padding;

    /**
     * Creates a new FieldMetadata instance from a field and its FlatField
     * annotation.
     *
     * @param field      the Java field
     * @param annotation the FlatField annotation
     * @throws NullPointerException if annotation is null
     */
    public FieldMetadata(Field field, FlatField annotation) {
        Objects.requireNonNull(annotation, "FlatField annotation cannot be null");
        this.field = field;
        this.name = annotation.name().isBlank()
            ?  (field != null 
                ? field.getName() 
                : "unnamed") 
            : annotation.name();
        this.position = annotation.position();
        this.length = annotation.length();
        this.precision = annotation.precision();
        this.dateTimeFormat = annotation.dateTimeFormat();
        this.padding = annotation.padding();
    }

    /**
     * Gets the Java field.
     * 
     * @return the field, may be null
     */
    public Field getField() {
        return field;
    }

    /**
     * Gets the field name.
     * 
     * @return the name, never null
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the field position in the flat file.
     * 
     * @return the position
     */
    public int getPosition() {
        return position;
    }

    /**
     * Gets the field length.
     * 
     * @return the length
     */
    public int getLength() {
        return length;
    }

    /**
     * Gets the precision for decimal numbers.
     * 
     * @return the precision
     */
    public int getPrecision() {
        return precision;
    }

    /**
     * Gets the date/time format pattern.
     * 
     * @return the date/time format, may be empty but never null
     */
    public String getDateTimeFormat() {
        return dateTimeFormat;
    }

    /**
     * Gets the padding character.
     * 
     * @return the padding character
     */
    public char getPadding() {
        return padding;
    }

    /**
     * Gets the field type.
     * 
     * @return the field type, or Object.class if field is null
     */
    public Class<?> getType() {
        return field != null ? field.getType() : Object.class;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldMetadata that = (FieldMetadata) o;
        return position == that.position &&
               length == that.length &&
               precision == that.precision &&
               padding == that.padding &&
               Objects.equals(field, that.field) &&
               Objects.equals(name, that.name) &&
               Objects.equals(dateTimeFormat, that.dateTimeFormat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, name, position, length, precision, dateTimeFormat, padding);
    }

    @Override
    public String toString() {
        return String.format(
                "FieldMetadata{name='%s', position=%d, length=%d, precision=%d, dateTimeFormat='%s', padding='%c'}",
                name, position, length, precision, dateTimeFormat, padding);
    }
}