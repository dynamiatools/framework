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
package tools.dynamia.navigation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class JavaModuleBuilderTest {

    @Test
    public void testBuild() {
        JavaModuleBuilder builder = new JavaModuleBuilder("Module");

        builder.addGroup("Group1")
                .addPage("page1", "The Page 1", "the/path/page")
                .addPage("page2", "The Page 2", "the/path/page")
                .addPage("page3", "The Page 3", "the/path/page");

        builder.addGroup("grp2", "Group2")
                .addPage("page1", "The Page 1", "the/path/page")
                .addPage("page2", "The Page 2", "the/path/page")
                .addPage("page3", "The Page 3", "the/path/page");

        Module module = builder.build();
        assertEquals(2, module.getPageGroups().size());
        assertEquals(3, module.getFirstPageGroup().getPages().size());
    }

    @Test
    public void testBuildWithPageGroup() {
        Module module = new Module("customers", "Customers")
                .addPageGroup(new PageGroup("page1", "The Page 1", "the/path/page")
                        .addPage(new Page("page2", "The Page 2", "the/path/page"),
                                new Page("page3", "The Page 3", "the/path/page"),
                                new Page("page4", "The Page 4", "the/path/page")
                        )
                );


    }
}
