package io.github.allanhladki.flat2java.spring;

import io.github.allanhladki.flat2java.Flat2JavaOptions;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.RoundingMode;

/**
 * {@code flat2java.*} configuration properties. Each property maps to the {@link Flat2JavaOptions}
 * setting of the same name and has the same default.
 *
 * <pre>
 * flat2java:
 *   allow-null-strings: true
 *   null-value-padding: "*"
 *   fail-on-string-truncation: true
 *   decimal:
 *     use-separator: true
 *     separator: ","
 *     rounding-mode: half-up
 * </pre>
 */
@ConfigurationProperties(prefix = "flat2java")
public class Flat2JavaProperties {

    /** Creates properties with the default values. */
    public Flat2JavaProperties() {}

    /** Whether a null String field is written as an empty column. */
    private boolean allowNullStrings = true;

    /** Character that fills the column of a null String field with the default padding. */
    private char nullValuePadding = ' ';

    /** Whether a String longer than its column fails instead of being truncated. */
    private boolean failOnStringTruncation = false;

    /** BigDecimal settings. */
    private final Decimal decimal = new Decimal();

    /**
     * Converts these properties to options.
     *
     * @return the options
     */
    public Flat2JavaOptions toOptions() {
        return Flat2JavaOptions.builder()
                .allowNullStrings(allowNullStrings)
                .nullValuePadding(nullValuePadding)
                .failOnStringTruncation(failOnStringTruncation)
                .useDecimalSeparator(decimal.useSeparator)
                .decimalSeparator(decimal.separator)
                .roundingMode(decimal.roundingMode)
                .build();
    }

    /**
     * Returns whether a null String field is written as an empty column.
     *
     * @return whether a null String field is written as an empty column
     */
    public boolean isAllowNullStrings() {
        return allowNullStrings;
    }

    /**
     * Sets whether a null String field is written as an empty column.
     *
     * @param allowNullStrings whether a null String field is written as an empty column
     */
    public void setAllowNullStrings(boolean allowNullStrings) {
        this.allowNullStrings = allowNullStrings;
    }

    /**
     * Returns the fill character of null String fields.
     *
     * @return the fill character of null String fields
     */
    public char getNullValuePadding() {
        return nullValuePadding;
    }

    /**
     * Sets the fill character of null String fields.
     *
     * @param nullValuePadding the fill character of null String fields
     */
    public void setNullValuePadding(char nullValuePadding) {
        this.nullValuePadding = nullValuePadding;
    }

    /**
     * Returns whether a String longer than its column fails.
     *
     * @return whether a String longer than its column fails
     */
    public boolean isFailOnStringTruncation() {
        return failOnStringTruncation;
    }

    /**
     * Sets whether a String longer than its column fails.
     *
     * @param failOnStringTruncation whether a String longer than its column fails
     */
    public void setFailOnStringTruncation(boolean failOnStringTruncation) {
        this.failOnStringTruncation = failOnStringTruncation;
    }

    /**
     * Returns the {@code flat2java.decimal.*} properties.
     *
     * @return the {@code flat2java.decimal.*} properties
     */
    public Decimal getDecimal() {
        return decimal;
    }

    /** {@code flat2java.decimal.*} properties. */
    public static class Decimal {

        /** Creates decimal properties with the default values. */
        public Decimal() {}

        /** Whether BigDecimal fields with a precision are written with an explicit separator. */
        private boolean useSeparator = false;

        /** Decimal separator used when use-separator is true. */
        private char separator = '.';

        /** Rounding applied when a value has more decimal places than its precision. */
        private RoundingMode roundingMode = RoundingMode.UNNECESSARY;

        /**
         * Returns whether BigDecimal fields are written with an explicit separator.
         *
         * @return whether BigDecimal fields are written with an explicit separator
         */
        public boolean isUseSeparator() {
            return useSeparator;
        }

        /**
         * Sets whether BigDecimal fields are written with an explicit separator.
         *
         * @param useSeparator whether BigDecimal fields are written with an explicit separator
         */
        public void setUseSeparator(boolean useSeparator) {
            this.useSeparator = useSeparator;
        }

        /**
         * Returns the decimal separator.
         *
         * @return the decimal separator
         */
        public char getSeparator() {
            return separator;
        }

        /**
         * Sets the decimal separator.
         *
         * @param separator the decimal separator
         */
        public void setSeparator(char separator) {
            this.separator = separator;
        }

        /**
         * Returns the rounding mode.
         *
         * @return the rounding mode
         */
        public RoundingMode getRoundingMode() {
            return roundingMode;
        }

        /**
         * Sets the rounding mode.
         *
         * @param roundingMode the rounding mode
         */
        public void setRoundingMode(RoundingMode roundingMode) {
            this.roundingMode = roundingMode;
        }
    }
}
