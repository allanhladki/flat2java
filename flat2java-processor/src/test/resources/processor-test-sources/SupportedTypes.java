/* This class is used for annotation processor tests.
 * It uses all supported field types with @FlatField annotations
 * to verify that the processor allows successful compilation.
 */
package test;

import io.github.allanhladki.flat2java.annotation.FlatField;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SupportedTypes {

    @FlatField(length = 10)
    private String stringField;

    @FlatField(length = 5)
    private Integer integerField;

    @FlatField(length = 5)
    private int intField;

    @FlatField(length = 10)
    private Long longField;

    @FlatField(length = 10)
    private long longPrimitiveField;

    @FlatField(length = 20)
    private BigInteger bigIntegerField;

    @FlatField(length = 15, precision = 2)
    private BigDecimal bigDecimalField;

    @FlatField(length = 8, dateTimeFormat = "yyyyMMdd")
    private LocalDate dateField;

    @FlatField(length = 14, dateTimeFormat = "yyyyMMddHHmmss")
    private LocalDateTime dateTimeField;
} 