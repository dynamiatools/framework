package tools.dynamia.commons;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Collection;

/**
 * Basic generic response container. Use it when need send data between objects or system
 *
 * @param <T>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> implements Serializable {

    private String status;
    private int size;
    private String error;
    private boolean valid;
    private T data;

    public Response() {
    }

    public Response(T data) {
        setData(data);
    }

    public void setData(T data) {
        this.data = data;
        if (data != null) {
            valid = true;
            if (data instanceof Collection) {
                size = ((Collection<?>) data).size();
            }
        }
    }

    public T getData() {
        return data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
        if (error != null) {
            valid = false;
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
