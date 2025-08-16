package com.github.allanhladki.flat2java.deserializer;

import com.github.allanhladki.flat2java.metadata.FieldMetadata;
import com.github.allanhladki.flat2java.metadata.FieldMetadataExtractor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for deserializing flat file strings into Java objects. This reverses the logic used
 * in {@code ObjectSerializer} by slicing the input string using {@link FieldMetadata} and
 * converting each slice to the appropriate field type.
 */
public final class ObjectDeserializer {
    private static final Logger logger = LoggerFactory.getLogger(ObjectDeserializer.class);

    private ObjectDeserializer() {
        // Utility class
    }

    /**
     * Deserializes a flat string into an instance of the specified class, including inherited fields.
     *
     * @param flat the flat string to deserialize
     * @param clazz the class to instantiate and populate
     * @return the populated instance
     */
    public static <T> T deserialize(String flat, Class<T> clazz) {
        return deserialize(flat, clazz, true);
    }

    /**
     * Deserializes a flat string into an instance of the specified class.
     *
     * @param flat the flat string to deserialize
     * @param clazz the class to instantiate and populate
     * @param includeInherited whether to include fields from superclasses
     * @return the populated instance
     */
    public static <T> T deserialize(String flat, Class<T> clazz, boolean includeInherited) {
        Objects.requireNonNull(flat, "Flat string cannot be null");
        Objects.requireNonNull(clazz, "Class cannot be null");

        logger.debug(
                "Deserializing to type: {} (inherited: {}), input length: {}",
                clazz.getName(),
                includeInherited,
                flat.length());

        List<FieldMetadata> fieldMetadataList =
                includeInherited
                        ? FieldMetadataExtractor.extractIncludingInherited(clazz)
                        : FieldMetadataExtractor.extract(clazz);

        if (fieldMetadataList.isEmpty()) {
            throw new IllegalArgumentException(
                    "No fields annotated with @FlatField found in class: " + clazz.getName());
        }

        int expectedLength = fieldMetadataList.stream().mapToInt(FieldMetadata::getLength).sum();
        if (flat.length() != expectedLength) {
            throw new IllegalArgumentException(
                    String.format(
                            "Input length (%d) does not match expected total length (%d) for class %s",
                            flat.length(), expectedLength, clazz.getName()));
        }

        T instance = instantiate(clazz);

        int offset = 0;
        for (FieldMetadata metadata : fieldMetadataList) {
            int len = metadata.getLength();
            String slice = flat.substring(offset, offset + len);
            offset += len;

            Object value = FieldDeserializer.parse(slice, metadata);
            assignField(instance, metadata.getField(), value, metadata.getName());
        }

        logger.debug("Deserialized instance of type {} successfully", clazz.getName());
        return instance;
    }

    private static <T> T instantiate(Class<T> clazz) {
        try {
            Constructor<T> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    String.format("Class '%s' must have a no-arg constructor", clazz.getName()), e);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(
                    String.format(
                            "Class '%s' cannot be instantiated (abstract, interface, or missing no-arg"
                                    + " constructor)",
                            clazz.getName()),
                    e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(
                    String.format("No-arg constructor in class '%s' is not accessible", clazz.getName()), e);
        } catch (Exception e) {
            throw new RuntimeException(
                    String.format(
                            "Failed to instantiate class '%s' with a no-arg constructor", clazz.getName()),
                    e);
        }
    }

    private static void assignField(Object instance, Field field, Object value, String fieldName) {
        try {
            field.setAccessible(true);

            // Type validation - ensure the parsed value matches the field type
            if (value != null && !field.getType().isAssignableFrom(value.getClass())) {
                throw new IllegalArgumentException(
                        String.format(
                                "Field '%s': type mismatch - expected %s but got %s",
                                fieldName, field.getType().getSimpleName(), value.getClass().getSimpleName()));
            }

            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(
                    String.format(
                            "Failed to set field '%s' on class '%s'", fieldName, instance.getClass().getName()),
                    e);
        }
    }
}
