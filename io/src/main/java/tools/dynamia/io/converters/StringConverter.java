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
package tools.dynamia.io.converters;


import tools.dynamia.integration.sterotypes.Provider;

/**
 * The Class StringConverter.
 *
 * @author Mario A. Serrano Leones
 */
@Provider
public class StringConverter implements Converter<String> {

    /* (non-Javadoc)
     * @see tools.dynamia.io.converters.Converter#getTargetClass()
     */
    @Override
    public Class<String> getTargetClass() {
        return String.class;
    }

    /* (non-Javadoc)
     * @see tools.dynamia.io.converters.Converter#toString(java.lang.Object)
     */
    @Override
    public String toString(String value) {
        return value;
    }

    /* (non-Javadoc)
     * @see tools.dynamia.io.converters.Converter#toObject(java.lang.String)
     */
    @Override
    public String toObject(String value) {
        return value;
    }
}
