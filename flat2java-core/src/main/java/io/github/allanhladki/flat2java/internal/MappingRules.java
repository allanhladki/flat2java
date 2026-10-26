package io.github.allanhladki.flat2java.internal;

import io.github.allanhladki.flat2java.exception.MappingException;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalQuery;
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

    /**
     * A field with its {@code position} attribute, for {@link #checkPositions}.
     *
     * @param label the field label
     * @param position the declared position, {@code 0} when not set
     */
    public record Positioned(String label, int position) {}

    /** What a declared type is, as far as nesting is concerned; see {@link #checkNestable}. */
    public enum TypeShape {
        /** A type variable such as {@code T}. */
        TYPE_VARIABLE,
        /** {@code java.lang.Object}. */
        OBJECT,
        /** A primitive, array or enum that is not a supported scalar. */
        NOT_A_CLASS,
        /** An interface or abstract class. */
        ABSTRACT,
        /** A concrete class or record without {@code @FlatField} fields. */
        NO_ANNOTATED_FIELDS,
        /** A concrete class or record with {@code @FlatField} fields. */
        NESTABLE
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
     * Rejects a field type that is neither a supported scalar nor a nested record.
     *
     * @param label the field label
     * @param typeName the declared type
     * @param shape what the type is
     */
    public static void checkNestable(String label, String typeName, TypeShape shape) {
        switch (shape) {
            case TYPE_VARIABLE ->
                    throw unsupportedType(
                            label, typeName, "type variables cannot be instantiated when reading");
            case OBJECT ->
                    throw unsupportedType(
                            label, typeName, "declare the concrete class of the nested record");
            case ABSTRACT ->
                    throw unsupportedType(
                            label, typeName, "abstract types cannot be instantiated when reading");
            case NOT_A_CLASS, NO_ANNOTATED_FIELDS -> throw unsupportedType(label, typeName, null);
            case NESTABLE -> {}
        }
    }

    /**
     * Validates the element type of a {@code List} or array field.
     *
     * @param label the field label
     * @param typeName the declared type of the field
     * @param rawList whether the field is a {@code List} without a type argument
     * @param wildcard whether the type argument is a wildcard
     * @param elementRepeated whether the element type is itself a {@code List} or an array
     */
    public static void checkElementType(
            String label,
            String typeName,
            boolean rawList,
            boolean wildcard,
            boolean elementRepeated) {
        if (rawList) {
            throw unsupportedType(label, typeName, "declare the element type, as in List<String>");
        }
        if (wildcard) {
            throw unsupportedType(label, typeName, "declare the element type without a wildcard");
        }
        if (elementRepeated) {
            throw unsupportedType(
                    label, typeName, "the elements of a List or array cannot be Lists or arrays");
        }
    }

    /**
     * Returns the error for a class without a no-arg constructor.
     *
     * @param typeName simple name of the class
     * @param innerClass whether the class is a non-static member class
     * @return the exception to throw
     */
    public static MappingException noArgConstructorRequired(String typeName, boolean innerClass) {
        return new MappingException(
                typeName
                        + ": a no-arg constructor is required"
                        + (innerClass ? " (inner classes must be declared static)" : ""));
    }

    /**
     * Returns the number of characters a field takes.
     *
     * @param length the declared length
     * @param occurs the declared number of elements, {@code 0} for a field that is not repeated
     * @return {@code length}, times {@code occurs} for a repeated field
     */
    public static int width(int length, int occurs) {
        return occurs > 0 ? length * occurs : length;
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
        boolean hasFormat = hasText(dateTimeFormat);
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
     * Validates a {@code BigDecimal} field written with an explicit decimal separator. Depends on
     * the {@code useDecimalSeparator} option, so it is checked when the layout is built, not by the
     * annotation processor.
     *
     * @param label the field label
     * @param length the declared length
     * @param precision the declared precision
     */
    public static void checkDecimalSeparator(String label, int length, int precision) {
        if (precision > 0 && length < precision + 2) {
            throw new MappingException(
                    label
                            + ": length "
                            + length
                            + " cannot hold "
                            + precision
                            + " decimal places plus a decimal separator");
        }
    }

    /**
     * Returns the error for a record whose total length does not fit an {@code int}.
     *
     * @param typeName simple name of the mapped class
     * @return the exception to throw
     */
    public static MappingException recordTooLong(String typeName) {
        return new MappingException(
                typeName + ": the record length exceeds " + Integer.MAX_VALUE + " characters");
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
        if (precision != 0 || hasText(dateTimeFormat) || padding != DEFAULT_PADDING) {
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
        boolean dateOnly = kind == FieldKind.LOCAL_DATE;
        TemporalQuery<?> query = dateOnly ? LocalDate::from : LocalDateTime::from;
        String a;
        String b;
        try {
            a = formatter.format(dateOnly ? SAMPLE_A.toLocalDate() : SAMPLE_A);
            b = formatter.format(dateOnly ? SAMPLE_B.toLocalDate() : SAMPLE_B);
            formatter.parse(a, query);
            formatter.parse(b, query);
        } catch (DateTimeException e) {
            throw new MappingException(
                    label
                            + ": dateTimeFormat '"
                            + pattern
                            + "' cannot represent a "
                            + (dateOnly ? "LocalDate" : "LocalDateTime")
                            + ": "
                            + e.getMessage(),
                    e);
        }
        String centuryEarlier =
                formatter.format(
                        dateOnly
                                ? SAMPLE_A.minusYears(100).toLocalDate()
                                : SAMPLE_A.minusYears(100));
        if (centuryEarlier.equals(a)) {
            throw new MappingException(
                    label
                            + ": dateTimeFormat '"
                            + pattern
                            + "' does not keep the century (as with yy); use yyyy");
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

    private static boolean hasText(String value) {
        return value != null && !value.isEmpty();
    }

    private static void checkLength(String label, int length) {
        if (length <= 0) {
            throw new MappingException(label + ": length must be greater than 0, got " + length);
        }
    }
}
