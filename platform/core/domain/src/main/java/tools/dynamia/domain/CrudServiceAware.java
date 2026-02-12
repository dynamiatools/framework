package tools.dynamia.domain;

import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.DomainUtils;

/**
 * Mixin interface that provides convenient access to {@link CrudService} instances.
 *
 * <p>This interface acts as a helper to simplify obtaining {@link CrudService} references
 * in any class without the need for explicit dependency injection or manual lookups.
 * By implementing this interface, classes gain immediate access to the framework's
 * persistence layer through the {@link #crudService()} method.</p>
 *
 * <p><b>Key benefits:</b></p>
 * <ul>
 *   <li>Eliminates boilerplate code for CrudService injection</li>
 *   <li>Provides consistent access pattern across the application</li>
 *   <li>Useful in contexts where dependency injection is not available or practical</li>
 *   <li>Enables quick database operations without explicit service references</li>
 *   <li>Ideal for utility classes, actions, listeners, and custom components</li>
 * </ul>
 *
 * <p><b>Common use cases:</b></p>
 * <ul>
 *   <li>Action classes that need to perform CRUD operations</li>
 *   <li>Event listeners that require database access</li>
 *   <li>Utility classes performing entity operations</li>
 *   <li>Custom validators that need to query the database</li>
 *   <li>Report generators accessing entity data</li>
 * </ul>
 *
 * <p><b>Basic usage example:</b></p>
 * <pre>{@code
 * @Component
 * public class CustomerAction extends AbstractAction implements CrudServiceAware {
 *
 *     @Override
 *     public void actionPerformed(ActionEvent evt) {
 *         // Access CrudService directly without injection
 *         List<Customer> customers = crudService().findAll(Customer.class);
 *
 *         // Perform operations
 *         customers.forEach(customer -> {
 *             customer.setStatus("ACTIVE");
 *             crudService().update(customer);
 *         });
 *     }
 * }
 * }</pre>
 *
 * <p><b>Usage in validators:</b></p>
 * <pre>{@code
 * public class UniqueEmailValidator implements CrudServiceAware {
 *
 *     public boolean isEmailUnique(String email, Long excludeId) {
 *         QueryParameters params = QueryParameters.with("email", email);
 *         if (excludeId != null) {
 *             params.add("id", QueryParameters.NOT_EQUALS, excludeId);
 *         }
 *
 *         return crudService().count(User.class, params) == 0;
 *     }
 * }
 * }</pre>
 *
 * <p><b>Usage in listeners:</b></p>
 * <pre>{@code
 * @Component
 * public class OrderListener implements CrudServiceListener<Order>, CrudServiceAware {
 *
 *     @Override
 *     public void afterCreate(Order order) {
 *         // Update inventory using CrudService
 *         order.getItems().forEach(item -> {
 *             Product product = crudService().find(Product.class, item.getProductId());
 *             product.setStock(product.getStock() - item.getQuantity());
 *             crudService().update(product);
 *         });
 *     }
 * }
 * }</pre>
 *
 * <p><b>Note:</b> This interface uses the default method pattern, so no implementation is required.
 * The {@link #crudService()} method delegates to {@link DomainUtils#lookupCrudService()}, which
 * retrieves the active {@link CrudService} instance from the application context.</p>
 *
 * @see CrudService
 * @see DomainUtils#lookupCrudService()
 * @see tools.dynamia.domain.util.CrudServiceListener
 */
public interface CrudServiceAware {

    /**
     * Obtains a reference to the active {@link CrudService} instance.
     *
     * <p>This method provides convenient access to the framework's persistence layer
     * without requiring explicit dependency injection. It uses the service locator
     * pattern to retrieve the current {@link CrudService} bean from the application context.</p>
     *
     * <p>The returned service can be used to perform all standard CRUD operations:</p>
     * <ul>
     *   <li>Create, update, delete, and save entities</li>
     *   <li>Query and search for entities with various criteria</li>
     *   <li>Execute batch operations</li>
     *   <li>Manage transactions and entity lifecycle</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * // Find all active users
     * List<User> users = crudService().find(User.class,
     *     QueryParameters.with("active", true));
     *
     * // Create a new entity
     * Product product = new Product("Laptop", 999.99);
     * product = crudService().create(product);
     *
     * // Update existing entity
     * product.setPrice(899.99);
     * crudService().update(product);
     * }</pre>
     *
     * @return the active {@link CrudService} instance, never {@code null}
     * @throws IllegalStateException if no CrudService bean is available in the application context
     *
     * @see CrudService
     * @see DomainUtils#lookupCrudService()
     */
    default CrudService crudService() {
        return DomainUtils.lookupCrudService();
    }
}


