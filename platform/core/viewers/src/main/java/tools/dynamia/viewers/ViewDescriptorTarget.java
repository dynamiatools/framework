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

/**
 * Represents the target criteria used to locate or match a {@link ViewDescriptor}.
 * <p>
 * A {@code ViewDescriptorTarget} encapsulates three optional filtering attributes:
 * <ul>
 *   <li><b>beanClass</b> – the Java class of the domain object the view descriptor is associated with.</li>
 *   <li><b>type</b> – a string identifier for the kind of view (e.g., {@code "form"}, {@code "table"}, {@code "tree"}).</li>
 *   <li><b>id</b> – a unique string identifier for the specific view descriptor.</li>
 * </ul>
 * Any combination of these attributes can be used to narrow down the desired descriptor.
 * The special constant {@link #ALL} represents an open target with no restrictions,
 * matching every registered view descriptor.
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
public class ViewDescriptorTarget {

    /**
     * A shared {@code ViewDescriptorTarget} instance with no filtering criteria set.
     * Use this constant when all registered view descriptors should be considered
     * as potential matches.
     */
    public static final ViewDescriptorTarget ALL = new ViewDescriptorTarget();

    /**
     * The Java class of the bean (domain object) that the target view descriptor
     * is associated with. May be {@code null} if class-based filtering is not required.
     */
    private Class beanClass;

    /**
     * The view type identifier (e.g., {@code "form"}, {@code "table"}).
     * May be {@code null} if type-based filtering is not required.
     */
    private String type;

    /**
     * The unique identifier of the view descriptor.
     * May be {@code null} if id-based filtering is not required.
     */
    private String id;

    /**
     * Creates a new {@code ViewDescriptorTarget} with no filtering criteria.
     * Equivalent to using {@link #ALL}.
     */
    public ViewDescriptorTarget() {
    }

    /**
     * Creates a new {@code ViewDescriptorTarget} with all three filtering criteria.
     *
     * @param beanClass the Java class of the domain object associated with the target descriptor
     * @param type      the view type identifier (e.g., {@code "form"}, {@code "table"})
     * @param id        the unique identifier of the view descriptor
     */
    public ViewDescriptorTarget(Class beanClass, String type, String id) {
        this.beanClass = beanClass;
        this.type = type;
        this.id = id;
    }

    /**
     * Creates a new {@code ViewDescriptorTarget} filtered by view type and identifier,
     * without restricting to a specific bean class.
     *
     * @param type the view type identifier (e.g., {@code "form"}, {@code "table"})
     * @param id   the unique identifier of the view descriptor
     */
    public ViewDescriptorTarget(String type, String id) {
        this.type = type;
        this.id = id;
    }

    /**
     * Creates a new {@code ViewDescriptorTarget} filtered only by identifier.
     *
     * @param id the unique identifier of the view descriptor
     */
    public ViewDescriptorTarget(String id) {
        this.id = id;
    }

    /**
     * Returns the Java class of the bean (domain object) associated with the target view descriptor.
     *
     * @return the bean class, or {@code null} if not set
     */
    public Class getBeanClass() {
        return beanClass;
    }

    /**
     * Sets the Java class of the bean (domain object) associated with the target view descriptor.
     *
     * @param beanClass the bean class to filter by, or {@code null} to remove the restriction
     */
    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
    }

    /**
     * Returns the view type identifier used to filter view descriptors
     * (e.g., {@code "form"}, {@code "table"}, {@code "tree"}).
     *
     * @return the view type, or {@code null} if not set
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the view type identifier used to filter view descriptors.
     *
     * @param type the view type to filter by (e.g., {@code "form"}, {@code "table"}),
     *             or {@code null} to remove the restriction
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the unique identifier of the target view descriptor.
     *
     * @return the view descriptor id, or {@code null} if not set
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the target view descriptor.
     *
     * @param id the view descriptor id to filter by, or {@code null} to remove the restriction
     */
    public void setId(String id) {
        this.id = id;
    }
}
