package tools.dynamia.modules.email.api;

/**
 * A failure of the template editor API that maps to an HTTP status.
 */
public class EmailApiException extends RuntimeException {

    private final int status;
    private final String code;

    /**
     * @param status  the HTTP status
     * @param code    a short machine readable code
     * @param message what happened
     */
    public EmailApiException(int status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
