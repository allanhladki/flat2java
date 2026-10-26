/* This class is used for annotation processor tests.
 * It uses a date/time field with empty date/time format to verify that
 * the processor ignores empty date/time format validation.
 */
package test;

import io.github.allanhladki.flat2java.annotation.FlatField;
import java.time.LocalDate;

public class EmptyDateTimeFormat {

    // This field should compile successfully even with empty date/time format
    @FlatField(length = 8, dateTimeFormat = "")
    private LocalDate dateField;
} 