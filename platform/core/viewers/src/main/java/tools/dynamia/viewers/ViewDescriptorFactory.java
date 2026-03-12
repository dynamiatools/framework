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
package tools.dynamia.viewers;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Central factory for resolving and managing {@link ViewDescriptor} instances.
 *
 * <p>A {@code ViewDescriptorFactory} is responsible for loading, caching, and providing
 * {@link ViewDescriptor}s by identifier, target class, view type, and device. It acts as the
 * primary entry point for the descriptor resolution pipeline in the DynamiaTools viewer
 * framework.</p>
 *
 * <p>Descriptor lookup follows this general precedence:
 * <ol>
 *   <li>Exact match by identifier ({@link #getDescriptor(String)}).</li>
 *   <li>Match by target class, device, and view type
 *       ({@link #getDescriptor(Class, String, String)}).</li>
 *   <li>Auto-generated descriptor when no registered descriptor is found (depending on
 *       implementation).</li>
 * </ol>
 * </p>
 *
 * <p>Use {@code find*} variants when you want a {@code null} result instead of auto-generation
 * on a miss.</p>
 *
 * @see ViewDescriptor
 * @see ViewFactory
 */
public interface ViewDescriptorFactory extends Serializable {

    /**
     * Returns the {@link ViewDescriptor} registered under the given identifier.
     *
     * @param id the unique descriptor identifier; must not be {@code null}
     * @return the matching {@link ViewDescriptor}; never {@code null}
     * @throws ViewDescriptorNotFoundException if no descriptor is found for {@code id}
     */
    ViewDescriptor getDescriptor(String id);

    /**
     * Returns the {@link ViewDescriptor} for the given target class and view type, using the
     * default device ({@code "screen"}).
     *
     * <p>If no descriptor is registered, an auto-generated one may be returned depending on the
     * implementation.</p>
     *
     * @param targetClass the domain class for which the descriptor is requested; must not be {@code null}
     * @param viewType    the view type name (e.g., {@code "form"}, {@code "table"}); must not be {@code null}
     * @return the resolved or generated {@link ViewDescriptor}; never {@code null}
     */
    ViewDescriptor getDescriptor(Class<?> targetClass, String viewType);

    /**
     * Returns the {@link ViewDescriptor} for the given target class, device, and view type.
     *
     * <p>If no descriptor is registered, an auto-generated one may be returned depending on the
     * implementation.</p>
     *
     * @param beanClass the domain class; must not be {@code null}
     * @param device    the target device identifier (e.g., {@code "screen"}, {@code "mobile"});
     *                  must not be {@code null}
     * @param viewType  the view type name; must not be {@code null}
     * @return the resolved or generated {@link ViewDescriptor}; never {@code null}
     */
    ViewDescriptor getDescriptor(Class beanClass, String device, String viewType);

    /**
     * Looks up a {@link ViewDescriptor} for the given target class, device, and view type
     * without triggering auto-generation.
     *
     * @param beanClass the domain class; must not be {@code null}
     * @param device    the target device identifier; must not be {@code null}
     * @param viewType  the view type name; must not be {@code null}
     * @return the matching {@link ViewDescriptor}, or {@code null} if not found
     */
    ViewDescriptor findDescriptor(Class beanClass, String device, String viewType);

    /**
     * Triggers a full reload of all registered {@link ViewDescriptor}s from their source
     * resources (e.g., classpath XML/YAML files, annotations).
     *
     * <p>This is typically invoked at application startup or whenever descriptors need to be
     * refreshed (e.g., during hot-reload in development).</p>
     */
    void loadViewDescriptors();

    /**
     * Looks up a {@link ViewDescriptor} for the given target class and view type using the
     * default device, without triggering auto-generation.
     *
     * @param beanClass the domain class; must not be {@code null}
     * @param viewType  the view type name; must not be {@code null}
     * @return the matching {@link ViewDescriptor}, or {@code null} if not found
     */
    ViewDescriptor findDescriptor(Class beanClass, String viewType);

    /**
     * Returns the {@link ViewDescriptor} registered under the given identifier for a specific device.
     *
     * @param id     the unique descriptor identifier; must not be {@code null}
     * @param device the target device identifier; must not be {@code null}
     * @return the matching {@link ViewDescriptor}; never {@code null}
     * @throws ViewDescriptorNotFoundException if no descriptor is found
     */
    ViewDescriptor getDescriptor(String id, String device);

    /**
     * Returns all registered descriptors grouped by their target class for the given view type.
     *
     * <p>Each entry maps a domain class to its corresponding {@link ViewDescriptor} of the
     * specified type.</p>
     *
     * @param viewType the view type name to filter by; must not be {@code null}
     * @return a non-null, possibly empty set of entries; order is not guaranteed
     */
    Set<Map.Entry<Class, ViewDescriptor>> findDescriptorsByType(String viewType);

    /**
     * Returns all {@link ViewDescriptor}s registered for the given domain class, across all
     * view types and devices.
     *
     * @param entityClass the domain class to look up; must not be {@code null}
     * @return a non-null, possibly empty set of descriptors
     */
    Set<ViewDescriptor> findDescriptorByClass(Class entityClass);
}
