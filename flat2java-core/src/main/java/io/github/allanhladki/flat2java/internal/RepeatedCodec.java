package io.github.allanhladki.flat2java.internal;

import io.github.allanhladki.flat2java.exception.SerializationException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@code List} or array field with a fixed number of elements, written one after the other with
 * the element codec. Lists are read as mutable {@link ArrayList}s.
 */
final class RepeatedCodec implements FieldCodec {

    private final String label;
    private final FieldCodec element;
    private final int occurs;
    private final Class<?> arrayComponent;

    /**
     * @param arrayComponent the component type of an array field, or {@code null} for a list
     */
    RepeatedCodec(String label, FieldCodec element, int occurs, Class<?> arrayComponent) {
        this.label = label;
        this.element = element;
        this.occurs = occurs;
        this.arrayComponent = arrayComponent;
    }

    @Override
    public void encode(Object value, StringBuilder out) {
        if (value == null) {
            throw new SerializationException(
                    label + ": null is not allowed on List and array fields");
        }
        if (arrayComponent != null) {
            checkSize(Array.getLength(value));
            for (int i = 0; i < occurs; i++) {
                element.encode(Array.get(value, i), out);
            }
        } else {
            List<?> list = (List<?>) value;
            checkSize(list.size());
            for (Object item : list) {
                element.encode(item, out);
            }
        }
    }

    @Override
    public Object decode(String line, int start) {
        int width = element.length();
        if (arrayComponent != null) {
            Object array = Array.newInstance(arrayComponent, occurs);
            for (int i = 0; i < occurs; i++) {
                Array.set(array, i, element.decode(line, start + i * width));
            }
            return array;
        }
        List<Object> list = new ArrayList<>(occurs);
        for (int i = 0; i < occurs; i++) {
            list.add(element.decode(line, start + i * width));
        }
        return list;
    }

    @Override
    public int length() {
        return element.length() * occurs;
    }

    private void checkSize(int size) {
        if (size != occurs) {
            throw new SerializationException(
                    label + ": has " + size + " elements, but occurs is " + occurs);
        }
    }
}
