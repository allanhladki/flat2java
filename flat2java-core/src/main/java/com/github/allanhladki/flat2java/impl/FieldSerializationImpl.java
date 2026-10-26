package com.github.allanhladki.flat2java.impl;

import com.github.allanhladki.flat2java.exception.TranslateExceptions;
import com.github.allanhladki.flat2java.interfaces.FieldSerializationInterface;
import com.github.allanhladki.flat2java.metadata.FieldMetadata;
import com.github.allanhladki.flat2java.serializer.FieldSerializer;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Implementation of {@link FieldSerializationInterface} that delegates to the static utility
 * methods. This implementation provides the interface pattern while reusing existing, well-tested
 * code. All methods are annotated with @TranslateExceptions for automatic exception translation.
 */
public class FieldSerializationImpl implements FieldSerializationInterface {

    @Override
    @TranslateExceptions(
            context = "serializing String value",
            operationType = TranslateExceptions.OperationType.SERIALIZATION)
    public String serializeString(String value) {
        return FieldSerializer.serialize(value);
    }

    @Override
    @TranslateExceptions(
            context = "serializing Integer value",
            operationType = TranslateExceptions.OperationType.SERIALIZATION)
    public String serializeInteger(Integer value) {
        return FieldSerializer.serialize(value);
    }

    @Override
    @TranslateExceptions(
            context = "serializing Long value",
            operationType = TranslateExceptions.OperationType.SERIALIZATION)
    public String serializeLong(Long value) {
        return FieldSerializer.serialize(value);
    }

    @Override
    @TranslateExceptions(
            context = "serializing BigInteger value",
            operationType = TranslateExceptions.OperationType.SERIALIZATION)
    public String serializeBigInteger(BigInteger value) {
        return FieldSerializer.serialize(value);
    }

    @Override
    @TranslateExceptions(
            context = "serializing BigDecimal value with precision",
            operationType = TranslateExceptions.OperationType.SERIALIZATION)
    public String serializeBigDecimal(BigDecimal value, int precision) {
        return FieldSerializer.serialize(value, precision);
    }

    @Override
    @TranslateExceptions(
            context = "serializing LocalDate value with format",
            operationType = TranslateExceptions.OperationType.SERIALIZATION)
    public String serializeLocalDate(LocalDate value, String dateTimeFormat) {
        return FieldSerializer.serialize(value, dateTimeFormat);
    }

    @Override
    @TranslateExceptions(
            context = "serializing LocalDateTime value with format",
            operationType = TranslateExceptions.OperationType.SERIALIZATION)
    public String serializeLocalDateTime(LocalDateTime value, String dateTimeFormat) {
        return FieldSerializer.serialize(value, dateTimeFormat);
    }

    /**
     * Formats a string value to the specified length using padding or truncation. This is a utility
     * method used internally by the serialization process.
     *
     * @param value the string value to format
     * @param metadata the field metadata containing length and padding information
     * @return the formatted string
     */
    @TranslateExceptions(
            context = "formatting string to length",
            operationType = TranslateExceptions.OperationType.SERIALIZATION)
    public String formatToLength(String value, FieldMetadata metadata) {
        return FieldSerializer.formatToLength(value, metadata);
    }

    /**
     * Creates an empty field string with the specified length and padding.
     *
     * @param metadata the field metadata containing length and padding information
     * @return the empty field string
     */
    @TranslateExceptions(
            context = "creating empty field",
            operationType = TranslateExceptions.OperationType.SERIALIZATION)
    public String createEmptyField(FieldMetadata metadata) {
        return FieldSerializer.createEmptyField(metadata);
    }
}
