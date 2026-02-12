package tools.dynamia.reports;

import java.util.Map;

/**
 * Interceptor for modifying report parameters before they are processed.
 * <p>
 * Implementations of this interface can intercept and transform parameter maps
 * used in report generation, allowing for dynamic parameter manipulation,
 * validation, or enrichment before the report is rendered.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @Component
 * public class CustomReportInterceptor implements ReportParametersInterceptor {
 *
 *     @Override
 *     public Map<String, Object> intercept(Map<String, Object> params) {
 *         params.put("timestamp", System.currentTimeMillis());
 *         params.put("user", getCurrentUser());
 *         return params;
 *     }
 * }
 * }</pre>
 *
 * @since 1.0
 */
public interface ReportParametersInterceptor {

    /**
     * Intercepts and modifies the report parameters map.
     * <p>
     * This method receives the original parameters and returns a potentially
     * modified version. The returned map will be used for report generation.
     * </p>
     *
     * @param params the original report parameters map, never null
     * @return the modified parameters map, never null
     *
     * @throws IllegalArgumentException if required parameters are missing or invalid
     */
    Map<String, Object> intercept(Map<String, Object> params);
}
