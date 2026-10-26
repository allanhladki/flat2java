package io.github.allanhladki.flat2java.exception;

/**
 * Thrown when a class cannot be mapped to a flat record because its {@code @FlatField} declarations
 * are invalid.
 */
public class MappingException extends Flat2JavaException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with a message.
     *
     * @param message the detail message
     */
    public MappingException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a message and a cause.
     *
     * @param message the detail message
     * @param cause the underlying cause
     */
    public MappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
