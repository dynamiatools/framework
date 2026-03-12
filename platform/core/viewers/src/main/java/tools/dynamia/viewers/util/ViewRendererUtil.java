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

import java.util.List;

/**
 * Utility class for {@link tools.dynamia.viewers.ViewRenderer} implementations that provides
 * convenience methods for invoking {@link ViewRendererCustomizer} callbacks at the appropriate
 * points during the view rendering lifecycle (before render, after render, per-field render) and
 * for filtering the set of renderable fields.
 */
public class ViewRendererUtil {

    private ViewRendererUtil() {
    }

    /**
     * Finds the first {@link ViewRendererCustomizer} registered in the container that matches
     * both the bean class and the view type declared in the given {@link ViewDescriptor}.
     *
     * @param viewDescriptor the descriptor of the view being rendered; may be {@code null}
     * @return the matching {@link ViewRendererCustomizer}, or {@code null} if none is found or
     *         if {@code viewDescriptor} or its bean class is {@code null}
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
     * Determines whether a given field should be rendered. Call this method before rendering
     * each field; if it returns {@code false}, skip rendering for that field.
     *
     * @param descriptor the view descriptor that contains the field
     * @param field      the field to evaluate
     * @return {@code true} if the field should be rendered, {@code false} otherwise
     */
    public static boolean isFieldRenderable(ViewDescriptor descriptor, Field field) {
        ViewRendererCustomizer customizer = findViewRendererCustomizer(descriptor);
        return customizer == null || customizer.isRenderable(field);
    }

    /**
     * Invokes the {@link ViewRendererCustomizer#afterRender(View)} callback after the entire view
     * has been rendered. Any {@link ClassCastException} thrown by the customizer is silently ignored
     * to handle generic type mismatches gracefully.
     *
     * @param descriptor the view descriptor associated with the rendered view
     * @param view       the fully rendered view instance
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
     * Invokes the {@link ViewRendererCustomizer#beforeRender(View)} callback before the view
     * rendering process begins. Any {@link ClassCastException} thrown by the customizer is
     * silently ignored to handle generic type mismatches gracefully.
     *
     * @param descriptor the view descriptor associated with the view about to be rendered
     * @param view       the view instance that is about to be rendered
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
     * Invokes the {@link ViewRendererCustomizer#afterFieldRender(Field, Object)} callback
     * after a single field has been rendered, passing along the rendered UI component.
     *
     * @param descriptor the view descriptor that owns the field
     * @param field      the field that was just rendered
     * @param component  the UI component produced by rendering the field
     */
    public static void afterFieldRender(ViewDescriptor descriptor, Field field, Object component) {
        ViewRendererCustomizer customizer = findViewRendererCustomizer(descriptor);
        if (customizer != null) {
            customizer.afterFieldRender(field, component);
        }
    }

    /**
     * Filters the fields of a view descriptor to include only those that are renderable based on visibility,
     * field restrictions, and custom renderability logic.
     *
     * @param view       the view containing context for field restrictions
     * @param descriptor the view descriptor containing the fields to be filtered
     * @return a list of fields that are renderable
     */
    public static List<Field> filterRenderableFields(View view, ViewDescriptor descriptor) {
        var restrictions = FieldRestrictions.findRestrictions();
        return descriptor.sortFields()
                .stream().filter(Field::isVisible)
                .filter(f -> isFieldRenderable(descriptor, f))
                .filter(f -> FieldRestrictions.isFieldVisble(restrictions, view, f))
                .toList();
    }
}
