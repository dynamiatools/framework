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
package tools.dynamia.commons;

/**
 * Represents a property name-value-type triple from a bean.
 * Used in functional-style operations for bean property manipulation.
 *
 * @param name  the property name
 * @param value the property value (can be null)
 * @param type  the property type class
 * @author Ing. Mario Serrano Leones
 */
public record PropertyValue(String name, Object value, Class<?> type) {

    /**
     * Checks if the property value is null.
     *
     * @return true if value is null, false otherwise
     */
    public boolean isNull() {
        return value == null;
    }

    /**
     * Checks if the property value is not null.
     *
     * @return true if value is not null, false otherwise
     */
    public boolean isPresent() {
        return value != null;
    }

    /**
     * Gets the value cast to the specified type.
     *
     * @param <T>        the target type
     * @param targetType the class of the target type
     * @return the value cast to target type
     * @throws ClassCastException if the value cannot be cast to target type
     */
    @SuppressWarnings("unchecked")
    public <T> T getValueAs(Class<T> targetType) {
        return (T) value;
    }
}
