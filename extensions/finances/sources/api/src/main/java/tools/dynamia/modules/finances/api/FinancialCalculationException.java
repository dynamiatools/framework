package tools.dynamia.modules.finances.api;

/**
 * Exception thrown when a financial calculation fails.
 * This is a runtime exception to avoid cluttering method signatures.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * if (charge.getValue() == null) {
 *     throw new FinancialCalculationException("Charge value cannot be null");
 * }
 * }</pre>
 *
 * @author Dynamia Finance Framework
 * @since 1.0.0
 */
public class FinancialCalculationException extends RuntimeException {

    /**
     * Creates a new exception with the specified message.
     *
     * @param message the error message
     */
    public FinancialCalculationException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with the specified message and cause.
     *
     * @param message the error message
     * @param cause the cause of the exception
     */
    public FinancialCalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}
