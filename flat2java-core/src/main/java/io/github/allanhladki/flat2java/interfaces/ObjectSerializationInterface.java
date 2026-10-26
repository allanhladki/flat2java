package io.github.allanhladki.flat2java.interfaces;

import io.github.allanhladki.flat2java.metadata.FieldMetadata;

/**
 * Interface for serializing objects to flat file format strings. This interface uses field metadata
 * extracted from {@link io.github.allanhladki.flat2java.annotation.FlatField} annotations to
 * convert object fields to properly formatted strings.
 */
public interface ObjectSerializationInterface {

    /**
     * Serializes an object to a flat file format string using inheritance (includes superclass
     * fields). This is the default behavior.
     *
     * @param object the object to serialize
     * @return the serialized string representation
     */
    String serialize(Object object);

    /**
     * Serializes an object to a flat file format string with configurable inheritance behavior.
     *
     * @param object the object to serialize
     * @param includeInherited whether to include fields from superclasses
     * @return the serialized string representation
     */
    String serialize(Object object, boolean includeInherited);

    /**
     * Serializes a single field value using its metadata.
     *
     * @param fieldValue the field value to serialize
     * @param metadata the field metadata
     * @return the serialized field value
     */
    String serializeField(Object fieldValue, FieldMetadata metadata);
}
