/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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
 * Basic interface for CrudController, normaly use it in UIs
 * @param <E>
 */
public interface CrudControllerAPI<E> {

	void setCrudService(CrudService crudService);

	CrudService getCrudService();

	void save();

	void delete();

	void query();

	/**
	 * Helper method to do a programatic delete
	 *
	 * @param entity
	 */
    void delete(E entity);

	/**
	 * Helper method to do a programatic edit
	 *
	 * @param entity
	 */
    void edit(E entity);

	void newEntity();

	void newExample();

	void reloadEntity();

	E getEntity();

	void setEntity(E entity);

	E getExample();

	E getSelected();

	boolean isSaved();

	boolean isDeleted();

	void setSelected(E selected);

	QueryParameters getParams();

	void setParams(QueryParameters params);

	Object getParameter(String param);

	void setParemeter(String key, Object value);

	DataSet getQueryResult();

	void setQueryResult(DataSet queryResult);

	boolean isQueryResultEmpty();

	DataPaginator getDataPaginator();

	BeanSorter getSorter();

	Class<E> getEntityClass();

	void setEntityClass(Class<E> entityClass);

	void doQuery();

	void doSave();

	void doSaveAndEdit();

	void doEdit();

	void doDelete();

	void doCreate();

	Map<String, Object> getDefaultEntityValues();

	void setConfirmBeforeSave(boolean confirm);

	boolean isConfirmBeforeSave();

	void onSave(Callback onSave);

	void clear();
}
