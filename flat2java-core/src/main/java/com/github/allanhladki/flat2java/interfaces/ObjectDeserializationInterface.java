package com.github.allanhladki.flat2java.interfaces;

/**
 * Interface for deserializing flat file strings into Java objects. This interface uses field
 * metadata extracted from {@link com.github.allanhladki.flat2java.markers.FlatField} annotations to
 * convert string representations back to object fields.
 */
public interface ObjectDeserializationInterface {

    /**
     * Deserializes a flat string into an instance of the specified class, including inherited fields.
     * This is the default behavior.
     *
     * @param flat the flat string to deserialize
     * @param clazz the class to instantiate and populate
     * @return the populated instance
     */
    <T> T deserialize(String flat, Class<T> clazz);

    /**
     * Deserializes a flat string into an instance of the specified class.
     *
     * @param flat the flat string to deserialize
     * @param clazz the class to instantiate and populate
     * @param includeInherited whether to include fields from superclasses
     * @return the populated instance
     */
    <T> T deserialize(String flat, Class<T> clazz, boolean includeInherited);
}
