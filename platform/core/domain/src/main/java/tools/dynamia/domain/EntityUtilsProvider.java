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

import tools.dynamia.domain.query.Parameter;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Provides utility methods for working with domain entities.
 * This interface abstracts common operations related to entity identification,
 * validation, and field analysis, allowing different persistence frameworks
 * to implement their own strategies.
 * <p>
 * Implementations of this interface should be registered as Spring components
 * to be automatically discovered and used by the framework.
 * </p>
 *
 * Example:
 * <pre>{@code
 * @Component
 * public class JpaEntityUtilsProvider implements EntityUtilsProvider {
 *     public Serializable findId(Object entity) {
 *         // JPA-specific implementation
 *     }
 * }
 * }</pre>
 */
public interface EntityUtilsProvider {

    /**
     * Extracts the identifier (primary key) from the given entity.
     *
     * @param entity the domain entity to extract the ID from
     * @return the entity's identifier, or null if not found or not yet assigned
     *
     * Example:
     * <pre>{@code
     * Customer customer = customerService.findById(123L);
     * Serializable id = entityUtils.findId(customer);
     * // id will be 123L
     * }</pre>
     */
    Serializable findId(Object entity);

    /**
     * Checks whether the given object is a domain entity.
     *
     * @param entity the object to check
     * @return true if the object is a managed domain entity, false otherwise
     *
     * Example:
     * <pre>{@code
     * boolean isEntity = entityUtils.isEntity(customer);
     * if (isEntity) {
     *     // Perform entity-specific operations
     * }
     * }</pre>
     */
    boolean isEntity(Object entity);

    /**
     * Checks whether the given class represents a domain entity type.
     *
     * @param entityClass the class to check
     * @return true if the class is a domain entity class, false otherwise
     *
     * Example:
     * <pre>{@code
     * boolean isEntity = entityUtils.isEntity(Customer.class);
     * if (isEntity) {
     *     // Proceed with entity-related logic
     * }
     * }</pre>
     */
    boolean isEntity(Class entityClass);

    /**
     * Determines whether a field should be persisted to the database.
     * This method helps identify which fields in an entity class are
     * eligible for persistence, excluding transient or computed fields.
     *
     * @param field the field to check
     * @return true if the field should be persisted, false otherwise
     *
     * Example:
     * <pre>{@code
     * Field nameField = Customer.class.getDeclaredField("name");
     * if (entityUtils.isPersitable(nameField)) {
     *     // Include field in persistence operations
     * }
     * }</pre>
     */
    boolean isPersitable(Field field);

    /**
     * Returns the default parameter class used for query operations.
     * This class is used when constructing queries with parameters
     * in the domain layer.
     *
     * @return the default Parameter implementation class
     *
     * Example:
     * <pre>{@code
     * Class<? extends Parameter> paramClass = entityUtils.getDefaultParameterClass();
     * Parameter param = paramClass.getDeclaredConstructor().newInstance();
     * }</pre>
     */
    Class<? extends Parameter> getDefaultParameterClass();
}
