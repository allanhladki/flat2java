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
        @FlatField(length = 10)
        private String id;

        @FlatField(length = 8, dateTimeFormat = "yyyyMMdd")
        private LocalDate date;

        @FlatField(length = 5)
        private Integer quantity;

        @FlatField(length = 10, precision = 2)
        private BigDecimal price;

        @FlatField(length = 8, dateTimeFormat = "ddMMyyyy")
        private LocalDate expiryDate;

        // Non-annotated field
        @SuppressWarnings("unused")
        private String ignored;
    }

    @Test
    void shouldExtractAnnotatedFields() {
        // when
        List<FieldMetadata> metadata = FieldMetadataExtractor.extract(TestRecord.class);

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
    void shouldHandleEmptyClass() {
        // given
        class EmptyClass {}

        // when
        List<FieldMetadata> metadata = FieldMetadataExtractor.extract(EmptyClass.class);

        // then
        assertNotNull(metadata, "Metadata list should not be null");
        assertTrue(metadata.isEmpty(), "Should return empty list for class without annotations");
    }

    @Test
    void shouldThrowNullPointerExceptionForNullClass() {
        // when/then
        assertThrows(NullPointerException.class, () -> FieldMetadataExtractor.extract(null));
    }

    @Test
    void shouldExtractFieldsFromSuperclassAndSubclass() {

        class SuperRecord {
            @FlatField(length = 5)
            private String superField1;

            @FlatField(length = 5)
            private String superField2;
        }

        class SubRecord extends SuperRecord {
            @FlatField(length = 5)
            private String subField1;

            @FlatField(length = 5)
            private String subField2;
        }
        
        List<FieldMetadata> metadata = FieldMetadataExtractor.extractIncludingInherited(SubRecord.class);
        assertNotNull(metadata, "Metadata list should not be null");
        assertEquals(4, metadata.size(), "Should find 4 annotated fields from both superclass and subclass");
        assertEquals("superField1", metadata.get(0).getName());
        assertEquals("superField2", metadata.get(1).getName());
        assertEquals("subField1", metadata.get(2).getName());
        assertEquals("subField2", metadata.get(3).getName());
    }
} 