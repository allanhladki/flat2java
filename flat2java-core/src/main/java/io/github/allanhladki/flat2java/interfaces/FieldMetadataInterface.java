package io.github.allanhladki.flat2java.interfaces;

import io.github.allanhladki.flat2java.metadata.FieldMetadata;

import java.util.List;

/**
 * Interface for extracting field metadata from classes annotated with {@link
 * io.github.allanhladki.flat2java.annotation.FlatField}.
 */
public interface FieldMetadataInterface {

    /**
     * Extracts field metadata from all fields in the given class that are annotated with {@link
     * io.github.allanhladki.flat2java.annotation.FlatField}. The fields are ordered by their
     * declaration order in the class.
     *
     * @param clazz the class to extract field metadata from
     * @return a list of FieldMetadata objects, one for each annotated field, in declaration order
     */
    List<FieldMetadata> extractFieldMetadata(Class<?> clazz);

    /**
     * Extracts field metadata from all fields in the given class and its superclasses that are
     * annotated with {@link io.github.allanhladki.flat2java.annotation.FlatField}. The fields are
     * ordered by their declaration order, superclass fields first.
     *
     * @param clazz the class to extract field metadata from
     * @return a list of FieldMetadata objects, one for each annotated field, in declaration order
     */
    List<FieldMetadata> extractFieldMetadataIncludingInherited(Class<?> clazz);
}
