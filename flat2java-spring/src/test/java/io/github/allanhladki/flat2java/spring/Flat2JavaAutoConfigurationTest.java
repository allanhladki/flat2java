package io.github.allanhladki.flat2java.spring;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.allanhladki.flat2java.Flat2Java;
import io.github.allanhladki.flat2java.Flat2JavaOptions;
import io.github.allanhladki.flat2java.annotation.FlatField;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.math.RoundingMode;

class Flat2JavaAutoConfigurationTest {

    private final ApplicationContextRunner runner =
            new ApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(Flat2JavaAutoConfiguration.class));

    static class Price {
        @FlatField(length = 8, precision = 2)
        BigDecimal value = new BigDecimal("12.345");
    }

    @Test
    void registersFlat2JavaWithDefaultOptions() {
        runner.run(
                context -> {
                    assertThat(context).hasSingleBean(Flat2Java.class);
                    assertThat(context.getBean(Flat2Java.class).options())
                            .isEqualTo(Flat2JavaOptions.defaults());
                });
    }

    @Test
    void propertyDefaultsMatchTheCoreDefaults() {
        assertThat(new Flat2JavaProperties().toOptions()).isEqualTo(Flat2JavaOptions.defaults());
    }

    @Test
    void appliesEveryProperty() {
        runner.withPropertyValues(
                        "flat2java.allow-null-strings=false",
                        "flat2java.null-value-padding=*",
                        "flat2java.fail-on-string-truncation=true",
                        "flat2java.decimal.use-separator=true",
                        "flat2java.decimal.separator=,",
                        "flat2java.decimal.rounding-mode=half-up")
                .run(
                        context ->
                                assertThat(context.getBean(Flat2JavaOptions.class))
                                        .isEqualTo(
                                                Flat2JavaOptions.builder()
                                                        .allowNullStrings(false)
                                                        .nullValuePadding('*')
                                                        .failOnStringTruncation(true)
                                                        .useDecimalSeparator(true)
                                                        .decimalSeparator(',')
                                                        .roundingMode(RoundingMode.HALF_UP)
                                                        .build()));
    }

    @Test
    void propertiesChangeTheOutput() {
        runner.withPropertyValues(
                        "flat2java.decimal.use-separator=true",
                        "flat2java.decimal.separator=,",
                        "flat2java.decimal.rounding-mode=HALF_UP")
                .run(
                        context ->
                                assertThat(context.getBean(Flat2Java.class).write(new Price()))
                                        .isEqualTo("00012,35"));
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomOptions {
        @Bean
        Flat2JavaOptions customOptions() {
            return Flat2JavaOptions.builder().nullValuePadding('#').build();
        }
    }

    @Test
    void userOptionsReplaceTheProperties() {
        runner.withUserConfiguration(CustomOptions.class)
                .withPropertyValues("flat2java.null-value-padding=*")
                .run(
                        context ->
                                assertThat(
                                                context.getBean(Flat2Java.class)
                                                        .options()
                                                        .nullValuePadding())
                                        .isEqualTo('#'));
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomInstance {
        @Bean
        Flat2Java customFlat2Java() {
            return Flat2Java.create(Flat2JavaOptions.builder().allowNullStrings(false).build());
        }
    }

    @Test
    void userInstanceReplacesTheDefault() {
        runner.withUserConfiguration(CustomInstance.class)
                .run(
                        context -> {
                            assertThat(context).hasSingleBean(Flat2Java.class);
                            assertThat(
                                            context.getBean(Flat2Java.class)
                                                    .options()
                                                    .allowNullStrings())
                                    .isFalse();
                        });
    }
}
