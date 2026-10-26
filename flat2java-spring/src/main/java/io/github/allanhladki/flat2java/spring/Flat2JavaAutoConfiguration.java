package io.github.allanhladki.flat2java.spring;

import io.github.allanhladki.flat2java.Flat2Java;
import io.github.allanhladki.flat2java.Flat2JavaOptions;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Registers a {@link Flat2Java} bean configured from the {@code flat2java.*} properties.
 *
 * <p>Define a {@link Flat2JavaOptions} bean to replace the options, or a {@link Flat2Java} bean to
 * replace the instance.
 */
@AutoConfiguration
@ConditionalOnClass(Flat2Java.class)
@EnableConfigurationProperties(Flat2JavaProperties.class)
public class Flat2JavaAutoConfiguration {

    /**
     * Options built from {@link Flat2JavaProperties}.
     *
     * @param properties the bound properties
     * @return the options
     */
    @Bean
    @ConditionalOnMissingBean
    public Flat2JavaOptions flat2JavaOptions(Flat2JavaProperties properties) {
        return properties.toOptions();
    }

    /**
     * The shared, thread-safe {@link Flat2Java} instance.
     *
     * @param options the options
     * @return the instance
     */
    @Bean
    @ConditionalOnMissingBean
    public Flat2Java flat2Java(Flat2JavaOptions options) {
        return Flat2Java.create(options);
    }
}
