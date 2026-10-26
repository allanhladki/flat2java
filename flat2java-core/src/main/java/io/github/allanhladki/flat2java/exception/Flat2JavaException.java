package io.github.allanhladki.flat2java.exception;

/** Base class of every exception thrown by flat2java. */
public class Flat2JavaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with a message.
     *
     * @param message the detail message
     */
    public Flat2JavaException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a message and a cause.
     *
     * @param message the detail message
     * @param cause the underlying cause
     */
    public Flat2JavaException(String message, Throwable cause) {
        super(message, cause);
    }
}
