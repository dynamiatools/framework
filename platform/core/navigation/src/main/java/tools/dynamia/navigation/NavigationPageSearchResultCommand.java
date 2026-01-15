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

import tools.dynamia.integration.search.SearchResultCommandExecutor;
import tools.dynamia.integration.search.SearchResult;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.sterotypes.Provider;

/**
 * @author Mario Serrano Leones
 */
@Provider
public class NavigationPageSearchResultCommand implements SearchResultCommandExecutor {

    @Override
    public void execute(String command, SearchResult result) {
        if (result.getData() instanceof Page) {
            NavigationManager navManager = Containers.get().findObject(NavigationManager.class);
            if (navManager != null) {
                navManager.setCurrentPage((Page) result.getData());
            }
        }
    }

}
