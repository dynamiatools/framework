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

package tools.dynamia.zk.viewers.mv;

import tools.dynamia.viewers.ViewRenderer;
import tools.dynamia.viewers.ViewType;

/**
 * Represents the multi-view type in the viewers framework.
 * Provides the name and renderer for multi-view components.
 */
public class MultiViewType implements ViewType {

    /**
     * The name identifier for the multi-view type.
     */
    public static final String NAME = "multiview";

    /**
     * Returns the name of this view type.
     *
     * @return the name of the view type.
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Returns the renderer associated with this view type.
     *
     * @return the view renderer for multi-view components.
     */
    @Override
    public ViewRenderer getViewRenderer() {
        return new MultiViewRenderer();
    }

}
