package io.github.allanhladki.flat2java.config;

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
    private static final String NULL_HANDLING_ENV_VAR = "FLAT2JAVA_ALLOW_NULL_STRINGS";

    private static final String NULL_VALUE_PADDING_ENV_VAR = "FLAT2JAVA_NULL_VALUE_PADDING";
    private static final String BIGDECIMAL_USE_DECIMAL_SEPARATOR_ENV_VAR =
            "FLAT2JAVA_BIGDECIMAL_USE_DECIMAL_SEPARATOR";
    private static final String BIGDECIMAL_DECIMAL_SEPARATOR_ENV_VAR =
            "FLAT2JAVA_BIGDECIMAL_DECIMAL_SEPARATOR";
    private static final String BIGDECIMAL_ROUNDING_MODE_ENV_VAR =
            "FLAT2JAVA_BIGDECIMAL_ROUNDING_MODE";
    private static final String THROW_ON_STRING_TRUNCATION_ENV_VAR =
            "FLAT2JAVA_THROW_ON_STRING_TRUNCATION";

    /** Default configuration values */
    private static final boolean DEFAULT_NULL_HANDLING = true;

    private static final char DEFAULT_NULL_VALUE_PADDING = ' ';
    private static final boolean DEFAULT_BIGDECIMAL_USE_DECIMAL_SEPARATOR = false;
    private static final char DEFAULT_BIGDECIMAL_DECIMAL_SEPARATOR = '.';
    private static final RoundingMode DEFAULT_BIGDECIMAL_ROUNDING_MODE = RoundingMode.UNNECESSARY;
    private static final boolean DEFAULT_THROW_ON_STRING_TRUNCATION = false;

    /** Cached configuration values */
    private static final boolean ALLOW_NULL_VALUES = getNullHandlingConfig();

    private static final char NULL_VALUE_PADDING = getNullValuePaddingConfig();
    private static final boolean BIGDECIMAL_USE_DECIMAL_SEPARATOR =
            getBigDecimalUseDecimalSeparatorConfig();
    private static final char BIGDECIMAL_DECIMAL_SEPARATOR = getBigDecimalDecimalSeparatorConfig();
    private static final RoundingMode BIGDECIMAL_ROUNDING_MODE = getBigDecimalRoundingModeConfig();
    private static final boolean THROW_ON_STRING_TRUNCATION = getThrowOnStringTruncationConfig();

    /** Private constructor to prevent instantiation */
    private Flat2JavaConfig() {
        // Utility class
    }

    /**
     * Gets the null string handling configuration from environment variable.
     *
     * @return true if null string values should be allowed, false if exceptions should be thrown
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

        try {
            boolean config = Boolean.parseBoolean(envValue);
            logger.trace("Environment variable {} set to: {}", NULL_HANDLING_ENV_VAR, config);
            return config;
        } catch (Exception e) {
            logger.warn(
                    "Environment variable {} has invalid value '{}', using default: {}",
                    NULL_HANDLING_ENV_VAR,
                    envValue,
                    DEFAULT_NULL_HANDLING);
            return DEFAULT_NULL_HANDLING;
        }
    }

    /**
     * Gets the null value padding configuration from environment variable.
     *
     * @return the character to use for padding null values
     */
    private static char getNullValuePaddingConfig() {
        String envValue = System.getenv(NULL_VALUE_PADDING_ENV_VAR);
        if (envValue == null) {
            logger.trace(
                    "Environment variable {} not set, using default: {}",
                    NULL_VALUE_PADDING_ENV_VAR,
                    DEFAULT_NULL_VALUE_PADDING);
            return DEFAULT_NULL_VALUE_PADDING;
        }

        if (envValue.length() != 1) {
            logger.warn(
                    "Environment variable {} must be a single character, using default: {}",
                    NULL_VALUE_PADDING_ENV_VAR,
                    DEFAULT_NULL_VALUE_PADDING);
            return DEFAULT_NULL_VALUE_PADDING;
        }

        char config = envValue.charAt(0);
        logger.trace("Environment variable {} set to: {}", NULL_VALUE_PADDING_ENV_VAR, config);
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

        try {
            boolean config = Boolean.parseBoolean(envValue);
            logger.trace(
                    "Environment variable {} set to: {}",
                    BIGDECIMAL_USE_DECIMAL_SEPARATOR_ENV_VAR,
                    config);
            return config;
        } catch (Exception e) {
            logger.warn(
                    "Environment variable {} has invalid value '{}', using default: {}",
                    BIGDECIMAL_USE_DECIMAL_SEPARATOR_ENV_VAR,
                    envValue,
                    DEFAULT_BIGDECIMAL_USE_DECIMAL_SEPARATOR);
            return DEFAULT_BIGDECIMAL_USE_DECIMAL_SEPARATOR;
        }
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
        logger.trace(
                "Environment variable {} set to: {}", BIGDECIMAL_DECIMAL_SEPARATOR_ENV_VAR, config);
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
            logger.trace(
                    "Environment variable {} set to: {}", BIGDECIMAL_ROUNDING_MODE_ENV_VAR, config);
            return config;
        } catch (IllegalArgumentException e) {
            logger.warn(
                    "Environment variable {} has an invalid value '{}', using default: {}. Valid"
                            + " values: {}",
                    BIGDECIMAL_ROUNDING_MODE_ENV_VAR,
                    envValue,
                    DEFAULT_BIGDECIMAL_ROUNDING_MODE,
                    getValidRoundingModes());
            return DEFAULT_BIGDECIMAL_ROUNDING_MODE;
        }
    }

    /**
     * Gets a comma-separated list of valid rounding mode values for error messages.
     *
     * @return string containing valid rounding mode values
     */
    private static String getValidRoundingModes() {
        RoundingMode[] modes = RoundingMode.values();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < modes.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(modes[i].name());
        }
        return sb.toString();
    }

    /**
     * Gets the string truncation configuration from environment variable.
     *
     * @return true if exceptions should be thrown when strings are truncated, false if truncation
     *     should be allowed
     */
    private static boolean getThrowOnStringTruncationConfig() {
        String envValue = System.getenv(THROW_ON_STRING_TRUNCATION_ENV_VAR);
        if (envValue == null) {
            logger.trace(
                    "Environment variable {} not set, using default: {}",
                    THROW_ON_STRING_TRUNCATION_ENV_VAR,
                    DEFAULT_THROW_ON_STRING_TRUNCATION);
            return DEFAULT_THROW_ON_STRING_TRUNCATION;
        }

        try {
            boolean config = Boolean.parseBoolean(envValue);
            logger.trace(
                    "Environment variable {} set to: {}",
                    THROW_ON_STRING_TRUNCATION_ENV_VAR,
                    config);
            return config;
        } catch (Exception e) {
            logger.warn(
                    "Environment variable {} has invalid value '{}', using default: {}",
                    THROW_ON_STRING_TRUNCATION_ENV_VAR,
                    envValue,
                    DEFAULT_THROW_ON_STRING_TRUNCATION);
            return DEFAULT_THROW_ON_STRING_TRUNCATION;
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
     * Gets the environment variable name for null string handling configuration.
     *
     * @return the environment variable name
     */
    public static String getNullHandlingEnvVarName() {
        return NULL_HANDLING_ENV_VAR;
    }

    /**
     * Gets the character to use for padding null values.
     *
     * @return the character to use for padding null values
     */
    public static char getNullValuePadding() {
        return NULL_VALUE_PADDING;
    }

    /**
     * Gets the environment variable name for null value padding configuration.
     *
     * @return the environment variable name
     */
    public static String getNullValuePaddingEnvVarName() {
        return NULL_VALUE_PADDING_ENV_VAR;
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
     * Gets the environment variable name for BigDecimal decimal separator configuration.
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

    /**
     * Checks if exceptions should be thrown when strings are truncated during formatting.
     *
     * @return true if exceptions should be thrown when strings are truncated, false if truncation
     *     should be allowed
     */
    public static boolean isThrowOnStringTruncationEnabled() {
        return THROW_ON_STRING_TRUNCATION;
    }

    /**
     * Gets the environment variable name for string truncation configuration.
     *
     * @return the environment variable name
     */
    public static String getThrowOnStringTruncationEnvVarName() {
        return THROW_ON_STRING_TRUNCATION_ENV_VAR;
    }
}
