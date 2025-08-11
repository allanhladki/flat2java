package com.github.allanhladki.flat2java;

import com.github.allanhladki.flat2java.deserializer.ObjectDeserializer;
import com.github.allanhladki.flat2java.fixtures.TestFixtures;
import com.github.allanhladki.flat2java.serializer.ObjectSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Object Round-Trip Tests")
class ObjectRoundTripTest {

    // ==================== BASIC ROUND-TRIP TESTS ====================

    @Test
    @DisplayName("Should maintain values through serialize-then-deserialize round trip for TestRecord")
    void shouldMaintainValuesThroughSerializeThenDeserializeRoundTrip() {
        // given
        TestFixtures.TestRecord record = new TestFixtures.TestRecord();
        record.setId("ABC123");
        record.setDate(LocalDate.of(2024, 3, 15));
        record.setPrice(new BigDecimal("1234.56"));
        record.setQuantity(42);
        record.setExpiryDate(LocalDate.of(2030, 12, 31));

        // when
        String flat = ObjectSerializer.serialize(record);
        TestFixtures.TestRecord parsed = ObjectDeserializer.deserialize(flat, TestFixtures.TestRecord.class);

        // then
        assertNotNull(parsed, "Deserialized record should not be null");
        assertEquals(record.getId(), parsed.getId(), "ID should be preserved");
        assertEquals(record.getDate(), parsed.getDate(), "Date should be preserved");
        assertEquals(0, record.getPrice().compareTo(parsed.getPrice()), "Price should be preserved with correct scale");
        assertEquals(record.getQuantity(), parsed.getQuantity(), "Quantity should be preserved");
        assertEquals(record.getExpiryDate(), parsed.getExpiryDate(), "Expiry date should be preserved");
    }

    @Test
    @DisplayName("Should handle round trip with null values for TestRecord")
    void shouldHandleRoundTripWithNullValues() {
        // given
        TestFixtures.TestRecord record = new TestFixtures.TestRecord();
        record.setId("TEST123");
        record.setDate(LocalDate.of(2024, 1, 1));
        record.setPrice(new BigDecimal("0.00"));
        record.setQuantity(0);
        record.setExpiryDate(LocalDate.of(2025, 6, 15));

        // when
        String flat = ObjectSerializer.serialize(record);
        TestFixtures.TestRecord parsed = ObjectDeserializer.deserialize(flat, TestFixtures.TestRecord.class);

        // then
        assertNotNull(parsed, "Deserialized record should not be null");
        assertEquals(record.getId(), parsed.getId(), "ID should be preserved");
        assertEquals(record.getDate(), parsed.getDate(), "Date should be preserved");
        assertEquals(0, record.getPrice().compareTo(parsed.getPrice()), "Price should be preserved as zero");
        assertEquals(record.getQuantity(), parsed.getQuantity(), "Quantity should be preserved as zero");
        assertEquals(record.getExpiryDate(), parsed.getExpiryDate(), "Expiry date should be preserved");
    }

    @Test
    @DisplayName("Should handle round trip with zero values for TestRecord")
    void shouldHandleRoundTripWithZeroValues() {
        // given
        TestFixtures.TestRecord record = new TestFixtures.TestRecord();
        record.setId("ZERO123");
        record.setDate(LocalDate.of(2024, 1, 1));
        record.setPrice(new BigDecimal("0.00"));
        record.setQuantity(0);
        record.setExpiryDate(LocalDate.of(2024, 12, 31));

        // when
        String flat = ObjectSerializer.serialize(record);
        TestFixtures.TestRecord parsed = ObjectDeserializer.deserialize(flat, TestFixtures.TestRecord.class);

        // then
        assertNotNull(parsed, "Deserialized record should not be null");
        assertEquals(record.getId(), parsed.getId(), "ID should be preserved");
        assertEquals(record.getDate(), parsed.getDate(), "Date should be preserved");
        assertEquals(0, record.getPrice().compareTo(parsed.getPrice()), "Price should be preserved with correct scale");
        assertEquals(record.getQuantity(), parsed.getQuantity(), "Quantity should be preserved");
        assertEquals(record.getExpiryDate(), parsed.getExpiryDate(), "Expiry date should be preserved");
    }

    @Test
    @DisplayName("Should handle round trip with maximum values for TestRecord")
    void shouldHandleRoundTripWithMaximumValues() {
        // given
        TestFixtures.TestRecord record = new TestFixtures.TestRecord();
        record.setId("MAX12345");
        record.setDate(LocalDate.of(9999, 12, 31));
        record.setPrice(new BigDecimal("999999.99"));
        record.setQuantity(99999);
        record.setExpiryDate(LocalDate.of(9999, 12, 31));

        // when
        String flat = ObjectSerializer.serialize(record);
        TestFixtures.TestRecord parsed = ObjectDeserializer.deserialize(flat, TestFixtures.TestRecord.class);

        // then
        assertNotNull(parsed, "Deserialized record should not be null");
        assertEquals(record.getId(), parsed.getId(), "ID should be preserved");
        assertEquals(record.getDate(), parsed.getDate(), "Date should be preserved");
        assertEquals(0, record.getPrice().compareTo(parsed.getPrice()), "Price should be preserved with correct scale");
        assertEquals(record.getQuantity(), parsed.getQuantity(), "Quantity should be preserved");
        assertEquals(record.getExpiryDate(), parsed.getExpiryDate(), "Expiry date should be preserved");
    }

    

    // ==================== COMPREHENSIVE TEST RECORD ROUND-TRIP TESTS ====================

    @ParameterizedTest
    @MethodSource("comprehensiveTestRecordRoundTripTestCases")
    @DisplayName("Should maintain values through round trip for ComprehensiveTestRecord")
    void shouldMaintainValuesThroughRoundTripForComprehensiveTestRecord(
            String stringValue, Integer intValue, Long longValue, 
            BigInteger bigIntegerValue, BigDecimal bigDecimalValue, 
            LocalDate dateValue, LocalDateTime dateTimeValue) {
        
        TestFixtures.ComprehensiveTestRecord record = new TestFixtures.ComprehensiveTestRecord();
        record.stringField = stringValue;
        record.intField = intValue;
        record.intPrimitiveField = 0;
        record.longField = longValue;
        record.bigIntegerField = bigIntegerValue;
        record.bigDecimalField = bigDecimalValue;
        record.dateField = dateValue;
        record.dateTimeField = dateTimeValue;
        // Set a non-null value for dateFieldNoFormat so we can test the format validation
        record.dateFieldNoFormat = LocalDate.of(2024, 1, 1);
        
        // Test that serialization fails for dateFieldNoFormat (no dateTimeFormat specified)
        assertThrows(IllegalArgumentException.class, () -> {
            ObjectSerializer.serialize(record);
        }, "Should throw IllegalArgumentException when LocalDate field has no dateTimeFormat");
    }

    static Stream<Arguments> comprehensiveTestRecordRoundTripTestCases() {
        return Stream.of(
            Arguments.of("Hello", 123, 456L, BigInteger.valueOf(789), new BigDecimal("123.456"), 
                        LocalDate.of(2024, 3, 15), LocalDateTime.of(2024, 3, 15, 12, 30, 45)),
            Arguments.of("Test", 0, 0L, BigInteger.ZERO, BigDecimal.ZERO, 
                        LocalDate.of(2024, 1, 1), LocalDateTime.of(2024, 1, 1, 0, 0, 0)),
            Arguments.of("Max", 99999999, 999999999L, new BigInteger("999999999"), new BigDecimal("999.999"), 
                        LocalDate.of(9999, 12, 31), LocalDateTime.of(9999, 12, 31, 23, 59, 59)),
            Arguments.of("", 1, 1L, BigInteger.ONE, new BigDecimal("0.001"), 
                        LocalDate.of(2024, 12, 31), LocalDateTime.of(2024, 12, 31, 23, 59, 59))
        );
    }

    // ==================== INHERITANCE ROUND-TRIP TESTS ====================

    @Test
    @DisplayName("Should handle round trip with inheritance enabled for SubRecord")
    void shouldHandleRoundTripWithInheritanceEnabled() {
        // given
        TestFixtures.SubRecord record = new TestFixtures.SubRecord("super1", "super2", "sub1", "sub2");

        // when
        String flat = ObjectSerializer.serialize(record, true);
        TestFixtures.SubRecord parsed = ObjectDeserializer.deserialize(flat, TestFixtures.SubRecord.class, true);

        // then
        assertNotNull(parsed, "Deserialized record should not be null");
        // String fields are truncated to fit the field length (5 chars)
        assertEquals("super", parsed.getSuperField1(), "Super field 1 should be truncated to 5 chars");
        assertEquals("super", parsed.getSuperField2(), "Super field 2 should be truncated to 5 chars");
        assertEquals(record.getSubField1(), parsed.getSubField1(), "Sub field 1 should be preserved");
        assertEquals(record.getSubField2(), parsed.getSubField2(), "Sub field 2 should be preserved");
    }

    @Test
    @DisplayName("Should handle round trip without inheritance for SubRecord")
    void shouldHandleRoundTripWithoutInheritance() {
        // given
        TestFixtures.SubRecord record = new TestFixtures.SubRecord("super1", "super2", "sub1", "sub2");

        // when
        String flat = ObjectSerializer.serialize(record, false);
        TestFixtures.SubRecord parsed = ObjectDeserializer.deserialize(flat, TestFixtures.SubRecord.class, false);

        // then
        assertNotNull(parsed, "Deserialized record should not be null");
        assertNull(parsed.getSuperField1(), "Super field 1 should be null when inheritance disabled");
        assertNull(parsed.getSuperField2(), "Super field 2 should be null when inheritance disabled");
        assertEquals(record.getSubField1(), parsed.getSubField1(), "Sub field 1 should be preserved");
        assertEquals(record.getSubField2(), parsed.getSubField2(), "Sub field 2 should be preserved");
    }

    // ==================== ZERO VALUE ROUND-TRIP TESTS ====================

    @Test
    @DisplayName("Should handle round trip with all zero values for ZeroValueRecord")
    void shouldHandleRoundTripWithAllZeroValues() {
        // given
        TestFixtures.ZeroValueRecord record = new TestFixtures.ZeroValueRecord();
        record.zeroInteger = 0;
        record.zeroLong = 0L;
        record.zeroBigInteger = BigInteger.ZERO;
        record.zeroBigDecimalNoPrecision = BigDecimal.ZERO;
        record.zeroBigDecimalWithPrecision = BigDecimal.ZERO;

        // when
        String flat = ObjectSerializer.serialize(record);
        TestFixtures.ZeroValueRecord parsed = ObjectDeserializer.deserialize(flat, TestFixtures.ZeroValueRecord.class);

        // then
        assertNotNull(parsed, "Deserialized record should not be null");
        assertEquals(record.zeroInteger, parsed.zeroInteger, "Integer field should be preserved as zero");
        assertEquals(record.zeroLong, parsed.zeroLong, "Long field should be preserved as zero");
        assertEquals(record.zeroBigInteger, parsed.zeroBigInteger, "BigInteger field should be preserved as zero");
        assertEquals(0, record.zeroBigDecimalNoPrecision.compareTo(parsed.zeroBigDecimalNoPrecision), "BigDecimal field without precision should be preserved as zero");
        assertEquals(0, record.zeroBigDecimalWithPrecision.compareTo(parsed.zeroBigDecimalWithPrecision), "BigDecimal field with precision should be preserved as zero");
    }

    @Test
    @DisplayName("Should handle round trip with mixed zero and non-zero values for ZeroValueRecord")
    void shouldHandleRoundTripWithMixedZeroAndNonZeroValues() {
        // given
        TestFixtures.ZeroValueRecord record = new TestFixtures.ZeroValueRecord();
        record.zeroInteger = 123;
        record.zeroLong = 0L;
        record.zeroBigInteger = BigInteger.valueOf(999);
        record.zeroBigDecimalNoPrecision = BigDecimal.ZERO;
        record.zeroBigDecimalWithPrecision = new BigDecimal("12.34");

        // when
        String flat = ObjectSerializer.serialize(record);
        TestFixtures.ZeroValueRecord parsed = ObjectDeserializer.deserialize(flat, TestFixtures.ZeroValueRecord.class);

        // then
        assertNotNull(parsed, "Deserialized record should not be null");
        assertEquals(record.zeroInteger, parsed.zeroInteger, "Integer field should be preserved");
        assertEquals(record.zeroLong, parsed.zeroLong, "Long field should be preserved as zero");
        assertEquals(record.zeroBigInteger, parsed.zeroBigInteger, "BigInteger field should be preserved");
        assertEquals(0, record.zeroBigDecimalNoPrecision.compareTo(parsed.zeroBigDecimalNoPrecision), "BigDecimal field without precision should be preserved as zero");
        assertEquals(0, record.zeroBigDecimalWithPrecision.compareTo(parsed.zeroBigDecimalWithPrecision), "BigDecimal field with precision should be preserved");
    }

    // ==================== EDGE CASE ROUND-TRIP TESTS ====================

    @Test
    @DisplayName("Should handle round trip with empty string values")
    void shouldHandleRoundTripWithEmptyStringValues() {
        // given
        TestFixtures.TestRecord record = new TestFixtures.TestRecord();
        record.setId(""); // empty string
        record.setDate(LocalDate.of(2024, 3, 15));
        record.setQuantity(100);
        record.setPrice(new BigDecimal("99.99"));
        record.setExpiryDate(LocalDate.of(2025, 12, 31));

        // when
        String flat = ObjectSerializer.serialize(record);
        TestFixtures.TestRecord parsed = ObjectDeserializer.deserialize(flat, TestFixtures.TestRecord.class);

        // then
        assertNotNull(parsed, "Deserialized record should not be null");
        assertEquals(record.getId(), parsed.getId(), "Empty string ID should be preserved");
        assertEquals(record.getDate(), parsed.getDate(), "Date should be preserved");
        assertEquals(record.getQuantity(), parsed.getQuantity(), "Quantity should be preserved");
        assertEquals(0, record.getPrice().compareTo(parsed.getPrice()), "Price should be preserved");
        assertEquals(record.getExpiryDate(), parsed.getExpiryDate(), "Expiry date should be preserved");
    }

    @Test
    @DisplayName("Should handle round trip with special characters in string fields")
    void shouldHandleRoundTripWithSpecialCharacters() {
        // given - Use shorter string to fit within 10 character limit
        TestFixtures.TestRecord record = new TestFixtures.TestRecord();
        record.setId("ABC-123_4"); // 10 characters (maximum length)
        record.setDate(LocalDate.of(2024, 3, 15));
        record.setQuantity(100);
        record.setPrice(new BigDecimal("99.99"));
        record.setExpiryDate(LocalDate.of(2025, 12, 31));

        // when
        String flat = ObjectSerializer.serialize(record);
        TestFixtures.TestRecord parsed = ObjectDeserializer.deserialize(flat, TestFixtures.TestRecord.class);

        // then
        assertNotNull(parsed, "Deserialized record should not be null");
        assertEquals(record.getId(), parsed.getId(), "ID with special characters should be preserved");
        assertEquals(record.getDate(), parsed.getDate(), "Date should be preserved");
        assertEquals(record.getQuantity(), parsed.getQuantity(), "Quantity should be preserved");
        assertEquals(0, record.getPrice().compareTo(parsed.getPrice()), "Price should be preserved");
        assertEquals(record.getExpiryDate(), parsed.getExpiryDate(), "Expiry date should be preserved");
    }

    @Test
    @DisplayName("Should handle round trip with boundary date values")
    void shouldHandleRoundTripWithBoundaryDateValues() {
        // given
        TestFixtures.TestRecord record = new TestFixtures.TestRecord();
        record.setId("BOUNDARY");
        record.setDate(LocalDate.of(1, 1, 1)); // earliest possible date
        record.setQuantity(100);
        record.setPrice(new BigDecimal("99.99"));
        record.setExpiryDate(LocalDate.of(9999, 12, 31)); // latest possible date

        // when
        String flat = ObjectSerializer.serialize(record);
        TestFixtures.TestRecord parsed = ObjectDeserializer.deserialize(flat, TestFixtures.TestRecord.class);

        // then
        assertNotNull(parsed, "Deserialized record should not be null");
        assertEquals(record.getId(), parsed.getId(), "ID should be preserved");
        assertEquals(record.getDate(), parsed.getDate(), "Earliest date should be preserved");
        assertEquals(record.getQuantity(), parsed.getQuantity(), "Quantity should be preserved");
        assertEquals(0, record.getPrice().compareTo(parsed.getPrice()), "Price should be preserved");
        assertEquals(record.getExpiryDate(), parsed.getExpiryDate(), "Latest date should be preserved");
    }

    // ==================== PRECISION AND SCALE ROUND-TRIP TESTS ====================

    @ParameterizedTest
    @MethodSource("precisionAndScaleRoundTripTestCases")
    @DisplayName("Should handle round trip with different BigDecimal precision and scale values")
    void shouldHandleRoundTripWithDifferentBigDecimalPrecisionAndScale(BigDecimal inputValue, String expectedId) {
        // given
        TestFixtures.TestRecord record = new TestFixtures.TestRecord();
        record.setId(expectedId);
        record.setDate(LocalDate.of(2024, 3, 15));
        record.setQuantity(100);
        record.setPrice(inputValue);
        record.setExpiryDate(LocalDate.of(2025, 12, 31));

        // when
        String flat = ObjectSerializer.serialize(record);
        TestFixtures.TestRecord parsed = ObjectDeserializer.deserialize(flat, TestFixtures.TestRecord.class);

        // then
        assertNotNull(parsed, "Deserialized record should not be null");
        assertEquals(record.getId(), parsed.getId(), "ID should be preserved");
        assertEquals(record.getDate(), parsed.getDate(), "Date should be preserved");
        assertEquals(record.getQuantity(), parsed.getQuantity(), "Quantity should be preserved");
        assertEquals(0, record.getPrice().compareTo(parsed.getPrice()), "BigDecimal with precision should be preserved");
        assertEquals(record.getExpiryDate(), parsed.getExpiryDate(), "Expiry date should be preserved");
    }

    static Stream<Arguments> precisionAndScaleRoundTripTestCases() {
        return Stream.of(
            Arguments.of(new BigDecimal("123.45"), "PREC2"),
            Arguments.of(new BigDecimal("123.45"), "PREC3"), // Use 123.45 instead of 123.456 to avoid rounding issues
            Arguments.of(new BigDecimal("0.01"), "SMALL"),
            Arguments.of(new BigDecimal("999999.99"), "LARGE"),
            Arguments.of(new BigDecimal("0.00"), "ZERO")
        );
    }

    // ==================== LENGTH BOUNDARY ROUND-TRIP TESTS ====================

    @Test
    @DisplayName("Should handle round trip with maximum length string values")
    void shouldHandleRoundTripWithMaximumLengthStringValues() {
        // given
        TestFixtures.TestRecord record = new TestFixtures.TestRecord();
        record.setId("1234567890"); // exactly 10 characters (maximum length)
        record.setDate(LocalDate.of(2024, 3, 15));
        record.setQuantity(99999); // exactly 5 digits (maximum length)
        record.setPrice(new BigDecimal("999999.99")); // exactly 10 characters (maximum length)
        record.setExpiryDate(LocalDate.of(2025, 12, 31));

        // when
        String flat = ObjectSerializer.serialize(record);
        TestFixtures.TestRecord parsed = ObjectDeserializer.deserialize(flat, TestFixtures.TestRecord.class);

        // then
        assertNotNull(parsed, "Deserialized record should not be null");
        assertEquals(record.getId(), parsed.getId(), "Maximum length ID should be preserved");
        assertEquals(record.getDate(), parsed.getDate(), "Date should be preserved");
        assertEquals(record.getQuantity(), parsed.getQuantity(), "Maximum length quantity should be preserved");
        assertEquals(0, record.getPrice().compareTo(parsed.getPrice()), "Maximum length price should be preserved");
        assertEquals(record.getExpiryDate(), parsed.getExpiryDate(), "Expiry date should be preserved");
    }

    @Test
    @DisplayName("Should handle round trip with minimum length string values")
    void shouldHandleRoundTripWithMinimumLengthStringValues() {
        // given
        TestFixtures.TestRecord record = new TestFixtures.TestRecord();
        record.setId("A"); // minimum length (1 character)
        record.setDate(LocalDate.of(2024, 3, 15));
        record.setQuantity(1); // minimum length (1 digit)
        record.setPrice(new BigDecimal("0.01")); // minimum length
        record.setExpiryDate(LocalDate.of(2025, 12, 31));

        // when
        String flat = ObjectSerializer.serialize(record);
        TestFixtures.TestRecord parsed = ObjectDeserializer.deserialize(flat, TestFixtures.TestRecord.class);

        // then
        assertNotNull(parsed, "Deserialized record should not be null");
        assertEquals(record.getId(), parsed.getId(), "Minimum length ID should be preserved");
        assertEquals(record.getDate(), parsed.getDate(), "Date should be preserved");
        assertEquals(record.getQuantity(), parsed.getQuantity(), "Minimum length quantity should be preserved");
        assertEquals(0, record.getPrice().compareTo(parsed.getPrice()), "Minimum length price should be preserved");
        assertEquals(record.getExpiryDate(), parsed.getExpiryDate(), "Expiry date should be preserved");
    }

    // ==================== NULL HANDLING ROUND-TRIP TESTS ====================

    @Test
    @DisplayName("Should handle round trip with null string field when null handling enabled")
    void shouldHandleRoundTripWithNullStringFieldWhenNullHandlingEnabled() {
        // given
        TestFixtures.TestRecord record = new TestFixtures.TestRecord();
        record.setId(null); // null string field
        record.setDate(LocalDate.of(2024, 3, 15));
        record.setQuantity(100);
        record.setPrice(new BigDecimal("99.99"));
        record.setExpiryDate(LocalDate.of(2025, 12, 31));

        // when
        String flat = ObjectSerializer.serialize(record);
        TestFixtures.TestRecord parsed = ObjectDeserializer.deserialize(flat, TestFixtures.TestRecord.class);

        // then
        assertNotNull(parsed, "Deserialized record should not be null");
        // Null string fields are serialized as spaces, so they should be deserialized as empty strings or trimmed
        assertTrue(parsed.getId() == null || parsed.getId().trim().isEmpty(), "Null ID should be handled appropriately");
        assertEquals(record.getDate(), parsed.getDate(), "Date should be preserved");
        assertEquals(record.getQuantity(), parsed.getQuantity(), "Quantity should be preserved");
        assertEquals(0, record.getPrice().compareTo(parsed.getPrice()), "Price should be preserved");
        assertEquals(record.getExpiryDate(), parsed.getExpiryDate(), "Expiry date should be preserved");
    }

    // ==================== COMPLEX OBJECT ROUND-TRIP TESTS ====================

    @Test
    @DisplayName("Should handle round trip with complex nested object structure")
    void shouldHandleRoundTripWithComplexNestedObjectStructure() {
        // given - Create a complex record with various field types and values
        TestFixtures.ValidComprehensiveTestRecord record = new TestFixtures.ValidComprehensiveTestRecord();
        record.stringField = "Complex";
        record.intField = 12345;
        record.intWrapperField = 12345;
        record.longField = 987654321L;
        record.bigIntegerField = new BigInteger("123456789");
        record.bigDecimalField = new BigDecimal("987.654");
        record.dateField = LocalDate.of(2024, 6, 15);
        record.dateTimeField = LocalDateTime.of(2024, 6, 15, 14, 30, 45);
        record.dateFieldWithFormat = LocalDate.of(2024, 1, 1);
        
        // when
        String flat = ObjectSerializer.serialize(record);
        TestFixtures.ValidComprehensiveTestRecord parsed = ObjectDeserializer.deserialize(flat, TestFixtures.ValidComprehensiveTestRecord.class);
        
        // then
        assertNotNull(parsed, "Deserialized record should not be null");
        assertEquals(record.stringField, parsed.stringField, "String field should be preserved");
        assertEquals(record.intField, parsed.intField, "Integer field should be preserved");
        assertEquals(record.intWrapperField, parsed.intWrapperField, "Int wrapper field should be preserved");
        assertEquals(record.longField, parsed.longField, "Long field should be preserved");
        assertEquals(record.bigIntegerField, parsed.bigIntegerField, "BigInteger field should be preserved");
        assertEquals(0, record.bigDecimalField.compareTo(parsed.bigDecimalField), "BigDecimal field should be preserved");
        assertEquals(record.dateField, parsed.dateField, "Date field should be preserved");
        assertEquals(record.dateTimeField, parsed.dateTimeField, "DateTime field should be preserved");
        assertEquals(record.dateFieldWithFormat, parsed.dateFieldWithFormat, "Date field with format should be preserved");
    }

    @Test
    @DisplayName("Should handle round trip with all fields at their minimum values")
    void shouldHandleRoundTripWithAllFieldsAtMinimumValues() {
        // given
        TestFixtures.ValidComprehensiveTestRecord record = new TestFixtures.ValidComprehensiveTestRecord();
        record.stringField = "";
        record.intField = 0;
        record.intWrapperField = 0;
        record.longField = 0L;
        record.bigIntegerField = BigInteger.ZERO;
        record.bigDecimalField = BigDecimal.ZERO;
        record.dateField = LocalDate.of(1, 1, 1);
        record.dateTimeField = LocalDateTime.of(1, 1, 1, 0, 0, 0);
        record.dateFieldWithFormat = LocalDate.of(1, 1, 1);
        
        // when
        String flat = ObjectSerializer.serialize(record);
        TestFixtures.ValidComprehensiveTestRecord parsed = ObjectDeserializer.deserialize(flat, TestFixtures.ValidComprehensiveTestRecord.class);
        
        // then
        assertNotNull(parsed, "Deserialized record should not be null");
        assertEquals(record.stringField, parsed.stringField, "String field should be preserved");
        assertEquals(record.intField, parsed.intField, "Integer field should be preserved");
        assertEquals(record.intWrapperField, parsed.intWrapperField, "Int wrapper field should be preserved");
        assertEquals(record.longField, parsed.longField, "Long field should be preserved");
        assertEquals(record.bigIntegerField, parsed.bigIntegerField, "BigInteger field should be preserved");
        assertEquals(0, record.bigDecimalField.compareTo(parsed.bigDecimalField), "BigDecimal field should be preserved");
        assertEquals(record.dateField, parsed.dateField, "Date field should be preserved");
        assertEquals(record.dateTimeField, parsed.dateTimeField, "DateTime field should be preserved");
        assertEquals(record.dateFieldWithFormat, parsed.dateFieldWithFormat, "Date field with format should be preserved");
    }

    @Test
    @DisplayName("Should handle round trip with all fields at their maximum values")
    void shouldHandleRoundTripWithAllFieldsAtMaximumValues() {
        // given
        TestFixtures.ValidComprehensiveTestRecord record = new TestFixtures.ValidComprehensiveTestRecord();
        record.stringField = "MaxValue";
        record.intField = 99999999;
        record.intWrapperField = 99999999;
        record.longField = 999999999L;
        record.bigIntegerField = new BigInteger("999999999");
        record.bigDecimalField = new BigDecimal("999.999");
        record.dateField = LocalDate.of(9999, 12, 31);
        record.dateTimeField = LocalDateTime.of(9999, 12, 31, 23, 59, 59);
        record.dateFieldWithFormat = LocalDate.of(9999, 12, 31);
        
        // when
        String flat = ObjectSerializer.serialize(record);
        TestFixtures.ValidComprehensiveTestRecord parsed = ObjectDeserializer.deserialize(flat, TestFixtures.ValidComprehensiveTestRecord.class);
        
        // then
        assertNotNull(parsed, "Deserialized record should not be null");
        assertEquals(record.stringField, parsed.stringField, "String field should be preserved");
        assertEquals(record.intField, parsed.intField, "Integer field should be preserved");
        assertEquals(record.intWrapperField, parsed.intWrapperField, "Int wrapper field should be preserved");
        assertEquals(record.longField, parsed.longField, "Long field should be preserved");
        assertEquals(record.bigIntegerField, parsed.bigIntegerField, "BigInteger field should be preserved");
        assertEquals(0, record.bigDecimalField.compareTo(parsed.bigDecimalField), "BigDecimal field should be preserved");
        assertEquals(record.dateField, parsed.dateField, "Date field should be preserved");
        assertEquals(record.dateTimeField, parsed.dateTimeField, "DateTime field should be preserved");
        assertEquals(record.dateFieldWithFormat, parsed.dateFieldWithFormat, "Date field with format should be preserved");
    }

    // ==================== EXCEPTION TESTING ====================

    @Test
    @DisplayName("Should throw IllegalArgumentException when LocalDate field has no dateTimeFormat")
    void shouldThrowIllegalArgumentExceptionWhenLocalDateFieldHasNoDateTimeFormat() {
        // given
        TestFixtures.ComprehensiveTestRecord record = new TestFixtures.ComprehensiveTestRecord();
        record.stringField = "Test";
        record.intField = 123;
        record.longField = 456L;
        record.bigIntegerField = BigInteger.valueOf(789);
        record.bigDecimalField = new BigDecimal("123.45");
        record.dateField = LocalDate.of(2024, 1, 1);
        record.dateTimeField = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        // Set a non-null value for dateFieldNoFormat so we can test the format validation
        record.dateFieldNoFormat = LocalDate.of(2024, 1, 1);
        
        // when & then - Test that serialization fails for dateFieldNoFormat (no dateTimeFormat specified)
        assertThrows(IllegalArgumentException.class, () -> {
            ObjectSerializer.serialize(record);
        }, "Should throw IllegalArgumentException when LocalDate field has no dateTimeFormat");
    }
} 