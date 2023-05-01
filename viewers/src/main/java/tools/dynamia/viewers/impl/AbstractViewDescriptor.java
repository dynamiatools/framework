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
package tools.dynamia.viewers.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.commons.reflect.PropertyInfo;
import tools.dynamia.domain.Descriptor;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.FieldGroup;
import tools.dynamia.viewers.IndexableComparator;
import tools.dynamia.viewers.InvalidViewDescriptorFieldException;
import tools.dynamia.viewers.MergeableViewDescriptor;
import tools.dynamia.viewers.ViewCustomizer;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.ViewRenderer;
import tools.dynamia.viewers.util.ViewDescriptorReaderUtils;
import tools.dynamia.viewers.util.Viewers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The Class AbstractViewDescriptor.
 *
 * @author Mario A. Serrano Leones
 */
public abstract class AbstractViewDescriptor implements MergeableViewDescriptor, Serializable {

    /**
     * The id.
     */
    private String id;

    /**
     * The bean class.
     */
    private Class<?> beanClass;

    /**
     * The view customizers class.
     */
    private Class<? extends ViewCustomizer> viewCustomizerClass;

    /**
     * The fields.
     */
    private List<Field> fields = new ArrayList<>();

    /**
     * The field groups.
     */
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private final List<FieldGroup> fieldGroups = new ArrayList<>();

    /**
     * The view type name.
     */
    private String viewTypeName;

    /**
     * The params.
     */
    private Map<String, Object> params = new HashMap<>();

    /**
     * The logger.
     */
    private final LoggingService logger = new SLF4JLoggingService(getClass());

    /**
     * The autofields.
     */
    private boolean autofields = true;

    /**
     * The messages.
     */
    private String messages;

    /**
     * The extends value.
     */
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private String extendsValue;


    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Class<? extends ViewRenderer> customViewRenderer;

    private String device = "screen";

    /**
     * Instantiates a new abstract view descriptor.
     */
    public AbstractViewDescriptor() {
    }

    /**
     * Instantiates a new abstract view descriptor.
     *
     * @param viewTypeName the view type name
     */
    public AbstractViewDescriptor(String viewTypeName) {
        this(null, viewTypeName);
    }

    /**
     * Instantiates a new abstract view descriptor.
     *
     * @param beanClass    the bean class
     * @param viewTypeName the view type name
     */
    public AbstractViewDescriptor(Class<?> beanClass, String viewTypeName) {
        this(beanClass, viewTypeName, true);
    }

    /**
     * Instantiates a new abstract view descriptor.
     *
     * @param beanClass    the bean class
     * @param viewTypeName the view type name
     * @param autofields   the autofields
     */
    public AbstractViewDescriptor(Class<?> beanClass, String viewTypeName, boolean autofields) {
        this.autofields = autofields;
        if (beanClass != null && beanClass.isAnnotationPresent(Descriptor.class)) {
            this.autofields = false;
        }

        setViewTypeName(viewTypeName);
        setBeanClass(beanClass);
        generateId();


    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.viewers.ViewDescriptor#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    public void setId(String id) {
        this.id = id;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.viewers.ViewDescriptor#getViewTypeName()
     */
    @Override
    public String getViewTypeName() {
        return viewTypeName;
    }

    /**
     * Sets the view type name.
     *
     * @param viewTypeName the new view type name
     */
    public void setViewTypeName(String viewTypeName) {
        this.viewTypeName = viewTypeName;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.viewers.ViewDescriptor#getExtends()
     */
    @Override
    public String getExtends() {
        return extendsValue;
    }

    /**
     * Sets the extends.
     *
     * @param extendsValue the new extends
     */
    public void setExtends(String extendsValue) {
        this.extendsValue = extendsValue;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.viewers.ViewDescriptor#getBeanClass()
     */
    @Override
    public Class<?> getBeanClass() {
        return beanClass;
    }

    /**
     * Sets the bean class.
     *
     * @param beanClass the new bean class
     */
    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
        if (this.beanClass != null) {
            if (autofields) {
                createFieldsFromClass();
            } else {
                Descriptor descriptor = Viewers.findClassDescriptor(beanClass, getViewTypeName());
                if (descriptor != null) {
                    loadParametersFromClassDescriptors(descriptor);
                    createFieldsFromClassDescriptor(descriptor);
                }
            }
        }
    }


    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.viewers.ViewDescriptor#getViewCustomizerClass()
     */
    @Override
    public Class<? extends ViewCustomizer> getViewCustomizerClass() {
        return viewCustomizerClass;
    }

    /**
     * Sets the view customizers class.
     *
     * @param viewCustomizerClass the new view customizers class
     */
    public void setViewCustomizerClass(Class<? extends ViewCustomizer> viewCustomizerClass) {
        this.viewCustomizerClass = viewCustomizerClass;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.viewers.ViewDescriptor#getField(java.lang.String)
     */
    @Override
    public Field getField(String name) {
        return fields.stream()
                .filter(f -> f.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.viewers.ViewDescriptor#getFields()
     */
    @Override
    public List<Field> getFields() {
        return fields;
    }

    /**
     * Sets the fields.
     *
     * @param fields the new fields
     */
    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.viewers.ViewDescriptor#getFieldGroups()
     */
    @Override
    public List<FieldGroup> getFieldGroups() {
        return fieldGroups;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.viewers.ViewDescriptor#getParams()
     */
    @Override
    public Map<String, Object> getParams() {
        return params;
    }

    /**
     * Sets the params.
     *
     * @param params the params
     */
    public void setParams(Map<String, Object> params) {
        if (params != null) {
            this.params = params;
        }
    }

    /**
     * Sets the component class.
     *
     * @param fieldName       the field name
     * @param componentClass  the component class
     * @param componentConfig the component config
     */
    public void setComponentClass(String fieldName, Class<?> componentClass, Map<String, Object> componentConfig) {
        Field field = getField(fieldName);
        field.setComponentClass(componentClass);
        field.setParams(componentConfig);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.viewers.ViewDescriptor#addField(com.dynamia.tools.
     * viewers .Field)
     */
    @Override
    public void addField(Field field) {

        Field exists = getField(field.getName());
        if (exists != null) {
            if (field.getComponentClass() != null) {
                exists.setComponentClass(field.getComponentClass());
            }

            if (field.getFieldClass() != null) {
                exists.setFieldClass(field.getFieldClass());
            }

            exists.setLabel(field.getLabel());
            exists.setDescription(field.getDescription());
            exists.setVisible(field.isVisible());
            exists.getParams().putAll(field.getParams());
            exists.setComponent(field.getComponent());
            exists.setIndex(field.getIndex());
            exists.setComponentCustomizer(field.getComponentCustomizer());
            configureFieldComponent(exists);
        } else {
            validateField(field);
            field.setViewDescriptor(this);
            fields.add(field);
            configureFieldComponent(field);
        }
    }

    /**
     * Validate field.
     *
     * @param field the field
     */
    private void validateField(Field field) {
        if (field.getName() == null) {
            logger.warn("Field for view descriptor " + getBeanClass() + " - " + getViewTypeName() + " dont have name");
        }

        if (field.getFieldClass() == null) {
            try {
                logger.debug("Trying to find Field class for field " + field);
                PropertyInfo info = BeanUtils.getPropertyInfo(beanClass, field.getName());
                if (info != null) {
                    logger.debug("Field class found: " + info);
                    field.setFieldClass(info.getType());
                    field.setPropertyInfo(info);
                }

            } catch (Exception e) {
                logger.debug("No field class found. Is a custom field");
            }
        }

        if (field.getFieldClass() == null) {
            String name = getBeanClass() == null ? getId() : getBeanClass().toString();
            logger.debug("Field " + field.getName() + " dont specified a field class. ViewDescriptor " + name
                    + " - " + getViewTypeName());
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dynamia.tools.viewers.ViewDescriptor#addFieldGroup(com.dynamia.tools
     * .viewers.FieldGroup)
     */
    @Override
    public void addFieldGroup(FieldGroup fieldGroup) {
        if (!fieldGroups.contains(fieldGroup)) {
            fieldGroups.add(fieldGroup);
            if (fieldGroup.getIndex() == 0) {
                fieldGroup.setIndex(fieldGroups.size());
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dynamia.tools.viewers.ViewDescriptor#removeField(java.lang.String)
     */
    @Override
    public void removeField(String name) {
        Field field = getField(name);
        if (field != null) {
            fields.remove(field);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dynamia.tools.viewers.ViewDescriptor#getFieldGroup(java.lang.String)
     */
    @Override
    public FieldGroup getFieldGroup(String name) {
        FieldGroup group = null;

        for (FieldGroup fg : getFieldGroups()) {
            if (fg.getName().equals(name)) {
                group = fg;
                break;
            }
        }
        return group;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.viewers.ViewDescriptor#addParam(java.lang.String,
     * java.lang.Object)
     */
    @Override
    public void addParam(String name, Object value) {
        params.put(name, value);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dynamia.tools.viewers.MergeableViewDescriptor#merge(com.dynamia.tools
     * .viewers.ViewDescriptor)
     */
    @Override
    public void merge(ViewDescriptor anotherVD) {

        getParams().putAll(anotherVD.getParams());
        getLayout().getParams().putAll(anotherVD.getLayout().getParams());
        for (Field avdField : anotherVD.getFields()) {
            Field thisfield = getField(avdField.getName());
            if (thisfield == null) {
                thisfield = avdField.clone();
                addField(thisfield);
            } else {
                thisfield.setLabel(avdField.getLabel());
                thisfield.getParams().putAll(avdField.getParams());
            }
        }

        for (FieldGroup avdGroup : anotherVD.getFieldGroups()) {
            FieldGroup thisGroup = getFieldGroup(avdGroup.getName());
            if (thisGroup == null) {
                thisGroup = avdGroup.clone();
                addFieldGroup(avdGroup);
            }
            thisGroup.setIcon(avdGroup.getIcon());
            thisGroup.setIndex(avdGroup.getIndex());
            thisGroup.setLabel(avdGroup.getLabel());
            thisGroup.setDescription(avdGroup.getDescription());

            for (Field avdField : avdGroup.getFields()) {
                Field thisField = getField(avdField.getName());
                if (thisField != null) {
                    thisGroup.addField(thisField);
                }
            }
        }

    }

    /**
     * Hide fields.
     *
     * @param fieldNames the field names
     */
    public void hideFields(String... fieldNames) {
        for (String name : fieldNames) {
            Field field = getField(name);
            if (field != null) {
                field.setVisible(false);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dynamia.tools.viewers.ViewDescriptor#sortFieldGroups(java.util.List)
     */
    @Override
    public void sortFieldGroups(List<String> fieldGroupNames) {
        int index = 1;
        for (String name : fieldGroupNames) {
            FieldGroup group = getFieldGroup(name);
            if (group != null) {
                group.setIndex(index);
                index++;
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.viewers.ViewDescriptor#sortFields(java.util.List)
     */
    @Override
    public void sortFields(List<String> fieldNames) {
        int index = 1;
        for (String name : fieldNames) {
            Field field = getField(name);
            if (field != null) {
                field.setIndex(index);
                index++;
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.viewers.ViewDescriptor#getMessages()
     */
    @Override
    public String getMessages() {
        return messages;
    }

    /**
     * Sets the messages.
     *
     * @param messages the new messages
     */
    public void setMessages(String messages) {
        this.messages = messages;
    }

    /**
     * Configure field component.
     *
     * @param field the field
     */
    private void configureFieldComponent(Field field) {
        if (field != null && getViewTypeName() != null) {
            Viewers.customizeField(getViewTypeName(), field);
        }
    }

    /**
     * Creates the fields.
     */
    private void createFieldsFromClass() {
        fields.clear();
        List<PropertyInfo> properties = BeanUtils.getPropertiesInfo(beanClass);
        for (PropertyInfo property : properties) {
            createField(property);
        }
    }

    private void createField(PropertyInfo property) {
        if (property == null) {
            return;
        }
        Field field = new Field(property.getName(), property.getType());
        field.setPropertyInfo(property);
        String label = StringUtils.capitalize(field.getName());
        label = StringUtils.addSpaceBetweenWords(label);
        field.setLabel(label);


        Descriptor descriptor = Viewers.findPropertyDescriptor(property, getViewTypeName());
        if (descriptor != null) {
            if (!descriptor.label().isEmpty()) {
                field.setLabel(descriptor.label());
            }

            if (!descriptor.description().isEmpty()) {
                field.setDescription(descriptor.description());
            }

            if (!descriptor.view().isEmpty()) {
                field.setComponent(descriptor.view());
            }

            if (descriptor.params().length > 0) {
                try {
                    Stream.of(descriptor.params()).map(param -> param.split(":")).forEach(kv -> {
                        String name = kv[0].trim();
                        String value = kv[1].trim();
                        if (name.contains(".")) {
                            parseSubparams(name, value, field.getParams());
                        } else {
                            field.addParam(name, ViewDescriptorReaderUtils.parseValue(value));
                        }
                    });
                } catch (Exception e) {
                    throw new InvalidViewDescriptorFieldException("Cannot parse field Descriptor parameters for: " + field.getName() + " -> " + Arrays.toString(descriptor.params()), e);
                }
            }
        }


        addField(field);
    }

    @SuppressWarnings("unchecked")
    private void parseSubparams(String name, Object value, Map<String, Object> params) {
        if (name.contains(".")) {
            String[] subname = name.split("\\.");

            Map<String, Object> subparams = null;
            if (params.get(subname[0]) == null) {
                subparams = new HashMap<>();
                params.put(subname[0], subparams);
            } else if (params.get(subname[0]) instanceof Map) {
                //noinspection unchecked
                subparams = (Map<String, Object>) params.get(subname[0]);
            }

            if (subparams != null) {
                subparams.put(subname[1], value);
            }
        }

    }

    private void createFieldsFromClassDescriptor(Descriptor classDescriptor) {
        fields.clear();

        if (classDescriptor != null) {
            Stream.of(classDescriptor.fields()).map(f -> BeanUtils.getPropertyInfo(beanClass, f)).forEach(this::createField);

        }
    }

    private void loadParametersFromClassDescriptors(Descriptor classDescriptor) {

        if (classDescriptor.params().length > 0) {
            Stream.of(classDescriptor.params()).map(p -> p.split(":")).forEach(kv -> {
                String name = kv[0].trim();
                String value = kv[1].trim();


                addParam(name, ViewDescriptorReaderUtils.parseValue(value));
            });
        }

        if (classDescriptor.viewParams().length > 0) {
            Stream.of(classDescriptor.viewParams()).map(p -> p.split(":")).forEach(kv -> {
                String name = kv[0].trim();
                String value = kv[1].trim();

                getLayout().addParam(name, ViewDescriptorReaderUtils.parseValue(value));
            });
        }

    }


    /**
     * Gets the visible fields.
     *
     * @return the visible fields
     */
    private int getVisibleFields() {
        int count = 0;
        for (Field field : fields) {
            if (field.isVisible()) {
                count++;
            }
        }
        return count;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ViewDescriptor Info: " + getClass() + "   ==> " + getBeanClass() +
                "\n -> ID:" + getId() +
                "\n -> Fields Count:" + getFields().size() +
                "\n -> Visible Fields Count:" + getVisibleFields();
    }

    /**
     * Generate id.
     */
    private void generateId() {
        id = getViewTypeName() + System.currentTimeMillis();
    }

    @Override
    public Class<? extends ViewRenderer> getCustomViewRenderer() {
        return customViewRenderer;
    }

    public void setCustomViewRenderer(Class<? extends ViewRenderer> viewRendererClass) {
        this.customViewRenderer = viewRendererClass;
    }

    @Override
    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    @Override
    public boolean isAutofields() {
        return autofields;
    }

    @Override
    public List<Field> sortFields() {
        return getFields().stream().sorted(new IndexableComparator()).collect(Collectors.toList());
    }
}
