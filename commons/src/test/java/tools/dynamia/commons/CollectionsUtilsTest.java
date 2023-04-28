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
package tools.dynamia.commons;

import junit.framework.TestCase;
import tools.dynamia.commons.collect.CollectionWrapper;
import tools.dynamia.commons.collect.CollectionsUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class CollectionsUtilsTest extends TestCase {

    public void testCollectionGroup() {
        Collection<String> data = new ArrayList<>();
        for (int i = 0; i < 75; i++) {
            data.add("DATA-" + i);
        }

        Collection<CollectionWrapper> groups = CollectionsUtils.group(data, 3);

        assertEquals(25, groups.size());
        assertEquals(ArrayList.class, groups.getClass());

        data = new HashSet<>();
        for (int i = 0; i < 73; i++) {
            data.add("DATA-" + i);
        }

        groups = CollectionsUtils.group(data, 5);

        assertEquals(15, groups.size());
        assertEquals(HashSet.class, groups.getClass());

    }

}
