package tools.dynamia.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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

        Assertions.assertNotNull(r.getId());
        Assertions.assertFalse(crudService.findAll(SomeEntity.class).isEmpty());
    }

    @Test
    public void shouldCreate_10_Entities() {
        CrudService crudService = new InMemoryCrudService();

        for (int i = 0; i < 10; i++) {
            var entity = new SomeEntity();
            entity.setName("Test " + i);
            var r = crudService.create(entity);
            Assertions.assertNotNull(r.getId());
        }

        List<SomeEntity> result = crudService.findAll(SomeEntity.class);
        Assertions.assertEquals(result.size(), 10);
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
        Assertions.assertNotNull(first);
        Assertions.assertEquals(first.getName(), "Test 0");
    }

    @Test
    public void shouldFilterByParamters() {
        CrudService crudService = new InMemoryCrudService();
        createSamples(crudService);
        List<SomeEntity> filtered = crudService.find(SomeEntity.class, QueryParameters.with("accountId", QueryConditions.eq(1L))
                .add("active", true));


        Assertions.assertEquals(filtered.size(), 5);
        filtered = crudService.find(SomeEntity.class, QueryParameters.with("age", 41));
        Assertions.assertEquals(filtered.size(), 1);
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

        Assertions.assertEquals(result.size(), 1);
    }

    @Test
    public void shouldDeleteAll() {
        CrudService crudService = new InMemoryCrudService();
        createSamples(crudService);
        crudService.deleteAll(SomeEntity.class);
        Assertions.assertTrue(crudService.findAll(SomeEntity.class).isEmpty());
    }

    @Test
    public void shouldUpdateEntity() {
        CrudService crudService = new InMemoryCrudService();
        var entity = crudService.create(new SomeEntity());
        Assertions.assertNotNull(entity.getId());

        entity.setName("Test Entity");
        entity.setAge(100);
        var result = crudService.update(entity);

        Assertions.assertEquals(result.getAge(), 100);
    }

    @Test
    public void shoudListEntityProperties() {
        CrudService crudService = new InMemoryCrudService();
        createSamples(crudService);
        List<String> names = crudService.getPropertyValues(SomeEntity.class, "name");
        System.out.println(names);
        Assertions.assertEquals(names.size(), 10);
    }

    @Test
    public void shouldValidatePersonName() {
        CrudService crudService = new InMemoryCrudService();
        Assertions.assertThrows(ValidationError.class, () -> crudService.create(new Person(null, 19)));
    }

    @Test
    public void shouldValidatePersonAge() {
        CrudService crudService = new InMemoryCrudService();
        Assertions.assertThrows(ValidationError.class, () -> crudService.create(new Person("Jhon", 15)));
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
        Assertions.assertEquals(young.getAge(), 20);
        Assertions.assertTrue(beforeCreateFired.get());
        Assertions.assertTrue(afterCreateFired.get());

    }

    @Test
    public void shouldUpdateCounters() {
        CrudService crudService = new InMemoryCrudService();

        OtherEntity entity = new OtherEntity();
        crudService.increaseCounter(entity, "counter");
        crudService.increaseCounter(entity, "counter");
        crudService.increaseCounter(entity, "counter");
        Assertions.assertEquals(entity.getCounter(), 3);

        crudService.deacreaseCounter(entity, "counter");
        Assertions.assertEquals(entity.getCounter(), 2);

        crudService.increaseCounter(entity, "otherCounter");
        crudService.increaseCounter(entity, "otherCounter");
        crudService.increaseCounter(entity, "otherCounter");
        Assertions.assertEquals(entity.getOtherCounter(), 3);

        crudService.deacreaseCounter(entity, "otherCounter");
        Assertions.assertEquals(entity.getOtherCounter(), 2);


        crudService.increaseCounter(entity, "anotherCounter");
        crudService.increaseCounter(entity, "anotherCounter");
        crudService.increaseCounter(entity, "anotherCounter");
        Assertions.assertEquals(entity.getAnotherCounter().longValue(), 3L);

        crudService.deacreaseCounter(entity, "anotherCounter");
        Assertions.assertEquals(entity.getAnotherCounter().longValue(), 2L);

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
