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


import tools.dynamia.integration.Containers;

/**
 * Central factory for creating {@link View} instances from {@link ViewDescriptor}s or typed values.
 *
 * <p>A {@code ViewFactory} bridges the gap between the metadata layer ({@link ViewDescriptor})
 * and the actual rendered UI components ({@link View}). It resolves the appropriate
 * {@link ViewRenderer} for a given view type and delegates the construction of the component.</p>
 *
 * <p>The singleton instance can be obtained from the application container via
 * {@link #getInstance()}.</p>
 *
 * <p>Typical usage:</p>
 * <pre>{@code
 * ViewFactory factory = ViewFactory.getInstance();
 * View<MyEntity> view = factory.getView("form", myEntity);
 * }</pre>
 *
 * @see ViewDescriptor
 * @see ViewDescriptorFactory
 * @see ViewRenderer
 * @see ViewType
 */
public interface ViewFactory {

    /**
     * Returns the application-scoped {@code ViewFactory} instance from the service container.
     *
     * @return the {@code ViewFactory} singleton; never {@code null}
     * @throws IllegalStateException if no {@code ViewFactory} bean is registered
     */
    static ViewFactory getInstance() {
        return Containers.get().findObject(ViewFactory.class);
    }

    /**
     * Creates a {@link View} for the given descriptor without an initial value.
     *
     * @param viewDescriptor the descriptor defining the view structure; must not be {@code null}
     * @return a newly constructed {@link View}; never {@code null}
     */
    View getView(ViewDescriptor viewDescriptor);

    /**
     * Creates a {@link View} for the given descriptor and pre-populates it with {@code value}.
     *
     * @param <T>            the type of the domain object
     * @param viewDescriptor the descriptor defining the view structure; must not be {@code null}
     * @param value          the initial value to set on the view; may be {@code null}
     * @return a newly constructed and populated {@link View}; never {@code null}
     */
    <T> View<T> getView(ViewDescriptor viewDescriptor, T value);

    /**
     * Creates a {@link View} for the given view type name, inferring the target class from
     * the runtime type of {@code value}.
     *
     * @param <T>   the type of the domain object
     * @param type  the view type name (e.g., {@code "form"}, {@code "table"}); must not be {@code null}
     * @param value the value whose class is used to resolve the {@link ViewDescriptor};
     *              must not be {@code null}
     * @return a newly constructed and populated {@link View}; never {@code null}
     */
    <T> View<T> getView(String type, T value);

    /**
     * Creates a {@link View} for the given view type name and target device, inferring the target
     * class from the runtime type of {@code value}.
     *
     * @param <T>    the type of the domain object
     * @param type   the view type name; must not be {@code null}
     * @param device the target device identifier (e.g., {@code "screen"}, {@code "mobile"});
     *               must not be {@code null}
     * @param value  the value whose class is used to resolve the {@link ViewDescriptor};
     *               must not be {@code null}
     * @return a newly constructed and populated {@link View}; never {@code null}
     */
    <T> View<T> getView(String type, String device, T value);

    /**
     * Creates a {@link View} for the given view type name and an explicit target class.
     *
     * <p>Use this overload when {@code value} may be {@code null} or when the descriptor should
     * be resolved against a specific class rather than the value's runtime type.</p>
     *
     * @param <T>       the type of the domain object
     * @param type      the view type name; must not be {@code null}
     * @param value     the initial value to set on the view; may be {@code null}
     * @param beanClass the class used to resolve the {@link ViewDescriptor}; must not be {@code null}
     * @return a newly constructed and populated {@link View}; never {@code null}
     */
    <T> View<T> getView(String type, T value, Class<?> beanClass);

    /**
     * Creates a {@link View} for the given view type name, target device, and explicit target class.
     *
     * @param <T>       the type of the domain object
     * @param type      the view type name; must not be {@code null}
     * @param device    the target device identifier; must not be {@code null}
     * @param value     the initial value to set on the view; may be {@code null}
     * @param beanClass the class used to resolve the {@link ViewDescriptor}; must not be {@code null}
     * @return a newly constructed and populated {@link View}; never {@code null}
     */
    <T> View<T> getView(String type, String device, T value, Class<?> beanClass);

    /**
     * Creates a {@link View} using the given view type name and an already-resolved
     * {@link ViewDescriptor}, bypassing the descriptor lookup.
     *
     * <p>Use this overload when you have a pre-configured or programmatically built descriptor
     * and want to avoid the overhead of a descriptor factory lookup.</p>
     *
     * @param <T>            the type of the domain object
     * @param type           the view type name used to select the {@link ViewRenderer};
     *                       must not be {@code null}
     * @param value          the initial value to set on the view; may be {@code null}
     * @param viewDescriptor the pre-built descriptor to use; must not be {@code null}
     * @return a newly constructed and populated {@link View}; never {@code null}
     */
    <T> View<T> getView(String type, T value, ViewDescriptor viewDescriptor);

}
