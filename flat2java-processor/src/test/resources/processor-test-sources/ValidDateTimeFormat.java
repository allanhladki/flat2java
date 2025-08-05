/* This class is used for annotation processor tests.
 * It uses valid date/time formats with @FlatField annotations
 * to verify that the processor allows successful compilation.
 */
package test;

import com.github.allanhladki.flat2java.markers.FlatField;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ValidDateTimeFormat {

    @FlatField(length = 8, dateTimeFormat = "yyyyMMdd")
    private LocalDate dateField;

    @FlatField(length = 14, dateTimeFormat = "yyyyMMddHHmmss")
    private LocalDateTime dateTimeField;
} 