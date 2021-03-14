/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import tools.dynamia.domain.EntityHandler;
import tools.dynamia.domain.services.CrudService;

import java.util.List;

import static tools.dynamia.domain.EntityHandler.handle;
import static tools.dynamia.domain.query.QueryConditions.gt;
import static tools.dynamia.domain.query.QueryConditions.isNotNull;
import static tools.dynamia.domain.query.QueryParameters.with;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/applicationContext.xml")
public class JpaCrudServiceTest {

    @Autowired
    @Qualifier("jpacrud")
    private CrudService crudService;

    @Test
    @Transactional
    public void shouldUpdate4() {
        for (int i = 0; i < 4; i++) {
            DummyEntity ent = new DummyEntity("Dummy" + i);
            crudService.save(ent);
        }

        int result = crudService.batchUpdate(DummyEntity.class, "name", "THE_DUMMY", with("name", isNotNull()));
        Assert.assertEquals(4, result);

        List<DummyEntity> dummies = crudService.find(DummyEntity.class, with("name", "THE_DUMMY"));
        Assert.assertEquals(4, dummies.size());

    }

    @Test
    @Transactional
    public void findByFieldsTestResultShouldBeEmpty() {

        List<DummyEntity> result = crudService.findByFields(DummyEntity.class, "xxx", with("id", gt(10L)), "name");
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    @Transactional
    public void shouldFind4UsingStaticHelperMethods() {
        for (int i = 0; i < 4; i++) {
            DummyEntity ent = new DummyEntity("Dummy" + i);
            crudService.save(ent);
        }

        List<DummyEntity> result = handle(DummyEntity.class).findAll();
        Assert.assertEquals(4, result.size());
    }

    @Test
    @Transactional
    public void shouldFindByNameUsingStaticHelperMethods() {
        for (int i = 0; i < 4; i++) {
            DummyEntity ent = new DummyEntity("Dummy" + i);
            crudService.save(ent);
        }

        List<DummyEntity> result = DummyEntity.findByName("Dummy0");
        Assert.assertEquals(1, result.size());

        DummyEntity dummyEntity = result.get(0);
        Assert.assertEquals("Dummy0", dummyEntity.getName());
    }
}
