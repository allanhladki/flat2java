package com.github.allanhladki.flat2java.spring;

import com.github.allanhladki.flat2java.factory.Flat2JavaFactory;
import com.github.allanhladki.flat2java.interfaces.*;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot auto-configuration for Flat2Java services.
 *
 * <p>This configuration automatically creates and configures Flat2Java services when Spring Boot is
 * detected on the classpath. Users can override any bean by defining their own beans with the same
 * names.
 *
 * <p>The following beans are automatically configured:
 *
 * <ul>
 *   <li>{@link FieldMetadataInterface} - Field metadata extraction service
 *   <li>{@link FieldSerializationInterface} - Field serialization service
 *   <li>{@link ObjectSerializationInterface} - Object serialization service
 *   <li>{@link FieldDeserializationInterface} - Field deserialization service
 *   <li>{@link ObjectDeserializationInterface} - Object deserialization service
 * </ul>
 *
 * @since 0.0.1-rc2
 */
@Configuration
@EnableConfigurationProperties(Flat2JavaProperties.class)
public class Flat2JavaAutoConfiguration {

    /**
     * Creates the Flat2Java factory bean.
     *
     * @return configured Flat2Java factory
     */
    @Bean
    @ConditionalOnMissingBean
    public Flat2JavaFactory flat2JavaFactory() {
        return new Flat2JavaFactory();
    }

    /**
     * Creates the field metadata interface bean.
     *
     * @param factory the Flat2Java factory
     * @return configured field metadata interface
     */
    @Bean
    @ConditionalOnMissingBean
    public FieldMetadataInterface fieldMetadataInterface(Flat2JavaFactory factory) {
        return factory.getFieldMetadataInterface();
    }

    /**
     * Creates the field serialization interface bean.
     *
     * @param factory the Flat2Java factory
     * @return configured field serialization interface
     */
    @Bean
    @ConditionalOnMissingBean
    public FieldSerializationInterface fieldSerializationInterface(Flat2JavaFactory factory) {
        return factory.getFieldSerializationInterface();
    }

    /**
     * Creates the object serialization interface bean.
     *
     * @param factory the Flat2Java factory
     * @return configured object serialization interface
     */
    @Bean
    @ConditionalOnMissingBean
    public ObjectSerializationInterface objectSerializationInterface(Flat2JavaFactory factory) {
        return factory.getObjectSerializationInterface();
    }

    /**
     * Creates the field deserialization interface bean.
     *
     * @param factory the Flat2Java factory
     * @return configured field deserialization interface
     */
    @Bean
    @ConditionalOnMissingBean
    public FieldDeserializationInterface fieldDeserializationInterface(Flat2JavaFactory factory) {
        return factory.getFieldDeserializationInterface();
    }

    /**
     * Creates the object deserialization interface bean.
     *
     * @param factory the Flat2Java factory
     * @return configured object deserialization interface
     */
    @Bean
    @ConditionalOnMissingBean
    public ObjectDeserializationInterface objectDeserializationInterface(Flat2JavaFactory factory) {
        return factory.getObjectDeserializationInterface();
    }
}
