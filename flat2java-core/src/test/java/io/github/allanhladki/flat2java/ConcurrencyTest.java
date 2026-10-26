package io.github.allanhladki.flat2java;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.allanhladki.flat2java.annotation.FlatField;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class ConcurrencyTest {

    static class Entry {
        @FlatField(length = 6)
        int id;

        @FlatField(length = 9, precision = 2)
        BigDecimal amount;

        @FlatField(length = 8, dateTimeFormat = "yyyyMMdd")
        LocalDate date;
    }

    @Test
    void oneInstanceIsSafeToShareBetweenThreads() throws Exception {
        Flat2Java flat = Flat2Java.create();
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Callable<Boolean>> tasks = new ArrayList<>();
            for (int t = 0; t < 8; t++) {
                int seed = t;
                tasks.add(
                        () -> {
                            for (int i = 0; i < 2_000; i++) {
                                Entry e = new Entry();
                                e.id = seed * 10_000 + i;
                                e.amount = BigDecimal.valueOf(i, 2);
                                e.date = LocalDate.of(2024, 1, 1).plusDays(i);
                                Entry copy = flat.read(flat.write(e), Entry.class);
                                if (copy.id != e.id
                                        || !copy.amount.equals(e.amount)
                                        || !copy.date.equals(e.date)) {
                                    return false;
                                }
                            }
                            return true;
                        });
            }
            for (Future<Boolean> result : pool.invokeAll(tasks)) {
                assertThat(result.get()).isTrue();
            }
        } finally {
            pool.shutdownNow();
        }
    }
}
