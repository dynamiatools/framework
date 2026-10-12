package tools.dynamia.actions.replay;

/**
 * Thrown by the code that handles an answer when the answer cannot be accepted and the same question must be asked again:
 * a form whose submission failed validation, a choice whose option no longer exists, a form the action did not close.
 * {@link ReplaySession#interact} rolls the pass back, drops the answer and emits the same step with the error, so the user
 * answers again with what was sent still in front of them.
 */
public class ReplayRetry extends RuntimeException {

    private final String field;

    /**
     * @param message what was wrong, shown to the user; {@code null} to show the question again with no message
     */
    public ReplayRetry(String message) {
        this(message, null);
    }

    /**
     * @param message what was wrong, shown to the user
     * @param field   name of the field that failed, or {@code null} when it is not about one field
     */
    public ReplayRetry(String message, String field) {
        super(message, null, false, false);
        this.field = field;
    }

    /** @return the field that failed, or null */
    public String getField() {
        return field;
    }
}
