package io.github.allanhladki.flat2java.impl;

import io.github.allanhladki.flat2java.deserializer.FieldDeserializer;
import io.github.allanhladki.flat2java.exception.TranslateExceptions;
import io.github.allanhladki.flat2java.interfaces.FieldDeserializationInterface;
import io.github.allanhladki.flat2java.metadata.FieldMetadata;

/**
 * Implementation of {@link FieldDeserializationInterface} that delegates to the static utility
 * methods. This implementation provides the interface pattern while reusing existing, well-tested
 * code. All methods are annotated with @TranslateExceptions for automatic exception translation.
 */
public class FieldDeserializationImpl implements FieldDeserializationInterface {

    @Override
    @TranslateExceptions(
            context = "parsing field value",
            operationType = TranslateExceptions.OperationType.DESERIALIZATION)
    public Object parse(String slice, FieldMetadata metadata) {
        return FieldDeserializer.parse(slice, metadata);
    }
}
