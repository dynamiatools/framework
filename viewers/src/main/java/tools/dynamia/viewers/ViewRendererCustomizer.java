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
 * Interface for customizing the rendering process of a ViewRenderer.
 *
 * @param <V> the type of view to customize
 */
public interface ViewRendererCustomizer<V extends View>   extends Serializable {

    /**
     * Gets the target bean class for customization.
     *
     * @return the target bean class
     */
    Class<?> getTargetBeanClass();

    /**
     * Gets the target view type for customization.
     *
     * @return the target view type
     */
    String getTargetViewType();

    /**
     * Determines if the field is renderable.
     *
     * @param field the field to check
     * @return true if renderable, false otherwise
     */
    boolean isRenderable(Field field);

    /**
     * Called before rendering the view.
     *
     * @param view the view to customize
     */
    void beforeRender(V view);

    /**
     * Called after rendering the view.
     *
     * @param view the view to customize
     */
    void afterRender(V view);

    /**
     * Called after rendering a field component.
     *
     * @param field the field rendered
     * @param component the rendered component
     */
    void afterFieldRender(Field field, Object component);
}
