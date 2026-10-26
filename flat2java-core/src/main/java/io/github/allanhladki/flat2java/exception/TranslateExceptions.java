package io.github.allanhladki.flat2java.exception;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that should have their exceptions translated to the project's custom
 * exceptions. This annotation can be used to automatically translate standard Java exceptions to
 * appropriate Flat2Java exceptions without cluttering the method code.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * @TranslateExceptions
 * public String serialize(String value) {
 *     if (value == null) {
 *         throw new NullPointerException("Value cannot be null");
 *     }
 *     return value;
 * }
 * }</pre>
 *
 * <p>The annotation processor will automatically wrap the method execution and translate any thrown
 * exceptions to appropriate Flat2Java exceptions.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TranslateExceptions {

    /**
     * The context to use for exception translation. This will be included in the error message to
     * provide better context about where the error occurred.
     *
     * @return the context string, defaults to the method name
     */
    String context() default "";

    /**
     * The type of operation being performed. This determines which type of exception should be
     * thrown for unknown exception types.
     *
     * @return the operation type
     */
    OperationType operationType();

    /** Enumeration of operation types to help with exception translation. */
    enum OperationType {
        /**
         * Explicitly mark as a validation operation. All exceptions will be translated to
         * ValidationException unless they are already Flat2Java exceptions.
         */
        VALIDATION,

        /**
         * Explicitly mark as a serialization operation. All exceptions will be translated to
         * SerializationException unless they are already Flat2Java exceptions.
         */
        SERIALIZATION,

        /**
         * Explicitly mark as a deserialization operation. All exceptions will be translated to
         * DeserializationException unless they are already Flat2Java exceptions.
         */
        DESERIALIZATION
    }
}
