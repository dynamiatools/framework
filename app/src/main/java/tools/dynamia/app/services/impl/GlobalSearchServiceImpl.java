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
package tools.dynamia.app.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import tools.dynamia.app.GlobalSearchCommandExecutor;
import tools.dynamia.app.GlobalSearchProvider;
import tools.dynamia.app.GlobalSearchResult;
import tools.dynamia.app.services.GlobalSearchService;
import tools.dynamia.integration.Containers;
import tools.dynamia.web.util.RequestSender;

import java.util.ArrayList;
import java.util.List;

@Service
public class GlobalSearchServiceImpl implements GlobalSearchService {

    private static final int DEFAULT_MAX_RESULT = 20;

    @Autowired
    private List<GlobalSearchProvider> providers;


    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * tools.dynamia.app.services.impl.GlobalSearchService#search(java.lang.
	 * String)
     */
    @Override
    @Cacheable("globalSearch")
    public List<GlobalSearchResult> search(String query) {
        return search(query, DEFAULT_MAX_RESULT);
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * tools.dynamia.app.services.impl.GlobalSearchService#search(java.lang.
	 * String, int)
     */
    @Override
    @Cacheable("globalSearch")
    public List<GlobalSearchResult> search(String query, int maxResult) {
        List<GlobalSearchResult> results = new ArrayList<>();
        int counter = 0;
        for (GlobalSearchProvider provider : providers) {
            List<GlobalSearchResult> providerResult = provider.search(query);
            if (providerResult != null && !providerResult.isEmpty()) {
                for (GlobalSearchResult globalSearchResult : providerResult) {
                    results.add(globalSearchResult);
                    counter++;

                    if (counter >= maxResult) {
                        break;
                    }
                }
            }

            if (counter >= maxResult) {
                break;
            }
        }

        return results;

    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see tools.dynamia.app.services.impl.GlobalSearchService#
	 * openGlobalSearchResult (tools.dynamia.app.GlobalSearchResult,
	 * tools.dynamia.web.util.RequestSender)
     */
    @Override
    public void openGlobalSearchResult(GlobalSearchResult result, RequestSender requestSender) {
        if (result.getUri() != null && !result.getUri().isEmpty() && result.getCommand() == null) {
            if (requestSender != null) {
                requestSender.send(null, result.getUri(), result.getParameters());
            }
        } else if (result.getCommand() != null && !result.getCommand().isEmpty()) {
            sendCommandMessage(result);
        }
    }

    private void sendCommandMessage(GlobalSearchResult result) {
        Containers.get().findObjects(GlobalSearchCommandExecutor.class).forEach(c -> c.execute(result.getCommand(), result));
    }

}
