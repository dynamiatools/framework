package tools.dynamia.modules.functions.services;

import tools.dynamia.modules.functions.FunctionResult;
import tools.dynamia.modules.functions.domain.DynamiaHttpFunction;

import java.util.Map;

/**
 * Registry and execution engine for {@link DynamiaHttpFunction}s. Implementations resolve functions by
 * name and version, validate call parameters against their declared definitions, and execute the
 * underlying HTTP call, acting as an internal capability bus for Dynamia-based applications.
 *
 * @author Mario A. Serrano Leones
 */
public interface DynamiaHttpFunctionsService {

    /**
     * Resolves the highest active version of a function.
     *
     * @param name the function name (e.g. {@code WhatsApp.sendMessage})
     * @return the resolved function, or {@code null} if no active version exists
     */
    DynamiaHttpFunction findLatestVersion(String name);

    /**
     * Resolves a specific version of a function, or the highest active version when {@code version} is
     * {@code null}.
     *
     * @param name    the function name
     * @param version the requested version, or {@code null} to resolve the latest active version
     * @return the resolved function, or {@code null} if not found
     */
    DynamiaHttpFunction findVersion(String name, Integer version);

    /**
     * Validates the provided call parameters against a function's parameter definitions.
     *
     * @param function the function definition
     * @param params   the parameters supplied by the caller
     * @throws tools.dynamia.domain.ValidationError if a required parameter is missing or has an invalid
     *                                               value for its declared type
     */
    void validateParameters(DynamiaHttpFunction function, Map<String, Object> params);

    /**
     * Calls the highest active version of a function.
     *
     * @param name   the function name
     * @param params the call parameters
     * @return the execution result
     *
     * Example:
     * <pre>{@code
     * FunctionResult result = service.call("WhatsApp.sendMessage", Map.of("number", "123", "message", "Hello"));
     * }</pre>
     */
    FunctionResult call(String name, Map<String, Object> params);

    /**
     * Calls a specific version of a function.
     *
     * @param name    the function name
     * @param version the requested version, or {@code null} to resolve the latest active version
     * @param params  the call parameters
     * @return the execution result
     */
    FunctionResult call(String name, Integer version, Map<String, Object> params);
}