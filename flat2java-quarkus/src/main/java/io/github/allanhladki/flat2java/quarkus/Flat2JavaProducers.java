package io.github.allanhladki.flat2java.quarkus;

import io.github.allanhladki.flat2java.Flat2Java;
import io.github.allanhladki.flat2java.Flat2JavaOptions;
import io.github.allanhladki.flat2java.internal.OptionValues;
import io.quarkus.arc.DefaultBean;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import org.eclipse.microprofile.config.Config;

import java.util.Optional;
import java.util.function.Function;

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
        read(config, "allow-null-strings", OptionValues::parseBoolean)
                .ifPresent(builder::allowNullStrings);
        read(config, "null-value-padding", OptionValues::parseChar)
                .ifPresent(builder::nullValuePadding);
        read(config, "fail-on-string-truncation", OptionValues::parseBoolean)
                .ifPresent(builder::failOnStringTruncation);
        read(config, "decimal.use-separator", OptionValues::parseBoolean)
                .ifPresent(builder::useDecimalSeparator);
        read(config, "decimal.separator", OptionValues::parseChar)
                .ifPresent(builder::decimalSeparator);
        read(config, "decimal.rounding-mode", OptionValues::parseRoundingMode)
                .ifPresent(builder::roundingMode);
        return builder.build();
    }

    private static <T> Optional<T> read(Config config, String key, Function<String, T> parser) {
        return config.getOptionalValue(PREFIX + key, String.class)
                .map(value -> OptionValues.parse(PREFIX + key, value, parser));
    }
}
