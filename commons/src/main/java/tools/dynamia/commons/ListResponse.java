package tools.dynamia.commons;

import java.util.Collections;
import java.util.List;

/**
 * A specialized Response for returning a list of items as the response data.
 * <p>
 * This class extends {@link Response} with a generic List type, providing convenient constructors and static builders
 * for common use cases involving lists, such as success, error, empty, and not found responses.
 * <br>
 * Example usage:
 * <pre>
 * ListResponse<String> response = ListResponse.ok(List.of("A", "B"));
 * </pre>
 * </p>
 *
 * @param <T> the type of the items in the list
 */
public class ListResponse<T> extends Response<List<T>> {

    /**
     * Constructs an empty ListResponse with status "OK" and an empty list.
     */
    public ListResponse() {
        super(Collections.emptyList());
        setStatus("OK");
        setStatusCode(200);
        setValid(true);
    }

    /**
     * Constructs a ListResponse with the given list of items and status "OK".
     *
     * @param data the list of items
     */
    public ListResponse(List<T> data) {
        super(data);
        setStatus("OK");
        setStatusCode(200);
        setValid(true);
    }

    /**
     * Constructs a ListResponse with the given list of items and custom status.
     *
     * @param data       the list of items
     * @param status     the response status
     * @param statusCode the response status code
     * @param valid      whether the response is valid
     */
    public ListResponse(List<T> data, String status, int statusCode, boolean valid) {
        super(data);
        setStatus(status);
        setStatusCode(statusCode);
        setValid(valid);
    }

    /**
     * Creates a successful ListResponse with the given list of items and status "OK".
     *
     * @param data the list of items
     * @param <T>  the type of the items
     * @return a ListResponse instance with status "OK"
     */
    public static <T> ListResponse<T> ok(List<T> data) {
        return new ListResponse<>(data);
    }

    /**
     * Creates an empty successful ListResponse with status "OK".
     *
     * @param <T> the type of the items
     * @return a ListResponse instance with an empty list and status "OK"
     */
    public static <T> ListResponse<T> empty() {
        return new ListResponse<>(Collections.emptyList());
    }

    /**
     * Creates an error ListResponse with the given error message and status "ERROR".
     *
     * @param error the error message
     * @param <T>   the type of the items
     * @return a ListResponse instance with status "ERROR" and valid=false
     */
    public static <T> ListResponse<T> errorList(String error) {
        ListResponse<T> response = new ListResponse<>(Collections.emptyList(), "ERROR", 500, false);
        response.setError(error);
        return response;
    }

    /**
     * Creates a ListResponse indicating that the requested resource was not found.
     *
     * @param <T> the type of the items
     * @return a ListResponse instance with status "NOT_FOUND", statusCode=404, valid=false
     */
    public static <T> ListResponse<T> notFoundList() {
        return new ListResponse<>(Collections.emptyList(), "NOT_FOUND", 404, false);
    }

    /**
     * Creates a custom ListResponse with the specified parameters.
     *
     * @param data       the list of items
     * @param status     the response status
     * @param statusCode the response status code
     * @param error      the error message
     * @param valid      whether the response is valid
     * @param <T>        the type of the items
     * @return a custom ListResponse instance
     */
    public static <T> ListResponse<T> custom(List<T> data, String status, int statusCode, String error, boolean valid) {
        ListResponse<T> response = new ListResponse<>(data, status, statusCode, valid);
        response.setError(error);
        return response;
    }
}

