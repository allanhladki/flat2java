package com.github.allanhladki.flat2java.fixtures;

import com.github.allanhladki.flat2java.markers.FlatField;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Unified test fixtures for flat2java library tests.
 * This class contains all test classes used across different test files.
 */
public class TestFixtures {

    /** Test fixture class with annotated fields. */
    public static class TestRecord {
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

        // Constructors
        public TestRecord() {}

        public TestRecord(String id, LocalDate date, Integer quantity, BigDecimal price, LocalDate expiryDate) {
            this.id = id;
            this.date = date;
            this.quantity = quantity;
            this.price = price;
            this.expiryDate = expiryDate;
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public LocalDate getExpiryDate() { return expiryDate; }
        public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    }

    /** Test fixture class with all supported field types for comprehensive testing. */
    public static class ComprehensiveTestRecord {
        @FlatField(length = 8)
        public String stringField;

        @FlatField(length = 8)
        public Integer intField;

        @FlatField(length = 9)
        public Long longField;

        @FlatField(length = 9)
        public BigInteger bigIntegerField;

        @FlatField(length = 7, precision = 3)
        public BigDecimal bigDecimalField;

        @FlatField(length = 8, dateTimeFormat = "yyyyMMdd")
        public LocalDate dateField;

        @FlatField(length = 14, dateTimeFormat = "yyyyMMddHHmmss")
        public LocalDateTime dateTimeField;

        @FlatField(length = 8)
        public LocalDate dateFieldNoFormat;

        // Constructors
        public ComprehensiveTestRecord() {}

        public ComprehensiveTestRecord(String stringField, Integer intField, Long longField, 
                                     BigInteger bigIntegerField, BigDecimal bigDecimalField, 
                                     LocalDate dateField, LocalDateTime dateTimeField, LocalDate dateFieldNoFormat) {
            this.stringField = stringField;
            this.intField = intField;
            this.longField = longField;
            this.bigIntegerField = bigIntegerField;
            this.bigDecimalField = bigDecimalField;
            this.dateField = dateField;
            this.dateTimeField = dateTimeField;
            this.dateFieldNoFormat = dateFieldNoFormat;
        }
    }

    /** Test fixture class for inheritance testing - Superclass. */
    public static class SuperRecord {
        @FlatField(length = 5)
        private String superField1;

        @FlatField(length = 5)
        private String superField2;

        public SuperRecord() {}

        public SuperRecord(String superField1, String superField2) {
            this.superField1 = superField1;
            this.superField2 = superField2;
        }

        public String getSuperField1() { return superField1; }
        public void setSuperField1(String superField1) { this.superField1 = superField1; }
        public String getSuperField2() { return superField2; }
        public void setSuperField2(String superField2) { this.superField2 = superField2; }
    }

    /** Test fixture class for inheritance testing - Subclass. */
    public static class SubRecord extends SuperRecord {
        @FlatField(length = 5)
        private String subField1;

        @FlatField(length = 5)
        private String subField2;

        public SubRecord() {}

        public SubRecord(String superField1, String superField2, String subField1, String subField2) {
            super(superField1, superField2);
            this.subField1 = subField1;
            this.subField2 = subField2;
        }

        public String getSubField1() { return subField1; }
        public void setSubField1(String subField1) { this.subField1 = subField1; }
        public String getSubField2() { return subField2; }
        public void setSubField2(String subField2) { this.subField2 = subField2; }
    }

    /** Test fixture class with no annotated fields. */
    public static class EmptyRecord {
        private String field1;
        private Integer field2;

        public EmptyRecord() {}

        public String getField1() { return field1; }
        public void setField1(String field1) { this.field1 = field1; }
        public Integer getField2() { return field2; }
        public void setField2(Integer field2) { this.field2 = field2; }
    }

    /** Test fixture class with no fields at all. */
    public static class EmptyClass {
        // No fields
    }

    /** Test fixture class for validation testing - invalid configurations. */
    public static class InvalidValidationRecord {
        @FlatField(length = 0) // Invalid: length must be positive
        public String invalidLengthField;

        @FlatField(length = 5, precision = 5) // Invalid: precision >= length
        public BigDecimal invalidPrecisionField;

        @FlatField(length = 5, precision = -1) // Invalid: negative precision
        public BigDecimal negativePrecisionField;

        @FlatField(length = 8, dateTimeFormat = "yyyyMMdd") // Valid format
        public LocalDate validDateField;

        @FlatField(length = 6, dateTimeFormat = "yyyyMMdd") // Invalid: format produces 8 chars, length is 6
        public LocalDate invalidDateField;

        @FlatField(length = 14, dateTimeFormat = "yyyyMMddHHmmss") // Valid format
        public LocalDateTime validDateTimeField;

        @FlatField(length = 10, dateTimeFormat = "yyyyMMddHHmmss") // Invalid: format produces 14 chars, length is 10
        public LocalDateTime invalidDateTimeField;

        @FlatField(length = 5, dateTimeFormat = "invalid") // Invalid: invalid format
        public LocalDate invalidFormatField;

        public InvalidValidationRecord() {}
    }

    /** Test fixture class for zero value testing. */
    public static class ZeroValueRecord {
        @FlatField(length = 5)
        public Integer zeroInteger;

        @FlatField(length = 5)
        public Long zeroLong;

        @FlatField(length = 5)
        public BigInteger zeroBigInteger;

        @FlatField(length = 5, precision = 0)
        public BigDecimal zeroBigDecimalNoPrecision;

        @FlatField(length = 7, precision = 2)
        public BigDecimal zeroBigDecimalWithPrecision;

        public ZeroValueRecord() {}

        public ZeroValueRecord(Integer zeroInteger, Long zeroLong, BigInteger zeroBigInteger, 
                             BigDecimal zeroBigDecimalNoPrecision, BigDecimal zeroBigDecimalWithPrecision) {
            this.zeroInteger = zeroInteger;
            this.zeroLong = zeroLong;
            this.zeroBigInteger = zeroBigInteger;
            this.zeroBigDecimalNoPrecision = zeroBigDecimalNoPrecision;
            this.zeroBigDecimalWithPrecision = zeroBigDecimalWithPrecision;
        }
    }
} 