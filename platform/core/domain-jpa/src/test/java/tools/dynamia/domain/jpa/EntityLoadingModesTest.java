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
package tools.dynamia.domain.jpa;

import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.dynamia.commons.BeanMap;
import tools.dynamia.domain.query.BeanMapEntityMapper;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.DomainUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * How entities loaded for UI components behave with and without Open Persistence In View.
 * <p>
 * "Without OSIV" is simulated by running the query with no surrounding transaction: the persistence context is closed
 * when the CrudService returns, like a view rendered without OSIV. "With OSIV" runs the query and the reads inside one
 * transaction, which keeps the persistence context open the whole time.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = JpaTestConfig.class)
public class EntityLoadingModesTest {

    @Autowired
    private CrudService crudService;
    @Autowired
    private PlatformTransactionManager txManager;

    private TransactionTemplate tx;

    @BeforeEach
    public void setUp() {
        tx = new TransactionTemplate(txManager);
        tx.executeWithoutResult(s -> {
            MapperCategory books = crudService.save(new MapperCategory("Books"));
            crudService.save(new MapperItem("alpha", books));
            crudService.save(new MapperItem("beta", null));
        });
    }

    @AfterEach
    public void tearDown() {
        tx.executeWithoutResult(s -> {
            crudService.executeQuery("select i from MapperItem i").forEach(i -> crudService.delete(i));
            crudService.executeQuery("select c from MapperCategory c order by c.id desc").forEach(c -> crudService.delete(c));
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<?> findRows(QueryParameters params) {
        return crudService.find((Class) MapperItem.class, params);
    }

    private MapperItem loadRow(BeanMap row) {
        return (MapperItem) crudService.load(row.getBeanClass(), (java.io.Serializable) row.getId());
    }

    private static QueryParameters byName(String name) {
        return QueryParameters.with("name", name);
    }

    // ---------------------------------------------------------------- initializeEntity mapper (combobox, radiogroup)

    @Test
    public void withoutOsivInitializeMapperKeepsEntitiesUsableAfterTheSessionCloses() {
        List<?> result = crudService.find(MapperItem.class, byName("alpha").mapWith(DomainUtils::initializeEntity));

        MapperItem item = assertInstanceOf(MapperItem.class, result.getFirst());
        assertEquals("Books", item.getCategory().getName());
    }

    @Test
    public void withoutOsivInitializeMapperUnproxiesToOneRelations() {
        List<?> result = crudService.find(MapperItem.class, byName("alpha").mapWith(DomainUtils::initializeEntity));

        MapperItem item = (MapperItem) result.getFirst();
        assertEquals(MapperCategory.class, item.getCategory().getClass(), "to-one must be the real class, not a proxy");
    }

    @Test
    public void withoutOsivInitializeMapperHandlesNullRelations() {
        List<?> result = crudService.find(MapperItem.class, byName("beta").mapWith(DomainUtils::initializeEntity));

        assertNull(((MapperItem) result.getFirst()).getCategory());
    }

    @Test
    public void withoutOsivAndWithoutMapperLazyRelationsFail() {
        List<MapperItem> result = crudService.find(MapperItem.class, byName("alpha"));

        assertThrows(LazyInitializationException.class, () -> result.getFirst().getCategory().getName());
    }

    @Test
    public void withOsivPlainQueryReturnsUsableEntities() {
        tx.executeWithoutResult(s -> {
            List<MapperItem> result = crudService.find(MapperItem.class, byName("alpha"));

            assertEquals("Books", result.getFirst().getCategory().getName());
        });
    }

    @Test
    public void withOsivInitializeMapperReturnsTheSameKindOfEntities() {
        tx.executeWithoutResult(s -> {
            List<?> result = crudService.find(MapperItem.class, byName("alpha").mapWith(DomainUtils::initializeEntity));

            MapperItem item = assertInstanceOf(MapperItem.class, result.getFirst());
            assertEquals("Books", item.getCategory().getName());
        });
    }

    @Test
    public void initializedEntitiesAreEqualToTheirReloadedCopy() {
        MapperItem mapped = (MapperItem) crudService.find(MapperItem.class,
                byName("alpha").mapWith(DomainUtils::initializeEntity)).getFirst();
        MapperItem loaded = crudService.find(MapperItem.class, mapped.getId());

        assertEquals(mapped, loaded);
        assertEquals(mapped.hashCode(), loaded.hashCode());
    }

    // ---------------------------------------------------------------- BeanMap rows (pickers, tables) + load

    @Test
    public void withoutOsivBeanMapRowIsResolvedToTheEntityWithLoad() {
        BeanMap row = (BeanMap) findRows(
                byName("alpha").mapWith(new BeanMapEntityMapper("id", "name", "category.name"))).getFirst();

        assertEquals("alpha", row.get("name"));
        assertEquals("Books", row.get("category.name"));

        MapperItem entity = loadRow(row);

        assertEquals("alpha", entity.getName());
        assertEquals("Books", entity.getCategory().getName(), "load must initialize to-one relations");
        assertEquals(MapperCategory.class, entity.getCategory().getClass());
    }

    @Test
    public void withOsivMapperStillMapsToBeanMapsThatLoadBackToTheSameEntity() {
        tx.executeWithoutResult(s -> {
            BeanMap row = (BeanMap) findRows(
                    byName("alpha").mapWith(new BeanMapEntityMapper("id", "name"))).getFirst();

            MapperItem entity = loadRow(row);

            assertEquals(row.getId(), entity.getId());
            assertEquals("Books", entity.getCategory().getName());
        });
    }

    @Test
    public void loadOfMissingIdReturnsNull() {
        assertNull(crudService.load(MapperItem.class, Long.MAX_VALUE));
    }
}
