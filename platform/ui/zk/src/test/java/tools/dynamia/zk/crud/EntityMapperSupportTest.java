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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tools.dynamia.commons.BeanMap;
import tools.dynamia.domain.EntityUtilsProvider;
import tools.dynamia.domain.OpenPersistenceInViewProvider;
import tools.dynamia.domain.query.BeanMapEntityMapper;
import tools.dynamia.domain.query.EntityMapper;
import tools.dynamia.domain.query.Parameter;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Behaviour of {@link EntityMapperSupport} with Open Persistence In View enabled, disabled and not available.
 */
public class EntityMapperSupportTest {

    private static final Object LOADED = new Object();

    @AfterEach
    public void tearDown() {
        Containers.get().removeAllContainers();
    }

    private static void install(Boolean osivDisabled, Object... others) {
        SimpleObjectContainer container = new SimpleObjectContainer("entity-mapper-support-test");
        if (osivDisabled != null) {
            container.addObject(new OpenPersistenceInViewProvider() {
                @Override
                public boolean beforeView() {
                    return false;
                }

                @Override
                public void afterView(boolean participate) {
                }

                @Override
                public boolean isDisabled() {
                    return osivDisabled;
                }
            });
        }
        for (Object other : others) {
            container.addObject(other);
        }
        Containers.get().installObjectContainer(container);
    }

    /**
     * CrudService that records the calls to load(Class, Serializable) and answers {@link #LOADED}.
     */
    private static CrudService crudService(List<Object[]> loads) {
        return (CrudService) Proxy.newProxyInstance(CrudService.class.getClassLoader(), new Class[]{CrudService.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("load") && args.length == 2) {
                        loads.add(args);
                        return LOADED;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static class MarkerEntityUtils implements EntityUtilsProvider {
        @Override
        public Serializable findId(Object entity) {
            return null;
        }

        @Override
        public boolean isEntity(Object entity) {
            return false;
        }

        @Override
        public boolean isEntity(Class entityClass) {
            return false;
        }

        @Override
        public boolean isPersitable(Field field) {
            return false;
        }

        @Override
        public Class<? extends Parameter> getDefaultParameterClass() {
            return null;
        }

        @Override
        public Object initializeEntity(Object entity) {
            return "initialized:" + entity;
        }
    }

    // ------------------------------------------------------------------------------------------ OSIV detection

    @Test
    public void osivEnabledWhenProviderSaysSo() {
        install(false);
        assertFalse(EntityMapperSupport.isOpenPersistenceInViewDisabled());
    }

    @Test
    public void osivDisabledWhenProviderSaysSo() {
        install(true);
        assertTrue(EntityMapperSupport.isOpenPersistenceInViewDisabled());
    }

    @Test
    public void osivIsTreatedAsDisabledWithoutProvider() {
        install(null);
        assertTrue(EntityMapperSupport.isOpenPersistenceInViewDisabled());
    }

    // ------------------------------------------------------------------------------------------ BeanMap mapper

    @Test
    public void withOsivNoBeanMapMapperIsAttached() {
        install(false);
        QueryParameters params = new QueryParameters();

        assertFalse(EntityMapperSupport.configure(params, "name"));
        assertNull(params.getMapper());
    }

    @Test
    public void withoutOsivBeanMapMapperIsAttached() {
        install(true);
        QueryParameters params = new QueryParameters();

        assertTrue(EntityMapperSupport.configure(params, "name", "category.name"));
        assertInstanceOf(BeanMapEntityMapper.class, params.getMapper());
    }

    @Test
    public void withoutOsivBeanMapMapperWithNullDescriptorAndPropertiesStillMapsTheId() {
        install(true);
        QueryParameters params = new QueryParameters();

        assertTrue(EntityMapperSupport.configure(params, (String[]) null));
        assertInstanceOf(BeanMapEntityMapper.class, params.getMapper());

        QueryParameters noDescriptor = new QueryParameters();
        assertTrue(EntityMapperSupport.configure(noDescriptor, (tools.dynamia.viewers.ViewDescriptor) null));
        assertInstanceOf(BeanMapEntityMapper.class, noDescriptor.getMapper());
    }

    @Test
    public void withoutOsivACustomMapperIsNeverReplaced() {
        install(true);
        EntityMapper<String> custom = entity -> "custom";
        QueryParameters params = new QueryParameters().mapWith(custom);

        assertFalse(EntityMapperSupport.configure(params, "name"));
        assertSame(custom, params.getMapper());
    }

    @Test
    public void withoutOsivAnOldBeanMapMapperIsRefreshed() {
        install(true);
        QueryParameters params = new QueryParameters().mapWith(new BeanMapEntityMapper("name"));
        EntityMapper<?> before = params.getMapper();

        assertTrue(EntityMapperSupport.configure(params, "code"));
        assertNotSame(before, params.getMapper());
    }

    // ------------------------------------------------------------------------------------------ entity mapper

    @Test
    public void withOsivEntitiesAreLeftAsTheyAre() {
        install(false, new MarkerEntityUtils());
        QueryParameters params = new QueryParameters();

        assertFalse(EntityMapperSupport.configureEntities(params));
        assertNull(params.getMapper());
    }

    @Test
    public void withoutOsivEntitiesAreInitializedByTheEntityUtilsProvider() {
        install(true, new MarkerEntityUtils());
        QueryParameters params = new QueryParameters();

        assertTrue(EntityMapperSupport.configureEntities(params));
        assertEquals("initialized:row", params.getMapper().map("row"));
    }

    @Test
    public void withoutOsivEntitiesMapperNeverReplacesACustomMapper() {
        install(true, new MarkerEntityUtils());
        EntityMapper<String> custom = entity -> "custom";
        QueryParameters params = new QueryParameters().mapWith(custom);

        assertFalse(EntityMapperSupport.configureEntities(params));
        assertSame(custom, params.getMapper());
    }

    // ------------------------------------------------------------------------------------------ BeanMap -> entity

    @Test
    public void beanMapRowIsReloadedById() {
        List<Object[]> loads = new ArrayList<>();
        BeanMap row = new BeanMap();
        row.setBeanClass(String.class);
        row.setId(7L);

        Object entity = EntityMapperSupport.toEntity(crudService(loads), row);

        assertSame(LOADED, entity);
        assertEquals(1, loads.size());
        assertEquals(String.class, loads.getFirst()[0]);
        assertEquals(7L, loads.getFirst()[1]);
    }

    @Test
    public void beanMapWithoutIdIsReturnedUnchanged() {
        List<Object[]> loads = new ArrayList<>();
        BeanMap row = new BeanMap();
        row.setBeanClass(String.class);

        assertSame(row, EntityMapperSupport.toEntity(crudService(loads), row));
        assertTrue(loads.isEmpty());
    }

    @Test
    public void entitiesAndNullAreReturnedUnchangedWithoutQueries() {
        List<Object[]> loads = new ArrayList<>();
        CrudService service = crudService(loads);
        Object entity = new Object();

        assertSame(entity, EntityMapperSupport.toEntity(service, entity));
        assertNull(EntityMapperSupport.toEntity(service, null));
        assertTrue(loads.isEmpty());
    }
}
