package tools.dynamia.viewers;

/**
 * Interface for defining restrictions on fields within a {@link View} descriptor.
 * <p>
 * Implementations of this interface are responsible for evaluating the access level of a specific {@link Field}
 * in the context of a {@link View}. The restriction can determine whether a field is visible, hidden, read-only,
 * or has another access level, based on custom logic provided by the implementation.
 * </p>
 * <p>
 * The {@code getOrder()} method defines the priority of the restriction. Restrictions with lower order values
 * are evaluated first, allowing for fine-grained control over which restriction takes precedence when multiple
 * restrictions apply to the same field. This enables a flexible and extensible mechanism for field access control
 * in view descriptors.
 * </p>
 * <p>
 * Typical use cases include security-based field restrictions, user role management, dynamic UI adaptation,
 * and business rule enforcement for field-level access in viewers.
 * </p>
 *
 * Example:
 * <pre>{@code
 * public class SecurityFieldRestriction implements FieldRestriction {
 *     public int getOrder() {
 *         return 100;
 *     }
 *
 *     public FieldRestrictionType evaluale(View view, Field field) {
 *         if (field.getName().equals("salary") && !hasAdminRole()) {
 *             return FieldRestrictionType.HIDDEN;
 *         }
 *         return FieldRestrictionType.VISIBLE;
 *     }
 * }
 * }</pre>
 *
 * @author Dynamia Soluciones IT
 */
public interface FieldRestriction {

    /**
     * Returns the order (priority) of this restriction. Lower values indicate higher priority.
     * <p>
     * Restrictions are evaluated in ascending order of their order value. A restriction with
     * order 0 will be evaluated before a restriction with order 100.
     * </p>
     *
     * @return the order value of this restriction
     */
    int getOrder();

    /**
     * Evaluates the restriction for the given {@link View} and {@link Field}.
     * <p>
     * Implementations should return the appropriate {@link FieldRestrictionType} based on their logic,
     * which may include checking user permissions, business rules, or field metadata.
     * </p>
     *
     * @param view the view descriptor containing the field
     * @param field the field to evaluate
     * @return the evaluated {@link FieldRestrictionType} for the field in the given view
     */
    FieldRestrictionType evaluale(View view, Field field);

}
