package tools.dynamia.modules.functions;

import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

/**
 * Represents the outcome of a {@link DynamiaHttpFunction} execution. A result is either a structured
 * JSON-like payload ({@link #getData()}) or raw binary content ({@link #getBinaryData()}) with an
 * associated content type, matching the response model described in the extension's README.
 *
 * @author Mario A. Serrano Leones
 */
public class FunctionResult {

    private static final ObjectMapper MAPPER = new ObjectMapper();

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

    /**
     * Indicates whether {@link #getData()} holds a structured JSON payload (object or array) that can be
     * read with {@link #toJson()} or {@link #toJson(Class)}.
     *
     * @return {@code true} when the result is not binary and carries a JSON object/array payload
     */
    public boolean isJson() {
        return !isBinary() && (data instanceof Map || data instanceof List);
    }

    /**
     * Returns {@link #getData()} as a {@code Map}, parsing it first when it was kept as a raw JSON
     * string (e.g. because the target endpoint returned malformed JSON).
     *
     * @return the payload as a {@link Map}, or an empty map when there is no data
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJson() {
        return convert(Map.class);
    }

    /**
     * Converts {@link #getData()} into the given DTO class using Jackson, so callers don't have to deal
     * with raw {@code Map}/{@code String} payloads.
     *
     * @param type the target class to parse/convert the payload into
     * @param <T>  the target type
     * @return the payload converted to {@code type}, or {@code null} when there is no data
     *
     * Example:
     * <pre>{@code
     * FunctionResult result = DynamiaFunctions.call("WhatsApp.sendMessage", params);
     * SendMessageResponse response = result.toJson(SendMessageResponse.class);
     * }</pre>
     */
    public <T> T toJson(Class<T> type) {
        return convert(type);
    }

    @SuppressWarnings("unchecked")
    private <T> T convert(Class<T> type) {
        if (data == null) {
            return type == Map.class ? (T) Map.of() : null;
        }
        if (type.isInstance(data)) {
            return type.cast(data);
        }
        if (data instanceof String text) {
            return MAPPER.readValue(text, type);
        }
        return MAPPER.convertValue(data, type);
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