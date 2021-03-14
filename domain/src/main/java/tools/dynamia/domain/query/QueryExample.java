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
package tools.dynamia.domain.query;

import tools.dynamia.domain.AbstractEntity;
import tools.dynamia.domain.util.AbstractContactInfo;
import tools.dynamia.domain.util.DomainUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * The Class QueryExample.
 *
 * @author Mario A. Serrano Leones
 */
public class QueryExample {

    /**
     * The example.
     */
    private Object example;

    /**
     * The exclude zeroes.
     */
    private boolean excludeZeroes;

    /**
     * The exclude falses.
     */
    private boolean excludeFalses;

    /**
     * The excluded fields.
     */
    private Set<String> excludedFields = new HashSet<>();

    /**
     * Instantiates a new query example.
     *
     * @param example the example
     */
    public QueryExample(Object example) {
        this(example, true, true);
    }

    /**
     * Instantiates a new query example.
     *
     * @param example       the example
     * @param excludeZeroes the exclude zeroes
     * @param excludeFalses the exclude falses
     */
    public QueryExample(Object example, boolean excludeZeroes, boolean excludeFalses) {
        this.example = example;
        this.excludeZeroes = excludeZeroes;
        this.excludeFalses = excludeFalses;
    }

    /**
     * Instantiates a new query example.
     *
     * @param example the example
     * @param exclude the exclude
     */
    public QueryExample(Object example, String... exclude) {
        this(example);
        for (String fieldName : exclude) {
            exclude(fieldName);
        }
    }

    /**
     * Builds the.
     *
     * @return the query parameters
     */
    public QueryParameters build() {

        QueryParameters params = new QueryParameters();

        Field[] fields = example.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {

                if (isValidFieldName(field.getName()) && isNonExcluded(field.getName()) && DomainUtils.isPersitable(field)) {
                    field.setAccessible(true);
                    Object value = field.get(example);
                    if (isValidFieldValue(value) && isNonZero(value) && isNonFalse(value)) {

                        if (value instanceof AbstractContactInfo) {
                            parseContactInfo(field.getName(), value, params);
                        } else {
                            params.put(field.getName(), value);
                        }
                    }
                }
            } catch (Exception e) {
                throw new QueryExampleException("Error building example paramaters", e);
            }
        }
        return params;
    }

    /**
     * Exclude.
     *
     * @param fieldName the field name
     * @return the query example
     */
    public QueryExample exclude(String fieldName) {
        excludedFields.add(fieldName);
        return this;
    }

    /**
     * Checks if is valid field name.
     *
     * @param fieldName the field name
     * @return true, if is valid field name
     */
    private boolean isValidFieldName(String fieldName) {
        return !fieldName.equalsIgnoreCase("serialVersionUID")
                && !fieldName.equalsIgnoreCase("creationDate")
                && !fieldName.equalsIgnoreCase("creationTime");
    }

    /**
     * Checks if is valid field value.
     *
     * @param value the value
     * @return true, if is valid field value
     */
    private boolean isValidFieldValue(Object value) {
        if (value instanceof AbstractEntity) {
            AbstractEntity ent = (AbstractEntity) value;
            if (ent.getId() != null) {
                return true;
            }
        }

        return value != null
                && !value.toString().isEmpty()
                && !(value instanceof Collection);
    }

    /**
     * Checks if is non zero.
     *
     * @param value the value
     * @return true, if is non zero
     */
    private boolean isNonZero(Object value) {
        if (!excludeZeroes) {
            return true;
        }
        if (value instanceof Number) {
            Number num = (Number) value;
            return num.longValue() != 0;
        }
        return true;
    }

    /**
     * Checks if is non false.
     *
     * @param value the value
     * @return true, if is non false
     */
    private boolean isNonFalse(Object value) {
        if (!excludeFalses) {
            return true;
        }

        if (value instanceof Boolean) {
            return Boolean.TRUE.equals(value);
        } else {
            return true;
        }
    }


    /**
     * Checks if is non excluded.
     *
     * @param fieldName the field name
     * @return true, if is non excluded
     */
    private boolean isNonExcluded(String fieldName) {
        if (excludedFields != null) {
            return !excludedFields.contains(fieldName);
        }
        return false;
    }

    /**
     * Parses the contact info.
     *
     * @param name   the name
     * @param obj    the obj
     * @param params the params
     * @throws Exception the exception
     */
    private void parseContactInfo(String name, Object obj, QueryParameters params) throws Exception {
        Field fields[] = AbstractContactInfo.class.getDeclaredFields();
        for (Field field : fields) {
            if (isValidFieldName(field.getName())) {
                field.setAccessible(true);
                Object value = field.get(obj);
                if (isValidFieldValue(value)) {
                    params.put(name + "." + field.getName(), value);
                }
            }
        }
    }
}
