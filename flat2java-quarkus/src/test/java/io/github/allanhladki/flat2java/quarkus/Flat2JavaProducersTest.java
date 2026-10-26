package io.github.allanhladki.flat2java.quarkus;

import static org.junit.jupiter.api.Assertions.*;

import io.github.allanhladki.flat2java.interfaces.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Test class for Flat2JavaProducers to ensure CDI beans are properly created. */
@DisplayName("Flat2JavaProducers")
class Flat2JavaProducersTest {

    @Test
    @DisplayName("Should create field metadata interface bean")
    void testFieldMetadataInterfaceBean() {
        Flat2JavaProducers producers = new Flat2JavaProducers();
        FieldMetadataInterface bean = producers.fieldMetadataInterface();

        assertNotNull(bean);
        assertTrue(bean instanceof FieldMetadataInterface);
    }

    @Test
    @DisplayName("Should create field serialization interface bean")
    void testFieldSerializationInterfaceBean() {
        Flat2JavaProducers producers = new Flat2JavaProducers();
        FieldSerializationInterface bean = producers.fieldSerializationInterface();

        assertNotNull(bean);
        assertTrue(bean instanceof FieldSerializationInterface);
    }

    @Test
    @DisplayName("Should create object serialization interface bean")
    void testObjectSerializationInterfaceBean() {
        Flat2JavaProducers producers = new Flat2JavaProducers();
        ObjectSerializationInterface bean = producers.objectSerializationInterface();

        assertNotNull(bean);
        assertTrue(bean instanceof ObjectSerializationInterface);
    }

    @Test
    @DisplayName("Should create field deserialization interface bean")
    void testFieldDeserializationInterfaceBean() {
        Flat2JavaProducers producers = new Flat2JavaProducers();
        FieldDeserializationInterface bean = producers.fieldDeserializationInterface();

        assertNotNull(bean);
        assertTrue(bean instanceof FieldDeserializationInterface);
    }

    @Test
    @DisplayName("Should create object deserialization interface bean")
    void testObjectDeserializationInterfaceBean() {
        Flat2JavaProducers producers = new Flat2JavaProducers();
        ObjectDeserializationInterface bean = producers.objectDeserializationInterface();

        assertNotNull(bean);
        assertTrue(bean instanceof ObjectDeserializationInterface);
    }

    @Test
    @DisplayName("Should create flat2java factory bean")
    void testFlat2JavaFactoryBean() {
        Flat2JavaProducers producers = new Flat2JavaProducers();
        var factory = producers.flat2JavaFactory();

        assertNotNull(factory);
        assertTrue(factory instanceof io.github.allanhladki.flat2java.factory.Flat2JavaFactory);
    }
}
