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
package tools.dynamia.domain.query;

import java.io.Serializable;

/**
 * Interface representing an entity that store parameter with metadata and state. It define
 * the contract for parameter entities that can be used to store application, users or config parameters.
 * Parameters are identified by a unique identifier and can have a label, name, value, value type, description,
 *
 *
 * @param <ID> the type of the parameter identifier, must be serializable
 * @author Mario Serrano Leones
 *
 * @see {@link tools.dynamia.domain.jpa.JpaParameter}
 * @see {@link tools.dynamia.modules.saas.jpa.AccountParameter}
 */

public interface Parameter<ID extends Serializable> {


    /**
     * The parameter ID
     * @return ID
     */
    ID getId();

    /**
     * The parameter label, used for display purposes
     * @return the label
     */
    String getLabel();

    /**
     * Sets the parameter label, used for display purposes
     * @param label the label to set
     */
    void setLabel(String label);

    /**
     * Checks if is cacheable.
     *
     * @return true, if is cacheable
     */
    boolean isCacheable();

    /**
     * Sets the cacheable.
     *
     * @param cacheable the new cacheable
     */
    void setCacheable(boolean cacheable);

    /**
     * Checks if is disabled.
     *
     * @return true, if is disabled
     */
    boolean isDisabled();


    /**
     * Sets the disabled.
     *
     * @param disabled the new disabled
     */
    void setDisabled(boolean disabled);

    /**
     * Gets the name.
     *
     * @return the name
     */
    String getName();

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    void setName(String name);

    /**
     * Gets the value.
     *
     * @return the value
     */
    String getValue();

    /**
     * Sets the value.
     *
     * @param value the new value
     */
    void setValue(String value);

    /**
     * Gets the value type.
     *
     * @return the value type
     */
    String getValueType();

    /**
     * Sets the value type.
     *
     * @param valueType the new value type
     */
    void setValueType(String valueType);

    /**
     * Gets the description.
     *
     * @return the description
     */
    String getDescription();

    /**
     * Sets the description.
     *
     * @param description the new description
     */
    void setDescription(String description);


    String identifier();
}
