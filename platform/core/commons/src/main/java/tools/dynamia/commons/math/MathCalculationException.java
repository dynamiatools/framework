package tools.dynamia.commons.math;

/**
 * Basic math exception
 */
public class MathCalculationException extends RuntimeException {

    private static final long serialVersionUID = 6235428117353457356L;

    /**
     * CalculatorException.
     */
    public MathCalculationException() {
        super();
    }

    /**
     * CalculatorException.
     *
     * @param message the message
     */
    public MathCalculationException(final String message) {
        super(message);
    }
}