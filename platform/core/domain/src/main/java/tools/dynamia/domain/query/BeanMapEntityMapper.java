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
package tools.dynamia.domain.query;

import tools.dynamia.commons.BeanMap;
import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.domain.util.DomainUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Maps an entity to a read-only {@link BeanMap} inside the open persistence context (proxies are resolved through
 * {@link tools.dynamia.domain.EntityUtilsProvider}, so this class is persistence-agnostic). Each requested property path
 * (nested paths like {@code category.name} included) is stored under its flat dotted key. Related entities are
 * unproxied and kept as objects, and collections are copied into plain collections, so everything stays usable
 * after the session is closed. A null relation in a nested path yields {@code null}.
 *
 * <pre>{@code
 * QueryParameters params = QueryParameters.with("name", "x")
 *         .mapWith(new BeanMapEntityMapper("name", "category", "category.name"));
 * List<BeanMap> rows = crudService.find(Item.class, params);
 * }</pre>
 */
public class BeanMapEntityMapper implements EntityMapper<BeanMap> {

    private final String[] properties;

    /**
     * @param properties property paths to copy into the map
     */
    public BeanMapEntityMapper(String... properties) {
        this.properties = properties != null ? properties : new String[0];
    }

    /**
     * Maps the entity to a {@link BeanMap} with its id, class, string representation and requested properties.
     *
     * @param entity the entity, never null
     * @return the read-only map
     */
    @Override
    public BeanMap map(Object entity) {
        Object target = DomainUtils.unproxy(entity);
        BeanMap map = new BeanMap();
        map.setBeanClass(DomainUtils.getEntityClass(target));
        map.setName(map.getBeanClass().getSimpleName());
        map.setStringRepresentation(target.toString());
        map.setFields(properties);
        try {
            map.setId(DomainUtils.findEntityId(target));
        } catch (Exception e) {
            // not an identifiable entity, id stays null
        }

        for (String property : properties) {
            map.put(property, load(ObjectOperations.invokeGetMethod(target, property)));
        }
        return map;
    }

    private Object load(Object value) {
        if (value instanceof Map<?, ?> source) {
            Map<Object, Object> copy = new LinkedHashMap<>();
            source.forEach((k, v) -> copy.put(k, DomainUtils.unproxy(v)));
            return copy;
        } else if (value instanceof Set<?> source) {
            Set<Object> copy = new LinkedHashSet<>();
            source.forEach(v -> copy.add(DomainUtils.unproxy(v)));
            return copy;
        } else if (value instanceof Collection<?> source) {
            List<Object> copy = new ArrayList<>(source.size());
            source.forEach(v -> copy.add(DomainUtils.unproxy(v)));
            return copy;
        }
        return DomainUtils.unproxy(value);
    }
}
