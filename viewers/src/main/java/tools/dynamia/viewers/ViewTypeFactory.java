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
 * A factory for creating ViewType objects.
 *
 * @author Mario A. Serrano Leones
 */
public interface ViewTypeFactory {

    /**
     * Gets the view type.
     *
     * @param name the name
     * @return the view type
     */
    ViewType getViewType(String name);

    /**
     * Gets the view renderer.
     *
     * @param viewType the view type
     * @return the view renderer
     */
    ViewRenderer getViewRenderer(ViewType viewType);

    /**
     * Sets the custom view renderer.
     *
     * @param viewTypeName the view type name
     * @param viewRendererClass the view renderer class
     */
    void setCustomViewRenderer(String viewTypeName, Class<? extends ViewRenderer> viewRendererClass);
}
