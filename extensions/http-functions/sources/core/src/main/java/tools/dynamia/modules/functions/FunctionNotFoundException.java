package tools.dynamia.modules.functions;

/**
 * Thrown when a requested {@link tools.dynamia.modules.functions.domain.DynamiaHttpFunction} (or a
 * specific version of it) does not exist at all. Callers should map this exception to an HTTP 404
 * response, distinguishing it from {@link FunctionInactiveException} for troubleshooting purposes.
 *
 * @author Mario A. Serrano Leones
 */
public class FunctionNotFoundException extends RuntimeException {

    public FunctionNotFoundException(String name, Integer version) {
        super(version != null
                ? "Function [" + name + "] version [" + version + "] not found"
                : "Function [" + name + "] not found");
    }
}