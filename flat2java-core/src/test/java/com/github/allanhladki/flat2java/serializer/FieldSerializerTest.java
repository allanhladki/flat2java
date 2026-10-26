package com.github.allanhladki.flat2java.serializer;

import static org.junit.jupiter.api.Assertions.*;

import com.github.allanhladki.flat2java.config.Flat2JavaConfig;
import com.github.allanhladki.flat2java.fixtures.TestFixtures;
import com.github.allanhladki.flat2java.markers.FlatField;
import com.github.allanhladki.flat2java.metadata.FieldMetadata;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
@DisplayName("FieldSerializer Tests")
class FieldSerializerTest {

    private MockedStatic<Flat2JavaConfig> mockedConfig;

    @BeforeEach
    void setUp() {
        // Default configuration
        mockedConfig = Mockito.mockStatic(Flat2JavaConfig.class);
        mockedConfig.when(Flat2JavaConfig::isBigDecimalUseDecimalSeparator).thenReturn(false);
        mockedConfig.when(Flat2JavaConfig::getBigDecimalDecimalSeparator).thenReturn('.');
        mockedConfig
                .when(Flat2JavaConfig::getBigDecimalRoundingMode)
                .thenReturn(RoundingMode.UNNECESSARY);
    }

    @AfterEach
    void tearDown() {
        if (mockedConfig != null) {
            mockedConfig.close();
        }
    }

    // ==================== STRING TESTS ====================

    @ParameterizedTest
    @MethodSource("stringSerializationAndFormattingTestCases")
    @DisplayName("Should serialize and format string fields correctly")
    void testStringSerializationAndFormatting(
            String inputValue, String expectedSerialized, String expectedFormatted)
            throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("stringField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        // Test serialization
        String serialized = FieldSerializer.serialize(inputValue);
        assertEquals(expectedSerialized, serialized);

        // Test formatting to length
        String formatted = FieldSerializer.formatToLength(serialized, metadata);
        assertEquals(expectedFormatted, formatted);
    }

    static Stream<Arguments> stringSerializationAndFormattingTestCases() {
        return Stream.of(
                Arguments.of("Hello", "Hello", "Hello   "),
                Arguments.of("Test", "Test", "Test    "),
                Arguments.of("VeryLongString", "VeryLongString", "VeryLong"),
                Arguments.of("", "", "        "),
                Arguments.of("123", "123", "123     "));
    }

    @Test
    @DisplayName("Should throw exception when string truncation is enabled")
    void testStringTruncationException() throws Exception {
        mockedConfig.when(Flat2JavaConfig::isThrowOnStringTruncationEnabled).thenReturn(true);
        mockedConfig
                .when(Flat2JavaConfig::getThrowOnStringTruncationEnvVarName)
                .thenReturn("FLAT2JAVA_THROW_ON_STRING_TRUNCATION");

        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("stringField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        String serialized = FieldSerializer.serialize("VeryLongString");
        assertEquals("VeryLongString", serialized);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            FieldSerializer.formatToLength(serialized, metadata);
                        });

        assertTrue(
                exception
                        .getMessage()
                        .contains(
                                "Field 'stringField' value 'VeryLongString' (length 14) exceeds"
                                        + " target length 8"));
    }

    // ==================== NUMERIC TESTS ====================

    @ParameterizedTest
    @MethodSource("integerSerializationAndFormattingTestCases")
    @DisplayName("Should serialize and format integer fields correctly")
    void testIntegerSerializationAndFormatting(
            Integer inputValue, String expectedSerialized, String expectedFormatted)
            throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("intField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        // Test serialization
        String serialized = FieldSerializer.serialize(inputValue);
        assertEquals(expectedSerialized, serialized);

        // Test formatting to length
        String formatted = FieldSerializer.formatToLength(serialized, metadata);
        assertEquals(expectedFormatted, formatted);
    }

    static Stream<Arguments> integerSerializationAndFormattingTestCases() {
        return Stream.of(
                Arguments.of(123, "123", "00000123"),
                Arguments.of(0, "0", "00000000"),
                Arguments.of(99999999, "99999999", "99999999"),
                Arguments.of(1, "1", "00000001"));
    }

    @ParameterizedTest
    @MethodSource("longSerializationAndFormattingTestCases")
    @DisplayName("Should serialize and format long fields correctly")
    void testLongSerializationAndFormatting(
            Long inputValue, String expectedSerialized, String expectedFormatted) throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("longField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        // Test serialization
        String serialized = FieldSerializer.serialize(inputValue);
        assertEquals(expectedSerialized, serialized);

        // Test formatting to length
        String formatted = FieldSerializer.formatToLength(serialized, metadata);
        assertEquals(expectedFormatted, formatted);
    }

    static Stream<Arguments> longSerializationAndFormattingTestCases() {
        return Stream.of(
                Arguments.of(123L, "123", "000000123"),
                Arguments.of(0L, "0", "000000000"),
                Arguments.of(999999999L, "999999999", "999999999"),
                Arguments.of(1L, "1", "000000001"));
    }

    @ParameterizedTest
    @MethodSource("bigIntegerSerializationAndFormattingTestCases")
    @DisplayName("Should serialize and format BigInteger fields correctly")
    void testBigIntegerSerializationAndFormatting(
            BigInteger inputValue, String expectedSerialized, String expectedFormatted)
            throws Exception {
        Field field =
                TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("bigIntegerField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        // Test serialization
        String serialized = FieldSerializer.serialize(inputValue);
        assertEquals(expectedSerialized, serialized);

        // Test formatting to length
        String formatted = FieldSerializer.formatToLength(serialized, metadata);
        assertEquals(expectedFormatted, formatted);
    }

    static Stream<Arguments> bigIntegerSerializationAndFormattingTestCases() {
        return Stream.of(
                Arguments.of(BigInteger.valueOf(123), "123", "000000123"),
                Arguments.of(BigInteger.ZERO, "0", "000000000"),
                Arguments.of(new BigInteger("999999999"), "999999999", "999999999"),
                Arguments.of(BigInteger.ONE, "1", "000000001"));
    }

    // ==================== BIGDECIMAL TESTS ====================

    @ParameterizedTest
    @MethodSource("bigDecimalSerializationAndFormattingTestCases")
    @DisplayName("Should serialize and format BigDecimal fields correctly")
    void testBigDecimalSerializationAndFormatting(
            BigDecimal inputValue, String expectedSerialized, String expectedFormatted)
            throws Exception {
        Field field =
                TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("bigDecimalField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        // Test serialization
        String serialized = FieldSerializer.serialize(inputValue, annotation.precision());
        assertEquals(expectedSerialized, serialized);

        // Test formatting to length
        String formatted = FieldSerializer.formatToLength(serialized, metadata);
        assertEquals(expectedFormatted, formatted);
    }

    static Stream<Arguments> bigDecimalSerializationAndFormattingTestCases() {
        return Stream.of(
                Arguments.of(new BigDecimal("123.456"), "123456", "0123456"),
                Arguments.of(BigDecimal.ZERO, "0", "0000000"),
                Arguments.of(new BigDecimal("999.999"), "999999", "0999999"),
                Arguments.of(new BigDecimal("0.001"), "1", "0000001"));
    }

    @ParameterizedTest
    @MethodSource("bigDecimalDecimalSeparatorTestCases")
    @DisplayName("Should serialize BigDecimal with decimal separator when enabled")
    void testBigDecimalWithDecimalSeparator(
            BigDecimal inputValue, char separator, String expectedSerialized) {
        mockedConfig.when(Flat2JavaConfig::isBigDecimalUseDecimalSeparator).thenReturn(true);
        mockedConfig.when(Flat2JavaConfig::getBigDecimalDecimalSeparator).thenReturn(separator);

        String result = FieldSerializer.serialize(inputValue, 2);
        assertEquals(expectedSerialized, result);
    }

    static Stream<Arguments> bigDecimalDecimalSeparatorTestCases() {
        return Stream.of(
                Arguments.of(new BigDecimal("123.45"), '.', "123.45"),
                Arguments.of(new BigDecimal("123.45"), ',', "123,45"),
                Arguments.of(new BigDecimal("123.45"), ';', "123;45"),
                Arguments.of(new BigDecimal("123"), '.', "123.00"));
    }

    // ==================== DATE/TIME TESTS ====================

    @ParameterizedTest
    @MethodSource("localDateSerializationAndFormattingTestCases")
    @DisplayName("Should serialize and format LocalDate fields correctly")
    void testLocalDateSerializationAndFormatting(
            LocalDate inputValue, String expectedSerialized, String expectedFormatted)
            throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("dateField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        // Test serialization
        String serialized = FieldSerializer.serialize(inputValue, "yyyyMMdd");
        assertEquals(expectedSerialized, serialized);

        // Test formatting to length
        String formatted = FieldSerializer.formatToLength(serialized, metadata);
        assertEquals(expectedFormatted, formatted);
    }

    static Stream<Arguments> localDateSerializationAndFormattingTestCases() {
        return Stream.of(
                Arguments.of(LocalDate.of(2024, 3, 15), "20240315", "20240315"),
                Arguments.of(LocalDate.of(2024, 12, 31), "20241231", "20241231"),
                Arguments.of(LocalDate.of(2024, 1, 1), "20240101", "20240101"));
    }

    @ParameterizedTest
    @MethodSource("localDateTimeSerializationAndFormattingTestCases")
    @DisplayName("Should serialize and format LocalDateTime fields correctly")
    void testLocalDateTimeSerializationAndFormatting(
            LocalDateTime inputValue, String expectedSerialized, String expectedFormatted)
            throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("dateTimeField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        // Test serialization
        String serialized = FieldSerializer.serialize(inputValue, "yyyyMMddHHmmss");
        assertEquals(expectedSerialized, serialized);

        // Test formatting to length
        String formatted = FieldSerializer.formatToLength(serialized, metadata);
        assertEquals(expectedFormatted, formatted);
    }

    static Stream<Arguments> localDateTimeSerializationAndFormattingTestCases() {
        return Stream.of(
                Arguments.of(
                        LocalDateTime.of(2024, 3, 15, 12, 30, 45),
                        "20240315123045",
                        "20240315123045"),
                Arguments.of(
                        LocalDateTime.of(2024, 12, 31, 23, 59, 59),
                        "20241231235959",
                        "20241231235959"),
                Arguments.of(
                        LocalDateTime.of(2024, 1, 1, 0, 0, 0), "20240101000000", "20240101000000"));
    }

    // ==================== ERROR TESTS ====================

    @ParameterizedTest
    @ValueSource(classes = {String.class, Integer.class, BigDecimal.class})
    @DisplayName("Should throw NullPointerException for null values")
    void testNullValues(Class<?> type) {
        if (type == BigDecimal.class) {
            assertThrows(
                    NullPointerException.class,
                    () -> {
                        FieldSerializer.serialize((BigDecimal) null, 2);
                    });
        } else if (type == Integer.class) {
            assertThrows(
                    NullPointerException.class,
                    () -> {
                        FieldSerializer.serialize((Integer) null);
                    });
        } else {
            assertThrows(
                    NullPointerException.class,
                    () -> {
                        FieldSerializer.serialize((String) null);
                    });
        }
    }

    @ParameterizedTest
    @MethodSource("negativeValueTestCases")
    @DisplayName("Should throw exception for negative values")
    void testNegativeValues(Object value) {
        if (value instanceof BigDecimal) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> {
                        FieldSerializer.serialize((BigDecimal) value, 2);
                    });
        } else if (value instanceof Integer) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> {
                        FieldSerializer.serialize((Integer) value);
                    });
        } else if (value instanceof Long) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> {
                        FieldSerializer.serialize((Long) value);
                    });
        } else if (value instanceof BigInteger) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> {
                        FieldSerializer.serialize((BigInteger) value);
                    });
        }
    }

    static Stream<Arguments> negativeValueTestCases() {
        return Stream.of(
                Arguments.of(-123),
                Arguments.of(-123L),
                Arguments.of(BigInteger.valueOf(-123)),
                Arguments.of(new BigDecimal("-123.45")));
    }

    @Test
    @DisplayName("Should throw exception for LocalDate without format")
    void testLocalDateWithoutFormat() {
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    FieldSerializer.serialize(LocalDate.of(2024, 3, 15), "");
                });
    }

    @Test
    @DisplayName("Should throw exception for LocalDateTime without format")
    void testLocalDateTimeWithoutFormat() {
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    FieldSerializer.serialize(LocalDateTime.of(2024, 3, 15, 12, 30, 45), "");
                });
    }

    // ==================== ROUNDING MODE TESTS ====================

    @ParameterizedTest
    @MethodSource("roundingModeTestCases")
    @DisplayName("Should use different rounding modes correctly")
    void testRoundingModes(BigDecimal value, RoundingMode roundingMode, String expected) {
        mockedConfig.when(Flat2JavaConfig::getBigDecimalRoundingMode).thenReturn(roundingMode);

        String result = FieldSerializer.serialize(value, 2);
        assertEquals(expected, result);
    }

    static Stream<Arguments> roundingModeTestCases() {
        return Stream.of(
                Arguments.of(new BigDecimal("123.456"), RoundingMode.HALF_UP, "12346"),
                Arguments.of(new BigDecimal("123.456"), RoundingMode.HALF_DOWN, "12346"),
                Arguments.of(new BigDecimal("123.456"), RoundingMode.HALF_EVEN, "12346"),
                Arguments.of(new BigDecimal("123.456"), RoundingMode.UP, "12346"),
                Arguments.of(new BigDecimal("123.456"), RoundingMode.DOWN, "12345"),
                Arguments.of(new BigDecimal("123.456"), RoundingMode.CEILING, "12346"),
                Arguments.of(new BigDecimal("123.456"), RoundingMode.FLOOR, "12345"));
    }

    // ==================== NUMERIC FIELD TRUNCATION TESTS ====================

    @Test
    @DisplayName("Should always throw exception for numeric field truncation")
    void testNumericFieldTruncation() throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("intField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        String serialized = FieldSerializer.serialize(123456789);
        assertEquals("123456789", serialized);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            FieldSerializer.formatToLength(serialized, metadata);
                        });

        assertTrue(
                exception
                        .getMessage()
                        .contains(
                                "Field 'intField' value '123456789' (length 9) exceeds target"
                                        + " length 8"));
        assertTrue(exception.getMessage().contains("Numeric fields cannot be truncated"));
    }

    @Test
    @DisplayName("Should always use zero padding for numeric fields regardless of annotation")
    void testNumericFieldsAlwaysUseZeroPadding() throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("intField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        String serialized = FieldSerializer.serialize(123);
        assertEquals("123", serialized);

        // Even with space padding annotation, numeric fields should use zero padding
        String formatted = FieldSerializer.formatToLength(serialized, metadata);
        assertEquals("00000123", formatted); // Zero padding, not space padding
    }

    // ==================== ZERO VALUE TESTS ====================

    @ParameterizedTest
    @MethodSource("zeroValueTestCases")
    @DisplayName("Should serialize and format zero values correctly")
    void testZeroValues(Object value, String expectedSerialized, String expectedFormatted)
            throws Exception {
        Field field = TestFixtures.ZeroValueRecord.class.getDeclaredField("zeroInteger");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        // Test serialization
        String serialized;
        if (value instanceof BigDecimal) {
            serialized = FieldSerializer.serialize((BigDecimal) value, 2);
        } else if (value instanceof Integer) {
            serialized = FieldSerializer.serialize((Integer) value);
        } else if (value instanceof Long) {
            serialized = FieldSerializer.serialize((Long) value);
        } else if (value instanceof BigInteger) {
            serialized = FieldSerializer.serialize((BigInteger) value);
        } else {
            throw new IllegalArgumentException("Unsupported type: " + value.getClass());
        }

        assertEquals(expectedSerialized, serialized);

        // Test formatting to length
        String formatted = FieldSerializer.formatToLength(serialized, metadata);
        assertEquals(expectedFormatted, formatted);
    }

    static Stream<Arguments> zeroValueTestCases() {
        return Stream.of(
                Arguments.of(0, "0", "00000"),
                Arguments.of(0L, "0", "00000"),
                Arguments.of(BigInteger.ZERO, "0", "00000"),
                Arguments.of(BigDecimal.ZERO, "0", "00000"));
    }

    // ==================== TEST RECORD INTEGRATION TESTS ====================

    @Test
    @DisplayName("Should serialize and format TestRecord fields correctly")
    void testTestRecordIntegration() throws Exception {
        // Test string field
        Field stringField = TestFixtures.TestRecord.class.getDeclaredField("id");
        FlatField stringAnnotation = stringField.getAnnotation(FlatField.class);
        FieldMetadata stringMetadata = new FieldMetadata(stringField, stringAnnotation);

        String stringSerialized = FieldSerializer.serialize("TEST123");
        assertEquals("TEST123", stringSerialized);
        String stringFormatted = FieldSerializer.formatToLength(stringSerialized, stringMetadata);
        assertEquals("TEST123   ", stringFormatted); // length 10, right-padded

        // Test numeric field
        Field numericField = TestFixtures.TestRecord.class.getDeclaredField("quantity");
        FlatField numericAnnotation = numericField.getAnnotation(FlatField.class);
        FieldMetadata numericMetadata = new FieldMetadata(numericField, numericAnnotation);

        String numericSerialized = FieldSerializer.serialize(123);
        assertEquals("123", numericSerialized);
        String numericFormatted =
                FieldSerializer.formatToLength(numericSerialized, numericMetadata);
        assertEquals("00123", numericFormatted); // length 5, left-padded with zeros

        // Test BigDecimal field
        Field bigDecimalField = TestFixtures.TestRecord.class.getDeclaredField("price");
        FlatField bigDecimalAnnotation = bigDecimalField.getAnnotation(FlatField.class);
        FieldMetadata bigDecimalMetadata = new FieldMetadata(bigDecimalField, bigDecimalAnnotation);

        String bigDecimalSerialized = FieldSerializer.serialize(new BigDecimal("123.45"), 2);
        assertEquals("12345", bigDecimalSerialized);
        String bigDecimalFormatted =
                FieldSerializer.formatToLength(bigDecimalSerialized, bigDecimalMetadata);
        assertEquals("0000012345", bigDecimalFormatted); // length 10, left-padded with zeros

        // Test date field
        Field dateField = TestFixtures.TestRecord.class.getDeclaredField("date");
        FlatField dateAnnotation = dateField.getAnnotation(FlatField.class);
        FieldMetadata dateMetadata = new FieldMetadata(dateField, dateAnnotation);

        String dateSerialized = FieldSerializer.serialize(LocalDate.of(2024, 3, 15), "yyyyMMdd");
        assertEquals("20240315", dateSerialized);
        String dateFormatted = FieldSerializer.formatToLength(dateSerialized, dateMetadata);
        assertEquals("20240315", dateFormatted); // length 8, exact match
    }
}
