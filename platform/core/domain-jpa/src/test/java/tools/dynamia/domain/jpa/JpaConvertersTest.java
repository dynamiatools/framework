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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tools.dynamia.domain.services.CrudService;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = JpaTestConfig.class)
public class JpaConvertersTest {

    @Autowired
    private CrudService crudService;

    @Test
    public void testMapConverter() {
        var entity = new DummyEntityJson();
        entity.getData().put("name", "harold");
        entity.getData().put("age", 20);

        crudService.executeWithinTransaction(() -> crudService.create(entity));


        var other = crudService.find(DummyEntityJson.class, entity.getId());
        Assertions.assertNotNull(other);
        Assertions.assertNotNull(other.getData());
        Assertions.assertEquals("harold", other.getData().get("name"));
        Assertions.assertEquals(20, other.getData().get("age"));
    }

    @Test
    public void testListConverter() {
        var entity = new DummyEntityJson();
        entity.getAddresses().add(new Address("Main Av 123", "Int 001", "New York City", "New York"));
        entity.getAddresses().add(new Address("Cra 1", "Apto 123", "Medellin", "Antioquia"));

        crudService.executeWithinTransaction(() -> crudService.create(entity));


        var other = crudService.find(DummyEntityJson.class, entity.getId());
        Assertions.assertNotNull(other);
        Assertions.assertNotNull(other.getAddresses());
        Assertions.assertFalse(other.getAddresses().isEmpty());

        var first = other.getAddresses().stream().findFirst();
        Assertions.assertTrue(first.isPresent());

        Assertions.assertEquals("Main Av 123", first.get().getLine1());

    }
}
