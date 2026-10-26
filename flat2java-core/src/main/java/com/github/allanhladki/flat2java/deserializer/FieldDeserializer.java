package com.github.allanhladki.flat2java.deserializer;

import com.github.allanhladki.flat2java.config.Flat2JavaConfig;
import com.github.allanhladki.flat2java.metadata.FieldMetadata;
import com.github.allanhladki.flat2java.serializer.SerializationTypes;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for deserializing individual field values from flat file strings. This class
 * handles type-specific parsing logic and mirrors the functionality of {@code FieldSerializer} in
 * reverse.
 */
public final class FieldDeserializer {

    private FieldDeserializer() {
        // Utility class
    }

    /**
     * Parses a string slice into the appropriate Java type based on field metadata.
     *
     * @param slice the string slice to parse
     * @param metadata the field metadata containing type and formatting information
     * @return the parsed value. For string fields, all-padding values return empty strings (not
     *     null) to maintain round-trip consistency when null handling is enabled.
     * @throws IllegalArgumentException if the slice cannot be parsed or type is unsupported
     */
    public static Object parse(String slice, FieldMetadata metadata) {
        if (slice == null) {
            throw new IllegalArgumentException("Slice cannot be null");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("Field metadata cannot be null");
        }

        Class<?> type = metadata.getField().getType();

        if (SerializationTypes.isStringType(type)) {
            return parseString(slice, metadata);
        }

        if (SerializationTypes.isNumericType(type)) {
            return parseNumeric(slice, metadata);
        }

        if (SerializationTypes.isDateTimeType(type)) {
            return parseDateTime(slice, metadata);
        }

        throw new IllegalArgumentException(
                String.format(
                        "Unsupported field type: %s for field '%s'. %s",
                        type.getSimpleName(),
                        metadata.getName(),
                        SerializationTypes.getSupportedTypesDescription()));
    }

    private static String parseString(String slice, FieldMetadata metadata) {
        if (isAllPadding(slice, metadata.getPadding())) {
            // When null handling is enabled, empty strings represent null values
            // but we deserialize them to empty strings to maintain round-trip consistency
            return "";
        }
        return rtrim(slice, metadata.getPadding());
    }

    private static Object parseNumeric(String slice, FieldMetadata metadata) {
        // For numeric fields, we always use zero padding
        // Numeric fields cannot be null - they must contain valid values

        // Check if it's all zeros - this represents the value 0
        if (isAllPadding(slice, '0')) {
            return parseNumericValue("0", metadata);
        }

        // Check if it's all spaces - this is invalid for numeric fields
        if (isAllPadding(slice, ' ')) {
            throw new IllegalArgumentException(
                    String.format(
                            "Field '%s': cannot deserialize all-padding value '%s' to numeric field"
                                    + " - numeric fields must contain valid values",
                            metadata.getName(), slice));
        }

        // Trim leading zeros and parse the value
        String trimmed = ltrimWithEffectiveNumericPadding(slice, metadata);
        if (trimmed.isEmpty()) {
            // If after trimming zeros, the string is empty, treat as "0"
            trimmed = "0";
        }

        return parseNumericValue(trimmed, metadata);
    }

    private static Object parseNumericValue(String value, FieldMetadata metadata) {
        Class<?> type = metadata.getField().getType();

        if (type == SerializationTypes.SupportedTypes.INTEGER
                || type == SerializationTypes.SupportedTypes.INT_PRIMITIVE) {
            return parseNonNegativeInteger(value, metadata.getName());
        }
        if (type == SerializationTypes.SupportedTypes.LONG
                || type == SerializationTypes.SupportedTypes.LONG_PRIMITIVE) {
            return parseNonNegativeLong(value, metadata.getName());
        }
        if (type == SerializationTypes.SupportedTypes.BIG_INTEGER) {
            return parseNonNegativeBigInteger(value, metadata.getName());
        }
        if (type == SerializationTypes.SupportedTypes.BIG_DECIMAL) {
            return parseBigDecimal(value, metadata);
        }

        throw new IllegalArgumentException(
                String.format(
                        "Unsupported numeric type: %s for field '%s'",
                        type.getSimpleName(), metadata.getName()));
    }

    private static Object parseDateTime(String slice, FieldMetadata metadata) {
        // Date/time fields cannot be null - they must contain valid values

        // Check for all-padding values (spaces or zeros) - these are invalid for date/time fields
        if (isAllPadding(slice, metadata.getPadding()) || isAllPadding(slice, '0')) {
            throw new IllegalArgumentException(
                    String.format(
                            "Field '%s': cannot deserialize all-padding value '%s' to date/time"
                                    + " field - date/time fields must contain valid values",
                            metadata.getName(), slice));
        }

        String dateTimeFormat = metadata.getDateTimeFormat();
        if (dateTimeFormat == null || dateTimeFormat.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Field '%s': dateTimeFormat is required for date/time fields",
                            metadata.getName()));
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateTimeFormat);
            Class<?> type = metadata.getField().getType();

            if (type == SerializationTypes.SupportedTypes.LOCAL_DATE) {
                return LocalDate.parse(slice, formatter);
            }
            if (type == SerializationTypes.SupportedTypes.LOCAL_DATE_TIME) {
                return LocalDateTime.parse(slice, formatter);
            }

            throw new IllegalArgumentException(
                    String.format(
                            "Unsupported date/time type: %s for field '%s'",
                            type.getSimpleName(), metadata.getName()));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    String.format(
                            "Field '%s': failed to parse date/time value '%s' with format '%s'",
                            metadata.getName(), slice, dateTimeFormat),
                    e);
        }
    }

    private static Integer parseNonNegativeInteger(String s, String fieldName) {
        try {
            int v = Integer.parseInt(s);
            if (v < 0) {
                throw new IllegalArgumentException(
                        String.format(
                                "Field '%s': value must be non-negative, got %d", fieldName, v));
            }
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format("Field '%s': invalid integer '%s'", fieldName, s), e);
        }
    }

    private static Long parseNonNegativeLong(String s, String fieldName) {
        try {
            long v = Long.parseLong(s);
            if (v < 0) {
                throw new IllegalArgumentException(
                        String.format(
                                "Field '%s': value must be non-negative, got %d", fieldName, v));
            }
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format("Field '%s': invalid long '%s'", fieldName, s), e);
        }
    }

    private static BigInteger parseNonNegativeBigInteger(String s, String fieldName) {
        try {
            BigInteger v = new BigInteger(s);
            if (v.compareTo(BigInteger.ZERO) < 0) {
                throw new IllegalArgumentException(
                        String.format(
                                "Field '%s': value must be non-negative, got %s", fieldName, v));
            }
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format("Field '%s': invalid BigInteger '%s'", fieldName, s), e);
        }
    }

    private static BigDecimal parseBigDecimal(String s, FieldMetadata metadata) {
        int precision = metadata.getPrecision();
        if (precision < 0) {
            throw new IllegalArgumentException(
                    String.format(
                            "Field '%s': precision must be non-negative", metadata.getName()));
        }

        try {
            if (Flat2JavaConfig.isBigDecimalUseDecimalSeparator()) {
                char separator = Flat2JavaConfig.getBigDecimalDecimalSeparator();
                String normalized = (separator == '.') ? s : s.replace(separator, '.');
                BigDecimal value = new BigDecimal(normalized);
                if (precision > 0) {
                    return value.setScale(precision, Flat2JavaConfig.getBigDecimalRoundingMode());
                }
                return value.stripTrailingZeros();
            }

            BigInteger intRep = new BigInteger(s);
            BigDecimal value = new BigDecimal(intRep);
            if (precision > 0) {
                value = value.movePointLeft(precision);
                return value.setScale(precision, Flat2JavaConfig.getBigDecimalRoundingMode());
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format("Field '%s': invalid BigDecimal '%s'", metadata.getName(), s), e);
        }
    }

    private static String ltrimWithEffectiveNumericPadding(String slice, FieldMetadata metadata) {
        // For numeric fields, we always trim leading zeros
        int i = 0;
        while (i < slice.length() && slice.charAt(i) == '0') {
            i++;
        }
        return slice.substring(i);
    }

    /**
     * Checks if a string consists entirely of padding characters. For numeric fields, this should
     * typically only be true for space padding, not for zero padding, as "00000" represents a valid
     * numeric value.
     *
     * @param slice the string to check
     * @param padding the padding character to check for
     * @return true if the string consists entirely of padding characters
     */
    private static boolean isAllPadding(String slice, char padding) {
        for (int i = 0; i < slice.length(); i++) {
            if (slice.charAt(i) != padding) {
                return false;
            }
        }
        return true;
    }

    private static String rtrim(String s, char padding) {
        int end = s.length();
        while (end > 0 && s.charAt(end - 1) == padding) {
            end--;
        }
        return s.substring(0, end);
    }
}
