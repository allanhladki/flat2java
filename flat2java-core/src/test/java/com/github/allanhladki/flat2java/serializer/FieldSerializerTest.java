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
        assertEquals("Hello   ", formatted); // 8 chars total, right-padded with spaces
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
        assertEquals("VeryLong", formatted); // truncated to 8 chars
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
        assertEquals("00000123", formatted); // 8 chars total, left-padded with zeros
    }

    @Test
    @DisplayName("Should format Integer field with space padding when configured")
    void testFormatIntegerWithSpacePadding() throws Exception {
        mockedConfig.when(Flat2JavaConfig::getDefaultNumericPadding).thenReturn(' ');
        
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("intField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);
        
        String serialized = FieldSerializer.serialize(123);
        assertEquals("123", serialized); // Verify serialization
        
        String formatted = FieldSerializer.formatToLength(serialized, metadata);
        assertEquals("     123", formatted); // 8 chars total, left-padded with spaces
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
        assertEquals("123456789", formatted); // 9 chars total, exact length
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
        assertEquals("012345678", formatted); // 9 chars total, exact length
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
        String result = FieldSerializer.serialize(new BigDecimal("123.456"), 2);
        assertEquals("12346", result); // 2 decimal places, rounded, no separator
    }

    @Test
    @DisplayName("Should serialize BigDecimal field with precision 0 correctly")
    void testSerializeBigDecimalPrecision0() throws Exception {
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
        assertEquals("0023450", formatted); // 7 chars total, left-padded with zeros
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
        assertEquals("023.450", formatted); // 7 chars total, left-padded with zeros
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
        assertEquals("*****12345", formatted); // 10 chars total, left-padded with asterisks
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
        assertEquals("****123.45", formatted); // 10 chars total, left-padded with asterisks
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
        assertEquals("****123,45", formatted); // 10 chars total, left-padded with asterisks
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
        assertEquals("20240315", formatted); // 8 chars total, exact length
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
        assertEquals("20240315143045", formatted); // 14 chars total, exact length
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
        assertEquals("00000", formatted); // 5 chars total, left-padded with zeros
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
        assertEquals("00000", formatted); // 5 chars total, left-padded with zeros
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
        assertEquals("00000", formatted); // 5 chars total, left-padded with zeros
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
        assertEquals("00000", formatted); // 5 chars total, left-padded with zeros
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
        assertEquals("0000000", formatted); // 7 chars total, left-padded with zeros
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
        assertEquals("0000.00", formatted); // 7 chars total, left-padded with zeros
    }

} 