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
 * Wrapper record for holding a value and its class type.
 * <p>
 * Useful for dynamic value handling, reflection, and type-safe operations where both the value and its type are needed.
 * Commonly used in bean utilities and dynamic property setters/getters.
 *
 * @param value      the value to wrap
 * @param valueClass the class type of the value
 * @author Mario A. Serrano Leones
 */
public record ValueWrapper(Object value, Class<?> valueClass) {

    /**
     * Constructs a new {@code ValueWrapper} with the given value and class type.
     *
     * @param value      the value to wrap
     * @param valueClass the class type of the value
     */
    public ValueWrapper {
    }

    /**
     * Returns the wrapped value.
     *
     * @return the value
     */
    @Override
    public Object value() {
        return value;
    }

    public Object getValue() {
        return value;
    }

    /**
     * Returns the class type of the wrapped value.
     *
     * @return the value class
     */
    @Override
    public Class<?> valueClass() {
        return valueClass;
    }
}
