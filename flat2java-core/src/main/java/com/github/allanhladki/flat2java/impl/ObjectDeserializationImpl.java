package com.github.allanhladki.flat2java.impl;

import com.github.allanhladki.flat2java.deserializer.ObjectDeserializer;
import com.github.allanhladki.flat2java.exception.TranslateExceptions;
import com.github.allanhladki.flat2java.interfaces.ObjectDeserializationInterface;

/**
 * Implementation of {@link ObjectDeserializationInterface} that delegates to the static utility
 * methods. This implementation provides the interface pattern while reusing existing, well-tested
 * code. All methods are annotated with @TranslateExceptions for automatic exception translation.
 */
public class ObjectDeserializationImpl implements ObjectDeserializationInterface {

    @Override
    @TranslateExceptions(
            context = "deserializing object",
            operationType = TranslateExceptions.OperationType.DESERIALIZATION)
    public <T> T deserialize(String flat, Class<T> clazz) {
        return ObjectDeserializer.deserialize(flat, clazz);
    }

    @Override
    @TranslateExceptions(
            context = "deserializing object with inheritance",
            operationType = TranslateExceptions.OperationType.DESERIALIZATION)
    public <T> T deserialize(String flat, Class<T> clazz, boolean includeInherited) {
        return ObjectDeserializer.deserialize(flat, clazz, includeInherited);
    }
}
