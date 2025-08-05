package com.github.allanhladki.flat2java.serializer;

import com.github.allanhladki.flat2java.config.Flat2JavaConfig;
import com.github.allanhladki.flat2java.fixtures.TestFixtures;
import com.github.allanhladki.flat2java.markers.FlatField;
import com.github.allanhladki.flat2java.metadata.FieldMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class FieldSerializerTest {

    private MockedStatic<Flat2JavaConfig> mockedConfig;

    @BeforeEach
    void setUp() {
        // Default configuration
        mockedConfig = Mockito.mockStatic(Flat2JavaConfig.class);
        mockedConfig.when(Flat2JavaConfig::getDefaultNumericPadding).thenReturn('0');
        mockedConfig.when(Flat2JavaConfig::isBigDecimalUseDecimalSeparator).thenReturn(false);
        mockedConfig.when(Flat2JavaConfig::getBigDecimalDecimalSeparator).thenReturn('.');
        mockedConfig.when(Flat2JavaConfig::getBigDecimalRoundingMode).thenReturn(RoundingMode.UNNECESSARY);
    }

    @AfterEach
    void tearDown() {
        if (mockedConfig != null) {
            mockedConfig.close();
        }
    }

    // ==================== STRING TESTS ====================

    @Test
    @DisplayName("Should format String field to length with right padding")
    void testFormatStringToLength() throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("stringField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);
        
        String serialized = FieldSerializer.serialize("Hello");
        assertEquals("Hello", serialized); // Verify serialization
        
        String formatted = FieldSerializer.formatToLength(serialized, metadata);
        assertEquals("Hello   ", formatted); // Field length: 8, right-padded with spaces
    }

    @Test
    @DisplayName("Should truncate String field that exceeds length")
    void testTruncateStringField() throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("stringField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);
        
        String serialized = FieldSerializer.serialize("VeryLongString");
        assertEquals("VeryLongString", serialized); // Verify serialization
        
        String formatted = FieldSerializer.formatToLength(serialized, metadata);
        assertEquals("VeryLong", formatted); // Field length: 8, truncated to fit
    }

    // ==================== INTEGER TESTS ====================


    @Test
    @DisplayName("Should format Integer field to length with leading zeros")
    void testFormatIntegerToLength() throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("intField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);
        
        String serialized = FieldSerializer.serialize(123);
        assertEquals("123", serialized); // Verify serialization
        
        String formatted = FieldSerializer.formatToLength(serialized, metadata);
        assertEquals("00000123", formatted); // Field length: 8, left-padded with zeros
    }

    @Test
    @DisplayName("Should format Integer field with space padding when configured")
    void testFormatIntegerWithSpacePadding() throws Exception {
        mockedConfig.when(Flat2JavaConfig::getDefaultNumericPadding).thenReturn(FlatField.Defaults.PADDING);
        
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("intField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);
        
        String serialized = FieldSerializer.serialize(123);
        assertEquals("123", serialized); // Verify serialization
        
        String formatted = FieldSerializer.formatToLength(serialized, metadata);
        assertEquals("     123", formatted); // Field length: 8, left-padded with spaces
    }

    // ==================== LONG TESTS ====================

    @Test
    @DisplayName("Should format Long field to length with leading zeros")
    void testFormatLongToLength() throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("longField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);
        
        String serialized = FieldSerializer.serialize(123456789L);
        assertEquals("123456789", serialized); // Verify serialization
        
        String formatted = FieldSerializer.formatToLength(serialized, metadata);
        assertEquals("123456789", formatted); // Field length: 9, exact length (no padding needed)
    }

    // ==================== BIGINTEGER TESTS ====================

    @Test
    @DisplayName("Should format BigInteger field to length with leading zeros")
    void testFormatBigIntegerToLength() throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("bigIntegerField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);
        
        String serialized = FieldSerializer.serialize(new BigInteger("12345678"));
        assertEquals("12345678", serialized); // Verify serialization
        
        String formatted = FieldSerializer.formatToLength(serialized, metadata);
        assertEquals("012345678", formatted); // Field length: 9, left-padded with zeros
    }

    // ==================== BIGDECIMAL TESTS ====================

    @Test
    @DisplayName("Should serialize BigDecimal field with precision 3 correctly (no decimal separator)")
    void testSerializeBigDecimalPrecision3() throws Exception {
        String result = FieldSerializer.serialize(new BigDecimal("123.45"), 3);
        assertEquals("123450", result); // 3 decimal places, no separator
    }

    @Test
    @DisplayName("Should serialize BigDecimal field with precision 2 correctly (no decimal separator)")
    void testSerializeBigDecimalPrecision2() throws Exception {
        // Mock HALF_UP rounding mode for this test since we're testing rounding behavior
        mockedConfig.when(Flat2JavaConfig::getBigDecimalRoundingMode).thenReturn(RoundingMode.HALF_UP);
        
        String result = FieldSerializer.serialize(new BigDecimal("123.456"), 2);
        assertEquals("12346", result); // 2 decimal places, rounded, no separator
    }

    @Test
    @DisplayName("Should serialize BigDecimal field with precision 0 correctly")
    void testSerializeBigDecimalPrecision0() throws Exception {
        // Mock HALF_UP rounding mode for this test since we're testing rounding behavior
        mockedConfig.when(Flat2JavaConfig::getBigDecimalRoundingMode).thenReturn(RoundingMode.HALF_UP);
        
        String result = FieldSerializer.serialize(new BigDecimal("123.45"), 0);
        assertEquals("123", result); // 0 precision means it must be an integer
    }

    @Test
    @DisplayName("Should serialize BigDecimal field with precision 4 correctly (no decimal separator)")
    void testSerializeBigDecimalPrecision4() throws Exception {
        String result = FieldSerializer.serialize(new BigDecimal("123.4"), 4);
        assertEquals("1234000", result); // 4 decimal places, no separator
    }

    @Test
    @DisplayName("Should serialize BigDecimal field with decimal separator when enabled")
    void testSerializeBigDecimalWithDecimalSeparator() throws Exception {
        mockedConfig.when(Flat2JavaConfig::isBigDecimalUseDecimalSeparator).thenReturn(true);
        
        String result = FieldSerializer.serialize(new BigDecimal("123.45"), 3);
        assertEquals("123.450", result); // 3 decimal places with separator
    }

    @Test
    @DisplayName("Should serialize BigDecimal field with custom decimal separator")
    void testSerializeBigDecimalWithCustomSeparator() throws Exception {
        mockedConfig.when(Flat2JavaConfig::isBigDecimalUseDecimalSeparator).thenReturn(true);
        mockedConfig.when(Flat2JavaConfig::getBigDecimalDecimalSeparator).thenReturn(',');
        
        String result = FieldSerializer.serialize(new BigDecimal("123.45"), 3);
        assertEquals("123,450", result); // Uses comma as decimal separator
    }

    @Test
    @DisplayName("Should serialize BigDecimal field with semicolon decimal separator")
    void testSerializeBigDecimalWithSemicolonSeparator() throws Exception {
        mockedConfig.when(Flat2JavaConfig::isBigDecimalUseDecimalSeparator).thenReturn(true);
        mockedConfig.when(Flat2JavaConfig::getBigDecimalDecimalSeparator).thenReturn(';');
        
        String result = FieldSerializer.serialize(new BigDecimal("123.45"), 2);
        assertEquals("123;45", result); // Uses semicolon as decimal separator
    }

    @Test
    @DisplayName("Should serialize BigDecimal field with default decimal separator")
    void testSerializeBigDecimalWithDefaultSeparator() throws Exception {
        mockedConfig.when(Flat2JavaConfig::isBigDecimalUseDecimalSeparator).thenReturn(true);
        mockedConfig.when(Flat2JavaConfig::getBigDecimalDecimalSeparator).thenReturn('.');
        
        String result = FieldSerializer.serialize(new BigDecimal("123.45"), 3);
        assertEquals("123.450", result); // Uses default dot as decimal separator
    }

    @Test
    @DisplayName("Should format BigDecimal field to length with leading zeros (no decimal separator)")
    void testFormatBigDecimalToLength() throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("bigDecimalField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);
        
        String serialized = FieldSerializer.serialize(new BigDecimal("23.45"), 3);
        assertEquals("23450", serialized); // Verify serialization (no decimal separator)
        
        String formatted = FieldSerializer.formatToLength(serialized, metadata);
        assertEquals("0023450", formatted); // Field length: 7, precision: 3, left-padded with zeros
    }

    @Test
    @DisplayName("Should format BigDecimal field with decimal separator when enabled")
    void testFormatBigDecimalWithDecimalSeparator() throws Exception {
        mockedConfig.when(Flat2JavaConfig::isBigDecimalUseDecimalSeparator).thenReturn(true);
        
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("bigDecimalField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);
        
        String serialized = FieldSerializer.serialize(new BigDecimal("23.45"), 3);
        assertEquals("23.450", serialized); // Verify serialization (with decimal separator)
        
        String formatted = FieldSerializer.formatToLength(serialized, metadata);
        assertEquals("023.450", formatted); // Field length: 7, precision: 3, left-padded with zeros
    }

    @Test
    @DisplayName("Should format BigDecimal field with custom padding character (no decimal separator)")
    void testFormatBigDecimalWithCustomPadding() throws Exception {
        // Create a test class with custom padding
        class CustomPaddingTestClass {
            @FlatField(length = 10, precision = 2, padding = '*')
            public BigDecimal customField;
        }
        
        Field field = CustomPaddingTestClass.class.getDeclaredField("customField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);
        
        String serialized = FieldSerializer.serialize(new BigDecimal("123.45"), 2);
        assertEquals("12345", serialized); // Verify serialization (no decimal separator)
        
        String formatted = FieldSerializer.formatToLength(serialized, metadata);
        assertEquals("*****12345", formatted); // Field length: 10, precision: 2, left-padded with asterisks
    }

    @Test
    @DisplayName("Should format BigDecimal field with custom padding and decimal separator")
    void testFormatBigDecimalWithCustomPaddingAndSeparator() throws Exception {
        mockedConfig.when(Flat2JavaConfig::isBigDecimalUseDecimalSeparator).thenReturn(true);
        
        // Create a test class with custom padding
        class CustomPaddingTestClass {
            @FlatField(length = 10, precision = 2, padding = '*')
            public BigDecimal customField;
        }
        
        Field field = CustomPaddingTestClass.class.getDeclaredField("customField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);
        
        String serialized = FieldSerializer.serialize(new BigDecimal("123.45"), 2);
        assertEquals("123.45", serialized); // Verify serialization (with decimal separator)
        
        String formatted = FieldSerializer.formatToLength(serialized, metadata);
        assertEquals("****123.45", formatted); // Field length: 10, precision: 2, left-padded with asterisks
    }

    @Test
    @DisplayName("Should format BigDecimal field with custom padding and comma separator")
    void testFormatBigDecimalWithCustomPaddingAndCommaSeparator() throws Exception {
        mockedConfig.when(Flat2JavaConfig::isBigDecimalUseDecimalSeparator).thenReturn(true);
        mockedConfig.when(Flat2JavaConfig::getBigDecimalDecimalSeparator).thenReturn(',');
        
        // Create a test class with custom padding
        class CustomPaddingTestClass {
            @FlatField(length = 10, precision = 2, padding = '*')
            public BigDecimal customField;
        }
        
        Field field = CustomPaddingTestClass.class.getDeclaredField("customField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);
        
        String serialized = FieldSerializer.serialize(new BigDecimal("123.45"), 2);
        assertEquals("123,45", serialized); // Verify serialization (with comma separator)
        
        String formatted = FieldSerializer.formatToLength(serialized, metadata);
        assertEquals("****123,45", formatted); // Field length: 10, precision: 2, left-padded with asterisks
    }

    // ==================== LOCALDATE TESTS ====================

    @Test
    @DisplayName("Should format LocalDate field to length with right padding")
    void testFormatLocalDateToLength() throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("dateField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);
        
        LocalDate date = LocalDate.of(2024, 3, 15);
        String serialized = FieldSerializer.serialize(date, "yyyyMMdd");
        assertEquals("20240315", serialized); // Verify serialization
        
        String formatted = FieldSerializer.formatToLength(serialized, metadata);
        assertEquals("20240315", formatted); // Field length: 8, exact length (no padding needed)
    }

    // ==================== LOCALDATETIME TESTS ====================

    @Test
    @DisplayName("Should format LocalDateTime field to length with right padding")
    void testFormatLocalDateTimeToLength() throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("dateTimeField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);
        
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 14, 30, 45);
        String serialized = FieldSerializer.serialize(dateTime, "yyyyMMddHHmmss");
        assertEquals("20240315143045", serialized); // Verify serialization
        
        String formatted = FieldSerializer.formatToLength(serialized, metadata);
        assertEquals("20240315143045", formatted); // Field length: 14, exact length (no padding needed)
    }

    // ==================== ERROR TESTS ====================

    @Test
    @DisplayName("Should throw exception for unsupported types")
    void testUnsupportedType() throws Exception {
        // Create a test class with an unsupported field type
        class UnsupportedTestClass {
            @FlatField(length = 8)
            private Object unsupportedField;
        }
        
        UnsupportedTestClass testObject = new UnsupportedTestClass();
        testObject.unsupportedField = new Object();
        
        assertThrows(IllegalArgumentException.class, () -> {
            ObjectSerializer.serialize(testObject);
        });
    }

    @Test
    @DisplayName("Should throw exception for LocalDate without format")
    void testLocalDateWithoutFormat() throws Exception {
        // Test the FieldSerializer directly for LocalDate without format
        LocalDate date = LocalDate.of(2024, 3, 15);
        
        assertThrows(IllegalArgumentException.class, () -> {
            FieldSerializer.serialize(date, ""); // Empty format should cause exception
        });
    }

    @Test
    @DisplayName("Should throw NullPointerException for null String")
    void testNullString() {
        assertThrows(NullPointerException.class, () -> {
            FieldSerializer.serialize((String) null);
        });
    }

    @Test
    @DisplayName("Should throw NullPointerException for null Integer")
    void testNullInteger() {
        assertThrows(NullPointerException.class, () -> {
            FieldSerializer.serialize((Integer) null);
        });
    }

    @Test
    @DisplayName("Should throw NullPointerException for null BigDecimal")
    void testNullBigDecimal() {
        assertThrows(NullPointerException.class, () -> {
            FieldSerializer.serialize((BigDecimal) null, 2);
        });
    }

    // ==================== POSITIVE VALUE VALIDATION TESTS ====================

    @Test
    @DisplayName("Should throw exception for negative Integer")
    void testNegativeInteger() {
        assertThrows(IllegalArgumentException.class, () -> {
            FieldSerializer.serialize(-123);
        });
    }

    @Test
    @DisplayName("Should throw exception for negative Long")
    void testNegativeLong() {
        assertThrows(IllegalArgumentException.class, () -> {
            FieldSerializer.serialize(-123L);
        });
    }

    @Test
    @DisplayName("Should throw exception for negative BigInteger")
    void testNegativeBigInteger() {
        assertThrows(IllegalArgumentException.class, () -> {
            FieldSerializer.serialize(new BigInteger("-123"));
        });
    }

    @Test
    @DisplayName("Should throw exception for negative BigDecimal")
    void testNegativeBigDecimal() {
        assertThrows(IllegalArgumentException.class, () -> {
            FieldSerializer.serialize(new BigDecimal("-123.45"), 2);
        });
    }

    // ==================== ZERO VALUE TESTS ====================

    @Test
    @DisplayName("Should serialize zero Integer correctly")
    void testZeroInteger() {
        String result = FieldSerializer.serialize(0);
        assertEquals("0", result);
    }

    @Test
    @DisplayName("Should serialize zero Long correctly")
    void testZeroLong() {
        String result = FieldSerializer.serialize(0L);
        assertEquals("0", result);
    }

    @Test
    @DisplayName("Should serialize zero BigInteger correctly")
    void testZeroBigInteger() {
        String result = FieldSerializer.serialize(BigInteger.ZERO);
        assertEquals("0", result);
    }

    @Test
    @DisplayName("Should serialize zero BigDecimal with precision 0 correctly")
    void testZeroBigDecimalNoPrecision() {
        String result = FieldSerializer.serialize(BigDecimal.ZERO, 0);
        assertEquals("0", result);
    }

    @Test
    @DisplayName("Should serialize zero BigDecimal with precision 2 correctly")
    void testZeroBigDecimalWithPrecision() {
        String result = FieldSerializer.serialize(BigDecimal.ZERO, 2);
        assertEquals("0", result); // BigDecimal.ZERO with precision 2 becomes "0" (no decimal separator by default)
    }

    @Test
    @DisplayName("Should serialize zero BigDecimal with precision 2 and decimal separator")
    void testZeroBigDecimalWithPrecisionAndSeparator() {
        mockedConfig.when(Flat2JavaConfig::isBigDecimalUseDecimalSeparator).thenReturn(true);
        
        String result = FieldSerializer.serialize(BigDecimal.ZERO, 2);
        assertEquals("0.00", result);
    }

    @Test
    @DisplayName("Should format zero Integer to length with leading zeros")
    void testFormatZeroIntegerToLength() throws Exception {
        Field field = TestFixtures.ZeroValueRecord.class.getDeclaredField("zeroInteger");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);
        
        String serialized = FieldSerializer.serialize(0);
        assertEquals("0", serialized); // Verify serialization
        
        String formatted = FieldSerializer.formatToLength(serialized, metadata);
        assertEquals("00000", formatted); // Field length: 5, left-padded with zeros
    }

    @Test
    @DisplayName("Should format zero Long to length with leading zeros")
    void testFormatZeroLongToLength() throws Exception {
        Field field = TestFixtures.ZeroValueRecord.class.getDeclaredField("zeroLong");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);
        
        String serialized = FieldSerializer.serialize(0L);
        assertEquals("0", serialized); // Verify serialization
        
        String formatted = FieldSerializer.formatToLength(serialized, metadata);
        assertEquals("00000", formatted); // Field length: 5, left-padded with zeros
    }

    @Test
    @DisplayName("Should format zero BigInteger to length with leading zeros")
    void testFormatZeroBigIntegerToLength() throws Exception {
        Field field = TestFixtures.ZeroValueRecord.class.getDeclaredField("zeroBigInteger");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);
        
        String serialized = FieldSerializer.serialize(BigInteger.ZERO);
        assertEquals("0", serialized); // Verify serialization
        
        String formatted = FieldSerializer.formatToLength(serialized, metadata);
        assertEquals("00000", formatted); // Field length: 5, left-padded with zeros
    }

    @Test
    @DisplayName("Should format zero BigDecimal with precision 0 to length")
    void testFormatZeroBigDecimalNoPrecisionToLength() throws Exception {
        Field field = TestFixtures.ZeroValueRecord.class.getDeclaredField("zeroBigDecimalNoPrecision");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);
        
        String serialized = FieldSerializer.serialize(BigDecimal.ZERO, 0);
        assertEquals("0", serialized); // Verify serialization
        
        String formatted = FieldSerializer.formatToLength(serialized, metadata);
        assertEquals("00000", formatted); // Field length: 5, precision: 0, left-padded with zeros
    }

    @Test
    @DisplayName("Should format zero BigDecimal with precision 2 to length")
    void testFormatZeroBigDecimalWithPrecisionToLength() throws Exception {
        Field field = TestFixtures.ZeroValueRecord.class.getDeclaredField("zeroBigDecimalWithPrecision");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);
        
        String serialized = FieldSerializer.serialize(BigDecimal.ZERO, 2);
        assertEquals("0", serialized); // Verify serialization (no decimal separator)
        
        String formatted = FieldSerializer.formatToLength(serialized, metadata);
        assertEquals("0000000", formatted); // Field length: 7, precision: 2, left-padded with zeros
    }

    @Test
    @DisplayName("Should format zero BigDecimal with precision 2 and decimal separator to length")
    void testFormatZeroBigDecimalWithPrecisionAndSeparatorToLength() throws Exception {
        mockedConfig.when(Flat2JavaConfig::isBigDecimalUseDecimalSeparator).thenReturn(true);
        
        Field field = TestFixtures.ZeroValueRecord.class.getDeclaredField("zeroBigDecimalWithPrecision");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);
        
        String serialized = FieldSerializer.serialize(BigDecimal.ZERO, 2);
        assertEquals("0.00", serialized); // Verify serialization (with decimal separator)
        
        String formatted = FieldSerializer.formatToLength(serialized, metadata);
        assertEquals("0000.00", formatted); // Field length: 7, precision: 2, left-padded with zeros
    }

    // ==================== ROUNDING MODE TESTS ====================

    @Test
    @DisplayName("Should use UNNECESSARY rounding mode (default)")
    void testUnnecessaryRoundingModeDefault() {
        // Test that UNNECESSARY works for exact values (default behavior)
        String result1 = FieldSerializer.serialize(new BigDecimal("12.34"), 2);
        assertEquals("1234", result1); // 12.34 is exact, no rounding needed
        
        String result2 = FieldSerializer.serialize(new BigDecimal("12.00"), 2);
        assertEquals("1200", result2); // 12.00 is exact, no rounding needed
    }

    @Test
    @DisplayName("Should use HALF_UP rounding mode")
    void testHalfUpRoundingMode() {
        mockedConfig.when(Flat2JavaConfig::getBigDecimalRoundingMode).thenReturn(RoundingMode.HALF_UP);
        
        // Test cases where HALF_UP rounds up on ties
        String result1 = FieldSerializer.serialize(new BigDecimal("12.345"), 2);
        assertEquals("1235", result1); // 12.345 → 12.35 → 1235 (no decimal separator)
        
        String result2 = FieldSerializer.serialize(new BigDecimal("12.335"), 2);
        assertEquals("1234", result2); // 12.335 → 12.34 → 1234 (no decimal separator)
    }

    @Test
    @DisplayName("Should use HALF_DOWN rounding mode")
    void testHalfDownRoundingMode() {
        mockedConfig.when(Flat2JavaConfig::getBigDecimalRoundingMode).thenReturn(RoundingMode.HALF_DOWN);
        
        // Test cases where HALF_DOWN rounds down on ties
        String result1 = FieldSerializer.serialize(new BigDecimal("12.345"), 2);
        assertEquals("1234", result1); // 12.345 → 12.34 → 1234 (no decimal separator)
        
        String result2 = FieldSerializer.serialize(new BigDecimal("12.335"), 2);
        assertEquals("1233", result2); // 12.335 → 12.33 → 1233 (no decimal separator)
    }

    @Test
    @DisplayName("Should use HALF_EVEN rounding mode (Banker's rounding)")
    void testHalfEvenRoundingMode() {
        mockedConfig.when(Flat2JavaConfig::getBigDecimalRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        
        // Test cases where HALF_EVEN rounds to even on ties
        String result1 = FieldSerializer.serialize(new BigDecimal("12.345"), 2);
        assertEquals("1234", result1); // 12.345 → 12.34 → 1234 (rounds to even)
        
        String result2 = FieldSerializer.serialize(new BigDecimal("12.335"), 2);
        assertEquals("1234", result2); // 12.335 → 12.34 → 1234 (rounds to even)
        
        String result3 = FieldSerializer.serialize(new BigDecimal("12.355"), 2);
        assertEquals("1236", result3); // 12.355 → 12.36 → 1236 (rounds to even)
    }

    @Test
    @DisplayName("Should use UP rounding mode")
    void testUpRoundingMode() {
        mockedConfig.when(Flat2JavaConfig::getBigDecimalRoundingMode).thenReturn(RoundingMode.UP);
        
        // Test cases where UP always rounds up
        String result1 = FieldSerializer.serialize(new BigDecimal("12.341"), 2);
        assertEquals("1235", result1); // 12.341 → 12.35 → 1235 (always up)
        
        String result2 = FieldSerializer.serialize(new BigDecimal("12.300"), 2);
        assertEquals("1230", result2); // 12.300 → 12.30 → 1230 (no rounding needed, exact value)
    }

    @Test
    @DisplayName("Should use DOWN rounding mode")
    void testDownRoundingMode() {
        mockedConfig.when(Flat2JavaConfig::getBigDecimalRoundingMode).thenReturn(RoundingMode.DOWN);
        
        // Test cases where DOWN always rounds down
        String result1 = FieldSerializer.serialize(new BigDecimal("12.349"), 2);
        assertEquals("1234", result1); // 12.349 → 12.34 → 1234 (always down)
        
        String result2 = FieldSerializer.serialize(new BigDecimal("12.399"), 2);
        assertEquals("1239", result2); // 12.399 → 12.39 → 1239 (always down)
    }

    @Test
    @DisplayName("Should use CEILING rounding mode")
    void testCeilingRoundingMode() {
        mockedConfig.when(Flat2JavaConfig::getBigDecimalRoundingMode).thenReturn(RoundingMode.CEILING);
        
        // Test cases where CEILING rounds towards positive infinity
        String result1 = FieldSerializer.serialize(new BigDecimal("12.341"), 2);
        assertEquals("1235", result1); // 12.341 → 12.35 → 1235 (towards positive infinity)
        
        String result2 = FieldSerializer.serialize(new BigDecimal("12.300"), 2);
        assertEquals("1230", result2); // 12.300 → 12.30 → 1230 (no rounding needed, exact value)
    }

    @Test
    @DisplayName("Should use FLOOR rounding mode")
    void testFloorRoundingMode() {
        mockedConfig.when(Flat2JavaConfig::getBigDecimalRoundingMode).thenReturn(RoundingMode.FLOOR);
        
        // Test cases where FLOOR rounds towards negative infinity
        String result1 = FieldSerializer.serialize(new BigDecimal("12.349"), 2);
        assertEquals("1234", result1); // 12.349 → 12.34 → 1234 (towards negative infinity)
        
        String result2 = FieldSerializer.serialize(new BigDecimal("12.399"), 2);
        assertEquals("1239", result2); // 12.399 → 12.39 → 1239 (towards negative infinity)
    }

    @Test
    @DisplayName("Should use UNNECESSARY rounding mode for exact values")
    void testUnnecessaryRoundingModeExact() {
        // UNNECESSARY is now the default, so we don't need to mock it
        // Test cases where UNNECESSARY works for exact values
        String result1 = FieldSerializer.serialize(new BigDecimal("12.34"), 2);
        assertEquals("1234", result1); // 12.34 is exact, no rounding needed
        
        String result2 = FieldSerializer.serialize(new BigDecimal("12.00"), 2);
        assertEquals("1200", result2); // 12.00 is exact, no rounding needed
    }

    @Test
    @DisplayName("Should throw exception for UNNECESSARY rounding mode when rounding is needed")
    void testUnnecessaryRoundingModeThrowsException() {
        // UNNECESSARY is now the default, so we don't need to mock it
        // Test that UNNECESSARY throws exception when rounding would be needed
        assertThrows(ArithmeticException.class, () -> {
            FieldSerializer.serialize(new BigDecimal("12.345"), 2);
        });
        
        assertThrows(ArithmeticException.class, () -> {
            FieldSerializer.serialize(new BigDecimal("12.341"), 2);
        });
    }

    @Test
    @DisplayName("Should use rounding mode with decimal separator enabled")
    void testRoundingModeWithDecimalSeparator() {
        mockedConfig.when(Flat2JavaConfig::isBigDecimalUseDecimalSeparator).thenReturn(true);
        mockedConfig.when(Flat2JavaConfig::getBigDecimalRoundingMode).thenReturn(RoundingMode.HALF_DOWN);
        
        // Test rounding mode with decimal separator
        String result = FieldSerializer.serialize(new BigDecimal("12.345"), 2);
        assertEquals("12.34", result); // 12.345 → 12.34 with HALF_DOWN
    }

    @Test
    @DisplayName("Should use rounding mode with custom decimal separator")
    void testRoundingModeWithCustomDecimalSeparator() {
        mockedConfig.when(Flat2JavaConfig::isBigDecimalUseDecimalSeparator).thenReturn(true);
        mockedConfig.when(Flat2JavaConfig::getBigDecimalDecimalSeparator).thenReturn(',');
        mockedConfig.when(Flat2JavaConfig::getBigDecimalRoundingMode).thenReturn(RoundingMode.HALF_UP);
        
        // Test rounding mode with custom decimal separator
        String result = FieldSerializer.serialize(new BigDecimal("12.345"), 2);
        assertEquals("12,35", result); // 12.345 → 12.35 with HALF_UP and comma separator
    }

    @Test
    @DisplayName("Should use rounding mode for precision 0 (integer conversion)")
    void testRoundingModeWithPrecisionZero() {
        mockedConfig.when(Flat2JavaConfig::getBigDecimalRoundingMode).thenReturn(RoundingMode.HALF_UP);
        
        // Test rounding mode when precision is 0 (converts to integer)
        String result = FieldSerializer.serialize(new BigDecimal("12.5"), 0);
        assertEquals("13", result); // 12.5 → 13 with HALF_UP
    }

    @Test
    @DisplayName("Should use rounding mode for high precision values")
    void testRoundingModeWithHighPrecision() {
        mockedConfig.when(Flat2JavaConfig::getBigDecimalRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        
        // Test rounding mode with high precision
        String result = FieldSerializer.serialize(new BigDecimal("12.3456789"), 4);
        assertEquals("123457", result); // 12.3456789 → 12.3457 → 123457 with HALF_EVEN
    }

    @Test
    @DisplayName("Should use rounding mode for edge case values")
    void testRoundingModeWithEdgeCases() {
        mockedConfig.when(Flat2JavaConfig::getBigDecimalRoundingMode).thenReturn(RoundingMode.HALF_UP);
        
        // Test edge cases
        String result1 = FieldSerializer.serialize(new BigDecimal("0.005"), 2);
        assertEquals("1", result1); // 0.005 → 0.01 → 1 with HALF_UP (raw value, no padding)
        
        String result2 = FieldSerializer.serialize(new BigDecimal("999.999"), 2);
        assertEquals("100000", result2); // 999.999 → 1000.00 → 100000 with HALF_UP (raw value, no padding)
    }

    @Test
    @DisplayName("Should use rounding mode with proper field formatting (leading zeros)")
    void testRoundingModeWithFieldFormatting() throws Exception {
        mockedConfig.when(Flat2JavaConfig::getBigDecimalRoundingMode).thenReturn(RoundingMode.HALF_UP);
        
        // Create a field metadata with length 7 (to show padding)
        class EdgeCaseTestClass {
            @FlatField(length = 7, precision = 2)
            public BigDecimal edgeCaseField;
        }
        
        Field field = EdgeCaseTestClass.class.getDeclaredField("edgeCaseField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);
        
        // Test edge case with proper formatting (including leading zeros)
        String serialized = FieldSerializer.serialize(new BigDecimal("0.005"), 2);
        assertEquals("1", serialized); // Raw serialization (no padding)
        
        String formatted = FieldSerializer.formatToLength(serialized, metadata);
        assertEquals("0000001", formatted); // Field length: 7, precision: 2, left-padded with zeros
    }

    // ==================== NUMBERS LESS THAN 1 TESTS ====================

    @Test
    @DisplayName("Should serialize numbers less than 1 correctly (no decimal separator)")
    void testNumbersLessThanOneSerialization() {
        // Test raw serialization of numbers less than 1
        String result1 = FieldSerializer.serialize(new BigDecimal("0.5"), 2);
        assertEquals("50", result1); // 0.5 → 50 (representing 50 cents)
        
        String result2 = FieldSerializer.serialize(new BigDecimal("0.25"), 2);
        assertEquals("25", result2); // 0.25 → 25 (representing 25 cents)
        
        String result3 = FieldSerializer.serialize(new BigDecimal("0.125"), 3);
        assertEquals("125", result3); // 0.125 → 125 (representing 125 thousandths)
        
        String result4 = FieldSerializer.serialize(new BigDecimal("0.01"), 2);
        assertEquals("1", result4); // 0.01 → 1 (representing 1 cent)
        
        String result5 = FieldSerializer.serialize(new BigDecimal("0.001"), 3);
        assertEquals("1", result5); // 0.001 → 1 (representing 1 thousandth)
    }

    @Test
    @DisplayName("Should format numbers less than 1 with proper leading zeros (no decimal separator)")
    void testNumbersLessThanOneFormatting() throws Exception {
        // Create field metadata with length 8
        class LessThanOneTestClass {
            @FlatField(length = 8, precision = 2)
            public BigDecimal lessThanOneField;
        }
        
        Field field = LessThanOneTestClass.class.getDeclaredField("lessThanOneField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);
        
        // Test formatting with leading zeros
        String serialized1 = FieldSerializer.serialize(new BigDecimal("0.5"), 2);
        assertEquals("50", serialized1); // Raw serialization
        
        String formatted1 = FieldSerializer.formatToLength(serialized1, metadata);
        assertEquals("00000050", formatted1); // Field length: 8, precision: 2, left-padded with zeros
        
        String serialized2 = FieldSerializer.serialize(new BigDecimal("0.25"), 2);
        assertEquals("25", serialized2); // Raw serialization
        
        String formatted2 = FieldSerializer.formatToLength(serialized2, metadata);
        assertEquals("00000025", formatted2); // Field length: 8, precision: 2, left-padded with zeros
        
        String serialized3 = FieldSerializer.serialize(new BigDecimal("0.01"), 2);
        assertEquals("1", serialized3); // Raw serialization
        
        String formatted3 = FieldSerializer.formatToLength(serialized3, metadata);
        assertEquals("00000001", formatted3); // Field length: 8, precision: 2, left-padded with zeros
    }

    @Test
    @DisplayName("Should serialize numbers less than 1 with decimal separator enabled")
    void testNumbersLessThanOneWithDecimalSeparator() {
        mockedConfig.when(Flat2JavaConfig::isBigDecimalUseDecimalSeparator).thenReturn(true);
        
        // Test with decimal separator enabled
        String result1 = FieldSerializer.serialize(new BigDecimal("0.5"), 2);
        assertEquals("0.50", result1); // 0.5 → 0.50 (with decimal separator)
        
        String result2 = FieldSerializer.serialize(new BigDecimal("0.25"), 2);
        assertEquals("0.25", result2); // 0.25 → 0.25 (with decimal separator)
        
        String result3 = FieldSerializer.serialize(new BigDecimal("0.01"), 2);
        assertEquals("0.01", result3); // 0.01 → 0.01 (with decimal separator)
        
        String result4 = FieldSerializer.serialize(new BigDecimal("0.125"), 3);
        assertEquals("0.125", result4); // 0.125 → 0.125 (with decimal separator)
    }

    @Test
    @DisplayName("Should format numbers less than 1 with decimal separator and proper padding")
    void testNumbersLessThanOneWithDecimalSeparatorAndPadding() throws Exception {
        mockedConfig.when(Flat2JavaConfig::isBigDecimalUseDecimalSeparator).thenReturn(true);
        
        // Create field metadata with length 10
        class LessThanOneDecimalTestClass {
            @FlatField(length = 10, precision = 2)
            public BigDecimal lessThanOneDecimalField;
        }
        
        Field field = LessThanOneDecimalTestClass.class.getDeclaredField("lessThanOneDecimalField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);
        
        // Test formatting with decimal separator and padding
        String serialized1 = FieldSerializer.serialize(new BigDecimal("0.5"), 2);
        assertEquals("0.50", serialized1); // Raw serialization with decimal separator
        
        String formatted1 = FieldSerializer.formatToLength(serialized1, metadata);
        assertEquals("0000000.50", formatted1); // Field length: 10, precision: 2, left-padded with zeros
        
        String serialized2 = FieldSerializer.serialize(new BigDecimal("0.25"), 2);
        assertEquals("0.25", serialized2); // Raw serialization with decimal separator
        
        String formatted2 = FieldSerializer.formatToLength(serialized2, metadata);
        assertEquals("0000000.25", formatted2); // Field length: 10, precision: 2, left-padded with zeros
        
        String serialized3 = FieldSerializer.serialize(new BigDecimal("0.01"), 2);
        assertEquals("0.01", serialized3); // Raw serialization with decimal separator
        
        String formatted3 = FieldSerializer.formatToLength(serialized3, metadata);
        assertEquals("0000000.01", formatted3); // Field length: 10, precision: 2, left-padded with zeros
    }

    @Test
    @DisplayName("Should handle very small numbers less than 1 with different precisions")
    void testVerySmallNumbersLessThanOne() {
        // Test very small numbers with different precisions
        String result1 = FieldSerializer.serialize(new BigDecimal("0.001"), 3);
        assertEquals("1", result1); // 0.001 → 1 (representing 1 thousandth)
        
        String result2 = FieldSerializer.serialize(new BigDecimal("0.0001"), 4);
        assertEquals("1", result2); // 0.0001 → 1 (representing 1 ten-thousandth)
        
        String result3 = FieldSerializer.serialize(new BigDecimal("0.00001"), 5);
        assertEquals("1", result3); // 0.00001 → 1 (representing 1 hundred-thousandth)
        
        // Test with decimal separator
        mockedConfig.when(Flat2JavaConfig::isBigDecimalUseDecimalSeparator).thenReturn(true);
        
        String result4 = FieldSerializer.serialize(new BigDecimal("0.001"), 3);
        assertEquals("0.001", result4); // 0.001 → 0.001 (with decimal separator)
        
        String result5 = FieldSerializer.serialize(new BigDecimal("0.0001"), 4);
        assertEquals("0.0001", result5); // 0.0001 → 0.0001 (with decimal separator)
    }

    @Test
    @DisplayName("Should handle edge cases for numbers less than 1 with rounding")
    void testNumbersLessThanOneWithRounding() {
        mockedConfig.when(Flat2JavaConfig::getBigDecimalRoundingMode).thenReturn(RoundingMode.HALF_UP);
        
        // Test edge cases with rounding
        String result1 = FieldSerializer.serialize(new BigDecimal("0.005"), 2);
        assertEquals("1", result1); // 0.005 → 0.01 → 1 (HALF_UP rounding)
        
        String result2 = FieldSerializer.serialize(new BigDecimal("0.004"), 2);
        assertEquals("0", result2); // 0.004 → 0.00 → 0 (HALF_UP rounding)
        
        String result3 = FieldSerializer.serialize(new BigDecimal("0.006"), 2);
        assertEquals("1", result3); // 0.006 → 0.01 → 1 (HALF_UP rounding)
        
        // Test with different rounding modes
        mockedConfig.when(Flat2JavaConfig::getBigDecimalRoundingMode).thenReturn(RoundingMode.HALF_DOWN);
        
        String result4 = FieldSerializer.serialize(new BigDecimal("0.005"), 2);
        assertEquals("0", result4); // 0.005 → 0.00 → 0 (HALF_DOWN rounding)
        
        String result5 = FieldSerializer.serialize(new BigDecimal("0.006"), 2);
        assertEquals("1", result5); // 0.006 → 0.01 → 1 (HALF_DOWN rounding)
    }

    @Test
    @DisplayName("Should format edge cases for numbers less than 1 with proper padding")
    void testNumbersLessThanOneEdgeCasesWithPadding() throws Exception {
        mockedConfig.when(Flat2JavaConfig::getBigDecimalRoundingMode).thenReturn(RoundingMode.HALF_UP);
        
        // Create field metadata with length 6
        class EdgeCaseLessThanOneTestClass {
            @FlatField(length = 6, precision = 2)
            public BigDecimal edgeCaseLessThanOneField;
        }
        
        Field field = EdgeCaseLessThanOneTestClass.class.getDeclaredField("edgeCaseLessThanOneField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);
        
        // Test edge cases with proper padding
        String serialized1 = FieldSerializer.serialize(new BigDecimal("0.005"), 2);
        assertEquals("1", serialized1); // 0.005 → 0.01 → 1 (HALF_UP rounding)
        
        String formatted1 = FieldSerializer.formatToLength(serialized1, metadata);
        assertEquals("000001", formatted1); // Field length: 6, precision: 2, left-padded with zeros
        
        String serialized2 = FieldSerializer.serialize(new BigDecimal("0.004"), 2);
        assertEquals("0", serialized2); // 0.004 → 0.00 → 0 (HALF_UP rounding)
        
        String formatted2 = FieldSerializer.formatToLength(serialized2, metadata);
        assertEquals("000000", formatted2); // Field length: 6, precision: 2, left-padded with zeros
        
        // Test with decimal separator
        mockedConfig.when(Flat2JavaConfig::isBigDecimalUseDecimalSeparator).thenReturn(true);
        
        String serialized3 = FieldSerializer.serialize(new BigDecimal("0.005"), 2);
        assertEquals("0.01", serialized3); // 0.005 → 0.01 (with decimal separator)
        
        String formatted3 = FieldSerializer.formatToLength(serialized3, metadata);
        assertEquals("000.01", formatted3); // Field length: 6, precision: 2, left-padded with zeros
    }

} 