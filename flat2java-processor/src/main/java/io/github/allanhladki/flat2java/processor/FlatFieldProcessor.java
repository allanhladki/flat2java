package io.github.allanhladki.flat2java.processor;

import io.github.allanhladki.flat2java.annotation.FlatField;
import io.github.allanhladki.flat2java.exception.MappingException;
import io.github.allanhladki.flat2java.internal.FieldKind;
import io.github.allanhladki.flat2java.internal.MappingRules;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

/**
 * Reports invalid {@code @FlatField} declarations as compilation errors. It applies the same rules,
 * with the same messages, as the runtime validation in {@code
 * io.github.allanhladki.flat2java.internal.MappingRules}.
 */
@SupportedAnnotationTypes("io.github.allanhladki.flat2java.annotation.FlatField")
public class FlatFieldProcessor extends AbstractProcessor {

    // Per round: the annotated fields of each type, and the types known to have no cycle.
    private final Map<TypeElement, List<VariableElement>> fieldsByType = new HashMap<>();
    private final Set<TypeElement> acyclicTypes = new HashSet<>();

    /** Creates the processor. Called by the compiler through the service loader. */
    public FlatFieldProcessor() {}

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        fieldsByType.clear();
        acyclicTypes.clear();
        Set<TypeElement> mappedTypes = new LinkedHashSet<>();
        for (VariableElement field :
                ElementFilter.fieldsIn(roundEnv.getElementsAnnotatedWith(FlatField.class))) {
            report(field, () -> validateField(field));
            mappedTypes.add((TypeElement) field.getEnclosingElement());
        }
        for (TypeElement type : mappedTypes) {
            report(type, () -> validatePositions(type));
        }
        return false;
    }

    private void validateField(VariableElement field) {
        FlatField a = field.getAnnotation(FlatField.class);
        String label = label(field);
        MappingRules.checkNotStatic(label, field.getModifiers().contains(Modifier.STATIC));

        TypeMirror declared = field.asType();
        boolean repeated = isRepeated(declared);
        MappingRules.checkOccurs(label, repeated, a.length(), a.occurs());
        TypeMirror type = valueType(declared);
        if (repeated) {
            MappingRules.checkElementType(
                    label,
                    declared.toString(),
                    type == null,
                    type != null && type.getKind() == TypeKind.WILDCARD,
                    type != null && isRepeated(type));
        }
        Optional<FieldKind> kind = FieldKind.ofTypeName(typeName(type));
        if (kind.isPresent()) {
            MappingRules.checkScalar(
                    label, kind.get(), a.length(), a.precision(), a.dateTimeFormat(), a.padding());
            return;
        }

        TypeElement nested = nestableType(label, type);
        Deque<TypeElement> path = new ArrayDeque<>();
        path.add((TypeElement) field.getEnclosingElement());
        checkNoCycle(nested, path);
        MappingRules.checkNested(
                label,
                nested.getSimpleName().toString(),
                a.length(),
                annotatedFields(nested).stream().mapToInt(FlatFieldProcessor::width).sum(),
                a.precision(),
                a.dateTimeFormat(),
                a.padding());
    }

    /** Whether the type is an array or a {@code java.util.List}. */
    private static boolean isRepeated(TypeMirror type) {
        return type.getKind() == TypeKind.ARRAY || typeName(type).equals("java.util.List");
    }

    /**
     * The type of one value of a field: the element type of an array or list, which is {@code null}
     * for a raw list, or the declared type otherwise.
     */
    private static TypeMirror valueType(TypeMirror type) {
        if (type instanceof ArrayType array) {
            return array.getComponentType();
        }
        if (isRepeated(type)) {
            List<? extends TypeMirror> arguments = ((DeclaredType) type).getTypeArguments();
            return arguments.isEmpty() ? null : arguments.get(0);
        }
        return type;
    }

    private static int width(VariableElement field) {
        FlatField a = field.getAnnotation(FlatField.class);
        return MappingRules.width(a.length(), a.occurs());
    }

    private TypeElement nestableType(String label, TypeMirror type) {
        TypeElement element =
                type.getKind() == TypeKind.DECLARED
                        ? (TypeElement) ((DeclaredType) type).asElement()
                        : null;
        String name = element != null ? element.getQualifiedName().toString() : type.toString();
        MappingRules.checkNestable(label, name, shape(type, element));
        if (element.getKind() != ElementKind.RECORD && !hasNoArgConstructor(element)) {
            throw MappingRules.noArgConstructorRequired(
                    element.getSimpleName().toString(),
                    element.getNestingKind().isNested()
                            && !element.getModifiers().contains(Modifier.STATIC));
        }
        return element;
    }

    private MappingRules.TypeShape shape(TypeMirror type, TypeElement element) {
        if (type.getKind() == TypeKind.TYPEVAR) {
            return MappingRules.TypeShape.TYPE_VARIABLE;
        }
        if (element == null || element.getKind() == ElementKind.ENUM) {
            return MappingRules.TypeShape.NOT_A_CLASS;
        }
        if (element.getQualifiedName().contentEquals("java.lang.Object")) {
            return MappingRules.TypeShape.OBJECT;
        }
        if (element.getKind() == ElementKind.INTERFACE
                || element.getModifiers().contains(Modifier.ABSTRACT)) {
            return MappingRules.TypeShape.ABSTRACT;
        }
        return annotatedFields(element).isEmpty()
                ? MappingRules.TypeShape.NO_ANNOTATED_FIELDS
                : MappingRules.TypeShape.NESTABLE;
    }

    /**
     * Walks the nested types reachable from {@code nested}. Types already walked without finding a
     * cycle are skipped, so shared nested types are visited once.
     */
    private void checkNoCycle(TypeElement nested, Deque<TypeElement> path) {
        boolean onPath = path.contains(nested);
        if (!onPath && acyclicTypes.contains(nested)) {
            return;
        }
        MappingRules.checkNoCycle(
                path.stream().map(t -> t.getSimpleName().toString()).toList(),
                nested.getSimpleName().toString(),
                onPath);
        path.addLast(nested);
        for (VariableElement field : annotatedFields(nested)) {
            TypeMirror type = valueType(field.asType());
            if (type != null
                    && type.getKind() == TypeKind.DECLARED
                    && FieldKind.ofTypeName(typeName(type)).isEmpty()) {
                checkNoCycle((TypeElement) ((DeclaredType) type).asElement(), path);
            }
        }
        path.removeLast();
        acyclicTypes.add(nested);
    }

    private void validatePositions(TypeElement type) {
        MappingRules.checkPositions(
                type.getSimpleName().toString(),
                annotatedFields(type).stream()
                        .map(
                                f ->
                                        new MappingRules.Positioned(
                                                label(f),
                                                f.getAnnotation(FlatField.class).position()))
                        .toList());
    }

    /** Annotated, non-static fields of the hierarchy, superclass first. */
    private List<VariableElement> annotatedFields(TypeElement type) {
        return fieldsByType.computeIfAbsent(type, this::collectAnnotatedFields);
    }

    private List<VariableElement> collectAnnotatedFields(TypeElement type) {
        Deque<TypeElement> hierarchy = new ArrayDeque<>();
        for (TypeElement t = type; t != null; t = superclass(t)) {
            hierarchy.addFirst(t);
        }
        List<VariableElement> fields = new ArrayList<>();
        for (TypeElement t : hierarchy) {
            for (VariableElement field : ElementFilter.fieldsIn(t.getEnclosedElements())) {
                if (field.getAnnotation(FlatField.class) != null
                        && !field.getModifiers().contains(Modifier.STATIC)) {
                    fields.add(field);
                }
            }
        }
        return fields;
    }

    private TypeElement superclass(TypeElement type) {
        TypeMirror superclass = type.getSuperclass();
        if (superclass.getKind() != TypeKind.DECLARED) {
            return null;
        }
        TypeElement element = (TypeElement) ((DeclaredType) superclass).asElement();
        return element.getQualifiedName().contentEquals("java.lang.Object") ? null : element;
    }

    private static boolean hasNoArgConstructor(TypeElement type) {
        List<ExecutableElement> constructors =
                ElementFilter.constructorsIn(type.getEnclosedElements());
        return constructors.isEmpty()
                || constructors.stream().anyMatch(c -> c.getParameters().isEmpty());
    }

    /** Binary-compatible name used by {@link FieldKind}: {@code int}, {@code java.lang.String}. */
    private static String typeName(TypeMirror type) {
        if (type == null) {
            return "";
        }
        if (type.getKind().isPrimitive()) {
            return type.getKind().name().toLowerCase(Locale.ROOT);
        }
        if (type.getKind() == TypeKind.DECLARED) {
            return ((TypeElement) ((DeclaredType) type).asElement()).getQualifiedName().toString();
        }
        return type.toString();
    }

    private static String label(VariableElement field) {
        return MappingRules.label(
                field.getEnclosingElement().getSimpleName().toString(),
                field.getSimpleName().toString());
    }

    private void report(Element element, Runnable validation) {
        try {
            validation.run();
        } catch (MappingException e) {
            processingEnv
                    .getMessager()
                    .printMessage(Diagnostic.Kind.ERROR, e.getMessage(), element);
        }
    }
}
