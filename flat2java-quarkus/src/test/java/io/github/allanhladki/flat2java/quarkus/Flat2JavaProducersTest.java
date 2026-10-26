package io.github.allanhladki.flat2java.quarkus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.allanhladki.flat2java.Flat2JavaOptions;
import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfigBuilder;

import org.eclipse.microprofile.config.Config;
import org.junit.jupiter.api.Test;

import java.math.RoundingMode;
import java.util.Map;

class Flat2JavaProducersTest {

    static Config config(Map<String, String> properties) {
        return new SmallRyeConfigBuilder()
                .withSources(new PropertiesConfigSource(properties, "test", 100))
                .build();
    }

    @Test
    void emptyConfigurationGivesDefaults() {
        assertThat(Flat2JavaProducers.toOptions(config(Map.of())))
                .isEqualTo(Flat2JavaOptions.defaults());
    }

    @Test
    void readsEveryProperty() {
        Config config =
                config(
                        Map.of(
                                "flat2java.allow-null-strings", "false",
                                "flat2java.null-value-padding", "*",
                                "flat2java.fail-on-string-truncation", "true",
                                "flat2java.decimal.use-separator", "true",
                                "flat2java.decimal.separator", ",",
                                "flat2java.decimal.rounding-mode", "half-up"));

        assertThat(Flat2JavaProducers.toOptions(config))
                .isEqualTo(
                        Flat2JavaOptions.builder()
                                .allowNullStrings(false)
                                .nullValuePadding('*')
                                .failOnStringTruncation(true)
                                .useDecimalSeparator(true)
                                .decimalSeparator(',')
                                .roundingMode(RoundingMode.HALF_UP)
                                .build());
    }

    @Test
    void multiCharacterValuesAreRejected() {
        Config config = config(Map.of("flat2java.decimal.separator", ",,"));

        assertThatThrownBy(() -> Flat2JavaProducers.toOptions(config))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Invalid value ',,' for flat2java.decimal.separator: expected a single"
                                + " character");
    }
}
