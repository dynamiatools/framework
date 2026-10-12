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

import org.hibernate.Hibernate;
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
import tools.dynamia.commons.collect.PagedList;
import tools.dynamia.domain.query.BeanMapEntityMapper;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Entity to BeanMap mapping must happen inside the CrudService transaction. These tests deliberately run WITHOUT a
 * test-managed transaction, so the persistence context is closed when the query returns, exactly like a view
 * rendered without Open Persistence In View.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = JpaTestConfig.class)
public class EntityMapperTest {

    private static final String[] PROPS = {"name", "category", "category.name", "tags", "label"};

    @Autowired
    private CrudService crudService;
    @Autowired
    private PlatformTransactionManager txManager;

    private TransactionTemplate tx;

    @BeforeEach
    public void setUp() {
        tx = new TransactionTemplate(txManager);
        tx.executeWithoutResult(s -> {
            MapperCategory media = crudService.save(new MapperCategory("Media"));
            MapperCategory books = new MapperCategory("Books");
            books.setParent(media);
            books = crudService.save(books);
            crudService.save(new MapperItem("alpha", books));
            crudService.save(new MapperItem("beta", null));
            MapperItem gamma = new MapperItem("gamma", books);
            gamma.getTags().add("red");
            gamma.getTags().add("blue");
            crudService.save(gamma);
        });
    }

    @AfterEach
    public void tearDown() {
        tx.executeWithoutResult(s -> {
            crudService.executeQuery("select i from MapperItem i").forEach(i -> crudService.delete(i));
            crudService.executeQuery("select c from MapperCategory c order by c.id desc").forEach(c -> crudService.delete(c));
        });
    }

    private BeanMap findMapped(String name) {
        QueryParameters params = QueryParameters.with("name", name).mapWith(new BeanMapEntityMapper(PROPS));
        List<?> result = crudService.find(MapperItem.class, params);
        assertEquals(1, result.size());
        return assertInstanceOf(BeanMap.class, result.get(0));
    }

    @Test
    public void withoutMapperLazyRelationFailsOutsideSession() {
        List<MapperItem> items = crudService.find(MapperItem.class, QueryParameters.with("name", "alpha"));
        assertEquals(1, items.size());
        assertThrows(LazyInitializationException.class, () -> items.get(0).getCategory().getName());
    }

    @Test
    public void withoutMapperResultsAreEntities() {
        List<?> items = crudService.find(MapperItem.class, QueryParameters.with("name", "alpha"));
        assertInstanceOf(MapperItem.class, items.get(0));
    }

    @Test
    public void mapsPlainAndTransientProperties() {
        BeanMap map = findMapped("alpha");

        assertEquals("alpha", map.get("name"));
        assertEquals("Item alpha", map.get("label"));
        assertEquals(MapperItem.class, map.getBeanClass());
        assertNotNull(map.getId());
        assertEquals("Item(alpha)", map.toString());
    }

    @Test
    public void mapsNestedPathUsingFlatKey() {
        BeanMap map = findMapped("alpha");
        assertEquals("Books", map.get("category.name"));
    }

    @Test
    public void keepsRelatedEntityAsInitializedObject() {
        BeanMap map = findMapped("alpha");

        MapperCategory category = assertInstanceOf(MapperCategory.class, map.get("category"));
        assertTrue(Hibernate.isInitialized(category));
        assertEquals("Books", category.getName());
    }

    @Test
    public void nullRelationMapsToNullWithoutError() {
        BeanMap map = findMapped("beta");

        assertNull(map.get("category"));
        assertNull(map.get("category.name"));
    }

    @Test
    public void initializesRequestedCollections() {
        BeanMap map = findMapped("gamma");

        List<?> tags = assertInstanceOf(List.class, map.get("tags"));
        assertTrue(Hibernate.isInitialized(tags));
        assertEquals(List.of("red", "blue"), tags);
    }

    @Test
    public void mapsEveryPageOfPaginatedResult() {
        QueryParameters params = new QueryParameters().paginate(1).orderBy("name")
                .mapWith(new BeanMapEntityMapper(PROPS));

        List<?> result = crudService.find(MapperItem.class, params);
        assertInstanceOf(PagedList.class, result);

        int count = 0;
        for (Object row : result) {
            BeanMap map = assertInstanceOf(BeanMap.class, row);
            assertNotNull(map.get("name"));
            count++;
        }
        assertEquals(3, count);
    }

    @Test
    public void mapperDoesNotChangeQueryFiltering() {
        QueryParameters params = new QueryParameters().mapWith(new BeanMapEntityMapper(PROPS));
        assertEquals(3, crudService.find(MapperItem.class, params).size());
    }

    private PagedList<?> pagedResult(QueryParameters params) {
        List<?> result = crudService.find(MapperItem.class, params);
        return assertInstanceOf(PagedList.class, result);
    }

    @Test
    public void changingPageLoadsMappedRows() {
        PagedList<?> paged = pagedResult(new QueryParameters().paginate(1).orderBy("name")
                .mapWith(new BeanMapEntityMapper(PROPS)));

        paged.getDataSource().setActivePage(2);
        BeanMap beta = assertInstanceOf(BeanMap.class, paged.getDataSource().getPageData().get(0));
        assertEquals("beta", beta.get("name"));

        paged.getDataSource().setActivePage(3);
        BeanMap gamma = assertInstanceOf(BeanMap.class, paged.getDataSource().getPageData().get(0));
        assertEquals("Books", gamma.get("category.name"));
    }

    @Test
    public void readOnlyPaginatedResultIsMapped() {
        PagedList<?> paged = pagedResult(new QueryParameters().paginate(2).orderBy("name").setReadOnly(true)
                .mapWith(new BeanMapEntityMapper(PROPS)));

        paged.getDataSource().setActivePage(2);
        assertInstanceOf(BeanMap.class, paged.getDataSource().getPageData().get(0));
    }

    @Test
    public void queryMetadataCarriesTheMapper() {
        PagedList<?> paged = pagedResult(new QueryParameters().paginate(2).orderBy("name")
                .mapWith(new BeanMapEntityMapper(PROPS)));
        var metadata = ((JpaPagedListDataSource<?>) paged.getDataSource()).getQueryMetadata();

        assertNotNull(metadata.getParameters().getMapper());
        assertInstanceOf(BeanMap.class, crudService.find(metadata).get(0));
    }

    @Test
    public void applyMapperNeverFlattensAPagedList() {
        PagedList<?> paged = pagedResult(new QueryParameters().paginate(1).orderBy("name")
                .mapWith(new BeanMapEntityMapper(PROPS)));
        int activePage = paged.getDataSource().getActivePage();

        List<?> result = JpaCrudService.applyMapper(null, new QueryParameters().mapWith(new BeanMapEntityMapper(PROPS)), paged);

        assertSame(paged, result);
        assertEquals(activePage, paged.getDataSource().getActivePage());
    }

    private MapperCategory mappedCategory(String... properties) {
        QueryParameters params = QueryParameters.with("name", "alpha").mapWith(new BeanMapEntityMapper(properties));
        BeanMap map = assertInstanceOf(BeanMap.class, crudService.find(MapperItem.class, params).get(0));
        return assertInstanceOf(MapperCategory.class, map.get("category"));
    }

    @Test
    public void threeLevelPathKeepsTheWholeChainUsable() {
        // binding "category.parent.name" is evaluated by ZK as bean.category.parent.name, after the session is closed
        MapperCategory category = mappedCategory("category", "category.parent.name");

        assertTrue(Hibernate.isInitialized(category.getParent()));
        assertEquals("Media", category.getParent().getName());
    }

    @Test
    public void threeLevelPathNotRequestedStaysLazy() {
        MapperCategory category = mappedCategory("category");

        assertFalse(Hibernate.isInitialized(category.getParent()));
        assertThrows(LazyInitializationException.class, () -> category.getParent().getName());
    }

    @Test
    public void threeLevelPathIsAlsoStoredUnderItsFlatKey() {
        BeanMap map = findMappedWith("alpha", "category", "category.parent.name");
        assertEquals("Media", map.get("category.parent.name"));
    }

    private BeanMap findMappedWith(String name, String... properties) {
        QueryParameters params = QueryParameters.with("name", name).mapWith(new BeanMapEntityMapper(properties));
        return assertInstanceOf(BeanMap.class, crudService.find(MapperItem.class, params).get(0));
    }
}
