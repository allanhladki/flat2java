package com.github.allanhladki.flat2java.quarkus;

import com.github.allanhladki.flat2java.factory.Flat2JavaFactory;
import com.github.allanhladki.flat2java.interfaces.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Typed;
import jakarta.inject.Singleton;

/**
 * CDI producers for Flat2Java services in Quarkus applications.
 * 
 * <p>This class provides CDI beans for all Flat2Java interfaces, making them
 * available for dependency injection in Quarkus applications. The services are
 * created using the Flat2JavaFactory and are configured with exception translation.</p>
 * 
 * <p>The following beans are produced:</p>
 * <ul>
 *   <li>{@link FieldMetadataInterface} - Field metadata extraction service</li>
 *   <li>{@link FieldSerializationInterface} - Field serialization service</li>
 *   <li>{@link ObjectSerializationInterface} - Object serialization service</li>
 *   <li>{@link FieldDeserializationInterface} - Field deserialization service</li>
 *   <li>{@link ObjectDeserializationInterface} - Object deserialization service</li>
 * </ul>
 * 
 * <p>Usage example:</p>
 * <pre>
 * @ApplicationScoped
 * public class MyService {
 *     @Inject
 *     private ObjectSerializationInterface serializer;
 *     
 *     @Inject
 *     private ObjectDeserializationInterface deserializer;
 * }
 * </pre>
 * 
 * @since 0.0.1-rc2
 */
@ApplicationScoped
public class Flat2JavaProducers {

    /**
     * Produces the Flat2Java factory as a singleton bean.
     * 
     * @return configured Flat2Java factory
     */
    @Produces
    @Singleton
    @Typed(Flat2JavaFactory.class)
    public Flat2JavaFactory flat2JavaFactory() {
        return new Flat2JavaFactory();
    }

    /**
     * Produces the field metadata interface bean.
     * 
     * @return configured field metadata interface
     */
    @Produces
    @ApplicationScoped
    @Typed(FieldMetadataInterface.class)
    public FieldMetadataInterface fieldMetadataInterface() {
        Flat2JavaFactory factory = new Flat2JavaFactory();
        return factory.getFieldMetadataInterface();
    }

    /**
     * Produces the field serialization interface bean.
     * 
     * @return configured field serialization interface
     */
    @Produces
    @ApplicationScoped
    @Typed(FieldSerializationInterface.class)
    public FieldSerializationInterface fieldSerializationInterface() {
        Flat2JavaFactory factory = new Flat2JavaFactory();
        return factory.getFieldSerializationInterface();
    }

    /**
     * Produces the object serialization interface bean.
     * 
     * @return configured object serialization interface
     */
    @Produces
    @ApplicationScoped
    @Typed(ObjectSerializationInterface.class)
    public ObjectSerializationInterface objectSerializationInterface() {
        Flat2JavaFactory factory = new Flat2JavaFactory();
        return factory.getObjectSerializationInterface();
    }

    /**
     * Produces the field deserialization interface bean.
     * 
     * @return configured field deserialization interface
     */
    @Produces
    @ApplicationScoped
    @Typed(FieldDeserializationInterface.class)
    public FieldDeserializationInterface fieldDeserializationInterface() {
        Flat2JavaFactory factory = new Flat2JavaFactory();
        return factory.getFieldDeserializationInterface();
    }

    /**
     * Produces the object deserialization interface bean.
     * 
     * @return configured object deserialization interface
     */
    @Produces
    @ApplicationScoped
    @Typed(ObjectDeserializationInterface.class)
    public ObjectDeserializationInterface objectDeserializationInterface() {
        Flat2JavaFactory factory = new Flat2JavaFactory();
        return factory.getObjectDeserializationInterface();
    }
}
