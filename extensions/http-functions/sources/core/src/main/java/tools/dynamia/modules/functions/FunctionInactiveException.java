package tools.dynamia.modules.functions;

/**
 * Thrown when a requested {@link tools.dynamia.modules.functions.domain.DynamiaHttpFunction} (or a
 * specific version of it) exists but is not {@code ACTIVE} (e.g. it is still {@code DRAFT},
 * {@code INACTIVE} or {@code DELETED}). Callers should map this exception to an HTTP 404 response,
 * distinguishing it from {@link FunctionNotFoundException} for troubleshooting purposes.
 *
 * @author Mario A. Serrano Leones
 */
public class FunctionInactiveException extends RuntimeException {

    public FunctionInactiveException(String name, Integer version) {
        super(version != null
                ? "Function [" + name + "] version [" + version + "] is not active"
                : "Function [" + name + "] is not active");
    }
}
