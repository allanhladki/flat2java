/* This class is used for annotation processor tests.
 * It intentionally uses an invalid @FlatField annotation (length = 0)
 * to verify that the processor produces a compile-time error. 
*/
package test;

import io.github.allanhladki.flat2java.annotation.FlatField;

public class InvalidFlatField {

    // This field should trigger a compile-time error due to length = 0
    @FlatField(length = 0)
    private String badField;
    
} 