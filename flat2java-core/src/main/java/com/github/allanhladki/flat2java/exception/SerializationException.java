package com.github.allanhladki.flat2java.exception;

/**
 * Exception thrown when serialization operations fail. This includes errors during object-to-string
 * conversion and formatting.
 */
public class SerializationException extends Flat2JavaException {

    /**
     * Constructs a new SerializationException with the specified detail message.
     *
     * @param message the detail message
     */
    public SerializationException(String message) {
        super(message);
    }

    /**
     * Constructs a new SerializationException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new SerializationException with the specified cause.
     *
     * @param cause the cause
     */
    public SerializationException(Throwable cause) {
        super(cause);
    }
}
