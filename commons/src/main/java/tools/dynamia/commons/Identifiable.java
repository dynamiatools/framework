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

package tools.dynamia.commons;

import java.io.Serializable;

/**
 * The Interface Identifiable. Represents entities that have a unique identifier.
 * This interface is commonly implemented by domain entities, data objects, and persistent entities
 * to provide a standard way to access and manage unique identifiers. The identifier is typically
 * used for persistence operations, caching, equality comparisons, and entity referencing.
 * <br><br>
 * <b>Usage:</b><br>
 * <br>
 * <code>
 * public class User implements Identifiable&lt;Long&gt; {
 *     private Long id;
 *     private String name;
 *     
 *     public Long getId() {
 *         return id;
 *     }
 *     
 *     public void setId(Long id) {
 *         this.id = id;
 *     }
 * }
 * </code>
 *
 * @param <ID> the identifier type, must extend Serializable
 * @author Mario A. Serrano Leones
 */
public interface Identifiable<ID extends Serializable> {

    /**
     * Gets the unique identifier of this entity.
     *
     * @return the id
     */
    ID getId();

    /**
     * Sets the unique identifier of this entity.
     *
     * @param id the id to set
     */
    void setId(ID id);
}
