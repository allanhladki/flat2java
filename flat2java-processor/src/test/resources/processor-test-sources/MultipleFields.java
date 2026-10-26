/* This class is used for annotation processor tests.
 * It uses multiple fields with @FlatField annotations to verify that
 * the processor can handle multiple fields in the same class.
 */
package test;

import io.github.allanhladki.flat2java.annotation.FlatField;
import java.time.LocalDate;

public class MultipleFields {

    @FlatField(length = 10)
    private String name;

    @FlatField(length = 5)
    private Integer age;

    @FlatField(length = 8, dateTimeFormat = "yyyyMMdd")
    private LocalDate birthDate;
} 