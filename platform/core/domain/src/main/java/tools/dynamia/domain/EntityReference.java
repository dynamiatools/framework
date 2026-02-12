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
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a reference to an entity from another module, avoiding tight coupling between modules.
 * EntityReference allows modules to reference entities without directly depending on their classes,
 * promoting loose coupling and modularity in the application architecture.
 *
 * <p>This is particularly useful in multi-module applications where direct dependencies between
 * modules should be minimized. Instead of holding a direct reference to an entity object,
 * you can store its ID and class name, and resolve it when needed using {@code DomainUtils}.</p>
 *
 * <p><strong>Usage in entities:</strong></p>
 * <p>Fields can be marked as entity references using the {@code @Reference} annotation:</p>
 * <pre>{@code
 * @Entity
 * public class Invoice {
 *     @Reference("Contact")
 *     private Long contactId;
 *
 *     private String contactName; // Optional: store the name for display
 *
 *     // Other fields...
 * }
 * }</pre>
 *
 * <p><strong>Working with EntityReference using DomainUtils:</strong></p>
 * <pre>{@code
 * // Get an EntityReferenceRepository by alias
 * EntityReferenceRepository<Long> repo = DomainUtils.getEntityReferenceRepositoryByAlias("Contact");
 *
 * // Load an entity reference by ID
 * EntityReference<Long> contactRef = repo.load(123L);
 * System.out.println("Contact: " + contactRef.getName()); // Prints: "John Doe"
 *
 * // Access additional attributes
 * String email = (String) contactRef.getAttribute("email");
 * String phone = (String) contactRef.getAttribute("phone");
 *
 * // Find entity references by text search
 * List<EntityReference<Long>> results = repo.find("John", null);
 *
 * // Get entity reference name directly using DomainUtils
 * String contactName = DomainUtils.getEntityReferenceName("Contact", 123L);
 * String contactNameOrDefault = DomainUtils.getEntityReferenceName("Contact", 123L, "Unknown Contact");
 * }</pre>
 *
 * <p><strong>EntityReferenceRepository:</strong></p>
 * <p>EntityReferenceRepository implementations are responsible for constructing and exporting
 * EntityReference objects from actual entities. You can use the default implementation
 * {@link DefaultEntityReferenceRepository} and expose it as a Spring bean:</p>
 * <pre>{@code
 * @Configuration
 * public class EntityReferencesConfig {
 *
 *     @Bean
 *     public EntityReferenceRepository<Long> contactReferenceRepository() {
 *         DefaultEntityReferenceRepository<Long> repo =
 *             new DefaultEntityReferenceRepository<>(Contact.class, "name", "email");
 *         repo.setAlias("Contact");
 *         repo.setCacheable(true); // Enable caching for better performance
 *         return repo;
 *     }
 * }
 * }</pre>
 *
 * @param <ID> the type of the entity identifier, must be serializable
 *
 * @see tools.dynamia.domain.util.DomainUtils
 * @see tools.dynamia.domain.EntityReferenceRepository
 * @see tools.dynamia.domain.DefaultEntityReferenceRepository
 */
public class EntityReference<ID extends Serializable> implements Serializable {

    /**
     * Serial version UID for serialization compatibility
     */
    private static final long serialVersionUID = 3969866122612228133L;

    /**
     * The unique identifier of the referenced entity
     */
    private ID id;

    /**
     * The display name of the referenced entity
     */
    private String name;

    /**
     * The fully qualified class name of the referenced entity
     */
    private String className;

    /**
     * An optional description of the referenced entity
     */
    private String description;

    /**
     * Additional custom attributes for the referenced entity
     */
    private final Map<String, Object> attributes = new HashMap<>();

    /**
     * Default constructor for creating an empty entity reference.
     * Fields can be set using the provided setter methods.
     */
    public EntityReference() {
    }

    /**
     * Constructs an entity reference with the specified ID and class name.
     *
     * @param id the unique identifier of the referenced entity
     * @param className the fully qualified class name of the referenced entity
     */
    public EntityReference(ID id, String className) {
        super();
        this.id = id;
        this.className = className;
    }

    /**
     * Constructs an entity reference with the specified ID, class name, and display name.
     *
     * @param id the unique identifier of the referenced entity
     * @param className the fully qualified class name of the referenced entity
     * @param name the display name of the referenced entity
     */
    public EntityReference(ID id, String className, String name) {
        super();
        this.id = id;
        this.name = name;
        this.className = className;
    }

    /**
     * Gets the unique identifier of the referenced entity.
     *
     * @return the entity ID
     */
    public ID getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the referenced entity.
     *
     * @param id the entity ID to set
     */
    public void setId(ID id) {
        this.id = id;
    }

    /**
     * Gets the display name of the referenced entity.
     *
     * @return the entity name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name of the referenced entity.
     *
     * @param name the entity name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the fully qualified class name of the referenced entity.
     *
     * @return the entity class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the fully qualified class name of the referenced entity.
     *
     * @param className the entity class name to set
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Gets the description of the referenced entity.
     *
     * @return the entity description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the referenced entity.
     *
     * @param description the entity description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Adds a custom attribute to the entity reference.
     * This allows storing additional metadata without modifying the entity structure.
     *
     * @param name the attribute name
     * @param value the attribute value
     */
    public void addAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    /**
     * Retrieves a custom attribute from the entity reference.
     *
     * @param name the attribute name
     * @return the attribute value, or null if not found
     */
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * Gets all custom attributes of the entity reference.
     *
     * @return a map containing all attributes
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Returns the string representation of the entity reference, which is the entity's name.
     *
     * @return the name of the referenced entity
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Generates a hash code based on the entity's ID and class name.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((className == null) ? 0 : className.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    /**
     * Compares this entity reference with another object for equality.
     * Two entity references are considered equal if they have the same ID and class name.
     *
     * @param obj the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EntityReference other = (EntityReference) obj;
        if (className == null) {
            if (other.className != null) {
                return false;
            }
        } else if (!className.equals(other.className)) {
            return false;
        }
        if (id == null) {
            return other.id == null;
        } else return id.equals(other.id);
    }

}
