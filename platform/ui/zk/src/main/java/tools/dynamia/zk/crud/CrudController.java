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

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.WrongValuesException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zul.Window;
import org.zkoss.zul.ext.Paginal;
import tools.dynamia.commons.*;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.crud.CrudControllerAPI;
import tools.dynamia.crud.QueryProjectionBuilder;
import tools.dynamia.domain.CrudServiceException;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.jdbc.QueryInterruptedException;
import tools.dynamia.domain.query.DataPaginator;
import tools.dynamia.domain.query.DataSet;
import tools.dynamia.domain.query.ListDataSet;
import tools.dynamia.domain.query.QueryExecuter;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.domain.util.QueryBuilder;
import tools.dynamia.integration.Containers;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.viewers.DataSetView;
import tools.dynamia.zk.util.ZKUtil;
import tools.dynamia.zk.viewers.table.TableView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ZK Framework implementation of the CRUD controller for managing entity operations in web applications.
 * This class provides a complete lifecycle management for CRUD operations including create, read, update,
 * delete, query execution, pagination, sorting, and integration with ZK UI components.
 *
 * <p>The controller acts as a mediator between the user interface (ZK components) and the business layer
 * (CrudService), handling user interactions, validation, error handling, and UI feedback. It supports
 * advanced features like subcrud controllers for master-detail relationships, query projections,
 * lifecycle hooks (before/after events), and automatic transaction management.</p>
 *
 * <h3>Basic Usage</h3>
 * <pre>{@code
 * // In Java code
 * CrudController<Customer> controller = new CrudController<>(Customer.class, crudService);
 * controller.doCreate();
 * controller.getEntity().setName("John Doe");
 * controller.doSave();
 * }</pre>
 *
 * <h3>Custom Controller in View Descriptors</h3>
 * <p>You can specify a custom controller class that extends CrudController in your CRUD view descriptor.
 * This allows you to override methods and add custom behavior specific to your entity.</p>
 *
 * <pre>{@code
 * # ContactCrud.yml
 * view: crud
 * beanClass: com.myapp.domain.Contact
 * controller: com.myapp.controller.ContactController
 *
 * fields:
 *   name:
 *   email:
 *   phone:
 * }</pre>
 *
 * <p>Your custom controller implementation:</p>
 * <pre>{@code
 * package com.myapp.controller;
 *
 * import tools.dynamia.zk.crud.CrudController;
 * import com.myapp.domain.Contact;
 *
 * public class ContactController extends CrudController<Contact> {
 *
 *     @Override
 *     protected void beforeSave() {
 *         // Custom validation or business logic before saving
 *         if (getEntity().getEmail() == null) {
 *             throw new ValidationError("Email is required");
 *         }
 *     }
 *
 *     @Override
 *     protected void afterSave() {
 *         // Custom actions after successful save
 *         sendWelcomeEmail(getEntity());
 *     }
 *
 *     private void sendWelcomeEmail(Contact contact) {
 *         // Send email logic
 *     }
 * }
 * }</pre>
 *
 * <h3>Lifecycle Hooks</h3>
 * <p>The controller provides several lifecycle hooks that can be overridden to customize behavior:</p>
 * <ul>
 *   <li>{@link #beforeCreate()} / {@link #afterCreate()} - Called when creating a new entity</li>
 *   <li>{@link #beforeSave()} / {@link #afterSave()} - Called when saving an entity</li>
 *   <li>{@link #beforeEdit()} / {@link #afterEdit()} - Called when editing an entity</li>
 *   <li>{@link #beforeDelete()} / {@link #afterDelete()} - Called when deleting an entity</li>
 *   <li>{@link #beforeQuery()} / {@link #afterQuery()} - Called when executing queries</li>
 *   <li>{@link #afterPageLoaded()} - Called after the ZK page is loaded</li>
 *   <li>{@link #afterInit()} - Called after controller initialization</li>
 * </ul>
 *
 * <h3>Master-Detail Relationships</h3>
 * <p>The controller supports subcrud controllers for managing child entities:</p>
 * <pre>{@code
 * CrudController<Order> orderController = new CrudController<>(Order.class);
 * SubcrudController<OrderItem> itemsController = new SubcrudController<>(OrderItem.class);
 * orderController.addSubcrudController(itemsController);
 * }</pre>
 *
 * <h3>Features</h3>
 * <ul>
 *   <li>Automatic CRUD operation handling with minimal configuration</li>
 *   <li>Integration with ZK UI components (tables, forms, paginators)</li>
 *   <li>Query-by-example support</li>
 *   <li>Query projection for optimized database queries</li>
 *   <li>Pagination and sorting capabilities</li>
 *   <li>Transaction management with configurable strategies</li>
 *   <li>Validation and error handling with user-friendly messages</li>
 *   <li>Default entity values for new instances</li>
 *   <li>Confirmation dialogs for critical operations</li>
 *   <li>Event listeners and callbacks</li>
 * </ul>
 *
 * @param <E> the type of the domain entity managed by this controller
 * @author Ing. Mario Serrano Leones
 * @see CrudControllerAPI
 * @see SubcrudController
 * @see CrudService
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class CrudController<E> extends SelectorComposer implements Serializable, CrudControllerAPI<E> {

    /**
     * Serial version UID for serialization compatibility.
     */
    private static final long serialVersionUID = 5762960709271600367L;

    /**
     * The current entity being managed (created or edited).
     */
    private E entity;

    /**
     * Example entity used for query-by-example operations.
     */
    private E example;

    /**
     * Currently selected entity from query results.
     */
    private E selected;

    /**
     * Dataset containing the results of the last query operation.
     */
    private DataSet queryResult;

    /**
     * Query parameters used for filtering, sorting, and pagination.
     */
    private QueryParameters params;

    /**
     * The class type of the entity managed by this controller.
     */
    private Class<E> entityClass;

    /**
     * Logging service for controller operations.
     */
    protected LoggingService logger;

    /**
     * CRUD service for database operations.
     */
    protected CrudService crudService;

    /**
     * Flag indicating whether to automatically clear the page after save operations.
     */
    private boolean autoClearPage = false;

    /**
     * Flag indicating whether to automatically reload the entity from database after operations.
     */
    private boolean autoReloadEntity = true;

    /**
     * List of subcrud controllers for managing child entities in master-detail relationships.
     */
    private final List<SubcrudController> subcontrollers = new ArrayList<>();

    /**
     * Human-readable name of the entity, used in user messages.
     */
    private String name;

    /**
     * Current dialog window, if the controller is being used in a modal dialog.
     */
    private Window currentDialog;

    /**
     * Bean sorter for ordering query results.
     */
    private final BeanSorter sorter = new BeanSorter();

    /**
     * Data paginator for handling paginated query results.
     */
    private DataPaginator dataPaginator;

    /**
     * ZK paginator component for UI pagination controls.
     */
    private Paginal paginator;

    /**
     * ZK dataset view component (table, grid, etc.) displaying query results.
     */
    protected DataSetView dataSetView;

    /**
     * Flag indicating whether to always use query-by-example instead of regular find.
     */
    private boolean alwaysFindByExample = false;

    /**
     * Flag indicating if the last save operation was successful.
     */
    private boolean saved;

    /**
     * Flag indicating if the last delete operation was successful.
     */
    private boolean deleted;

    /**
     * Map of default values to be applied when creating new entity instances.
     */
    private final Map<String, Object> defaultEntityValues = new HashMap<>();

    /**
     * Flag indicating whether to show a confirmation dialog before saving.
     */
    private boolean confirmBeforeSave;

    /**
     * Callback to be executed after successful save operations.
     */
    private Callback onSaveCallback;

    /**
     * Flag indicating whether to use query projections for optimized queries.
     */
    private boolean queryProjection;

    /**
     * Default query parameters applied to all queries.
     */
    private QueryParameters defaultParameters;

    /**
     * Custom attributes map for storing additional controller state.
     */
    private Map<String, Object> attributes = new HashMap<>();

    /**
     * Flag indicating whether to execute save operations in a new transaction.
     */
    private boolean saveWithNewTransaction = true;

    /**
     * Internationalized messages for controller operations.
     */
    private final ClassMessages messages = ClassMessages.get(CrudController.class);


    /**
     * Default constructor. The entity class must be set later or will be inferred from generic type.
     */
    public CrudController() {
        this(null);
    }

    /**
     * Creates a new CRUD controller for the specified entity class.
     *
     * @param entityClass the class of the entity to manage
     */
    public CrudController(Class entityClass) {
        this.entityClass = entityClass;
        init();
    }

    /**
     * Creates a new CRUD controller with a specific entity class and CRUD service.
     *
     * @param entityClass the class of the entity to manage
     * @param crudService the service for database operations
     */
    public CrudController(Class entityClass, CrudService crudService) {
        this.entityClass = entityClass;
        this.crudService = crudService;
        init();
    }

    /**
     * Adds a subcrud controller for managing child entities in a master-detail relationship.
     * The parent entity is automatically set to this controller's current entity.
     *
     * @param subController the subcrud controller to add
     */
    public void addSubcrudController(SubcrudController subController) {
        subController.setParentEntity(getEntity());
        subcontrollers.add(subController);
    }

    @Override
    public boolean isConfirmBeforeSave() {
        return confirmBeforeSave;
    }

    @Override
    public void setConfirmBeforeSave(boolean confirmBeforeSave) {
        this.confirmBeforeSave = confirmBeforeSave;
    }

    /**
     * Gets the list of registered subcrud controllers.
     *
     * @return the list of subcrud controllers
     */
    protected List<SubcrudController> getSubcontrollers() {
        return subcontrollers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCrudService(CrudService crudService) {
        this.crudService = crudService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CrudService getCrudService() {
        return crudService;
    }

    /**
     * Callback method invoked after the ZK component is composed.
     * This method initializes the entity from arguments and sets up the dialog if present.
     *
     * @param comp the ZK component
     * @throws Exception if an error occurs during composition
     */
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        Object ent = getArg("entity");

        if (ent != null && ent.getClass().equals(entityClass)) {
            setEntity((E) ent);
            reloadEntity();
        }

        Object dialog = getArg("dialog");

        if (dialog != null && dialog instanceof Window) {
            this.currentDialog = (Window) dialog;
        }

        afterPageLoaded();
    }

    /**
     * {@inheritDoc}
     * <p>This implementation uses the configured CrudService to persist the current entity.
     * If {@link #isSaveWithNewTransaction()} is true, the operation executes in a new transaction.</p>
     */
    @Override
    public void save() {
        logger.debug("Saving entity " + entityClass);
        if (isSaveWithNewTransaction()) {
            crudService.executeWithinTransaction(() -> crudService.save(entity, DomainUtils.findEntityId(entity)));
        } else {
            crudService.save(entity, DomainUtils.findEntityId(entity));
        }
    }

    /**
     * {@inheritDoc}
     * <p>This implementation deletes the currently selected entity using the configured CrudService.</p>
     */
    @Override
    public void delete() {
        if (DomainUtils.isEntity(getSelected())) {
            crudService.delete(getSelected().getClass(), DomainUtils.findEntityId(getSelected()));
        } else {
            crudService.delete(getSelected());
        }
    }

    /**
     * {@inheritDoc}
     * <p>This implementation supports multiple query strategies:</p>
     * <ul>
     *   <li>Query executor pattern if entity implements {@link QueryExecuter}</li>
     *   <li>Query-by-example if {@link #alwaysFindByExample} is enabled</li>
     *   <li>Query projection if {@link #isQueryProjection()} is enabled</li>
     *   <li>Standard find operation using entity class</li>
     * </ul>
     */
    @Override
    public void query() {

        try {
            if (ObjectOperations.isAssignable(entityClass, QueryExecuter.class)) {
                QueryExecuter queryExecutor = (QueryExecuter) ObjectOperations.newInstance(entityClass);
                setQueryResult(new ListDataSet(queryExecutor.executeQuery(crudService, getParams())));
            } else if (alwaysFindByExample) {
                setQueryResult(new ListDataSet(crudService.findByExample(getExample(), getParams())));
            } else if (isQueryProjection()) {
                var queryProjection = createQueryProjection();
                var projectionResult = crudService.executeQuery(queryProjection, getParams());
                setQueryResult(new ListDataSet(projectionResult));
            } else {
                setQueryResult(new ListDataSet(crudService.find(entityClass, getParams())));
            }

        } catch (CrudServiceException e) {
            if (getQueryResult() == null || getQueryResult().getSize() <= 0) {
                UIMessages.showMessage(messages.get("noRecordsFound"));
            }
        } catch (QueryInterruptedException e) {
            logger.error(e);
            UIMessages.showMessageDialog(messages.get("searchInterruptedMessage"), messages.get("error"), MessageType.ERROR);
        } catch (ValidationError error) {
            UIMessages.showMessage(error.getMessage(), MessageType.ERROR);
        } catch (Exception e) {
            logger.error(e);
            UIMessages.showException(messages.get("searchErrorMessage", e.getMessage()), e);
        }
    }

    /**
     * Creates a query projection from the current view descriptor for optimized database queries.
     * This reduces the amount of data fetched by only selecting the fields displayed in the view.
     *
     * @return a query builder configured with the projection
     */
    private QueryBuilder createQueryProjection() {
        return QueryProjectionBuilder.buildFromViewDescriptor(getEntityClass(), dataSetView.getViewDescriptor(), getParams());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void delete(E entity) {
        setSelected(entity);
        doDelete();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void edit(E entity) {
        setSelected(entity);
        doEdit();
    }

    /**
     * {@inheritDoc}
     * <p>Creates a new instance and applies default values from {@link #getDefaultEntityValues()}.</p>
     */
    @Override
    public void newEntity() {
        if (entityClass != null) {
            try {
                entity = ObjectOperations.newInstance(entityClass);
                ObjectOperations.setupBean(entity, getDefaultEntityValues());
            } catch (Exception ex) {
                logger.error("Error creating new entity", ex);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void newExample() {
        if (entityClass != null) {
            try {
                example = ObjectOperations.newInstance(entityClass);
            } catch (Exception ex) {
                logger.error("Error creating new example object", ex);
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>This implementation reloads the entity by ID if {@link #autoReloadEntity} is true.</p>
     */
    @Override
    public void reloadEntity() {
        if (entity != null && DomainUtils.findEntityId(entity) != null && autoReloadEntity) {
            entity = crudService.findSingle(entityClass, "id", DomainUtils.findEntityId(entity));
            autoReloadEntity = true;
        }
    }

    /**
     * Closes the current dialog window if one is active.
     * This method is called after successful save operations when using modal dialogs.
     */
    public void closeCurrentDialog() {
        if (currentDialog != null) {
            Events.postEvent(Events.ON_CLOSE, currentDialog, this);
            currentDialog.detach();
            currentDialog.setVisible(false);
            currentDialog = null;
        }
    }

    /**
     * Handles exceptions caught during CRUD operations.
     * If the exception is a ValidationError, it displays the message and rethrows it.
     * Otherwise, it logs the exception.
     *
     * @param e the exception that was caught
     * @throws ValidationError if the exception is a validation error
     */
    protected void exceptionCaught(Exception e) {
        if (e instanceof ValidationError) {
            UIMessages.showMessage(e.getMessage(), MessageType.ERROR);
            throw (ValidationError) e;

        } else {
            logger.error(e);

        }
    }

    /**
     * Lifecycle hook called after the ZK page is loaded.
     * Override this method to perform custom initialization after the UI is ready.
     */
    protected void afterPageLoaded() {
    }

    /**
     * Lifecycle hook called before creating a new entity.
     * Override this method to perform custom logic before entity creation.
     */
    protected void beforeCreate() {
    }

    /**
     * Lifecycle hook called after creating a new entity.
     * Override this method to perform custom logic after entity creation.
     */
    protected void afterCreate() {
    }

    /**
     * Lifecycle hook called before saving an entity.
     * Override this method to perform validation or custom logic before persistence.
     *
     * @throws ValidationError if validation fails
     */
    protected void beforeSave() {
    }

    /**
     * Lifecycle hook called after successfully saving an entity.
     * Override this method to perform custom actions after persistence.
     */
    protected void afterSave() {
    }

    /**
     * Lifecycle hook called before executing a query.
     * Override this method to modify query parameters or add custom filters.
     */
    protected void beforeQuery() {
    }

    /**
     * Lifecycle hook called after executing a query.
     * Override this method to process query results or update the UI.
     */
    protected void afterQuery() {
    }

    /**
     * Lifecycle hook called before editing an entity.
     * Override this method to perform custom logic before loading the entity for editing.
     */
    protected void beforeEdit() {
    }

    /**
     * Lifecycle hook called after editing an entity.
     * Override this method to perform custom logic after the entity is loaded for editing.
     */
    protected void afterEdit() {
    }

    /**
     * Lifecycle hook called before deleting an entity.
     * Override this method to perform validation or custom logic before deletion.
     *
     * @throws ValidationError if validation fails
     */
    protected void beforeDelete() {
    }

    /**
     * Lifecycle hook called after successfully deleting an entity.
     * Override this method to perform custom actions after deletion.
     */
    protected void afterDelete() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E getEntity() {
        if (entity == null) {
            newEntity();
        }
        return entity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEntity(E entity) {
        this.entity = entity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E getExample() {
        if (example == null) {
            newExample();
        }
        return example;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E getSelected() {
        return selected;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSaved() {
        return saved;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * Sets whether to always use query-by-example instead of regular find operations.
     *
     * @param alwaysFindByExample true to always use query-by-example
     */
    public void setAlwaysFindByExample(boolean alwaysFindByExample) {
        this.alwaysFindByExample = alwaysFindByExample;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelected(E selected) {
        this.selected = selected;
        if (dataSetView != null) {
            dataSetView.setSelected(selected);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryParameters getParams() {
        if (params == null) {
            params = new QueryParameters();
        }
        return params;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setParams(QueryParameters params) {
        this.params = params;
        if (this.getParams() == null) {
            this.params = new QueryParameters();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getParameter(String param) {
        return getParams().get(param);
    }

    /**
     * Checks if a parameter exists in the query parameters.
     *
     * @param param the parameter name to check
     * @return true if the parameter exists and is not null
     */
    public boolean hasParameter(String param) {
        return getParams().get(param) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParemeter(String key, Object value) {
        if (value != null) {
            params.add(key, value);
        } else {
            params.remove(key);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataSet getQueryResult() {
        return queryResult;
    }

    /**
     * Gets the query results as a List.
     * If the query result is a ListDataSet, returns the underlying list; otherwise returns an empty list.
     *
     * @return the query results as a list
     */
    public List<E> getQueryResultList() {
        if (queryResult instanceof ListDataSet) {
            return ((ListDataSet) queryResult).getData();
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setQueryResult(DataSet queryResult) {
        this.queryResult = queryResult;
        updateDataSetView();
        afterQuery();

        if (isQueryResultEmpty() && dataSetView.isEmpty()) {
            UIMessages.showMessage("La consulta no arrojo resultados", MessageType.WARNING);
        }
    }

    /**
     * Sets the query results from a List.
     * This is a convenience method that wraps the list in a ListDataSet.
     *
     * @param queryResult the list of query results
     */
    public final void setQueryResult(List queryResult) {
        setQueryResult(new ListDataSet(queryResult));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isQueryResultEmpty() {
        if (getQueryResult() == null) {
            return true;
        }
        return getQueryResult().getSize() == 0;
    }

    /**
     * Checks if the page should be automatically cleared after save operations.
     *
     * @return true if auto-clear is enabled
     */
    public boolean isAutoClearPage() {
        return autoClearPage;
    }

    /**
     * Sets whether to automatically clear the page after save operations.
     *
     * @param autoClearPage true to enable auto-clear
     */
    public void setAutoClearPage(boolean autoClearPage) {
        this.autoClearPage = autoClearPage;
    }

    /**
     * Sets the ZK paginator component for pagination controls.
     *
     * @param paginator the paginator component
     */
    public void setPaginator(Paginal paginator) {
        this.paginator = paginator;
    }

    /**
     * Sets the dataset view component that displays query results.
     *
     * @param dataSetView the dataset view component (table, grid, etc.)
     */
    public void setDataSetView(DataSetView dataSetView) {
        this.dataSetView = dataSetView;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataPaginator getDataPaginator() {
        return dataPaginator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BeanSorter getSorter() {
        return sorter;
    }

    /**
     * Gets the current dialog window if the controller is being used in a modal.
     *
     * @return the current dialog window, or null if not in a dialog
     */
    public Window getCurrentDialog() {
        return currentDialog;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<E> getEntityClass() {
        return entityClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEntityClass(Class<E> entityClass) {
        this.entityClass = entityClass;
        if (entityClass != null) {
            name = StringUtils.addSpaceBetweenWords(entityClass.getSimpleName());
        }
    }

    /**
     * Initializes the controller by setting up the CRUD service, logger, and creating the initial entity.
     * This method is called by the constructors.
     */
    private void init() {
        if (crudService == null) {
            crudService = Containers.get().findObject(CrudService.class);
        }

        if (logger == null) {
            logger = Containers.get().findObject(LoggingService.class);
        }

        if (logger == null) {
            logger = new SLF4JLoggingService(CrudController.class);
        }

        params = new QueryParameters();
        if (entityClass == null) {
            try {
                setEntityClass(ObjectOperations.getGenericTypeClass(this));

            } catch (Exception e) {
                logger.warn("Cannot get generic class for EntityClass, you should invoke setEntityClass or use the constructor");
            }
        }

        newEntity();
        afterInit();
    }

    /**
     * Updates the dataset view component with the current query results.
     * This method is called after executing a query.
     */
    protected void updateDataSetView() {
        if (dataSetView != null) {
            dataSetView.setValue(getQueryResult());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void doQuery() {
        try {
            if (defaultParameters != null && !defaultParameters.isEmpty()) {
                defaultParameters.forEach(this::setParemeter);
            }
            configurePaginator();
            configureSorter();
            beforeQuery();
            query();

        } catch (Exception e) {
            exceptionCaught(e);
        }
    }

    /**
     * Configures the data paginator from the ZK paginator component if present.
     * This method sets up pagination parameters for the query.
     */
    private void configurePaginator() {
        if (paginator != null) {
            dataPaginator = new DataPaginator();
            dataPaginator.setPageSize(paginator.getPageSize());
            getParams().paginate(dataPaginator);

            if (dataSetView.getViewDescriptor().getParams().get("pagination") == Boolean.FALSE) {
                getParams().paginate(null);
            }
        }
    }

    /**
     * Configures the sorter for query results.
     * This method sets up sorting parameters from the table view if present.
     */
    private void configureSorter() {
        if (getParams().getSorter() == null) {
            getParams().sort(sorter);

            if (dataSetView instanceof TableView tableView) {
                if (tableView.getOrderBy() != null) {
                    getParams().orderBy(tableView.getOrderBy(), true);
                }
                if (tableView.getMaxResults() > 0) {
                    getParams().setMaxResults(tableView.getMaxResults());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>This method executes the complete save workflow including:</p>
     * <ul>
     *   <li>Deleting marked child entities in subcrud controllers</li>
     *   <li>Calling {@link #beforeSave()} hook</li>
     *   <li>Persisting the entity via {@link #save()}</li>
     *   <li>Creating/updating child entities in subcrud controllers</li>
     *   <li>Calling {@link #afterSave()} hook</li>
     *   <li>Showing success message</li>
     *   <li>Closing dialog if present</li>
     *   <li>Executing save callback if registered</li>
     * </ul>
     */
    @Override
    @Listen("onClick = #save")
    public final void doSave() {
        Callback saveCallbak = () -> {
            saved = false;
            try {
                doDeletesInSubcontrollers();
                beforeSave();
                save();
                doChangesInSubcontrollers();
                showMessageOnSaveSuccessfull();
                saved = true;
                afterSave();
                newEntity();
                closeCurrentDialog();
                if (isAutoClearPage()) {
                    ZKUtil.clearPage(getPage());
                }
                if (onSaveCallback != null) {
                    onSaveCallback.doSomething();
                }
            } catch (WrongValueException | WrongValuesException e) {
                throw e;
            } catch (Exception e) {
                logger.error("Error saving " + entityClass, e);
                exceptionCaught(e);
            }
        };

        if (isConfirmBeforeSave()) {
            UIMessages.showQuestion(messages.get("saveConfirmMessage", name), saveCallbak);
        } else {
            saveCallbak.doSomething();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSave(Callback onSave) {
        this.onSaveCallback = onSave;

    }

    /**
     * Displays a success message after saving the entity.
     * The message includes the entity name or string representation.
     */
    public void showMessageOnSaveSuccessfull() {
        if (name != null) {
            UIMessages.showMessage(messages.get("savedSuccessfully", name));
        } else {
            UIMessages.showMessage(messages.get("savedSuccessfully", getEntity()));
        }
    }

    /**
     * Executes delete operations in all registered subcrud controllers.
     * This method is called during the save workflow to remove child entities marked for deletion.
     */
    protected void doDeletesInSubcontrollers() {
        if (!(this instanceof SubcrudController)) {
            if (!subcontrollers.isEmpty()) {
                for (SubcrudController subcontroller : subcontrollers) {
                    try {
                        subcontroller.doDeletes();
                    } catch (Exception e) {
                        logger.warn("Exception running subcrud controller " + subcontroller + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Executes create and update operations in all registered subcrud controllers.
     * This method is called during the save workflow to persist child entity changes.
     */
    protected void doChangesInSubcontrollers() {
        if (!(this instanceof SubcrudController)) { // Parent controllers

            if (!subcontrollers.isEmpty()) {
                for (SubcrudController subcontroller : subcontrollers) {
                    try {
                        subcontroller.doCreates();
                        subcontroller.doUpdates();
                    } catch (Exception e) {
                        logger.warn("Exception running subcrud controller " + subcontroller + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void doSaveAndEdit() {
        saved = false;
        try {
            beforeSave();
            save();
            UIMessages.showMessage(messages.get("saveAndEditMessage", name));
            afterSave();
            setSelected(getEntity());
            doEdit();
            saved = true;
        } catch (WrongValueException | WrongValuesException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al guardar " + entityClass, e);
            exceptionCaught(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Listen("onClick = #edit")
    public final void doEdit() {
        saved = true;
        Object ent = getSelected();
        if (ent != null) {
            beforeEdit();
            var entityId = DomainUtils.findEntityId(ent);
            if (entityId != null) {
                setEntity(crudService.find(entityClass, entityId));
            } else {
                setEntity((E) ent);
            }
            afterEdit();
        } else {
            UIMessages.showMessage(messages.get("editSelectItemMessage", name), MessageType.WARNING);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Listen("onClick = #delete")
    public final void doDelete() {

        deleted = false;
        if (getSelected() != null) {
            beforeDelete();
            UIMessages.getDisplayer().showCustomQuestion(messages.get("deleteConfirmMessage", name, getSelected()), messages.get("deleteConfirmTitle"),
                    messages.get("deleteYesLabel"), messages.get("deleteNoLabel"), MessageType.CRITICAL, () -> {
                        try {
                            delete();
                            afterDelete();
                            doQuery();
                            deleted = true;
                            UIMessages.showMessage(messages.get("deletedSuccessfully", name), MessageType.NORMAL);
                            setSelected(null);
                        } catch (ValidationError e) {
                            UIMessages.showMessage(e.getMessage(), MessageType.WARNING);
                        } catch (Exception e) {
                            logger.error(e);
                            if (e.getMessage() != null && e.getMessage().contains("ConstraintViolationException")) {
                                UIMessages.showMessage(messages.get("deleteErrorMessageConstraint", name), MessageType.WARNING);
                            } else {
                                UIMessages.showMessage(messages.get("deleteErrorMessage", name), MessageType.ERROR);
                            }
                        }
                    }, Callback.DO_NOTHING);
        } else {
            UIMessages.showMessage(messages.get("deleteSelectItemMessage", name), MessageType.WARNING);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Listen("onClick = #create")
    public final void doCreate() {
        saved = false;
        beforeCreate();
        newEntity();
        afterCreate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getDefaultEntityValues() {
        return defaultEntityValues;
    }

    /**
     * Gets an argument value from the current ZK execution context.
     * This is useful for retrieving parameters passed when opening windows or dialogs.
     *
     * @param name the argument name
     * @return the argument value, or null if not found
     */
    public Object getArg(Object name) {

        return Executions.getCurrent().getArg().get(name);
    }

    /**
     * Lifecycle hook called after controller initialization.
     * Override this method to perform custom setup after the controller is initialized.
     */
    protected void afterInit() {
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        queryResult = null;
        params = new QueryParameters();
    }

    /**
     * Logs an informational message using the controller's logger.
     *
     * @param message the message to log
     */
    protected void log(String message) {
        logger.info(message);
    }

    /**
     * Logs an error message with exception details using the controller's logger.
     *
     * @param messsage the error message
     * @param exception the exception to log
     */
    protected void log(String messsage, Throwable exception) {
        logger.error(messsage, exception);
    }

    /**
     * Checks if query projection is enabled for optimized database queries.
     * When enabled, only fields displayed in the view are fetched from the database.
     *
     * @return true if query projection is enabled
     */
    public boolean isQueryProjection() {
        return queryProjection;
    }

    /**
     * Sets whether to use query projections for optimized database queries.
     * When enabled, only fields displayed in the view are fetched from the database.
     *
     * @param queryProjection true to enable query projection
     */
    public void setQueryProjection(boolean queryProjection) {
        this.queryProjection = queryProjection;
    }

    /**
     * Gets the default query parameters that are applied to all queries.
     * These parameters are automatically added before executing any query.
     *
     * @return the default query parameters
     */
    public QueryParameters getDefaultParameters() {
        return defaultParameters;
    }

    /**
     * Sets default query parameters that will be applied to all queries.
     * These parameters are automatically added before executing any query.
     *
     * @param defaultParameters the default query parameters to set
     */
    public void setDefaultParameters(QueryParameters defaultParameters) {
        this.defaultParameters = defaultParameters;
    }

    /**
     * Gets the custom attributes map for storing additional controller state.
     * This can be used to store any custom data needed by extended controllers.
     *
     * @return the custom attributes map
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Sets the custom attributes map for storing additional controller state.
     *
     * @param attributes the custom attributes map to set
     */
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    /**
     * Checks if save operations should execute in a new transaction.
     * When true, saves are isolated from the current transaction context.
     *
     * @return true if saves execute in new transactions
     */
    public boolean isSaveWithNewTransaction() {
        return saveWithNewTransaction;
    }

    /**
     * Sets whether save operations should execute in a new transaction.
     * When true, saves are isolated from the current transaction context.
     *
     * @param saveWithNewTransaction true to execute saves in new transactions
     */
    public void setSaveWithNewTransaction(boolean saveWithNewTransaction) {
        this.saveWithNewTransaction = saveWithNewTransaction;
    }
}
