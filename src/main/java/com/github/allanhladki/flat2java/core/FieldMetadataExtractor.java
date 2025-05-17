package com.github.allanhladki.flat2java.core;

import com.github.allanhladki.flat2java.markers.FlatField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for extracting field metadata from classes annotated with {@link FlatField}.
 */
public class FieldMetadataExtractor {
    
    private static final Logger logger = LoggerFactory.getLogger(FieldMetadataExtractor.class);

    /**
     * Extracts field metadata from all fields in the given class that are annotated with {@link FlatField}.
     * The fields are ordered by their position in the flat string.
     *
     * @param clazz the class to extract field metadata from
     * @return a list of FieldMetadata objects, one for each annotated field, ordered by position
     * @throws NullPointerException if clazz is null
     */
    public static List<FieldMetadata> extract(Class<?> clazz) {
        Objects.requireNonNull(clazz, "Class cannot be null");
        logger.debug("Extracting field metadata from class: {}", clazz.getName());
        
        List<FieldMetadata> metadata = Arrays.stream(clazz.getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(FlatField.class))
            .map(field -> new FieldMetadata(field, field.getAnnotation(FlatField.class)))
            .peek(FieldMetadataExtractor::logFieldMetadata)
            .sorted(Comparator.comparingInt(FieldMetadata::getPosition))
            .collect(Collectors.toList());
            
        logger.debug("Found {} annotated fields in class {}", metadata.size(), clazz.getName());
        return metadata;
    }

    /**
     * Extracts field metadata from all fields in the given class and its superclasses
     * that are annotated with {@link FlatField}.
     * The fields are ordered by their position in the flat string.
     *
     * @param clazz the class to extract field metadata from
     * @return a list of FieldMetadata objects, one for each annotated field, ordered by position
     * @throws NullPointerException if clazz is null
     */
    public static List<FieldMetadata> extractIncludingInherited(Class<?> clazz) {
        Objects.requireNonNull(clazz, "Class cannot be null");
        logger.debug("Extracting field metadata from class hierarchy starting with: {}", clazz.getName());
        
        List<FieldMetadata> metadataList = new ArrayList<>();
        Class<?> currentClass = clazz;
        
        // Traverse up the inheritance hierarchy
        while (currentClass != null && currentClass != Object.class) {
            logger.debug("Processing class: {}", currentClass.getName());
            metadataList.addAll(extract(currentClass));
            currentClass = currentClass.getSuperclass();
        }
        
        // Sort the combined list by position
        List<FieldMetadata> sortedMetadata = metadataList.stream()
            .sorted(Comparator.comparingInt(FieldMetadata::getPosition))
            .collect(Collectors.toList());
            
        logger.debug("Found total of {} annotated fields in class hierarchy", sortedMetadata.size());
        return sortedMetadata;
    }

    /**
     * Logs detailed information about a field metadata at TRACE level.
     *
     * @param fieldMetadata the field metadata to log
     */
    private static void logFieldMetadata(FieldMetadata fieldMetadata) {
        logger.trace("Found field: name='{}', position={}, length={}, precision={}, dateTimeFormat='{}', padding='{}'",
            fieldMetadata.getName(),
            fieldMetadata.getPosition(),
            fieldMetadata.getLength(),
            fieldMetadata.getPrecision(),
            fieldMetadata.getDateTimeFormat(),
            fieldMetadata.getPadding());
    }

} 