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
package tools.dynamia.domain;

import java.io.Serializable;

/**
 * The Interface Referenceable. Represents entities that can be converted to entity references.
 * This interface provides a standardized mechanism for creating lightweight references to domain
 * entities, which is essential for distributed systems, caching strategies, serialization, and
 * lazy loading patterns. Entity references typically contain just the identifier and type information,
 * allowing for efficient storage and transmission while maintaining referential integrity.
 * <br><br>
 * <b>Usage:</b><br>
 * <br>
 * <code>
 * public class Product implements Referenceable&lt;Long&gt; {
 *     private Long id;
 *     private String name;
 *     
 *     public EntityReference&lt;Long&gt; toEntityReference() {
 *         return EntityReference.create(this.getClass(), this.id);
 *     }
 * }
 * 
 * // Usage
 * Product product = productService.findById(1L);
 * EntityReference&lt;Long&gt; ref = product.toEntityReference();
 * // Send reference instead of full entity
 * </code>
 *
 * @param <ID> the identifier type
 * @author Ing. Mario Serrano
 */
public interface Referenceable<ID extends Serializable> {

    /**
     * Converts this entity to an entity reference.
     *
     * @return the entity reference
     */
    EntityReference<ID> toEntityReference();

}
