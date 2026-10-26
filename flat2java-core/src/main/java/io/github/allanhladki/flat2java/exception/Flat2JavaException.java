package io.github.allanhladki.flat2java.exception;

/**
 * Base exception class for all flat2java library exceptions. This provides a common exception
 * hierarchy for the library.
 */
public class Flat2JavaException extends RuntimeException {

    /**
     * Constructs a new Flat2JavaException with the specified detail message.
     *
     * @param message the detail message
     */
    public Flat2JavaException(String message) {
        super(message);
    }

    /**
     * Constructs a new Flat2JavaException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public Flat2JavaException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new Flat2JavaException with the specified cause.
     *
     * @param cause the cause
     */
    public Flat2JavaException(Throwable cause) {
        super(cause);
    }
}
