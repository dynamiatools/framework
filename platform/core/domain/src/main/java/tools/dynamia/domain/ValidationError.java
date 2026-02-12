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
package tools.dynamia.domain;

import jakarta.validation.ValidationException;


/**
 * The Class ValidationError.
 *
 * @author Mario A. Serrano Leones
 */
public class ValidationError extends ValidationException {

    /**
     * The invalid value.
     */
    private Object invalidValue;

    /**
     * The invalid property.
     */
    private String invalidProperty;

    /**
     * The object class.
     */
    private Class objectClass;

    /**
     * Instantiates a new validation error.
     *
     * @param message the message
     */
    public ValidationError(String message) {
        super(message);
    }

    /**
     * Instantiates a new validation error using String.format arguments
     *
     */
    public ValidationError(String messageFormat, Object... args) {
        super(String.format(messageFormat, args));
    }

    /**
     * Instantiates a new validation error.
     *
     * @param message the message
     * @param invalidValue the invalid value
     * @param invalidProperty the invalid property
     * @param objectClass the object class
     */
    public ValidationError(String message, Object invalidValue, String invalidProperty, Class objectClass) {
        super(message);
        this.invalidValue = invalidValue;
        this.invalidProperty = invalidProperty;
        this.objectClass = objectClass;
    }

    /**
     * Gets the invalid property.
     *
     * @return the invalid property
     */
    public String getInvalidProperty() {
        return invalidProperty;
    }

    /**
     * Gets the invalid value.
     *
     * @return the invalid value
     */
    public Object getInvalidValue() {
        return invalidValue;
    }

    /**
     * Gets the object class.
     *
     * @return the object class
     */
    public Class getObjectClass() {
        return objectClass;
    }
}
