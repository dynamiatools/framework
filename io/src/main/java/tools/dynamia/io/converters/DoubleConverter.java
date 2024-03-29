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
package tools.dynamia.io.converters;


import tools.dynamia.integration.sterotypes.Provider;

/**
 * The Class DoubleConverter.
 *
 * @author Mario A. Serrano Leones
 */
@Provider
public class DoubleConverter implements Converter<Double> {

    /* (non-Javadoc)
     * @see com.dynamia.tools.io.converters.Converter#getTargetClass()
     */
    @Override
    public Class<Double> getTargetClass() {
        return Double.class;
    }

    /* (non-Javadoc)
     * @see com.dynamia.tools.io.converters.Converter#toString(java.lang.Object)
     */
    @Override
    public String toString(Double value) {
        return String.valueOf(value);
    }

    /* (non-Javadoc)
     * @see com.dynamia.tools.io.converters.Converter#toObject(java.lang.String)
     */
    @Override
    public Double toObject(String value) {
        return Double.valueOf(value);
    }
}
