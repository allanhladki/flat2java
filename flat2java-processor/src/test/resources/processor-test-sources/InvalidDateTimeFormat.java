/* This class is used for annotation processor tests.
 * It intentionally uses an invalid date/time format with @FlatField annotation
 * to verify that the processor produces a compile-time error.
 */
package test;

import io.github.allanhladki.flat2java.annotation.FlatField;
import java.time.LocalDate;

public class InvalidDateTimeFormat {

    // This field should trigger a compile-time error due to invalid date/time format
    @FlatField(length = 8, dateTimeFormat = "invalid-pattern")
    private LocalDate dateField;
} 