package io.github.allanhladki.flat2java.processor;

import io.github.allanhladki.flat2java.annotation.FlatField;
import io.github.allanhladki.flat2java.exception.MappingException;
import io.github.allanhladki.flat2java.internal.FieldKind;
import io.github.allanhladki.flat2java.internal.MappingRules;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    /** Creates the processor. Called by the compiler through the service loader. */
    public FlatFieldProcessor() {}

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
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
        TypeMirror type = repeated ? elementType(label, declared) : declared;
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

    /** The element type of an array or list field, rejecting nested repetition and wildcards. */
    private static TypeMirror elementType(String label, TypeMirror type) {
        TypeMirror element;
        if (type.getKind() == TypeKind.ARRAY) {
            element = ((ArrayType) type).getComponentType();
        } else {
            List<? extends TypeMirror> arguments = ((DeclaredType) type).getTypeArguments();
            if (arguments.isEmpty()) {
                throw MappingRules.unsupportedType(
                        label, type.toString(), "declare the element type, as in List<String>");
            }
            element = arguments.get(0);
            if (element.getKind() == TypeKind.WILDCARD) {
                throw MappingRules.unsupportedType(
                        label, type.toString(), "declare the element type without a wildcard");
            }
        }
        if (isRepeated(element)) {
            throw MappingRules.unsupportedType(
                    label,
                    type.toString(),
                    "the elements of a List or array cannot be Lists or arrays");
        }
        return element;
    }

    /** Characters taken by a field: its length, times occurs for a list or array. */
    private static int width(VariableElement field) {
        FlatField a = field.getAnnotation(FlatField.class);
        return a.occurs() > 0 ? a.length() * a.occurs() : a.length();
    }

    private TypeElement nestableType(String label, TypeMirror type) {
        String name = type.toString();
        if (type.getKind() == TypeKind.TYPEVAR) {
            throw MappingRules.unsupportedType(
                    label, name, "type variables cannot be instantiated when reading");
        }
        if (type.getKind() != TypeKind.DECLARED) {
            throw MappingRules.unsupportedType(label, name, null);
        }
        TypeElement element = (TypeElement) ((DeclaredType) type).asElement();
        name = element.getQualifiedName().toString();
        if (name.equals("java.lang.Object")) {
            throw MappingRules.unsupportedType(
                    label, name, "declare the concrete class of the nested record");
        }
        if (element.getKind() == ElementKind.ENUM) {
            throw MappingRules.unsupportedType(label, name, null);
        }
        if (element.getKind() == ElementKind.INTERFACE
                || element.getModifiers().contains(Modifier.ABSTRACT)) {
            throw MappingRules.unsupportedType(
                    label, name, "abstract types cannot be instantiated when reading");
        }
        if (annotatedFields(element).isEmpty()) {
            throw MappingRules.unsupportedType(label, name, null);
        }
        if (element.getKind() != ElementKind.RECORD && !hasNoArgConstructor(element)) {
            String hint =
                    element.getNestingKind().isNested()
                                    && !element.getModifiers().contains(Modifier.STATIC)
                            ? " (inner classes must be declared static)"
                            : "";
            throw new MappingException(
                    element.getSimpleName() + ": a no-arg constructor is required" + hint);
        }
        return element;
    }

    private void checkNoCycle(TypeElement nested, Deque<TypeElement> path) {
        MappingRules.checkNoCycle(
                path.stream().map(t -> t.getSimpleName().toString()).collect(Collectors.toList()),
                nested.getSimpleName().toString(),
                path.contains(nested));
        path.addLast(nested);
        for (VariableElement field : annotatedFields(nested)) {
            TypeMirror type = field.asType();
            if (type.getKind() == TypeKind.ARRAY) {
                type = ((ArrayType) type).getComponentType();
            } else if (isRepeated(type) && !((DeclaredType) type).getTypeArguments().isEmpty()) {
                type = ((DeclaredType) type).getTypeArguments().get(0);
            }
            if (type.getKind() == TypeKind.DECLARED
                    && FieldKind.ofTypeName(typeName(type)).isEmpty()) {
                checkNoCycle((TypeElement) ((DeclaredType) type).asElement(), path);
            }
        }
        path.removeLast();
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
                        .collect(Collectors.toList()));
    }

    /** Annotated, non-static fields of the hierarchy, superclass first. */
    private List<VariableElement> annotatedFields(TypeElement type) {
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
        if (type.getKind().isPrimitive()) {
            return type.getKind().name().toLowerCase(java.util.Locale.ROOT);
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
