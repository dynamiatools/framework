package tools.dynamia.modules.finances.api;

/**
 * Exception thrown when attempting to perform an operation on a document
 * that is not in the appropriate state.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * if (document.isPosted()) {
 *     throw new InvalidDocumentStateException(
 *         "Cannot modify a POSTED document"
 *     );
 * }
 * }</pre>
 *
 * @author Dynamia Finance Framework
 * @since 26.1
 */
public class InvalidDocumentStateException extends RuntimeException {

    /**
     * Creates a new exception with the specified message.
     *
     * @param message the error message
     */
    public InvalidDocumentStateException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with the specified message and cause.
     *
     * @param message the error message
     * @param cause the cause of the exception
     */
    public InvalidDocumentStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
