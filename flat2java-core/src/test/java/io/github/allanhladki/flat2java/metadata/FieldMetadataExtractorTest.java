package io.github.allanhladki.flat2java.metadata;

import static org.junit.jupiter.api.Assertions.*;

import io.github.allanhladki.flat2java.fixtures.TestFixtures;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class FieldMetadataExtractorTest {

    @Test
    @DisplayName("Should extract annotated fields")
    void shouldExtractAnnotatedFields() {
        // when
        List<FieldMetadata> metadata =
                FieldMetadataExtractor.extract(TestFixtures.TestRecord.class);

        // then
        assertNotNull(metadata, "Metadata list should not be null");
        assertEquals(5, metadata.size(), "Should find exactly 5 annotated fields");

        // Verify order and properties by declaration order
        assertEquals("id", metadata.get(0).getName());
        assertEquals(10, metadata.get(0).getLength());

        assertEquals("date", metadata.get(1).getName());
        assertEquals(8, metadata.get(1).getLength());
        assertEquals("yyyyMMdd", metadata.get(1).getDateTimeFormat());

        assertEquals("quantity", metadata.get(2).getName());
        assertEquals(5, metadata.get(2).getLength());

        assertEquals("price", metadata.get(3).getName());
        assertEquals(10, metadata.get(3).getLength());
        assertEquals(2, metadata.get(3).getPrecision());

        assertEquals("expiryDate", metadata.get(4).getName());
        assertEquals(8, metadata.get(4).getLength());
        assertEquals("ddMMyyyy", metadata.get(4).getDateTimeFormat());
    }

    @Test
    @DisplayName("Should handle empty class")
    void shouldHandleEmptyClass() {
        // when
        List<FieldMetadata> metadata =
                FieldMetadataExtractor.extract(TestFixtures.EmptyClass.class);

        // then
        assertNotNull(metadata, "Metadata list should not be null");
        assertTrue(metadata.isEmpty(), "Should return empty list for class without annotations");
    }

    @Test
    @DisplayName("Should throw NullPointerException for null class")
    void shouldThrowNullPointerExceptionForNullClass() {
        // when/then
        assertThrows(NullPointerException.class, () -> FieldMetadataExtractor.extract(null));
    }

    @Test
    @DisplayName("Should extract fields from superclass and subclass")
    void shouldExtractFieldsFromSuperclassAndSubclass() {
        // when
        List<FieldMetadata> metadata =
                FieldMetadataExtractor.extractIncludingInherited(TestFixtures.SubRecord.class);

        // then
        assertNotNull(metadata, "Metadata list should not be null");
        assertEquals(
                4,
                metadata.size(),
                "Should find 4 annotated fields from both superclass and subclass");
        assertEquals("superField1", metadata.get(0).getName());
        assertEquals("superField2", metadata.get(1).getName());
        assertEquals("subField1", metadata.get(2).getName());
        assertEquals("subField2", metadata.get(3).getName());
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    @DisplayName("Should throw exception for zero length")
    void shouldThrowExceptionForZeroLength() {
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    FieldMetadataExtractor.extract(TestFixtures.InvalidValidationRecord.class);
                });
    }

    @Test
    @DisplayName("Should throw exception for precision greater than or equal to length")
    void shouldThrowExceptionForPrecisionGreaterThanOrEqualToLength() {
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    FieldMetadataExtractor.extract(TestFixtures.InvalidValidationRecord.class);
                });
    }

    @Test
    @DisplayName("Should throw exception for negative precision")
    void shouldThrowExceptionForNegativePrecision() {
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    FieldMetadataExtractor.extract(TestFixtures.InvalidValidationRecord.class);
                });
    }

    @Test
    @DisplayName("Should throw exception for LocalDate format length mismatch")
    void shouldThrowExceptionForLocalDateFormatLengthMismatch() {
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    FieldMetadataExtractor.extract(TestFixtures.InvalidValidationRecord.class);
                });
    }

    @Test
    @DisplayName("Should throw exception for LocalDateTime format length mismatch")
    void shouldThrowExceptionForLocalDateTimeFormatLengthMismatch() {
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    FieldMetadataExtractor.extract(TestFixtures.InvalidValidationRecord.class);
                });
    }

    @Test
    @DisplayName("Should throw exception for invalid date format")
    void shouldThrowExceptionForInvalidDateFormat() {
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    FieldMetadataExtractor.extract(TestFixtures.InvalidValidationRecord.class);
                });
    }

    @Test
    @DisplayName("Should throw exception for unsupported field type Object")
    void shouldThrowExceptionForUnsupportedObjectType() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            FieldMetadataExtractor.extract(
                                    TestFixtures.UnsupportedObjectRecord.class);
                        });

        assertTrue(
                exception
                        .getMessage()
                        .contains(
                                "Field 'unsupportedObjectField': unsupported field type 'Object'"));
        assertTrue(
                exception
                        .getMessage()
                        .contains(
                                "Supported types: String, Integer/int, Long/long, BigInteger,"
                                        + " BigDecimal, LocalDate, LocalDateTime"));
    }

    @Test
    @DisplayName("Should throw exception for unsupported field type Double")
    void shouldThrowExceptionForUnsupportedDoubleType() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            FieldMetadataExtractor.extract(
                                    TestFixtures.UnsupportedDoubleRecord.class);
                        });

        assertTrue(
                exception
                        .getMessage()
                        .contains(
                                "Field 'unsupportedDoubleField': unsupported field type 'Double'"));
        assertTrue(
                exception
                        .getMessage()
                        .contains(
                                "Supported types: String, Integer/int, Long/long, BigInteger,"
                                        + " BigDecimal, LocalDate, LocalDateTime"));
    }

    @Test
    @DisplayName("Should throw exception for unsupported field type Float")
    void shouldThrowExceptionForUnsupportedFloatType() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            FieldMetadataExtractor.extract(
                                    TestFixtures.UnsupportedFloatRecord.class);
                        });

        assertTrue(
                exception
                        .getMessage()
                        .contains("Field 'unsupportedFloatField': unsupported field type 'Float'"));
        assertTrue(
                exception
                        .getMessage()
                        .contains(
                                "Supported types: String, Integer/int, Long/long, BigInteger,"
                                        + " BigDecimal, LocalDate, LocalDateTime"));
    }

    @Test
    @DisplayName("Should throw exception for unsupported field type Boolean")
    void shouldThrowExceptionForUnsupportedBooleanType() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            FieldMetadataExtractor.extract(
                                    TestFixtures.UnsupportedBooleanRecord.class);
                        });

        assertTrue(
                exception
                        .getMessage()
                        .contains(
                                "Field 'unsupportedBooleanField': unsupported field type"
                                        + " 'Boolean'"));
        assertTrue(
                exception
                        .getMessage()
                        .contains(
                                "Supported types: String, Integer/int, Long/long, BigInteger,"
                                        + " BigDecimal, LocalDate, LocalDateTime"));
    }

    @Test
    @DisplayName("Should throw exception for unsupported field type List")
    void shouldThrowExceptionForUnsupportedListType() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            FieldMetadataExtractor.extract(
                                    TestFixtures.UnsupportedListRecord.class);
                        });

        assertTrue(
                exception
                        .getMessage()
                        .contains("Field 'unsupportedListField': unsupported field type 'List'"));
        assertTrue(
                exception
                        .getMessage()
                        .contains(
                                "Supported types: String, Integer/int, Long/long, BigInteger,"
                                        + " BigDecimal, LocalDate, LocalDateTime"));
    }

    @Test
    @DisplayName("Should validate correct LocalDate format length")
    void shouldValidateCorrectLocalDateFormatLength() {
        // This should not throw an exception
        List<FieldMetadata> metadata =
                FieldMetadataExtractor.extract(TestFixtures.TestRecord.class);
        assertFalse(metadata.isEmpty());

        // Find the date field and verify it has correct format
        FieldMetadata dateField =
                metadata.stream().filter(m -> m.getName().equals("date")).findFirst().orElse(null);

        assertNotNull(dateField);
        assertEquals("yyyyMMdd", dateField.getDateTimeFormat());
        assertEquals(8, dateField.getLength());
    }

    @Test
    @DisplayName("Should validate correct LocalDateTime format length")
    void shouldValidateCorrectLocalDateTimeFormatLength() {
        // This should not throw an exception
        List<FieldMetadata> metadata =
                FieldMetadataExtractor.extract(TestFixtures.ComprehensiveTestRecord.class);
        assertFalse(metadata.isEmpty());

        // Find the dateTime field and verify it has correct format
        FieldMetadata dateTimeField =
                metadata.stream()
                        .filter(m -> m.getName().equals("dateTimeField"))
                        .findFirst()
                        .orElse(null);

        assertNotNull(dateTimeField);
        assertEquals("yyyyMMddHHmmss", dateTimeField.getDateTimeFormat());
        assertEquals(14, dateTimeField.getLength());
    }
}
