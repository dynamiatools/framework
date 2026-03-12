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

/**
 * Core abstraction for a UI view that holds a typed value and is driven by a {@link ViewDescriptor}.
 *
 * <p>A {@code View} is the runtime representation of a UI component built by a {@link ViewRenderer}.
 * It is parameterized by the type {@code T} of the domain object (or value) it displays or edits.
 * The structure and behaviour of the view are defined by its associated {@link ViewDescriptor}.</p>
 *
 * <p>Views can be nested: a view may have a parent view, allowing composite UI hierarchies to be
 * constructed (e.g., a sub-form embedded inside a main form).</p>
 *
 * <p>Typical lifecycle:
 * <ol>
 *   <li>A {@link ViewDescriptor} is resolved by {@link ViewDescriptorFactory}.</li>
 *   <li>A {@code View} instance is created by {@link ViewFactory} using a {@link ViewRenderer}.</li>
 *   <li>A value is set via {@link #setValue(Object)}, causing the view to reflect the domain object.</li>
 * </ol>
 * </p>
 *
 * @param <T> the type of the domain object (value) managed by this view
 * @see ViewDescriptor
 * @see ViewFactory
 * @see ViewRenderer
 */
public interface View<T> extends Serializable {

    /**
     * Returns the current value held by this view.
     *
     * @return the current value, or {@code null} if no value has been set
     */
    T getValue();

    /**
     * Sets the value to be displayed or edited by this view.
     *
     * <p>Implementations should update the visual state of the view to reflect the new value.</p>
     *
     * @param value the new value; may be {@code null}
     */
    void setValue(T value);

    /**
     * Assigns the {@link ViewDescriptor} that defines the structure and metadata of this view.
     *
     * <p>This is typically called once during view construction by the {@link ViewFactory}.</p>
     *
     * @param viewDescriptor the descriptor to associate with this view; must not be {@code null}
     */
    void setViewDescriptor(ViewDescriptor viewDescriptor);

    /**
     * Returns the {@link ViewDescriptor} that describes the structure and metadata of this view.
     *
     * @return the view descriptor; may be {@code null} if not yet initialized
     */
    ViewDescriptor getViewDescriptor();

    /**
     * Returns the parent view that contains this view, if any.
     *
     * <p>Used to navigate composite view hierarchies.</p>
     *
     * @return the parent {@link View}, or {@code null} if this is a top-level view
     */
    View getParentView();

    /**
     * Sets the parent view that contains this view.
     *
     * @param view the parent {@link View}; may be {@code null} for top-level views
     */
    void setParentView(View view);

    /**
     * Sets an arbitrary source object associated with this view.
     *
     * <p>The source can represent the originating controller, component, or context object.
     * The default implementation is a no-op; subclasses may override to store the value.</p>
     *
     * @param source the source object; may be {@code null}
     */
    default void setSource(Object source) {
    }

    /**
     * Returns the source object previously set via {@link #setSource(Object)}.
     *
     * <p>The default implementation always returns {@code null}; subclasses may override.</p>
     *
     * @return the source object, or {@code null} if not set
     */
    default Object getSource() {
        return null;
    }
}
