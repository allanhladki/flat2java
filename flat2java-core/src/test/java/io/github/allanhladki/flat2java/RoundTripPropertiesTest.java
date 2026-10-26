package io.github.allanhladki.flat2java;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.allanhladki.flat2java.annotation.FlatField;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** For every value that fits its column, {@code read(write(r))} equals {@code r}. */
class RoundTripPropertiesTest {

    private static final Flat2Java FLAT = Flat2Java.create();
    private static final Flat2Java COMMA =
            Flat2Java.create(
                    Flat2JavaOptions.builder()
                            .useDecimalSeparator(true)
                            .decimalSeparator(',')
                            .build());

    static class Inner {
        @FlatField(length = 9)
        long code;

        @FlatField(length = 4)
        String tag;
    }

    static class Sample {
        @FlatField(length = 12)
        String text;

        @FlatField(length = 10)
        int number;

        @FlatField(length = 20)
        BigInteger big;

        @FlatField(length = 14, precision = 3)
        BigDecimal amount;

        @FlatField(length = 8, dateTimeFormat = "yyyyMMdd")
        LocalDate date;

        @FlatField(length = 17, dateTimeFormat = "yyyyMMddHHmmssSSS")
        LocalDateTime timestamp;

        @FlatField(length = 13)
        Inner inner;
    }

    @Property
    void roundTripWithImpliedDecimals(@ForAll("samples") Sample sample) {
        assertThat(FLAT.read(FLAT.write(sample), Sample.class))
                .usingRecursiveComparison()
                .isEqualTo(sample);
    }

    @Property
    void roundTripWithExplicitSeparator(@ForAll("samples") Sample sample) {
        assertThat(COMMA.read(COMMA.write(sample), Sample.class))
                .usingRecursiveComparison()
                .isEqualTo(sample);
    }

    @Property
    void writtenRecordsHaveTheRecordLength(@ForAll("samples") Sample sample) {
        assertThat(FLAT.write(sample)).hasSize(FLAT.recordLength(Sample.class));
    }

    @Provide
    Arbitrary<Sample> samples() {
        // Trailing spaces are padding, so generated text never ends with one.
        Arbitrary<String> text =
                Arbitraries.strings()
                        .withCharRange('!', '~')
                        .withChars(' ')
                        .ofMinLength(1)
                        .ofMaxLength(12)
                        .filter(s -> !s.endsWith(" "));
        Arbitrary<BigDecimal> amount =
                Arbitraries.longs()
                        .between(0, 9_999_999_999L)
                        .map(unscaled -> BigDecimal.valueOf(unscaled, 3));
        Arbitrary<LocalDate> date =
                Arbitraries.integers()
                        .between(0, 3_000_000)
                        .map(d -> LocalDate.of(1, 1, 1).plusDays(d));
        Arbitrary<LocalDateTime> timestamp =
                Arbitraries.longs()
                        .between(0, 250_000_000_000L)
                        .map(ms -> LocalDateTime.of(2000, 1, 1, 0, 0).plusNanos(ms * 1_000_000));
        Arbitrary<Inner> inner =
                Combinators.combine(
                                Arbitraries.longs().between(0, 999_999_999L),
                                Arbitraries.strings().alpha().ofLength(4))
                        .as(
                                (code, tag) -> {
                                    Inner i = new Inner();
                                    i.code = code;
                                    i.tag = tag;
                                    return i;
                                });
        return Combinators.combine(
                        text,
                        Arbitraries.integers().between(0, Integer.MAX_VALUE),
                        Arbitraries.bigIntegers()
                                .between(
                                        BigInteger.ZERO,
                                        BigInteger.TEN.pow(20).subtract(BigInteger.ONE)),
                        amount,
                        date,
                        timestamp,
                        inner)
                .as(
                        (t, n, b, a, d, ts, i) -> {
                            Sample s = new Sample();
                            s.text = t;
                            s.number = n;
                            s.big = b;
                            s.amount = a;
                            s.date = d;
                            s.timestamp = ts;
                            s.inner = i;
                            return s;
                        });
    }
}
