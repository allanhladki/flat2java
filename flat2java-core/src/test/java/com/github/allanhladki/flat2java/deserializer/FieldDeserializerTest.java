package com.github.allanhladki.flat2java.deserializer;

import static org.junit.jupiter.api.Assertions.*;

import com.github.allanhladki.flat2java.config.Flat2JavaConfig;
import com.github.allanhladki.flat2java.fixtures.TestFixtures;
import com.github.allanhladki.flat2java.markers.FlatField;
import com.github.allanhladki.flat2java.metadata.FieldMetadata;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("FieldDeserializer Tests")
class FieldDeserializerTest {

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
    @MethodSource("stringDeserializationTestCases")
    @DisplayName("Should deserialize string fields correctly with default annotation")
    void testStringDeserialization(String inputSlice, String expectedValue, char padding)
            throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("stringField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        Object result = FieldDeserializer.parse(inputSlice, metadata);
        assertEquals(expectedValue, result);
    }

    static Stream<Arguments> stringDeserializationTestCases() {
        return Stream.of(
                Arguments.of("Hello   ", "Hello", ' '),
                Arguments.of("Test    ", "Test", ' '),
                Arguments.of("VeryLong", "VeryLong", ' '),
                Arguments.of("        ", "", ' '),
                Arguments.of("123     ", "123", ' '),
                Arguments.of("ABC     ", "ABC", ' '));
    }

    @ParameterizedTest
    @MethodSource("stringDeserializationWithDifferentPaddingTestCases")
    @DisplayName("Should deserialize string fields correctly with different padding characters")
    void testStringDeserializationWithDifferentPadding(
            String inputSlice, String expectedValue, char padding) throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("stringField");

        // Mock the annotation to return custom padding
        FlatField mockAnnotation = Mockito.mock(FlatField.class);
        Mockito.when(mockAnnotation.precision()).thenReturn(0);
        Mockito.when(mockAnnotation.length()).thenReturn(8);
        Mockito.when(mockAnnotation.padding()).thenReturn(padding);
        Mockito.when(mockAnnotation.dateTimeFormat()).thenReturn("");

        FieldMetadata mockMetadata = new FieldMetadata(field, mockAnnotation);

        Object result = FieldDeserializer.parse(inputSlice, mockMetadata);
        assertEquals(expectedValue, result);
    }

    static Stream<Arguments> stringDeserializationWithDifferentPaddingTestCases() {
        return Stream.of(
                // Space padding
                Arguments.of("Hello   ", "Hello", ' '),
                Arguments.of("Test    ", "Test", ' '),
                Arguments.of("        ", "", ' '),
                // Zero padding
                Arguments.of("Hello000", "Hello", '0'),
                Arguments.of("Test0000", "Test", '0'),
                Arguments.of("00000000", "", '0'),
                // Asterisk padding
                Arguments.of("Hello***", "Hello", '*'),
                Arguments.of("Test****", "Test", '*'),
                Arguments.of("********", "", '*'),
                // Dash padding
                Arguments.of("Hello---", "Hello", '-'),
                Arguments.of("Test----", "Test", '-'),
                Arguments.of("--------", "", '-'),
                // Underscore padding
                Arguments.of("Hello___", "Hello", '_'),
                Arguments.of("Test____", "Test", '_'),
                Arguments.of("________", "", '_'),
                // Dot padding
                Arguments.of("Hello...", "Hello", '.'),
                Arguments.of("Test....", "Test", '.'),
                Arguments.of("........", "", '.'),
                // Hash padding
                Arguments.of("Hello###", "Hello", '#'),
                Arguments.of("Test####", "Test", '#'),
                Arguments.of("########", "", '#'),
                // Mixed content with different padding
                Arguments.of("123*****", "123", '*'),
                Arguments.of("ABC00000", "ABC", '0'),
                Arguments.of("XYZ-----", "XYZ", '-'));
    }

    @Test
    @DisplayName("Should handle edge cases with different padding characters")
    void testEdgeCasesWithDifferentPadding() throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("stringField");

        // Test case where padding character appears in actual content
        FlatField mockAnnotation = Mockito.mock(FlatField.class);
        Mockito.when(mockAnnotation.precision()).thenReturn(0);
        Mockito.when(mockAnnotation.length()).thenReturn(8);
        Mockito.when(mockAnnotation.padding()).thenReturn('*');
        Mockito.when(mockAnnotation.dateTimeFormat()).thenReturn("");

        FieldMetadata mockMetadata = new FieldMetadata(field, mockAnnotation);

        // Content contains the padding character - should only trim trailing padding
        Object result = FieldDeserializer.parse("Hello***", mockMetadata);
        assertEquals("Hello", result);

        // Content with padding character in the middle should not be trimmed
        result = FieldDeserializer.parse("He*lo***", mockMetadata);
        assertEquals("He*lo", result);

        // All padding characters should return empty string
        result = FieldDeserializer.parse("********", mockMetadata);
        assertEquals("", result);

        // Test with zero padding - create a new mock object
        FlatField mockAnnotationZero = Mockito.mock(FlatField.class);
        Mockito.when(mockAnnotationZero.precision()).thenReturn(0);
        Mockito.when(mockAnnotationZero.length()).thenReturn(8);
        Mockito.when(mockAnnotationZero.padding()).thenReturn('0');
        Mockito.when(mockAnnotationZero.dateTimeFormat()).thenReturn("");

        FieldMetadata mockMetadataZero = new FieldMetadata(field, mockAnnotationZero);

        result = FieldDeserializer.parse("Hello000", mockMetadataZero);
        assertEquals("Hello", result);

        // Content with zeros in the middle should not be trimmed
        result = FieldDeserializer.parse("He0lo000", mockMetadataZero);
        assertEquals("He0lo", result);

        // All zeros should return empty string
        result = FieldDeserializer.parse("00000000", mockMetadataZero);
        assertEquals("", result);
    }

    @Test
    @DisplayName("Debug test for rtrim functionality")
    void testRtrimDebug() throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("stringField");

        // Test with zero padding
        FlatField mockAnnotation = Mockito.mock(FlatField.class);
        Mockito.when(mockAnnotation.precision()).thenReturn(0);
        Mockito.when(mockAnnotation.length()).thenReturn(8);
        Mockito.when(mockAnnotation.padding()).thenReturn('0');
        Mockito.when(mockAnnotation.dateTimeFormat()).thenReturn("");

        FieldMetadata mockMetadata = new FieldMetadata(field, mockAnnotation);

        // Debug: Let's see what's actually happening
        System.out.println("Testing with padding: " + mockMetadata.getPadding());
        System.out.println("Testing input: 'Hello000'");

        Object result = FieldDeserializer.parse("Hello000", mockMetadata);
        System.out.println("Result: '" + result + "'");
        System.out.println("Result type: " + result.getClass().getSimpleName());
        System.out.println("Result length: " + ((String) result).length());

        // This should work if rtrim is working correctly
        assertEquals("Hello", result);
    }

    @Test
    @DisplayName("Should throw exception for null slice")
    void testNullSlice() throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("stringField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        assertThrows(IllegalArgumentException.class, () -> FieldDeserializer.parse(null, metadata));
    }

    @Test
    @DisplayName("Should throw exception for null metadata")
    void testNullMetadata() {
        assertThrows(IllegalArgumentException.class, () -> FieldDeserializer.parse("test", null));
    }

    @Test
    @DisplayName("Should never return null for string fields - only empty strings")
    void testStringFieldsNeverReturnNull() throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("stringField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        // All-padding values should return empty strings, not null
        Object result = FieldDeserializer.parse("        ", metadata);
        assertEquals("", result);
        assertFalse(result == null);

        // Test with different padding characters - create mock with asterisk padding
        FlatField mockAnnotation = Mockito.mock(FlatField.class);
        Mockito.when(mockAnnotation.precision()).thenReturn(0);
        Mockito.when(mockAnnotation.length()).thenReturn(7);
        Mockito.when(mockAnnotation.padding()).thenReturn('*');
        Mockito.when(mockAnnotation.dateTimeFormat()).thenReturn("");

        FieldMetadata mockMetadata = new FieldMetadata(field, mockAnnotation);
        result = FieldDeserializer.parse("*******", mockMetadata);
        assertEquals("", result);
        assertFalse(result == null);
    }

    // ==================== INTEGER TESTS ====================

    @ParameterizedTest
    @MethodSource("integerDeserializationTestCases")
    @DisplayName("Should deserialize integer fields correctly")
    void testIntegerDeserialization(String inputSlice, Integer expectedValue, char padding)
            throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("intField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        Object result = FieldDeserializer.parse(inputSlice, metadata);
        assertEquals(expectedValue, result);
    }

    static Stream<Arguments> integerDeserializationTestCases() {
        return Stream.of(
                Arguments.of("12345", 12345, '0'),
                Arguments.of("00042", 42, '0'),
                Arguments.of("00000", 0, '0'),
                Arguments.of("99999", 99999, '0'));
    }

    @ParameterizedTest
    @MethodSource("integerErrorTestCases")
    @DisplayName("Should throw exception for invalid integer values")
    void testIntegerErrors(String inputSlice, String expectedError) throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("intField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class, () -> FieldDeserializer.parse(inputSlice, metadata));
        assertTrue(exception.getMessage().contains(expectedError));
    }

    static Stream<Arguments> integerErrorTestCases() {
        return Stream.of(
                Arguments.of("abcde", "invalid integer"),
                Arguments.of("-1234", "must be non-negative"),
                Arguments.of("12.34", "invalid integer"),
                Arguments.of("     ", "cannot deserialize all-padding value"));
    }

    // ==================== LONG TESTS ====================

    @ParameterizedTest
    @MethodSource("longDeserializationTestCases")
    @DisplayName("Should deserialize long fields correctly")
    void testLongDeserialization(String inputSlice, Long expectedValue, char padding)
            throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("longField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        Object result = FieldDeserializer.parse(inputSlice, metadata);
        assertEquals(expectedValue, result);
    }

    static Stream<Arguments> longDeserializationTestCases() {
        return Stream.of(
                Arguments.of("123456789", 123456789L, '0'),
                Arguments.of("000000042", 42L, '0'),
                Arguments.of("000000000", 0L, '0'),
                Arguments.of("999999999", 999999999L, '0'));
    }

    // ==================== BIGINTEGER TESTS ====================

    @ParameterizedTest
    @MethodSource("bigIntegerDeserializationTestCases")
    @DisplayName("Should deserialize BigInteger fields correctly")
    void testBigIntegerDeserialization(String inputSlice, BigInteger expectedValue, char padding)
            throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("bigIntegerField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        Object result = FieldDeserializer.parse(inputSlice, metadata);
        assertEquals(expectedValue, result);
    }

    static Stream<Arguments> bigIntegerDeserializationTestCases() {
        return Stream.of(
                Arguments.of("123456789", new BigInteger("123456789"), '0'),
                Arguments.of("000000042", new BigInteger("42"), '0'),
                Arguments.of("000000000", BigInteger.ZERO, '0'),
                Arguments.of("999999999", new BigInteger("999999999"), '0'));
    }

    // ==================== BIGDECIMAL TESTS ====================

    @ParameterizedTest
    @MethodSource("bigDecimalDeserializationTestCases")
    @DisplayName("Should deserialize BigDecimal fields correctly")
    void testBigDecimalDeserialization(String inputSlice, BigDecimal expectedValue, char padding)
            throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("bigDecimalField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        Object result = FieldDeserializer.parse(inputSlice, metadata);
        assertEquals(expectedValue, result);
    }

    static Stream<Arguments> bigDecimalDeserializationTestCases() {
        return Stream.of(
                Arguments.of("12345", new BigDecimal("12.345"), '0'),
                Arguments.of("00042", new BigDecimal("0.042"), '0'),
                Arguments.of("00000", new BigDecimal("0.000"), '0'),
                Arguments.of("99999", new BigDecimal("99.999"), '0'));
    }

    @ParameterizedTest
    @MethodSource("bigDecimalDecimalSeparatorTestCases")
    @DisplayName("Should deserialize BigDecimal with decimal separator when enabled")
    void testBigDecimalWithDecimalSeparator(
            String inputSlice, char separator, BigDecimal expectedValue) throws Exception {
        mockedConfig.when(Flat2JavaConfig::isBigDecimalUseDecimalSeparator).thenReturn(true);
        mockedConfig.when(Flat2JavaConfig::getBigDecimalDecimalSeparator).thenReturn(separator);

        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("bigDecimalField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        Object result = FieldDeserializer.parse(inputSlice, metadata);
        assertEquals(expectedValue, result);
    }

    static Stream<Arguments> bigDecimalDecimalSeparatorTestCases() {
        return Stream.of(
                Arguments.of("123.45", '.', new BigDecimal("123.450")),
                Arguments.of("123,45", ',', new BigDecimal("123.450")),
                Arguments.of("0.42", '.', new BigDecimal("0.420")),
                Arguments.of("0,42", ',', new BigDecimal("0.420")));
    }

    @Test
    @DisplayName("Should throw exception for negative precision")
    void testBigDecimalNegativePrecision() throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("bigDecimalField");

        // Mock the annotation to return negative precision
        FlatField mockAnnotation = Mockito.mock(FlatField.class);
        Mockito.when(mockAnnotation.precision()).thenReturn(-1);
        Mockito.when(mockAnnotation.length()).thenReturn(5);
        Mockito.when(mockAnnotation.padding()).thenReturn('0');
        Mockito.when(mockAnnotation.dateTimeFormat()).thenReturn("");

        FieldMetadata mockMetadata = new FieldMetadata(field, mockAnnotation);

        assertThrows(
                IllegalArgumentException.class, () -> FieldDeserializer.parse("12345", mockMetadata));
    }

    // ==================== SPACE PADDING EXCEPTION TESTS ====================

    @Test
    @DisplayName("Should throw exception for space padding in numeric fields")
    void testSpacePaddingExceptionInNumericFields() throws Exception {
        // Test integer field
        Field intField = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("intField");
        FlatField intAnnotation = intField.getAnnotation(FlatField.class);
        FieldMetadata intMetadata = new FieldMetadata(intField, intAnnotation);

        assertThrows(
                IllegalArgumentException.class, () -> FieldDeserializer.parse("     ", intMetadata));

        // Test long field
        Field longField = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("longField");
        FlatField longAnnotation = longField.getAnnotation(FlatField.class);
        FieldMetadata longMetadata = new FieldMetadata(longField, longAnnotation);

        assertThrows(
                IllegalArgumentException.class, () -> FieldDeserializer.parse("         ", longMetadata));

        // Test BigInteger field
        Field bigIntField =
                TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("bigIntegerField");
        FlatField bigIntAnnotation = bigIntField.getAnnotation(FlatField.class);
        FieldMetadata bigIntMetadata = new FieldMetadata(bigIntField, bigIntAnnotation);

        assertThrows(
                IllegalArgumentException.class, () -> FieldDeserializer.parse("         ", bigIntMetadata));

        // Test BigDecimal field
        Field bigDecField =
                TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("bigDecimalField");
        FlatField bigDecAnnotation = bigDecField.getAnnotation(FlatField.class);
        FieldMetadata bigDecMetadata = new FieldMetadata(bigDecField, bigDecAnnotation);

        assertThrows(
                IllegalArgumentException.class, () -> FieldDeserializer.parse("     ", bigDecMetadata));
    }

    // ==================== DATE/TIME TESTS ====================

    @ParameterizedTest
    @MethodSource("localDateDeserializationTestCases")
    @DisplayName("Should deserialize LocalDate fields correctly")
    void testLocalDateDeserialization(String inputSlice, LocalDate expectedValue, char padding)
            throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("dateField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        Object result = FieldDeserializer.parse(inputSlice, metadata);
        assertEquals(expectedValue, result);
    }

    static Stream<Arguments> localDateDeserializationTestCases() {
        return Stream.of(
                Arguments.of("20240315", LocalDate.of(2024, 3, 15), '0'),
                Arguments.of("20241231", LocalDate.of(2024, 12, 31), '0'));
    }

    @ParameterizedTest
    @MethodSource("localDateTimeDeserializationTestCases")
    @DisplayName("Should deserialize LocalDateTime fields correctly")
    void testLocalDateTimeDeserialization(
            String inputSlice, LocalDateTime expectedValue, char padding) throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("dateTimeField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        Object result = FieldDeserializer.parse(inputSlice, metadata);
        assertEquals(expectedValue, result);
    }

    static Stream<Arguments> localDateTimeDeserializationTestCases() {
        return Stream.of(
                Arguments.of("20240315120000", LocalDateTime.of(2024, 3, 15, 12, 0, 0), '0'),
                Arguments.of("20241231235959", LocalDateTime.of(2024, 12, 31, 23, 59, 59), '0'));
    }

    @Test
    @DisplayName("Should throw exception for LocalDate without format")
    void testLocalDateWithoutFormat() throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("dateField");

        // Mock the annotation to return null format
        FlatField mockAnnotation = Mockito.mock(FlatField.class);
        Mockito.when(mockAnnotation.precision()).thenReturn(0);
        Mockito.when(mockAnnotation.length()).thenReturn(8);
        Mockito.when(mockAnnotation.padding()).thenReturn('0');
        Mockito.when(mockAnnotation.dateTimeFormat()).thenReturn("");

        FieldMetadata mockMetadata = new FieldMetadata(field, mockAnnotation);

        assertThrows(
                IllegalArgumentException.class, () -> FieldDeserializer.parse("20240315", mockMetadata));
    }

    @Test
    @DisplayName("Should throw exception for LocalDateTime without format")
    void testLocalDateTimeWithoutFormat() throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("dateTimeField");

        // Mock the annotation to return null format
        FlatField mockAnnotation = Mockito.mock(FlatField.class);
        Mockito.when(mockAnnotation.precision()).thenReturn(0);
        Mockito.when(mockAnnotation.length()).thenReturn(14);
        Mockito.when(mockAnnotation.padding()).thenReturn('0');
        Mockito.when(mockAnnotation.dateTimeFormat()).thenReturn("");

        FieldMetadata mockMetadata = new FieldMetadata(field, mockAnnotation);

        assertThrows(
                IllegalArgumentException.class,
                () -> FieldDeserializer.parse("20240315120000", mockMetadata));
    }

    @Test
    @DisplayName("Should throw exception for invalid date format")
    void testInvalidDateFormat() throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("dateField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        assertThrows(
                IllegalArgumentException.class, () -> FieldDeserializer.parse("invalid", metadata));
    }

    @Test
    @DisplayName("Should throw exception for space padding in date/time fields")
    void testSpacePaddingExceptionInDateTimeFields() throws Exception {
        // Test LocalDate field
        Field dateField = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("dateField");
        FlatField dateAnnotation = dateField.getAnnotation(FlatField.class);
        FieldMetadata dateMetadata = new FieldMetadata(dateField, dateAnnotation);

        assertThrows(
                IllegalArgumentException.class, () -> FieldDeserializer.parse("        ", dateMetadata));

        // Test LocalDateTime field
        Field dateTimeField =
                TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("dateTimeField");
        FlatField dateTimeAnnotation = dateTimeField.getAnnotation(FlatField.class);
        FieldMetadata dateTimeMetadata = new FieldMetadata(dateTimeField, dateTimeAnnotation);

        assertThrows(
                IllegalArgumentException.class,
                () -> FieldDeserializer.parse("            ", dateTimeMetadata));
    }

    @Test
    @DisplayName("Should throw exception for zero padding in date/time fields")
    void testZeroPaddingExceptionInDateTimeFields() throws Exception {
        // Test LocalDate field
        Field dateField = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("dateField");
        FlatField dateAnnotation = dateField.getAnnotation(FlatField.class);
        FieldMetadata dateMetadata = new FieldMetadata(dateField, dateAnnotation);

        assertThrows(
                IllegalArgumentException.class, () -> FieldDeserializer.parse("00000000", dateMetadata));

        // Test LocalDateTime field
        Field dateTimeField =
                TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("dateTimeField");
        FlatField dateTimeAnnotation = dateTimeField.getAnnotation(FlatField.class);
        FieldMetadata dateTimeMetadata = new FieldMetadata(dateTimeField, dateTimeAnnotation);

        assertThrows(
                IllegalArgumentException.class,
                () -> FieldDeserializer.parse("00000000000000", dateTimeMetadata));
    }

    // ==================== PADDING TESTS ====================

    @ParameterizedTest
    @MethodSource("paddingTestCases")
    @DisplayName("Should handle different padding characters correctly")
    void testPaddingHandling(String inputSlice, char padding, String expectedResult)
            throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("stringField");

        // Mock the annotation to return custom padding
        FlatField mockAnnotation = Mockito.mock(FlatField.class);
        Mockito.when(mockAnnotation.precision()).thenReturn(0);
        Mockito.when(mockAnnotation.length()).thenReturn(8);
        Mockito.when(mockAnnotation.padding()).thenReturn(padding);
        Mockito.when(mockAnnotation.dateTimeFormat()).thenReturn("");

        FieldMetadata mockMetadata = new FieldMetadata(field, mockAnnotation);

        Object result = FieldDeserializer.parse(inputSlice, mockMetadata);
        assertEquals(expectedResult, result);
    }

    static Stream<Arguments> paddingTestCases() {
        return Stream.of(
                Arguments.of("Hello   ", ' ', "Hello"),
                Arguments.of("Test    ", ' ', "Test"),
                Arguments.of("        ", ' ', ""),
                Arguments.of("        ", ' ', ""));
    }

    @Test
    @DisplayName("Should handle custom padding characters correctly")
    void testCustomPaddingHandling() throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("stringField");

        // Mock the annotation to return custom padding
        FlatField mockAnnotation = Mockito.mock(FlatField.class);
        Mockito.when(mockAnnotation.precision()).thenReturn(0);
        Mockito.when(mockAnnotation.length()).thenReturn(8);
        Mockito.when(mockAnnotation.padding()).thenReturn('*');
        Mockito.when(mockAnnotation.dateTimeFormat()).thenReturn("");

        FieldMetadata mockMetadata = new FieldMetadata(field, mockAnnotation);

        // Test with asterisk padding
        Object result = FieldDeserializer.parse("ABC*****", mockMetadata);
        assertEquals("ABC", result);

        // Test with all asterisks
        result = FieldDeserializer.parse("********", mockMetadata);
        assertEquals("", result);
    }

    // ==================== NUMERIC PADDING TESTS ====================

    @Test
    @DisplayName("Should always use zero padding for numeric fields regardless of annotation")
    void testNumericFieldsAlwaysUseZeroPadding() throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("intField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        // Even with space padding annotation, numeric fields should use zero padding
        Object result = FieldDeserializer.parse("00000", metadata);
        assertEquals(0, result);

        // All spaces should throw exception for numeric fields
        assertThrows(IllegalArgumentException.class, () -> FieldDeserializer.parse("     ", metadata));
    }

    @Test
    @DisplayName("Should ignore annotation padding for numeric fields and always use zero padding")
    void testAnnotationNumericPaddingIgnored() throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("intField");

        // Mock the annotation to return custom padding
        FlatField mockAnnotation = Mockito.mock(FlatField.class);
        Mockito.when(mockAnnotation.precision()).thenReturn(0);
        Mockito.when(mockAnnotation.length()).thenReturn(5);
        Mockito.when(mockAnnotation.padding()).thenReturn('*');
        Mockito.when(mockAnnotation.dateTimeFormat()).thenReturn("");

        FieldMetadata mockMetadata = new FieldMetadata(field, mockAnnotation);

        // Even with '*' padding annotation, numeric fields should use zero padding
        Object result = FieldDeserializer.parse("00000", mockMetadata);
        assertEquals(0, result);

        // All asterisks should throw exception for numeric fields
        assertThrows(
                IllegalArgumentException.class, () -> FieldDeserializer.parse("*****", mockMetadata));
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Should handle empty string after trimming padding")
    void testEmptyStringAfterTrimming() throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("intField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        Object result = FieldDeserializer.parse("00000", metadata);
        assertEquals(0, result);
    }

    @Test
    @DisplayName("Should handle all padding for reference types")
    void testAllPaddingForReferenceTypes() throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("intField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        // All spaces should throw exception for numeric fields
        assertThrows(IllegalArgumentException.class, () -> FieldDeserializer.parse("     ", metadata));
    }

    @Test
    @DisplayName("Should handle all padding for primitive types")
    void testAllPaddingForPrimitiveTypes() throws Exception {
        Field field = TestFixtures.ComprehensiveTestRecord.class.getDeclaredField("intPrimitiveField");
        FlatField annotation = field.getAnnotation(FlatField.class);
        FieldMetadata metadata = new FieldMetadata(field, annotation);

        Object result = FieldDeserializer.parse("00000", metadata);
        assertEquals(0, result);
    }
}
