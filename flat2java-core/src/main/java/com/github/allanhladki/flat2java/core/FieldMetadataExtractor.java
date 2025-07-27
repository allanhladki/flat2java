package com.github.allanhladki.flat2java.core;

import com.github.allanhladki.flat2java.markers.FlatField;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for extracting field metadata from classes annotated with {@link FlatField}.
 */
public class FieldMetadataExtractor {
    private static final Logger logger = LoggerFactory.getLogger(FieldMetadataExtractor.class);

    /**
     * Extracts field metadata from all fields in the given class that are annotated with {@link FlatField}.
     * The fields are ordered by their declaration order in the class.
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
                metadata.add(new FieldMetadata(field, field.getAnnotation(FlatField.class)));
            }
        }
        logger.debug("Found {} annotated fields in class {}", metadata.size(), clazz.getName());
        return metadata;
    }

    /**
     * Extracts field metadata from all fields in the given class and its superclasses
     * that are annotated with {@link FlatField}.
     * The fields are ordered by their declaration order, superclass fields first.
     *
     * @param clazz the class to extract field metadata from
     * @return a list of FieldMetadata objects, one for each annotated field, in declaration order
     * @throws NullPointerException if clazz is null
     */
    public static List<FieldMetadata> extractIncludingInherited(Class<?> clazz) {
        Objects.requireNonNull(clazz, "Class cannot be null");
        logger.debug("Extracting field metadata from class hierarchy starting with: {}", clazz.getName());

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
} 