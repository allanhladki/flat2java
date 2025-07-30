package com.github.allanhladki.flat2java.serializer;

import com.github.allanhladki.flat2java.markers.FlatField;
import com.github.allanhladki.flat2java.config.Flat2JavaConfig;
import com.github.allanhladki.flat2java.metadata.FieldMetadata;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for serializing field values of specific types to strings.
 * This class provides type-specific serialization methods that can be used
 * independently or by the ObjectSerializer.
 */
public class FieldSerializer {
    private static final Logger logger = LoggerFactory.getLogger(FieldSerializer.class);


    /**
     * Serializes a String value.
     */
    public static String serialize(String value) {
        if (value == null) {
            throw new NullPointerException("String value cannot be null");
        }
        return value;
    }
    //TODO: Option to throw exception if truncate the string is needed.

    /**
     * Serializes an Integer value.
     */
    public static String serialize(Integer value) {
        if (value == null) {
            throw new NullPointerException("Integer value cannot be null");
        }
        if (value < 0) {
            throw new IllegalArgumentException("Integer value must be non-negative, got: " + value);
        }
        return value.toString();
    }

    /**
     * Serializes a Long value.
     */
    public static String serialize(Long value) {
        if (value == null) {
            throw new NullPointerException("Long value cannot be null");
        }
        if (value < 0) {
            throw new IllegalArgumentException("Long value must be non-negative, got: " + value);
        }
        return value.toString();
    }

    /**
     * Serializes a BigInteger value.
     */
    public static String serialize(BigInteger value) {
        if (value == null) {
            throw new NullPointerException("BigInteger value cannot be null");
        }
        if (value.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("BigInteger value must be non-negative, got: " + value);
        }
        return value.toString();
    }

    /**
     * Serializes a BigDecimal value with precision formatting.
     */
    public static String serialize(BigDecimal value, int precision) {
        if (value == null) {
            throw new NullPointerException("BigDecimal value cannot be null");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("BigDecimal value must be non-negative, got: " + value);
        }

        //TODO: create a enviroment variable to allow the user to choose the rounding mode.

        if (precision == 0) {
            return serialize(value.intValue());
        }

        if (precision > 0) {
            // Scale the value to the specified precision
            BigDecimal scaledValue = value.setScale(precision, RoundingMode.HALF_UP);
            
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

    /**
     * Serializes a LocalDate value using the specified date format.
     */
    public static String serialize(LocalDate value, String dateTimeFormat) {
        if (value == null) {
            throw new NullPointerException("LocalDate value cannot be null");
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

    /**
     * Serializes a LocalDateTime value using the specified date format.
     */
    public static String serialize(LocalDateTime value, String dateTimeFormat) {
        if (value == null) {
            throw new NullPointerException("LocalDateTime value cannot be null");
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
        char defaultPadding;

        try{
            defaultPadding = (char) FlatField.class.getMethod("padding").getDefaultValue();
        } catch (NoSuchMethodException e) {
            logger.error("Failed to get default padding value", e);
            defaultPadding = ' ';
        }

        if (isNumeric && paddingChar == defaultPadding) {
            paddingChar = Flat2JavaConfig.getDefaultNumericPadding();
        }

        if (value.length() > targetLength) {
            // Truncate if too long
            logger.warn("Field '{}' value '{}' (length {}) exceeds target length {}, truncating", 
                       metadata.getName(), value, value.length(), targetLength);
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
     * Creates an empty field string with the appropriate length and padding.
     */
    public static String createEmptyField(FieldMetadata metadata) {
        return String.valueOf(metadata.getPadding()).repeat(metadata.getLength());
    }
} 