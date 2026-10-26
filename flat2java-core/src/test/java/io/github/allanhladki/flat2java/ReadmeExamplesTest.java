package io.github.allanhladki.flat2java;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.allanhladki.flat2java.annotation.FlatField;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** The code samples of README.md. Keep both in sync. */
class ReadmeExamplesTest {

    public static class Payment {
        @FlatField(length = 10)
        private String id;

        @FlatField(length = 8, dateTimeFormat = "yyyyMMdd")
        private LocalDate date;

        @FlatField(length = 9, precision = 2)
        private BigDecimal amount;

        public Payment() {}

        public Payment(String id, LocalDate date, BigDecimal amount) {
            this.id = id;
            this.date = date;
            this.amount = amount;
        }
    }

    @Test
    void quickStart() {
        Flat2Java flat = Flat2Java.create();

        Payment payment = new Payment("P-0001", LocalDate.of(2024, 3, 15), new BigDecimal("99.90"));
        String line = flat.write(payment); // "P-0001    20240315000009990"
        Payment copy = flat.read(line, Payment.class);

        assertThat(line).isEqualTo("P-0001    20240315000009990");
        assertThat(flat.recordLength(Payment.class)).isEqualTo(27);
        assertThat(copy).usingRecursiveComparison().isEqualTo(payment);
    }

    @Test
    void readingAFile(@TempDir Path dir) throws IOException {
        Flat2Java flat = Flat2Java.create();
        Path path = dir.resolve("payments.txt");
        Files.write(path, List.of("P-0001    20240315000009990", "P-0002    20240316000000150"));

        List<Payment> payments;
        try (Stream<String> lines = Files.lines(path)) {
            payments =
                    lines.map(line -> flat.read(line, Payment.class)).collect(Collectors.toList());
        }

        assertThat(payments).hasSize(2);
        assertThat(payments.get(1).amount).isEqualTo(new BigDecimal("1.50"));
    }

    public static class Address {
        @FlatField(length = 20)
        private String street;

        @FlatField(length = 2)
        private String state;
    }

    public static class Customer {
        @FlatField(length = 6)
        private int id;

        @FlatField(length = 22) // 20 + 2, the sum of the Address fields
        private Address address;
    }

    @Test
    void nestedRecords() {
        Customer customer = new Customer();
        customer.id = 42;
        customer.address = new Address();
        customer.address.street = "Rua das Flores 100";
        customer.address.state = "PR";

        assertThat(Flat2Java.create().write(customer)).isEqualTo("000042Rua das Flores 100  PR");
    }

    public static class Header {
        @FlatField(position = 1, length = 2)
        private String type;

        @FlatField(position = 3, length = 8, dateTimeFormat = "yyyyMMdd")
        private LocalDate created;

        @FlatField(position = 2, length = 5)
        private int sequence;
    }

    @Test
    void explicitPositions() {
        Header header = new Header();
        header.type = "HD";
        header.sequence = 1;
        header.created = LocalDate.of(2024, 3, 15);

        assertThat(Flat2Java.create().write(header)).isEqualTo("HD0000120240315");
    }

    @Test
    void options() {
        Flat2JavaOptions options =
                Flat2JavaOptions.builder()
                        .useDecimalSeparator(true)
                        .decimalSeparator(',')
                        .roundingMode(RoundingMode.HALF_UP)
                        .build();
        Flat2Java flat = Flat2Java.create(options);

        Payment payment =
                new Payment("P-0001", LocalDate.of(2024, 3, 15), new BigDecimal("99.899"));

        assertThat(flat.write(payment)).isEqualTo("P-0001    20240315000099,90");
    }

    public static class Item {
        @FlatField(length = 3)
        private String sku;

        @FlatField(length = 2)
        private int quantity;

        Item(String sku, int quantity) {
            this.sku = sku;
            this.quantity = quantity;
        }

        private Item() {}
    }

    public static class Invoice {
        @FlatField(length = 4)
        private int id;

        @FlatField(length = 3, occurs = 3)
        private int[] counts;

        @FlatField(length = 5, occurs = 2)
        private List<Item> items; // Item fields: 3 + 2
    }

    @Test
    void listsAndArrays() {
        Invoice invoice = new Invoice();
        invoice.id = 12;
        invoice.counts = new int[] {1, 20, 300};
        invoice.items = List.of(new Item("X1", 5), new Item("Y22", 10));

        assertThat(Flat2Java.create().write(invoice))
                .isEqualTo("0012" + "001020300" + "X1 05Y2210");
    }

    public record Point(@FlatField(length = 4) int x, @FlatField(length = 4) int y) {}

    @Test
    void javaRecords() {
        Flat2Java flat = Flat2Java.create();

        assertThat(flat.write(new Point(12, 7))).isEqualTo("00120007");
        assertThat(flat.read("00120007", Point.class)).isEqualTo(new Point(12, 7));
    }
}
