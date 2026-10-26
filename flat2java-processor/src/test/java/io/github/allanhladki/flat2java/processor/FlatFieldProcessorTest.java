package io.github.allanhladki.flat2java.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;

import org.junit.jupiter.api.Test;

class FlatFieldProcessorTest {

    private static final String HEADER =
            "package test;\n"
                    + "import io.github.allanhladki.flat2java.annotation.FlatField;\n"
                    + "import java.math.*;\n"
                    + "import java.time.*;\n"
                    + "import java.util.*;\n";

    /** Compiles {@code test.Sample} (and any package-private classes in the same source). */
    private static Compilation compile(String body) {
        return Compiler.javac()
                .withProcessors(new FlatFieldProcessor())
                .compile(JavaFileObjects.forSourceString("test.Sample", HEADER + body));
    }

    @Test
    void validMappingCompiles() {
        Compilation compilation =
                compile(
                        "public class Sample {\n"
                            + "  @FlatField(length = 10) String text;\n"
                            + "  @FlatField(length = 5) Integer boxedInt;\n"
                            + "  @FlatField(length = 5) int primitiveInt;\n"
                            + "  @FlatField(length = 10) Long boxedLong;\n"
                            + "  @FlatField(length = 10) long primitiveLong;\n"
                            + "  @FlatField(length = 20) BigInteger big;\n"
                            + "  @FlatField(length = 15, precision = 2) BigDecimal amount;\n"
                            + "  @FlatField(length = 8, dateTimeFormat = \"yyyyMMdd\") LocalDate"
                            + " d;\n"
                            + "  @FlatField(length = 14, dateTimeFormat = \"yyyyMMddHHmmss\")"
                            + " LocalDateTime t;\n"
                            + "  @FlatField(length = 3, padding = '_') String code;\n"
                            + "}\n");

        assertThat(compilation).succeededWithoutWarnings();
    }

    @Test
    void nonPositiveLength() {
        assertThat(compile("public class Sample { @FlatField(length = 0) String s; }"))
                .hadErrorContaining("Sample.s: length must be greater than 0, got 0");
    }

    @Test
    void negativePrecision() {
        assertThat(
                        compile(
                                "public class Sample { @FlatField(length = 10, precision = -1)"
                                        + " BigDecimal v; }"))
                .hadErrorContaining("Sample.v: precision must not be negative, got -1");
    }

    @Test
    void precisionNotSmallerThanLength() {
        assertThat(
                        compile(
                                "public class Sample { @FlatField(length = 5, precision = 5)"
                                        + " BigDecimal v; }"))
                .hadErrorContaining("Sample.v: precision (5) must be smaller than length (5)");
    }

    @Test
    void precisionOutsideBigDecimal() {
        assertThat(compile("public class Sample { @FlatField(length = 5, precision = 2) int v; }"))
                .hadErrorContaining("Sample.v: precision is only allowed on BigDecimal fields");
    }

    @Test
    void paddingOnNumber() {
        assertThat(compile("public class Sample { @FlatField(length = 5, padding = '*') long v; }"))
                .hadErrorContaining("Sample.v: padding is only allowed on String fields");
    }

    @Test
    void staticField() {
        assertThat(compile("public class Sample { @FlatField(length = 5) static String s; }"))
                .hadErrorContaining("Sample.s: @FlatField is not allowed on static fields");
    }

    @Test
    void unsupportedPrimitive() {
        assertThat(compile("public class Sample { @FlatField(length = 1) boolean b; }"))
                .hadErrorContaining("Sample.b: unsupported type boolean");
    }

    @Test
    void interfaceType() {
        assertThat(compile("public class Sample { @FlatField(length = 10) Collection<String> l; }"))
                .hadErrorContaining(
                        "Sample.l: unsupported type java.util.Collection (abstract types cannot be"
                                + " instantiated when reading)");
    }

    @Test
    void dateWithoutFormat() {
        assertThat(compile("public class Sample { @FlatField(length = 8) LocalDate d; }"))
                .hadErrorContaining(
                        "Sample.d: dateTimeFormat is required for LocalDate/LocalDateTime fields");
    }

    @Test
    void formatOnNonDate() {
        assertThat(
                        compile(
                                "public class Sample { @FlatField(length = 8, dateTimeFormat ="
                                        + " \"yyyyMMdd\") String s; }"))
                .hadErrorContaining(
                        "Sample.s: dateTimeFormat is only allowed on LocalDate/LocalDateTime"
                                + " fields");
    }

    @Test
    void invalidFormat() {
        assertThat(
                        compile(
                                "public class Sample { @FlatField(length = 8, dateTimeFormat ="
                                        + " \"invalid-pattern\") LocalDate d; }"))
                .hadErrorContaining("Sample.d: invalid dateTimeFormat 'invalid-pattern'");
    }

    @Test
    void formatLengthMismatch() {
        assertThat(
                        compile(
                                "public class Sample { @FlatField(length = 10, dateTimeFormat ="
                                        + " \"yyyyMMdd\") LocalDate d; }"))
                .hadErrorContaining(
                        "Sample.d: dateTimeFormat 'yyyyMMdd' produces 8 characters, but length is"
                                + " 10");
    }

    @Test
    void variableWidthFormat() {
        assertThat(
                        compile(
                                "public class Sample { @FlatField(length = 8, dateTimeFormat ="
                                        + " \"d/M/yyyy\") LocalDate d; }"))
                .hadErrorContaining("Sample.d: dateTimeFormat 'd/M/yyyy' has a variable width");
    }

    @Test
    void validPositions() {
        assertThat(
                        compile(
                                "public class Sample {\n"
                                        + "  @FlatField(position = 2, length = 1) String b;\n"
                                        + "  @FlatField(position = 1, length = 1) String a;\n"
                                        + "}"))
                .succeededWithoutWarnings();
    }

    @Test
    void duplicatePosition() {
        assertThat(
                        compile(
                                "public class Sample {\n"
                                        + "  @FlatField(position = 1, length = 1) String a;\n"
                                        + "  @FlatField(position = 1, length = 1) String b;\n"
                                        + "}"))
                .hadErrorContaining("Sample: position 1 is used by both Sample.a and Sample.b");
    }

    @Test
    void mixedPositionsAcrossHierarchy() {
        assertThat(
                        compile(
                                "public class Sample extends Base {\n"
                                        + "  @FlatField(length = 1) String derived;\n"
                                        + "}\n"
                                        + "class Base { @FlatField(position = 1, length = 1)"
                                        + " String base; }"))
                .hadErrorContaining(
                        "Sample: either every @FlatField sets position or none does; missing on"
                                + " Sample.derived");
    }

    @Test
    void positionGap() {
        assertThat(
                        compile(
                                "public class Sample {\n"
                                        + "  @FlatField(position = 1, length = 1) String a;\n"
                                        + "  @FlatField(position = 3, length = 1) String b;\n"
                                        + "}"))
                .hadErrorContaining("Sample: positions must be contiguous starting at 1");
    }

    @Test
    void validNestedRecord() {
        assertThat(
                        compile(
                                "public class Sample {\n"
                                        + "  @FlatField(length = 4) int id;\n"
                                        + "  @FlatField(length = 12) Address address;\n"
                                        + "}\n"
                                        + "class Place { @FlatField(length = 2) String state; }\n"
                                        + "class Address extends Place {\n"
                                        + "  @FlatField(length = 10) String city;\n"
                                        + "}"))
                .succeededWithoutWarnings();
    }

    @Test
    void nestedLengthMismatch() {
        assertThat(
                        compile(
                                "public class Sample { @FlatField(length = 9) Address address; }\n"
                                        + "class Address { @FlatField(length = 10) String city; }"))
                .hadErrorContaining(
                        "Sample.address: length is 9, but the fields of Address add up to 10");
    }

    @Test
    void nestedWithScalarAttributes() {
        assertThat(
                        compile(
                                "public class Sample { @FlatField(length = 10, precision = 1)"
                                        + " Address address; }\n"
                                        + "class Address { @FlatField(length = 10) String city; }"))
                .hadErrorContaining(
                        "Sample.address: precision, dateTimeFormat and padding are not allowed on"
                                + " a nested record field");
    }

    @Test
    void nestedCycle() {
        assertThat(
                        compile(
                                "public class Sample {\n"
                                        + "  @FlatField(length = 1) String value;\n"
                                        + "  @FlatField(length = 2) Link next;\n"
                                        + "}\n"
                                        + "class Link { @FlatField(length = 1) Sample sample; }"))
                .hadErrorContaining("Cyclic nested records: Sample -> Link -> Sample");
    }

    @Test
    void nestedWithoutNoArgConstructor() {
        assertThat(
                        compile(
                                "public class Sample { @FlatField(length = 1) Code code; }\n"
                                        + "class Code {\n"
                                        + "  @FlatField(length = 1) String value;\n"
                                        + "  Code(String value) { this.value = value; }\n"
                                        + "}"))
                .hadErrorContaining("Code: a no-arg constructor is required");
    }

    @Test
    void objectField() {
        assertThat(compile("public class Sample { @FlatField(length = 5) Object value; }"))
                .hadErrorContaining(
                        "Sample.value: unsupported type java.lang.Object (declare the concrete"
                                + " class of the nested record)");
    }

    @Test
    void typeVariableField() {
        assertThat(compile("public class Sample<T> { @FlatField(length = 5) T value; }"))
                .hadErrorContaining(
                        "Sample.value: unsupported type T (type variables cannot be instantiated"
                                + " when reading)");
    }

    @Test
    void classWithoutAnnotatedFields() {
        assertThat(compile("public class Sample { @FlatField(length = 5) StringBuilder sb; }"))
                .hadErrorContaining("Sample.sb: unsupported type java.lang.StringBuilder.");
    }

    @Test
    void validListsArraysAndRecordsCompile() {
        Compilation compilation =
                compile(
                        "public class Sample {\n"
                                + "  @FlatField(length = 3, occurs = 2) List<String> codes;\n"
                                + "  @FlatField(length = 3, occurs = 3) int[] counts;\n"
                                + "  @FlatField(length = 5, occurs = 2) List<Item> items;\n"
                                + "  @FlatField(length = 8) Point point;\n"
                                + "}\n"
                                + "class Item {\n"
                                + "  @FlatField(length = 3) String sku;\n"
                                + "  @FlatField(length = 2) int quantity;\n"
                                + "}\n"
                                + "record Point(@FlatField(length = 4) int x,"
                                + " @FlatField(length = 2, occurs = 2) List<String> tags) {}\n");

        assertThat(compilation).succeededWithoutWarnings();
    }

    @Test
    void occursRequiredOnLists() {
        assertThat(compile("public class Sample { @FlatField(length = 3) List<String> codes; }"))
                .hadErrorContaining(
                        "Sample.codes: occurs is required on List and array fields and must be"
                                + " greater than 0, got 0");
    }

    @Test
    void occursOnlyOnListsAndArrays() {
        assertThat(compile("public class Sample { @FlatField(length = 3, occurs = 2) String s; }"))
                .hadErrorContaining("Sample.s: occurs is only allowed on List and array fields");
    }

    @Test
    void listOfLists() {
        assertThat(
                        compile(
                                "public class Sample { @FlatField(length = 3, occurs = 2)"
                                        + " List<List<String>> codes; }"))
                .hadErrorContaining(
                        "Sample.codes: unsupported type"
                            + " java.util.List<java.util.List<java.lang.String>> (the elements of a"
                            + " List or array cannot be Lists or arrays)");
    }

    @Test
    void wildcardList() {
        assertThat(
                        compile(
                                "public class Sample { @FlatField(length = 3, occurs = 2)"
                                        + " List<? extends CharSequence> codes; }"))
                .hadErrorContaining("(declare the element type without a wildcard)");
    }

    @Test
    void rawList() {
        assertThat(
                        compile(
                                "public class Sample { @SuppressWarnings(\"rawtypes\")"
                                        + " @FlatField(length = 3, occurs = 2) List codes; }"))
                .hadErrorContaining("(declare the element type, as in List<String>)");
    }

    @Test
    void nestedListElementLengthIsPerElement() {
        assertThat(
                        compile(
                                "public class Sample { @FlatField(length = 10, occurs = 2)"
                                        + " List<Item> items; }\n"
                                        + "class Item { @FlatField(length = 5) String sku; }"))
                .hadErrorContaining(
                        "Sample.items: length is 10, but the fields of Item add up to 5");
    }

    @Test
    void nestedSumCountsRepeatedFields() {
        assertThat(
                        compile(
                                "public class Sample { @FlatField(length = 3) Item item; }\n"
                                        + "class Item { @FlatField(length = 2, occurs = 3)"
                                        + " int[] counts; }"))
                .hadErrorContaining("Sample.item: length is 3, but the fields of Item add up to 6");
    }

    @Test
    void recordComponentsAreValidated() {
        assertThat(
                        compile(
                                "public class Sample { @FlatField(length = 3) Point p; }\n"
                                        + "record Point(@FlatField(length = 0) int x) {}"))
                .hadErrorContaining("Point.x: length must be greater than 0, got 0");
    }

    @Test
    void cycleThroughAList() {
        assertThat(
                        compile(
                                "public class Sample { @FlatField(length = 1) Node node; }\n"
                                        + "class Node { @FlatField(length = 1, occurs = 1)"
                                        + " List<Node> children; }"))
                .hadErrorContaining("Cyclic nested records");
    }

    @Test
    void innerClassIsRejected() {
        assertThat(
                        compile(
                                "public class Sample {\n"
                                        + "  @FlatField(length = 5) Address address;\n"
                                        + "  class Address { @FlatField(length = 5) String zip; }\n"
                                        + "}"))
                .hadErrorContaining(
                        "Address: a no-arg constructor is required (inner classes must be declared"
                                + " static)");
    }

    @Test
    void staticNestedClassCompiles() {
        assertThat(
                        compile(
                                "public class Sample {\n"
                                        + "  @FlatField(length = 5) Address address;\n"
                                        + "  static class Address { @FlatField(length = 5) String"
                                        + " zip; }\n"
                                        + "}"))
                .succeededWithoutWarnings();
    }

    @Test
    void twoDigitYearIsRejected() {
        assertThat(
                        compile(
                                "public class Sample { @FlatField(length = 6, dateTimeFormat ="
                                        + " \"yyMMdd\") LocalDate d; }"))
                .hadErrorContaining("does not keep the century (as with yy); use yyyy");
    }
}
