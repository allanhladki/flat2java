package io.github.allanhladki.flat2java;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.allanhladki.flat2java.annotation.FlatField;
import io.github.allanhladki.flat2java.exception.MappingException;
import io.github.allanhladki.flat2java.exception.SerializationException;

import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

class NestedRecordTest {

    private final Flat2Java flat = Flat2Java.create();

    static class Address {
        @FlatField(length = 10)
        String city;

        @FlatField(length = 2)
        String state;
    }

    static class Person {
        @FlatField(length = 8)
        String name;
    }

    static class Customer extends Person {
        @FlatField(length = 12)
        Address address;
    }

    static class Order {
        @FlatField(length = 4)
        int id;

        @FlatField(length = 20)
        Customer customer;

        @FlatField(length = 6, precision = 2)
        BigDecimal total;

        @FlatField(length = 8, dateTimeFormat = "yyyyMMdd")
        LocalDate date;
    }

    private static Order order() {
        Address address = new Address();
        address.city = "Curitiba";
        address.state = "PR";
        Customer customer = new Customer();
        customer.name = "Ana";
        customer.address = address;
        Order order = new Order();
        order.id = 7;
        order.customer = customer;
        order.total = new BigDecimal("15.90");
        order.date = LocalDate.of(2024, 5, 1);
        return order;
    }

    @Test
    void nestedFieldsAreInlined() {
        assertThat(flat.write(order())).isEqualTo("0007Ana     Curitiba  PR00159020240501");
    }

    @Test
    void twoLevelsRoundTrip() {
        Order copy = flat.read(flat.write(order()), Order.class);

        assertThat(copy).usingRecursiveComparison().isEqualTo(order());
    }

    @Test
    void recordLengthIncludesNestedFields() {
        assertThat(flat.recordLength(Order.class)).isEqualTo(38);
    }

    @Test
    void nullNestedRecordFails() {
        Order order = order();
        order.customer = null;

        assertThatThrownBy(() -> flat.write(order))
                .isInstanceOf(SerializationException.class)
                .hasMessage("Order.customer: null is not allowed on nested record fields");
    }

    static class PositionedPair {
        @FlatField(position = 2, length = 1)
        String b;

        @FlatField(position = 1, length = 1)
        String a;
    }

    static class Holder {
        @FlatField(length = 1)
        String first;

        @FlatField(length = 2)
        PositionedPair pair;
    }

    @Test
    void nestedPositionsAreIndependentOfTheParent() {
        Holder h = new Holder();
        h.first = "X";
        h.pair = new PositionedPair();
        h.pair.a = "A";
        h.pair.b = "B";

        assertThat(flat.write(h)).isEqualTo("XAB");
    }

    static class WrongLength {
        @FlatField(length = 10)
        Address address;
    }

    @Test
    void lengthMustEqualTheNestedFieldsSum() {
        assertThatThrownBy(() -> flat.recordLength(WrongLength.class))
                .isInstanceOf(MappingException.class)
                .hasMessage(
                        "WrongLength.address: length is 10, but the fields of Address add up to"
                                + " 12");
    }

    static class NestedWithAttributes {
        @FlatField(length = 12, padding = '*')
        Address address;
    }

    @Test
    void scalarAttributesAreNotAllowedOnNestedFields() {
        assertThatThrownBy(() -> flat.recordLength(NestedWithAttributes.class))
                .isInstanceOf(MappingException.class)
                .hasMessage(
                        "NestedWithAttributes.address: precision, dateTimeFormat and padding are"
                                + " not allowed on a nested record field");
    }

    static class Node {
        @FlatField(length = 1)
        String value;

        @FlatField(length = 2)
        Link next;
    }

    static class Link {
        @FlatField(length = 1)
        Node node;
    }

    @Test
    void cyclesAreRejected() {
        assertThatThrownBy(() -> flat.recordLength(Node.class))
                .isInstanceOf(MappingException.class)
                .hasMessage("Cyclic nested records: Node -> Link -> Node");
    }

    static class ObjectField {
        @FlatField(length = 5)
        Object value;
    }

    @Test
    void objectFieldsAreRejected() {
        assertThatThrownBy(() -> flat.recordLength(ObjectField.class))
                .isInstanceOf(MappingException.class)
                .hasMessageStartingWith(
                        "ObjectField.value: unsupported type java.lang.Object (declare the"
                                + " concrete class of the nested record)");
    }

    static class GenericField<T> {
        @FlatField(length = 5)
        T value;
    }

    @Test
    void typeVariablesAreRejected() {
        assertThatThrownBy(() -> flat.recordLength(GenericField.class))
                .isInstanceOf(MappingException.class)
                .hasMessageStartingWith(
                        "GenericField.value: unsupported type T (type variables cannot be"
                                + " instantiated when reading)");
    }

    static class InterfaceField {
        @FlatField(length = 5)
        Serializable value;
    }

    @Test
    void abstractTypesAreRejected() {
        assertThatThrownBy(() -> flat.recordLength(InterfaceField.class))
                .isInstanceOf(MappingException.class)
                .hasMessageStartingWith(
                        "InterfaceField.value: unsupported type java.io.Serializable (abstract"
                                + " types cannot be instantiated when reading)");
    }

    static class UnmappedType {
        @FlatField(length = 5)
        StringBuilder value;
    }

    @Test
    void classesWithoutAnnotatedFieldsAreUnsupported() {
        assertThatThrownBy(() -> flat.recordLength(UnmappedType.class))
                .isInstanceOf(MappingException.class)
                .hasMessageStartingWith(
                        "UnmappedType.value: unsupported type java.lang.StringBuilder. Supported"
                                + " types:");
    }

    // Package-private classes with default constructors, and an unannotated field that is ignored.
    static class Brand {
        @FlatField(length = 10)
        String name;

        @FlatField(length = 4)
        int id;
    }

    static class Car {
        int price;

        @FlatField(length = 14)
        Brand brand;
    }

    @Test
    void unannotatedFieldsAreLeftOut() {
        Car car = new Car();
        car.price = 50_000;
        car.brand = new Brand();
        car.brand.name = "Fiat";
        car.brand.id = 12;

        String line = flat.write(car);
        Car copy = flat.read(line, Car.class);

        assertThat(line).isEqualTo("Fiat      0012");
        assertThat(copy.price).isZero();
        assertThat(copy.brand).usingRecursiveComparison().isEqualTo(car.brand);
    }
}
