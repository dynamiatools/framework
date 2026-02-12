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

import tools.dynamia.commons.BeanSorter;
import tools.dynamia.commons.Callback;
import tools.dynamia.domain.query.DataPaginator;
import tools.dynamia.domain.query.DataSet;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;

import java.util.Map;

/**
 * Core API for managing CRUD operations on domain entities within user interface contexts.
 * This interface provides a comprehensive set of methods for creating, reading, updating, and deleting entities,
 * along with query capabilities, pagination support, and lifecycle hooks.
 *
 * <p>Implementations of this interface are typically used in UI controllers to handle entity operations
 * with minimal boilerplate code. It integrates seamlessly with the CrudService layer and provides
 * state management for current entity, selected entity, and query results.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * CrudControllerAPI<Customer> controller = new CrudController<>(Customer.class, crudService);
 * controller.doCreate();
 * controller.getEntity().setName("John Doe");
 * controller.doSave();
 * }</pre>
 *
 * @param <E> the type of the domain entity managed by this controller
 */
public interface CrudControllerAPI<E> {

	/**
	 * Sets the CrudService used to perform database operations.
	 *
	 * @param crudService the service instance to handle persistence operations
	 */
	void setCrudService(CrudService crudService);

	/**
	 * Gets the current CrudService instance.
	 *
	 * @return the service responsible for CRUD operations
	 */
	CrudService getCrudService();

	/**
	 * Saves the current entity using the configured CrudService.
	 * This method typically triggers validation and persistence operations.
	 * Use {@link #doSave()} for the complete save workflow including UI feedback.
	 */
	void save();

	/**
	 * Deletes the current entity using the configured CrudService.
	 * This method performs the deletion without confirmation or UI feedback.
	 * Use {@link #doDelete()} for the complete deletion workflow.
	 */
	void delete();

	/**
	 * Executes a query using the current example entity and query parameters.
	 * Results are stored in the query result dataset.
	 * Use {@link #doQuery()} for the complete query workflow with UI updates.
	 */
	void query();

	/**
	 * Performs a programmatic deletion of a specific entity.
	 * This is a convenience method for deleting entities without setting them as the current entity.
	 *
	 * @param entity the entity to delete
	 */
	void delete(E entity);

	/**
	 * Performs a programmatic edit operation on a specific entity.
	 * Sets the given entity as the current entity and prepares it for editing.
	 *
	 * @param entity the entity to edit
	 */
	void edit(E entity);

	/**
	 * Creates a new instance of the managed entity class and sets it as the current entity.
	 * The new entity is initialized with default values if configured.
	 */
	void newEntity();

	/**
	 * Creates a new example entity instance for query-by-example operations.
	 * The example entity is used to specify search criteria.
	 */
	void newExample();

	/**
	 * Reloads the current entity from the database, discarding any unsaved changes.
	 * This is useful to refresh the entity state after external modifications.
	 */
	void reloadEntity();

	/**
	 * Gets the current entity being managed by this controller.
	 *
	 * @return the current entity, or null if no entity is set
	 */
	E getEntity();

	/**
	 * Sets the current entity to be managed by this controller.
	 *
	 * @param entity the entity to set as current
	 */
	void setEntity(E entity);

	/**
	 * Gets the example entity used for query-by-example operations.
	 *
	 * @return the example entity, or null if no example is set
	 */
	E getExample();

	/**
	 * Gets the currently selected entity from the query results.
	 * This is typically used in list/table views where users can select a row.
	 *
	 * @return the selected entity, or null if no entity is selected
	 */
	E getSelected();

	/**
	 * Checks if the current entity has been successfully saved.
	 *
	 * @return true if the entity was saved in the last operation, false otherwise
	 */
	boolean isSaved();

	/**
	 * Checks if the current entity has been successfully deleted.
	 *
	 * @return true if the entity was deleted in the last operation, false otherwise
	 */
	boolean isDeleted();

	/**
	 * Sets the currently selected entity from query results.
	 *
	 * @param selected the entity to mark as selected
	 */
	void setSelected(E selected);

	/**
	 * Gets the query parameters used for searching and filtering entities.
	 *
	 * @return the current query parameters
	 */
	QueryParameters getParams();

	/**
	 * Sets the query parameters for searching and filtering entities.
	 *
	 * @param params the query parameters to use
	 */
	void setParams(QueryParameters params);

	/**
	 * Gets a specific parameter value from the query parameters.
	 *
	 * @param param the parameter name
	 * @return the parameter value, or null if not found
	 */
	Object getParameter(String param);

	/**
	 * Sets a specific parameter value in the query parameters.
	 *
	 * @param key the parameter name
	 * @param value the parameter value
	 */
	void setParemeter(String key, Object value);

	/**
	 * Gets the dataset containing the results of the last query operation.
	 *
	 * @return the query result dataset
	 */
	DataSet getQueryResult();

	/**
	 * Sets the dataset containing query results.
	 *
	 * @param queryResult the dataset to set
	 */
	void setQueryResult(DataSet queryResult);

	/**
	 * Checks if the query results are empty.
	 *
	 * @return true if no results were found in the last query, false otherwise
	 */
	boolean isQueryResultEmpty();

	/**
	 * Gets the paginator for handling large query result sets.
	 *
	 * @return the data paginator instance
	 */
	DataPaginator getDataPaginator();

	/**
	 * Gets the sorter used to order query results.
	 *
	 * @return the bean sorter instance
	 */
	BeanSorter getSorter();

	/**
	 * Gets the class type of the entity managed by this controller.
	 *
	 * @return the entity class
	 */
	Class<E> getEntityClass();

	/**
	 * Sets the class type of the entity to be managed by this controller.
	 *
	 * @param entityClass the entity class to manage
	 */
	void setEntityClass(Class<E> entityClass);

	/**
	 * Executes a complete query workflow including UI updates and feedback.
	 * This method triggers the query operation and updates the view accordingly.
	 */
	void doQuery();

	/**
	 * Executes a complete save workflow including validation, persistence, and UI feedback.
	 * This is the main method to persist entity changes from the user interface.
	 */
	void doSave();

	/**
	 * Executes a save operation and immediately switches to edit mode for the saved entity.
	 * Useful for workflows where users want to continue editing after saving.
	 */
	void doSaveAndEdit();

	/**
	 * Executes a complete edit workflow, preparing the selected entity for modification.
	 * This method sets up the UI for editing the currently selected entity.
	 */
	void doEdit();

	/**
	 * Executes a complete delete workflow including confirmation prompts and UI feedback.
	 * This is the main method to delete entities from the user interface.
	 */
	void doDelete();

	/**
	 * Executes a complete create workflow, initializing a new entity and preparing the UI.
	 * This method sets up the form for creating a new entity instance.
	 */
	void doCreate();

	/**
	 * Gets the default values to be applied to new entities.
	 * These values are automatically set when creating new entity instances.
	 *
	 * @return a map containing default field names and their values
	 */
	Map<String, Object> getDefaultEntityValues();

	/**
	 * Sets whether to show a confirmation dialog before saving entities.
	 *
	 * @param confirm true to require confirmation, false to save directly
	 */
	void setConfirmBeforeSave(boolean confirm);

	/**
	 * Checks if confirmation is required before saving entities.
	 *
	 * @return true if confirmation is enabled, false otherwise
	 */
	boolean isConfirmBeforeSave();

	/**
	 * Registers a callback to be executed after successful save operations.
	 * This allows custom logic to be triggered post-save.
	 *
	 * @param onSave the callback to execute after saving
	 */
	void onSave(Callback onSave);

	/**
	 * Clears the controller state, resetting all entities and query results.
	 * This method is useful for resetting the controller to its initial state.
	 */
	void clear();
}
