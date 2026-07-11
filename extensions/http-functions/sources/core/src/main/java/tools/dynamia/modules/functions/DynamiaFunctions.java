package tools.dynamia.modules.functions;

import tools.dynamia.integration.Containers;
import tools.dynamia.modules.functions.services.DynamiaHttpFunctionsService;

import java.util.Map;

/**
 * Static entry point to call {@link tools.dynamia.modules.functions.domain.DynamiaHttpFunction}s from
 * regular Java code, as if they were normal method calls, without coupling the caller to a specific
 * implementation. By default, the highest active version of a function is invoked, but a specific
 * version can be requested explicitly.
 *
 * Example:
 * <pre>{@code
 * FunctionResult result = DynamiaFunctions.call("WhatsApp.sendMessage",
 *         Map.of("number", "123456789", "message", "Hello"));
 *
 * FunctionResult v2Result = DynamiaFunctions.call("WhatsApp.sendMessage", 2,
 *         Map.of("number", "123456789", "message", "Hello"));
 * }</pre>
 *
 * @author Mario A. Serrano Leones
 */
public class DynamiaFunctions {

    private DynamiaFunctions() {
    }

    /**
     * Calls the highest active version of a function.
     *
     * @param name   the function name (e.g. {@code WhatsApp.sendMessage})
     * @param params the call parameters
     * @return the execution result
     */
    public static FunctionResult call(String name, Map<String, Object> params) {
        return service().call(name, params);
    }

    /**
     * Calls a specific version of a function.
     *
     * @param name    the function name
     * @param version the requested version
     * @param params  the call parameters
     * @return the execution result
     */
    public static FunctionResult call(String name, int version, Map<String, Object> params) {
        return service().call(name, version, params);
    }

    /**
     * Calls the highest active version of a function, auto-registering it as a {@code DRAFT} placeholder
     * when it does not exist yet, so integration code doesn't fail hard while the function is pending
     * configuration (url, method, body template, etc.) in the back office.
     *
     * @param name       the function name
     * @param params     the call parameters
     * @param autoCreate when {@code true}, a missing function is auto-registered as {@code DRAFT} instead
     *                   of throwing {@link FunctionNotFoundException}
     * @return the execution result
     */
    public static FunctionResult call(String name, Map<String, Object> params, boolean autoCreate) {
        return service().call(name, params, autoCreate);
    }

    /**
     * Calls a specific version of a function, auto-registering it as a {@code DRAFT} placeholder when it
     * does not exist yet. See {@link #call(String, Map, boolean)}.
     *
     * @param name       the function name
     * @param version    the requested version
     * @param params     the call parameters
     * @param autoCreate when {@code true}, a missing function is auto-registered as {@code DRAFT} instead
     *                   of throwing {@link FunctionNotFoundException}
     * @return the execution result
     */
    public static FunctionResult call(String name, int version, Map<String, Object> params, boolean autoCreate) {
        return service().call(name, version, params, autoCreate);
    }

    private static DynamiaHttpFunctionsService service() {
        DynamiaHttpFunctionsService service = Containers.get().findObject(DynamiaHttpFunctionsService.class);
        if (service == null) {
            throw new IllegalStateException("No " + DynamiaHttpFunctionsService.class.getName() + " implementation available");
        }
        return service;
    }
}
