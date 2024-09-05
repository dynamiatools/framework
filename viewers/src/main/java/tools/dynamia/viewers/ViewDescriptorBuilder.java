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

import tools.dynamia.commons.MapBuilder;
import tools.dynamia.viewers.impl.DefaultViewDescriptor;

import java.util.Arrays;
import java.util.Map;

/**
 * The Class ViewDescriptorBuilder.
 *
 * @author Mario A. Serrano Leones
 */
@SuppressWarnings("unchecked")
public class ViewDescriptorBuilder {

    /**
     * The vd.
     */
    protected DefaultViewDescriptor descriptor;

    protected ViewDescriptorBuilder() {
    }

    /**
     * View descriptor.
     *
     * @param type       the type
     * @param beanClass  the bean class
     * @param autofields the autofields
     * @return the view descriptor builder
     */
    public static ViewDescriptorBuilder viewDescriptor(String type, Class beanClass, boolean autofields) {
        ViewDescriptorBuilder builder = new ViewDescriptorBuilder();
        builder.descriptor = new DefaultViewDescriptor(beanClass, type, autofields);
        return builder;
    }

    /**
     * View descriptor.
     *
     * @param type      the type
     * @param beanClass the bean class
     * @return the view descriptor builder
     */
    public static ViewDescriptorBuilder viewDescriptor(String type, Class beanClass) {
        ViewDescriptorBuilder builder = new ViewDescriptorBuilder();
        builder.descriptor = new DefaultViewDescriptor(beanClass, type);
        return builder;
    }

    public static ViewDescriptorBuilder viewDescriptor(String type) {
        ViewDescriptorBuilder builder = new ViewDescriptorBuilder();
        builder.descriptor = new DefaultViewDescriptor(null, type);
        return builder;
    }

    public ViewDescriptorBuilder id(String id) {
        descriptor.setId(id);
        return this;
    }

    /**
     * Customizer.
     *
     * @param customizer the customizers
     * @return the view descriptor builder
     */
    public ViewDescriptorBuilder customizer(Class<? extends ViewCustomizer> customizer) {
        descriptor.setViewCustomizerClass(customizer);
        return this;
    }

    /**
     * Sort fields.
     *
     * @param fields the fields
     * @return the view descriptor builder
     */
    public ViewDescriptorBuilder sortFields(String... fields) {
        descriptor.sortFields(Arrays.asList(fields));
        return this;
    }

    /**
     * Hidden.
     *
     * @param fields the fields
     * @return the view descriptor builder
     */
    public ViewDescriptorBuilder hidden(String... fields) {
        descriptor.hideFields(fields);
        return this;
    }

    /**
     * Fields.
     *
     * @param fields the fields
     * @return the view descriptor builder
     */
    public ViewDescriptorBuilder fields(FieldBuilder... fields) {
        for (FieldBuilder field : fields) {
            descriptor.addField(field.build());
        }
        return this;
    }

    /**
     * Groups.
     *
     * @param groups the groups
     * @return the view descriptor builder
     */
    public ViewDescriptorBuilder groups(FieldGroupBuilder... groups) {
        for (FieldGroupBuilder fieldGroupBuilder : groups) {
            FieldGroup group = fieldGroupBuilder.build();
            for (String field : fieldGroupBuilder.getFields()) {
                group.addField(descriptor.getField(field));
            }
        }
        return this;
    }

    /**
     * Params.
     *
     * @param keyValue the key value
     * @return the view descriptor builder
     */
    public ViewDescriptorBuilder params(Object... keyValue) {
        Map params = MapBuilder.put(keyValue);
        //noinspection unchecked
        descriptor.getParams().putAll(params);
        return this;
    }

    /**
     * Layout.
     *
     * @param keyValue the key value
     * @return the view descriptor builder
     */
    @SuppressWarnings("unchecked")
    public ViewDescriptorBuilder layout(Object... keyValue) {
        Map params = MapBuilder.put(keyValue);
        //noinspection unchecked
        descriptor.getLayout().getParams().putAll(params);
        return this;
    }

    /**
     * Field.
     *
     * @param name the name
     * @return the field builder
     */
    public static FieldBuilder field(String name) {
        return field(name, null);
    }

    /**
     * Field.
     *
     * @param name  the name
     * @param label the label
     * @return the field builder
     */
    public static FieldBuilder field(String name, String label) {
        return field(name, label, null);
    }

    /**
     * Field.
     *
     * @param name      the name
     * @param label     the label
     * @param component the component
     * @return the field builder
     */
    public static FieldBuilder field(String name, String label, String component) {
        FieldBuilder fb = new FieldBuilder(name);
        fb.label(label).component(component);
        return fb;
    }

    /**
     * Group.
     *
     * @param name the name
     * @return the field group builder
     */
    public static FieldGroupBuilder group(String name) {
        return group(name, name);
    }

    /**
     * Group.
     *
     * @param name  the name
     * @param label the label
     * @return the field group builder
     */
    public static FieldGroupBuilder group(String name, String label) {
        return group(name, label, null);
    }

    /**
     * Group.
     *
     * @param name  the name
     * @param label the label
     * @param icon  the icon
     * @return the field group builder
     */
    public static FieldGroupBuilder group(String name, String label, String icon) {
        return new FieldGroupBuilder(name);
    }

    /**
     * Builds the.
     *
     * @return the view descriptor
     */
    public ViewDescriptor build() {
        return descriptor;
    }

    public ViewDescriptorBuilder autofields(boolean autofields) {
        descriptor.setAutofields(autofields);
        return this;
    }
}
