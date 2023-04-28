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

package tools.dynamia.domain.services;

import tools.dynamia.domain.query.QueryParameters;

import java.io.Serializable;

public interface GraphCrudService extends CrudService {

	/**
	 * Save with depth
	 *
	 * @param t
	 * @param depth
	 */
    <T> void save(T t, int depth);

	/**
	 * Load entity and relationships with depth
	 *
	 * @param type
	 * @param id
	 * @param depth
	 * @return
	 */
	<T> T find(Class<T> type, Serializable id, int depth);

	/**
	 * Query for Object
	 *
	 * @param type
	 * @param query
	 * @param params
	 * @return
	 */
	<T> T queryObject(Class<T> type, String query, QueryParameters params);

	<T> Iterable<T> query(Class<T> type, String query, QueryParameters params);

}
