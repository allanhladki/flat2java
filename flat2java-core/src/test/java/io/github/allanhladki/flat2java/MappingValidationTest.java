package io.github.allanhladki.flat2java;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.allanhladki.flat2java.annotation.FlatField;
import io.github.allanhladki.flat2java.exception.MappingException;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Invalid {@code @FlatField} declarations are reported as {@link MappingException}. */
class MappingValidationTest {

    private final Flat2Java flat = Flat2Java.create();

    private void assertMappingError(Class<?> type, String message) {
        assertThatThrownBy(() -> flat.recordLength(type))
                .isInstanceOf(MappingException.class)
                .hasMessage(message);
    }

    static class VariableWidthDate {
        @FlatField(length = 8, dateTimeFormat = "d/M/yyyy")
        LocalDate date;
    }

    @Test
    void variableWidthDatePatternIsRejected() {
        assertMappingError(
                VariableWidthDate.class,
                "VariableWidthDate.date: dateTimeFormat 'd/M/yyyy' has a variable width"
                        + " ('1/1/2024' vs '28/12/2024'); use fixed-width pattern letters such"
                        + " as dd, MM, HH");
    }

    static class WrongDateLength {
        @FlatField(length = 10, dateTimeFormat = "yyyyMMdd")
        LocalDate date;
    }

    @Test
    void datePatternWidthMustMatchLength() {
        assertMappingError(
                WrongDateLength.class,
                "WrongDateLength.date: dateTimeFormat 'yyyyMMdd' produces 8 characters, but"
                        + " length is 10");
    }

    static class DateWithoutFormat {
        @FlatField(length = 8)
        LocalDate date;
    }

    @Test
    void dateWithoutPatternIsRejected() {
        assertMappingError(
                DateWithoutFormat.class,
                "DateWithoutFormat.date: dateTimeFormat is required for LocalDate/LocalDateTime"
                        + " fields");
    }

    static class DateTimeWithDatePattern {
        @FlatField(length = 8, dateTimeFormat = "yyyyMMdd")
        LocalDateTime at;
    }

    @Test
    void dateTimePatternMustContainTheTime() {
        assertThatThrownBy(() -> flat.recordLength(DateTimeWithDatePattern.class))
                .isInstanceOf(MappingException.class)
                .hasMessageStartingWith(
                        "DateTimeWithDatePattern.at: dateTimeFormat 'yyyyMMdd' cannot represent a"
                                + " LocalDateTime");
    }

    static class InvalidPattern {
        @FlatField(length = 8, dateTimeFormat = "yyyyMMdd{")
        LocalDate date;
    }

    @Test
    void invalidPatternIsRejected() {
        assertThatThrownBy(() -> flat.recordLength(InvalidPattern.class))
                .isInstanceOf(MappingException.class)
                .hasMessageStartingWith("InvalidPattern.date: invalid dateTimeFormat 'yyyyMMdd{'");
    }

    static class FormatOnString {
        @FlatField(length = 8, dateTimeFormat = "yyyyMMdd")
        String text;
    }

    @Test
    void patternOnNonDateIsRejected() {
        assertMappingError(
                FormatOnString.class,
                "FormatOnString.text: dateTimeFormat is only allowed on LocalDate/LocalDateTime"
                        + " fields");
    }

    static class PrecisionOnInt {
        @FlatField(length = 5, precision = 2)
        int value;
    }

    @Test
    void precisionOutsideBigDecimalIsRejected() {
        assertMappingError(
                PrecisionOnInt.class,
                "PrecisionOnInt.value: precision is only allowed on BigDecimal fields");
    }

    static class PrecisionTooLarge {
        @FlatField(length = 5, precision = 5)
        BigDecimal value;
    }

    @Test
    void precisionMustBeSmallerThanLength() {
        assertMappingError(
                PrecisionTooLarge.class,
                "PrecisionTooLarge.value: precision (5) must be smaller than length (5)");
    }

    static class NegativePrecision {
        @FlatField(length = 5, precision = -1)
        BigDecimal value;
    }

    @Test
    void negativePrecisionIsRejected() {
        assertMappingError(
                NegativePrecision.class,
                "NegativePrecision.value: precision must not be negative, got -1");
    }

    static class ZeroLength {
        @FlatField(length = 0)
        String text;
    }

    @Test
    void lengthMustBePositive() {
        assertMappingError(
                ZeroLength.class, "ZeroLength.text: length must be greater than 0, got 0");
    }

    static class PaddingOnNumber {
        @FlatField(length = 5, padding = ' ')
        int ok;

        @FlatField(length = 5, padding = '*')
        int value;
    }

    @Test
    void paddingOnNumbersIsRejected() {
        assertMappingError(
                PaddingOnNumber.class,
                "PaddingOnNumber.value: padding is only allowed on String fields; numeric fields"
                        + " are always zero-filled");
    }

    static class StaticField {
        @FlatField(length = 5)
        static String constant;

        @FlatField(length = 5)
        String value;
    }

    @Test
    void staticFieldsAreRejected() {
        assertMappingError(
                StaticField.class,
                "StaticField.constant: @FlatField is not allowed on static fields");
    }

    static class Unsupported {
        @FlatField(length = 1)
        boolean flag;
    }

    @Test
    void unsupportedTypesAreRejected() {
        assertMappingError(
                Unsupported.class,
                "Unsupported.flag: unsupported type boolean. Supported types: String,"
                        + " int/Integer, long/Long, BigInteger, BigDecimal, LocalDate,"
                        + " LocalDateTime, a concrete class or record with @FlatField fields, or"
                        + " a List or array of those types.");
    }

    static class NoFields {
        String ignored;
    }

    @Test
    void classesWithoutAnnotatedFieldsAreRejected() {
        assertMappingError(NoFields.class, "NoFields: no fields annotated with @FlatField");
    }

    class Inner {
        @FlatField(length = 1)
        String value;
    }

    @Test
    void innerClassesNeedToBeStatic() {
        assertMappingError(
                Inner.class,
                "Inner: a no-arg constructor is required (inner classes must be declared static)");
    }

    static class NoDefaultConstructor {
        @FlatField(length = 1)
        String value;

        NoDefaultConstructor(String value) {
            this.value = value;
        }
    }

    @Test
    void noArgConstructorIsRequired() {
        assertMappingError(
                NoDefaultConstructor.class,
                "NoDefaultConstructor: a no-arg constructor is required");
    }

    abstract static class Shape {
        @FlatField(length = 1)
        String kind;
    }

    @Test
    void abstractClassesCannotBeMapped() {
        assertMappingError(Shape.class, "Shape: a mapped type must be a concrete class");
    }

    static class MixedPositions {
        @FlatField(position = 1, length = 1)
        String a;

        @FlatField(length = 1)
        String b;
    }

    @Test
    void positionsAreAllOrNothing() {
        assertMappingError(
                MixedPositions.class,
                "MixedPositions: either every @FlatField sets position or none does; missing on"
                        + " MixedPositions.b");
    }

    static class DuplicatePositions {
        @FlatField(position = 1, length = 1)
        String a;

        @FlatField(position = 1, length = 1)
        String b;
    }

    @Test
    void positionsAreUnique() {
        assertMappingError(
                DuplicatePositions.class,
                "DuplicatePositions: position 1 is used by both DuplicatePositions.a and"
                        + " DuplicatePositions.b");
    }

    static class GapInPositions {
        @FlatField(position = 1, length = 1)
        String a;

        @FlatField(position = 3, length = 1)
        String b;
    }

    @Test
    void positionsAreContiguous() {
        assertMappingError(
                GapInPositions.class,
                "GapInPositions: positions must be contiguous starting at 1; position 2 is"
                        + " missing, found [1, 3]");
    }

    static class PositionedBase {
        @FlatField(position = 2, length = 1)
        String base;
    }

    static class PositionedDerived extends PositionedBase {
        @FlatField(length = 1)
        String derived;
    }

    @Test
    void positionRulesCoverTheWholeHierarchy() {
        assertMappingError(
                PositionedDerived.class,
                "PositionedDerived: either every @FlatField sets position or none does; missing on"
                        + " PositionedDerived.derived");
    }
}
