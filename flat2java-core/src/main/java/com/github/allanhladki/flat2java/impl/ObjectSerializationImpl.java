package com.github.allanhladki.flat2java.impl;

import com.github.allanhladki.flat2java.exception.TranslateExceptions;
import com.github.allanhladki.flat2java.interfaces.ObjectSerializationInterface;
import com.github.allanhladki.flat2java.metadata.FieldMetadata;
import com.github.allanhladki.flat2java.serializer.ObjectSerializer;

/**
 * Implementation of {@link ObjectSerializationInterface} that delegates to the static utility
 * methods. This implementation provides the interface pattern while reusing existing, well-tested
 * code. All methods are annotated with @TranslateExceptions for automatic exception translation.
 */
public class ObjectSerializationImpl implements ObjectSerializationInterface {

    @Override
    @TranslateExceptions(
            context = "serializing object",
            operationType = TranslateExceptions.OperationType.SERIALIZATION)
    public String serialize(Object object) {
        return ObjectSerializer.serialize(object);
    }

    @Override
    @TranslateExceptions(
            context = "serializing object with inheritance",
            operationType = TranslateExceptions.OperationType.SERIALIZATION)
    public String serialize(Object object, boolean includeInherited) {
        return ObjectSerializer.serialize(object, includeInherited);
    }

    @Override
    @TranslateExceptions(
            context = "serializing field with metadata",
            operationType = TranslateExceptions.OperationType.SERIALIZATION)
    public String serializeField(Object fieldValue, FieldMetadata metadata) {
        return ObjectSerializer.serialize(fieldValue, metadata);
    }
}
