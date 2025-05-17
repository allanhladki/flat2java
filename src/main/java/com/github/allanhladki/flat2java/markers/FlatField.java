package com.github.allanhladki.flat2java.markers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark fields that should be mapped from flat file data.
 * This annotation is used to define the position, length, and format of fields in a flat file record.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FlatField {

    /**
     * The name of the field.
     * @return the name
     */
    String name() default "";

    /**
     * The position of the field in the flat file record (1-based).
     * @return the position
     */
    int position();

    /**
     * The length of the field in the flat file record.
     * @return the field length
     */
    int length() default 0;

    /**
     * The precision for decimal numbers (number of decimal places).
     * Only used for BigDecimal fields.
     * @return the precision
     */
    int precision() default 0;

    /**
     * The date format pattern to use when parsing date time fields.
     * Uses the standard Java date format patterns.
     * Example: "yyyyMMdd" for dates like "20240315"
     * @return the date format pattern
     */
    String dateTimeFormat() default "";

    /**
     * Optional padding character used in the field.
     * @return the padding character, default is space
     */
    char padding() default ' ';

} 