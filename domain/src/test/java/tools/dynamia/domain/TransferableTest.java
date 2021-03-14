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

package tools.dynamia.domain;

import org.junit.Test;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.domain.query.Parameter;
import tools.dynamia.domain.test.OtherEntity;
import tools.dynamia.domain.test.SomeEntity;
import tools.dynamia.domain.test.SomeEntityDTO;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;

import java.io.Serializable;
import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

public class TransferableTest {

    @Test
    public void shouldAutoCreateDTO() {
        SimpleObjectContainer container = new SimpleObjectContainer();
        container.addObject("utils", new BasicEntityUtilsProvider());
        Containers.get().installObjectContainer(container);

        SomeEntity entity = new SomeEntity();
        entity.setName("mario");
        entity.setCompany("dynamia");
        entity.setAge(31);
        entity.setActive(true);
        entity.setId(1L);
        entity.setAccountId(100L);

        OtherEntity other = new OtherEntity();
        other.setId(10L);
        other.setName("Other");
        entity.setOtherEntity(other);


        SomeEntityDTO dto = entity.toDTO();

        assertEquals(entity.getId(), dto.getId());
        assertEquals(entity.getAccountId(), dto.getAccountId());
        assertEquals(entity.getName(), dto.getName());
        assertEquals(entity.getCompany(), dto.getCompany());
        assertEquals(entity.getAge(), dto.getAge());
        assertEquals(entity.isActive(), dto.isActive());
        assertEquals(entity.getOtherEntity().getId(), dto.getOtherEntityId());
        assertEquals(entity.getOtherEntity().getName(), dto.getOtherEntity());
    }

    class BasicEntityUtilsProvider implements EntityUtilsProvider {

        @Override
        public Serializable findId(Object entity) {
            return (Serializable) BeanUtils.invokeGetMethod(entity, "id");
        }

        @Override
        public boolean isEntity(Object entity) {
            return isEntity(entity.getClass());
        }

        @Override
        public boolean isEntity(Class entityClass) {
            return entityClass.getSimpleName().endsWith("Entity");
        }

        @Override
        public boolean isPersitable(Field field) {
            return false;
        }

        @Override
        public Class<? extends Parameter> getDefaultParameterClass() {
            return null;
        }
    }
}
