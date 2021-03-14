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
 * The Class LongConverter.
 *
 * @author Mario A. Serrano Leones
 */
@Provider
public class LongConverter implements Converter<Long> {

    /* (non-Javadoc)
     * @see tools.dynamia.io.converters.Converter#getTargetClass()
     */
    @Override
    public Class<Long> getTargetClass() {
        return Long.class;
    }

    /* (non-Javadoc)
     * @see tools.dynamia.io.converters.Converter#toString(java.lang.Object)
     */
    @Override
    public String toString(Long value) {
        return String.valueOf(value);
    }

    /* (non-Javadoc)
     * @see tools.dynamia.io.converters.Converter#toObject(java.lang.String)
     */
    @Override
    public Long toObject(String value) {
        return Long.valueOf(value);
    }
}
