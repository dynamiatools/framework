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
package tools.dynamia.zk.ui;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.ListModelList;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.search.SearchService;
import tools.dynamia.integration.search.SearchResult;
import tools.dynamia.web.util.HttpUtils;

import java.util.HashMap;
import java.util.List;

public class Searchbox extends Combobox {

    /**
     *
     */
    private static final long serialVersionUID = -3070761233489513310L;


    public Searchbox() {
        addEventListener(Events.ON_CHANGING, this::search);
        addEventListener(Events.ON_OK, this::open);
    }

    private void open(Event evt) {
        setValue(null);
        setSelectedItem(null);
        SearchResult result = (SearchResult) ((ListModelList) getModel()).getSelection().iterator().next();
        service().openGlobalSearchResult(result, (hostname, uri, parms) -> {
            String p = "";
            if (parms != null) {
                p = "?" + HttpUtils.formatRequestParams(new HashMap<>(parms));
            }
            Executions.getCurrent().sendRedirect(uri + p);

        });

    }

    private void search(Event evt) {
        InputEvent e = (InputEvent) evt;
        String query = e.getValue();
        if (query != null && query.length() >= 2) {
            List<SearchResult> results = service().search(query);
            if (results != null && !results.isEmpty()) {
                setModel(new ListModelList<>(results));
                open();
            }
        }
    }

    private SearchService service() {
        return Containers.get().findObject(SearchService.class);
    }

}
