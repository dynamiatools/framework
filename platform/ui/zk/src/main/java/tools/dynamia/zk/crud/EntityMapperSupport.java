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
package tools.dynamia.zk.crud;

import tools.dynamia.commons.BeanMap;
import tools.dynamia.crud.ViewDescriptorProperties;
import tools.dynamia.domain.LoadPlan;
import tools.dynamia.domain.OpenPersistenceInViewProvider;
import tools.dynamia.domain.query.BeanMapEntityMapper;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.integration.Containers;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.util.Viewers;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Shared support for ZK components that query entities while Open Persistence In View is disabled. In that mode
 * the {@link CrudService} must map the results to read-only {@link BeanMap}s while the persistence context is still
 * open, and the real entity is only loaded (by id) when a row is actually selected.
 *
 * <pre>{@code
 * EntityMapperSupport.configure(params, descriptor);
 * List<?> rows = crudService.find(Item.class, params);
 * Item item = (Item) EntityMapperSupport.toEntity(crudService, rows.getFirst());
 * }</pre>
 */
public final class EntityMapperSupport {

    private EntityMapperSupport() {
    }

    /**
     * Tells whether Open Persistence In View is disabled (or not available at all).
     *
     * @return {@code true} if queries must return BeanMaps instead of attached entities
     */
    public static boolean isOpenPersistenceInViewDisabled() {
        OpenPersistenceInViewProvider provider = Containers.get().findObject(OpenPersistenceInViewProvider.class);
        return provider == null || provider.isDisabled();
    }

    /**
     * When Open Persistence In View is disabled, attaches a {@link BeanMapEntityMapper} built from the descriptor
     * to the parameters. A mapper set explicitly by the application is never replaced.
     *
     * @param params     the query parameters to configure
     * @param descriptor the view descriptor that defines the properties to copy, if {@code null} only the id and
     *                   the string representation are mapped
     * @return {@code true} if a mapper was attached by this call
     */
    public static boolean configure(QueryParameters params, ViewDescriptor descriptor) {
        if (!isOpenPersistenceInViewDisabled()) {
            return false;
        }
        if (params.getMapper() == null || params.getMapper() instanceof BeanMapEntityMapper) {
            String[] properties = descriptor != null ? ViewDescriptorProperties.propertiesOf(descriptor) : new String[]{"id"};
            params.mapWith(new BeanMapEntityMapper(properties));
            return true;
        }
        return false;
    }

    /**
     * Same as {@link #configure(QueryParameters, ViewDescriptor)} but with an explicit list of property paths
     * (the {@code id} is always mapped).
     *
     * @param params     the query parameters to configure
     * @param properties the property paths to copy, for example the fields a list shows
     * @return {@code true} if a mapper was attached by this call
     */
    public static boolean configure(QueryParameters params, String... properties) {
        if (!isOpenPersistenceInViewDisabled()) {
            return false;
        }
        if (params.getMapper() == null || params.getMapper() instanceof BeanMapEntityMapper) {
            Set<String> paths = new LinkedHashSet<>();
            paths.add("id");
            if (properties != null) {
                paths.addAll(Arrays.asList(properties));
            }
            params.mapWith(new BeanMapEntityMapper(paths.toArray(new String[0])));
            return true;
        }
        return false;
    }

    /**
     * When Open Persistence In View is disabled, attaches a mapper that returns the entities themselves, unproxied and
     * with their to-one relations initialized (see {@link DomainUtils#initializeEntity(Object)}) while the persistence
     * context is still open. It is meant for models whose items are assigned to entity properties (combobox, radiogroup),
     * where a read-only {@link BeanMap} cannot be used. A mapper set explicitly by the application is never replaced.
     *
     * @param params the query parameters to configure
     * @return {@code true} if a mapper was attached by this call
     */
    public static boolean configureEntities(QueryParameters params) {
        if (isOpenPersistenceInViewDisabled() && params.getMapper() == null) {
            params.mapWith(DomainUtils::initializeEntity);
            return true;
        }
        return false;
    }

    /**
     * The associations an entity must carry to be shown with the descriptor: its collection fields (for example the
     * child table of a form). The entity is loaded with them while the persistence context is open, so rendering the
     * view does not throw {@code LazyInitializationException} when Open Persistence In View is disabled.
     *
     * @param descriptor the view descriptor, may be null
     * @return the plan, empty when the descriptor has no collection fields
     */
    public static LoadPlan loadPlanOf(ViewDescriptor descriptor) {
        return descriptor == null ? LoadPlan.EMPTY : LoadPlan.of(ViewDescriptorProperties.collectionsOf(descriptor));
    }

    /**
     * Same as {@link #loadPlanOf(ViewDescriptor)} for the descriptor registered for the class under that name
     * ({@code "form"} for the default form). Answers an empty plan when there is no such descriptor.
     */
    public static LoadPlan loadPlanOf(Class<?> entityClass, String descriptorName) {
        try {
            return loadPlanOf(Viewers.findViewDescriptor(entityClass, descriptorName));
        } catch (RuntimeException e) {
            return LoadPlan.EMPTY;
        }
    }

    /**
     * Returns the real entity for a row that may be a read-only {@link BeanMap}, reloading it by id. Any other
     * object is returned unchanged.
     *
     * @param crudService the service used to load the entity
     * @param row         an entity or a BeanMap row, may be {@code null}
     * @return the entity, or the same row if it is not a BeanMap
     */
    public static Object toEntity(CrudService crudService, Object row) {
        if (row instanceof BeanMap beanMap && beanMap.getId() != null) {
            return crudService.load(beanMap.getBeanClass(), (Serializable) beanMap.getId());
        }
        return row;
    }
}
