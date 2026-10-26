package io.github.allanhladki.flat2java.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Maps a field to a fixed-width column of a flat record.
 *
 * <p>Supported field types are {@code String}, {@code int}/{@code Integer}, {@code long}/{@code
 * Long}, {@code BigInteger}, {@code BigDecimal}, {@code LocalDate}, {@code LocalDateTime}, any
 * concrete class or record whose own fields are annotated with {@code @FlatField} (a nested
 * record), and a {@code List} or array of one of those types with {@link #occurs()} set. See the
 * project README for the encoding of each type.
 *
 * <p>Without {@link #position()}, fields are laid out in declaration order, superclass first. When
 * positions are set, they order every field of the class hierarchy, superclass fields included. On
 * a Java record, annotate the record components; their declaration order is the component order.
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FlatField {

    /**
     * One-based position of the field in the record. The default {@code 0} means "not set", in
     * which case declaration order is used.
     *
     * <p>Either every annotated field of a class hierarchy sets a position or none does. Positions
     * must be unique and contiguous, starting at 1. Declaration order is what the JVM reports
     * through reflection; it matches the source order on mainstream JVMs, but the Java
     * specification does not guarantee it. Set positions when the layout must not depend on it.
     *
     * @return the position, or {@code 0} when not set
     */
    int position() default 0;

    /**
     * Width of the field in characters. For a nested record it must equal the sum of the lengths of
     * the nested type's fields. For a {@code List} or array field it is the width of one element.
     *
     * @return the field width, greater than zero
     */
    int length();

    /**
     * Number of elements of a {@code List} or array field. Required on those types and not allowed
     * on others. The field occupies {@code length * occurs} characters, and the list or array must
     * have exactly {@code occurs} elements when written.
     *
     * <p>The other attributes ({@code precision}, {@code dateTimeFormat}, {@code padding}) apply to
     * each element.
     *
     * @return the number of elements, or {@code 0} for a field that is not repeated
     */
    int occurs() default 0;

    /**
     * Number of decimal places of a {@code BigDecimal} field. Not allowed on other types. With the
     * {@code useDecimalSeparator} option, {@link #length()} must be at least {@code precision + 2}.
     *
     * @return the number of decimal places, smaller than {@link #length()}
     */
    int precision() default 0;

    /**
     * {@link java.time.format.DateTimeFormatter} pattern of a {@code LocalDate} or {@code
     * LocalDateTime} field. Required for those types and not allowed on others. The pattern must
     * produce output of exactly {@link #length()} characters for every date and keep the century,
     * so {@code yy} is rejected.
     *
     * @return the date/time pattern
     */
    String dateTimeFormat() default "";

    /**
     * Character used to fill a {@code String} field on the right. Not allowed on other types:
     * numeric fields are always zero-filled on the left.
     *
     * @return the padding character
     */
    char padding() default ' ';
}
