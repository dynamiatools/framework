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
 * Interface representing a view with a value and descriptor.
 *
 * @param <T> the type of value managed by the view
 */
public interface View<T> extends Serializable {

    /**
     * Gets the value of the view.
     *
     * @return the value
     */
    T getValue();

    /**
     * Sets the value of the view.
     *
     * @param value the new value
     */
    void setValue(T value);

    /**
     * Sets the view descriptor.
     *
     * @param viewDescriptor the new view descriptor
     */
    void setViewDescriptor(ViewDescriptor viewDescriptor);

    /**
     * Gets the view descriptor.
     *
     * @return the view descriptor
     */
    ViewDescriptor getViewDescriptor();

    /**
     * Gets the parent view.
     *
     * @return the parent view
     */
    View getParentView();

    /**
     * Sets the parent view.
     *
     * @param view the new parent view
     */
    void setParentView(View view);

    default void setSource(Object source) {

    }

    default Object getSource() {
        return null;
    }
}
