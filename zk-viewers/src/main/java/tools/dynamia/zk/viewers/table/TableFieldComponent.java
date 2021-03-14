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

package tools.dynamia.zk.viewers.table;

import org.zkoss.zk.ui.Component;

public class TableFieldComponent {

    private String fieldName;
    private Component component;

    public TableFieldComponent(String fieldName, Component component) {
        super();
        this.fieldName = fieldName;
        this.component = component;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Component getComponent() {
        return component;
    }
}
