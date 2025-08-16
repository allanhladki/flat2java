package com.github.allanhladki.flat2java.exception;

/**
 * Exception thrown when validation operations fail. This includes field validation, configuration
 * validation, and metadata validation errors.
 */
public class ValidationException extends Flat2JavaException {

    /**
     * Constructs a new ValidationException with the specified detail message.
     *
     * @param message the detail message
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Constructs a new ValidationException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new ValidationException with the specified cause.
     *
     * @param cause the cause
     */
    public ValidationException(Throwable cause) {
        super(cause);
    }
}
