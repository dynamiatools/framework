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
package tools.dynamia.zk.ui.model;

import org.zkoss.bind.Binder;
import tools.dynamia.commons.reflect.PropertyInfo;
import tools.dynamia.domain.query.QueryCondition;

public class FilterField {

    private final PropertyInfo info;
    private String label;
    private final QueryCondition condition;
    private final String path;
    private final Binder binder;

    public FilterField(String path, PropertyInfo info, String label, QueryCondition condition, Binder binder) {
        super();
        this.path = path;
        this.info = info;
        this.label = label;
        this.condition = condition;
        this.binder = binder;
    }

    public String getPath() {
        return path;
    }

    public String getFullPath() {
        if (path != null) {
            return path + "." + info.getName();
        } else {
            return info.getName();
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public PropertyInfo getInfo() {
        return info;
    }

    public QueryCondition getCondition() {
        return condition;
    }

    public Binder getBinder() {
        return binder;
    }

}
