package io.github.allanhladki.flat2java.quarkus;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.allanhladki.flat2java.Flat2Java;
import io.github.allanhladki.flat2java.annotation.FlatField;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.Config;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

/** Injects the produced beans in a real CDI container (Weld). */
@EnableWeld
class Flat2JavaInjectionTest {

    @ApplicationScoped
    static class TestConfig {
        @Produces
        Config config() {
            return Flat2JavaProducersTest.config(
                    Map.of(
                            "flat2java.decimal.use-separator", "true",
                            "flat2java.decimal.separator", ","));
        }
    }

    static class Price {
        @FlatField(length = 7, precision = 2)
        BigDecimal value = new BigDecimal("3.50");
    }

    @WeldSetup
    WeldInitiator weld = WeldInitiator.from(Flat2JavaProducers.class, TestConfig.class).build();

    @Inject Flat2Java flat;

    @Test
    void injectsAConfiguredInstance() {
        String line = flat.write(new Price());

        assertThat(line).isEqualTo("0003,50");
        assertThat(flat.read(line, Price.class).value).isEqualTo(new BigDecimal("3.50"));
    }
}
