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

package tools.dynamia.reports;

/**
 * The Interface ExporterFieldLoader. Used to load field values for report export.
 * This interface provides a customizable mechanism for extracting and transforming field
 * values from data objects during report export processes. Field loaders enable complex
 * data transformation, computed field generation, nested property access, and custom
 * formatting before values are written to export formats like Excel, CSV, or XML.
 * They are essential for creating flexible and maintainable export solutions in reporting systems.
 * <br><br>
 * <b>Usage:</b><br>
 * <br>
 * <code>
 * public class UserFieldLoader implements ExporterFieldLoader&lt;User&gt; {
 *     
 *     public Object load(String field, User user) {
 *         switch (field) {
 *             case "fullName":
 *                 return user.getFirstName() + " " + user.getLastName();
 *             case "age":
 *                 return calculateAge(user.getBirthDate());
 *             case "status":
 *                 return user.isActive() ? "Active" : "Inactive";
 *             default:
 *                 return {@link tools.dynamia.commons.ObjectOperations}.getFieldValue(field, user);
 *         }
 *     }
 * }
 * 
 * // Usage in exporter
 * ExporterFieldLoader&lt;User&gt; loader = new UserFieldLoader();
 * Object value = loader.load("fullName", user);
 * </code>
 *
 * @param <T> the type of data being processed
 * @author Mario A. Serrano Leones
 */
public interface ExporterFieldLoader<T> {

    /**
     * Loads the value for a specific field from the given data.
     *
     * @param field the field name
     * @param data the data object
     * @return the loaded field value
     */
    Object load(String field, T data);
}
