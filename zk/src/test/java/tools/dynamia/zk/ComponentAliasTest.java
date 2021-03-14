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
package tools.dynamia.zk;

import org.junit.Test;
import tools.dynamia.zk.ui.Colorbox;
import tools.dynamia.zk.ui.Iconbox;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class ComponentAliasTest {

    @Test
    public void checkColoboxAlias() {
        Class expected = Colorbox.class;
        Class result = ComponentAliasIndex.getInstance().get("colorbox");

        assertEquals(expected, result);
    }

    @Test
    public void checkIconboxAlias() {
        Class expected = Iconbox.class;
        Class result = ComponentAliasIndex.getInstance().get("iconbox");

        assertEquals(expected, result);
    }

}
