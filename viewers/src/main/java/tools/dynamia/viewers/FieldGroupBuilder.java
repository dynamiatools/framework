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


import java.util.List;

/**
 * The Class FieldGroupBuilder.
 *
 * @author Mario A. Serrano Leones
 */
public class FieldGroupBuilder {

    /**
     * The group.
     */
    private final FieldGroup group;

    /**
     * The fields.
     */
    private String[] fields;

    /**
     * Instantiates a new field group builder.
     *
     * @param name the name
     */
    public FieldGroupBuilder(String name) {
        group = new FieldGroup(name);
    }

    /**
     * Label.
     *
     * @param label the label
     * @return the field group builder
     */
    public FieldGroupBuilder label(String label) {
        group.setLabel(label);
        return this;
    }

    /**
     * Icon.
     *
     * @param icon the icon
     * @return the field group builder
     */
    public FieldGroupBuilder icon(String icon) {
        group.setIcon(icon);
        return this;
    }

    /**
     * Description.
     *
     * @param description the description
     * @return the field group builder
     */
    public FieldGroupBuilder description(String description) {
        group.setDescription(description);
        return this;
    }

    /**
     * Fields.
     *
     * @param fields the fields
     * @return the field group builder
     */
    public FieldGroupBuilder fields(String... fields) {
        this.fields = fields;
        return this;
    }

    public FieldGroupBuilder fields(List<String> fields) {
        this.fields = fields.toArray(String[]::new);
        return this;
    }

    /**
     * Gets the fields.
     *
     * @return the fields
     */
    public String[] getFields() {
        return fields;
    }

    /**
     * Builds the.
     *
     * @return the field group
     */
    public FieldGroup build() {
        return group;
    }
}
