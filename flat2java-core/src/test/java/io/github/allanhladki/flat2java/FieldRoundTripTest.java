package io.github.allanhladki.flat2java;

import static org.junit.jupiter.api.Assertions.*;

import io.github.allanhladki.flat2java.annotation.FlatField;
import io.github.allanhladki.flat2java.config.Flat2JavaConfig;
import io.github.allanhladki.flat2java.deserializer.FieldDeserializer;
import io.github.allanhladki.flat2java.fixtures.TestFixtures;
import io.github.allanhladki.flat2java.metadata.FieldMetadata;
import io.github.allanhladki.flat2java.serializer.FieldSerializer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
@DisplayName("Field Round-Trip Tests")
class FieldRoundTripTest {

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
        mockedConfig.when(Flat2JavaConfig::isNullHandlingEnabled).thenReturn(true);
        mockedConfig.when(Flat2JavaConfig::getNullValuePadding).thenReturn(' ');
    }

    @AfterEach
    void tearDown() {
        if (mockedConfig != null) {
            mockedConfig.close();
        }
    }

    // ==================== STRING ROUND-TRIP TESTS ====================

    @ParameterizedTest
    @MethodSource("stringRoundTripTestCases")
    @DisplayName("Should maintain string values through serialize-then-deserialize round trip")
    void testStringRoundTrip(String inputValue, char padding) throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("stringField");

        // Create mock annotation with specified padding
        FlatField mockAnnotation = Mockito.mock(FlatField.class);
        Mockito.when(mockAnnotation.precision()).thenReturn(0);
        Mockito.when(mockAnnotation.length()).thenReturn(8);
        Mockito.when(mockAnnotation.padding()).thenReturn(padding);
        Mockito.when(mockAnnotation.dateTimeFormat()).thenReturn("");

        FieldMetadata metadata = new FieldMetadata(field, mockAnnotation);

        // Serialize
        String serialized = FieldSerializer.serialize(inputValue);
        String formatted = FieldSerializer.formatToLength(serialized, metadata);

        // Deserialize
        Object deserialized = FieldDeserializer.parse(formatted, metadata);

        // Assert round-trip integrity
        assertEquals(
                inputValue,
                deserialized,
                String.format(
                        "String value should be preserved through round trip with padding '%c'",
                        padding));
    }

    static Stream<Arguments> stringRoundTripTestCases() {
        return Stream.of(
                Arguments.of("Hello", ' '),
                Arguments.of("World", '0'),
                Arguments.of("Test", '*'),
                Arguments.of("", ' '),
                Arguments.of("A", ' '),
                Arguments.of("Longer", ' '));
    }

    @Test
    @DisplayName("Should handle string round trip with edge cases")
    void testStringRoundTripEdgeCases() throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("stringField");

        // Test with padding character appearing in content
        FlatField mockAnnotation = Mockito.mock(FlatField.class);
        Mockito.when(mockAnnotation.precision()).thenReturn(0);
        Mockito.when(mockAnnotation.length()).thenReturn(10);
        Mockito.when(mockAnnotation.padding()).thenReturn('*');
        Mockito.when(mockAnnotation.dateTimeFormat()).thenReturn("");

        FieldMetadata metadata = new FieldMetadata(field, mockAnnotation);

        String inputValue = "Hello*Wor";

        // Serialize
        String serialized = FieldSerializer.serialize(inputValue);
        String formatted = FieldSerializer.formatToLength(serialized, metadata);

        // Deserialize
        Object deserialized = FieldDeserializer.parse(formatted, metadata);

        // Assert round-trip integrity
        assertEquals(
                inputValue,
                deserialized,
                "String with padding character in content should be preserved through round trip");
    }

    // ==================== INTEGER ROUND-TRIP TESTS ====================

    @ParameterizedTest
    @MethodSource("integerRoundTripTestCases")
    @DisplayName("Should maintain integer values through serialize-then-deserialize round trip")
    void testIntegerRoundTrip(Integer inputValue) throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("intField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        // Serialize
        String serialized = FieldSerializer.serialize(inputValue);
        String formatted = FieldSerializer.formatToLength(serialized, metadata);

        // Deserialize
        Object deserialized = FieldDeserializer.parse(formatted, metadata);

        // Assert round-trip integrity
        assertEquals(
                inputValue, deserialized, "Integer value should be preserved through round trip");
    }

    static Stream<Arguments> integerRoundTripTestCases() {
        return Stream.of(
                Arguments.of(0),
                Arguments.of(1),
                Arguments.of(42),
                Arguments.of(99999999),
                Arguments.of(12345));
    }

    // ==================== LONG ROUND-TRIP TESTS ====================

    @ParameterizedTest
    @MethodSource("longRoundTripTestCases")
    @DisplayName("Should maintain long values through serialize-then-deserialize round trip")
    void testLongRoundTrip(Long inputValue) throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("longField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        // Serialize
        String serialized = FieldSerializer.serialize(inputValue);
        String formatted = FieldSerializer.formatToLength(serialized, metadata);

        // Deserialize
        Object deserialized = FieldDeserializer.parse(formatted, metadata);

        // Assert round-trip integrity
        assertEquals(inputValue, deserialized, "Long value should be preserved through round trip");
    }

    static Stream<Arguments> longRoundTripTestCases() {
        return Stream.of(
                Arguments.of(0L),
                Arguments.of(1L),
                Arguments.of(42L),
                Arguments.of(999999999L),
                Arguments.of(123456789L));
    }

    // ==================== BIGINTEGER ROUND-TRIP TESTS ====================

    @ParameterizedTest
    @MethodSource("bigIntegerRoundTripTestCases")
    @DisplayName("Should maintain BigInteger values through serialize-then-deserialize round trip")
    void testBigIntegerRoundTrip(BigInteger inputValue) throws Exception {
        Field field =
                TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("bigIntegerField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        // Serialize
        String serialized = FieldSerializer.serialize(inputValue);
        String formatted = FieldSerializer.formatToLength(serialized, metadata);

        // Deserialize
        Object deserialized = FieldDeserializer.parse(formatted, metadata);

        // Assert round-trip integrity
        assertEquals(
                inputValue,
                deserialized,
                "BigInteger value should be preserved through round trip");
    }

    static Stream<Arguments> bigIntegerRoundTripTestCases() {
        return Stream.of(
                Arguments.of(BigInteger.ZERO),
                Arguments.of(BigInteger.ONE),
                Arguments.of(new BigInteger("42")),
                Arguments.of(new BigInteger("999999999")),
                Arguments.of(new BigInteger("123456789")));
    }

    // ==================== BIGDECIMAL ROUND-TRIP TESTS ====================

    @ParameterizedTest
    @MethodSource("bigDecimalRoundTripTestCases")
    @DisplayName("Should maintain BigDecimal values through serialize-then-deserialize round trip")
    void testBigDecimalRoundTrip(BigDecimal inputValue, int precision) throws Exception {
        Field field =
                TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("bigDecimalField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        // Serialize using the annotation's precision, not the test parameter
        String serialized = FieldSerializer.serialize(inputValue, annotation.precision());
        String formatted = FieldSerializer.formatToLength(serialized, metadata);

        // Deserialize
        Object deserialized = FieldDeserializer.parse(formatted, metadata);

        // Assert round-trip integrity - use compareTo for BigDecimal to handle scale differences
        if (deserialized instanceof BigDecimal) {
            assertEquals(
                    0,
                    ((BigDecimal) inputValue).compareTo((BigDecimal) deserialized),
                    "BigDecimal value should be preserved through round trip with annotation"
                            + " precision "
                            + annotation.precision());
        } else {
            assertEquals(
                    inputValue,
                    deserialized,
                    "BigDecimal value should be preserved through round trip with annotation"
                            + " precision "
                            + annotation.precision());
        }
    }

    static Stream<Arguments> bigDecimalRoundTripTestCases() {
        return Stream.of(
                Arguments.of(BigDecimal.ZERO, 3),
                Arguments.of(new BigDecimal("123.450"), 3),
                Arguments.of(new BigDecimal("0.010"), 3),
                Arguments.of(new BigDecimal("99.990"), 3),
                Arguments.of(new BigDecimal("123.456"), 3));
    }

    @Test
    @DisplayName("Should maintain BigDecimal values with decimal separator through round trip")
    void testBigDecimalWithDecimalSeparatorRoundTrip() throws Exception {
        // Enable decimal separator
        mockedConfig.when(Flat2JavaConfig::isBigDecimalUseDecimalSeparator).thenReturn(true);
        mockedConfig.when(Flat2JavaConfig::getBigDecimalDecimalSeparator).thenReturn('.');

        BigDecimal inputValue = new BigDecimal("123.45");
        int precision = 2;

        // Serialize
        String serialized = FieldSerializer.serialize(inputValue, precision);

        // Use a real field from test fixtures and create a mock annotation
        Field field =
                TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("bigDecimalField");

        FlatField mockAnnotation = Mockito.mock(FlatField.class);
        Mockito.when(mockAnnotation.precision()).thenReturn(precision);
        Mockito.when(mockAnnotation.length()).thenReturn(10);
        Mockito.when(mockAnnotation.padding()).thenReturn('0');
        Mockito.when(mockAnnotation.dateTimeFormat()).thenReturn("");

        FieldMetadata metadata = new FieldMetadata(field, mockAnnotation);

        // Format to length
        String formatted = FieldSerializer.formatToLength(serialized, metadata);

        // Deserialize
        Object deserialized = FieldDeserializer.parse(formatted, metadata);

        // Assert round-trip integrity
        assertEquals(
                inputValue,
                deserialized,
                "BigDecimal value with decimal separator should be preserved through round trip");
    }

    // ==================== DATE/TIME ROUND-TRIP TESTS ====================

    @ParameterizedTest
    @MethodSource("localDateRoundTripTestCases")
    @DisplayName("Should maintain LocalDate values through serialize-then-deserialize round trip")
    void testLocalDateRoundTrip(LocalDate inputValue) throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("dateField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        String format = "yyyyMMdd";

        // Serialize
        String serialized = FieldSerializer.serialize(inputValue, format);
        String formatted = FieldSerializer.formatToLength(serialized, metadata);

        // Deserialize
        Object deserialized = FieldDeserializer.parse(formatted, metadata);

        // Assert round-trip integrity
        assertEquals(
                inputValue, deserialized, "LocalDate value should be preserved through round trip");
    }

    static Stream<Arguments> localDateRoundTripTestCases() {
        return Stream.of(
                Arguments.of(LocalDate.of(2024, 1, 1)),
                Arguments.of(LocalDate.of(2024, 12, 31)),
                Arguments.of(LocalDate.of(2024, 6, 15)),
                Arguments.of(LocalDate.of(2000, 2, 29)) // Leap year
                );
    }

    @ParameterizedTest
    @MethodSource("localDateTimeRoundTripTestCases")
    @DisplayName(
            "Should maintain LocalDateTime values through serialize-then-deserialize round trip")
    void testLocalDateTimeRoundTrip(LocalDateTime inputValue) throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("dateTimeField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        String format = "yyyyMMddHHmmss";

        // Serialize
        String serialized = FieldSerializer.serialize(inputValue, format);
        String formatted = FieldSerializer.formatToLength(serialized, metadata);

        // Deserialize
        Object deserialized = FieldDeserializer.parse(formatted, metadata);

        // Assert round-trip integrity
        assertEquals(
                inputValue,
                deserialized,
                "LocalDateTime value should be preserved through round trip");
    }

    static Stream<Arguments> localDateTimeRoundTripTestCases() {
        return Stream.of(
                Arguments.of(LocalDateTime.of(2024, 1, 1, 0, 0, 0)),
                Arguments.of(LocalDateTime.of(2024, 12, 31, 23, 59, 59)),
                Arguments.of(LocalDateTime.of(2024, 6, 15, 12, 30, 45)),
                Arguments.of(LocalDateTime.of(2000, 2, 29, 12, 0, 0)) // Leap year
                );
    }

    // ==================== PADDING CONSISTENCY TESTS ====================

    @Test
    @DisplayName("Should maintain padding consistency through round trip")
    void testPaddingConsistencyRoundTrip() throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("stringField");

        // Test different padding characters
        char[] paddingChars = {' ', '0', '*', '-', '_'};

        for (char padding : paddingChars) {
            FlatField mockAnnotation = Mockito.mock(FlatField.class);
            Mockito.when(mockAnnotation.precision()).thenReturn(0);
            Mockito.when(mockAnnotation.length()).thenReturn(10);
            Mockito.when(mockAnnotation.padding()).thenReturn(padding);
            Mockito.when(mockAnnotation.dateTimeFormat()).thenReturn("");

            FieldMetadata metadata = new FieldMetadata(field, mockAnnotation);

            String inputValue = "Test";

            // Serialize
            String serialized = FieldSerializer.serialize(inputValue);
            String formatted = FieldSerializer.formatToLength(serialized, metadata);

            // Verify padding is correct
            assertTrue(
                    formatted.endsWith(String.valueOf(padding).repeat(6)),
                    String.format(
                            "Formatted string should end with padding character '%c'", padding));

            // Deserialize
            Object deserialized = FieldDeserializer.parse(formatted, metadata);

            // Assert round-trip integrity
            assertEquals(
                    inputValue,
                    deserialized,
                    String.format(
                            "Value should be preserved through round trip with padding '%c'",
                            padding));
        }
    }

    // ==================== NULL HANDLING ROUND-TRIP TESTS ====================

    @Test
    @DisplayName("Should handle null values correctly through round trip")
    void testNullValueRoundTrip() throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("stringField");

        // Test with space padding for null values
        FlatField mockAnnotation = Mockito.mock(FlatField.class);
        Mockito.when(mockAnnotation.precision()).thenReturn(0);
        Mockito.when(mockAnnotation.length()).thenReturn(8);
        Mockito.when(mockAnnotation.padding()).thenReturn(' ');
        Mockito.when(mockAnnotation.dateTimeFormat()).thenReturn("");

        FieldMetadata metadata = new FieldMetadata(field, mockAnnotation);

        // Serialize null value using ObjectSerializer
        String formatted =
                io.github.allanhladki.flat2java.serializer.ObjectSerializer.serialize(
                        null, metadata);

        // Should be all spaces (null value padding)
        assertEquals("        ", formatted, "Null value should be serialized as spaces");

        // Deserialize
        Object deserialized = FieldDeserializer.parse(formatted, metadata);

        // Should return empty string (not null) for round-trip consistency
        assertEquals(
                "",
                deserialized,
                "Null value should deserialize to empty string for round-trip consistency");
    }

    // ==================== EDGE CASE ROUND-TRIP TESTS ====================

    @Test
    @DisplayName("Should handle edge cases through round trip")
    void testEdgeCaseRoundTrip() throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("stringField");

        // Test with very long string that gets truncated
        FlatField mockAnnotation = Mockito.mock(FlatField.class);
        Mockito.when(mockAnnotation.precision()).thenReturn(0);
        Mockito.when(mockAnnotation.length()).thenReturn(5);
        Mockito.when(mockAnnotation.padding()).thenReturn(' ');
        Mockito.when(mockAnnotation.dateTimeFormat()).thenReturn("");

        FieldMetadata metadata = new FieldMetadata(field, mockAnnotation);

        String inputValue = "VeryLongString";

        // Serialize
        String serialized = FieldSerializer.serialize(inputValue);
        String formatted = FieldSerializer.formatToLength(serialized, metadata);

        // Should be truncated to 5 characters
        assertEquals(
                5, formatted.length(), "Formatted string should be truncated to specified length");

        // Deserialize
        Object deserialized = FieldDeserializer.parse(formatted, metadata);

        // Should return the truncated value
        assertEquals(
                "VeryL", deserialized, "Truncated string should be preserved through round trip");
    }

    @Test
    @DisplayName("Should handle zero values through round trip")
    void testZeroValueRoundTrip() throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("intField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        Integer inputValue = 0;

        // Serialize
        String serialized = FieldSerializer.serialize(inputValue);
        String formatted = FieldSerializer.formatToLength(serialized, metadata);

        // Should be zero-padded
        assertTrue(formatted.startsWith("0"), "Zero value should be zero-padded");

        // Deserialize
        Object deserialized = FieldDeserializer.parse(formatted, metadata);

        // Assert round-trip integrity
        assertEquals(inputValue, deserialized, "Zero value should be preserved through round trip");
    }
}
