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
package tools.dynamia.viewers;

import tools.dynamia.commons.MapBuilder;
import tools.dynamia.viewers.util.Viewers;

import java.util.Map;

/**
 * The Class FieldBuilder.
 *
 * @author Mario A. Serrano Leones
 */
public class FieldBuilder {

    /**
     * The field.
     */
    private Field field;

    /**
     * Instantiates a new field builder.
     *
     * @param name the name
     */
    public FieldBuilder(String name) {
        field = new Field(name);
    }

    /**
     * Label.
     *
     * @param label the label
     * @return the field builder
     */
    public FieldBuilder label(String label) {
        field.setLabel(label);
        return this;
    }

    /**
     * Description.
     *
     * @param description the description
     * @return the field builder
     */
    public FieldBuilder description(String description) {
        field.setDescription(description);
        return this;
    }

    /**
     * Component.
     *
     * @param component the component
     * @return the field builder
     */
    public FieldBuilder component(String component) {
        field.setComponent(component);
        return this;
    }

    /**
     * Component customizers.
     *
     * @param customizer the customizers
     * @return the field builder
     */
    public FieldBuilder componentCustomizer(String customizer) {
        field.setComponentCustomizer(customizer);
        return this;
    }

    /**
     * Field class.
     *
     * @param fieldClass the field class
     * @return the field builder
     */
    public FieldBuilder fieldClass(Class fieldClass) {
        field.setFieldClass(fieldClass);
        return this;
    }

    /**
     * Params.
     *
     * @param keyValue the key value
     * @return the field builder
     */
    public FieldBuilder params(Object... keyValue) {
        Map params = MapBuilder.put(keyValue);
        field.getParams().putAll(params);

        return this;
    }

    public FieldBuilder ignoreBindings() {
        params(Viewers.PARAM_IGNORE_BINDINGS, true);
        return this;
    }

    public FieldBuilder span(int columns) {
        params(Viewers.PARAM_SPAN, columns);
        return this;
    }

    /**
     * Builds the.
     *
     * @return the field
     */
    public Field build() {
        return field;
    }
}
