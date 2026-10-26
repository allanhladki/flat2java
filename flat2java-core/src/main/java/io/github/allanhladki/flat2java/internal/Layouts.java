package io.github.allanhladki.flat2java.internal;

import io.github.allanhladki.flat2java.Flat2JavaOptions;
import io.github.allanhladki.flat2java.annotation.FlatField;
import io.github.allanhladki.flat2java.exception.MappingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Builds and caches the {@link RecordLayout} of each mapped class for one set of options.
 * Thread-safe.
 */
public final class Layouts {

    private static final Logger LOG = LoggerFactory.getLogger(Layouts.class);

    private final Flat2JavaOptions options;
    private final Map<Class<?>, RecordLayout> cache = new ConcurrentHashMap<>();

    /**
     * @param options the options the codecs are built with
     */
    public Layouts(Flat2JavaOptions options) {
        this.options = options;
    }

    /**
     * Returns the layout of {@code type}, building and validating it on first use.
     *
     * @param type the mapped class
     * @return the layout
     * @throws MappingException if the class cannot be mapped
     */
    public RecordLayout of(Class<?> type) {
        return of(type, new ArrayDeque<>());
    }

    private RecordLayout of(Class<?> type, Deque<Class<?>> path) {
        RecordLayout layout = cache.get(type);
        if (layout != null) {
            return layout;
        }
        // Built outside computeIfAbsent: nested types recurse into this method.
        layout = build(type, path);
        RecordLayout previous = cache.putIfAbsent(type, layout);
        return previous != null ? previous : layout;
    }

    private RecordLayout build(Class<?> type, Deque<Class<?>> path) {
        String typeName = type.getSimpleName();
        if (type.isRecord()) {
            throw new MappingException(
                    typeName
                            + ": Java records are not supported; use a class with a no-arg"
                            + " constructor");
        }
        if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
            throw new MappingException(typeName + ": a mapped type must be a concrete class");
        }

        List<Field> fields = annotatedFields(type);
        if (fields.isEmpty()) {
            throw new MappingException(typeName + ": no fields annotated with @FlatField");
        }
        MappingRules.checkPositions(
                typeName,
                fields.stream()
                        .map(f -> new MappingRules.Positioned(label(f), position(f)))
                        .collect(Collectors.toList()));
        if (position(fields.get(0)) != 0) {
            fields.sort(Comparator.comparingInt(Layouts::position));
        }

        path.addLast(type);
        List<RecordLayout.Column> columns = new ArrayList<>(fields.size());
        for (Field field : fields) {
            columns.add(new RecordLayout.Column(field, label(field), codec(field, path)));
            field.setAccessible(true);
        }
        path.removeLast();

        RecordLayout layout = new RecordLayout(type, constructor(type), columns);
        LOG.debug(
                "Mapped {} to a record of {} characters ({} fields)",
                type.getName(),
                layout.length(),
                layout.fieldCount());
        return layout;
    }

    private FieldCodec codec(Field field, Deque<Class<?>> path) {
        FlatField a = field.getAnnotation(FlatField.class);
        String label = label(field);
        Optional<FieldKind> kind = FieldKind.ofTypeName(field.getType().getName());
        if (kind.isPresent()) {
            MappingRules.checkScalar(
                    label, kind.get(), a.length(), a.precision(), a.dateTimeFormat(), a.padding());
            return scalarCodec(label, kind.get(), a);
        }

        Class<?> nestedType = field.getType();
        checkNestable(field, label, nestedType);
        MappingRules.checkNoCycle(
                path.stream().map(Class::getSimpleName).collect(Collectors.toList()),
                nestedType.getSimpleName(),
                path.contains(nestedType));
        RecordLayout nested = of(nestedType, path);
        MappingRules.checkNested(
                label,
                nestedType.getSimpleName(),
                a.length(),
                nested.length(),
                a.precision(),
                a.dateTimeFormat(),
                a.padding());
        return new NestedCodec(label, nested);
    }

    private FieldCodec scalarCodec(String label, FieldKind kind, FlatField a) {
        switch (kind) {
            case STRING:
                char nullFill =
                        a.padding() == MappingRules.DEFAULT_PADDING
                                ? options.nullValuePadding()
                                : a.padding();
                return new StringCodec(
                        label,
                        a.length(),
                        a.padding(),
                        nullFill,
                        options.allowNullStrings(),
                        options.failOnStringTruncation());
            case BIG_DECIMAL:
                return new DecimalCodec(
                        label,
                        a.length(),
                        a.precision(),
                        options.roundingMode(),
                        options.useDecimalSeparator(),
                        options.decimalSeparator());
            case LOCAL_DATE:
            case LOCAL_DATE_TIME:
                return new TemporalCodec(label, a.length(), kind, a.dateTimeFormat());
            default:
                return new WholeNumberCodec(label, a.length(), kind);
        }
    }

    private static void checkNestable(Field field, String label, Class<?> type) {
        String typeName = field.getGenericType().getTypeName();
        if (field.getGenericType() instanceof TypeVariable) {
            throw MappingRules.unsupportedType(
                    label, typeName, "type variables cannot be instantiated when reading");
        }
        if (type == Object.class) {
            throw MappingRules.unsupportedType(
                    label, typeName, "declare the concrete class of the nested record");
        }
        if (type.isPrimitive() || type.isArray() || type.isEnum()) {
            throw MappingRules.unsupportedType(label, typeName, null);
        }
        if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
            throw MappingRules.unsupportedType(
                    label, typeName, "abstract types cannot be instantiated when reading");
        }
        if (annotatedFields(type).isEmpty()) {
            throw MappingRules.unsupportedType(label, typeName, null);
        }
    }

    /** Annotated fields of the hierarchy, superclass first, in declaration order. */
    private static List<Field> annotatedFields(Class<?> type) {
        Deque<Class<?>> hierarchy = new ArrayDeque<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            hierarchy.addFirst(c);
        }
        List<Field> fields = new ArrayList<>();
        for (Class<?> c : hierarchy) {
            for (Field field : c.getDeclaredFields()) {
                if (field.isAnnotationPresent(FlatField.class)) {
                    MappingRules.checkNotStatic(
                            label(field), Modifier.isStatic(field.getModifiers()));
                    fields.add(field);
                }
            }
        }
        return fields;
    }

    private static Constructor<?> constructor(Class<?> type) {
        try {
            Constructor<?> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException e) {
            String hint =
                    type.isMemberClass() && !Modifier.isStatic(type.getModifiers())
                            ? " (inner classes must be declared static)"
                            : "";
            throw new MappingException(
                    type.getSimpleName() + ": a no-arg constructor is required" + hint, e);
        } catch (RuntimeException e) {
            // InaccessibleObjectException when the class is in a module that is not open.
            throw new MappingException(
                    type.getSimpleName() + ": constructor is not accessible: " + e.getMessage(), e);
        }
    }

    private static String label(Field field) {
        return MappingRules.label(field.getDeclaringClass().getSimpleName(), field.getName());
    }

    private static int position(Field field) {
        return field.getAnnotation(FlatField.class).position();
    }
}
