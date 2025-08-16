package com.github.allanhladki.flat2java.serializer;

import com.github.allanhladki.flat2java.config.Flat2JavaConfig;
import com.github.allanhladki.flat2java.markers.FlatField;
import com.github.allanhladki.flat2java.metadata.FieldMetadata;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for serializing field values of specific types to strings. This class provides
 * type-specific serialization methods that can be used independently or by the ObjectSerializer.
 *
 * <p>This class handles the conversion of Java objects to string representations suitable for
 * fixed-length flat file formats. It supports various data types including strings, numbers, dates,
 * and times.
 *
 * <p>All serialization methods perform validation and throw appropriate exceptions for invalid
 * input values.
 */
public class FieldSerializer {
    private static final Logger logger = LoggerFactory.getLogger(FieldSerializer.class);

    /** Serializes a String value. */
    public static String serialize(String value) {
        if (value == null) {
            throw new NullPointerException("String value cannot be null");
        }
        return value;
    }

    /** Serializes an Integer value. */
    public static String serialize(Integer value) {
        if (value == null) {
            throw new NullPointerException("Integer value cannot be null");
        }
        if (value < 0) {
            throw new IllegalArgumentException("Integer value must be non-negative, got: " + value);
        }
        return value.toString();
    }

    /** Serializes a Long value. */
    public static String serialize(Long value) {
        if (value == null) {
            throw new NullPointerException("Long value cannot be null");
        }
        if (value < 0) {
            throw new IllegalArgumentException("Long value must be non-negative, got: " + value);
        }
        return value.toString();
    }

    /** Serializes a BigInteger value. */
    public static String serialize(BigInteger value) {
        if (value == null) {
            throw new NullPointerException("BigInteger value cannot be null");
        }
        if (value.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("BigInteger value must be non-negative, got: " + value);
        }
        return value.toString();
    }

    /** Serializes a BigDecimal value with precision formatting. */
    public static String serialize(BigDecimal value, int precision) {
        if (value == null) {
            throw new NullPointerException("BigDecimal value cannot be null");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("BigDecimal value must be non-negative, got: " + value);
        }

        if (precision == 0) {
            // Apply rounding mode when converting to integer
            BigDecimal roundedValue = value.setScale(0, Flat2JavaConfig.getBigDecimalRoundingMode());
            return serialize(roundedValue.intValue());
        }

        if (precision > 0) {
            // Scale the value to the specified precision
            BigDecimal scaledValue =
                    value.setScale(precision, Flat2JavaConfig.getBigDecimalRoundingMode());

            if (Flat2JavaConfig.isBigDecimalUseDecimalSeparator()) {
                // Use decimal separator with custom character
                char separator = Flat2JavaConfig.getBigDecimalDecimalSeparator();
                String formatted = scaledValue.toPlainString();

                // Ensure we have the decimal point even for whole numbers
                if (!formatted.contains(".")) {
                    formatted += ".0";
                }

                // Replace the default decimal point with the custom separator
                if (separator != '.') {
                    formatted = formatted.replace('.', separator);
                }

                // Pad with zeros to match precision
                int decimalIndex = formatted.indexOf(separator);
                int currentPrecision = formatted.length() - decimalIndex - 1;
                if (currentPrecision < precision) {
                    formatted += "0".repeat(precision - currentPrecision);
                }

                return formatted;
            }

            // Don't use decimal separator - convert to integer representation
            // For example: 12.34 with precision 2 becomes "1234"
            BigDecimal multiplied = scaledValue.multiply(BigDecimal.valueOf(Math.pow(10, precision)));
            return multiplied.toBigInteger().toString();
        }
        return value.toPlainString();
    }

    /** Serializes a LocalDate value using the specified date format. */
    public static String serialize(LocalDate value, String dateTimeFormat) {
        if (value == null) {
            throw new NullPointerException(
                    "LocalDate value cannot be null - date/time fields must contain valid values");
        }
        if (dateTimeFormat.isEmpty()) {
            throw new IllegalArgumentException("DateTime format is required for LocalDate field");
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateTimeFormat);
            return value.format(formatter);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format("Invalid date format '%s': %s", dateTimeFormat, e.getMessage()), e);
        }
    }

    /** Serializes a LocalDateTime value using the specified date format. */
    public static String serialize(LocalDateTime value, String dateTimeFormat) {
        if (value == null) {
            throw new NullPointerException(
                    "LocalDateTime value cannot be null - date/time fields must contain valid values");
        }
        if (dateTimeFormat.isEmpty()) {
            throw new IllegalArgumentException("DateTime format is required for LocalDateTime field");
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateTimeFormat);
            return value.format(formatter);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format("Invalid date format '%s': %s", dateTimeFormat, e.getMessage()), e);
        }
    }

    /**
     * Formats a serialized value to the specified length with proper padding.
     *
     * @param value the value to format
     * @param metadata the field metadata containing length and padding information
     * @param field the field being formatted (used to determine padding direction)
     * @return the formatted string with appropriate padding
     */
    public static String formatToLength(String value, FieldMetadata metadata) {
        int targetLength = metadata.getLength();
        char paddingChar = metadata.getPadding();
        Field field = metadata.getField();
        boolean isNumeric = SerializationTypes.isNumericField(field);

        // For numeric fields, always use zero padding regardless of annotation
        if (isNumeric) {
            paddingChar = '0';
        }

        if (value.length() > targetLength) {
            // Always throw exception for numeric fields to prevent data corruption
            if (SerializationTypes.isNumericField(field)) {
                throw new IllegalArgumentException(
                        String.format(
                                "Field '%s' value '%s' (length %d) exceeds target length %d. Numeric fields cannot"
                                        + " be truncated to prevent data corruption.",
                                metadata.getName(), value, value.length(), targetLength));
            }

            // For non-numeric fields, check if we should throw an exception or truncate
            if (Flat2JavaConfig.isThrowOnStringTruncationEnabled()) {
                throw new IllegalArgumentException(
                        String.format(
                                "Field '%s' value '%s' (length %d) exceeds target length %d. Set %s=false to allow"
                                        + " truncation.",
                                metadata.getName(),
                                value,
                                value.length(),
                                targetLength,
                                Flat2JavaConfig.getThrowOnStringTruncationEnvVarName()));
            }

            // Truncate if too long (only for non-numeric fields)
            logger.warn(
                    "Field '{}' value '{}' (length {}) exceeds target length {}, truncating",
                    metadata.getName(),
                    value,
                    value.length(),
                    targetLength);
            return value.substring(0, targetLength);
        }

        if (value.length() < targetLength) {
            // Pad based on field type
            String padding = String.valueOf(paddingChar).repeat(targetLength - value.length());

            if (SerializationTypes.isNumericField(field)) {
                // For numeric fields, add leading padding
                return padding + value;
            }

            return value + padding;
        }

        return value;
    }

    /**
     * Creates an empty field representation for null values.
     *
     * @param metadata the field metadata containing length and padding information
     * @return the empty field string with appropriate padding
     */
    public static String createEmptyField(FieldMetadata metadata) {
        char paddingChar = metadata.getPadding();
        Field field = metadata.getField();

        // For numeric fields, throw exception - they cannot be null
        if (SerializationTypes.isNumericField(field)) {
            throw new NullPointerException(
                    String.format(
                            "Field '%s' cannot be null - numeric fields must contain valid values",
                            metadata.getName()));
        }

        // For date/time fields, throw exception - they cannot be null
        if (SerializationTypes.isDateTimeField(field)) {
            throw new NullPointerException(
                    String.format(
                            "Field '%s' cannot be null - date/time fields must contain valid values",
                            metadata.getName()));
        }

        // For null values, use the configured null value padding
        // This ensures consistency between serializer and deserializer
        if (paddingChar == FlatField.Defaults.PADDING) {
            // Use configuration's null value padding for non-numeric, non-date/time fields
            paddingChar = Flat2JavaConfig.getNullValuePadding();
        }

        return String.valueOf(paddingChar).repeat(metadata.getLength());
    }
}
