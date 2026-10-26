package io.github.allanhladki.flat2java.internal;

import java.math.RoundingMode;
import java.util.Locale;
import java.util.function.Function;

/**
 * Parsers for option values given as text, shared by {@code Flat2JavaOptions.fromEnvironment()} and
 * the framework integrations so that every source accepts the same values.
 */
public final class OptionValues {

    private OptionValues() {}

    /**
     * Parses {@code value} with {@code parser}, naming the setting in the error.
     *
     * @param setting the environment variable or property name, used in messages
     * @param value the raw value
     * @param parser one of the parsers of this class
     * @param <T> the parsed type
     * @return the parsed value
     * @throws IllegalArgumentException if the value is invalid
     */
    public static <T> T parse(String setting, String value, Function<String, T> parser) {
        try {
            return parser.apply(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid value '" + value + "' for " + setting + ": " + e.getMessage(), e);
        }
    }

    /**
     * @param value {@code true} or {@code false}, in any case
     * @return the boolean
     */
    public static boolean parseBoolean(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("true")) {
            return true;
        }
        if (normalized.equals("false")) {
            return false;
        }
        throw new IllegalArgumentException("expected 'true' or 'false'");
    }

    /**
     * @param value a single character, not trimmed so that a space can be given
     * @return the character
     */
    public static char parseChar(String value) {
        if (value.length() != 1) {
            throw new IllegalArgumentException("expected a single character");
        }
        return value.charAt(0);
    }

    /**
     * @param value a {@link RoundingMode} name in any case, with {@code -} or {@code _} ({@code
     *     half-up}, {@code HALF_UP})
     * @return the rounding mode
     */
    public static RoundingMode parseRoundingMode(String value) {
        try {
            return RoundingMode.valueOf(value.trim().replace('-', '_').toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("expected a java.math.RoundingMode name");
        }
    }
}
