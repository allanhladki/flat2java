package io.github.allanhladki.flat2java.internal;

import io.github.allanhladki.flat2java.exception.MappingException;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Validation rules for {@code @FlatField} declarations. The rules take plain values instead of
 * reflection objects so that the runtime and the annotation processor apply exactly the same checks
 * and report the same messages.
 *
 * <p>Every method throws {@link MappingException} with a message that starts with the field label
 * ({@code Type.field}).
 */
public final class MappingRules {

    /** Default padding character of {@code @FlatField}. */
    public static final char DEFAULT_PADDING = ' ';

    // Two samples whose formatted width differs for every variable-width pattern letter
    // (day 1 vs 28, month 1 vs 12, January vs December, Monday vs Saturday, hour 1 vs 23...).
    private static final LocalDateTime SAMPLE_A = LocalDateTime.of(2024, 1, 1, 1, 1, 1, 0);
    private static final LocalDateTime SAMPLE_B =
            LocalDateTime.of(2024, 12, 28, 23, 59, 59, 123_456_789);

    private MappingRules() {}

    /** A field with its {@code position} attribute, for {@link #checkPositions}. */
    public static final class Positioned {
        private final String label;
        private final int position;

        /**
         * @param label the field label
         * @param position the declared position, {@code 0} when not set
         */
        public Positioned(String label, int position) {
            this.label = label;
            this.position = position;
        }

        /**
         * @return the field label
         */
        public String label() {
            return label;
        }

        /**
         * @return the declared position, {@code 0} when not set
         */
        public int position() {
            return position;
        }
    }

    /**
     * Builds the label used in messages.
     *
     * @param declaringType simple name of the class that declares the field
     * @param fieldName the field name
     * @return {@code declaringType.fieldName}
     */
    public static String label(String declaringType, String fieldName) {
        return declaringType + "." + fieldName;
    }

    /**
     * Rejects {@code static} fields.
     *
     * @param label the field label
     * @param isStatic whether the field is static
     */
    public static void checkNotStatic(String label, boolean isStatic) {
        if (isStatic) {
            throw new MappingException(label + ": @FlatField is not allowed on static fields");
        }
    }

    /**
     * Returns the error for a field whose type is neither a supported scalar nor a nested record.
     *
     * @param label the field label
     * @param typeName the declared type
     * @param reason why the type cannot be used, or {@code null}
     * @return the exception to throw
     */
    public static MappingException unsupportedType(String label, String typeName, String reason) {
        String message =
                label
                        + ": unsupported type "
                        + typeName
                        + (reason == null ? "" : " (" + reason + ")")
                        + ". Supported types: "
                        + FieldKind.SUPPORTED_TYPES
                        + ", a concrete class or record with @FlatField fields, or a List or"
                        + " array of those types.";
        return new MappingException(message);
    }

    /**
     * Validates the attributes of a scalar field.
     *
     * @param label the field label
     * @param kind the field kind
     * @param length the declared length
     * @param precision the declared precision
     * @param dateTimeFormat the declared date/time pattern, empty when not set
     * @param padding the declared padding character
     */
    public static void checkScalar(
            String label,
            FieldKind kind,
            int length,
            int precision,
            String dateTimeFormat,
            char padding) {
        checkLength(label, length);
        if (precision < 0) {
            throw new MappingException(
                    label + ": precision must not be negative, got " + precision);
        }
        if (precision != 0 && kind != FieldKind.BIG_DECIMAL) {
            throw new MappingException(label + ": precision is only allowed on BigDecimal fields");
        }
        if (precision >= length) {
            throw new MappingException(
                    label
                            + ": precision ("
                            + precision
                            + ") must be smaller than length ("
                            + length
                            + ")");
        }
        if (padding != DEFAULT_PADDING && kind != FieldKind.STRING) {
            throw new MappingException(
                    label
                            + ": padding is only allowed on String fields; numeric fields are"
                            + " always zero-filled");
        }
        boolean hasFormat = dateTimeFormat != null && !dateTimeFormat.isEmpty();
        if (kind.isTemporal()) {
            if (!hasFormat) {
                throw new MappingException(
                        label + ": dateTimeFormat is required for LocalDate/LocalDateTime fields");
            }
            int width = temporalWidth(label, kind, dateTimeFormat);
            if (width != length) {
                throw new MappingException(
                        label
                                + ": dateTimeFormat '"
                                + dateTimeFormat
                                + "' produces "
                                + width
                                + " characters, but length is "
                                + length);
            }
        } else if (hasFormat) {
            throw new MappingException(
                    label + ": dateTimeFormat is only allowed on LocalDate/LocalDateTime fields");
        }
    }

    /**
     * Validates the attributes of a nested record field.
     *
     * @param label the field label
     * @param nestedType simple name of the nested type
     * @param length the declared length
     * @param nestedLength the sum of the lengths of the nested type's fields
     * @param precision the declared precision
     * @param dateTimeFormat the declared date/time pattern
     * @param padding the declared padding character
     */
    public static void checkNested(
            String label,
            String nestedType,
            int length,
            int nestedLength,
            int precision,
            String dateTimeFormat,
            char padding) {
        checkLength(label, length);
        if (precision != 0
                || (dateTimeFormat != null && !dateTimeFormat.isEmpty())
                || padding != DEFAULT_PADDING) {
            throw new MappingException(
                    label
                            + ": precision, dateTimeFormat and padding are not allowed on a nested"
                            + " record field");
        }
        if (length != nestedLength) {
            throw new MappingException(
                    label
                            + ": length is "
                            + length
                            + ", but the fields of "
                            + nestedType
                            + " add up to "
                            + nestedLength);
        }
    }

    /**
     * Validates the {@code occurs} attribute.
     *
     * @param label the field label
     * @param repeated whether the field is a {@code List} or an array
     * @param length the declared length of one element
     * @param occurs the declared number of elements, {@code 0} when not set
     */
    public static void checkOccurs(String label, boolean repeated, int length, int occurs) {
        if (!repeated) {
            if (occurs != 0) {
                throw new MappingException(
                        label + ": occurs is only allowed on List and array fields");
            }
            return;
        }
        if (occurs <= 0) {
            throw new MappingException(
                    label
                            + ": occurs is required on List and array fields and must be greater"
                            + " than 0, got "
                            + occurs);
        }
        if ((long) length * occurs > Integer.MAX_VALUE) {
            throw new MappingException(
                    label + ": length * occurs exceeds " + Integer.MAX_VALUE + " characters");
        }
    }

    /**
     * Validates the {@code position} attributes of all fields of a class hierarchy.
     *
     * @param typeName simple name of the class being mapped
     * @param fields the fields, superclass first, in declaration order
     */
    public static void checkPositions(String typeName, List<Positioned> fields) {
        List<String> missing = new ArrayList<>();
        Map<Integer, String> byPosition = new HashMap<>();
        for (Positioned field : fields) {
            if (field.position() < 0) {
                throw new MappingException(
                        field.label() + ": position must not be negative, got " + field.position());
            }
            if (field.position() == 0) {
                missing.add(field.label());
                continue;
            }
            String previous = byPosition.putIfAbsent(field.position(), field.label());
            if (previous != null) {
                throw new MappingException(
                        typeName
                                + ": position "
                                + field.position()
                                + " is used by both "
                                + previous
                                + " and "
                                + field.label());
            }
        }
        if (byPosition.isEmpty()) {
            return;
        }
        if (!missing.isEmpty()) {
            throw new MappingException(
                    typeName
                            + ": either every @FlatField sets position or none does; missing on "
                            + String.join(", ", missing));
        }
        for (int expected = 1; expected <= fields.size(); expected++) {
            if (!byPosition.containsKey(expected)) {
                throw new MappingException(
                        typeName
                                + ": positions must be contiguous starting at 1; position "
                                + expected
                                + " is missing, found "
                                + byPosition.keySet().stream()
                                        .sorted()
                                        .map(String::valueOf)
                                        .collect(Collectors.joining(", ", "[", "]")));
            }
        }
    }

    /**
     * Rejects a nested type that contains itself, directly or indirectly.
     *
     * @param path simple names of the types being mapped, outermost first
     * @param nestedType simple name of the nested type about to be added
     * @param contained whether {@code nestedType} already occurs in the path
     */
    public static void checkNoCycle(List<String> path, String nestedType, boolean contained) {
        if (contained) {
            throw new MappingException(
                    "Cyclic nested records: " + String.join(" -> ", path) + " -> " + nestedType);
        }
    }

    /**
     * Creates the formatter used for date/time fields: {@link Locale#ROOT}, {@link
     * ResolverStyle#STRICT} and the ISO era defaulted, so that {@code yyyy} works in strict mode.
     *
     * @param pattern the {@link DateTimeFormatter} pattern
     * @return the formatter
     * @throws IllegalArgumentException if the pattern is invalid
     */
    public static DateTimeFormatter formatter(String pattern) {
        return new DateTimeFormatterBuilder()
                .appendPattern(pattern)
                .parseDefaulting(ChronoField.ERA, 1)
                .toFormatter(Locale.ROOT)
                .withResolverStyle(ResolverStyle.STRICT);
    }

    private static int temporalWidth(String label, FieldKind kind, String pattern) {
        DateTimeFormatter formatter;
        try {
            formatter = formatter(pattern);
        } catch (IllegalArgumentException e) {
            throw new MappingException(
                    label + ": invalid dateTimeFormat '" + pattern + "': " + e.getMessage(), e);
        }
        String a;
        String b;
        try {
            if (kind == FieldKind.LOCAL_DATE) {
                a = formatter.format(SAMPLE_A.toLocalDate());
                b = formatter.format(SAMPLE_B.toLocalDate());
                LocalDate.parse(a, formatter);
                LocalDate.parse(b, formatter);
            } else {
                a = formatter.format(SAMPLE_A);
                b = formatter.format(SAMPLE_B);
                LocalDateTime.parse(a, formatter);
                LocalDateTime.parse(b, formatter);
            }
        } catch (DateTimeException e) {
            throw new MappingException(
                    label
                            + ": dateTimeFormat '"
                            + pattern
                            + "' cannot represent a "
                            + (kind == FieldKind.LOCAL_DATE ? "LocalDate" : "LocalDateTime")
                            + ": "
                            + e.getMessage(),
                    e);
        }
        if (a.length() != b.length()) {
            throw new MappingException(
                    label
                            + ": dateTimeFormat '"
                            + pattern
                            + "' has a variable width ('"
                            + a
                            + "' vs '"
                            + b
                            + "'); use fixed-width pattern letters such as dd, MM, HH");
        }
        return a.length();
    }

    private static void checkLength(String label, int length) {
        if (length <= 0) {
            throw new MappingException(label + ": length must be greater than 0, got " + length);
        }
    }
}
