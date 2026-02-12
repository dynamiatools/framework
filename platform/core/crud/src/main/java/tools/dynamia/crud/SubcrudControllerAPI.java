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

package tools.dynamia.crud;

import java.util.List;

/**
 * Specialized CRUD controller API for managing child entities within a parent-child relationship context.
 * This interface extends {@link CrudControllerAPI} to provide additional functionality for handling
 * sub-entities (children) that belong to a parent entity, commonly used in master-detail UI patterns.
 *
 * <p>A subcrud controller manages collections of child entities and tracks pending changes
 * (creates, updates, deletes) that are typically committed together with the parent entity.
 * This allows for transactional consistency when saving complex object graphs.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Managing order items within an order
 * SubcrudControllerAPI<OrderItem> itemsController = new SubcrudController<>(OrderItem.class);
 * itemsController.setParentEntity(order);
 * itemsController.doCreate();
 *
 * // Add new item
 * OrderItem newItem = itemsController.getEntity();
 * newItem.setProduct(product);
 * newItem.setQuantity(5);
 * itemsController.doSave();
 *
 * // All changes are tracked for batch processing
 * List<OrderItem> toCreate = itemsController.getToBeCreatedEntities();
 * List<OrderItem> toUpdate = itemsController.getToBeUpdatedEntities();
 * }</pre>
 *
 * @param <E> the type of the child entity managed by this subcrud controller
 */
public interface SubcrudControllerAPI<E> extends CrudControllerAPI<E> {

    /**
     * Gets the parent entity that owns the child entities managed by this controller.
     * The parent entity represents the "master" in a master-detail relationship.
     *
     * @return the parent entity instance, or null if no parent is set
     */
    Object getParentEntity();

    /**
     * Gets the name or property identifier of the parent entity.
     * This is typically used for UI labels, field binding, or navigation purposes.
     *
     * @return the parent entity name or property identifier
     */
    String getParentName();

    /**
     * Gets the name or property identifier of the child entities collection.
     * This represents the collection property in the parent entity that holds the children.
     *
     * @return the children collection name or property identifier
     */
    String getChildrenName();

    /**
     * Gets the list of existing child entities that have been modified and need to be updated.
     * These entities will be persisted when the parent entity is saved.
     *
     * @return a list of entities pending update operations, or an empty list if none
     */
    List<E> getToBeUpdatedEntities();

    /**
     * Gets the list of new child entities that have been created and need to be persisted.
     * These entities will be inserted into the database when the parent entity is saved.
     *
     * @return a list of entities pending creation, or an empty list if none
     */
    List<E> getToBeCreatedEntities();

    /**
     * Gets the list of existing child entities that have been marked for deletion.
     * These entities will be removed from the database when the parent entity is saved.
     *
     * @return a list of entities pending deletion, or an empty list if none
     */
    List<E> getToBeDeletedEntities();
}
