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
package tools.dynamia.integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The Class SimpleObjectContainer.
 */
public class SimpleObjectContainer implements ObjectContainer {

    /**
     * The objects.
     */
    private final Map<Object, String> objects = new HashMap<>();

    private String name = getClass().getSimpleName();

    public SimpleObjectContainer() {
    }

    public SimpleObjectContainer(String name) {
        super();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * tools.dynamia.integration.ObjectContainer#getObject(java.lang.String,
	 * java.lang.Class)
     */
    @Override
    public <T> T getObject(String name, Class<T> type) {
        List<T> r = getObjects(type);
        for (T t : r) {
            String n = objects.get(t);
            if (name.equals(n)) {
                return t;
            }
        }
        return null;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see tools.dynamia.integration.ObjectContainer#getObject(java.lang.Class)
     */
    @Override
    public <T> T getObject(Class<T> type) {
        List<T> r = getObjects(type);
        if (!r.isEmpty()) {
            return r.get(0);
        } else {
            return null;
        }
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * tools.dynamia.integration.ObjectContainer#getObjects(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> getObjects(Class<T> type) {
        List<T> r = new ArrayList<>();
        if (!objects.isEmpty()) {
            for (Object obj : objects.keySet()) {
                if (type.isAssignableFrom(obj.getClass())) {
                    r.add((T) obj);
                }
            }
        }
        return r;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * tools.dynamia.integration.ObjectContainer#getObject(java.lang.String)
     */
    @Override
    public Object getObject(String name) {
        return objects.get(name);
    }

    /**
     * Adds the object.
     *
     * @param name the name
     * @param object the object
     */
    public void addObject(String name, Object object) {
        objects.put(object, name);
    }

    /**
     * Gets the objects count.
     *
     * @return the objects count
     */
    public int getObjectsCount() {
        return objects.size();
    }

    @Override
    public String toString() {
        return getName();
    }
}
