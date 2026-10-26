package io.github.allanhladki.flat2java;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.allanhladki.flat2java.annotation.FlatField;
import io.github.allanhladki.flat2java.exception.DeserializationException;
import io.github.allanhladki.flat2java.exception.MappingException;
import io.github.allanhladki.flat2java.exception.SerializationException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

class Flat2JavaTest {

    private final Flat2Java flat = Flat2Java.create();

    static class AllTypes {
        @FlatField(length = 6)
        String text;

        @FlatField(length = 4)
        int primitiveInt;

        @FlatField(length = 5)
        Integer boxedInt;

        @FlatField(length = 6)
        long primitiveLong;

        @FlatField(length = 7)
        Long boxedLong;

        @FlatField(length = 8)
        BigInteger bigInteger;

        @FlatField(length = 7, precision = 2)
        BigDecimal amount;

        @FlatField(length = 8, dateTimeFormat = "yyyyMMdd")
        LocalDate date;

        @FlatField(length = 14, dateTimeFormat = "yyyyMMddHHmmss")
        LocalDateTime timestamp;
    }

    private static AllTypes sample() {
        AllTypes r = new AllTypes();
        r.text = "ABC";
        r.primitiveInt = 42;
        r.boxedInt = 7;
        r.primitiveLong = 123L;
        r.boxedLong = 9L;
        r.bigInteger = new BigInteger("12345");
        r.amount = new BigDecimal("12.34");
        r.date = LocalDate.of(2024, 3, 15);
        r.timestamp = LocalDateTime.of(2024, 3, 15, 8, 5, 9);
        return r;
    }

    @Test
    void writesEveryTypeWithItsEncoding() {
        assertThat(flat.write(sample()))
                .isEqualTo(
                        "ABC   "
                                + "0042"
                                + "00007"
                                + "000123"
                                + "0000009"
                                + "00012345"
                                + "0001234"
                                + "20240315"
                                + "20240315080509");
    }

    @Test
    void readsBackWhatItWrites() {
        AllTypes copy = flat.read(flat.write(sample()), AllTypes.class);

        assertThat(copy).usingRecursiveComparison().isEqualTo(sample());
    }

    @Test
    void recordLengthIsTheSumOfTheFieldLengths() {
        assertThat(flat.recordLength(AllTypes.class)).isEqualTo(65);
    }

    @Test
    void readRejectsALineOfTheWrongLength() {
        assertThatThrownBy(() -> flat.read("ABC", AllTypes.class))
                .isInstanceOf(DeserializationException.class)
                .hasMessage("AllTypes: expected a record of 65 characters, got 3");
    }

    @Test
    void readReportsTheColumnsOfAnInvalidField() {
        String line = flat.write(sample());
        String broken = line.substring(0, 6) + "00X2" + line.substring(10);

        assertThatThrownBy(() -> flat.read(broken, AllTypes.class))
                .isInstanceOf(DeserializationException.class)
                .hasMessage("AllTypes.primitiveInt (columns 7-10): expected digits only");
    }

    /** Regressions for the bugs found in the 0.0.1-rc2 review. */
    @Nested
    class Regressions {

        static class Primitives {
            @FlatField(length = 5)
            int number;

            @FlatField(length = 5)
            long big;
        }

        static class WholeDecimal {
            @FlatField(length = 12)
            BigDecimal value;
        }

        static class StrictDate {
            @FlatField(length = 8, dateTimeFormat = "yyyyMMdd")
            LocalDate date;
        }

        static class Text {
            @FlatField(length = 5)
            String value;
        }

        @Test
        void primitiveFieldsAreRead() {
            Primitives r = flat.read("0004200007", Primitives.class);

            assertThat(r.number).isEqualTo(42);
            assertThat(r.big).isEqualTo(7L);
        }

        @Test
        void bigDecimalWithoutPrecisionIsNotLimitedToInt() {
            WholeDecimal r = new WholeDecimal();
            r.value = new BigDecimal("4294967338");

            assertThat(flat.write(r)).isEqualTo("004294967338");
            assertThat(flat.read("004294967338", WholeDecimal.class).value)
                    .isEqualTo(new BigDecimal("4294967338"));
        }

        @Test
        void impossibleDatesAreRejected() {
            assertThatThrownBy(() -> flat.read("20240231", StrictDate.class))
                    .isInstanceOf(DeserializationException.class)
                    .hasMessageContaining("not a valid date/time for pattern 'yyyyMMdd'");
        }

        @Test
        void bigDecimalWithoutPrecisionKeepsPlainScaleWithSeparatorEnabled() {
            Flat2Java withSeparator =
                    Flat2Java.create(Flat2JavaOptions.builder().useDecimalSeparator(true).build());
            WholeDecimal r = new WholeDecimal();
            r.value = new BigDecimal("100");

            BigDecimal copy = withSeparator.read(withSeparator.write(r), WholeDecimal.class).value;

            assertThat(copy).isEqualTo(new BigDecimal("100"));
            assertThat(copy.toString()).isEqualTo("100");
        }

        @Test
        void customNullPaddingReadsBackAsEmpty() {
            Flat2Java custom =
                    Flat2Java.create(Flat2JavaOptions.builder().nullValuePadding('*').build());
            Text r = new Text();

            String line = custom.write(r);

            assertThat(line).isEqualTo("*****");
            assertThat(custom.read(line, Text.class).value).isEmpty();
        }

        @Test
        void signsAndSpacesAreNotNumbers() {
            assertThatThrownBy(() -> flat.read("+004200007", Primitives.class))
                    .isInstanceOf(DeserializationException.class);
            assertThatThrownBy(() -> flat.read("   4200007", Primitives.class))
                    .isInstanceOf(DeserializationException.class);
        }
    }

    @Nested
    class Strings {

        @Test
        void truncationDoesNotSplitSurrogatePairs() {
            Short s = new Short();
            s.value = "ab\uD83D\uDE00"; // "ab" followed by an emoji (two UTF-16 units)

            assertThat(flat.write(s)).isEqualTo("ab ");
        }

        static class Padded {
            @FlatField(length = 5, padding = '_')
            String code;
        }

        static class Short {
            @FlatField(length = 3)
            String value;
        }

        @Test
        void customPaddingFillsTheRight() {
            Padded r = new Padded();
            r.code = "AB";

            assertThat(flat.write(r)).isEqualTo("AB___");
            assertThat(flat.read("AB___", Padded.class).code).isEqualTo("AB");
        }

        @Test
        void nullIsWrittenAsPaddingAndReadAsEmpty() {
            assertThat(flat.write(new Short())).isEqualTo("   ");
            assertThat(flat.read("   ", Short.class).value).isEmpty();
        }

        @Test
        void nullFailsWhenNullStringsAreNotAllowed() {
            Flat2Java strict =
                    Flat2Java.create(Flat2JavaOptions.builder().allowNullStrings(false).build());

            assertThatThrownBy(() -> strict.write(new Short()))
                    .isInstanceOf(SerializationException.class)
                    .hasMessage(
                            "Short.value: null is not allowed because allowNullStrings is false");
        }

        @Test
        void longValuesAreTruncatedByDefault() {
            Short r = new Short();
            r.value = "ABCDEF";

            assertThat(flat.write(r)).isEqualTo("ABC");
        }

        @Test
        void longValuesFailWhenTruncationIsDisabled() {
            Flat2Java strict =
                    Flat2Java.create(
                            Flat2JavaOptions.builder().failOnStringTruncation(true).build());
            Short r = new Short();
            r.value = "ABCDEF";

            assertThatThrownBy(() -> strict.write(r))
                    .isInstanceOf(SerializationException.class)
                    .hasMessage(
                            "Short.value: value has 6 characters, but length is 3 and"
                                    + " failOnStringTruncation is true");
        }
    }

    @Nested
    class Numbers {

        static class Count {
            @FlatField(length = 3)
            Integer value;
        }

        static class Small {
            @FlatField(length = 10)
            int value;
        }

        @Test
        void valuesWiderThanTheColumnFail() {
            Count r = new Count();
            r.value = 1234;

            assertThatThrownBy(() -> flat.write(r))
                    .isInstanceOf(SerializationException.class)
                    .hasMessage(
                            "Count.value: value needs 4 characters, but length is 3; numeric"
                                    + " values are never truncated");
        }

        @Test
        void negativeValuesFail() {
            Count r = new Count();
            r.value = -1;

            assertThatThrownBy(() -> flat.write(r))
                    .isInstanceOf(SerializationException.class)
                    .hasMessageContaining("negative values are not supported");
        }

        @Test
        void nullFails() {
            assertThatThrownBy(() -> flat.write(new Count()))
                    .isInstanceOf(SerializationException.class)
                    .hasMessage("Count.value: null is not allowed on numeric fields");
        }

        @Test
        void valuesOutOfRangeForTheJavaTypeFail() {
            assertThatThrownBy(() -> flat.read("9999999999", Small.class))
                    .isInstanceOf(DeserializationException.class)
                    .hasMessage("Small.value (columns 1-10): value is out of range for int");
        }
    }

    @Nested
    class Decimals {

        static class Price {
            @FlatField(length = 8, precision = 2)
            BigDecimal value;
        }

        private Price price(String value) {
            Price p = new Price();
            p.value = new BigDecimal(value);
            return p;
        }

        @Test
        void impliedDecimalPoint() {
            assertThat(flat.write(price("12.34"))).isEqualTo("00001234");
            assertThat(flat.read("00001234", Price.class).value).isEqualTo(new BigDecimal("12.34"));
        }

        @Test
        void wholeNumbersGetTheScaleOfThePrecision() {
            assertThat(flat.write(price("12"))).isEqualTo("00001200");
            assertThat(flat.read("00001200", Price.class).value).isEqualTo(new BigDecimal("12.00"));
        }

        @Test
        void extraDecimalPlacesFailWithTheDefaultRoundingMode() {
            assertThatThrownBy(() -> flat.write(price("1.234")))
                    .isInstanceOf(SerializationException.class)
                    .hasMessageContaining("more than 2 decimal places");
        }

        @Test
        void extraDecimalPlacesAreRoundedWhenConfigured() {
            Flat2Java rounding =
                    Flat2Java.create(
                            Flat2JavaOptions.builder().roundingMode(RoundingMode.HALF_UP).build());

            assertThat(rounding.write(price("1.235"))).isEqualTo("00000124");
        }

        @Test
        void explicitSeparator() {
            Flat2Java comma =
                    Flat2Java.create(
                            Flat2JavaOptions.builder()
                                    .useDecimalSeparator(true)
                                    .decimalSeparator(',')
                                    .build());

            assertThat(comma.write(price("12.34"))).isEqualTo("00012,34");
            assertThat(comma.write(price("0.05"))).isEqualTo("00000,05");
            assertThat(comma.read("00012,34", Price.class).value)
                    .isEqualTo(new BigDecimal("12.34"));
        }

        @Test
        void explicitSeparatorMustBeInPlace() {
            Flat2Java dot =
                    Flat2Java.create(Flat2JavaOptions.builder().useDecimalSeparator(true).build());

            assertThatThrownBy(() -> dot.read("0001234.", Price.class))
                    .isInstanceOf(DeserializationException.class)
                    .hasMessage(
                            "Price.value (columns 1-8): expected digits with '.' followed by 2"
                                    + " decimal places");
        }

        static class Narrow {
            @FlatField(length = 3, precision = 2)
            BigDecimal value;
        }

        @Test
        void explicitSeparatorNeedsRoomForOneIntegerDigit() {
            Flat2Java dot =
                    Flat2Java.create(Flat2JavaOptions.builder().useDecimalSeparator(true).build());

            assertThatThrownBy(() -> dot.recordLength(Narrow.class))
                    .isInstanceOf(MappingException.class)
                    .hasMessage(
                            "Narrow.value: length 3 cannot hold 2 decimal places plus a decimal"
                                    + " separator");
        }

        @Test
        void negativeValuesFail() {
            assertThatThrownBy(() -> flat.write(price("-1.00")))
                    .isInstanceOf(SerializationException.class)
                    .hasMessage(
                            "Price.value: negative values are not supported by the flat format");
        }

        @Test
        void nullFails() {
            assertThatThrownBy(() -> flat.write(new Price()))
                    .isInstanceOf(SerializationException.class)
                    .hasMessage("Price.value: null is not allowed on numeric fields");
        }
    }

    @Nested
    class Dates {

        static class Event {
            @FlatField(length = 10, dateTimeFormat = "dd/MM/yyyy")
            LocalDate day;
        }

        static class Stamp {
            @FlatField(length = 16, dateTimeFormat = "uuuu-MM-dd HH:mm")
            LocalDateTime at;
        }

        @Test
        void patternWithSeparators() {
            Event e = new Event();
            e.day = LocalDate.of(2024, 10, 5);

            assertThat(flat.write(e)).isEqualTo("05/10/2024");
            assertThat(flat.read("05/10/2024", Event.class).day).isEqualTo(e.day);
        }

        @Test
        void prolepticYearPattern() {
            Stamp s = new Stamp();
            s.at = LocalDateTime.of(2024, 1, 2, 3, 4);

            assertThat(flat.read(flat.write(s), Stamp.class).at).isEqualTo(s.at);
        }

        @Test
        void yearsThatDoNotFitTheColumnFail() {
            Event e = new Event();
            e.day = LocalDate.of(12024, 1, 1);

            assertThatThrownBy(() -> flat.write(e))
                    .isInstanceOf(SerializationException.class)
                    .hasMessage("Event.day: formatted value has 12 characters, but length is 10");
        }

        @Test
        void nullFails() {
            assertThatThrownBy(() -> flat.write(new Event()))
                    .isInstanceOf(SerializationException.class)
                    .hasMessage("Event.day: null is not allowed on date/time fields");
        }
    }

    @Nested
    class Ordering {

        static class Base {
            @FlatField(length = 2)
            String base;
        }

        static class Derived extends Base {
            @FlatField(length = 3)
            String derived;
        }

        static class Positioned {
            @FlatField(position = 2, length = 2)
            String second;

            @FlatField(position = 1, length = 3)
            String first;
        }

        @Test
        void superclassFieldsComeFirst() {
            Derived d = new Derived();
            d.base = "AB";
            d.derived = "CDE";

            assertThat(flat.write(d)).isEqualTo("ABCDE");
            Derived copy = flat.read("ABCDE", Derived.class);
            assertThat(copy.base).isEqualTo("AB");
            assertThat(copy.derived).isEqualTo("CDE");
        }

        @Test
        void positionOverridesDeclarationOrder() {
            Positioned p = new Positioned();
            p.first = "111";
            p.second = "22";

            assertThat(flat.write(p)).isEqualTo("11122");
            assertThat(flat.read("11122", Positioned.class).second).isEqualTo("22");
        }

        static class PositionedBase {
            @FlatField(position = 2, length = 2)
            String base;
        }

        static class PositionedDerived extends PositionedBase {
            @FlatField(position = 1, length = 3)
            String derived;
        }

        @Test
        void positionsOrderTheWholeHierarchy() {
            PositionedDerived d = new PositionedDerived();
            d.base = "AB";
            d.derived = "CDE";

            assertThat(flat.write(d)).isEqualTo("CDEAB");
        }
    }

    @Test
    void instancesAreIndependent() {
        Flat2Java comma =
                Flat2Java.create(
                        Flat2JavaOptions.builder()
                                .useDecimalSeparator(true)
                                .decimalSeparator(',')
                                .build());
        Decimals.Price p = new Decimals.Price();
        p.value = new BigDecimal("1.50");

        assertThat(comma.write(p)).isEqualTo("00001,50");
        assertThat(flat.write(p)).isEqualTo("00000150");
        assertThat(comma.options().decimalSeparator()).isEqualTo(',');
    }
}
