package tools.dynamia.web.navigation;

import org.springframework.web.bind.annotation.RequestMethod;
import tools.dynamia.navigation.Page;


/**
 * Interface for customizing automatically generated REST API endpoints for CRUD pages.
 *
 * <p>This customizer allows you to modify or disable the default REST endpoints that are
 * automatically created for each {@link tools.dynamia.crud.CrudPage} in the navigation structure.
 * By implementing this interface and registering it as a Spring component, you can control
 * how REST endpoints are generated for your CRUD operations.</p>
 *
 * <p>Use cases include:</p>
 * <ul>
 *   <li>Customizing endpoint paths to match your API naming conventions</li>
 *   <li>Disabling REST endpoints for specific pages that should not be exposed</li>
 *   <li>Adding prefixes, suffixes, or transforming endpoint names</li>
 *   <li>Applying different endpoint patterns based on HTTP methods</li>
 * </ul>
 *
 * <p>Example implementation:</p>
 * <pre>{@code
 * @Component
 * public class MyRestCustomizer implements CrudRestNavigationCustomizer {
 *
 *     @Override
 *     public String customEndpoint(Page page, String actualEndpoint, RequestMethod requestMethod) {
 *         // Add API version prefix
 *         if (actualEndpoint != null) {
 *             return "/v1" + actualEndpoint;
 *         }
 *         return actualEndpoint;
 *     }
 * }
 * }</pre>
 *
 * <p>Example to disable endpoints for sensitive pages:</p>
 * <pre>{@code
 * @Component
 * public class SecurityRestCustomizer implements CrudRestNavigationCustomizer {
 *
 *     @Override
 *     public String customEndpoint(Page page, String actualEndpoint, RequestMethod requestMethod) {
 *         // Disable REST endpoints for admin pages
 *         if (page.getName().contains("admin")) {
 *             return null; // Returning null disables the endpoint
 *         }
 *         return actualEndpoint;
 *     }
 * }
 * }</pre>
 *
 * @see tools.dynamia.navigation.Page
 * @see tools.dynamia.crud.CrudPage
 */
public interface CrudRestNavigationCustomizer {


    /**
     * Customizes or disables an auto-generated REST API endpoint for a specific page and HTTP method.
     *
     * <p>This method is called during the REST endpoint generation process for each CRUD page.
     * You can modify the endpoint path, transform it according to your needs, or disable it entirely
     * by returning {@code null}.</p>
     *
     * @param page the navigation page for which the endpoint is being generated, typically a {@link tools.dynamia.crud.CrudPage}
     * @param actualEndpoint the default endpoint path that would be generated (e.g., "/api/my-module/contacts")
     * @param requestMethod the HTTP method for this endpoint (GET, POST, PUT, DELETE, etc.)
     * @return the customized endpoint path, or {@code null} to disable the endpoint for this page and method
     *
     * @see RequestMethod
     * @see tools.dynamia.navigation.Page
     */
    String customEndpoint(Page page, String actualEndpoint, RequestMethod requestMethod);
}
