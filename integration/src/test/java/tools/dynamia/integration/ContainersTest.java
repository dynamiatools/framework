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
package tools.dynamia.integration;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collection;

import static org.junit.Assert.*;

public class ContainersTest {

    private SimpleObjectContainer soc;

    @Before
    public void init() {
        soc = new SimpleObjectContainer();
        soc.addObject("nombre", "mario");
        soc.addObject("apellidos", "serrano");
        soc.addObject("other", new BigDecimal("1000"));
        Containers.get().removeAllContainers();
        Containers.get().installObjectContainer(soc);
    }

    @Test
    public void shouldHasOne() {
        assertEquals(1, Containers.get().getInstalledContainers().size());
    }

    @Test
    public void findObjectByNameAndType() {
        String name = Containers.get().findObject("nombre", String.class);
        assertNotNull(name);
        assertEquals("mario", name);
    }

    @Test
    public void findObjectByType() {
        BigDecimal num = Containers.get().findObject(BigDecimal.class);
        assertNotNull(num);
        assertEquals(new BigDecimal("1000"), num);
    }

    @Test
    public void shouldContains3() {
        assertEquals(3, soc.getObjectsCount());
        assertEquals(3, soc.getObjects(Object.class).size());
    }

    @Test
    public void findObjects() {
        Collection<String> strings = soc.getObjects(String.class);
        assertNotNull(strings);
        assertTrue(!strings.isEmpty());
        assertEquals(2, strings.size());

        strings = Containers.get().findObjects(String.class);
        assertNotNull(strings);
        assertTrue(!strings.isEmpty());
        assertEquals(2, strings.size());
    }

}
