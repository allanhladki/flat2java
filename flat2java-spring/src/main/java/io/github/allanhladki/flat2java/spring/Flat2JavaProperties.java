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

    public boolean isFailOnStringTruncation() {
        return failOnStringTruncation;
    }

    public void setFailOnStringTruncation(boolean failOnStringTruncation) {
        this.failOnStringTruncation = failOnStringTruncation;
    }

    public Decimal getDecimal() {
        return decimal;
    }

    /** {@code flat2java.decimal.*} properties. */
    public static class Decimal {

        /** Whether BigDecimal fields with a precision are written with an explicit separator. */
        private boolean useSeparator = false;

        /** Decimal separator used when use-separator is true. */
        private char separator = '.';

        /** Rounding applied when a value has more decimal places than its precision. */
        private RoundingMode roundingMode = RoundingMode.UNNECESSARY;

        public boolean isUseSeparator() {
            return useSeparator;
        }

        public void setUseSeparator(boolean useSeparator) {
            this.useSeparator = useSeparator;
        }

        public char getSeparator() {
            return separator;
        }

        public void setSeparator(char separator) {
            this.separator = separator;
        }

        public RoundingMode getRoundingMode() {
            return roundingMode;
        }

        public void setRoundingMode(RoundingMode roundingMode) {
            this.roundingMode = roundingMode;
        }
    }
}
