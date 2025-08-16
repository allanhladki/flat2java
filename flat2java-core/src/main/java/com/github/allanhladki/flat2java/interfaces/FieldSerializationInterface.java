package com.github.allanhladki.flat2java.interfaces;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Interface for serializing individual field values to strings. This interface handles the
 * conversion of Java objects to string representations suitable for fixed-length flat file formats.
 */
public interface FieldSerializationInterface {

    /**
     * Serializes a String value.
     *
     * @param value the string value to serialize
     * @return the serialized string
     */
    String serializeString(String value);

    /**
     * Serializes an Integer value.
     *
     * @param value the integer value to serialize
     * @return the serialized string
     */
    String serializeInteger(Integer value);

    /**
     * Serializes a Long value.
     *
     * @param value the long value to serialize
     * @return the serialized string
     */
    String serializeLong(Long value);

    /**
     * Serializes a BigInteger value.
     *
     * @param value the BigInteger value to serialize
     * @return the serialized string
     */
    String serializeBigInteger(BigInteger value);

    /**
     * Serializes a BigDecimal value with precision formatting.
     *
     * @param value the BigDecimal value to serialize
     * @param precision the number of decimal places
     * @return the serialized string
     */
    String serializeBigDecimal(BigDecimal value, int precision);

    /**
     * Serializes a LocalDate value using the specified date format.
     *
     * @param value the LocalDate value to serialize
     * @param dateTimeFormat the date format pattern
     * @return the serialized string
     */
    String serializeLocalDate(LocalDate value, String dateTimeFormat);

    /**
     * Serializes a LocalDateTime value using the specified date format.
     *
     * @param value the LocalDateTime value to serialize
     * @param dateTimeFormat the date format pattern
     * @return the serialized string
     */
    String serializeLocalDateTime(LocalDateTime value, String dateTimeFormat);
}
