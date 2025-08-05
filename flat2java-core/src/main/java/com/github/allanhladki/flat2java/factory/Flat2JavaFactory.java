package com.github.allanhladki.flat2java.factory;

import com.github.allanhladki.flat2java.exception.ExceptionTranslationInterceptor;
import com.github.allanhladki.flat2java.impl.FieldMetadataImpl;
import com.github.allanhladki.flat2java.impl.FieldSerializationImpl;
import com.github.allanhladki.flat2java.impl.ObjectSerializationImpl;
import com.github.allanhladki.flat2java.interfaces.FieldMetadataInterface;
import com.github.allanhladki.flat2java.interfaces.FieldSerializationInterface;
import com.github.allanhladki.flat2java.interfaces.ObjectSerializationInterface;

/**
 * Factory class for creating Flat2Java services with exception translation interceptors.
 * This factory provides a convenient way to create properly configured service instances
 * that automatically translate exceptions using the interceptor pattern.
 */
public class Flat2JavaFactory {
    
    private final FieldMetadataInterface fieldMetadataInterface;
    private final FieldSerializationInterface fieldSerializationInterface;
    private final ObjectSerializationInterface objectSerializationInterface;
    
    /**
     * Creates a new Flat2JavaFactory with default configuration.
     */
    public Flat2JavaFactory() {
        this.fieldMetadataInterface = createFieldMetadataInterface();
        this.fieldSerializationInterface = createFieldSerializationInterface();
        this.objectSerializationInterface = createObjectSerializationInterface();
    }
    
    /**
     * Gets the field metadata interface with exception translation.
     * 
     * @return the intercepted field metadata interface
     */
    public FieldMetadataInterface getFieldMetadataInterface() {
        return fieldMetadataInterface;
    }
    
    /**
     * Gets the field serialization interface with exception translation.
     * 
     * @return the intercepted field serialization interface
     */
    public FieldSerializationInterface getFieldSerializationInterface() {
        return fieldSerializationInterface;
    }
    
    /**
     * Gets the object serialization interface with exception translation.
     * 
     * @return the intercepted object serialization interface
     */
    public ObjectSerializationInterface getObjectSerializationInterface() {
        return objectSerializationInterface;
    }
    
    /**
     * Creates a field metadata interface with exception translation interceptor.
     * 
     * @return the intercepted field metadata interface
     */
    private FieldMetadataInterface createFieldMetadataInterface() {
        FieldMetadataImpl impl = new FieldMetadataImpl();
        return ExceptionTranslationInterceptor.intercept(impl, FieldMetadataInterface.class);
    }
    
    /**
     * Creates a field serialization interface with exception translation interceptor.
     * 
     * @return the intercepted field serialization interface
     */
    private FieldSerializationInterface createFieldSerializationInterface() {
        FieldSerializationImpl impl = new FieldSerializationImpl();
        return ExceptionTranslationInterceptor.intercept(impl, FieldSerializationInterface.class);
    }
    
    /**
     * Creates an object serialization interface with exception translation interceptor.
     * 
     * @return the intercepted object serialization interface
     */
    private ObjectSerializationInterface createObjectSerializationInterface() {
        ObjectSerializationImpl impl = new ObjectSerializationImpl();
        return ExceptionTranslationInterceptor.intercept(impl, ObjectSerializationInterface.class);
    }
} 