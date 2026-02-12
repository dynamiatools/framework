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

/**
 * Extended CRUD service interface for graph-based or relationship-aware data persistence.
 * <p>
 * This interface extends {@link CrudService} with capabilities for managing entity graphs and nested
 * relationships using depth control. It's particularly useful for graph databases (Neo4j, OrientDB),
 * object databases, or ORM scenarios where you need fine-grained control over relationship loading
 * and saving depth to avoid lazy loading exceptions or excessive queries.
 * </p>
 *
 * <p>
 * <b>Key features:</b>
 * <ul>
 *   <li>Depth-controlled entity persistence with cascading relationships</li>
 *   <li>Depth-controlled entity loading to fetch nested associations</li>
 *   <li>Native query support for graph databases or custom query languages</li>
 *   <li>Avoids N+1 query problems with explicit depth specification</li>
 *   <li>Flexible traversal of object graphs</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Depth levels:</b>
 * <ul>
 *   <li>Depth 0: Only the root entity (no relationships loaded/saved)</li>
 *   <li>Depth 1: Root entity + immediate relationships (1 level deep)</li>
 *   <li>Depth 2+: Root entity + relationships up to N levels deep</li>
 *   <li>Depth -1: Infinite depth (load/save entire graph - use with caution)</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Usage example:</b>
 * <pre>{@code
 * @Autowired
 * private GraphCrudService graphService;
 *
 * // Save order with all line items and their products (2 levels)
 * Order order = new Order();
 * order.addItem(new OrderItem(product1));
 * order.addItem(new OrderItem(product2));
 * graphService.save(order, 2);
 *
 * // Load order with items but not item's products (1 level)
 * Order found = graphService.find(Order.class, orderId, 1);
 *
 * // Custom graph query
 * List<Order> orders = graphService.query(Order.class,
 *     "MATCH (o:Order)-[:HAS_ITEM]->(i:OrderItem) WHERE o.status = $status RETURN o",
 *     QueryParameters.with("status", "PENDING"));
 * }</pre>
 * </p>
 *
 * @author Mario A. Serrano Leones
 * @see CrudService
 * @see QueryParameters
 */
public interface GraphCrudService extends CrudService {

	/**
	 * Saves an entity along with its relationships up to the specified depth.
	 * <p>
	 * This method persists the entity and cascades the save operation to related entities
	 * up to the specified number of levels. For example, depth 2 would save the entity,
	 * its direct relationships, and relationships of those relationships.
	 * </p>
	 *
	 * @param <T> the entity type
	 * @param t the entity to save
	 * @param depth the number of relationship levels to cascade save (0 = entity only, -1 = infinite)
	 */
    <T> void save(T t, int depth);

	/**
	 * Loads an entity by its identifier along with its relationships up to the specified depth.
	 * <p>
	 * This method fetches the entity and eagerly loads related entities up to the specified
	 * number of levels. This prevents lazy loading exceptions and reduces the number of queries
	 * compared to lazy loading each relationship individually.
	 * </p>
	 *
	 * @param <T> the entity type
	 * @param type the class of the entity to load
	 * @param id the unique identifier of the entity
	 * @param depth the number of relationship levels to load (0 = entity only, -1 = infinite)
	 * @return the loaded entity with relationships, or {@code null} if not found
	 */
	<T> T find(Class<T> type, Serializable id, int depth);

	/**
	 * Executes a custom query and returns a single result object.
	 * <p>
	 * This method allows executing native graph queries (e.g., Cypher for Neo4j, Gremlin, or custom query syntax)
	 * and retrieving a single entity result. Useful for complex queries that can't be expressed using
	 * standard query parameters.
	 * </p>
	 *
	 * @param <T> the expected result type
	 * @param type the class of the expected result
	 * @param query the native query string
	 * @param params query parameters for binding values
	 * @return the single query result, or {@code null} if no result found
	 */
	<T> T queryObject(Class<T> type, String query, QueryParameters params);

	/**
	 * Executes a custom query and returns multiple results.
	 * <p>
	 * This method allows executing native graph queries and retrieving multiple entity results.
	 * Supports iteration over large result sets without loading all results into memory at once.
	 * </p>
	 *
	 * @param <T> the expected result type
	 * @param type the class of the expected results
	 * @param query the native query string
	 * @param params query parameters for binding values
	 * @return an iterable of query results
	 */
	<T> Iterable<T> query(Class<T> type, String query, QueryParameters params);

}
