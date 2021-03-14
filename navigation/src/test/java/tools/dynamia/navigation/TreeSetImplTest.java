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
package tools.dynamia.navigation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class TreeSetImplTest {

    @Test
    public void testTreeSet() {
        Module mod = new Module();
        PageGroup pg1 = new PageGroup("grp1", "grp1");
        PageGroup pg2 = new PageGroup("grp2", "grp2");
        mod.addPageGroup(pg1);
        mod.addPageGroup(pg2);

        assertFalse(pg1.equals(pg2));
        for (PageGroup pageGroup : mod.getPageGroups()) {
        }
        assertEquals(2, mod.getPageGroups().size());
    }
}
