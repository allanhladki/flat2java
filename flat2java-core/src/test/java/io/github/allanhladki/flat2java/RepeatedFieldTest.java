package io.github.allanhladki.flat2java;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.allanhladki.flat2java.annotation.FlatField;
import io.github.allanhladki.flat2java.exception.DeserializationException;
import io.github.allanhladki.flat2java.exception.MappingException;
import io.github.allanhladki.flat2java.exception.SerializationException;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class RepeatedFieldTest {

    private final Flat2Java flat = Flat2Java.create();

    private void assertMappingError(Class<?> type, String message) {
        assertThatThrownBy(() -> flat.recordLength(type))
                .isInstanceOf(MappingException.class)
                .hasMessage(message);
    }

    static class Item {
        @FlatField(length = 3)
        String sku;

        @FlatField(length = 2)
        int quantity;
    }

    static class Invoice {
        @FlatField(length = 4)
        int id;

        @FlatField(length = 3, occurs = 2)
        List<String> codes;

        @FlatField(length = 3, occurs = 3)
        int[] counts;

        @FlatField(length = 5, precision = 2, occurs = 2)
        BigDecimal[] amounts;

        @FlatField(length = 8, dateTimeFormat = "yyyyMMdd", occurs = 1)
        List<LocalDate> dates;

        @FlatField(length = 5, occurs = 2)
        List<Item> items;
    }

    private static Item item(String sku, int quantity) {
        Item item = new Item();
        item.sku = sku;
        item.quantity = quantity;
        return item;
    }

    private static Invoice invoice() {
        Invoice invoice = new Invoice();
        invoice.id = 12;
        invoice.codes = List.of("AB", "CDE");
        invoice.counts = new int[] {1, 20, 300};
        invoice.amounts = new BigDecimal[] {new BigDecimal("1.50"), new BigDecimal("99.99")};
        invoice.dates = List.of(LocalDate.of(2024, 3, 15));
        invoice.items = List.of(item("X1", 5), item("Y22", 10));
        return invoice;
    }

    private static final String LINE =
            "0012" + "AB CDE" + "001020300" + "0015009999" + "20240315" + "X1 05Y2210";

    @Test
    void elementsAreWrittenOneAfterTheOther() {
        assertThat(flat.write(invoice())).isEqualTo(LINE);
        assertThat(flat.recordLength(Invoice.class)).isEqualTo(LINE.length());
    }

    @Test
    void roundTrip() {
        Invoice copy = flat.read(LINE, Invoice.class);

        assertThat(copy).usingRecursiveComparison().isEqualTo(invoice());
        assertThat(copy.items).isInstanceOf(ArrayList.class);
        copy.codes.add("mutable");
    }

    @Test
    void listWithAnotherSizeIsRejected() {
        Invoice invoice = invoice();
        invoice.codes = List.of("AB");

        assertThatThrownBy(() -> flat.write(invoice))
                .isInstanceOf(SerializationException.class)
                .hasMessage("Invoice.codes: has 1 elements, but occurs is 2");
    }

    @Test
    void arrayWithAnotherSizeIsRejected() {
        Invoice invoice = invoice();
        invoice.counts = new int[] {1, 2, 3, 4};

        assertThatThrownBy(() -> flat.write(invoice))
                .isInstanceOf(SerializationException.class)
                .hasMessage("Invoice.counts: has 4 elements, but occurs is 3");
    }

    @Test
    void nullListIsRejected() {
        Invoice invoice = invoice();
        invoice.items = null;

        assertThatThrownBy(() -> flat.write(invoice))
                .isInstanceOf(SerializationException.class)
                .hasMessage("Invoice.items: null is not allowed on List and array fields");
    }

    @Test
    void nullStringElementIsWrittenAsAnEmptyColumn() {
        Invoice invoice = invoice();
        invoice.codes = Arrays.asList(null, "CDE");

        Invoice copy = flat.read(flat.write(invoice), Invoice.class);

        assertThat(copy.codes).containsExactly("", "CDE");
    }

    @Test
    void invalidElementReportsItsColumns() {
        String line = LINE.substring(0, 10) + "0x1" + LINE.substring(13);

        assertThatThrownBy(() -> flat.read(line, Invoice.class))
                .isInstanceOf(DeserializationException.class)
                .hasMessageContaining("Invoice.counts (columns 11-13)");
    }

    static class MissingOccurs {
        @FlatField(length = 3)
        List<String> codes;
    }

    @Test
    void occursIsRequiredOnLists() {
        assertMappingError(
                MissingOccurs.class,
                "MissingOccurs.codes: occurs is required on List and array fields and must be"
                        + " greater than 0, got 0");
    }

    static class OccursOnScalar {
        @FlatField(length = 3, occurs = 2)
        String code;
    }

    @Test
    void occursIsOnlyAllowedOnListsAndArrays() {
        assertMappingError(
                OccursOnScalar.class,
                "OccursOnScalar.code: occurs is only allowed on List and array fields");
    }

    static class TooWide {
        @FlatField(length = 100_000, occurs = 100_000)
        List<String> codes;
    }

    @Test
    void totalWidthMustFitAnInt() {
        assertMappingError(
                TooWide.class, "TooWide.codes: length * occurs exceeds 2147483647 characters");
    }

    static class ElementAttributes {
        @FlatField(length = 3, precision = 3, occurs = 2)
        List<BigDecimal> amounts;
    }

    @Test
    void elementAttributesAreValidated() {
        assertMappingError(
                ElementAttributes.class,
                "ElementAttributes.amounts: precision (3) must be smaller than length (3)");
    }

    static class NestedLengthPerElement {
        @FlatField(length = 10, occurs = 2)
        List<Item> items;
    }

    @Test
    void nestedElementLengthIsTheLengthOfOneElement() {
        assertMappingError(
                NestedLengthPerElement.class,
                "NestedLengthPerElement.items: length is 10, but the fields of Item add up to 5");
    }

    static class RawList {
        @SuppressWarnings("rawtypes")
        @FlatField(length = 3, occurs = 2)
        List codes;
    }

    static class WildcardList {
        @FlatField(length = 3, occurs = 2)
        List<? extends CharSequence> codes;
    }

    static class ListOfLists {
        @FlatField(length = 3, occurs = 2)
        List<List<String>> codes;
    }

    static class ArrayOfArrays {
        @FlatField(length = 3, occurs = 2)
        int[][] codes;
    }

    static class Generic<T> {
        @FlatField(length = 3, occurs = 2)
        List<T> values;
    }

    static class OtherCollection {
        @FlatField(length = 3, occurs = 2)
        Collection<String> codes;
    }

    static class MapField {
        @FlatField(length = 3, occurs = 2)
        Map<String, String> codes;
    }

    @Test
    void unsupportedElementTypesAreRejected() {
        String supported =
                ". Supported types: String, int/Integer, long/Long, BigInteger, BigDecimal,"
                        + " LocalDate, LocalDateTime, a concrete class or record with @FlatField"
                        + " fields, or a List or array of those types.";
        assertMappingError(
                RawList.class,
                "RawList.codes: unsupported type java.util.List (declare the element type, as in"
                        + " List<String>)"
                        + supported);
        assertMappingError(
                WildcardList.class,
                "WildcardList.codes: unsupported type java.util.List<? extends"
                        + " java.lang.CharSequence> (declare the element type without a"
                        + " wildcard)"
                        + supported);
        assertMappingError(
                ListOfLists.class,
                "ListOfLists.codes: unsupported type"
                    + " java.util.List<java.util.List<java.lang.String>> (the elements of a List or"
                    + " array cannot be Lists or arrays)"
                        + supported);
        assertMappingError(
                ArrayOfArrays.class,
                "ArrayOfArrays.codes: unsupported type int[][] (the elements of a List or array"
                        + " cannot be Lists or arrays)"
                        + supported);
        assertMappingError(
                Generic.class,
                "Generic.values: unsupported type T (type variables cannot be instantiated when"
                        + " reading)"
                        + supported);
        assertThatThrownBy(() -> flat.recordLength(OtherCollection.class))
                .isInstanceOf(MappingException.class)
                .hasMessageStartingWith("OtherCollection.codes: occurs is only allowed on List");
        assertThatThrownBy(() -> flat.recordLength(MapField.class))
                .isInstanceOf(MappingException.class)
                .hasMessageStartingWith("MapField.codes: occurs is only allowed on List");
    }
}
