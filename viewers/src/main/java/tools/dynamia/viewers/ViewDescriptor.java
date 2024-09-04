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

import java.io.Serializable;
import java.util.List;
import java.util.Map;


/**
 * The Interface ViewDescriptor.
 *
 * @author Mario A. Serrano Leones
 */
public interface ViewDescriptor extends Serializable {

    /**
     * Gets the id.
     *
     * @return the id
     */
    String getId();

    /**
     * Gets the messages.
     *
     * @return the messages
     */
    String getMessages();

    /**
     * Gets the bean class.
     *
     * @return the bean class
     */
    Class<?> getBeanClass();

    /**
     * Gets the fields.
     *
     * @return the fields
     */
    List<Field> getFields();

    /**
     * Gets the field.
     *
     * @param name the name
     * @return the field
     */
    Field getField(String name);

    /**
     * Gets the field groups.
     *
     * @return the field groups
     */
    List<FieldGroup> getFieldGroups();

    /**
     * Gets the field group.
     *
     * @param name the name
     * @return the field group
     */
    FieldGroup getFieldGroup(String name);

    /**
     * Gets the layout.
     *
     * @return the layout
     */
    ViewLayout getLayout();

    /**
     * Gets the view type name.
     *
     * @return the view type name
     */
    String getViewTypeName();

    /**
     * Gets the extends.
     *
     * @return the extends
     */
    String getExtends();

    /**
     * Gets the view customizers class.
     *
     * @return the view customizers class
     */
    Class<? extends ViewCustomizer> getViewCustomizerClass();

    /**
     * Gets the custom view renderer.
     *
     * @return the custom view renderer
     */
    Class<? extends ViewRenderer> getCustomViewRenderer();

    /**
     * Gets the params.
     *
     * @return the params
     */
    Map<String, Object> getParams();

    /**
     * Adds the field.
     *
     * @param field the field
     */
    void addField(Field field);

    /**
     * Adds the field group.
     *
     * @param fieldGroup the field group
     */
    void addFieldGroup(FieldGroup fieldGroup);

    /**
     * Adds the param.
     *
     * @param name  the name
     * @param value the value
     */
    void addParam(String name, Object value);

    /**
     * Removes the field.
     *
     * @param name the name
     */
    void removeField(String name);

    /**
     * Sort fields.
     *
     * @param fieldNames the field names
     */
    void sortFields(List<String> fieldNames);

    /**
     * Sort field groups.
     *
     * @param fieldGroupNames the field group names
     */
    void sortFieldGroups(List<String> fieldGroupNames);

    /**
     * Device name, default is screen
     */
    String getDevice();

    /**
     *
     */
    boolean isAutofields();

    /**
     * Return fields sorted by index
     */
    List<Field> sortFields();

    /**
     * Return actions associated to this descriptor
     *
     * @return
     */
    List<ActionRef> getActions();


}
