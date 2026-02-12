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
package tools.dynamia.zk.crud;

import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.commons.ValueWrapper;
import tools.dynamia.commons.reflect.ReflectionException;
import tools.dynamia.crud.CrudControllerException;
import tools.dynamia.crud.SubcrudControllerAPI;
import tools.dynamia.domain.query.DataSet;
import tools.dynamia.domain.query.ListDataSet;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.ValidatorService;
import tools.dynamia.domain.util.CrudServiceListener;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.integration.Containers;
import tools.dynamia.zk.viewers.table.TableView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Specialized CRUD controller implementation for managing child entities in master-detail relationships.
 * This class extends {@link CrudController} to handle collections of child entities that belong to a parent entity,
 * providing support for deferred persistence, batch operations, and bidirectional relationship management.
 *
 * <p>The SubcrudController tracks changes to child entities (creates, updates, deletes) and defers their
 * persistence until the parent entity is saved. This ensures transactional consistency and proper
 * relationship management in complex object graphs. It automatically handles bidirectional relationships
 * between parent and child entities.</p>
 *
 * <h3>Key Features</h3>
 * <ul>
 *   <li>Deferred persistence - Changes are tracked and committed with parent entity</li>
 *   <li>Automatic bidirectional relationship management (parent.getChildren() ↔ child.setParent())</li>
 *   <li>Batch operations for creates, updates, and deletes</li>
 *   <li>In-memory entity management for unsaved parent entities</li>
 *   <li>Nested subcrud support for multi-level master-detail hierarchies</li>
 *   <li>Integration with ZK TableView for UI synchronization</li>
 * </ul>
 *
 * <h3>Basic Usage</h3>
 * <pre>{@code
 * // Creating a subcrud controller for order items
 * Order order = new Order();
 * SubcrudController<OrderItem> itemsController = new SubcrudController<>(
 *     OrderItem.class,
 *     order,           // parent entity
 *     "order",         // parent property name in OrderItem class
 *     "items"          // children collection name in Order class
 * );
 *
 * // Add new item (deferred until parent is saved)
 * itemsController.doCreate();
 * OrderItem item = itemsController.getEntity();
 * item.setProduct(product);
 * item.setQuantity(5);
 * itemsController.doSave(); // Tracked, not persisted yet
 *
 * // When parent is saved, all child operations are committed
 * itemsController.doCreates();  // Persists all new items
 * itemsController.doUpdates();  // Updates modified items
 * itemsController.doDeletes();  // Removes deleted items
 * }</pre>
 *
 * <h3>View Descriptor Configuration</h3>
 * <p>Subcrud controllers can be automatically configured in view descriptors:</p>
 * <pre>{@code
 * # OrderCrud.yml
 * view: crud
 * beanClass: com.myapp.domain.Order
 *
 * fields:
 *   customer:
 *   orderDate:
 *   items:  # Collection field automatically creates SubcrudController
 *     component: crudview
 *     params:
 *       parentName: order
 *       childrenName: items
 * }</pre>
 *
 * <h3>Integration with Parent Controller</h3>
 * <pre>{@code
 * // In a custom OrderController
 * public class OrderController extends CrudController<Order> {
 *
 *     private SubcrudController<OrderItem> itemsController;
 *
 *     @Override
 *     protected void afterInit() {
 *         itemsController = new SubcrudController<>(
 *             OrderItem.class,
 *             getEntity(),
 *             "order",
 *             "items"
 *         );
 *         addSubcrudController(itemsController);
 *     }
 *
 *     @Override
 *     protected void afterSave() {
 *         // Child operations are automatically handled by parent controller
 *         super.afterSave();
 *     }
 * }
 * }</pre>
 *
 * <h3>Bidirectional Relationship Management</h3>
 * <p>The controller automatically maintains both sides of the relationship:</p>
 * <ul>
 *   <li><strong>Parent → Child:</strong> Adds child to parent's collection (parent.getItems().add(child))</li>
 *   <li><strong>Child → Parent:</strong> Sets parent reference in child (child.setOrder(parent))</li>
 * </ul>
 *
 * <h3>Change Tracking</h3>
 * <p>The controller maintains three separate lists:</p>
 * <ul>
 *   <li>{@link #getToBeCreatedEntities()} - New child entities pending insertion</li>
 *   <li>{@link #getToBeUpdatedEntities()} - Modified existing entities pending update</li>
 *   <li>{@link #getToBeDeletedEntities()} - Entities marked for deletion</li>
 * </ul>
 *
 * <h3>Transaction Handling</h3>
 * <p>By default, subcrud operations do not create new transactions (saveWithNewTransaction = false).
 * They participate in the parent entity's transaction, ensuring ACID compliance for the entire
 * object graph.</p>
 *
 * @param <E> the type of the child entity managed by this subcrud controller
 * @see CrudController
 * @see SubcrudControllerAPI
 * @see tools.dynamia.domain.services.CrudService
 */
public class SubcrudController<E> extends CrudController<E> implements SubcrudControllerAPI<E> {

    /**
     * Serial version UID for serialization compatibility.
     */
    private static final long serialVersionUID = 2791457285184056200L;

    /**
     * Name of the parent property in the child entity class.
     * Used to set the parent reference (e.g., "order" in OrderItem.setOrder()).
     */
    private final String parentName;

    /**
     * The parent entity that owns the child entities managed by this controller.
     */
    private Object parent;

    /**
     * List of existing child entities that have been modified and need to be updated.
     * These entities are persisted when the parent entity is saved.
     */
    private final List<E> toBeUpdatedEntities = new ArrayList<>();

    /**
     * List of new child entities that have been created and need to be persisted.
     * These entities are inserted into the database when the parent entity is saved.
     */
    private final List<E> toBeCreatedEntities = new ArrayList<>();

    /**
     * List of existing child entities that have been marked for deletion.
     * These entities are removed from the database when the parent entity is saved.
     */
    private final List<E> toBeDeletedEntities = new ArrayList<>();

    /**
     * Name of the children collection property in the parent entity class.
     * Used to add children to the parent (e.g., "items" in Order.getItems()).
     */
    private final String childrenName;

    /**
     * Creates a subcrud controller for managing child entities.
     * The entity class will be inferred from generic type or must be set later.
     *
     * @param parent the parent entity that owns the children
     * @param parentName the name of the parent property in the child entity class
     * @param childrenName the name of the children collection property in the parent entity class
     */
    public SubcrudController(Object parent, String parentName, String childrenName) {
        this(null, parent, parentName, childrenName);
    }

    /**
     * Creates a subcrud controller for managing child entities of a specific class.
     *
     * @param entityClass the class of the child entity to manage
     * @param parent the parent entity that owns the children
     * @param parentName the name of the parent property in the child entity class (e.g., "order")
     * @param childrenName the name of the children collection property in the parent entity class (e.g., "items")
     */
    public SubcrudController(Class<E> entityClass, Object parent, String parentName, String childrenName) {
        super(entityClass);
        this.parentName = parentName;
        this.parent = parent;
        this.childrenName = childrenName;
        inspectParentChildrens();
        setSaveWithNewTransaction(false);
    }

    /**
     * {@inheritDoc}
     * <p>This implementation creates a new entity and automatically establishes
     * the relationship with the parent entity by calling {@link #relateChildParent(Object, Object)}.</p>
     */
    @Override
    public void newEntity() {
        super.newEntity();
        if (parent != null) {
            relateChildParent(getEntity(), parent);
        }
    }

    /**
     * Inspects the parent entity's children collection for unsaved entities.
     * If the parent is new (not yet persisted) and has children, those children are
     * added to the toBeCreated list for later persistence. This handles scenarios where
     * entities are created programmatically before being managed by the controller.
     */
    private void inspectParentChildrens() {
        if (parent != null && DomainUtils.findEntityId(parent) == null && childrenName != null) {
            @SuppressWarnings("unchecked") Collection<E> children = (Collection<E>) ObjectOperations.invokeGetMethod(parent, childrenName);
            if (children != null) {
                for (E child : children) {
                    if (DomainUtils.findEntityId(child) == null) {
                        toBeCreatedEntities.add(child);
                    }
                }
                children.clear();
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>This implementation automatically adds the parent entity as a query parameter
     * to filter children by their parent relationship.</p>
     */
    @Override
    protected void beforeQuery() {
        setParemeter(parentName, parent);
    }

    /**
     * {@inheritDoc}
     * <p>This implementation only executes the query if the parent entity has been persisted.
     * For unsaved parents, it returns an empty result set since children cannot exist in the
     * database without a persisted parent.</p>
     */
    @Override
    public void query() {
        if (DomainUtils.findEntityId(parent) != null) {
            super.query();
        } else {
            //noinspection unchecked
            setQueryResult(new ListDataSet(Collections.emptyList()));
        }
    }

    /**
     * {@inheritDoc}
     * <p>This implementation handles two scenarios:</p>
     * <ul>
     *   <li><strong>Parent not persisted:</strong> Validates the entity, establishes relationships,
     *       and adds it to the appropriate tracking list (toBeCreated or toBeUpdated) for later persistence.</li>
     *   <li><strong>Parent already persisted:</strong> Establishes relationships if needed and immediately
     *       persists the entity using the parent's save method.</li>
     * </ul>
     * <p>Validation is performed via {@link ValidatorService} before any relationship creation.</p>
     */
    @Override
    public void save() {
        ValidatorService validatorService = Containers.get().findObject(ValidatorService.class);
        validatorService.validate(getEntity());

        if (DomainUtils.findEntityId(parent) == null) {
            fireCrudListener();

            //add child to parent after validated
            createRelationship(getEntity(), parent);

            if (DomainUtils.findEntityId(getEntity()) == null) {
                if (!toBeCreatedEntities.contains(getEntity())) {
                    toBeCreatedEntities.add(getEntity());
                }
            } else if (!toBeUpdatedEntities.contains(getEntity())) {
                toBeUpdatedEntities.add(getEntity());
            }
        } else {
            if (DomainUtils.findEntityId(getEntity()) == null) {
                createRelationship(getEntity(), parent);
            }
            super.save();
        }

    }

    /**
     * {@inheritDoc}
     * <p>This implementation handles two scenarios:</p>
     * <ul>
     *   <li><strong>Parent not persisted:</strong> Moves the entity between tracking lists based on its state:
     *       <ul>
     *         <li>If the entity has an ID, moves from toBeUpdated to toBeDeleted</li>
     *         <li>If the entity is new, removes from toBeCreated (no database deletion needed)</li>
     *       </ul>
     *       Also removes the entity from the UI table view if present.
     *   </li>
     *   <li><strong>Parent already persisted:</strong> Immediately deletes the entity from the database
     *       using the parent's delete method.</li>
     * </ul>
     */
    @Override
    public void delete() {
        Serializable parentId = DomainUtils.findEntityId(parent);
        if (parentId == null) {
            if (DomainUtils.findEntityId(getSelected()) != null) {
                toBeUpdatedEntities.remove(getSelected());
                toBeDeletedEntities.add(getSelected());
            } else {
                toBeCreatedEntities.remove(getSelected());
            }

            if (dataSetView instanceof TableView tableView) {
                tableView.getSelectedItem().detach();
            }

        } else {
            super.delete();
        }

    }

    /**
     * Sets the parent entity for this subcrud controller.
     * If the current entity exists, it automatically establishes the relationship with the new parent.
     *
     * @param parentEntity the new parent entity
     */
    protected void setParentEntity(Object parentEntity) {
        this.parent = parentEntity;
        if (getEntity() != null) {
            relateChildParent(getEntity(), parent);
        }
    }

    /**
     * Creates a complete bidirectional relationship between a child entity and its parent.
     * This method calls both {@link #relateChildParent(Object, Object)} and {@link #relateParentChild(Object, Object)}
     * to ensure both sides of the relationship are properly established.
     *
     * @param newChild the child entity to relate
     * @param parent the parent entity to relate
     */
    private void createRelationship(E newChild, Object parent) {
        relateChildParent(newChild, parent);
        relateParentChild(newChild, parent);
    }

    /**
     * Establishes the parent-to-child side of the relationship by adding the child to the parent's collection.
     * This is equivalent to: {@code parent.getChildren().add(child);}
     *
     * <p>The method uses reflection to invoke the getter method for the children collection property
     * (specified by {@link #childrenName}) and adds the child entity to that collection.</p>
     *
     * @param newChild the child entity to add to the parent's collection
     * @param parent the parent entity that owns the collection
     */
    protected void relateParentChild(E newChild, Object parent) {
        try {

            Object object = ObjectOperations.invokeGetMethod(parent, childrenName);
            if (object != null && object instanceof Collection children) {
                //noinspection unchecked
                children.add(newChild);

            }

        } catch (Exception e) {
            logger.error("Cannot create relationship parent <-> child in SubcrudController " + getEntityClass() + ". Check children name ["
                    + childrenName + "]", e);

        }

    }

    /**
     * Establishes the child-to-parent side of the relationship by setting the parent reference in the child.
     * This is equivalent to: {@code child.setParent(value);}
     *
     * <p>The method uses reflection to invoke the setter method for the parent property
     * (specified by {@link #parentName}) in the child entity. If the direct reflection fails due to
     * proxy or inheritance issues, it attempts to use the parent's superclass.</p>
     *
     * @param newChild the child entity that will reference the parent
     * @param parent the parent entity to set in the child
     * @throws CrudControllerException if the relationship cannot be established and all fallback attempts fail
     */
    protected void relateChildParent(E newChild, Object parent) {
        try {
            ObjectOperations.invokeSetMethod(newChild, parentName, parent);
        } catch (ReflectionException e) {
            if (e.getCause().getClass() == NoSuchMethodException.class) {
                if (parent instanceof ValueWrapper) {
                    parent = ((ValueWrapper) parent).value();
                }

                if (parent.getClass().getSuperclass() != Object.class) {
                    createRelationship(newChild, new ValueWrapper(parent, parent.getClass().getSuperclass()));
                } else {
                    throw new CrudControllerException("Cannot create relationship parent <-> child in SubcrudController "
                            + getEntityClass() + ". Check parent name [" + parentName + "]", e);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>This implementation adds in-memory entities (toBeCreated and toBeUpdated) to the query results
     * before displaying them in the UI. This ensures that unsaved changes are visible in the table view.</p>
     */
    @Override
    public void setQueryResult(DataSet queryResult) {
        addInMemoryResults();
        super.setQueryResult(queryResult);
    }

    /**
     * Adds in-memory entities to the table view's default values.
     * This method combines entities from toBeCreated and toBeUpdated lists and sets them
     * as default values in the TableView, making unsaved changes visible in the UI.
     */
    private void addInMemoryResults() {
        if (dataSetView instanceof TableView tableView) {
            List<E> defaultValues = new ArrayList<>();
            defaultValues.addAll(toBeCreatedEntities);
            defaultValues.addAll(toBeUpdatedEntities);

            //noinspection unchecked
            tableView.setDefaultValue(defaultValues);
        }

    }

    /**
     * Persists all new child entities that have been created but not yet saved to the database.
     * This method is typically called when the parent entity is saved.
     *
     * <p>After persisting all entities, it recursively calls doCreates() on all nested subcrud controllers
     * to handle multi-level master-detail hierarchies.</p>
     *
     * <p>Only entities without an ID (new entities) are persisted. The toBeCreatedEntities list
     * is cleared after successful persistence.</p>
     */
    public void doCreates() {
        for (E entity : toBeCreatedEntities) {
            if (DomainUtils.findEntityId(entity) == null) {
                crudService.create(entity);
            }
        }
        toBeCreatedEntities.clear();

        for (SubcrudController subCrud : getSubcontrollers()) {
            subCrud.doCreates();
        }
    }

    /**
     * Updates all modified child entities that have been changed but not yet saved to the database.
     * This method is typically called when the parent entity is saved.
     *
     * <p>After updating all entities, it recursively calls doUpdates() on all nested subcrud controllers
     * to handle multi-level master-detail hierarchies.</p>
     *
     * <p>The toBeUpdatedEntities list is cleared after successful updates.</p>
     */
    public void doUpdates() {
        for (E entity : toBeUpdatedEntities) {
            crudService.update(entity);
        }
        toBeUpdatedEntities.clear();
        for (SubcrudController subCrud : getSubcontrollers()) {
            subCrud.doUpdates();
        }
    }

    /**
     * Deletes all child entities that have been marked for deletion.
     * This method is typically called when the parent entity is saved.
     *
     * <p>This method first recursively calls doDeletes() on all nested subcrud controllers
     * to ensure child entities are deleted before their parents (respecting foreign key constraints).
     * Then it deletes the entities in the toBeDeletedEntities list.</p>
     *
     * <p>The toBeDeletedEntities list is cleared after successful deletion.</p>
     */
    public void doDeletes() {
        for (SubcrudController subCrud : getSubcontrollers()) {
            subCrud.doDeletes();
        }

        for (E entity : toBeDeletedEntities) {
            crudService.delete(entity.getClass(), DomainUtils.findEntityId(entity));
        }
        toBeDeletedEntities.clear();

    }

    /**
     * Creates a list of entity IDs from a collection of entities.
     * This utility method extracts the primary key from each entity in the list.
     *
     * @param objects the list of entities
     * @return a list of Serializable IDs
     */
    private List<Serializable> createIdList(List<E> objects) {
        List<Serializable> ids = new ArrayList<>();

        for (E entity : objects) {
            ids.add(DomainUtils.findEntityId(entity));
        }
        return ids;
    }

    /**
     * {@inheritDoc}
     * <p>This implementation automatically calls {@link #beforeQuery()} to set up parent filtering.</p>
     */
    @Override
    public QueryParameters getParams() {
        beforeQuery();
        return super.getParams();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getParentEntity() {
        return parent;
    }

    /**
     * Fires CRUD service listeners before creating an entity.
     * This method notifies all registered {@link CrudServiceListener} instances
     * that a new entity is about to be created.
     */
    private void fireCrudListener() {
        for (CrudServiceListener listener : Containers.get().findObjects(CrudServiceListener.class)) {
            try {
                //noinspection unchecked
                listener.beforeCreate(getEntity());
            } catch (ClassCastException ignored) {
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<E> getToBeUpdatedEntities() {
        return toBeUpdatedEntities;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<E> getToBeCreatedEntities() {
        return toBeCreatedEntities;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<E> getToBeDeletedEntities() {
        return toBeDeletedEntities;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParentName() {
        return parentName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getChildrenName() {
        return childrenName;
    }
}
