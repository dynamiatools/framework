package tools.dynamia.modules.functions;

/**
 * Thrown when a {@link tools.dynamia.modules.functions.domain.DynamiaHttpFunction} fails during
 * execution (e.g. the target endpoint is unreachable or returns an unexpected error). Callers should
 * map this exception to an HTTP 500 response.
 *
 * @author Mario A. Serrano Leones
 */
public class FunctionExecutionException extends RuntimeException {

    public FunctionExecutionException(String name, int version, Throwable cause) {
        super("Error executing function [" + name + "] version [" + version + "]: " + cause.getMessage(), cause);
    }
}