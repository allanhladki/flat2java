package io.github.allanhladki.flat2java.impl;

import io.github.allanhladki.flat2java.exception.TranslateExceptions;
import io.github.allanhladki.flat2java.interfaces.FieldMetadataInterface;
import io.github.allanhladki.flat2java.metadata.FieldMetadata;
import io.github.allanhladki.flat2java.metadata.FieldMetadataExtractor;

import java.util.List;

/**
 * Implementation of {@link FieldMetadataInterface} that delegates to the static utility methods.
 * This implementation provides the interface pattern while reusing existing, well-tested code. All
 * methods are annotated with @TranslateExceptions for automatic exception translation.
 */
public class FieldMetadataImpl implements FieldMetadataInterface {

    @Override
    @TranslateExceptions(
            context = "extracting field metadata",
            operationType = TranslateExceptions.OperationType.VALIDATION)
    public List<FieldMetadata> extractFieldMetadata(Class<?> clazz) {
        return FieldMetadataExtractor.extract(clazz);
    }

    @Override
    @TranslateExceptions(
            context = "extracting field metadata with inheritance",
            operationType = TranslateExceptions.OperationType.VALIDATION)
    public List<FieldMetadata> extractFieldMetadataIncludingInherited(Class<?> clazz) {
        return FieldMetadataExtractor.extractIncludingInherited(clazz);
    }
}
