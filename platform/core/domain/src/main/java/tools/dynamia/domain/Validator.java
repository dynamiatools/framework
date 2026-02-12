/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.dynamia.domain;


/**
 * Strategy interface for implementing custom validation logic for domain entities and objects.
 *
 * <p>This interface provides a contract for creating reusable, type-safe validators that can
 * enforce business rules and data integrity constraints beyond standard bean validation annotations.
 * Validators implementing this interface are automatically discovered and invoked by the framework
 * during entity persistence operations (create, update, save).</p>
 *
 * <p><b>Key features:</b></p>
 * <ul>
 *   <li>Type-safe validation with generic type parameter</li>
 *   <li>Automatic integration with CRUD operations via {@link tools.dynamia.domain.services.CrudService}</li>
 *   <li>Centralized validation logic separated from entity classes</li>
 *   <li>Ability to perform complex, cross-field validations</li>
 *   <li>Support for database queries and external service validation</li>
 *   <li>Clear error reporting through {@link ValidationError}</li>
 * </ul>
 *
 * <p><b>When to use:</b></p>
 * <ul>
 *   <li>Complex business rules that can't be expressed with standard annotations</li>
 *   <li>Validation requiring database queries (e.g., uniqueness checks)</li>
 *   <li>Cross-field validation (e.g., start date must be before end date)</li>
 *   <li>Conditional validation based on entity state</li>
 *   <li>Integration with external services for validation</li>
 *   <li>Reusable validation logic across multiple layers</li>
 * </ul>
 *
 * <p><b>Framework Integration:</b> Validators are automatically registered as Spring beans
 * using the {@code @Provider} or {@code @Component} annotation. The framework invokes all
 * applicable validators before persisting entities to the database.</p>
 *
 * <p><b>Basic usage example:</b></p>
 * <pre>{@code
 * @Provider
 * public class CustomerValidator implements Validator<Customer> {
 *
 *     @Autowired
 *     private CrudService crudService;
 *
 *     @Override
 *     public void validate(Customer customer) throws ValidationError {
 *         // Validate required fields
 *         ValidatorUtil.validateEmpty(customer.getName(), "Customer name is required");
 *         ValidatorUtil.validateEmpty(customer.getEmail(), "Email is required");
 *
 *         // Check email uniqueness
 *         QueryParameters params = QueryParameters.with("email", customer.getEmail());
 *         if (customer.getId() != null) {
 *             params.add("id", QueryParameters.NOT_EQUALS, customer.getId());
 *         }
 *
 *         if (crudService.count(Customer.class, params) > 0) {
 *             throw new ValidationError("Email already exists: %s", customer.getEmail());
 *         }
 *     }
 * }
 * }</pre>
 *
 * <p><b>Cross-field validation example:</b></p>
 * <pre>{@code
 * @Provider
 * public class EventValidator implements Validator<Event> {
 *
 *     @Override
 *     public void validate(Event event) throws ValidationError {
 *         // Validate date range
 *         if (event.getStartDate() != null && event.getEndDate() != null) {
 *             if (event.getStartDate().isAfter(event.getEndDate())) {
 *                 throw new ValidationError("Start date must be before end date");
 *             }
 *         }
 *
 *         // Validate capacity
 *         if (event.getMaxAttendees() != null && event.getMaxAttendees() <= 0) {
 *             throw new ValidationError("Maximum attendees must be greater than zero");
 *         }
 *     }
 * }
 * }</pre>
 *
 * <p><b>Conditional validation example:</b></p>
 * <pre>{@code
 * @Provider
 * public class OrderValidator implements Validator<Order> {
 *
 *     @Override
 *     public void validate(Order order) throws ValidationError {
 *         // Validate order has items
 *         if (order.getItems() == null || order.getItems().isEmpty()) {
 *             throw new ValidationError("Order must have at least one item");
 *         }
 *
 *         // Validate payment method for specific order types
 *         if (order.getType() == OrderType.CREDIT) {
 *             if (order.getPaymentMethod() == null) {
 *                 throw new ValidationError("Payment method is required for credit orders");
 *             }
 *         }
 *
 *         // Validate total amount
 *         BigDecimal calculatedTotal = order.getItems().stream()
 *             .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
 *             .reduce(BigDecimal.ZERO, BigDecimal::add);
 *
 *         if (order.getTotal().compareTo(calculatedTotal) != 0) {
 *             throw new ValidationError("Order total does not match calculated amount");
 *         }
 *     }
 * }
 * }</pre>
 *
 * <p><b>Detailed error reporting:</b></p>
 * <pre>{@code
 * @Provider
 * public class ProductValidator implements Validator<Product> {
 *
 *     @Override
 *     public void validate(Product product) throws ValidationError {
 *         // Report detailed validation error with invalid value and property
 *         if (product.getPrice() != null && product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
 *             throw new ValidationError(
 *                 "Price cannot be negative",
 *                 product.getPrice(),
 *                 "price",
 *                 Product.class
 *             );
 *         }
 *     }
 * }
 * }</pre>
 *
 * <p><b>Note:</b> Validators should be stateless and thread-safe, as they are typically
 * registered as singleton Spring beans. Use dependency injection for any required services.</p>
 *
 * @param <T> the type of object to validate
 * @see ValidationError
 * @see ValidatorUtil
 * @see tools.dynamia.domain.services.ValidatorService
 * @see tools.dynamia.integration.sterotypes.Provider
 */
public interface Validator<T> {

    /**
     * Validates the given object according to defined business rules and constraints.
     *
     * <p>This method performs custom validation logic that goes beyond standard bean validation.
     * It should check all business rules, data integrity constraints, and dependencies required
     * for the object to be in a valid state.</p>
     *
     * <p>The method is automatically invoked by the framework during:</p>
     * <ul>
     *   <li>Entity creation via {@code CrudService.create()}</li>
     *   <li>Entity update via {@code CrudService.update()}</li>
     *   <li>Entity save via {@code CrudService.save()}</li>
     *   <li>Manual validation via {@code ValidatorService.validate()}</li>
     * </ul>
     *
     * <p><b>Implementation guidelines:</b></p>
     * <ul>
     *   <li>Throw {@link ValidationError} immediately when a validation rule fails</li>
     *   <li>Provide clear, user-friendly error messages</li>
     *   <li>Use {@link ValidatorUtil} for common validation patterns</li>
     *   <li>Validate all applicable rules in a single method call when possible</li>
     *   <li>Avoid side effects - validation should not modify the object</li>
     *   <li>Keep validation logic focused and testable</li>
     * </ul>
     *
     * <p><b>Example with multiple validations:</b></p>
     * <pre>{@code
     * @Override
     * public void validate(User user) throws ValidationError {
     *     // Check required fields
     *     ValidatorUtil.validateEmpty(user.getUsername(), "Username is required");
     *     ValidatorUtil.validateEmpty(user.getEmail(), "Email is required");
     *
     *     // Validate format
     *     if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
     *         throw new ValidationError("Invalid email format");
     *     }
     *
     *     // Validate business rules
     *     if (user.getAge() != null && user.getAge() < 18) {
     *         throw new ValidationError("User must be at least 18 years old");
     *     }
     * }
     * }</pre>
     *
     * @param t the object to validate, must not be {@code null}
     * @throws ValidationError if the object fails validation, containing details about the failure
     *
     * @see ValidationError
     * @see ValidatorUtil#validateEmpty(String, String)
     */
    void validate(T t) throws ValidationError;

}
