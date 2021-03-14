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
package tools.dynamia.zk.app.bstemplate;

import tools.dynamia.viewers.View;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.web.util.HttpUtils;
import tools.dynamia.zk.viewers.table.TableView;
import tools.dynamia.zk.viewers.table.TableViewRenderer;

import java.util.List;

public class BootstrapTableViewRenderer<T> extends TableViewRenderer<T> {

    @Override
    public View<List<T>> render(ViewDescriptor descriptor, List<T> value) {

        if (!descriptor.getParams().containsKey("showRowNumber")) {
            descriptor.addParam("showRowNumber", false);
        }

        TableView<T> view = (TableView<T>) super.render(descriptor, value);

        if (HttpUtils.isSmartphone()) {
            view.setSclass("tableview-mobile");
            view.getPagingChild().setMold(null);
        }
        return view;
    }
}
