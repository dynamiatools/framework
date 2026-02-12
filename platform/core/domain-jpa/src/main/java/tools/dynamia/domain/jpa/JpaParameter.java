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
package tools.dynamia.domain.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import tools.dynamia.domain.query.Parameter;

/**
 * The Class Parameter.
 *
 * @author Mario Serrano Leones
 */
@Entity
@Table(name = "app_parameters")
@DiscriminatorValue("Parameter")
public class JpaParameter extends BaseEntity implements Parameter<Long> {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    @Column(name = "param_name")
    @NotNull
    private String name;
    @Column(length = 1000, name = "param_value")
    private String value;
    private String valueType;
    private String label;
    private String description;
    private boolean disabled;
    private boolean cacheable;

    public JpaParameter() {
    }

    /**
     * Instantiates a new parameter.
     *
     * @param name  the name
     * @param value the value
     */
    public JpaParameter(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Instantiates a new parameter.
     *
     * @param name        the name
     * @param value       the value
     * @param description the description
     */
    public JpaParameter(String name, String value, String description) {
        this.name = name;
        this.value = value;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isCacheable() {
        return cacheable;
    }

    public void setCacheable(boolean cacheable) {
        this.cacheable = cacheable;
    }

    /**
     * Checks if is disabled.
     *
     * @return true, if is disabled
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * Sets the disabled.
     *
     * @param disabled the new disabled
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
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

    /**
     * Gets the value type.
     *
     * @return the value type
     */
    public String getValueType() {
        return valueType;
    }

    /**
     * Sets the value type.
     *
     * @param valueType the new value type
     */
    public void setValueType(String valueType) {
        this.valueType = valueType;
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

    /* (non-Javadoc)
     * @see AbstractEntity#toString()
     */
    @Override
    public String toString() {
        return name + ":" + value;
    }

    public String identifier() {
        return "Parameter";
    }
}
