package com.github.allanhladki.flat2java.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for Flat2Java Spring Boot integration.
 *
 * <p>These properties allow users to configure Flat2Java behavior through
 * application.properties/application.yml files or environment variables.
 *
 * <p>Example configuration:
 *
 * <pre>
 * flat2java:
 *   allow-null-strings: true
 *   null-value-padding: "*"
 *   bigdecimal:
 *     use-decimal-separator: true
 *     decimal-separator: ","
 *     rounding-mode: HALF_UP
 *   validation:
 *     throw-on-string-truncation: true
 * </pre>
 *
 * @since 0.0.1-rc2
 */
@ConfigurationProperties(prefix = "flat2java")
@EnableConfigurationProperties
@Validated
public class Flat2JavaProperties {

    /** Whether to allow null string field values. */
    private boolean allowNullStrings = false;

    /** Padding character for null string values. */
    private char nullValuePadding = ' ';

    /** BigDecimal-specific configuration. */
    private BigDecimalConfig bigdecimal = new BigDecimalConfig();

    /** Validation-specific configuration. */
    private ValidationConfig validation = new ValidationConfig();

    // Getters and setters
    public boolean isAllowNullStrings() {
        return allowNullStrings;
    }

    public void setAllowNullStrings(boolean allowNullStrings) {
        this.allowNullStrings = allowNullStrings;
    }

    public char getNullValuePadding() {
        return nullValuePadding;
    }

    public void setNullValuePadding(char nullValuePadding) {
        this.nullValuePadding = nullValuePadding;
    }

    public BigDecimalConfig getBigdecimal() {
        return bigdecimal;
    }

    public void setBigdecimal(BigDecimalConfig bigdecimal) {
        this.bigdecimal = bigdecimal;
    }

    public ValidationConfig getValidation() {
        return validation;
    }

    public void setValidation(ValidationConfig validation) {
        this.validation = validation;
    }

    /** BigDecimal-specific configuration properties. */
    public static class BigDecimalConfig {
        /** Whether to use decimal separator in BigDecimal serialization. */
        private boolean useDecimalSeparator = false;

        /** Decimal separator character for BigDecimal. */
        private char decimalSeparator = '.';

        /** Rounding mode for BigDecimal precision. */
        private String roundingMode = "UNNECESSARY";

        // Getters and setters
        public boolean isUseDecimalSeparator() {
            return useDecimalSeparator;
        }

        public void setUseDecimalSeparator(boolean useDecimalSeparator) {
            this.useDecimalSeparator = useDecimalSeparator;
        }

        public char getDecimalSeparator() {
            return decimalSeparator;
        }

        public void setDecimalSeparator(char decimalSeparator) {
            this.decimalSeparator = decimalSeparator;
        }

        public String getRoundingMode() {
            return roundingMode;
        }

        public void setRoundingMode(String roundingMode) {
            this.roundingMode = roundingMode;
        }
    }

    /** Validation-specific configuration properties. */
    public static class ValidationConfig {
        /** Whether to throw exception when string values exceed field length. */
        private boolean throwOnStringTruncation = false;

        // Getters and setters
        public boolean isThrowOnStringTruncation() {
            return throwOnStringTruncation;
        }

        public void setThrowOnStringTruncation(boolean throwOnStringTruncation) {
            this.throwOnStringTruncation = throwOnStringTruncation;
        }
    }
}
