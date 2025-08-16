package com.github.allanhladki.flat2java.exception;

/**
 * Exception thrown when deserialization operations fail. This includes errors during
 * string-to-object conversion and parsing.
 */
public class DeserializationException extends Flat2JavaException {

    /**
     * Constructs a new DeserializationException with the specified detail message.
     *
     * @param message the detail message
     */
    public DeserializationException(String message) {
        super(message);
    }

    /**
     * Constructs a new DeserializationException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public DeserializationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new DeserializationException with the specified cause.
     *
     * @param cause the cause
     */
    public DeserializationException(Throwable cause) {
        super(cause);
    }
}
