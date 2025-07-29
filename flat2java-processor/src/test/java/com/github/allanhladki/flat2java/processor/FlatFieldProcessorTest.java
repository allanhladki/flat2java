package com.github.allanhladki.flat2java.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.Test;

/**
 * Tests for the FlatFieldProcessor annotation processor using compile-testing. These tests verify
 * that compile-time validation works as expected for @FlatField.
 */
class FlatFieldProcessorTest {

    /**
     * This test loads a Java source file with an invalid @FlatField annotation (length = 0) and
     * asserts that the processor produces a compile-time error containing the expected message.
     */
    @Test
    void shouldFailCompilationForNonPositiveLength() {
        // Load the invalid test class from resources
        JavaFileObject source =
                JavaFileObjects.forResource("processor-test-sources/InvalidFlatField.java");
        // Compile the source with the annotation processor
        Compilation compilation =
                Compiler.javac().withProcessors(new FlatFieldProcessor()).compile(source);
        // Assert that a compile error is produced with the expected message
        assertThat(compilation).hadErrorContaining("must be a positive integer");
    }

    /**
     * This test loads a Java source file with a valid @FlatField annotation (length = 5) and asserts
     * that the processor allows compilation to succeed.
     */
    @Test
    void shouldCompileSuccessfullyForValidLength() {
        // Load the valid test class from resources
        JavaFileObject source =
                JavaFileObjects.forResource("processor-test-sources/ValidFlatField.java");
        // Compile the source with the annotation processor
        Compilation compilation =
                Compiler.javac().withProcessors(new FlatFieldProcessor()).compile(source);
        // Assert that compilation succeeds (no errors)
        assertThat(compilation).succeeded();
    }
}
