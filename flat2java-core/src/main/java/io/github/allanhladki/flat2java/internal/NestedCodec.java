package io.github.allanhladki.flat2java.internal;

import io.github.allanhladki.flat2java.exception.SerializationException;

/** A field whose type is itself a mapped class; its columns are the nested record's columns. */
final class NestedCodec implements FieldCodec {

    private final String label;
    private final RecordLayout layout;

    NestedCodec(String label, RecordLayout layout) {
        this.label = label;
        this.layout = layout;
    }

    @Override
    public void encode(Object value, StringBuilder out) {
        if (value == null) {
            throw new SerializationException(
                    label + ": null is not allowed on nested record fields");
        }
        layout.encode(value, out);
    }

    @Override
    public Object decode(String line, int start) {
        return layout.decode(line, start);
    }

    @Override
    public int length() {
        return layout.length();
    }
}
