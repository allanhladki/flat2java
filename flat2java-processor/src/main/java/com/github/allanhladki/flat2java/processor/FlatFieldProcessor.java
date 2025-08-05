package com.github.allanhladki.flat2java.processor;

import com.github.allanhladki.flat2java.markers.FlatField;
import com.github.allanhladki.flat2java.metadata.FieldMetadataExtractor;
import com.github.allanhladki.flat2java.serializer.SerializationTypes;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * Annotation processor for validating @FlatField annotations at compile time.
 * 
 * <p>This processor validates that fields annotated with @FlatField have:
 * <ul>
 *   <li>Supported field types</li>
 *   <li>Valid length and precision values</li>
 *   <li>Valid date/time format patterns that match the specified length</li>
 * </ul>
 * 
 * <p>The processor uses the core validation logic from FieldMetadataExtractor
 * and SerializationTypes to ensure consistency between compile-time and runtime validation.
 */
@SupportedAnnotationTypes("com.github.allanhladki.flat2java.markers.FlatField")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class FlatFieldProcessor extends AbstractProcessor {

    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false; // No further processing needed
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(FlatField.class)) {
            if (element.getKind() == ElementKind.FIELD) {
                validateFlatField(element);
            } else {
                // Report error for non-field elements annotated with @FlatField
                printErrorMessage("@FlatField annotation can only be applied to fields, got: " + element.getKind(), element);
            }
        }
        return false; // Don't claim the annotation
    }

    /**
     * Validates a field annotated with @FlatField.
     * 
     * @param element the field element to validate
     */
    private void validateFlatField(Element element) {
        FlatField annotation = element.getAnnotation(FlatField.class);
        String fieldName = element.getSimpleName().toString();
        
        try {
            // Validate in order: type, length/precision, date/time format
            validateFieldType(element, fieldName);
            FieldMetadataExtractor.validateLengthAndPrecision(annotation.length(), annotation.precision(), fieldName);
            validateDateTimeFormat(annotation, element, fieldName);
            
        } catch (IllegalArgumentException e) {
            printErrorMessage(e.getMessage(), element);
        } catch (Exception e) {
            // Catch any unexpected errors and report them
            printErrorMessage("Unexpected error validating field '" + fieldName + "': " + e.getMessage(), element);
        }
    }

    /**
     * Validates the field type using annotation processing API.
     * 
     * @param element the field element to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if the field type is not supported
     */
    private void validateFieldType(Element element, String fieldName) {
        if (element.getKind() != ElementKind.FIELD) {
            throw new IllegalArgumentException("Element must be a field, got: " + element.getKind());
        }
        
        VariableElement variableElement = (VariableElement) element;
        TypeMirror typeMirror = variableElement.asType();
        String typeName = typeMirror.toString();
        
        // Try to get the Class<?> from the TypeMirror using SerializationTypes
        Class<?> fieldType = SerializationTypes.getClassFromTypeName(typeName);
        if (fieldType != null) {
            // Use the core validation method with Class<?>
            FieldMetadataExtractor.validateFieldType(fieldType, fieldName);
        } else {
            // Fallback: validate using type name from SerializationTypes
            if (!SerializationTypes.isSupportedTypeName(typeName)) {
                throw new IllegalArgumentException(
                        String.format("Field '%s': unsupported field type '%s'. %s", 
                                     fieldName, typeName, SerializationTypes.getSupportedTypesDescription()));
            }
        }
    }

    /**
     * Validates date/time format using annotation processing API.
     * Prioritizes using the FlatField annotation information.
     * 
     * @param annotation the FlatField annotation
     * @param element the field element to validate
     * @param fieldName the name of the field for error messages
     */
    private void validateDateTimeFormat(FlatField annotation, Element element, String fieldName) {
        String dateTimeFormat = annotation.dateTimeFormat();
        if (dateTimeFormat == null || dateTimeFormat.isEmpty()) {
            return; // No format specified, skip validation
        }

        VariableElement variableElement = (VariableElement) element;
        TypeMirror typeMirror = variableElement.asType();
        String typeName = typeMirror.toString();
        
        // Try to get the Class<?> from the TypeMirror using SerializationTypes
        Class<?> fieldType = SerializationTypes.getClassFromTypeName(typeName);
        if (fieldType != null) {
            // Use the core validation method with Class<?>
            FieldMetadataExtractor.validateDateTimeFormatLength(dateTimeFormat, fieldType, annotation.length(), fieldName);
        } else {
            // Fallback: validate using type name (only for date/time types)
            validateDateTimeFormatByName(dateTimeFormat, typeName, annotation.length(), fieldName);
        }
    }

    /**
     * Fallback validation for date/time format using type name.
     * This method is only used when Class<?> cannot be determined from TypeMirror.
     * 
     * @param dateTimeFormat the date/time format pattern
     * @param typeName the fully qualified type name
     * @param length the field length
     * @param fieldName the field name for error messages
     * @throws IllegalArgumentException if validation fails
     */
    private void validateDateTimeFormatByName(String dateTimeFormat, String typeName, int length, String fieldName) {
        // Only validate if the type name indicates a date/time type
        if (typeName.equals(SerializationTypes.SupportedTypes.LOCAL_DATE.getName()) || 
            typeName.equals(SerializationTypes.SupportedTypes.LOCAL_DATE_TIME.getName())) {
            try {
                // Create a sample date to test the format
                String sampleDate;
                if (typeName.equals(SerializationTypes.SupportedTypes.LOCAL_DATE.getName())) {
                    sampleDate = java.time.LocalDate.of(2024, 1, 1).format(java.time.format.DateTimeFormatter.ofPattern(dateTimeFormat));
                } else {
                    sampleDate = java.time.LocalDateTime.of(2024, 1, 1, 12, 0, 0).format(java.time.format.DateTimeFormatter.ofPattern(dateTimeFormat));
                }

                if (sampleDate.length() != length) {
                    throw new IllegalArgumentException(
                            String.format("Field '%s': dateTimeFormat '%s' produces %d characters, but length is %d", 
                                         fieldName, dateTimeFormat, sampleDate.length(), length));
                }
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        String.format("Field '%s': invalid dateTimeFormat '%s': %s", 
                                     fieldName, dateTimeFormat, e.getMessage()), e);
            }
        }
    }

    /**
     * Prints an error message to the messager.
     * 
     * @param message the error message
     * @param element the element where the error occurred
     */
    private void printErrorMessage(String message, Element element) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }
}
