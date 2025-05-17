package com.github.allanhladki.flat2java.core;

import com.github.allanhladki.flat2java.markers.FlatField;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FieldMetadataExtractorTest {

    /**
     * Test fixture class with annotated fields.
     */
    private static class TestRecord {
        @FlatField(position = 1, length = 10)
        private String id;

        @FlatField(position = 2, length = 8, dateTimeFormat = "yyyyMMdd")
        private LocalDate date;

        @FlatField(position = 3, length = 5)
        private Integer quantity;

        @FlatField(position = 5, length = 8, dateTimeFormat = "ddMMyyyy")
        private LocalDate expiryDate;

        // The order is wrong on purpose 
        @FlatField(position = 4, length = 10, precision = 2)
        private BigDecimal price;
        
        // Non-annotated field
        @SuppressWarnings("unused")
        private String ignored;
    }

    @Test
    void shouldExtractAnnotatedFields() {
        
        List<FieldMetadata> metadata = FieldMetadataExtractor.extract(TestRecord.class);

        assertNotNull(metadata, "Metadata list should not be null");
        assertEquals(5, metadata.size(), "Should find exactly 5 annotated fields");

        // Verify first field (id)
        FieldMetadata idField = metadata.get(0);
        assertEquals("id", idField.getName());
        assertEquals(1, idField.getPosition());
        assertEquals(10, idField.getLength());
        assertEquals(0, idField.getPrecision());
        assertTrue(idField.getDateTimeFormat().isEmpty());

        // Verify second field (date)
        FieldMetadata dateField = metadata.get(1);
        assertEquals("date", dateField.getName());
        assertEquals(2, dateField.getPosition());
        assertEquals(8, dateField.getLength());
        assertEquals(0, dateField.getPrecision());
        assertEquals("yyyyMMdd", dateField.getDateTimeFormat());

        // Verify third field (quantity)
        FieldMetadata quantityField = metadata.get(2);
        assertEquals("quantity", quantityField.getName());
        assertEquals(3, quantityField.getPosition());
        assertEquals(5, quantityField.getLength());
        assertEquals(0, quantityField.getPrecision());
        assertTrue(quantityField.getDateTimeFormat().isEmpty());

        // Verify fourth field (price)
        FieldMetadata priceField = metadata.get(3);
        assertEquals("price", priceField.getName());
        assertEquals(4, priceField.getPosition());
        assertEquals(10, priceField.getLength());
        assertEquals(2, priceField.getPrecision());
        assertTrue(priceField.getDateTimeFormat().isEmpty());

        // Verify fifth field (expiryDate)
        FieldMetadata expiryDateField = metadata.get(4);
        assertEquals("expiryDate", expiryDateField.getName());
        assertEquals(5, expiryDateField.getPosition());
        assertEquals(8, expiryDateField.getLength());
        assertEquals(0, expiryDateField.getPrecision());
        assertEquals("ddMMyyyy", expiryDateField.getDateTimeFormat());
    }

    @Test
    void shouldHandleEmptyClass() {

        class EmptyClass {}

        List<FieldMetadata> metadata = FieldMetadataExtractor.extract(EmptyClass.class);

        assertNotNull(metadata, "Metadata list should not be null");
        assertTrue(metadata.isEmpty(), "Should return empty list for class without annotations");
    }

    @Test
    void shouldThrowNullPointerExceptionForNullClass() {
        assertThrows(NullPointerException.class, () -> FieldMetadataExtractor.extract(null));
    }
} 