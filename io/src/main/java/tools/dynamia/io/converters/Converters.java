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

import tools.dynamia.integration.Containers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * The Class Converters.
 *
 * @author Mario A. Serrano Leones
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class Converters {

    /**
     * The Constant CACHE.
     */
    private static final Map<Class, Converter> CACHE = Collections.synchronizedMap(new HashMap<>());


    /**
     * Instantiates a new converters.
     */
    private Converters() {
    }

    /**
     * Get the defaul converter for specific class.
     *
     * @param <T> the generic type
     * @param clazz the clazz
     * @return the converter
     */
    public static <T> Converter<T> getConverter(Class<T> clazz) {
        if(CACHE.isEmpty()){
            Collection<Converter> converters = Containers.get().findObjects(Converter.class);
            if (converters != null) {
                for (Converter converter : converters) {
                    CACHE.put(converter.getTargetClass(), converter);
                }
            }
        }
        return CACHE.get(clazz);
    }

    /**
     * Convert the value to a proper string format.
     *
     * @param value the value
     * @return the string
     */
    public static String convert(Object value) {
        if (value == null) {
            return null;
        }
        Converter converter = null;
        if (value instanceof Class) {
            converter = getConverter((Class) value);
        } else {
            converter = getConverter(value.getClass());
        }

        if (converter != null) {
            return converter.toString(value);
        } else {
            throw new ConverterException("No converter found for type " + value.getClass());
        }
    }

    /**
     * Convert string value to Object class. For example "2014-04-01" to Date
     * object
     *
     * @param <T> the generic type
     * @param clazz the clazz
     * @param value the value
     * @return the t
     */
    public static <T> T convert(Class<T> clazz, String value) {
        if (value == null) {
            return null;
        }
        Converter<T> converter = getConverter(clazz);
        if (converter != null) {
            return converter.toObject(value);
        } else {
            throw new ConverterException("No converter found for type " + clazz);
        }
    }
}
