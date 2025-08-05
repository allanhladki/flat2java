package com.github.allanhladki.flat2java.markers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark fields that should be mapped from flat file data. This annotation is used to
 * define the length and format of fields in a flat file record.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FlatField {

    /**
     * Default values for annotation attributes.
     */
    final class Defaults {
        /** Default name value (empty string) */
        public static final String NAME = "";
        
        /** Default precision value */
        public static final int PRECISION = 0;
        
        /** Default date/time format value (empty string) */
        public static final String DATE_TIME_FORMAT = "";
        
        /** Default padding character (space) */
        public static final char PADDING = ' ';
        
        /** Private constructor to prevent instantiation */
        private Defaults() {}
    }

    /**
     * The name of the field.
     *
     * @return the name
     */
    String name() default Defaults.NAME;

    /**
     * The length of the field in the flat file record.
     *
     * @return the field length
     */
    int length();

    /**
     * The precision for decimal numbers (number of decimal places). Only used for BigDecimal fields.
     *
     * @return the precision
     */
    int precision() default Defaults.PRECISION;

    /**
     * The date format pattern to use when parsing date fields. Uses the standard Java date format
     * patterns. Example: "yyyyMMdd" for dates like "20240315"
     *
     * @return the date format pattern
     */
    String dateTimeFormat() default Defaults.DATE_TIME_FORMAT;

    /**
     * Optional padding character used in the field.
     *
     * @return the padding character, default is space
     */
    char padding() default Defaults.PADDING;
}
