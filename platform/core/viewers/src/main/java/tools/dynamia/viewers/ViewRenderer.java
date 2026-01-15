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
 * Implementation of this class will renderer views for {@link ViewDescriptor}. see {@link ViewType}.
 *
 * @author Mario A. Serrano Leones
 * @param <T> the generic type
 */

/**
 * Interface for rendering views from a ViewDescriptor and value.
 *
 * @param <T> the type of value to render
 */
public interface ViewRenderer<T> extends Serializable {

    /**
     * Renders a view using the given descriptor and value.
     *
     * @param descriptor the view descriptor
     * @param value the value to render
     * @return the rendered view
     */
    View<T> render(ViewDescriptor descriptor, T value);
}
