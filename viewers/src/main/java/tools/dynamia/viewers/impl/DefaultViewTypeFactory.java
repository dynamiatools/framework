/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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
package tools.dynamia.viewers.impl;

import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.SimpleCache;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.sterotypes.Component;
import tools.dynamia.viewers.ViewRenderer;
import tools.dynamia.viewers.ViewType;
import tools.dynamia.viewers.ViewTypeFactory;
import tools.dynamia.viewers.ViewTypeNotFoundException;

import java.util.Collection;

/**
 * A factory for creating DefaultViewType objects.
 *
 * @author Mario A. Serrano Leones
 */
@Component
public class DefaultViewTypeFactory implements ViewTypeFactory {

    /**
     * The cache.
     */
    private SimpleCache<String, ViewType> cache = new SimpleCache<>();
    private SimpleCache<String, Class<? extends ViewRenderer>> viewRenderers = new SimpleCache<>();

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dynamia.tools.viewers.ViewTypeFactory#getViewType(java.lang.String)
     */
    @Override
    public ViewType getViewType(String name) {
        ViewType viewType = cache.get(name);
        if (viewType == null) {
            viewType = findViewType(name);
        }

        if (viewType == null) {
            throw new ViewTypeNotFoundException("Cannot found view type [" + name + "]");
        } else {
            cache.add(name, viewType);
        }

        return viewType;

    }

    /**
     * Find view type.
     *
     * @param name the name
     * @return the view type
     */
    private ViewType findViewType(String name) {
        Collection<ViewType> viewTypes = Containers.get().findObjects(ViewType.class);
        if (viewTypes != null) {
            for (ViewType viewType : viewTypes) {
                if (viewType.getName().equalsIgnoreCase(name)) {
                    return viewType;
                }
            }
        }
        return null;
    }

    @Override
    public void setCustomViewRenderer(String viewTypeName, Class<? extends ViewRenderer> viewRendererClass) {
        viewRenderers.add(viewTypeName, viewRendererClass);

    }

    @Override
    public ViewRenderer getViewRenderer(ViewType viewType) {

        Class<? extends ViewRenderer> viewRendererClass = viewRenderers.get(viewType.getName());

        if (viewRendererClass != null) {
            return BeanUtils.newInstance(viewRendererClass);
        } else {
            return viewType.getViewRenderer();
        }
    }

}
