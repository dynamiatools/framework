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
package tools.dynamia.commons.collect;

import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.reflect.PropertyInfo;
import tools.dynamia.commons.reflect.ReflectionException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Helper class to work with collections
 *
 * @author Mario A. Serrano Leones
 */
public class CollectionsUtils {

    /**
     * Group a collection in sub collections with groupSize. Each element in
     * resulting collection is a CollectionWrapper object The result collection
     * is the same type of collection passes as parameters.
     *
     * @param collection the collection
     * @param groupSize  the group size
     * @return the collection
     */
    @SuppressWarnings("unchecked")
    public static Collection<CollectionWrapper> group(Collection collection, int groupSize) {

        Class<? extends Collection> collectionClass = collection.getClass();
        Collection<CollectionWrapper> groups = null;
        try {
            //noinspection unchecked
            groups = BeanUtils.newInstance(collectionClass);
        } catch (ReflectionException e) {
            groups = new ArrayList<>();
            collectionClass = ArrayList.class;
        }
        int i = 0;
        CollectionWrapper wrapper = null;
        for (Object object : collection) {
            if (i == groupSize) {
                i = 0;
            }

            if (i == 0) {
                //noinspection unchecked
                wrapper = new CollectionWrapper(BeanUtils.newInstance(collectionClass));
                groups.add(wrapper);
            }

            //noinspection unchecked
            wrapper.getCollection().add(object);
            i++;
        }

        return groups;
    }

    /**
     * Group a collection using a field from elements class
     *
     */
    public static Collection<CollectionWrapper> groupBy(Collection collection, Class elementClass, String fieldToGroup) {
        Class<? extends Collection> collectionClass = collection.getClass();

        Collection<CollectionWrapper> groups = null;
        PropertyInfo property = BeanUtils.getPropertyInfo(elementClass, fieldToGroup);
        try {
            //noinspection unchecked
            groups = BeanUtils.newInstance(collectionClass);
        } catch (ReflectionException e) {
            groups = new ArrayList<>();
            collectionClass = ArrayList.class;
        }
        Object grouper = null;
        CollectionWrapper wrapper = null;

        for (Object object : collection) {
            Object grouperTarget = null;
            if (property.is(Boolean.class)) {
                grouperTarget = BeanUtils.invokeBooleanGetMethod(object, fieldToGroup);
            } else {
                grouperTarget = BeanUtils.invokeGetMethod(object, fieldToGroup);
            }

            if (grouper != null && !grouper.equals(grouperTarget)) {
                grouper = null;
            }

            if (grouper == null && grouperTarget != null) {
                grouper = grouperTarget;
                //noinspection unchecked
                wrapper = new CollectionWrapper(BeanUtils.newInstance(collectionClass));
                wrapper.setName(grouperTarget.toString());
                wrapper.setValue(grouperTarget);
                groups.add(wrapper);
            }

            if (wrapper.getCollection() != null) {
                //noinspection unchecked
                wrapper.getCollection().add(object);
            }

        }
        return groups;
    }

    /**
     * Find first element from collection or null if is empty
     *
     */
    public static <T> T findFirst(Collection<T> collection) {
        if (collection.isEmpty()) {
            return null;
        }

        if (collection instanceof List) {
            return ((List<T>) collection).get(0);
        } else {
            return collection.stream().findFirst().get();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List iteratorToList(Iterator iterator) {
        List list = new ArrayList<>();
        iterator.forEachRemaining(list::add);
        return list;
    }

    private CollectionsUtils() {
    }

}
