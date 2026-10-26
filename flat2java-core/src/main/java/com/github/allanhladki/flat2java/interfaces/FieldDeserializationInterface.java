package com.github.allanhladki.flat2java.interfaces;

import com.github.allanhladki.flat2java.metadata.FieldMetadata;

/**
 * Interface for deserializing individual field values from flat file strings. This interface
 * handles the conversion of string representations back to Java objects suitable for fixed-length
 * flat file formats.
 */
public interface FieldDeserializationInterface {

    /**
     * Parses a string slice into the appropriate Java type based on field metadata.
     *
     * @param slice the string slice to parse
     * @param metadata the field metadata containing type and formatting information
     * @return the parsed value. For string fields, all-padding values return empty strings (not
     *     null) to maintain round-trip consistency when null handling is enabled.
     */
    Object parse(String slice, FieldMetadata metadata);
}
