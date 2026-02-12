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

import org.springframework.cache.Cache;
import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.commons.Identifiable;
import tools.dynamia.commons.Mappable;
import tools.dynamia.commons.collect.PagedList;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.CrudServiceListenerAdapter;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.integration.CacheManagerUtils;
import tools.dynamia.integration.Containers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link EntityReferenceRepository} with built-in cache support
 * and automatic entity-to-reference conversion.
 *
 * <p>This class simplifies the creation of entity reference repositories by providing
 * a ready-to-use implementation that works with any JPA entity. It automatically constructs
 * {@link EntityReference} objects from entities and provides search capabilities.</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Automatic entity-to-reference conversion</li>
 *   <li>Configurable cache support for improved performance</li>
 *   <li>Search by multiple fields</li>
 *   <li>Automatic cache invalidation on entity updates/deletes</li>
 *   <li>Support for entities implementing {@link Referenceable} interface</li>
 * </ul>
 *
 * <p><strong>Basic Usage:</strong></p>
 * <pre>{@code
 * @Configuration
 * public class EntityReferencesConfig {
 *
 *     // Simple repository with default settings
 *     @Bean
 *     public EntityReferenceRepository<Long> contactRepository() {
 *         DefaultEntityReferenceRepository<Long> repo =
 *             new DefaultEntityReferenceRepository<>(Contact.class, "name", "email");
 *         repo.setAlias("Contact");
 *         return repo;
 *     }
 *
 *     // Repository with cache enabled
 *     @Bean
 *     public EntityReferenceRepository<Long> productRepository() {
 *         DefaultEntityReferenceRepository<Long> repo =
 *             new DefaultEntityReferenceRepository<>(Product.class, "name", "sku", "category");
 *         repo.setAlias("Product");
 *         repo.setCacheable(true);
 *         repo.setCacheName("ProductReferences");
 *         repo.setMaxResult(100); // Limit search results
 *         return repo;
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Using the repository:</strong></p>
 * <pre>{@code
 * // Get the repository using DomainUtils
 * EntityReferenceRepository<Long> repo = DomainUtils.getEntityReferenceRepositoryByAlias("Contact");
 *
 * // Load by ID (cached if enabled)
 * EntityReference<Long> contact = repo.load(123L);
 *
 * // Search by text in configured fields
 * List<EntityReference<Long>> results = repo.find("john", null);
 *
 * // Load by specific field
 * EntityReference<Long> contactByEmail = repo.load("email", "john@example.com");
 *
 * // Load with multiple parameters
 * Map<String, Object> params = new HashMap<>();
 * params.put("status", "ACTIVE");
 * params.put("city", "New York");
 * EntityReference<Long> contact = repo.load(params);
 * }</pre>
 *
 * <p><strong>Custom entity reference creation:</strong></p>
 * <p>If your entity implements {@link Referenceable}, the repository will use the
 * {@code toEntityReference()} method. Otherwise, it will create a reference automatically
 * using the entity's {@code toString()} method for the name and extracting attributes.</p>
 *
 * <pre>{@code
 * @Entity
 * public class Contact implements Referenceable<Long> {
 *     @Id
 *     private Long id;
 *     private String name;
 *     private String email;
 *
 *     @Override
 *     public EntityReference<Long> toEntityReference() {
 *         EntityReference<Long> ref = new EntityReference<>(id, getClass().getName(), name);
 *         ref.addAttribute("email", email);
 *         return ref;
 *     }
 * }
 * }</pre>
 *
 * @param <ID> the type of the entity identifier
 *
 * @see EntityReferenceRepository
 * @see EntityReference
 * @see Referenceable
 * @see tools.dynamia.domain.util.DomainUtils
 */
public class DefaultEntityReferenceRepository<ID extends Serializable> extends CrudServiceListenerAdapter implements EntityReferenceRepository<ID> {

    private final LoggingService logger = new SLF4JLoggingService(DefaultEntityReferenceRepository.class);

    private CrudService crudService;
    private final Class<?> entityClass;
    private int maxResult = 60;
    private String[] findFields;
    private boolean cacheable;
    private String cacheName = "EntityReferencesCache";
    private String alias;

    /**
     * Creates a repository for the specified entity class with default settings.
     * Search fields will be set to ["id"] by default.
     *
     * @param entityClass the entity class to manage
     */
    public DefaultEntityReferenceRepository(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Creates a repository for the specified entity class with custom search fields.
     * The findFields are used when searching entities with the {@link #find(String, Map)} method.
     *
     * @param entityClass the entity class to manage
     * @param findFields the field names to use for text searching
     *
     * Example:
     * <pre>{@code
     * new DefaultEntityReferenceRepository<>(Contact.class, "name", "email", "phone")
     * }</pre>
     */
    public DefaultEntityReferenceRepository(Class<?> entityClass, String... findFields) {
        super();
        this.entityClass = entityClass;
        this.findFields = findFields;
    }

    /**
     * Creates a repository with a custom CrudService instance.
     * Useful for testing or when you need to use a specific CrudService implementation.
     *
     * @param crudService the CrudService to use
     * @param entityClass the entity class to manage
     * @param findFields the field names to use for text searching
     */
    public DefaultEntityReferenceRepository(CrudService crudService, Class<?> entityClass, String... findFields) {
        super();
        this.crudService = crudService;
        this.entityClass = entityClass;
        this.findFields = findFields;
    }


    /**
     * Checks if caching is enabled for this repository.
     *
     * @return true if caching is enabled, false otherwise
     */
    public boolean isCacheable() {
        return cacheable;
    }

    /**
     * Sets the field names to use for text searching in the {@link #find(String, Map)} method.
     *
     * @param findFields the field names to search (e.g., "name", "email", "sku")
     */
    public void setFindFields(String... findFields) {
        this.findFields = findFields;
    }

    /**
     * Enables or disables caching for this repository.
     * When enabled, the {@link #load(Serializable)} method will cache entity references
     * for improved performance. Cache is automatically invalidated when entities are
     * updated or deleted.
     *
     * @param cacheable true to enable caching, false to disable
     */
    public void setCacheable(boolean cacheable) {
        this.cacheable = cacheable;
    }

    /**
     * Gets the name of the cache used by this repository.
     *
     * @return the cache name (default is "EntityReferencesCache")
     */
    public String getCacheName() {
        return cacheName;
    }

    /**
     * Sets the name of the cache to use for this repository.
     * This allows using different caches for different entity types.
     *
     * @param cacheName the cache name to use
     */
    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    /**
     * Gets the alias used to identify this repository.
     *
     * @return the repository alias, or the simple class name if not explicitly set
     */
    @Override
    public String getAlias() {
        if (alias == null) {
            alias = entityClass.getSimpleName();
        }
        return alias;
    }

    /**
     * Sets the alias for this repository.
     * The alias is used with {@link tools.dynamia.domain.util.DomainUtils#getEntityReferenceRepositoryByAlias(String)}.
     *
     * @param alias the repository alias (e.g., "Contact", "Product")
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Gets the fully qualified class name of the entity managed by this repository.
     *
     * @return the entity class name
     */
    @Override
    public String getEntityClassName() {
        return entityClass.getName();
    }

    /**
     * Loads an entity reference by its unique identifier.
     * If caching is enabled, this method will try to load from cache first.
     *
     * @param id the entity identifier
     * @return the entity reference, or null if not found
     */
    @Override
    public EntityReference<ID> load(ID id) {
        if (id == null) {
            return null;
        }

        @SuppressWarnings("unchecked") EntityReference<ID> reference = loadFromCache(id, EntityReference.class);

        if (reference == null) {
            try {
                Object entity = getCrudService().find(entityClass, id);
                reference = createEntityReference(id, entity);
                if (reference != null) {
                    saveToCache(reference.getId(), reference);
                }
            } catch (Exception e) {
                logger.error("Error loading EntityReference id [" + id + "], entity class [" + getEntityClassName() + "]", e);
            }
        }

        return reference;
    }

    /**
     * Loads an entity reference by a specific field value.
     *
     * @param field the field name to search (e.g., "email", "sku")
     * @param value the field value to match
     * @return the entity reference, or null if not found
     *
     * Example:
     * <pre>{@code
     * EntityReference<Long> product = repo.load("sku", "PROD-123");
     * EntityReference<Long> contact = repo.load("email", "john@example.com");
     * }</pre>
     */
    @Override
    public EntityReference<ID> load(String field, Object value) {
        Object entity = getCrudService().findSingle(entityClass, QueryParameters.with(field, value)
                .setAutocreateSearcheableStrings(false));

        if (entity != null) {
            ID id = getId(entity);
            return createEntityReference(id, entity);
        }

        return null;
    }

    /**
     * Loads an entity reference using multiple parameter filters.
     *
     * @param params a map of field names and values to filter by
     * @return the entity reference, or null if not found
     *
     * Example:
     * <pre>{@code
     * Map<String, Object> params = new HashMap<>();
     * params.put("status", "ACTIVE");
     * params.put("category", "Electronics");
     * EntityReference<Long> product = repo.load(params);
     * }</pre>
     */
    /**
     * Finds entity references matching the search text and additional parameters.
     * The search text is matched against all configured findFields using a LIKE query.
     *
     * @param params additional filter parameters as field-value pairs (can be null)
     * @return a list of matching entity references (empty list if none found)
     *
     * Example:
     * <pre>{@code
     * // Simple text search
     * List<EntityReference<Long>> results = repo.find("john", null);
     *
     * // Search with additional filters
     * Map<String, Object> params = new HashMap<>();
     * params.put("status", "ACTIVE");
     * List<EntityReference<Long>> contacts = repo.find("john", params);
     * }</pre>
     */
    @Override
    public EntityReference<ID> load(Map<String, Object> params) {
        QueryParameters qparms = new QueryParameters(params);
        qparms.setAutocreateSearcheableStrings(false);
        Object entity = getCrudService().findSingle(entityClass, qparms);

        if (entity != null) {
            ID id = getId(entity);
            return createEntityReference(id, entity);
        }

        return null;
    }

    /**
     * Finds entity references matching the search text and additional parameters.
     * The search text is matched against all configured findFields using a LIKE query.
     * @param text the search text (can be null)
     * @param params additional filter parameters (can be null)
     * @return a list of matching entity references (empty list if none found)
     */
    @Override
    @SuppressWarnings({"rawtypes"})
    public List<EntityReference<ID>> find(String text, Map<String, Object> params) {

        List<EntityReference<ID>> entityReferences = new ArrayList<>();
        QueryParameters queryParameters = new QueryParameters(params);

        if (findFields == null) {
            findFields = new String[]{"id"};
        }

        queryParameters.paginate(maxResult);
        List result = getCrudService().findByFields(entityClass, text, queryParameters, findFields);
        if (result instanceof PagedList) {
            result = ((PagedList) result).getDataSource().getPageData();
        }

        if (result != null) {
            for (Object entity : result) {
                ID id = getId(entity);

                EntityReference<ID> reference = createEntityReference(id, entity);
                if (reference != null) {
                    entityReferences.add(reference);
                }
            }
        }

        return entityReferences;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private ID getId(Object entity) {
        ID id = null;
        if (entity instanceof Identifiable) {
            id = (ID) ((Identifiable) entity).getId();
        } else {
            id = (ID) DomainUtils.findEntityId(entity);
        }
        return id;
    }

    /**
     * Gets the first available entity reference from the repository.
     * Useful for getting a default or sample entity reference.
     *
     * @return the first entity reference, or null if the repository is empty
     */
    @Override
    public EntityReference<ID> getFirst() {
        Object entity = getCrudService().findSingle(entityClass, new QueryParameters());
        if (entity != null) {
            return createEntityReference(getId(entity), entity);
        } else {
            return null;
        }
    }

    /**
     * Creates an EntityReference from an entity object.
     * This is a protected method that can be overridden to customize reference creation.
     *
     * @param id the entity identifier
     * @param entity the entity object
     * @return the created entity reference, or null if entity or id is null
     */
    protected EntityReference<ID> createEntityReference(ID id, Object entity) {
        if (entity != null && id != null) {
            EntityReference ref = null;
            if (entity instanceof Referenceable) {
                ref = ((Referenceable<?>) entity).toEntityReference();
            } else {
                ref = new EntityReference<>(id, getEntityClassName(), entity.toString());
                if (entity instanceof Mappable) {
                    //noinspection unchecked
                    ref.getAttributes().putAll(((Mappable) entity).toMap());
                } else {
                    //noinspection unchecked
                    ref.getAttributes().putAll(ObjectOperations.getValuesMaps(entity));
                }
            }
            //noinspection unchecked
            return ref;
        } else {
            return null;
        }
    }

    /**
     * Gets the maximum number of results to return in find operations.
     *
     * @return the maximum result count (default is 50)
     */
    public int getMaxResult() {
        return maxResult;
    }

    /**
     * Sets the maximum number of results to return in find operations.
     * This helps prevent loading too many results and improves performance.
     *
     * @param maxResult the maximum result count
     */
    public void setMaxResult(int maxResult) {
        this.maxResult = maxResult;
    }

    /**
     * Gets the CrudService instance used for data access.
     *
     * @return the CrudService
     */
    private CrudService getCrudService() {
        if (crudService == null) {
            crudService = Containers.get().findObject(CrudService.class);
        }
        return crudService;
    }

    /**
     * Saves a value to the cache with the specified key.
     *
     * @param key the cache key
     * @param value the value to cache
     */
    protected void saveToCache(Object key, Object value) {
        Cache cache = getCache();
        if (cache != null) {
            cache.put(fixKey(key), value);
        }
    }

    /**
     * Removes a value from the cache.
     *
     * @param key the cache key to remove
     */
    protected void removeFromCache(Object key) {
        Cache cache = getCache();
        if (cache != null) {
            cache.evict(fixKey(key));
        }
    }

    /**
     * Loads a value from the cache.
     *
     * @param key the cache key
     * @param clazz the expected class type of the cached value
     * @param <T> the type of the value
     * @return the cached value, or null if not found
     */
    protected <T> T loadFromCache(Object key, Class<T> clazz) {
        T value = null;

        Cache cache = getCache();
        if (cache != null) {
            value = cache.get(fixKey(key), clazz);
        }

        return value;
    }

    /**
     * Gets the cache instance used by this repository.
     *
     * @return the cache, or null if caching is disabled
     */
    protected Cache getCache() {
        if (isCacheable()) {
            return CacheManagerUtils.getCache(cacheName);
        }
        return null;
    }

    /**
     * Callback executed after an entity is deleted.
     * Automatically clears the cached reference for the deleted entity.
     *
     * @param entity the deleted entity
     */
    @Override
    public void afterDelete(Object entity) {
        clearCache(entity);
    }

    /**
     * Callback executed after an entity is updated.
     * Automatically clears the cached reference so it will be reloaded on next access.
     *
     * @param entity the updated entity
     */
    @Override
    public void afterUpdate(Object entity) {
        clearCache(entity);
    }

    private void clearCache(Object entity) {
        if (entityClass != null && isCacheable() && entity != null && entity.getClass() == entityClass) {
            try {
                Object key = DomainUtils.findEntityId(entity);
                if (key != null) {
                    removeFromCache(fixKey(key));
                }
            } catch (Exception e) {
                logger.error("Error clearing cache from EntityReferenceRepository - Entity: " + entityClass, e);
            }
        }
    }

    /**
     * Fixes the cache key by prefixing it with the entity class name. Overridable to customize key format.
     *
     * @param key the original key
     * @return the fixed cache key
     */
    protected String fixKey(Object key) {
        return getEntityClassName() + "_" + key;
    }
}
