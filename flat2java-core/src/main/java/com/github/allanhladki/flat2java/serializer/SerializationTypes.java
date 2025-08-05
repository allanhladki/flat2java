package com.github.allanhladki.flat2java.serializer;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Utility class that defines and manages the supported types for serialization.
 * This class provides type checking and categorization methods for the flat2java library.
 */
public final class SerializationTypes {
    
    // Private constructor to prevent instantiation
    private SerializationTypes() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Supported types for serialization in the flat2java library.
     */
    public static final class SupportedTypes {
        // String types
        public static final Class<?> STRING = String.class;
        
        // Numeric types
        public static final Class<?> INTEGER = Integer.class;
        public static final Class<?> INT_PRIMITIVE = int.class;
        public static final Class<?> LONG = Long.class;
        public static final Class<?> LONG_PRIMITIVE = long.class;
        public static final Class<?> BIG_INTEGER = BigInteger.class;
        public static final Class<?> BIG_DECIMAL = BigDecimal.class;
        
        // Date/Time types
        public static final Class<?> LOCAL_DATE = LocalDate.class;
        public static final Class<?> LOCAL_DATE_TIME = LocalDateTime.class;
        
        private SupportedTypes() {
            // Prevent instantiation
        }
    }
    
    /**
     * Determines if a field is numeric based on its type.
     * 
     * @param field the field to check
     * @return true if the field is numeric, false otherwise
     */
    public static boolean isNumericField(Field field) {
        return isNumericType(field.getType());
    }
    
    /**
     * Determines if a class type is numeric.
     * 
     * @param type the class type to check
     * @return true if the type is numeric, false otherwise
     */
    public static boolean isNumericType(Class<?> type) {
        return type == SupportedTypes.INTEGER || type == SupportedTypes.INT_PRIMITIVE ||
               type == SupportedTypes.LONG || type == SupportedTypes.LONG_PRIMITIVE ||
               type == SupportedTypes.BIG_INTEGER ||
               type == SupportedTypes.BIG_DECIMAL;
    }
    
    /**
     * Determines if a field is a date/time type based on its type.
     * 
     * @param field the field to check
     * @return true if the field is a date/time type, false otherwise
     */
    public static boolean isDateTimeField(Field field) {
        return isDateTimeType(field.getType());
    }
    
    /**
     * Determines if a class type is a date/time type.
     * 
     * @param type the class type to check
     * @return true if the type is a date/time type, false otherwise
     */
    public static boolean isDateTimeType(Class<?> type) {
        return type == SupportedTypes.LOCAL_DATE ||
               type == SupportedTypes.LOCAL_DATE_TIME;
    }
    
    /**
     * Determines if a field is a string type based on its type.
     * 
     * @param field the field to check
     * @return true if the field is a string type, false otherwise
     */
    public static boolean isStringField(Field field) {
        return isStringType(field.getType());
    }
    
    /**
     * Determines if a class type is a string type.
     * 
     * @param type the class type to check
     * @return true if the type is a string type, false otherwise
     */
    public static boolean isStringType(Class<?> type) {
        return type == SupportedTypes.STRING;
    }
    
    /**
     * Checks if a type is supported for serialization.
     * 
     * @param type the class type to check
     * @return true if the type is supported, false otherwise
     */
    public static boolean isSupportedType(Class<?> type) {
        return isStringType(type) || isNumericType(type) || isDateTimeType(type);
    }
    
    /**
     * Checks if a field type is supported for serialization.
     * 
     * @param field the field to check
     * @return true if the field type is supported, false otherwise
     */
    public static boolean isSupportedField(Field field) {
        return isSupportedType(field.getType());
    }
    
    /**
     * Gets a human-readable description of the supported types.
     * 
     * @return a string describing all supported types
     */
    public static String getSupportedTypesDescription() {
        return "Supported types: String, Integer/int, Long/long, BigInteger, BigDecimal, LocalDate, LocalDateTime";
    }

    /**
     * Checks if a type name is supported for serialization.
     * This method is designed for annotation processing when Class<?> is not available.
     * 
     * @param typeName the fully qualified type name
     * @return true if the type is supported, false otherwise
     */
    public static boolean isSupportedTypeName(String typeName) {
        return typeName.equals(SupportedTypes.STRING.getName()) ||
               typeName.equals(SupportedTypes.INTEGER.getName()) || typeName.equals(SupportedTypes.INT_PRIMITIVE.getName()) ||
               typeName.equals(SupportedTypes.LONG.getName()) || typeName.equals(SupportedTypes.LONG_PRIMITIVE.getName()) ||
               typeName.equals(SupportedTypes.BIG_INTEGER.getName()) ||
               typeName.equals(SupportedTypes.BIG_DECIMAL.getName()) ||
               typeName.equals(SupportedTypes.LOCAL_DATE.getName()) ||
               typeName.equals(SupportedTypes.LOCAL_DATE_TIME.getName());
    }

    /**
     * Attempts to get a Class<?> from a type name.
     * This method is designed for annotation processing to convert TypeMirror to Class<?>.
     * 
     * @param typeName the fully qualified type name
     * @return the Class<?> if the type is supported, null otherwise
     */
    public static Class<?> getClassFromTypeName(String typeName) {
        try {
            // Handle primitive types
            if (typeName.equals(SupportedTypes.INT_PRIMITIVE.getName())) return SupportedTypes.INT_PRIMITIVE;
            if (typeName.equals(SupportedTypes.LONG_PRIMITIVE.getName())) return SupportedTypes.LONG_PRIMITIVE;
            
            // Handle common types
            if (typeName.equals(SupportedTypes.STRING.getName())) return SupportedTypes.STRING;
            if (typeName.equals(SupportedTypes.INTEGER.getName())) return SupportedTypes.INTEGER;
            if (typeName.equals(SupportedTypes.LONG.getName())) return SupportedTypes.LONG;
            if (typeName.equals(SupportedTypes.BIG_INTEGER.getName())) return SupportedTypes.BIG_INTEGER;
            if (typeName.equals(SupportedTypes.BIG_DECIMAL.getName())) return SupportedTypes.BIG_DECIMAL;
            if (typeName.equals(SupportedTypes.LOCAL_DATE.getName())) return SupportedTypes.LOCAL_DATE;
            if (typeName.equals(SupportedTypes.LOCAL_DATE_TIME.getName())) return SupportedTypes.LOCAL_DATE_TIME;
            
            return null; // Could not determine class
        } catch (Exception e) {
            return null; // Could not determine class
        }
    }


} 