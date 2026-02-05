package tools.dynamia.modules.finances.api;

/**
 * Exception thrown when attempting to perform operations between different currencies
 * without a proper exchange rate.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * if (!money1.getCurrencyCode().equals(money2.getCurrencyCode())) {
 *     throw new InvalidCurrencyOperationException(
 *         "Cannot add USD and EUR without exchange rate"
 *     );
 * }
 * }</pre>
 *
 * @author Dynamia Finance Framework
 * @since 1.0.0
 */
public class InvalidCurrencyOperationException extends RuntimeException {

    /**
     * Creates a new exception with the specified message.
     *
     * @param message the error message
     */
    public InvalidCurrencyOperationException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with the specified message and cause.
     *
     * @param message the error message
     * @param cause the cause of the exception
     */
    public InvalidCurrencyOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
