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
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.sterotypes.Component;
import tools.dynamia.viewers.*;
import tools.dynamia.viewers.util.Viewers;

import java.util.Collection;

/**
 * A factory for creating DefaultView objects.
 *
 * @author Mario A. Serrano Leones
 */
@Component
@SuppressWarnings({"unchecked", "rawtypes"})
public class DefaultViewFactory implements ViewFactory {

    private static final LoggingService LOGGER = new SLF4JLoggingService(DefaultViewFactory.class);

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dynamia.tools.viewers.ViewFactory#getView(com.dynamia.tools.viewers
     * .ViewDescriptor)
     */
    @Override
    public View getView(ViewDescriptor viewDescriptor) {
        return getView(viewDescriptor, null);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dynamia.tools.viewers.ViewFactory#getView(com.dynamia.tools.viewers
     * .ViewDescriptor, java.lang.Object)
     */
    @Override
    public <T> View<T> getView(ViewDescriptor viewDescriptor, T value) {
        return getView(viewDescriptor.getViewTypeName(), value, viewDescriptor);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.viewers.ViewFactory#getView(java.lang.String,
     * java.lang.Object)
     */
    @Override
    public <T> View<T> getView(String type, T value) {
        return getView(type, null, value);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.viewers.ViewFactory#getView(java.lang.String,
     * java.lang.Object, java.lang.Class)
     */
    @Override
    public <T> View<T> getView(String type, T value, Class<?> beanClass) {
        ViewDescriptor descriptor = Viewers.getViewDescriptor(beanClass, type);
        return getView(type, value, descriptor);
    }

    @Override
    public <T> View<T> getView(String type, String device, T value) {
        if (value == null) {
            throw new InvalidValueForViewException("value is null. Use getView(String viewType,Class beanClass,T value) for null values");
        }

        Class beanClass = null;

        if (value instanceof Collection) {
            Collection c = (Collection) value;
            for (Object object : c) {
                beanClass = object.getClass();
                break;
            }
        } else {
            beanClass = value.getClass();
        }
        return getView(type, device, value, beanClass);
    }

    @Override
    public <T> View<T> getView(String type, String device, T value, Class<?> beanClass) {
        ViewDescriptor descriptor = Viewers.getViewDescriptor(beanClass, device, type);
        return getView(type, value, descriptor);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.viewers.ViewFactory#getView(java.lang.String,
     * java.lang.Object, com.dynamia.tools.viewers.ViewDescriptor)
     */
    @Override
    public <T> View<T> getView(String type, T value, ViewDescriptor viewDescriptor) {
        if (type == null) {
            throw new ViewRendererException("ViewType Name is required to build a View");
        }

        ViewTypeFactory viewTypeFactory = Containers.get().findObject(ViewTypeFactory.class);
        ViewType viewType = viewTypeFactory.getViewType(type);
        ViewRenderer viewRenderer = null;

        try {
            viewRenderer = viewTypeFactory.getViewRenderer(viewType);

            if (viewDescriptor.getCustomViewRenderer() != null) {
                viewRenderer = BeanUtils.newInstance(viewDescriptor.getCustomViewRenderer());
            }

            if (viewDescriptor.getParams().containsKey(Viewers.PARAM_DEFAULT_VIEW_RENDERER)) {
                viewRenderer = viewType.getViewRenderer();
            }
        } catch (Exception e) {
            throw new ViewRendererException("Error initializing ViewRenderer for " + viewDescriptor, e);
        }


        View<T> view = viewRenderer.render(viewDescriptor, value);
        view.setViewDescriptor(viewDescriptor);

        if (viewDescriptor.getViewCustomizerClass() != null) {
            try {
                ViewCustomizer viewCustomizer = BeanUtils.newInstance(viewDescriptor.getViewCustomizerClass());
                viewCustomizer.customize(view);
            } catch (Exception e) {
                String name = viewDescriptor.getBeanClass() != null ? view.getViewDescriptor().getBeanClass().toString() : " ID: " + viewDescriptor.getId();
                LOGGER.error("Exception customizing View Descriptor " + viewDescriptor.getViewTypeName() + " - " + name+" using class "+viewDescriptor.getViewCustomizerClass(), e);
            }
        }

        return view;
    }
}
