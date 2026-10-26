package io.github.allanhladki.flat2java.metadata;

import io.github.allanhladki.flat2java.annotation.FlatField;
import io.github.allanhladki.flat2java.serializer.SerializationTypes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Utility class for extracting field metadata from classes annotated with {@link FlatField}. */
public class FieldMetadataExtractor {
    private static final Logger logger = LoggerFactory.getLogger(FieldMetadataExtractor.class);

    /**
     * Extracts field metadata from all fields in the given class that are annotated with {@link
     * FlatField}. The fields are ordered by their declaration order in the class.
     *
     * @param clazz the class to extract field metadata from
     * @return a list of FieldMetadata objects, one for each annotated field, in declaration order
     * @throws NullPointerException if clazz is null
     */
    public static List<FieldMetadata> extract(Class<?> clazz) {
        Objects.requireNonNull(clazz, "Class cannot be null");
        logger.debug("Extracting field metadata from class: {}", clazz.getName());

        List<FieldMetadata> metadata = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(FlatField.class)) {
                FlatField annotation = field.getAnnotation(FlatField.class);
                FieldMetadata fieldMetadata = new FieldMetadata(field, annotation);
                validateFieldMetadata(fieldMetadata);
                metadata.add(fieldMetadata);
            }
        }
        logger.debug("Found {} annotated fields in class {}", metadata.size(), clazz.getName());
        return metadata;
    }

    /**
     * Extracts field metadata from all fields in the given class and its superclasses that are
     * annotated with {@link FlatField}. The fields are ordered by their declaration order,
     * superclass fields first.
     *
     * @param clazz the class to extract field metadata from
     * @return a list of FieldMetadata objects, one for each annotated field, in declaration order
     * @throws NullPointerException if clazz is null
     */
    public static List<FieldMetadata> extractIncludingInherited(Class<?> clazz) {
        Objects.requireNonNull(clazz, "Class cannot be null");
        logger.debug(
                "Extracting field metadata from class hierarchy starting with: {}",
                clazz.getName());

        List<Class<?>> classHierarchy = new ArrayList<>();
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            classHierarchy.add(currentClass);
            currentClass = currentClass.getSuperclass();
        }
        Collections.reverse(classHierarchy); // Superclass fields first

        List<FieldMetadata> metadataList = new ArrayList<>();
        for (Class<?> c : classHierarchy) {
            logger.debug("Processing class: {}", c.getName());
            metadataList.addAll(extract(c));
        }
        logger.debug("Found total of {} annotated fields in class hierarchy", metadataList.size());
        return metadataList;
    }

    /**
     * Validates field metadata for various constraints.
     *
     * @param metadata the field metadata to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateFieldMetadata(FieldMetadata metadata) {
        validateFieldType(metadata);
        validateLengthAndPrecision(metadata);
        validateDateTimeFormatLength(metadata);
    }

    /**
     * Validates that the field type is supported for serialization.
     *
     * @param metadata the field metadata to validate
     * @throws IllegalArgumentException if validation fails
     */
    private static void validateFieldType(FieldMetadata metadata) {
        validateFieldType(metadata.getField().getType(), metadata.getName());
    }

    /**
     * Core validation method for field type. This is the single source of truth for field type
     * validation.
     *
     * @param fieldType the field type (Class<?> for reflection)
     * @param fieldName the field name for error messages
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateFieldType(Class<?> fieldType, String fieldName) {
        if (!SerializationTypes.isSupportedType(fieldType)) {
            throw new IllegalArgumentException(
                    String.format(
                            "Field '%s': unsupported field type '%s'. %s",
                            fieldName,
                            fieldType.getSimpleName(),
                            SerializationTypes.getSupportedTypesDescription()));
        }
    }

    /**
     * Validates that length and precision are positive integers and precision is smaller than
     * length.
     *
     * @param metadata the field metadata to validate
     * @throws IllegalArgumentException if validation fails
     */
    private static void validateLengthAndPrecision(FieldMetadata metadata) {
        validateLengthAndPrecision(
                metadata.getLength(), metadata.getPrecision(), metadata.getName());
    }

    /**
     * Core validation method for length and precision. This is the single source of truth for
     * length and precision validation.
     *
     * @param length the field length
     * @param precision the field precision
     * @param fieldName the field name for error messages
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateLengthAndPrecision(int length, int precision, String fieldName) {
        // Validate length is positive
        if (length <= 0) {
            throw new IllegalArgumentException(
                    String.format(
                            "Field '%s': length must be a positive integer, got %d",
                            fieldName, length));
        }

        // Validate precision is positive if specified
        if (precision < 0) {
            throw new IllegalArgumentException(
                    String.format(
                            "Field '%s': precision must be a non-negative integer, got %d",
                            fieldName, precision));
        }

        // Validate precision is smaller than length
        if (precision >= length) {
            throw new IllegalArgumentException(
                    String.format(
                            "Field '%s': precision (%d) must be smaller than length (%d)",
                            fieldName, precision, length));
        }
    }

    /**
     * Validates that date/time format patterns match the specified length.
     *
     * @param metadata the field metadata to validate
     * @throws IllegalArgumentException if validation fails
     */
    private static void validateDateTimeFormatLength(FieldMetadata metadata) {
        validateDateTimeFormatLength(
                metadata.getDateTimeFormat(),
                metadata.getField().getType(),
                metadata.getLength(),
                metadata.getName());
    }

    /**
     * Core validation method for date/time format patterns. This is the single source of truth for
     * date/time format validation.
     *
     * @param dateTimeFormat the date/time format pattern
     * @param fieldType the field type (Class<?> for reflection)
     * @param length the field length
     * @param fieldName the field name for error messages
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateDateTimeFormatLength(
            String dateTimeFormat, Class<?> fieldType, int length, String fieldName) {
        if (dateTimeFormat == null || dateTimeFormat.isEmpty()) {
            return; // No format specified, skip validation
        }

        if (fieldType == LocalDate.class || fieldType == LocalDateTime.class) {
            try {
                // Create a sample date to test the format
                String sampleDate;
                if (fieldType == LocalDate.class) {
                    sampleDate =
                            LocalDate.of(2024, 1, 1)
                                    .format(DateTimeFormatter.ofPattern(dateTimeFormat));
                } else {
                    sampleDate =
                            LocalDateTime.of(2024, 1, 1, 12, 0, 0)
                                    .format(DateTimeFormatter.ofPattern(dateTimeFormat));
                }

                if (sampleDate.length() != length) {
                    throw new IllegalArgumentException(
                            String.format(
                                    "Field '%s': dateTimeFormat '%s' produces %d characters, but"
                                            + " length is %d",
                                    fieldName, dateTimeFormat, sampleDate.length(), length));
                }
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        String.format(
                                "Field '%s': invalid dateTimeFormat '%s': %s",
                                fieldName, dateTimeFormat, e.getMessage()),
                        e);
            }
        }
    }
}
