package io.github.allanhladki.flat2java;

import java.math.RoundingMode;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Immutable settings of a {@link Flat2Java} instance.
 *
 * <pre>{@code
 * Flat2JavaOptions options = Flat2JavaOptions.builder()
 *         .useDecimalSeparator(true)
 *         .decimalSeparator(',')
 *         .roundingMode(RoundingMode.HALF_UP)
 *         .build();
 * }</pre>
 *
 * <p>The same settings can be read from environment variables with {@link #fromEnvironment()}.
 */
public final class Flat2JavaOptions {

    /** Environment variable for {@link #allowNullStrings()}. */
    public static final String ENV_ALLOW_NULL_STRINGS = "FLAT2JAVA_ALLOW_NULL_STRINGS";

    /** Environment variable for {@link #nullValuePadding()}. */
    public static final String ENV_NULL_VALUE_PADDING = "FLAT2JAVA_NULL_VALUE_PADDING";

    /** Environment variable for {@link #failOnStringTruncation()}. */
    public static final String ENV_FAIL_ON_STRING_TRUNCATION =
            "FLAT2JAVA_FAIL_ON_STRING_TRUNCATION";

    /** Environment variable for {@link #useDecimalSeparator()}. */
    public static final String ENV_DECIMAL_USE_SEPARATOR = "FLAT2JAVA_DECIMAL_USE_SEPARATOR";

    /** Environment variable for {@link #decimalSeparator()}. */
    public static final String ENV_DECIMAL_SEPARATOR = "FLAT2JAVA_DECIMAL_SEPARATOR";

    /** Environment variable for {@link #roundingMode()}. */
    public static final String ENV_DECIMAL_ROUNDING_MODE = "FLAT2JAVA_DECIMAL_ROUNDING_MODE";

    private static final Flat2JavaOptions DEFAULTS = builder().build();

    private final boolean allowNullStrings;
    private final char nullValuePadding;
    private final boolean failOnStringTruncation;
    private final boolean useDecimalSeparator;
    private final char decimalSeparator;
    private final RoundingMode roundingMode;

    private Flat2JavaOptions(Builder builder) {
        this.allowNullStrings = builder.allowNullStrings;
        this.nullValuePadding = builder.nullValuePadding;
        this.failOnStringTruncation = builder.failOnStringTruncation;
        this.useDecimalSeparator = builder.useDecimalSeparator;
        this.decimalSeparator = builder.decimalSeparator;
        this.roundingMode = builder.roundingMode;
    }

    /**
     * Returns the default options.
     *
     * @return the default options
     */
    public static Flat2JavaOptions defaults() {
        return DEFAULTS;
    }

    /**
     * Returns a builder initialized with the default values.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Reads the options from the {@code FLAT2JAVA_*} environment variables. Variables that are not
     * set keep their default value.
     *
     * @return the options
     * @throws IllegalArgumentException if a variable has an invalid value
     */
    public static Flat2JavaOptions fromEnvironment() {
        return fromEnvironment(System.getenv());
    }

    static Flat2JavaOptions fromEnvironment(Map<String, String> env) {
        Builder builder = builder();
        read(env, ENV_ALLOW_NULL_STRINGS, Flat2JavaOptions::parseBoolean)
                .ifPresent(builder::allowNullStrings);
        read(env, ENV_NULL_VALUE_PADDING, Flat2JavaOptions::parseChar)
                .ifPresent(builder::nullValuePadding);
        read(env, ENV_FAIL_ON_STRING_TRUNCATION, Flat2JavaOptions::parseBoolean)
                .ifPresent(builder::failOnStringTruncation);
        read(env, ENV_DECIMAL_USE_SEPARATOR, Flat2JavaOptions::parseBoolean)
                .ifPresent(builder::useDecimalSeparator);
        read(env, ENV_DECIMAL_SEPARATOR, Flat2JavaOptions::parseChar)
                .ifPresent(builder::decimalSeparator);
        read(
                        env,
                        ENV_DECIMAL_ROUNDING_MODE,
                        value -> RoundingMode.valueOf(value.trim().toUpperCase(Locale.ROOT)))
                .ifPresent(builder::roundingMode);
        return builder.build();
    }

    private static <T> Optional<T> read(
            Map<String, String> env, String name, Function<String, T> parser) {
        String value = env.get(name);
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(parser.apply(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid value '" + value + "' for environment variable " + name, e);
        }
    }

    private static boolean parseBoolean(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("true")) {
            return true;
        }
        if (normalized.equals("false")) {
            return false;
        }
        throw new IllegalArgumentException("expected 'true' or 'false'");
    }

    private static char parseChar(String value) {
        if (value.length() != 1) {
            throw new IllegalArgumentException("expected a single character");
        }
        return value.charAt(0);
    }

    /**
     * Whether a {@code null} {@code String} field is written as an empty column. When {@code
     * false}, writing a {@code null} string fails. Fields of other types never accept {@code null}.
     * Default: {@code true}.
     *
     * @return whether {@code null} strings are allowed
     */
    public boolean allowNullStrings() {
        return allowNullStrings;
    }

    /**
     * Character that fills the column of a {@code null} {@code String} field whose {@code padding}
     * is the default space. Default: space.
     *
     * @return the fill character for {@code null} strings
     */
    public char nullValuePadding() {
        return nullValuePadding;
    }

    /**
     * Whether a {@code String} longer than its column fails instead of being truncated. Default:
     * {@code false}.
     *
     * @return whether truncation fails
     */
    public boolean failOnStringTruncation() {
        return failOnStringTruncation;
    }

    /**
     * Whether {@code BigDecimal} fields with a precision greater than zero are written with an
     * explicit decimal separator ({@code 0012.34}) instead of an implied one ({@code 001234}).
     * Default: {@code false}.
     *
     * @return whether the decimal separator is written
     */
    public boolean useDecimalSeparator() {
        return useDecimalSeparator;
    }

    /**
     * Decimal separator used when {@link #useDecimalSeparator()} is {@code true}. Default: {@code
     * '.'}.
     *
     * @return the decimal separator
     */
    public char decimalSeparator() {
        return decimalSeparator;
    }

    /**
     * Rounding applied when a {@code BigDecimal} has more decimal places than its precision. The
     * default, {@link RoundingMode#UNNECESSARY}, makes such values fail instead of being rounded.
     *
     * @return the rounding mode
     */
    public RoundingMode roundingMode() {
        return roundingMode;
    }

    /**
     * Returns a builder initialized with these options.
     *
     * @return a new builder
     */
    public Builder toBuilder() {
        return new Builder()
                .allowNullStrings(allowNullStrings)
                .nullValuePadding(nullValuePadding)
                .failOnStringTruncation(failOnStringTruncation)
                .useDecimalSeparator(useDecimalSeparator)
                .decimalSeparator(decimalSeparator)
                .roundingMode(roundingMode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Flat2JavaOptions)) {
            return false;
        }
        Flat2JavaOptions that = (Flat2JavaOptions) o;
        return allowNullStrings == that.allowNullStrings
                && nullValuePadding == that.nullValuePadding
                && failOnStringTruncation == that.failOnStringTruncation
                && useDecimalSeparator == that.useDecimalSeparator
                && decimalSeparator == that.decimalSeparator
                && roundingMode == that.roundingMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                allowNullStrings,
                nullValuePadding,
                failOnStringTruncation,
                useDecimalSeparator,
                decimalSeparator,
                roundingMode);
    }

    @Override
    public String toString() {
        return "Flat2JavaOptions{allowNullStrings="
                + allowNullStrings
                + ", nullValuePadding='"
                + nullValuePadding
                + "', failOnStringTruncation="
                + failOnStringTruncation
                + ", useDecimalSeparator="
                + useDecimalSeparator
                + ", decimalSeparator='"
                + decimalSeparator
                + "', roundingMode="
                + roundingMode
                + '}';
    }

    /** Builder of {@link Flat2JavaOptions}. Not thread-safe. */
    public static final class Builder {
        private boolean allowNullStrings = true;
        private char nullValuePadding = ' ';
        private boolean failOnStringTruncation = false;
        private boolean useDecimalSeparator = false;
        private char decimalSeparator = '.';
        private RoundingMode roundingMode = RoundingMode.UNNECESSARY;

        private Builder() {}

        /**
         * Sets {@link Flat2JavaOptions#allowNullStrings()}.
         *
         * @param value the new value
         * @return this builder
         */
        public Builder allowNullStrings(boolean value) {
            this.allowNullStrings = value;
            return this;
        }

        /**
         * Sets {@link Flat2JavaOptions#nullValuePadding()}.
         *
         * @param value the new value
         * @return this builder
         */
        public Builder nullValuePadding(char value) {
            this.nullValuePadding = value;
            return this;
        }

        /**
         * Sets {@link Flat2JavaOptions#failOnStringTruncation()}.
         *
         * @param value the new value
         * @return this builder
         */
        public Builder failOnStringTruncation(boolean value) {
            this.failOnStringTruncation = value;
            return this;
        }

        /**
         * Sets {@link Flat2JavaOptions#useDecimalSeparator()}.
         *
         * @param value the new value
         * @return this builder
         */
        public Builder useDecimalSeparator(boolean value) {
            this.useDecimalSeparator = value;
            return this;
        }

        /**
         * Sets {@link Flat2JavaOptions#decimalSeparator()}.
         *
         * @param value the new value, which must not be a digit
         * @return this builder
         * @throws IllegalArgumentException if {@code value} is a digit
         */
        public Builder decimalSeparator(char value) {
            if (value >= '0' && value <= '9') {
                throw new IllegalArgumentException(
                        "Decimal separator must not be a digit: '" + value + "'");
            }
            this.decimalSeparator = value;
            return this;
        }

        /**
         * Sets {@link Flat2JavaOptions#roundingMode()}.
         *
         * @param value the new value
         * @return this builder
         */
        public Builder roundingMode(RoundingMode value) {
            this.roundingMode = Objects.requireNonNull(value, "roundingMode");
            return this;
        }

        /**
         * Builds the options.
         *
         * @return the options
         */
        public Flat2JavaOptions build() {
            return new Flat2JavaOptions(this);
        }
    }
}
