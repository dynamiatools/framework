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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares which associations of an entity must travel with it when a
 * {@link tools.dynamia.domain.services.CrudService} hands it out of the persistence context.
 * <p>
 * Without Open Persistence In View, an entity returned by {@code find}, {@code load}, {@code reload} or
 * {@code findSingle} is detached as soon as the call returns, so touching one of its lazy collections throws
 * {@code LazyInitializationException}. When the entity is annotated, the CrudService initializes the listed paths
 * <strong>only if the call is the outermost one</strong> (the entity is going to leave the persistence context);
 * inside a service that already runs in a transaction nothing is loaded.
 * <pre>{@code
 * @Entity
 * @InitializeOnLoad({"detalles", "impuestos", "detalles.subdetalles"})
 * public class Venta { ... }
 * }</pre>
 * <p>
 * It can also be placed directly on a <strong>field</strong> (or on its getter), which is usually clearer for
 * collections: the association is initialized and {@link #value()} lists paths <em>relative to it</em>.
 * <pre>{@code
 * @Entity
 * public class Venta {
 *     @InitializeOnLoad("subdetalles")      // initializes detalles and detalles.subdetalles
 *     @OneToMany(mappedBy = "venta") private List<DetalleVenta> detalles;
 *
 *     @InitializeOnLoad                      // initializes impuestos
 *     @OneToMany(mappedBy = "venta") private List<ImpuestoVenta> impuestos;
 * }
 * }</pre>
 * Declarations on the class and on its fields (including those of superclasses) add up. On a field,
 * {@link #allCollections()} and {@link #depth()} are ignored.
 *
 * @see LoadPlan
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
public @interface InitializeOnLoad {

    /**
     * Property paths to initialize, relative to the entity (or, on a field, relative to the annotated property). A
     * path walks through collections, so {@code "detalles.subdetalles"} initializes {@code detalles} and the
     * {@code subdetalles} of each element.
     */
    String[] value() default {};

    /**
     * Initializes every to-many association of the entity (and, up to {@link #depth()}, of its elements).
     */
    boolean allCollections() default false;

    /**
     * Nesting level used by {@link #allCollections()}: 1 initializes only the collections of the entity itself.
     */
    int depth() default 1;
}
