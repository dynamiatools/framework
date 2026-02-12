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

package tools.dynamia.domain.jdbc;

import tools.dynamia.commons.reflect.PropertyInfo;

import java.sql.ResultSet;

/**
 * Functional interface for mapping JDBC ResultSet data to object properties.
 * <p>
 * This interface provides a flexible mechanism for custom mapping logic when converting database
 * query results to Java objects. It allows fine-grained control over how individual properties
 * are populated from ResultSet columns, enabling type conversions, transformations, and complex
 * mapping scenarios that go beyond simple column-to-field mapping.
 * </p>
 *
 * <p>
 * <b>Key use cases:</b>
 * <ul>
 *   <li>Custom type conversion (e.g., database-specific types to Java types)</li>
 *   <li>Complex field mapping logic (e.g., combining multiple columns)</li>
 *   <li>Handling NULL values with default values or special logic</li>
 *   <li>Parsing JSON or XML columns into objects</li>
 *   <li>Applying business rules during data mapping</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Usage example:</b>
 * <pre>{@code
 * // Custom mapper for converting timestamps to LocalDateTime
 * ObjectMapper<User> timestampMapper = (user, property, rs) -> {
 *     if (property.getName().equals("createdAt")) {
 *         Timestamp timestamp = rs.getTimestamp("created_at");
 *         if (timestamp != null) {
 *             property.setValue(user, timestamp.toLocalDateTime());
 *         }
 *     }
 * };
 *
 * // Mapper for JSON columns
 * ObjectMapper<Product> jsonMapper = (product, property, rs) -> {
 *     if (property.getName().equals("metadata")) {
 *         String json = rs.getString("metadata_json");
 *         Map<String, Object> metadata = new ObjectMapper().readValue(json, Map.class);
 *         property.setValue(product, metadata);
 *     }
 * };
 *
 * // Usage with JDBC helper
 * List<User> users = jdbcHelper.query("SELECT * FROM users",
 *     User.class,
 *     timestampMapper);
 * }</pre>
 * </p>
 *
 * @param <T> the type of object being mapped
 * @author Mario A. Serrano Leones
 * @see ResultSet
 * @see PropertyInfo
 * @see JdbcRow
 */
@FunctionalInterface
public interface ObjectMapper<T> {

	/**
	 * Maps data from a ResultSet to a specific property of the target object.
	 * <p>
	 * This method is called for each property that needs to be populated from the ResultSet.
	 * Implementations should extract the appropriate value from the ResultSet, perform any
	 * necessary conversions or transformations, and set the value on the target object using
	 * the provided {@link PropertyInfo}.
	 * </p>
	 *
	 * @param object the target object to populate
	 * @param property metadata about the property to be set
	 * @param value the ResultSet containing the data (positioned at the current row)
	 */
	void map(T object, PropertyInfo property, ResultSet value);
}
