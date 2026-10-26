package io.github.allanhladki.flat2java.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Scalar field types supported by {@code @FlatField}, identified by type name so that both
 * reflection ({@link Class#getName()}) and annotation processing can use them.
 */
public enum FieldKind {
    STRING("java.lang.String"),
    INT("int", "java.lang.Integer"),
    LONG("long", "java.lang.Long"),
    BIG_INTEGER("java.math.BigInteger"),
    BIG_DECIMAL("java.math.BigDecimal"),
    LOCAL_DATE("java.time.LocalDate"),
    LOCAL_DATE_TIME("java.time.LocalDateTime");

    /** Human-readable list of the supported scalar types, used in error messages. */
    public static final String SUPPORTED_TYPES =
            "String, int/Integer, long/Long, BigInteger, BigDecimal, LocalDate, LocalDateTime";

    private static final Map<String, FieldKind> BY_TYPE_NAME = new HashMap<>();

    static {
        for (FieldKind kind : values()) {
            for (String name : kind.typeNames) {
                BY_TYPE_NAME.put(name, kind);
            }
        }
    }

    private final String[] typeNames;

    FieldKind(String... typeNames) {
        this.typeNames = typeNames;
    }

    /**
     * Returns the kind of a type given its binary name ({@code int}, {@code java.lang.String}).
     *
     * @param typeName the type name
     * @return the kind, or empty if the type is not a supported scalar
     */
    public static Optional<FieldKind> ofTypeName(String typeName) {
        return Optional.ofNullable(BY_TYPE_NAME.get(typeName));
    }

    /**
     * Whether values of this kind are written as zero-filled digits.
     *
     * @return {@code true} for numeric kinds
     */
    public boolean isNumeric() {
        return this == INT || this == LONG || this == BIG_INTEGER || this == BIG_DECIMAL;
    }

    /**
     * Whether values of this kind are written with a date/time pattern.
     *
     * @return {@code true} for {@code LocalDate} and {@code LocalDateTime}
     */
    public boolean isTemporal() {
        return this == LOCAL_DATE || this == LOCAL_DATE_TIME;
    }
}
