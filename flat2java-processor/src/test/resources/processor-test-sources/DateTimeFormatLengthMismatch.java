/* This class is used for annotation processor tests.
 * It intentionally uses a date/time format that produces a different length
 * than the specified field length to verify that the processor produces a compile-time error.
 */
package test;

import io.github.allanhladki.flat2java.annotation.FlatField;
import java.time.LocalDate;

public class DateTimeFormatLengthMismatch {

    // This field should trigger a compile-time error due to format length mismatch
    // yyyyMMdd produces 8 characters, but length is 10
    @FlatField(length = 10, dateTimeFormat = "yyyyMMdd")
    private LocalDate dateField;
} 