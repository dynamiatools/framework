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
package tools.dynamia.domain;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;

public class IdGeneratorsTest {

    @BeforeClass
    public static void init() {
        SimpleObjectContainer container = new SimpleObjectContainer("IdGeneratorsContainers");
        container.addObject("stringIdGenerator", new StringIdGenerator());
        container.addObject("longIdGenerator", new LongIdGenerator());
        Containers.get().installObjectContainer(container);
    }

    @Test
    public void shouldGenerateStringId() {
        String id = IdGenerators.createId(String.class);
        Assert.assertNotNull(id);
    }

    @Test
    public void shouldGenerateLongId() {
        Long id = IdGenerators.createId(Long.class);
        Assert.assertNotNull(id);
    }

    @Test(expected = IdGeneratorNotFoundException.class)
    public void shouldThrowException() {
        IdGenerators.createId(Integer.class);
    }
}
