package io.github.allanhladki.flat2java.exception;

/** Thrown when an object cannot be written as a flat record. */
public class SerializationException extends Flat2JavaException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with a message.
     *
     * @param message the detail message
     */
    public SerializationException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a message and a cause.
     *
     * @param message the detail message
     * @param cause the underlying cause
     */
    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
