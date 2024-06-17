package tools.dynamia.domain;

import org.junit.Assert;
import org.junit.Test;
import tools.dynamia.domain.query.QueryConditions;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;

import java.util.List;

public class InMemoryCrudServiceTest {


    @Test
    public void shouldCreateEntity() {
        CrudService crudService = new InMemoryCrudService();
        var entity = new SomeEntity();
        entity.setName("Test");
        var r = crudService.create(entity);

        Assert.assertNotNull(r.getId());
        Assert.assertFalse(crudService.findAll(SomeEntity.class).isEmpty());
    }

    @Test
    public void shouldCreate_10_Entities() {
        CrudService crudService = new InMemoryCrudService();

        for (int i = 0; i < 10; i++) {
            var entity = new SomeEntity();
            entity.setName("Test " + i);
            var r = crudService.create(entity);
            Assert.assertNotNull(r.getId());
        }

        List<SomeEntity> result = crudService.findAll(SomeEntity.class);
        Assert.assertEquals(result.size(), 10);
    }

    @Test
    public void shouldFindFirstEntity() {
        CrudService crudService = new InMemoryCrudService();

        for (int i = 0; i < 10; i++) {
            var entity = new SomeEntity();
            entity.setName("Test " + i);
            crudService.create(entity);
        }

        SomeEntity first = crudService.findFirst(SomeEntity.class);
        Assert.assertNotNull(first);
        Assert.assertEquals(first.getName(), "Test 0");
    }

    @Test
    public void shouldFilterByParamters() {
        CrudService crudService = new InMemoryCrudService();
        createSamples(crudService);
        List<SomeEntity> filtered = crudService.find(SomeEntity.class, QueryParameters.with("accountId", QueryConditions.eq(1L))
                .add("active", true));


        Assert.assertEquals(filtered.size(), 5);

        filtered = crudService.find(SomeEntity.class, QueryParameters.with("age", 41));


        Assert.assertEquals(filtered.size(), 1);
    }

    @Test
    public void shouldDeleteAll() {
        CrudService crudService = new InMemoryCrudService();
        createSamples(crudService);
        crudService.deleteAll(SomeEntity.class);
        Assert.assertTrue(crudService.findAll(SomeEntity.class).isEmpty());
    }

    @Test
    public void shouldUpdateEntity() {
        CrudService crudService = new InMemoryCrudService();
        var entity = crudService.create(new SomeEntity());
        Assert.assertNotNull(entity.getId());

        entity.setName("Test Entity");
        entity.setAge(100);
        var result = crudService.update(entity);

        Assert.assertEquals(result.getAge(), 100);
    }

    @Test
    public void shoudListEntityProperties() {
        CrudService crudService = new InMemoryCrudService();
        createSamples(crudService);
        List<String> names = crudService.getPropertyValues(SomeEntity.class, "name");
        System.out.println(names);
        Assert.assertEquals(names.size(), 10);
    }

    private static void createSamples(CrudService crudService) {
        for (int i = 0; i < 10; i++) {
            var entity = new SomeEntity();
            entity.setName("Test " + i);
            entity.setAge(1 + (i * 10));
            entity.setActive(i % 2 == 0);
            entity.setAccountId(i % 2 == 0 ? 1L : 2L);
            crudService.create(entity);
        }
    }
}
