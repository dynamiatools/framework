package tools.dynamia.modules.functions;

/**
 * Thrown when a requested {@link tools.dynamia.modules.functions.domain.DynamiaHttpFunction} (or a
 * specific version of it) cannot be resolved, or is found but not active. Callers should map this
 * exception to an HTTP 404 response.
 *
 * @author Mario A. Serrano Leones
 */
public class FunctionNotFoundException extends RuntimeException {

    public FunctionNotFoundException(String name, Integer version) {
        super(version != null
                ? "Function [" + name + "] version [" + version + "] not found or not active"
                : "Function [" + name + "] not found or not active");
    }
}