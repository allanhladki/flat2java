package com.github.allanhladki.flat2java.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for the FlatFieldProcessor annotation processor using compile-testing. These
 * tests verify that compile-time validation works as expected for @FlatField annotations.
 */
@DisplayName("FlatFieldProcessor Tests")
class FlatFieldProcessorTest {

    @Test
    @DisplayName("Should compile successfully for valid @FlatField annotation")
    void shouldCompileSuccessfullyForValidAnnotation() {
        JavaFileObject source =
                JavaFileObjects.forResource("processor-test-sources/ValidFlatField.java");
        Compilation compilation =
                Compiler.javac().withProcessors(new FlatFieldProcessor()).compile(source);
        assertThat(compilation).succeeded();
    }

    @Test
    @DisplayName("Should fail compilation for non-positive length")
    void shouldFailCompilationForNonPositiveLength() {
        JavaFileObject source =
                JavaFileObjects.forResource("processor-test-sources/InvalidFlatField.java");
        Compilation compilation =
                Compiler.javac().withProcessors(new FlatFieldProcessor()).compile(source);
        assertThat(compilation).hadErrorContaining("must be a positive integer");
    }

    @Test
    @DisplayName("Should fail compilation for negative precision")
    void shouldFailCompilationForNegativePrecision() {
        JavaFileObject source =
                JavaFileObjects.forResource("processor-test-sources/NegativePrecision.java");
        Compilation compilation =
                Compiler.javac().withProcessors(new FlatFieldProcessor()).compile(source);
        assertThat(compilation).hadErrorContaining("precision must be a non-negative integer");
    }

    @Test
    @DisplayName("Should fail compilation when precision >= length")
    void shouldFailCompilationWhenPrecisionGreaterThanOrEqualToLength() {
        JavaFileObject source =
                JavaFileObjects.forResource("processor-test-sources/PrecisionTooLarge.java");
        Compilation compilation =
                Compiler.javac().withProcessors(new FlatFieldProcessor()).compile(source);
        assertThat(compilation).hadErrorContaining("precision (5) must be smaller than length (5)");
    }

    @Test
    @DisplayName("Should fail compilation for unsupported field type")
    void shouldFailCompilationForUnsupportedFieldType() {
        JavaFileObject source =
                JavaFileObjects.forResource("processor-test-sources/UnsupportedType.java");
        Compilation compilation =
                Compiler.javac().withProcessors(new FlatFieldProcessor()).compile(source);
        assertThat(compilation).hadErrorContaining("unsupported field type");
    }

    @Test
    @DisplayName("Should compile successfully for all supported field types")
    void shouldCompileSuccessfullyForAllSupportedFieldTypes() {
        JavaFileObject source =
                JavaFileObjects.forResource("processor-test-sources/SupportedTypes.java");
        Compilation compilation =
                Compiler.javac().withProcessors(new FlatFieldProcessor()).compile(source);
        assertThat(compilation).succeeded();
    }

    @Test
    @DisplayName("Should fail compilation for invalid date/time format")
    void shouldFailCompilationForInvalidDateTimeFormat() {
        JavaFileObject source =
                JavaFileObjects.forResource("processor-test-sources/InvalidDateTimeFormat.java");
        Compilation compilation =
                Compiler.javac().withProcessors(new FlatFieldProcessor()).compile(source);
        assertThat(compilation).hadErrorContaining("invalid dateTimeFormat");
    }

    @Test
    @DisplayName("Should fail compilation when date/time format length doesn't match field length")
    void shouldFailCompilationWhenDateTimeFormatLengthDoesNotMatchFieldLength() {
        JavaFileObject source =
                JavaFileObjects.forResource("processor-test-sources/DateTimeFormatLengthMismatch.java");
        Compilation compilation =
                Compiler.javac().withProcessors(new FlatFieldProcessor()).compile(source);
        assertThat(compilation).hadErrorContaining("produces 8 characters, but length is 10");
    }

    @Test
    @DisplayName("Should compile successfully for date/time format with matching length")
    void shouldCompileSuccessfullyForDateTimeFormatWithMatchingLength() {
        JavaFileObject source =
                JavaFileObjects.forResource("processor-test-sources/ValidDateTimeFormat.java");
        Compilation compilation =
                Compiler.javac().withProcessors(new FlatFieldProcessor()).compile(source);
        assertThat(compilation).succeeded();
    }

    @Test
    @DisplayName("Should ignore date/time format validation for non-date/time fields")
    void shouldIgnoreDateTimeFormatValidationForNonDateTimeFields() {
        JavaFileObject source =
                JavaFileObjects.forResource("processor-test-sources/NonDateTimeFieldWithFormat.java");
        Compilation compilation =
                Compiler.javac().withProcessors(new FlatFieldProcessor()).compile(source);
        assertThat(compilation).succeeded();
    }

    @Test
    @DisplayName("Should compile successfully when dateTimeFormat is empty")
    void shouldCompileSuccessfullyWhenDateTimeFormatIsEmpty() {
        JavaFileObject source =
                JavaFileObjects.forResource("processor-test-sources/EmptyDateTimeFormat.java");
        Compilation compilation =
                Compiler.javac().withProcessors(new FlatFieldProcessor()).compile(source);
        assertThat(compilation).succeeded();
    }

    @Test
    @DisplayName("Should compile successfully for valid precision with BigDecimal")
    void shouldCompileSuccessfullyForValidPrecisionWithBigDecimal() {
        JavaFileObject source =
                JavaFileObjects.forResource("processor-test-sources/ValidBigDecimalPrecision.java");
        Compilation compilation =
                Compiler.javac().withProcessors(new FlatFieldProcessor()).compile(source);
        assertThat(compilation).succeeded();
    }

    @Test
    @DisplayName("Should handle multiple fields in same class")
    void shouldHandleMultipleFieldsInSameClass() {
        JavaFileObject source =
                JavaFileObjects.forResource("processor-test-sources/MultipleFields.java");
        Compilation compilation =
                Compiler.javac().withProcessors(new FlatFieldProcessor()).compile(source);
        assertThat(compilation).succeeded();
    }
}
