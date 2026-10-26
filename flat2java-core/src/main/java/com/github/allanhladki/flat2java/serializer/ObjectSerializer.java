package com.github.allanhladki.flat2java.serializer;

import com.github.allanhladki.flat2java.config.Flat2JavaConfig;
import com.github.allanhladki.flat2java.metadata.FieldMetadata;
import com.github.allanhladki.flat2java.metadata.FieldMetadataExtractor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Utility class for serializing objects to flat file format strings. This class uses field metadata
 * extracted from {@link com.github.allanhladki.flat2java.markers.FlatField} annotations to convert
 * object fields to properly formatted strings.
 *
 * <p>Null handling behavior for field values can be controlled via the environment variable {@code
 * FLAT2JAVA_ALLOW_NULL_VALUES}:
 *
 * <ul>
 *   <li>{@code true} - Serialize null field values to empty strings (default)
 *   <li>{@code false} - Throw exception when null field values are encountered
 * </ul>
 */
public class ObjectSerializer {
    private static final Logger logger = LoggerFactory.getLogger(ObjectSerializer.class);

    /**
     * Serializes an object to a flat file format string using inheritance (includes superclass
     * fields). This is the default behavior.
     *
     * @param object the object to serialize
     * @return the serialized string representation
     * @throws NullPointerException if object is null
     * @throws IllegalArgumentException if the object has no annotated fields
     */
    public static String serialize(Object object) {
        return serialize(object, true);
    }

    /**
     * Serializes an object to a flat file format string with configurable inheritance behavior.
     *
     * @param object the object to serialize
     * @param includeInherited whether to include fields from superclasses
     * @return the serialized string representation
     * @throws NullPointerException if object is null
     * @throws IllegalArgumentException if the object has no annotated fields
     */
    public static String serialize(Object object, boolean includeInherited) {
        Objects.requireNonNull(object, "Object cannot be null");

        logger.debug(
                "Serializing object of type: {} with inheritance: {}",
                object.getClass().getName(),
                includeInherited);

        List<FieldMetadata> fieldMetadataList =
                includeInherited
                        ? FieldMetadataExtractor.extractIncludingInherited(object.getClass())
                        : FieldMetadataExtractor.extract(object.getClass());

        if (fieldMetadataList.isEmpty()) {
            throw new IllegalArgumentException(
                    "No fields annotated with @FlatField found in class: "
                            + object.getClass().getName());
        }

        StringBuilder result = new StringBuilder();

        for (FieldMetadata metadata : fieldMetadataList) {
            String fieldValue = serializeField(object, metadata);
            result.append(fieldValue);
        }

        logger.debug("Serialized object to string of length {}: {}", result.length(), result);
        return result.toString();
    }

    /**
     * Serializes a single field to a string based on its metadata.
     *
     * @param object the object containing the field
     * @param metadata the field metadata
     * @return the serialized field value as a string
     */
    private static String serializeField(Object object, FieldMetadata metadata) {
        Field field = metadata.getField();
        if (field == null) {
            logger.error("Field is null for metadata: {}", metadata);
            throw new RuntimeException(String.format("Field is null for metadata: %s", metadata));
        }

        try {
            field.setAccessible(true);
            Object fieldValue = field.get(object);
            return serializeFieldValue(fieldValue, metadata, field);
        } catch (IllegalAccessException e) {
            logger.error("Failed to access field: {}", field.getName(), e);
            throw new RuntimeException(
                    String.format(
                            "Failed to access field '%s' in class '%s'",
                            field.getName(), object.getClass().getName()),
                    e);
        }
    }

    /**
     * Serializes a field value to a string based on the field metadata. This method handles the
     * conversion of different data types to properly formatted strings.
     *
     * @param fieldValue the value to serialize
     * @param metadata the field metadata containing formatting information
     * @param field the field being serialized
     * @return the serialized field value
     */
    private static String serializeFieldValue(
            Object fieldValue, FieldMetadata metadata, Field field) {
        if (fieldValue == null) {
            if (Flat2JavaConfig.isNullHandlingEnabled()) {
                logger.debug("Null field value for '{}', creating empty field", metadata.getName());
                return serialize(null, metadata);
            }

            logger.error(
                    "Null field value for '{}' but null handling is disabled", metadata.getName());
            throw new NullPointerException(
                    String.format(
                            "Field '%s' cannot be null. Set %s=true to allow null string field"
                                    + " values.",
                            metadata.getName(), Flat2JavaConfig.getNullHandlingEnvVarName()));
        }

        logger.debug(
                "Serializing field '{}' with value '{}' and metadata: {}",
                metadata.getName(),
                fieldValue,
                metadata);

        return serialize(fieldValue, metadata);
    }

    /**
     * Serializes a field value to a string based on its type and metadata.
     *
     * @param fieldValue the value to serialize
     * @param metadata the field metadata containing formatting information
     * @param field the field being serialized
     * @return the serialized field value as a string
     * @throws IllegalArgumentException if the field value cannot be serialized
     */
    public static String serialize(Object fieldValue, FieldMetadata metadata) {
        Objects.requireNonNull(metadata, "FieldMetadata cannot be null");

        if (fieldValue == null && metadata.getType() == SerializationTypes.SupportedTypes.STRING) {
            return FieldSerializer.createEmptyField(metadata);
        } else if (fieldValue == null) {
            throw new NullPointerException(
                    String.format(
                            "Field '%s' cannot be null. Set %s=true to allow null string field"
                                    + " values.",
                            metadata.getName(), Flat2JavaConfig.getNullHandlingEnvVarName()));
        }

        String serializedValue = serializeValue(fieldValue, metadata);
        return FieldSerializer.formatToLength(serializedValue, metadata);
    }

    private static String serializeValue(Object fieldValue, FieldMetadata metadata) {

        Class<?> fieldType = fieldValue.getClass();

        if (fieldValue instanceof String) {
            return FieldSerializer.serialize((String) fieldValue);
        }

        if (fieldValue instanceof Integer) {
            return FieldSerializer.serialize((Integer) fieldValue);
        }

        if (fieldValue instanceof Long) {
            return FieldSerializer.serialize((Long) fieldValue);
        }

        if (fieldValue instanceof BigInteger) {
            return FieldSerializer.serialize((BigInteger) fieldValue);
        }

        if (fieldValue instanceof BigDecimal) {
            return FieldSerializer.serialize((BigDecimal) fieldValue, metadata.getPrecision());
        }

        if (fieldValue instanceof LocalDate) {
            return FieldSerializer.serialize((LocalDate) fieldValue, metadata.getDateTimeFormat());
        }

        if (fieldValue instanceof LocalDateTime) {
            return FieldSerializer.serialize(
                    (LocalDateTime) fieldValue, metadata.getDateTimeFormat());
        }

        throw new IllegalArgumentException(
                String.format(
                        "Unsupported field type: %s for field '%s'. %s",
                        fieldType.getSimpleName(),
                        metadata.getName(),
                        SerializationTypes.getSupportedTypesDescription()));
    }
}
