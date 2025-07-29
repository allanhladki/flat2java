package com.github.allanhladki.flat2java.processor;

import com.github.allanhladki.flat2java.markers.FlatField;
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
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes("com.github.allanhladki.flat2java.markers.FlatField")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class FlatFieldProcessor extends AbstractProcessor {

    private Messager messager;
    private Element element;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(FlatField.class)) {
            if (element.getKind() == ElementKind.FIELD) {
                validateFlatField(element);
            }
        }
        return false;
    }

    /** Validates a field annotated with @FlatField. */
    private void validateFlatField(Element element) {
        this.element = element;
        validateFlatFieldLength();
    }

    /*
     * Validates the length of the field annotated with @FlatField.
     */
    private void validateFlatFieldLength() {
        FlatField flatField = element.getAnnotation(FlatField.class);
        int length = flatField.length();
        if (length <= 0) {
            printErrorMessage("@FlatField 'length' must be a positive integer, current: " + length);
        }
    }

    /*
     * Prints an error message to the messager.
     */
    private void printErrorMessage(String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, this.element);
    }
}
