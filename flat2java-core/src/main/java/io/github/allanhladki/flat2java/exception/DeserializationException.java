package io.github.allanhladki.flat2java.exception;

/** Thrown when a flat record cannot be read into an object. */
public class DeserializationException extends Flat2JavaException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with a message.
     *
     * @param message the detail message
     */
    public DeserializationException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a message and a cause.
     *
     * @param message the detail message
     * @param cause the underlying cause
     */
    public DeserializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
