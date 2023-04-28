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

import java.io.Serializable;
import java.util.HashMap;

/**
 * This class represent a POJO object has a map. Each property is a key
 */
public class BeanMap extends HashMap<String, Object> implements Serializable {


    private Object id;
    private String name;
    private Class beanClass;
    private String stringRepresentation;


    private String[] fields;

    public BeanMap() {
    }


    public void load(Object bean) {
        beanClass = bean.getClass();
        name = beanClass.getSimpleName();
        stringRepresentation = bean.toString();
        putAll(BeanUtils.getValuesMaps("", bean));
    }

    public void set(String key, Object value) {
        put(key, value);
    }

    public <T> T get(String key) {
        return (T) super.get(key);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class getBeanClass() {
        return beanClass;
    }


    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }


    public String[] getFields() {
        return fields;
    }

    public void setFields(String[] fields) {
        this.fields = fields;
    }

    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
    }

    @Override
    public String toString() {
        if (stringRepresentation != null) {
            return stringRepresentation;
        } else if (name != null) {
            return name;
        } else {
            return super.toString();
        }
    }
}
