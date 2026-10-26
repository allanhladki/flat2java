package io.github.allanhladki.flat2java;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import java.math.RoundingMode;
import java.util.Map;

class Flat2JavaOptionsTest {

    @Test
    void defaults() {
        Flat2JavaOptions options = Flat2JavaOptions.defaults();

        assertThat(options.allowNullStrings()).isTrue();
        assertThat(options.nullValuePadding()).isEqualTo(' ');
        assertThat(options.failOnStringTruncation()).isFalse();
        assertThat(options.useDecimalSeparator()).isFalse();
        assertThat(options.decimalSeparator()).isEqualTo('.');
        assertThat(options.roundingMode()).isEqualTo(RoundingMode.UNNECESSARY);
    }

    @Test
    void builderAndToBuilderRoundTrip() {
        Flat2JavaOptions options =
                Flat2JavaOptions.builder()
                        .allowNullStrings(false)
                        .nullValuePadding('*')
                        .failOnStringTruncation(true)
                        .useDecimalSeparator(true)
                        .decimalSeparator(',')
                        .roundingMode(RoundingMode.HALF_EVEN)
                        .build();

        assertThat(options.toBuilder().build()).isEqualTo(options).hasSameHashCodeAs(options);
        assertThat(options).isNotEqualTo(Flat2JavaOptions.defaults());
        assertThat(options.toString()).contains("decimalSeparator=','");
    }

    @Test
    void digitsCannotBeDecimalSeparators() {
        assertThatThrownBy(() -> Flat2JavaOptions.builder().decimalSeparator('5'))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void emptyEnvironmentGivesDefaults() {
        assertThat(Flat2JavaOptions.fromEnvironment(Map.of()))
                .isEqualTo(Flat2JavaOptions.defaults());
    }

    @Test
    void readsEveryEnvironmentVariable() {
        Flat2JavaOptions options =
                Flat2JavaOptions.fromEnvironment(
                        Map.of(
                                "FLAT2JAVA_ALLOW_NULL_STRINGS", "false",
                                "FLAT2JAVA_NULL_VALUE_PADDING", "*",
                                "FLAT2JAVA_FAIL_ON_STRING_TRUNCATION", "TRUE",
                                "FLAT2JAVA_DECIMAL_USE_SEPARATOR", "true",
                                "FLAT2JAVA_DECIMAL_SEPARATOR", ",",
                                "FLAT2JAVA_DECIMAL_ROUNDING_MODE", "half_up"));

        assertThat(options)
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
    void invalidBooleanFails() {
        assertThatThrownBy(
                        () ->
                                Flat2JavaOptions.fromEnvironment(
                                        Map.of("FLAT2JAVA_ALLOW_NULL_STRINGS", "yes")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Invalid value 'yes' for environment variable"
                                + " FLAT2JAVA_ALLOW_NULL_STRINGS");
    }

    @Test
    void invalidCharacterFails() {
        assertThatThrownBy(
                        () ->
                                Flat2JavaOptions.fromEnvironment(
                                        Map.of("FLAT2JAVA_DECIMAL_SEPARATOR", ",,")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void invalidRoundingModeFails() {
        assertThatThrownBy(
                        () ->
                                Flat2JavaOptions.fromEnvironment(
                                        Map.of("FLAT2JAVA_DECIMAL_ROUNDING_MODE", "NEAREST")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
