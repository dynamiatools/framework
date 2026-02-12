package tools.dynamia.domain;

import org.junit.Assert;
import org.junit.Test;
import tools.dynamia.domain.query.QueryConditions;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.CrudServiceListener;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
    public void shouldFilterByParamtersWithPathProperties() {
        CrudService crudService = new InMemoryCrudService();


        SomeEntity entity = new SomeEntity();
        entity.setName("Entity");
        entity.setActive(true);

        OtherEntity other = new OtherEntity();
        other.setName("Other");
        other.setActive(true);
        entity.setOtherEntity(other);

        crudService.create(entity);


        var result = crudService.find(SomeEntity.class, QueryParameters.with("active", true)
                .add("otherEntity.name", "Other")
                .add("otherEntity.active", true));

        Assert.assertEquals(result.size(), 1);
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

    @Test(expected = ValidationError.class)
    public void shouldValidatePersonName() {
        CrudService crudService = new InMemoryCrudService();
        crudService.create(new Person(null, 19));
    }

    @Test(expected = ValidationError.class)
    public void shouldValidatePersonAge() {
        CrudService crudService = new InMemoryCrudService();
        crudService.create(new Person("Jhon", 15));
    }

    @Test
    public void shouldFireListeners() {
        AtomicBoolean beforeCreateFired = new AtomicBoolean(false);
        AtomicBoolean afterCreateFired = new AtomicBoolean(false);

        var fixAgeListener = new CrudServiceListener<Person>() {
            @Override
            public void beforeCreate(Person entity) {
                beforeCreateFired.set(true);
                if (entity.getAge() < 18) {
                    System.out.println("Fixing Age");
                    entity.setAge(20);
                }
            }

            @Override
            public void afterCreate(Person entity) {
                afterCreateFired.set(true);
            }
        };

        CrudService crudService = new InMemoryCrudService(List.of(fixAgeListener));
        Person young = new Person("Mario", 15);
        crudService.create(young);
        Assert.assertEquals(young.getAge(), 20);
        Assert.assertTrue(beforeCreateFired.get());
        Assert.assertTrue(afterCreateFired.get());

    }

    @Test
    public void shouldUpdateCounters() {
        CrudService crudService = new InMemoryCrudService();

        OtherEntity entity = new OtherEntity();
        crudService.increaseCounter(entity, "counter");
        crudService.increaseCounter(entity, "counter");
        crudService.increaseCounter(entity, "counter");
        Assert.assertEquals(entity.getCounter(), 3);

        crudService.deacreaseCounter(entity, "counter");
        Assert.assertEquals(entity.getCounter(), 2);

        crudService.increaseCounter(entity, "otherCounter");
        crudService.increaseCounter(entity, "otherCounter");
        crudService.increaseCounter(entity, "otherCounter");
        Assert.assertEquals(entity.getOtherCounter(), 3);

        crudService.deacreaseCounter(entity, "otherCounter");
        Assert.assertEquals(entity.getOtherCounter(), 2);


        crudService.increaseCounter(entity, "anotherCounter");
        crudService.increaseCounter(entity, "anotherCounter");
        crudService.increaseCounter(entity, "anotherCounter");
        Assert.assertEquals(entity.getAnotherCounter().longValue(), 3L);

        crudService.deacreaseCounter(entity, "anotherCounter");
        Assert.assertEquals(entity.getAnotherCounter().longValue(), 2L);

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
