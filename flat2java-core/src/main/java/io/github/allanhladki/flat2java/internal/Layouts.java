package io.github.allanhladki.flat2java.internal;

import io.github.allanhladki.flat2java.Flat2JavaOptions;
import io.github.allanhladki.flat2java.annotation.FlatField;
import io.github.allanhladki.flat2java.exception.MappingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
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

        RecordLayout layout = new RecordLayout(type, instantiator(type, fields), columns);
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
        Class<?> type = field.getType();
        Type genericType = field.getGenericType();
        boolean repeated = type.isArray() || type == List.class;
        MappingRules.checkOccurs(label, repeated, a.length(), a.occurs());
        if (!repeated) {
            return elementCodec(label, type, genericType, a, path);
        }

        Class<?> elementType;
        Type genericElementType;
        if (type.isArray()) {
            elementType = type.getComponentType();
            genericElementType =
                    genericType instanceof GenericArrayType
                            ? ((GenericArrayType) genericType).getGenericComponentType()
                            : elementType;
        } else {
            genericElementType = listElementType(label, genericType);
            elementType = rawType(genericElementType);
        }
        if (elementType != null && (elementType.isArray() || elementType == List.class)) {
            throw MappingRules.unsupportedType(
                    label,
                    genericType.getTypeName(),
                    "the elements of a List or array cannot be Lists or arrays");
        }
        FieldCodec element = elementCodec(label, elementType, genericElementType, a, path);
        return new RepeatedCodec(label, element, a.occurs(), type.isArray() ? elementType : null);
    }

    /** The codec of a single value: a scalar or a nested record. */
    private FieldCodec elementCodec(
            String label, Class<?> type, Type genericType, FlatField a, Deque<Class<?>> path) {
        Optional<FieldKind> kind =
                type == null ? Optional.empty() : FieldKind.ofTypeName(type.getName());
        if (kind.isPresent()) {
            MappingRules.checkScalar(
                    label, kind.get(), a.length(), a.precision(), a.dateTimeFormat(), a.padding());
            return scalarCodec(label, kind.get(), a);
        }

        checkNestable(label, genericType, type);
        MappingRules.checkNoCycle(
                path.stream().map(Class::getSimpleName).collect(Collectors.toList()),
                type.getSimpleName(),
                path.contains(type));
        RecordLayout nested = of(type, path);
        MappingRules.checkNested(
                label,
                type.getSimpleName(),
                a.length(),
                nested.length(),
                a.precision(),
                a.dateTimeFormat(),
                a.padding());
        return new NestedCodec(label, nested);
    }

    private static Type listElementType(String label, Type listType) {
        if (!(listType instanceof ParameterizedType)) {
            throw MappingRules.unsupportedType(
                    label, listType.getTypeName(), "declare the element type, as in List<String>");
        }
        Type element = ((ParameterizedType) listType).getActualTypeArguments()[0];
        if (element instanceof WildcardType) {
            throw MappingRules.unsupportedType(
                    label, listType.getTypeName(), "declare the element type without a wildcard");
        }
        return element;
    }

    /** The class of a type, or {@code null} for a type variable. */
    private static Class<?> rawType(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        }
        if (type instanceof GenericArrayType) {
            return Object[].class;
        }
        return null;
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

    private static void checkNestable(String label, Type genericType, Class<?> type) {
        String typeName = genericType.getTypeName();
        if (genericType instanceof TypeVariable || type == null) {
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

    /**
     * Annotated fields of the hierarchy, superclass first, in declaration order. The fields of a
     * record follow the order of its components.
     */
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
        if (type.isRecord()) {
            List<String> components =
                    Arrays.stream(type.getRecordComponents())
                            .map(RecordComponent::getName)
                            .collect(Collectors.toList());
            fields.sort(Comparator.comparingInt(f -> components.indexOf(f.getName())));
        }
        return fields;
    }

    private static RecordLayout.Instantiator instantiator(Class<?> type, List<Field> columns) {
        if (!type.isRecord()) {
            return new RecordLayout.NoArgInstantiator(constructor(type));
        }
        RecordComponent[] components = type.getRecordComponents();
        Class<?>[] parameterTypes = new Class<?>[components.length];
        Object[] defaults = new Object[components.length];
        List<String> names = new ArrayList<>(components.length);
        for (int i = 0; i < components.length; i++) {
            parameterTypes[i] = components[i].getType();
            defaults[i] =
                    parameterTypes[i].isPrimitive()
                            ? Array.get(Array.newInstance(parameterTypes[i], 1), 0)
                            : null;
            names.add(components[i].getName());
        }
        int[] argumentOfColumn =
                columns.stream().mapToInt(f -> names.indexOf(f.getName())).toArray();
        try {
            Constructor<?> canonical = type.getDeclaredConstructor(parameterTypes);
            canonical.setAccessible(true);
            return new RecordLayout.CanonicalInstantiator(canonical, argumentOfColumn, defaults);
        } catch (NoSuchMethodException | RuntimeException e) {
            // NoSuchMethodException cannot happen for a record; RuntimeException covers
            // InaccessibleObjectException when the class is in a module that is not open.
            throw new MappingException(
                    type.getSimpleName()
                            + ": canonical constructor is not accessible: "
                            + e.getMessage(),
                    e);
        }
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
