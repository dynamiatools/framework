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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import tools.dynamia.commons.BeanMessages;
import tools.dynamia.commons.Messages;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.commons.reflect.AccessMode;
import tools.dynamia.commons.reflect.PropertyInfo;
import tools.dynamia.domain.contraints.NotEmpty;
import tools.dynamia.domain.util.DomainUtils;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The Class Field.
 *
 * @author Mario A. Serrano Leones
 */
public class Field implements Serializable, Indexable, Cloneable {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = -1782240844872423004L;

    private String name;
    private String label;
    private String description;
    private Class<?> fieldClass;
    @JsonIgnore
    private Class<?> componentClass;
    private String component;
    @JsonIgnore
    private String componentCustomizer;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private Map<String, Object> params = new HashMap<>();
    private boolean visible = true;
    @JsonIgnore
    private PropertyInfo propertyInfo;
    @JsonIgnore
    private ViewDescriptor viewDescriptor;
    @JsonIgnore
    private FieldGroup group;
    private int index;
    @JsonIgnore
    private String value;
    private boolean required;
    private String action;
    private String icon;
    private boolean showIconOnly;

    public Field() {
    }

    /**
     * Instantiates a new field.
     *
     * @param name the name
     */
    public Field(String name) {
        this.name = name;
    }

    /**
     * Instantiates a new field.
     *
     * @param name       the name
     * @param fieldClass the field class
     */
    public Field(String name, Class<?> fieldClass) {
        this.name = name;
        this.fieldClass = fieldClass;
    }

    /**
     * Instantiates a new field.
     *
     * @param name           the name
     * @param label          the label
     * @param fieldClass     the field class
     * @param componentClass the component class
     */
    public Field(String name, String label, Class<?> fieldClass, Class<?> componentClass) {
        this.name = name;
        this.label = label;
        this.fieldClass = fieldClass;
        this.componentClass = componentClass;
    }

    /**
     * Instantiates a new field.
     *
     * @param name           the name
     * @param label          the label
     * @param componentClass the component class
     */
    public Field(String name, String label, Class<?> componentClass) {
        this.name = name;
        this.label = label;
        this.componentClass = componentClass;
    }

    /**
     * Instantiates a new field.
     *
     * @param name           the name
     * @param label          the label
     * @param componentClass the component class
     * @param params         the params
     */
    public Field(String name, String label, Class<?> componentClass, Map<String, Object> params) {
        this.name = name;
        this.label = label;
        this.componentClass = componentClass;
        this.params = params;
    }

    /**
     * Instantiates a new field.
     *
     * @param name           the name
     * @param label          the label
     * @param description    the description
     * @param componentClass the component class
     * @param params         the params
     */
    public Field(String name, String label, String description, Class<?> componentClass, Map<String, Object> params) {
        this.name = name;
        this.label = label;
        this.description = description;
        this.componentClass = componentClass;
        this.params = params;
    }

    /**
     * Gets the view descriptor.
     *
     * @return the view descriptor
     */
    public ViewDescriptor getViewDescriptor() {
        return viewDescriptor;
    }

    /**
     * Sets the view descriptor.
     *
     * @param viewDescriptor the new view descriptor
     */
    public void setViewDescriptor(ViewDescriptor viewDescriptor) {
        if (this.viewDescriptor == null) {
            this.viewDescriptor = viewDescriptor;
        }
    }

    /**
     * Sets the.
     *
     * @param name  the name
     * @param value the value
     */
    public void set(String name, Object value) {
        addParam(name, value);
    }

    /**
     * Adds the param.
     *
     * @param name  the name
     * @param value the value
     */
    public void addParam(String name, Object value) {
        params.put(name, value);
    }

    /**
     * Gets the field class.
     *
     * @return the field class
     */
    public Class<?> getFieldClass() {
        return fieldClass;
    }

    /**
     * Sets the field class.
     *
     * @param fieldClass the new field class
     */
    public void setFieldClass(Class<?> fieldClass) {
        this.fieldClass = fieldClass;
    }

    /**
     * Gets the component class.
     *
     * @return the component class
     */
    public Class<?> getComponentClass() {
        return componentClass;
    }

    /**
     * Sets the component class.
     *
     * @param componentClass the new component class
     */
    public void setComponentClass(Class<?> componentClass) {
        this.componentClass = componentClass;
    }

    /**
     * Sets the component customizers.
     *
     * @param componentCustomizer the new component customizers
     */
    public void setComponentCustomizer(String componentCustomizer) {
        this.componentCustomizer = componentCustomizer;
    }

    /**
     * Gets the component customizers.
     *
     * @return the component customizers
     */
    public String getComponentCustomizer() {
        return componentCustomizer;
    }

    /**
     * Gets the component.
     *
     * @return the component
     */
    public String getComponent() {
        return component;
    }

    /**
     * Sets the component.
     *
     * @param component the new component
     */
    public void setComponent(String component) {
        this.component = component;
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
     * Overwrite the current params but dont replace it, internally it call
     * <code>this.params.putAll(newParams);</code>
     *
     * @param params the params
     */
    public void setParams(Map<String, Object> params) {
        if (params != null) {
            this.params.putAll(params);
        }
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
     * Gets the localized description.
     *
     * @param locale the locale
     * @return the localized description
     */
    public String getLocalizedDescription(Locale locale) {
        BeanMessages messages = BeanMessages.get(viewDescriptor.getBeanClass(), locale);
        String locDesc = getDescription();
        if (messages != null && propertyInfo != null) {
            locDesc = messages.getMessage(propertyInfo.getName() + ".description");
            if (locDesc != null && locDesc.equals(propertyInfo.getName() + ".description")) {
                locDesc = getDescription();
            }

        }

        return locDesc;
    }

    /**
     * Gets the localized description.
     *
     * @return the localized description
     */
    public String getLocalizedDescription() {
        return getLocalizedDescription(Locale.getDefault());
    }

    /**
     * Sets the description.
     *
     * @param description the new description
     */
    public void setDescription(String description) {
        if (description != null && !description.isEmpty()) {
            this.description = description;
        }
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    public String getLabel() {
        if (label == null) {
            label = StringUtils.capitalize(StringUtils.addSpaceBetweenWords(getName()));
        }
        return label;
    }

    /**
     * Gets the localized label.
     *
     * @param locale the locale
     * @return the localized label
     */
    public String getLocalizedLabel(Locale locale) {
        Class originClass = viewDescriptor.getBeanClass();
        if (name.contains(".") && propertyInfo != null) {
            originClass = propertyInfo.getOwnerClass();
        }

        BeanMessages messages = new BeanMessages(originClass, locale);
        String locLabel = getLabel();
        if (propertyInfo != null) {
            locLabel = messages.getMessage(propertyInfo.getName());
            if (locLabel != null && locLabel.equals(propertyInfo.getName())) {
                locLabel = getLabel();
            }
        }

        return locLabel;
    }

    /**
     * Gets the localized label.
     *
     * @return the localized label
     */
    public String getLocalizedLabel() {
        return getLocalizedLabel(Messages.getDefaultLocale());
    }

    /**
     * Sets the label.
     *
     * @param label the new label
     */
    public void setLabel(String label) {
        if (label != null && !label.isEmpty()) {
            this.label = label;
        }
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
        if (this.name == null) {
            this.name = name;
        }
    }

    /**
     * Sets the group.
     *
     * @param group the new group
     */
    public void setGroup(FieldGroup group) {
        this.group = group;
    }

    /**
     * Gets the group.
     *
     * @return the group
     */
    public FieldGroup getGroup() {
        return group;
    }

    /**
     * Checks if is visible.
     *
     * @return true, if is visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets the visible.
     *
     * @param visible the new visible
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Gets the property info.
     *
     * @return the property info
     */
    public PropertyInfo getPropertyInfo() {
        return propertyInfo;
    }

    /**
     * Sets the property info.
     *
     * @param propertyInfo the new property info
     */
    public void setPropertyInfo(PropertyInfo propertyInfo) {
        this.propertyInfo = propertyInfo;
        if (propertyInfo != null) {
            if (propertyInfo.isAnnotationPresent(NotNull.class) || propertyInfo.isAnnotationPresent(NotEmpty.class)) {
                setRequired(true);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public Field clone() {
        Field p = new Field(name, label, description, componentClass, new HashMap<>(params));
        p.setPropertyInfo(propertyInfo);
        p.setComponent(component);
        p.setVisible(visible);
        p.setIndex(index);
        p.setValue(value);
        p.setFieldClass(fieldClass);
        p.setComponentCustomizer(componentCustomizer);
        p.setAction(action);
        p.setParams(new HashMap<>(getParams()));

        return p;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Field{" + "name=" + name + '}';
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

    /**
     * Checks if is collection.
     *
     * @return true, if is collection
     */
    public boolean isCollection() {
        return propertyInfo != null && propertyInfo.isCollection();
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value.
     *
     * @param value the new value
     */
    public void setValue(String value) {
        this.value = value;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isEntity() {
        if (isCollection() && propertyInfo != null) {
            return DomainUtils.isEntity(propertyInfo.getGenericType());
        } else {
            return DomainUtils.isEntity(fieldClass);
        }
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isReadWrite() {
        return isProperty() && propertyInfo.getAccessMode() == AccessMode.READ_WRITE;
    }

    public boolean isReadOnly() {
        return isProperty() && propertyInfo.getAccessMode() == AccessMode.READ_ONLY;
    }

    public boolean isWriteOnly() {
        return isProperty() && propertyInfo.getAccessMode() == AccessMode.WRITE_ONLY;
    }

    public boolean isProperty() {
        return propertyInfo != null;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isShowIconOnly() {
        return showIconOnly;
    }

    public void setShowIconOnly(boolean showIconOnly) {
        this.showIconOnly = showIconOnly;
    }
}
