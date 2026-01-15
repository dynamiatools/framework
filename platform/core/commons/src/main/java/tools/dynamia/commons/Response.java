package tools.dynamia.commons;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Collection;

/**
 * A generic response container for transferring data, status, and error information between objects or systems.
 * <p>
 * This class is commonly used to wrap responses in APIs, services, or inter-module communication, providing
 * a standardized way to convey result data, status, error messages, and validity.
 * <br>
 * Example usage:
 * <pre>
 * Response<String> response = new Response<>();
 * response.setStatus("OK");
 * response.setData("Hello World");
 * </pre>
 * </p>
 *
 * @param <T> the type of the response data
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> implements Serializable {

    /**
     * The status of the response (e.g., "OK", "ERROR").
     */
    private String status;
    /**
     * The status code of the response (e.g., 200, 404).
     */
    private int statusCode;
    /**
     * The size of the data, if the data is a collection; otherwise, 0.
     */
    private int size;
    /**
     * The error message, if any error occurred.
     */
    private String error;
    /**
     * Indicates whether the response is valid (true if data is present and no error).
     */
    private boolean valid;
    /**
     * The actual response data.
     */
    private T data;

    /**
     * Constructs an empty {@code Response}.
     */
    public Response() {
    }

    /**
     * Constructs a {@code Response} with the specified data.
     * Sets validity to true and size if data is a collection.
     *
     * @param data the response data
     */
    public Response(T data) {
        setData(data);
    }

    /**
     * Sets the response data and updates validity and size accordingly.
     *
     * @param data the response data
     */
    public void setData(T data) {
        this.data = data;
        if (data != null) {
            valid = true;
            if (data instanceof Collection) {
                size = ((Collection<?>) data).size();
            }
        }
    }

    /**
     * Returns the response data.
     *
     * @return the response data
     */
    public T getData() {
        return data;
    }

    /**
     * Returns the error message, if any.
     *
     * @return the error message
     */
    public String getError() {
        return error;
    }

    /**
     * Sets the error message and marks the response as invalid.
     *
     * @param error the error message
     */
    public void setError(String error) {
        this.error = error;
        if (error != null) {
            valid = false;
        }
    }

    /**
     * Returns the status of the response.
     *
     * @return the response status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of the response.
     *
     * @param status the response status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the size of the data if it is a collection; otherwise, returns 0.
     *
     * @return the size of the data
     */
    public int getSize() {
        return size;
    }

    /**
     * Sets the size of the data. Typically used when data is a collection.
     *
     * @param size the size of the data
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Returns whether the response is valid (true if data is present and no error).
     *
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Sets the validity of the response.
     *
     * @param valid true if valid, false otherwise
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Creates a successful response with no data and status "OK".
     *
     * @param <T> the type of the response data
     * @return a Response instance with status "OK" and valid=true
     */
    public static <T> Response<T> ok() {
        Response<T> response = new Response<>();
        response.setStatus("OK");
        response.setStatusCode(200);
        response.setValid(true);
        return response;
    }

    /**
     * Creates a successful response with the given data and status "OK".
     *
     * @param data the response data
     * @param <T>  the type of the response data
     * @return a Response instance with status "OK", valid=true, and the given data
     */
    public static <T> Response<T> ok(T data) {
        Response<T> response = new Response<>(data);
        response.setStatus("OK");
        response.setStatusCode(200);
        response.setValid(true);
        return response;
    }

    /**
     * Creates an error response with the given error message and status "ERROR".
     *
     * @param error the error message
     * @param <T>   the type of the response data
     * @return a Response instance with status "ERROR", valid=false, and the given error message
     */
    public static <T> Response<T> error(String error) {
        Response<T> response = new Response<>();
        response.setStatus("ERROR");
        response.setStatusCode(500);
        response.setError(error);
        response.setValid(false);
        return response;
    }

    /**
     * Creates an error response with the given status code and error message.
     *
     * @param statusCode the status code
     * @param error      the error message
     * @param <T>        the type of the response data
     * @return a Response instance with status "ERROR", the given status code, valid=false, and the error message
     */
    public static <T> Response<T> error(int statusCode, String error) {
        Response<T> response = new Response<>();
        response.setStatus("ERROR");
        response.setStatusCode(statusCode);
        response.setError(error);
        response.setValid(false);
        return response;
    }

    /**
     * Creates a response with the given data and status "OK".
     *
     * @param data the response data
     * @param <T>  the type of the response data
     * @return a Response instance with status "OK", valid=true, and the given data
     */
    public static <T> Response<T> of(T data) {
        return ok(data);
    }

    /**
     * Creates a response for a collection of data with status "OK".
     *
     * @param data the collection of response data
     * @param <T>  the type of the response data
     * @return a Response instance with status "OK", valid=true, and the given collection
     */
    public static <T extends Collection<?>> Response<T> list(T data) {
        Response<T> response = new Response<>(data);
        response.setStatus("OK");
        response.setStatusCode(200);
        response.setValid(true);
        return response;
    }

    /**
     * Creates a response indicating that the requested resource was not found.
     *
     * @param <T> the type of the response data
     * @return a Response instance with status "NOT_FOUND", statusCode=404, valid=false
     */
    public static <T> Response<T> notFound() {
        Response<T> response = new Response<>();
        response.setStatus("NOT_FOUND");
        response.setStatusCode(404);
        response.setValid(false);
        return response;
    }

    /**
     * Creates a custom response with the specified parameters.
     *
     * @param status     the response status
     * @param statusCode the response status code
     * @param data       the response data
     * @param error      the error message
     * @param valid      whether the response is valid
     * @param <T>        the type of the response data
     * @return a custom Response instance
     */
    public static <T> Response<T> custom(String status, int statusCode, T data, String error, boolean valid) {
        Response<T> response = new Response<>(data);
        response.setStatus(status);
        response.setStatusCode(statusCode);
        response.setError(error);
        response.setValid(valid);
        return response;
    }
}
