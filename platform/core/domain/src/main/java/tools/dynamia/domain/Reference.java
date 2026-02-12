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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a field as a reference to another entity.
 * This annotation is used to establish references between entities that
 * will be resolved at runtime using an {@code EntityReferenceRepository}.
 *
 * <p>The value of the annotation specifies the alias of the
 * {@code EntityReferenceRepository} implementation that will be used to
 * resolve the reference. This allows flexible entity relationships without
 * direct database foreign keys.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * public class Order {
 *
 *     @Reference("customer")
 *     private Long customerId;
 *
 *     private String orderNumber;
 *     private BigDecimal total;
 *
 *     // getters and setters
 * }
 *
 * // The EntityReferenceRepository implementation
 * @Component("customer")
 * public class CustomerReferenceRepository implements EntityReferenceRepository {
 *
 *     @Override
 *     public Object load(Serializable id) {
 *         // Load customer by ID
 *         return customerService.findById((Long) id);
 *     }
 *
 *     @Override
 *     public String getAlias() {
 *         return "customer";
 *     }
 * }
 * }</pre>
 *
 * @author Mario A. Serrano Leones
 * @see tools.dynamia.domain.EntityReferenceRepository
 */
@Target(value = ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Reference {

    /**
     * The alias of the EntityReferenceRepository that will be used
     * to resolve this reference. This value must match the alias
     * returned by the repository's {@code getAlias()} method.
     *
     * @return the repository alias
     */
    String value();

}
