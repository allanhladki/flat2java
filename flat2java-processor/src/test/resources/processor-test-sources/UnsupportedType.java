/* This class is used for annotation processor tests.
 * It intentionally uses an unsupported field type with @FlatField annotation
 * to verify that the processor produces a compile-time error.
 */
package test;

import com.github.allanhladki.flat2java.markers.FlatField;
import java.util.List;

public class UnsupportedType {

    // This field should trigger a compile-time error due to unsupported type List
    @FlatField(length = 10)
    private List<String> field;
} 