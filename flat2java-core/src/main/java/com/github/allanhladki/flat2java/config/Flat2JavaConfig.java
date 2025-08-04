package com.github.allanhladki.flat2java.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.RoundingMode;

/**
 * Configuration class for flat2java library settings. This class manages all environment variables
 * and configuration options.
 */
public class Flat2JavaConfig {
    private static final Logger logger = LoggerFactory.getLogger(Flat2JavaConfig.class);

    /** Environment variable names */
    private static final String NULL_HANDLING_ENV_VAR = "FLAT2JAVA_ALLOW_NULL_VALUES";
    private static final String DEFAULT_NUMERIC_PADDING_ENV_VAR = "FLAT2JAVA_DEFAULT_NUMERIC_PADDING";
    private static final String BIGDECIMAL_USE_DECIMAL_SEPARATOR_ENV_VAR = "FLAT2JAVA_BIGDECIMAL_USE_DECIMAL_SEPARATOR";
    private static final String BIGDECIMAL_DECIMAL_SEPARATOR_ENV_VAR = "FLAT2JAVA_BIGDECIMAL_DECIMAL_SEPARATOR";
    private static final String BIGDECIMAL_ROUNDING_MODE_ENV_VAR = "FLAT2JAVA_BIGDECIMAL_ROUNDING_MODE";

    /** Default configuration values */
    private static final boolean DEFAULT_NULL_HANDLING = true;
    private static final char DEFAULT_NUMERIC_PADDING = '0';
    private static final boolean DEFAULT_BIGDECIMAL_USE_DECIMAL_SEPARATOR = false;
    private static final char DEFAULT_BIGDECIMAL_DECIMAL_SEPARATOR = '.';
    private static final RoundingMode DEFAULT_BIGDECIMAL_ROUNDING_MODE = RoundingMode.UNNECESSARY;

    /** Cached configuration values */
    private static final boolean ALLOW_NULL_VALUES = getNullHandlingConfig();
    private static final char NUMERIC_PADDING = getNumericPaddingConfig();
    private static final boolean BIGDECIMAL_USE_DECIMAL_SEPARATOR = getBigDecimalUseDecimalSeparatorConfig();
    private static final char BIGDECIMAL_DECIMAL_SEPARATOR = getBigDecimalDecimalSeparatorConfig();
    private static final RoundingMode BIGDECIMAL_ROUNDING_MODE = getBigDecimalRoundingModeConfig();

    /** Private constructor to prevent instantiation */
    private Flat2JavaConfig() {
        // Utility class
    }

    /**
     * Gets the null handling configuration from environment variable.
     *
     * @return true if null values should be allowed, false if exceptions should be thrown
     */
    private static boolean getNullHandlingConfig() {
        String envValue = System.getenv(NULL_HANDLING_ENV_VAR);
        if (envValue == null) {
            logger.trace(
                    "Environment variable {} not set, using default: {}",
                    NULL_HANDLING_ENV_VAR,
                    DEFAULT_NULL_HANDLING);
            return DEFAULT_NULL_HANDLING;
        }

        boolean config = Boolean.parseBoolean(envValue);
        logger.trace("Environment variable {} set to: {}", NULL_HANDLING_ENV_VAR, config);
        return config;
    }

    /**
     * Gets the default numeric padding configuration from environment variable.
     *
     * @return the character to use for padding numeric fields
     */
    private static char getNumericPaddingConfig() {
        String envValue = System.getenv(DEFAULT_NUMERIC_PADDING_ENV_VAR);
        if (envValue == null) {
            logger.trace(
                    "Environment variable {} not set, using default: {}",
                    DEFAULT_NUMERIC_PADDING_ENV_VAR,
                    DEFAULT_NUMERIC_PADDING);
            return DEFAULT_NUMERIC_PADDING;
        }

        if (envValue.length() != 1) {
            logger.warn(
                    "Environment variable {} must be a single character, using default: {}",
                    DEFAULT_NUMERIC_PADDING_ENV_VAR,
                    DEFAULT_NUMERIC_PADDING);
            return DEFAULT_NUMERIC_PADDING;
        }

        char config = envValue.charAt(0);
        logger.trace("Environment variable {} set to: {}", DEFAULT_NUMERIC_PADDING_ENV_VAR, config);
        return config;
    }

    /**
     * Gets the BigDecimal decimal separator usage configuration from environment variable.
     *
     * @return true if decimal separator should be used, false otherwise
     */
    private static boolean getBigDecimalUseDecimalSeparatorConfig() {
        String envValue = System.getenv(BIGDECIMAL_USE_DECIMAL_SEPARATOR_ENV_VAR);
        if (envValue == null) {
            logger.trace(
                    "Environment variable {} not set, using default: {}",
                    BIGDECIMAL_USE_DECIMAL_SEPARATOR_ENV_VAR,
                    DEFAULT_BIGDECIMAL_USE_DECIMAL_SEPARATOR);
            return DEFAULT_BIGDECIMAL_USE_DECIMAL_SEPARATOR;
        }

        boolean config = Boolean.parseBoolean(envValue);
        logger.trace("Environment variable {} set to: {}", BIGDECIMAL_USE_DECIMAL_SEPARATOR_ENV_VAR, config);
        return config;
    }

    /**
     * Gets the BigDecimal decimal separator character configuration from environment variable.
     *
     * @return the character to use as decimal separator
     */
    private static char getBigDecimalDecimalSeparatorConfig() {
        String envValue = System.getenv(BIGDECIMAL_DECIMAL_SEPARATOR_ENV_VAR);
        if (envValue == null) {
            logger.trace(
                    "Environment variable {} not set, using default: {}",
                    BIGDECIMAL_DECIMAL_SEPARATOR_ENV_VAR,
                    DEFAULT_BIGDECIMAL_DECIMAL_SEPARATOR);
            return DEFAULT_BIGDECIMAL_DECIMAL_SEPARATOR;
        }

        if (envValue.length() != 1) {
            logger.warn(
                    "Environment variable {} must be a single character, using default: {}",
                    BIGDECIMAL_DECIMAL_SEPARATOR_ENV_VAR,
                    DEFAULT_BIGDECIMAL_DECIMAL_SEPARATOR);
            return DEFAULT_BIGDECIMAL_DECIMAL_SEPARATOR;
        }

        char config = envValue.charAt(0);
        logger.trace("Environment variable {} set to: {}", BIGDECIMAL_DECIMAL_SEPARATOR_ENV_VAR, config);
        return config;
    }

    /**
     * Gets the BigDecimal rounding mode configuration from environment variable.
     *
     * @return the rounding mode to use for BigDecimal operations
     */
    private static RoundingMode getBigDecimalRoundingModeConfig() {
        String envValue = System.getenv(BIGDECIMAL_ROUNDING_MODE_ENV_VAR);
        if (envValue == null) {
            logger.trace(
                    "Environment variable {} not set, using default: {}",
                    BIGDECIMAL_ROUNDING_MODE_ENV_VAR,
                    DEFAULT_BIGDECIMAL_ROUNDING_MODE);
            return DEFAULT_BIGDECIMAL_ROUNDING_MODE;
        }

        try {
            RoundingMode config = RoundingMode.valueOf(envValue.toUpperCase());
            logger.trace("Environment variable {} set to: {}", BIGDECIMAL_ROUNDING_MODE_ENV_VAR, config);
            return config;
        } catch (IllegalArgumentException e) {
            logger.warn(
                    "Environment variable {} has an invalid value '{}', using default: {}",
                    BIGDECIMAL_ROUNDING_MODE_ENV_VAR,
                    envValue,
                    DEFAULT_BIGDECIMAL_ROUNDING_MODE);
            return DEFAULT_BIGDECIMAL_ROUNDING_MODE;
        }
    }

    /**
     * Checks if null values are allowed in serialization.
     *
     * @return true if null values should be serialized to empty strings, false if exceptions should
     *     be thrown
     */
    public static boolean isNullHandlingEnabled() {
        return ALLOW_NULL_VALUES;
    }

    /**
     * Gets the environment variable name for null handling configuration.
     *
     * @return the environment variable name
     */
    public static String getNullHandlingEnvVarName() {
        return NULL_HANDLING_ENV_VAR;
    }

    /**
     * Gets the default numeric padding character for numeric fields.
     *
     * @return the character to use for padding numeric fields
     */
    public static char getDefaultNumericPadding() {
        return NUMERIC_PADDING;
    }

    /**
     * Gets the environment variable name for default numeric padding configuration.
     *
     * @return the environment variable name
     */
    public static String getDefaultNumericPaddingEnvVarName() {
        return DEFAULT_NUMERIC_PADDING_ENV_VAR;
    }

    /**
     * Checks if BigDecimal fields should use decimal separators.
     *
     * @return true if decimal separators should be used, false otherwise
     */
    public static boolean isBigDecimalUseDecimalSeparator() {
        return BIGDECIMAL_USE_DECIMAL_SEPARATOR;
    }

    /**
     * Gets the decimal separator character for BigDecimal fields.
     *
     * @return the character to use as decimal separator
     */
    public static char getBigDecimalDecimalSeparator() {
        return BIGDECIMAL_DECIMAL_SEPARATOR;
    }

    /**
     * Gets the environment variable name for BigDecimal decimal separator usage configuration.
     *
     * @return the environment variable name
     */
    public static String getBigDecimalUseDecimalSeparatorEnvVarName() {
        return BIGDECIMAL_USE_DECIMAL_SEPARATOR_ENV_VAR;
    }

    /**
     * Gets the environment variable name for BigDecimal decimal separator character configuration.
     *
     * @return the environment variable name
     */
    public static String getBigDecimalDecimalSeparatorEnvVarName() {
        return BIGDECIMAL_DECIMAL_SEPARATOR_ENV_VAR;
    }

    /**
     * Gets the environment variable name for BigDecimal rounding mode configuration.
     *
     * @return the environment variable name
     */
    public static String getBigDecimalRoundingModeEnvVarName() {
        return BIGDECIMAL_ROUNDING_MODE_ENV_VAR;
    }

    /**
     * Gets the rounding mode for BigDecimal operations.
     *
     * @return the rounding mode to use for BigDecimal operations
     */
    public static RoundingMode getBigDecimalRoundingMode() {
        return BIGDECIMAL_ROUNDING_MODE;
    }
}
