package tools.dynamia.modules.functions;

/**
 * Represents the outcome of a {@link DynamiaHttpFunction} execution. A result is either a structured
 * JSON-like payload ({@link #getData()}) or raw binary content ({@link #getBinaryData()}) with an
 * associated content type, matching the response model described in the extension's README.
 *
 * @author Mario A. Serrano Leones
 */
public class FunctionResult {

    private final boolean success;
    private final Object data;
    private final byte[] binaryData;
    private final String contentType;
    private final String errorMessage;

    private FunctionResult(boolean success, Object data, byte[] binaryData, String contentType, String errorMessage) {
        this.success = success;
        this.data = data;
        this.binaryData = binaryData;
        this.contentType = contentType;
        this.errorMessage = errorMessage;
    }

    /**
     * Creates a successful result with a structured (JSON-serializable) payload.
     *
     * @param data the response payload
     * @return a successful {@link FunctionResult}
     */
    public static FunctionResult success(Object data) {
        return new FunctionResult(true, data, null, "application/json", null);
    }

    /**
     * Creates a successful result carrying raw binary content (image, PDF, CSV, etc.).
     *
     * @param binaryData  the raw bytes to stream back to the caller
     * @param contentType the content type of the binary payload
     * @return a successful {@link FunctionResult}
     */
    public static FunctionResult binary(byte[] binaryData, String contentType) {
        return new FunctionResult(true, null, binaryData, contentType, null);
    }

    /**
     * Creates a failed result with an error message.
     *
     * @param errorMessage the error description
     * @return a failed {@link FunctionResult}
     */
    public static FunctionResult error(String errorMessage) {
        return new FunctionResult(false, null, null, null, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isBinary() {
        return binaryData != null;
    }

    public Object getData() {
        return data;
    }

    public byte[] getBinaryData() {
        return binaryData;
    }

    public String getContentType() {
        return contentType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}