/* This class is used for annotation processor tests.
 * It intentionally uses an invalid @FlatField annotation (precision = -1)
 * to verify that the processor produces a compile-time error.
 */
package test;

import com.github.allanhladki.flat2java.markers.FlatField;

public class NegativePrecision {

    // This field should trigger a compile-time error due to precision = -1
    @FlatField(length = 10, precision = -1)
    private String field;
} 