package io.github.allanhladki.flat2java.factory;

import static org.junit.jupiter.api.Assertions.*;

import io.github.allanhladki.flat2java.exception.DeserializationException;
import io.github.allanhladki.flat2java.exception.ExceptionTranslationInterceptor;
import io.github.allanhladki.flat2java.exception.SerializationException;
import io.github.allanhladki.flat2java.fixtures.TestFixtures;
import io.github.allanhladki.flat2java.interfaces.FieldDeserializationInterface;
import io.github.allanhladki.flat2java.interfaces.FieldMetadataInterface;
import io.github.allanhladki.flat2java.interfaces.FieldSerializationInterface;
import io.github.allanhladki.flat2java.interfaces.ObjectDeserializationInterface;
import io.github.allanhladki.flat2java.interfaces.ObjectSerializationInterface;
import io.github.allanhladki.flat2java.metadata.FieldMetadata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

/**
 * Tests for {@link Flat2JavaFactory} to ensure it properly creates intercepted services with
 * exception translation capabilities.
 */
@DisplayName("Flat2JavaFactory")
class Flat2JavaFactoryTest {

    private Flat2JavaFactory factory;

    @BeforeEach
    void setUp() {
        factory = new Flat2JavaFactory();
    }

    @Test
    @DisplayName("Should create field metadata interface with exception translation")
    void testCreateFieldMetadataInterface() {
        FieldMetadataInterface fieldMetadataInterface = factory.getFieldMetadataInterface();

        assertNotNull(fieldMetadataInterface);

        // Test that it's a proxy (intercepted)
        assertTrue(ExceptionTranslationInterceptor.isProxy(fieldMetadataInterface));

        // Test that it works correctly
        List<FieldMetadata> metadata =
                fieldMetadataInterface.extractFieldMetadata(TestFixtures.FactoryTestClass.class);
        assertEquals(1, metadata.size());
        assertEquals("testField", metadata.get(0).getName());
    }

    @Test
    @DisplayName("Should create field serialization interface with exception translation")
    void testCreateFieldSerializationInterface() {
        FieldSerializationInterface fieldSerializationInterface =
                factory.getFieldSerializationInterface();

        assertNotNull(fieldSerializationInterface);

        // Test that it's a proxy (intercepted)
        assertTrue(ExceptionTranslationInterceptor.isProxy(fieldSerializationInterface));

        // Test that it works correctly
        String result = fieldSerializationInterface.serializeString("test");
        assertEquals("test", result);
    }

    @Test
    @DisplayName("Should create object serialization interface with exception translation")
    void testCreateObjectSerializationInterface() {
        ObjectSerializationInterface objectSerializationInterface =
                factory.getObjectSerializationInterface();

        assertNotNull(objectSerializationInterface);

        // Test that it's a proxy (intercepted)
        assertTrue(ExceptionTranslationInterceptor.isProxy(objectSerializationInterface));

        // Test that it works correctly
        TestFixtures.FactoryTestClass testObject = new TestFixtures.FactoryTestClass();
        testObject.testField = "test";
        String result = objectSerializationInterface.serialize(testObject);
        assertEquals("test      ", result); // 10 characters with padding
    }

    @Test
    @DisplayName("Should translate exceptions in field serialization")
    void testExceptionTranslationInFieldSerialization() {
        FieldSerializationInterface fieldSerializationInterface =
                factory.getFieldSerializationInterface();

        // Test null value translation - should now throw SerializationException
        SerializationException exception =
                assertThrows(
                        SerializationException.class,
                        () -> {
                            fieldSerializationInterface.serializeString(null);
                        });

        assertTrue(exception.getMessage().contains("serializing String value"));
        assertInstanceOf(NullPointerException.class, exception.getCause());
    }

    @Test
    @DisplayName("Should translate exceptions in object serialization")
    void testExceptionTranslationInObjectSerialization() {
        ObjectSerializationInterface objectSerializationInterface =
                factory.getObjectSerializationInterface();

        // Test null object translation - should now throw SerializationException
        SerializationException exception =
                assertThrows(
                        SerializationException.class,
                        () -> {
                            objectSerializationInterface.serialize(null);
                        });

        assertTrue(exception.getMessage().contains("serializing object"));
        assertInstanceOf(NullPointerException.class, exception.getCause());
    }

    @Test
    @DisplayName("Should handle BigDecimal serialization with precision")
    void testBigDecimalSerialization() {
        FieldSerializationInterface fieldSerializationInterface =
                factory.getFieldSerializationInterface();

        // Use a value that doesn't require rounding with UNNECESSARY mode
        BigDecimal value = new BigDecimal("12.34");
        String result = fieldSerializationInterface.serializeBigDecimal(value, 2);

        assertNotNull(result);
        // The exact result depends on configuration, but it should not throw an exception
    }

    @Test
    @DisplayName("Should handle field metadata extraction with inheritance")
    void testFieldMetadataExtractionWithInheritance() {
        FieldMetadataInterface fieldMetadataInterface = factory.getFieldMetadataInterface();

        List<FieldMetadata> metadata =
                fieldMetadataInterface.extractFieldMetadataIncludingInherited(
                        TestFixtures.FactoryTestClass.class);

        assertNotNull(metadata);
        assertEquals(1, metadata.size());
        assertEquals("testField", metadata.get(0).getName());
    }

    @Test
    @DisplayName("Should create field deserialization interface with exception translation")
    void testCreateFieldDeserializationInterface() {
        FieldDeserializationInterface fieldDeserializationInterface =
                factory.getFieldDeserializationInterface();

        assertNotNull(fieldDeserializationInterface);

        // Test that it's a proxy (intercepted)
        assertTrue(ExceptionTranslationInterceptor.isProxy(fieldDeserializationInterface));

        // Test that it works correctly - we need a valid FieldMetadata to test parsing
        // For now, just verify the interface is created and intercepted
        assertNotNull(fieldDeserializationInterface);
    }

    @Test
    @DisplayName("Should create object deserialization interface with exception translation")
    void testCreateObjectDeserializationInterface() {
        ObjectDeserializationInterface objectDeserializationInterface =
                factory.getObjectDeserializationInterface();

        assertNotNull(objectDeserializationInterface);

        // Test that it's a proxy (intercepted)
        assertTrue(ExceptionTranslationInterceptor.isProxy(objectDeserializationInterface));

        // Test that it works correctly
        TestFixtures.FactoryTestClass result =
                objectDeserializationInterface.deserialize(
                        "test      ", TestFixtures.FactoryTestClass.class);
        assertNotNull(result);
        assertEquals("test", result.testField);
    }

    @Test
    @DisplayName("Should translate exceptions in field deserialization")
    void testExceptionTranslationInFieldDeserialization() {
        FieldDeserializationInterface fieldDeserializationInterface =
                factory.getFieldDeserializationInterface();

        // Test null slice translation - should throw DeserializationException
        DeserializationException exception =
                assertThrows(
                        DeserializationException.class,
                        () -> {
                            fieldDeserializationInterface.parse(null, null);
                        });

        assertTrue(exception.getMessage().contains("parsing field value"));
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }

    @Test
    @DisplayName("Should translate exceptions in object deserialization")
    void testExceptionTranslationInObjectDeserialization() {
        ObjectDeserializationInterface objectDeserializationInterface =
                factory.getObjectDeserializationInterface();

        // Test null flat string translation - should throw DeserializationException
        DeserializationException exception =
                assertThrows(
                        DeserializationException.class,
                        () -> {
                            objectDeserializationInterface.deserialize(
                                    null, TestFixtures.FactoryTestClass.class);
                        });

        assertTrue(exception.getMessage().contains("deserializing object"));
        assertInstanceOf(NullPointerException.class, exception.getCause());
    }

    @Test
    @DisplayName("Should handle object deserialization with inheritance")
    void testObjectDeserializationWithInheritance() {
        ObjectDeserializationInterface objectDeserializationInterface =
                factory.getObjectDeserializationInterface();

        TestFixtures.FactoryTestClass result =
                objectDeserializationInterface.deserialize(
                        "test      ", TestFixtures.FactoryTestClass.class, false);

        assertNotNull(result);
        assertEquals("test", result.testField);
    }
}
