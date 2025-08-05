/* This class is used for annotation processor tests.
 * It uses a BigDecimal field with valid precision to verify that
 * the processor allows successful compilation.
 */
package test;

import com.github.allanhladki.flat2java.markers.FlatField;
import java.math.BigDecimal;

public class ValidBigDecimalPrecision {

    // This field should compile successfully with valid precision
    @FlatField(length = 10, precision = 2)
    private BigDecimal amount;
} 