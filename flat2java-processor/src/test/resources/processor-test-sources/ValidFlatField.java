/* This class is used for annotation processor tests.
 * It uses a valid @FlatField annotation (length = 5)
 * to verify that the processor allows successful compilation.
 */
package test;

import com.github.allanhladki.flat2java.markers.FlatField;

public class ValidFlatField {

    // This field should compile successfully due to length = 5
    @FlatField(length = 5)
    private String goodField;
} 