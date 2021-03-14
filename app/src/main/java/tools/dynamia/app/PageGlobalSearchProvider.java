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
package tools.dynamia.app;

import tools.dynamia.domain.IdGenerators;
import tools.dynamia.integration.Containers;
import tools.dynamia.navigation.NavigationManager;
import tools.dynamia.navigation.NavigationRestrictions;
import tools.dynamia.navigation.Page;

import java.util.List;
import java.util.stream.Collectors;

public class PageGlobalSearchProvider implements GlobalSearchProvider {

    public static final String PAGE_NAVIGATION_COMMAND = "gotoPage";
    public static final String PARAM_VIRTUAL_PATH = "virtualPath";

    @Override
    public String getId() {
        return "pageSearch";
    }

    @Override
    public String getName() {
        return "Pages";
    }

    @Override
    public List<GlobalSearchResult> search(String query) {
        NavigationManager navManager = Containers.get().findObject(NavigationManager.class);

        List<Page> pages = navManager.findPagesByName(query);
        if (pages != null && !pages.isEmpty()) {
            return pages.stream().filter(NavigationRestrictions::allowAccess).map(page -> {
                GlobalSearchResult result = new GlobalSearchResult();
                result.setCommand(PAGE_NAVIGATION_COMMAND);
                result.setUuid(IdGenerators.createId(String.class));
                try {
                    result.setName(page.getFullName());
                } catch (Exception e) {
                    result.setName(page.getName());
                }

                try {
                    result.setTitle(page.getPageGroup().getParentModule().getName() + ": " + page.getName());
                } catch (Exception e) {
                    result.setTitle(page.getName());
                }
                result.setDescription(page.getDescription());
                result.setUri("/page/" + page.getPrettyVirtualPath());
                result.setData(page);
                return result;
            }).collect(Collectors.toList());
        }

        return null;
    }

}
