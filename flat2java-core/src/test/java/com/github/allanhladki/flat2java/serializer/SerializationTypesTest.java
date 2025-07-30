package com.github.allanhladki.flat2java.serializer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Test class for SerializationTypes utility.
 */
class SerializationTypesTest {

    // Test classes for field type checking
    private static class TestClass {
        @SuppressWarnings("unused")
        public String stringField;
        @SuppressWarnings("unused")
        public Integer integerField;
        @SuppressWarnings("unused")
        public int intPrimitiveField;
        @SuppressWarnings("unused")
        public Long longField;
        @SuppressWarnings("unused")
        public long longPrimitiveField;
        @SuppressWarnings("unused")
        public BigInteger bigIntegerField;
        @SuppressWarnings("unused")
        public BigDecimal bigDecimalField;
        @SuppressWarnings("unused")
        public LocalDate localDateField;
        @SuppressWarnings("unused")
        public LocalDateTime localDateTimeField;
        @SuppressWarnings("unused")
        public Object unsupportedField;
        @SuppressWarnings("unused")
        public List<String> listField;
    }

    @Test
    @DisplayName("Should identify numeric fields correctly")
    void testIsNumericField() throws Exception {
        // Test numeric fields
        assertTrue(SerializationTypes.isNumericField(TestClass.class.getField("integerField")));
        assertTrue(SerializationTypes.isNumericField(TestClass.class.getField("intPrimitiveField")));
        assertTrue(SerializationTypes.isNumericField(TestClass.class.getField("longField")));
        assertTrue(SerializationTypes.isNumericField(TestClass.class.getField("longPrimitiveField")));
        assertTrue(SerializationTypes.isNumericField(TestClass.class.getField("bigIntegerField")));
        assertTrue(SerializationTypes.isNumericField(TestClass.class.getField("bigDecimalField")));

        // Test non-numeric fields
        assertFalse(SerializationTypes.isNumericField(TestClass.class.getField("stringField")));
        assertFalse(SerializationTypes.isNumericField(TestClass.class.getField("localDateField")));
        assertFalse(SerializationTypes.isNumericField(TestClass.class.getField("localDateTimeField")));
        assertFalse(SerializationTypes.isNumericField(TestClass.class.getField("unsupportedField")));
        assertFalse(SerializationTypes.isNumericField(TestClass.class.getField("listField")));
    }

    @Test
    @DisplayName("Should identify numeric types correctly")
    void testIsNumericType() {
        // Test numeric types
        assertTrue(SerializationTypes.isNumericType(Integer.class));
        assertTrue(SerializationTypes.isNumericType(int.class));
        assertTrue(SerializationTypes.isNumericType(Long.class));
        assertTrue(SerializationTypes.isNumericType(long.class));
        assertTrue(SerializationTypes.isNumericType(BigInteger.class));
        assertTrue(SerializationTypes.isNumericType(BigDecimal.class));

        // Test non-numeric types
        assertFalse(SerializationTypes.isNumericType(String.class));
        assertFalse(SerializationTypes.isNumericType(LocalDate.class));
        assertFalse(SerializationTypes.isNumericType(LocalDateTime.class));
        assertFalse(SerializationTypes.isNumericType(Object.class));
        assertFalse(SerializationTypes.isNumericType(List.class));
    }

    @Test
    @DisplayName("Should identify date/time fields correctly")
    void testIsDateTimeField() throws Exception {
        // Test date/time fields
        assertTrue(SerializationTypes.isDateTimeField(TestClass.class.getField("localDateField")));
        assertTrue(SerializationTypes.isDateTimeField(TestClass.class.getField("localDateTimeField")));

        // Test non-date/time fields
        assertFalse(SerializationTypes.isDateTimeField(TestClass.class.getField("stringField")));
        assertFalse(SerializationTypes.isDateTimeField(TestClass.class.getField("integerField")));
        assertFalse(SerializationTypes.isDateTimeField(TestClass.class.getField("bigDecimalField")));
        assertFalse(SerializationTypes.isDateTimeField(TestClass.class.getField("unsupportedField")));
    }

    @Test
    @DisplayName("Should identify date/time types correctly")
    void testIsDateTimeType() {
        // Test date/time types
        assertTrue(SerializationTypes.isDateTimeType(LocalDate.class));
        assertTrue(SerializationTypes.isDateTimeType(LocalDateTime.class));

        // Test non-date/time types
        assertFalse(SerializationTypes.isDateTimeType(String.class));
        assertFalse(SerializationTypes.isDateTimeType(Integer.class));
        assertFalse(SerializationTypes.isDateTimeType(BigDecimal.class));
        assertFalse(SerializationTypes.isDateTimeType(Object.class));
    }

    @Test
    @DisplayName("Should identify string fields correctly")
    void testIsStringField() throws Exception {
        // Test string field
        assertTrue(SerializationTypes.isStringField(TestClass.class.getField("stringField")));

        // Test non-string fields
        assertFalse(SerializationTypes.isStringField(TestClass.class.getField("integerField")));
        assertFalse(SerializationTypes.isStringField(TestClass.class.getField("localDateField")));
        assertFalse(SerializationTypes.isStringField(TestClass.class.getField("unsupportedField")));
    }

    @Test
    @DisplayName("Should identify string types correctly")
    void testIsStringType() {
        // Test string type
        assertTrue(SerializationTypes.isStringType(String.class));

        // Test non-string types
        assertFalse(SerializationTypes.isStringType(Integer.class));
        assertFalse(SerializationTypes.isStringType(LocalDate.class));
        assertFalse(SerializationTypes.isStringType(Object.class));
    }

    @Test
    @DisplayName("Should identify supported fields correctly")
    void testIsSupportedField() throws Exception {
        // Test supported fields
        assertTrue(SerializationTypes.isSupportedField(TestClass.class.getField("stringField")));
        assertTrue(SerializationTypes.isSupportedField(TestClass.class.getField("integerField")));
        assertTrue(SerializationTypes.isSupportedField(TestClass.class.getField("intPrimitiveField")));
        assertTrue(SerializationTypes.isSupportedField(TestClass.class.getField("longField")));
        assertTrue(SerializationTypes.isSupportedField(TestClass.class.getField("longPrimitiveField")));
        assertTrue(SerializationTypes.isSupportedField(TestClass.class.getField("bigIntegerField")));
        assertTrue(SerializationTypes.isSupportedField(TestClass.class.getField("bigDecimalField")));
        assertTrue(SerializationTypes.isSupportedField(TestClass.class.getField("localDateField")));
        assertTrue(SerializationTypes.isSupportedField(TestClass.class.getField("localDateTimeField")));

        // Test unsupported fields
        assertFalse(SerializationTypes.isSupportedField(TestClass.class.getField("unsupportedField")));
        assertFalse(SerializationTypes.isSupportedField(TestClass.class.getField("listField")));
    }

    @Test
    @DisplayName("Should identify supported types correctly")
    void testIsSupportedType() {
        // Test supported types
        assertTrue(SerializationTypes.isSupportedType(String.class));
        assertTrue(SerializationTypes.isSupportedType(Integer.class));
        assertTrue(SerializationTypes.isSupportedType(int.class));
        assertTrue(SerializationTypes.isSupportedType(Long.class));
        assertTrue(SerializationTypes.isSupportedType(long.class));
        assertTrue(SerializationTypes.isSupportedType(BigInteger.class));
        assertTrue(SerializationTypes.isSupportedType(BigDecimal.class));
        assertTrue(SerializationTypes.isSupportedType(LocalDate.class));
        assertTrue(SerializationTypes.isSupportedType(LocalDateTime.class));

        // Test unsupported types
        assertFalse(SerializationTypes.isSupportedType(Object.class));
        assertFalse(SerializationTypes.isSupportedType(List.class));
        assertFalse(SerializationTypes.isSupportedType(ArrayList.class));
        assertFalse(SerializationTypes.isSupportedType(Double.class));
        assertFalse(SerializationTypes.isSupportedType(Float.class));
        assertFalse(SerializationTypes.isSupportedType(Boolean.class));
    }

    @Test
    @DisplayName("Should provide correct supported types description")
    void testGetSupportedTypesDescription() {
        String description = SerializationTypes.getSupportedTypesDescription();
        assertNotNull(description);
        assertTrue(description.contains("String"));
        assertTrue(description.contains("Integer"));
        assertTrue(description.contains("Long"));
        assertTrue(description.contains("BigInteger"));
        assertTrue(description.contains("BigDecimal"));
        assertTrue(description.contains("LocalDate"));
        assertTrue(description.contains("LocalDateTime"));
    }

    @Test
    @DisplayName("Should have correct supported types constants")
    void testSupportedTypesConstants() {
        assertEquals(String.class, SerializationTypes.SupportedTypes.STRING);
        assertEquals(Integer.class, SerializationTypes.SupportedTypes.INTEGER);
        assertEquals(int.class, SerializationTypes.SupportedTypes.INT_PRIMITIVE);
        assertEquals(Long.class, SerializationTypes.SupportedTypes.LONG);
        assertEquals(long.class, SerializationTypes.SupportedTypes.LONG_PRIMITIVE);
        assertEquals(BigInteger.class, SerializationTypes.SupportedTypes.BIG_INTEGER);
        assertEquals(BigDecimal.class, SerializationTypes.SupportedTypes.BIG_DECIMAL);
        assertEquals(LocalDate.class, SerializationTypes.SupportedTypes.LOCAL_DATE);
        assertEquals(LocalDateTime.class, SerializationTypes.SupportedTypes.LOCAL_DATE_TIME);
    }

    @Test
    @DisplayName("Should prevent instantiation of utility class")
    void testUtilityClassInstantiation() {
        assertThrows(IllegalAccessException.class, () -> {
            // Use reflection to try to instantiate the class
            SerializationTypes.class.getDeclaredConstructor().newInstance();
        });
    }

    @Test
    @DisplayName("Should prevent instantiation of SupportedTypes inner class")
    void testSupportedTypesInstantiation() {
        assertThrows(IllegalAccessException.class, () -> {
            // Use reflection to try to instantiate the inner class
            SerializationTypes.SupportedTypes.class.getDeclaredConstructor().newInstance();
        });
    }
} 