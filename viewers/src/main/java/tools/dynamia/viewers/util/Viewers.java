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
package tools.dynamia.viewers.util;

import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.commons.reflect.PropertyInfo;
import tools.dynamia.domain.Descriptor;
import tools.dynamia.domain.Descriptors;
import tools.dynamia.integration.Containers;
import tools.dynamia.viewers.*;

import java.util.*;
import java.util.stream.Stream;

/**
 * The Class Viewers.
 *
 * @author Mario A. Serrano Leones
 */
public class Viewers {

    public static final String BEAN = "bean";
    public static final String ATTRIBUTE_TABLE_FIELD_COMPONENTS = "TABLE_FIELD_COMPONENTS";
    public static final String PARAM_NULLVALUE = "nullValue";

    public static final String LAYOUT_PARAM_COLUMNS = "columns";
    public static final String LAYOUT_PARAM_COLUMNS_WIDTH = "columnsWidth";
    public static final String ATTRIBUTE_FIELD_CLASS = "field-class";
    public static final String ATTRIBUTE_FIELD_NAME = "field-name";
    public static final String ATTRIBUTE_FORM_VIEW = "form-view";
    public static final String ATTRIBUTE_COLSPAN = "colspan";
    public static final String ATTRIBUTE_CLASS = "class";

    public static final String PARAM_BINDINGS = "bindings";
    public static final String PARAM_HFLEX = "hflex";
    public static final String PARAM_WIDTH = "width";
    public static final String PARAM_NEW_ROW = "newRow";
    public static final String PARAM_CONVERTER = "converter";
    public static final String PARAM_BINDING_ATTRIBUTE = "bindingAttribute";
    public static final String PARAM_IGNORE_BINDINGS = "ignoreBindings";
    public static final String PARAM_SPAN = "span";
    public static final String PARAM_LABEL_WIDTH = "labelWidth";
    public static final String PARAM_LABEL_ALIGN = "labelAlign";
    public static final String PARAM_LABEL_VALIGN = "labelValign";
    public static final String PARAM_HEIGHT = "height";
    public static final String PARAM_SHOW_LABEL = "showLabel";
    public static final String PARAM_STYLE_CLASS = "styleClass";
    public static final String PARAM_STYLE = "style";
    public static final String PARAM_BIND = "bind";
    public static final String PARAM_INPLACE = "inplace";
    public static final String PARAM_TITLE = "title";
    public static final String PARAM_DEFAULT_VIEW_RENDERER = "defaultViewRenderer";
    public static final String PARAMS_SORTABLE = "sortable";
    public static final String PARAM_AUTOQUERY = "autoquery";
    public static final String PARAM_ACTIONS = "actions";
    public static final String PARAM_ORDER_BY = "orderBy";
    public static final String PARAM_CONTROLLER = "controller";
    public static final String PARAM_MULTIVIEW_LISTENER = "multiViewListener";
    public static final String PARAM_RENDER_WHEN_NULL = "renderWhenNull";
    public static final String PARAM_PARENT_NAME = "parentName";
    public static final String PARAM_ROOT_LABEL_FIELD = "rootLabelField";
    public static final String PARAM_ROOT_LABEL = "rootLabel";
    public static final String PARAM_ROOT_ICON = "rootIcon";
    public static final String PARAM_FORMAT_PATTERN = "formatPattern";
    public static final String PARAM_HEADER = "header";
    public static final String PARAM_FOOTER = "footer";
    public static final String PARAM_ALIGN = "align";
    public static final String PARAM_FUNCTION = "function";
    public static final String PARAM_FUNCTION_CONVERTER = "functionConverter";
    public static final String PARAM_FROZEN_COLUMNS = "frozenColumns";
    public static final String PARAM_ITEM_RENDERER = "itemRenderer";
    public static final String PARAM_IN_MEMORY_SORTING = "inMemorySorting";
    public static final String PARAM_FILTER_CUSTOMIZER = "filterCustomizer";
    public static final String PARAM_CONDITION = "condition";
    public static final String PARAM_WRITABLE = "writable";
    public static final String PARAM_AUTOMODEL = "automodel";
    public static final String PARAM_ENUM_COLORS = "enumColors";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_COLORS = "colors";
    public static final String PARAM_CRUDSERVICE_NAME = "crudServiceName";
    public static final String PARAM_CONSTRAINT = "constraint";
    public static final String PARAM_DISABLED = "disabled";
    public static final String PARAMS_ATTRIBUTES = "attributes";
    public static final String PARAMS_MODEL_PROVIDER = "modelProvider";
    public static final String PARAM_CUSTOM_VIEW = "customView";
    public static final String PARAM_ACTION = "action";
    public static final String PARAM_MULTI_FUNCTION_PROCESSOR = "multiFunctionProcessor";
    public static final String PARAM_PAGINATION = "pagination";
    public static final String PARAM_MULTIVIEW = "multiview";

    /**
     * Find view descriptor.
     *
     * @param clazz the clazz
     * @param name  the name
     * @return the view descriptor
     */
    public static ViewDescriptor findViewDescriptor(Class clazz, String name) {
        ViewDescriptorFactory vdf = Containers.get().findObject(ViewDescriptorFactory.class);

        return vdf.findDescriptor(clazz, name);
    }

    /**
     * Find view descriptor.
     *
     * @param clazz  the clazz
     * @param device the device
     * @param name   the name
     * @return the view descriptor
     */
    public static ViewDescriptor findViewDescriptor(Class clazz, String device, String name) {
        ViewDescriptorFactory vdf = Containers.get().findObject(ViewDescriptorFactory.class);

        return vdf.findDescriptor(clazz, device, name);
    }

    /**
     * Get view descriptor.
     *
     * @param clazz the clazz
     * @param name  the name
     * @return the view descriptor
     */
    public static ViewDescriptor getViewDescriptor(Class clazz, String name) {
        ViewDescriptorFactory vdf = Containers.get().findObject(ViewDescriptorFactory.class);

        return vdf.getDescriptor(clazz, name);
    }

    /**
     * Get view descriptor.
     *
     * @param clazz  the clazz
     * @param device the device
     * @param name   the name
     * @return the view descriptor
     */
    public static ViewDescriptor getViewDescriptor(Class clazz, String device, String name) {
        ViewDescriptorFactory vdf = Containers.get().findObject(ViewDescriptorFactory.class);

        return vdf.getDescriptor(clazz, device, name);
    }

    /**
     * Find view descriptor.
     *
     * @param id the id
     * @return the view descriptor
     */
    public static ViewDescriptor findViewDescriptor(String id) {
        ViewDescriptorFactory vdf = Containers.get().findObject(ViewDescriptorFactory.class);
        return vdf.getDescriptor(id);
    }

    /**
     * Find view descriptor for a specific Devices
     *
     * @param id the id
     * @return the view descriptor
     */
    public static ViewDescriptor findViewDescriptor(String id, String device) {
        ViewDescriptorFactory vdf = Containers.get().findObject(ViewDescriptorFactory.class);
        return vdf.getDescriptor(id, device);
    }

    /**
     * Gets the view.
     *
     * @param clazz    the clazz
     * @param viewType the view type
     * @param value    the value
     * @return the view
     */
    public static View getView(Class clazz, String viewType, Object value) {
        ViewFactory viewFactory = Containers.get().findObject(ViewFactory.class);

        return viewFactory.getView(viewType, value, clazz);
    }

    /**
     * Gets the view.
     *
     * @param clazz    the clazz
     * @param viewType the view type
     * @param device   the device
     * @param value    the value
     * @return the view
     */
    public static View getView(Class clazz, String viewType, String device, Object value) {
        ViewFactory viewFactory = Containers.get().findObject(ViewFactory.class);

        return viewFactory.getView(viewType, device, value, clazz);
    }

    /**
     * Gets the view.
     *
     * @param viewDescriptor the view descriptor
     * @return the view
     */
    public static View getView(ViewDescriptor viewDescriptor) {
        ViewFactory viewFactory = Containers.get().findObject(ViewFactory.class);
        return viewFactory.getView(viewDescriptor);
    }

    /**
     * Gets the view.
     *
     * @param viewDescriptorId the view descriptor
     * @return the view
     */
    public static View getView(String viewDescriptorId) {
        return getView(findViewDescriptor(viewDescriptorId));
    }

    /**
     * @param descriptor
     * @param value
     * @return
     */
    public static View getView(ViewDescriptor descriptor, Object value) {
        View view = getView(descriptor);
        view.setValue(value);
        return view;
    }

    /**
     * Gets the fields.
     *
     * @param viewDescriptor the view descriptor
     * @return the fields
     */
    public static List<Field> getFields(ViewDescriptor viewDescriptor) {
       return viewDescriptor.sortFields();
    }

    /**
     * Gets the fields names.
     *
     * @param viewDescriptor the view descriptor
     * @return the fields names
     */
    public static String[] getFieldsNames(ViewDescriptor viewDescriptor) {
        List<String> names = new ArrayList<>();
        for (Field field : getFields(viewDescriptor)) {
            if (field.isVisible()) {
                names.add(field.getName());
            }
        }

        return names.toArray(new String[0]);

    }

    public static void customizeField(String viewTypeName, Field field) {
        Collection<FieldCustomizer> customizers = Containers.get().findObjects(FieldCustomizer.class);
        if (customizers != null) {
            for (FieldCustomizer fc : customizers) {
                fc.customize(viewTypeName, field);
            }
        }

    }

    public static void setupView(View view, Map<String, Object> params) {
        BeanUtils.setupBean(view, params);

    }

    public static Descriptor findClassDescriptor(Class<?> targetClass, String viewTypeName) {
        Descriptor classDescriptor = null;
        if (targetClass.isAnnotationPresent(Descriptors.class)) {
            Descriptors descriptors = targetClass.getAnnotation(Descriptors.class);
            classDescriptor = findDescriptor(descriptors, viewTypeName);
        } else if (targetClass.isAnnotationPresent(Descriptor.class)) {
            classDescriptor = targetClass.getAnnotation(Descriptor.class);
            if (!classDescriptor.type().isEmpty() && !classDescriptor.type().equals(viewTypeName)) {
                classDescriptor = null;
            }

        }
        return classDescriptor;
    }


    public static Descriptor findPropertyDescriptor(PropertyInfo property, String viewTypeName) {
        Descriptor propertyDescriptor = null;

        if (property.isAnnotationPresent(Descriptors.class)) {
            Descriptors descriptors = property.getAnnotation(Descriptors.class);
            propertyDescriptor = findDescriptor(descriptors, viewTypeName);
        } else if (property.isAnnotationPresent(Descriptor.class)) {
            propertyDescriptor = property.getAnnotation(Descriptor.class);
            if (!propertyDescriptor.type().isEmpty() && !propertyDescriptor.type().equals(viewTypeName)) {
                propertyDescriptor = null;
            }

        }
        return propertyDescriptor;
    }

    private static Descriptor findDescriptor(Descriptors descriptors, String viewTypeName) {
        Descriptor descriptor;
        descriptor = Stream.of(descriptors.value()).filter(d -> d.type().equals(viewTypeName)).findFirst().orElse(null);
        if (descriptor == null) {
            descriptor = Stream.of(descriptors.value()).filter(d -> d.type().isEmpty()).findFirst().orElse(null);
        }
        return descriptor;
    }

    private Viewers() {
    }

    /**
     * Build a classifer name using {@link ViewDescriptor} type and beanClass simple name or viewDescriptorId
     *
     * @param viewDescriptor
     * @return
     */
    public static String buildMessageClasffier(ViewDescriptor viewDescriptor) {
        String classifier = "";
        if (viewDescriptor != null) {
            if (viewDescriptor.getBeanClass() != null) {
                classifier = StringUtils.addSpaceBetweenWords(viewDescriptor.getBeanClass().getSimpleName());
            } else {
                classifier = StringUtils.capitalize(viewDescriptor.getId());
            }

            classifier += " " + StringUtils.capitalize(viewDescriptor.getViewTypeName());

        }
        return classifier;
    }
}
