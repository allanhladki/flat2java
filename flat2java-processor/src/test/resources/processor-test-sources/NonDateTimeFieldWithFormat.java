/* This class is used for annotation processor tests.
 * It uses a non-date/time field with a date/time format to verify that
 * the processor ignores date/time format validation for non-date/time fields.
 */
package test;

import io.github.allanhladki.flat2java.annotation.FlatField;

public class NonDateTimeFieldWithFormat {

    // This field should compile successfully even with date/time format
    // because it's not a date/time type
    @FlatField(length = 10, dateTimeFormat = "yyyyMMdd")
    private String stringField;
} 