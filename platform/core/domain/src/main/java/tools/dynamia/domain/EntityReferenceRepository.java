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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Repository for managing entity references. This interface is responsible for constructing
 * and exporting {@link EntityReference} objects from actual entities, providing a decoupled
 * way to access entity data across modules.
 *
 * <p>EntityReferenceRepository implementations can load data from local databases, external services,
 * or any other data source. The main purpose is to provide a lightweight representation of entities
 * without requiring direct dependencies on entity classes.</p>
 *
 * <p><strong>Implementation:</strong></p>
 * <p>You can use {@link DefaultEntityReferenceRepository} as a base implementation and expose it
 * as a Spring bean:</p>
 * <pre>{@code
 * @Configuration
 * public class ReferencesConfig {
 *
 *     @Bean
 *     public EntityReferenceRepository<Long> productRepository() {
 *         DefaultEntityReferenceRepository<Long> repo =
 *             new DefaultEntityReferenceRepository<>(Product.class, "name", "sku");
 *         repo.setAlias("Product");
 *         repo.setCacheable(true);
 *         return repo;
 *     }
 *
 *     @Bean
 *     public EntityReferenceRepository<Long> customerRepository() {
 *         DefaultEntityReferenceRepository<Long> repo =
 *             new DefaultEntityReferenceRepository<>(Customer.class, "name", "email", "phone");
 *         repo.setAlias("Customer");
 *         return repo;
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Usage:</strong></p>
 * <pre>{@code
 * // Get repository by alias using DomainUtils
 * EntityReferenceRepository<Long> repo = DomainUtils.getEntityReferenceRepositoryByAlias("Product");
 *
 * // Load a single entity reference
 * EntityReference<Long> product = repo.load(1L);
 *
 * // Search for entity references
 * List<EntityReference<Long>> products = repo.find("laptop", null);
 *
 * // Load by specific field
 * EntityReference<Long> productBySku = repo.load("sku", "PROD-123");
 * }</pre>
 *
 * @param <ID> the type of the entity identifier
 *
 * @see EntityReference
 * @see DefaultEntityReferenceRepository
 * @see tools.dynamia.domain.util.DomainUtils
 */
public interface EntityReferenceRepository<ID extends Serializable> {

    /**
     * Gets the alias used to identify this repository.
     * This alias is used with {@link tools.dynamia.domain.util.DomainUtils#getEntityReferenceRepositoryByAlias(String)}.
     *
     * @return the repository alias
     */
    String getAlias();

    /**
     * Gets the fully qualified class name of the entity managed by this repository.
     *
     * @return the entity class name
     */
    String getEntityClassName();

    /**
     * Loads an entity reference by its unique identifier.
     *
     * @param id the entity identifier
     * @return the entity reference, or null if not found
     */
    EntityReference<ID> load(ID id);

    /**
     * Loads an entity reference by a specific field value.
     *
     * @param field the field name to search
     * @param value the field value to match
     * @return the entity reference, or null if not found
     */
    EntityReference<ID> load(String field, Object value);

    /**
     * Loads an entity reference using multiple parameter filters.
     *
     * @param params a map of field names and values to filter by
     * @return the entity reference, or null if not found
     */
    EntityReference<ID> load(Map<String, Object> params);

    /**
     * Finds entity references matching the search text and additional parameters.
     *
     * @param text the search text (can be null)
     * @param params additional filter parameters (can be null)
     * @return a list of matching entity references
     */
    List<EntityReference<ID>> find(String text, Map<String, Object> params);

    /**
     * Gets the first available entity reference from the repository.
     *
     * @return the first entity reference, or null if the repository is empty
     */
    EntityReference<ID> getFirst();
}
