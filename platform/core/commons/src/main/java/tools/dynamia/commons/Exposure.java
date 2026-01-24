package tools.dynamia.commons;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark types, fields, or methods with exposure scopes.
 * Used to control the visibility and accessibility of elements in different contexts,
 * such as APIs, serialization, or security layers within the Dynamia Tools framework.
 *
 * <p>This annotation allows defining one or more scope names that determine
 * where and how the annotated element can be exposed or accessed. It's commonly
 * used in scenarios like:</p>
 * <ul>
 *   <li>REST API exposure control</li>
 *   <li>JSON serialization/deserialization filtering</li>
 *   <li>Security and access control</li>
 *   <li>Data view customization</li>
 *   <li>Module and component visibility</li>
 * </ul>
 *
 * <p><strong>Usage on Classes (Entity/DTO):</strong></p>
 * <pre>{@code
 * @Exposure(scope = {Scopes.API, Scopes.DATA_API})
 * public class UserDTO {
 *     private String name;
 *     private String email;
 * }
 * }</pre>
 *
 * <p><strong>Usage on Fields:</strong></p>
 * <pre>{@code
 * public class User {
 *     // Visible in UI and API contexts
 *     @Exposure(scope = {Scopes.UI, Scopes.API})
 *     private String name;
 *
 *     // Only visible in UI context
 *     @Exposure(scope = {Scopes.UI})
 *     private String email;
 *
 *     // Internal use only, not exposed
 *     @Exposure(scope = {Scopes.INTERNAL})
 *     private String passwordHash;
 * }
 * }</pre>
 *
 * <p><strong>Usage on Methods (Service):</strong></p>
 * <pre>{@code
 * public class AccountService {
 *     // Public API endpoint
 *     @Exposure(scope = {Scopes.API, Scopes.EXTERNAL})
 *     public AccountInfo getBasicInfo() {
 *         return new AccountInfo();
 *     }
 *
 *     // Admin API only
 *     @Exposure(scope = {Scopes.ADMIN_API})
 *     public FullAccountDetails getFullDetails() {
 *         return new FullAccountDetails();
 *     }
 *
 *     // Internal module access only
 *     @Exposure(scope = {Scopes.MODULE, Scopes.INTERNAL})
 *     public void updateInternalState() {
 *         // internal logic
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Multiple Scopes Example (Product/E-commerce):</strong></p>
 * <pre>{@code
 * public class Product {
 *     // Visible everywhere
 *     @Exposure(scope = {Scopes.UI, Scopes.API, Scopes.EXTERNAL})
 *     private String name;
 *
 *     // API and external systems can access
 *     @Exposure(scope = {Scopes.API, Scopes.EXTERNAL})
 *     private BigDecimal price;
 *
 *     // Only data API access
 *     @Exposure(scope = {Scopes.DATA_API})
 *     private BigDecimal wholesalePrice;
 *
 *     // Internal cost calculation only
 *     @Exposure(scope = {Scopes.INTERNAL})
 *     private BigDecimal cost;
 * }
 * }</pre>
 *
 * <p><strong>Usage with Reporting API:</strong></p>
 * <pre>{@code
 * public class SalesReport {
 *     // Exposed in reporting and admin contexts
 *     @Exposure(scope = {Scopes.REPORTING_API, Scopes.ADMIN_API})
 *     private BigDecimal totalRevenue;
 *
 *     // Reporting only
 *     @Exposure(scope = {Scopes.REPORTING_API})
 *     private List<Sale> sales;
 *
 *     // UI display
 *     @Exposure(scope = {Scopes.UI})
 *     private String formattedTotal;
 * }
 * }</pre>
 *
 * <p><strong>Custom Scopes Example:</strong></p>
 * <pre>{@code
 * // Register custom scopes
 * Scopes.registerScope("premium");
 * Scopes.registerScope("trial");
 *
 * public class Feature {
 *     // Available to all users
 *     @Exposure(scope = {Scopes.UI, Scopes.API})
 *     private String basicFeature;
 *
 *     // Available to premium users only
 *     @Exposure(scope = {"premium"})
 *     private String premiumFeature;
 *
 *     // Not available in trial mode
 *     @Exposure(scope = {"premium", Scopes.INTERNAL})
 *     private String advancedFeature;
 * }
 * }</pre>
 *
 * @see Scopes
 * @see java.lang.annotation.Target
 * @see java.lang.annotation.Retention
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Exposure {

    /**
     * Defines the scope or scopes in which the annotated element is exposed.
     * An element can belong to multiple scopes, allowing flexible visibility control.
     *
     * <p>It is recommended to use the constants defined in {@link Scopes} class for
     * consistency and type safety:</p>
     * <ul>
     *   <li>{@link Scopes#MODULE} - Accessible within its module</li>
     *   <li>{@link Scopes#APPLICATION} - Application-wide accessibility</li>
     *   <li>{@link Scopes#SYSTEM} - System-level accessibility</li>
     *   <li>{@link Scopes#EXTERNAL} - Accessible from external systems</li>
     *   <li>{@link Scopes#INTERNAL} - Internal use only</li>
     *   <li>{@link Scopes#UI} - Exposed in user interface</li>
     *   <li>{@link Scopes#API} - Exposed via API</li>
     *   <li>{@link Scopes#PUBLIC} - Public service</li>
     *   <li>{@link Scopes#DATA_API} - Exposed in data API</li>
     *   <li>{@link Scopes#ADMIN_API} - Exposed in admin API</li>
     *   <li>{@link Scopes#AUTH_API} - Exposed in authentication API</li>
     *   <li>{@link Scopes#REPORTING_API} - Exposed in reporting API</li>
     *   <li>{@link Scopes#METADATA_API} - Exposed in metadata API</li>
     * </ul>
     *
     * <p><strong>Recommended Usage:</strong></p>
     * <pre>{@code
     * // Using Scopes constants (recommended)
     * @Exposure(scope = {Scopes.UI, Scopes.API})
     * private String publicField;
     *
     * // Multiple scopes
     * @Exposure(scope = {Scopes.DATA_API, Scopes.ADMIN_API})
     * private String restrictedField;
     *
     * // Custom scopes can also be used
     * @Exposure(scope = {"custom-scope", Scopes.INTERNAL})
     * private String mixedScopeField;
     * }</pre>
     *
     * <p><strong>Usage Patterns:</strong></p>
     * <pre>{@code
     * // Check exposed fields programmatically
     * List<Field> apiFields = Scopes.getFieldsByScope(MyEntity.class, Scopes.API);
     *
     * // Check if field is exposed in specific scope
     * boolean isExposed = Scopes.isExposedInScope(field, Scopes.UI);
     *
     * // Get all methods exposed in admin API
     * List<Method> adminMethods = Scopes.getMethodsByScope(MyService.class, Scopes.ADMIN_API);
     * }</pre>
     *
     * @return an array of scope names
     * @see Scopes
     */
    String[] scope();
}


