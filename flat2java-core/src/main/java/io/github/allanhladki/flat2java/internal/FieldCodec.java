package io.github.allanhladki.flat2java.internal;

/** Converts the value of one field to and from its fixed-width column. */
interface FieldCodec {

    /**
     * Appends exactly {@link #length()} characters representing {@code value}.
     *
     * @param value the field value, possibly {@code null}
     * @param out the record being written
     * @throws io.github.allanhladki.flat2java.exception.SerializationException if the value cannot
     *     be represented
     */
    void encode(Object value, StringBuilder out);

    /**
     * Reads the column that starts at {@code start}.
     *
     * @param line the whole record
     * @param start zero-based index of the first character of the column
     * @return the field value
     * @throws io.github.allanhladki.flat2java.exception.DeserializationException if the column does
     *     not hold a valid value
     */
    Object decode(String line, int start);

    /**
     * @return the column width
     */
    int length();
}
