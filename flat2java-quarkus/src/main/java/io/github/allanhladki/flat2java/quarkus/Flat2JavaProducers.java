package io.github.allanhladki.flat2java.quarkus;

import io.github.allanhladki.flat2java.Flat2Java;
import io.github.allanhladki.flat2java.Flat2JavaOptions;
import io.quarkus.arc.DefaultBean;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import org.eclipse.microprofile.config.Config;

import java.math.RoundingMode;
import java.util.Locale;

/**
 * CDI producers of {@link Flat2Java} and {@link Flat2JavaOptions}, configured from the {@code
 * flat2java.*} MicroProfile Config properties (the same keys as the Spring module).
 *
 * <p>Both beans are {@link DefaultBean}s: declaring a producer of either type in the application
 * replaces them.
 */
@ApplicationScoped
public class Flat2JavaProducers {

    /** Created by the CDI container. */
    public Flat2JavaProducers() {}

    static final String PREFIX = "flat2java.";

    /**
     * Options read from the application configuration.
     *
     * @param config the application configuration
     * @return the options
     */
    @Produces
    @Singleton
    @DefaultBean
    public Flat2JavaOptions flat2JavaOptions(Config config) {
        return toOptions(config);
    }

    /**
     * The shared, thread-safe {@link Flat2Java} instance.
     *
     * @param options the options
     * @return the instance
     */
    @Produces
    @Singleton
    @DefaultBean
    public Flat2Java flat2Java(Flat2JavaOptions options) {
        return Flat2Java.create(options);
    }

    static Flat2JavaOptions toOptions(Config config) {
        Flat2JavaOptions.Builder builder = Flat2JavaOptions.builder();
        config.getOptionalValue(PREFIX + "allow-null-strings", Boolean.class)
                .ifPresent(builder::allowNullStrings);
        config.getOptionalValue(PREFIX + "null-value-padding", String.class)
                .map(value -> character("null-value-padding", value))
                .ifPresent(builder::nullValuePadding);
        config.getOptionalValue(PREFIX + "fail-on-string-truncation", Boolean.class)
                .ifPresent(builder::failOnStringTruncation);
        config.getOptionalValue(PREFIX + "decimal.use-separator", Boolean.class)
                .ifPresent(builder::useDecimalSeparator);
        config.getOptionalValue(PREFIX + "decimal.separator", String.class)
                .map(value -> character("decimal.separator", value))
                .ifPresent(builder::decimalSeparator);
        config.getOptionalValue(PREFIX + "decimal.rounding-mode", String.class)
                .map(
                        value ->
                                RoundingMode.valueOf(
                                        value.trim().replace('-', '_').toUpperCase(Locale.ROOT)))
                .ifPresent(builder::roundingMode);
        return builder.build();
    }

    private static char character(String key, String value) {
        if (value.length() != 1) {
            throw new IllegalArgumentException(
                    PREFIX + key + " must be a single character, got '" + value + "'");
        }
        return value.charAt(0);
    }
}
