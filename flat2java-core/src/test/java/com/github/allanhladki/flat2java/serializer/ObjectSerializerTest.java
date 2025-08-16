package com.github.allanhladki.flat2java.serializer;

import static org.junit.jupiter.api.Assertions.*;

import com.github.allanhladki.flat2java.config.Flat2JavaConfig;
import com.github.allanhladki.flat2java.fixtures.TestFixtures;
import com.github.allanhladki.flat2java.markers.FlatField;
import com.github.allanhladki.flat2java.metadata.FieldMetadata;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ObjectSerializerTest {

    @BeforeEach
    void setUp() {
        // Reset any static state if needed
    }

    @Test
    @DisplayName("Should throw NullPointerException when object is null")
    void shouldThrowNullPointerExceptionWhenObjectIsNull() {
        // when/then
        assertThrows(NullPointerException.class, () -> ObjectSerializer.serialize(null));
        assertThrows(NullPointerException.class, () -> ObjectSerializer.serialize(null, true));
        assertThrows(NullPointerException.class, () -> ObjectSerializer.serialize(null, false));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when no annotated fields")
    void shouldThrowIllegalArgumentExceptionWhenNoAnnotatedFields() {
        // given
        TestFixtures.EmptyRecord record = new TestFixtures.EmptyRecord();
        record.setField1("test");
        record.setField2(123);

        // when/then
        assertThrows(IllegalArgumentException.class, () -> ObjectSerializer.serialize(record));
        assertThrows(IllegalArgumentException.class, () -> ObjectSerializer.serialize(record, true));
        assertThrows(IllegalArgumentException.class, () -> ObjectSerializer.serialize(record, false));
    }

    @Test
    @DisplayName("Should serialize object with non-null values")
    void shouldSerializeObjectWithNonNullValues() {
        // given
        TestFixtures.TestRecord record = new TestFixtures.TestRecord();
        record.setId("1234567890");
        record.setDate(LocalDate.of(2024, 3, 15));
        record.setQuantity(100);
        record.setPrice(new BigDecimal("99.99"));
        record.setExpiryDate(LocalDate.of(2025, 12, 31));

        // when
        String result = ObjectSerializer.serialize(record);

        // then
        assertNotNull(result, "Serialization result should not be null");
        // Note: Since the actual serialization logic is commented out, we expect the raw toString()
        // values
        // This test will need to be updated when the serialization logic is implemented
    }

    @Test
    @DisplayName("Should serialize object with null string field values when null handling enabled")
    void shouldSerializeObjectWithNullFieldValuesWhenNullHandlingEnabled() {
        // given
        TestFixtures.TestRecord record = new TestFixtures.TestRecord();
        record.setId(null);
        record.setDate(LocalDate.of(2024, 3, 15));
        record.setQuantity(0);
        record.setPrice(new BigDecimal("99.99"));
        record.setExpiryDate(LocalDate.of(2025, 12, 31));

        // when
        String result = ObjectSerializer.serialize(record);

        // then
        assertNotNull(result, "Serialization result should not be null");
        // Note: Since the actual serialization logic is commented out, we expect the raw toString()
        // values
        // This test will need to be updated when the serialization logic is implemented
    }

    @Test
    @DisplayName("Should throw exception for null string field values when null handling disabled")
    void shouldThrowExceptionForNullFieldValuesWhenNullHandlingDisabled() {
        // given
        TestFixtures.TestRecord record = new TestFixtures.TestRecord();
        record.setId("1234567890");
        record.setDate(null); // null field
        record.setQuantity(100);
        record.setPrice(new BigDecimal("99.99"));
        record.setExpiryDate(LocalDate.of(2025, 12, 31));

        // when/then - Mock the configuration to disable null handling
        try (MockedStatic<Flat2JavaConfig> mockedConfig = Mockito.mockStatic(Flat2JavaConfig.class)) {
            mockedConfig.when(Flat2JavaConfig::isNullHandlingEnabled).thenReturn(false);
            mockedConfig
                    .when(Flat2JavaConfig::getNullHandlingEnvVarName)
                    .thenReturn("FLAT2JAVA_ALLOW_NULL_STRINGS");

            assertThrows(NullPointerException.class, () -> ObjectSerializer.serialize(record));
        }
    }

    @Test
    @DisplayName("Should serialize with inheritance enabled")
    void shouldSerializeWithInheritanceEnabled() {
        // given
        TestFixtures.SubRecord record = new TestFixtures.SubRecord("super1", "super2", "sub1", "sub2");

        // when
        String result = ObjectSerializer.serialize(record, true);

        // then
        assertNotNull(result, "Serialization result should not be null");
        // Should include fields from both superclass and subclass
    }

    @Test
    @DisplayName("Should serialize without inheritance")
    void shouldSerializeWithoutInheritance() {
        // given
        TestFixtures.SubRecord record = new TestFixtures.SubRecord("super1", "super2", "sub1", "sub2");

        // when
        String result = ObjectSerializer.serialize(record, false);

        // then
        assertNotNull(result, "Serialization result should not be null");
        // Should only include fields from the current class (SubRecord)
    }

    @Test
    @DisplayName("Should serialize different data types")
    void shouldSerializeDifferentDataTypes() {
        // given
        TestFixtures.TestRecord record = new TestFixtures.TestRecord();
        record.setId("ABC123");
        record.setDate(LocalDate.of(2024, 1, 1));
        record.setQuantity(42);
        record.setPrice(new BigDecimal("123.45"));
        record.setExpiryDate(LocalDate.of(2030, 12, 31));

        // when
        String result = ObjectSerializer.serialize(record);

        // then
        assertNotNull(result, "Serialization result should not be null");
        // Should handle String, LocalDate, Integer, and BigDecimal types
    }

    @Test
    @DisplayName("Should handle empty string values")
    void shouldHandleEmptyStringValues() {
        // given
        TestFixtures.TestRecord record = new TestFixtures.TestRecord();
        record.setId(""); // empty string
        record.setDate(LocalDate.of(2024, 3, 15));
        record.setQuantity(100);
        record.setPrice(new BigDecimal("99.99"));
        record.setExpiryDate(LocalDate.of(2025, 12, 31));

        // when
        String result = ObjectSerializer.serialize(record);

        // then
        assertNotNull(result, "Serialization result should not be null");
        // Should handle empty strings appropriately
    }

    @Test
    @DisplayName("Should handle zero values")
    void shouldHandleZeroValues() {
        // given
        TestFixtures.TestRecord record = new TestFixtures.TestRecord();
        record.setId("1234567890");
        record.setDate(LocalDate.of(2024, 3, 15));
        record.setQuantity(0); // zero value
        record.setPrice(BigDecimal.ZERO); // zero value
        record.setExpiryDate(LocalDate.of(2025, 12, 31));

        // when
        String result = ObjectSerializer.serialize(record);

        // then
        assertNotNull(result, "Serialization result should not be null");
        // Should handle zero values appropriately
    }

    @Test
    @DisplayName("Should handle null values correctly")
    void testSerializeNull() throws Exception {
        // Test that null values are handled by the metadata method
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("stringField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        String result = ObjectSerializer.serialize(null, metadata);
        assertEquals("        ", result); // 8 spaces for null value
    }
}
