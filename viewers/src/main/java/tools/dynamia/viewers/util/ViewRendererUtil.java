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

package tools.dynamia.viewers.util;

import tools.dynamia.integration.Containers;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.View;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.ViewRendererCustomizer;

/**
 * Util class for ViewRenderers implementation to call {@link ViewRendererCustomizer} methods
 */
public class ViewRendererUtil {

    private ViewRendererUtil() {

    }

    /**
     * Find first applicable {@link ViewRendererCustomizer}
     *
     */
    public static ViewRendererCustomizer findViewRendererCustomizer(ViewDescriptor viewDescriptor) {
        if (viewDescriptor == null || viewDescriptor.getBeanClass() == null) {
            return null;
        }
        String viewType = viewDescriptor.getViewTypeName();
        Class targetBeanClass = viewDescriptor.getBeanClass();

        return Containers.get().findObject(ViewRendererCustomizer.class, object -> object.getTargetBeanClass().equals(targetBeanClass) && object.getTargetViewType().equalsIgnoreCase(viewType));
    }

    /**
     * Call this method before render a field, if return false dont render the field
     *
     */
    public static boolean isFieldRenderable(ViewDescriptor descriptor, Field field) {
        ViewRendererCustomizer customizer = findViewRendererCustomizer(descriptor);
        return customizer == null || customizer.isRenderable(field);
    }

    /**
     * Call this method after all the view is rendered
     *
     */
    public static void afterRender(ViewDescriptor descriptor, View view) {
        ViewRendererCustomizer customizer = findViewRendererCustomizer(descriptor);
        try {
            if (customizer != null) {
                //noinspection unchecked
                customizer.afterRender(view);
            }
        } catch (ClassCastException e) {
            //Ignore: generic stuff
        }
    }

    /**
     * Call this method before starting render view internals
     *
     */
    public static void beforeRender(ViewDescriptor descriptor, View view) {
        ViewRendererCustomizer customizer = findViewRendererCustomizer(descriptor);
        try {
            if (customizer != null) {
                //noinspection unchecked
                customizer.beforeRender(view);
            }
        } catch (ClassCastException e) {
            //Ignore: generic stuff
        }
    }

    /**
     * Call this method for each field rendered
     *
     */
    public static void afterFieldRender(ViewDescriptor descriptor, Field field, Object component) {
        ViewRendererCustomizer customizer = findViewRendererCustomizer(descriptor);
        if (customizer != null) {
            customizer.afterFieldRender(field, component);
        }
    }
}
