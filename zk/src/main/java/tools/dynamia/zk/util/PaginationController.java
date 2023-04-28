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
package tools.dynamia.zk.util;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Paging;
import tools.dynamia.commons.Callback;
import tools.dynamia.domain.query.DataPaginator;

/**
 * Keep synchronize Dynamia DataPaginator and ZK Paging component
 * @author mario
 */
public class PaginationController {

    private final Paging paginator;
    private final Callback callback;
    private DataPaginator dataPaginator;

    /**
     *
     * @param paging
     * @param callback
     */
    public PaginationController(Paging paging, Callback callback) {
        this.paginator = paging;
        this.callback = callback;

        configurePaginator();
    }

    /**
     * You must call this method BEFORE the first
     * query, normally at receive some event like onClick$query
     */
    public void init() {
        if (dataPaginator != null) {
            dataPaginator.reset();
        }
        update();
    }

    /**
     * Re-synchronize the pagination. You must call this method AFTER the first
     * query, normally at receive some event like onClick$query
     *
     */
    public void update() {
        ZKUtil.synchronizePaginator(dataPaginator, paginator);
    }

    private void configurePaginator() {
        if (paginator != null) {
            dataPaginator = new DataPaginator();
            update();
            paginator.addEventListener("onPaging", evt -> {
                update();
                if (callback != null) {
                    callback.doSomething();
                }
            });
        }
    }

    public DataPaginator getDataPaginator() {
        return dataPaginator;
    }
}
