package tools.dynamia.web.navigation;

/**
 * Functional interface for providing the base path for REST API endpoints.
 * <p>
 * This interface allows customization of the root URL path where REST API endpoints are mounted.
 * It's particularly useful in scenarios where the API path needs to be configurable, versioned,
 * or dependent on deployment context (e.g., multi-tenant applications, API versioning strategies).
 * </p>
 *
 * <p>
 * <b>Common use cases:</b>
 * <ul>
 *   <li>Configurable API base paths (e.g., from properties or environment)</li>
 *   <li>API versioning (e.g., "/api/v1", "/api/v2")</li>
 *   <li>Multi-tenant applications with tenant-specific paths</li>
 *   <li>Context-sensitive API routing</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Usage example:</b>
 * <pre>{@code
 * // Configure via Spring Bean
 * @Bean
 * public RestApiBasePathProvider restApiBasePathProvider(
 *         @Value("${api.base-path:/api}") String basePath) {
 *     return () -> basePath;
 * }
 *
 * // Version-specific provider
 * @Component
 * public class VersionedApiPathProvider implements RestApiBasePathProvider {
 *     @Override
 *     public String getBaseApiPath() {
 *         return "/api/v" + getApiVersion();
 *     }
 * }
 *
 * // Usage in controllers
 * @RestController
 * @RequestMapping("${api.base-path:/api}/users")
 * public class UserController {
 *     // REST endpoints
 * }
 * }</pre>
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
@FunctionalInterface
public interface RestApiBasePathProvider {

    /**
     * Returns the base path for REST API endpoints.
     * <p>
     * The returned path should start with a forward slash and not end with one
     * (e.g., "/api", "/api/v1", "/rest"). This path will be used as the prefix
     * for all REST API endpoint mappings.
     * </p>
     *
     * @return the base API path, typically starting with "/" (e.g., "/api")
     */
    String getBaseApiPath();
}
