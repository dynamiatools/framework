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
 * @author Mario A. Serrano Leones
 */
public class ModuleInstallerTest {

    /**
     * Test of install method, of class ModuleInstaller.
     */
    @Test
    public void shouldInstalleModules() {
        ModuleContainer container = new ModuleContainer();


        container.installModule(createModule1());
        container.installModule(createModule2());
        container.installModule(createModule3());

        assertEquals(1, container.getModuleCount());

        Module modFinal = container.getModuleById("mod");
        assertEquals(1, modFinal.getPageGroups().size());

        Page page3 = container.findPage("mod/grp1/page3");
        assertEquals("page3", page3.getId());

        Page config1 = container.findPage("mod/grp1/config/cfg1");
        assertEquals("cfg1", config1.getId());
    }


    @Test
    public void shouldGenerateVirtualPath() {
        var page = new Page("page", "Main Page", "the/page");

        var twoLevelModule = new Module("mod", "module")
                .addPageGroup(new PageGroup("group", "Group")
                        .addPageGroup(new PageGroup("subgroup", "Sub Group")
                                .addPage(page)));


        String expected = "mod/group/subgroup/page";
        String actual = page.getVirtualPath();
        assertEquals(expected, actual);

        expected = "module/group/sub-group/main-page";
        actual = page.getPrettyVirtualPath();
        assertEquals(expected, actual);
    }

    @Test
    public void shouldGenerateSuperNestedVirtualPath() {
        var page = new Page("page", "Main Page", "the/page");

        var superNested = new Module("mod", "module")
                .addPageGroup(new PageGroup("level1", "Level 1")
                        .addPageGroup(new PageGroup("level2", "Level 2")
                                .addPageGroup(new PageGroup("level3", "Level 3")
                                        .addPageGroup(new PageGroup("level4", "Level 4")
                                                .addPage(page)))));


        String expected = "mod/level1/level2/level3/level4/page";
        String actual = page.getVirtualPath();
        assertEquals(expected, actual);

        expected = "module/level-1/level-2/level-3/level-4/main-page";
        actual = page.getPrettyVirtualPath();
        assertEquals(expected, actual);
    }

    private Module createModule1() {
        Module m = new Module("mod", "module");
        m.addPageGroup(new PageGroup("grp1", "grupo")
                .addPage(
                        new Page("page1", "page1", "the/page"),
                        new Page("page2", "page2", "the/page/2")
                ));

        return m;
    }

    private Module createModule2() {
        Module m = new Module("mod", "module");
        m.addPageGroup(new PageGroup("grp1", "grupo")
                .addPage(new Page("page3", "page3", "the/page/3")));
        return m;
    }

    private Module createModule3() {
        Module m = new Module("mod", "module");
        m.addPageGroup(new PageGroup("grp1", "grupo")
                .addPageGroup(new PageGroup("config", "Config").addPage(
                                new Page("cfg1", "config1", "the/page"),
                                new Page("cfg2", "config2", "the/page/2")
                        )
                ));

        return m;
    }

}
