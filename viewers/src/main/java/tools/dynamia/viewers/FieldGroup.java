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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.dynamia.commons.BeanMessages;
import tools.dynamia.commons.StringUtils;

import java.io.Serializable;
import java.util.*;

/**
 * The Class FieldGroup.
 *
 * @author Mario A. Serrano Leones
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldGroup implements Serializable, Indexable, Cloneable {


    private static final long serialVersionUID = -4318151472388145403L;
    @JsonIgnore
    private ViewDescriptor viewDescriptor;
    private String name;
    private String label;
    private String description;
    private String icon;
    @JsonIgnore
    private final List<Field> fields = new ArrayList<>();
    private int index;
    private boolean collapse;

    private final Map<String, Object> params = new HashMap<>();

    /**
     * Instantiates a new field group.
     *
     * @param name the name
     */
    public FieldGroup(String name) {
        this.name = name;
    }

    /**
     * Instantiates a new field group.
     *
     * @param name  the name
     * @param label the label
     */
    public FieldGroup(String name, String label) {
        this.name = name;
        this.label = label;
    }

    /**
     * Instantiates a new field group.
     *
     * @param name        the name
     * @param label       the label
     * @param description the description
     */
    public FieldGroup(String name, String label, String description) {
        this.name = name;
        this.label = label;
        this.description = description;
    }


    /**
     * Gets the params.
     *
     * @return the params
     */
    public Map<String, Object> getParams() {
        return params;
    }

    /**
     * Gets the icon.
     *
     * @return the icon
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Sets the icon.
     *
     * @param icon the new icon
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the fields.
     *
     * @return the fields
     */
    public List<Field> getFields() {
        return fields;
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    public String getLabel() {
        if (label == null) {
            label = StringUtils.capitalizeAllWords(StringUtils.addSpaceBetweenWords(getName()));
        }
        return label;
    }

    public String getLocalizedLabel() {
        return getLocalizedLabel(Locale.getDefault());
    }

    public String getLocalizedLabel(Locale locale) {
        String locLabel = getLabel();
        if (viewDescriptor != null) {
            BeanMessages messages = new BeanMessages(viewDescriptor.getBeanClass(), locale);

            String key = "group." + name;
            locLabel = messages.getMessage(key);
            if (locLabel != null && locLabel.equals(key)) {
                locLabel = getLabel();
            }

        }

        return locLabel;
    }

    /**
     * Sets the label.
     *
     * @param label the new label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Adds the field.
     *
     * @param field the field
     */
    public void addField(Field field) {
        if (field != null && !fields.contains(field)) {
            fields.add(field);
            field.setGroup(this);
        }
    }

    /**
     * Removes the field.
     *
     * @param field the field
     */
    public void removeField(Field field) {
        if (field != null && fields.remove(field)) {
            field.setGroup(null);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.viewers.Indexable#getIndex()
     */
    @Override
    public int getIndex() {
        return index;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.viewers.Indexable#setIndex(int)
     */
    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FieldGroup other = (FieldGroup) obj;
        return Objects.equals(this.name, other.name);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "FieldGroup{" + "name=" + name + '}';
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public FieldGroup clone() {
        FieldGroup g = new FieldGroup(name, label, description);
        g.setIcon(icon);
        g.setIndex(index);
        return g;
    }

    public void setViewDescriptor(ViewDescriptor viewDescriptor) {
        this.viewDescriptor = viewDescriptor;
    }

    public ViewDescriptor getViewDescriptor() {
        return viewDescriptor;
    }

    public boolean isCollapse() {
        return collapse;
    }

    public void setCollapse(boolean collapse) {
        this.collapse = collapse;
    }

    @JsonProperty("fields")
    public List<String> getFieldsNames() {
        return fields.stream().map(Field::getName).toList();
    }
}
