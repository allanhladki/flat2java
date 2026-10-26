package io.github.allanhladki.flat2java;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.allanhladki.flat2java.annotation.FlatField;
import io.github.allanhladki.flat2java.exception.DeserializationException;
import io.github.allanhladki.flat2java.exception.MappingException;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

class JavaRecordTest {

    private final Flat2Java flat = Flat2Java.create();

    record Address(@FlatField(length = 10) String city, @FlatField(length = 2) String state) {}

    record Payment(
            @FlatField(length = 4) int id,
            @FlatField(length = 8, dateTimeFormat = "yyyyMMdd") LocalDate date,
            @FlatField(length = 7, precision = 2) BigDecimal amount,
            @FlatField(length = 12) Address address,
            @FlatField(length = 2, occurs = 2) List<String> tags) {}

    private static final Payment PAYMENT =
            new Payment(
                    7,
                    LocalDate.of(2024, 3, 15),
                    new BigDecimal("12.34"),
                    new Address("Curitiba", "PR"),
                    List.of("A", "BC"));

    private static final String LINE = "0007" + "20240315" + "0001234" + "Curitiba  PR" + "A BC";

    @Test
    void writesComponentsInDeclarationOrder() {
        assertThat(flat.write(PAYMENT)).isEqualTo(LINE);
    }

    @Test
    void readsThroughTheCanonicalConstructor() {
        assertThat(flat.read(LINE, Payment.class)).isEqualTo(PAYMENT);
    }

    record Positioned(
            @FlatField(position = 2, length = 3) String b,
            @FlatField(position = 1, length = 3) String a) {}

    @Test
    void positionsReorderComponents() {
        assertThat(flat.write(new Positioned("bbb", "aaa"))).isEqualTo("aaabbb");
        assertThat(flat.read("aaabbb", Positioned.class)).isEqualTo(new Positioned("bbb", "aaa"));
    }

    record PartlyMapped(@FlatField(length = 3) String code, int count, String note, boolean flag) {}

    @Test
    void componentsWithoutTheAnnotationReceiveDefaultValues() {
        assertThat(flat.write(new PartlyMapped("abc", 5, "ignored", true))).isEqualTo("abc");
        assertThat(flat.read("abc", PartlyMapped.class))
                .isEqualTo(new PartlyMapped("abc", 0, null, false));
    }

    record Validated(@FlatField(length = 3) int value) {
        Validated {
            if (value > 100) {
                throw new IllegalArgumentException("too large");
            }
        }
    }

    @Test
    void exceptionsOfTheConstructorAreReported() {
        assertThatThrownBy(() -> flat.read("500", Validated.class))
                .isInstanceOf(DeserializationException.class)
                .hasMessage("Validated: canonical constructor threw an exception")
                .hasRootCauseMessage("too large");
    }

    static class Holder {
        @FlatField(length = 12)
        Address address;
    }

    @Test
    void recordsCanBeNestedInClasses() {
        Holder holder = new Holder();
        holder.address = new Address("Rio", "RJ");

        assertThat(flat.write(holder)).isEqualTo("Rio       RJ");
        assertThat(flat.read("Rio       RJ", Holder.class).address)
                .isEqualTo(new Address("Rio", "RJ"));
    }

    record Unmapped(String code) {}

    @Test
    void recordWithoutAnnotatedComponentsIsRejected() {
        assertThatThrownBy(() -> flat.recordLength(Unmapped.class))
                .isInstanceOf(MappingException.class)
                .hasMessage("Unmapped: no fields annotated with @FlatField");
    }
}
