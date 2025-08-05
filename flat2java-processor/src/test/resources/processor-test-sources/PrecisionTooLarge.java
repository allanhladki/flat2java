/* This class is used for annotation processor tests.
 * It intentionally uses an invalid @FlatField annotation (precision >= length)
 * to verify that the processor produces a compile-time error.
 */
package test;

import com.github.allanhladki.flat2java.markers.FlatField;

public class PrecisionTooLarge {

    // This field should trigger a compile-time error due to precision >= length
    @FlatField(length = 5, precision = 5)
    private String field;
} 