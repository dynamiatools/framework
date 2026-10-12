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

import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import tools.dynamia.commons.BeanMap;
import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.domain.AbstractEntity;

/**
 *
 * Render dynamic cells for each field. Rows can be entities or read-only {@link BeanMap}s.
 *
 * @author Mario A. Serrano Leones
 */
public class DynamicListItemRenderer implements ListitemRenderer<Object> {

    private String[] fields;

    @Override
    public void render(Listitem item, Object data, int index) {
        item.setValue(data);

        Object id = null;
        if (data instanceof AbstractEntity ent) {
            id = ent.getId();
        } else if (data instanceof BeanMap beanMap) {
            id = beanMap.getId();
        }
        if (id != null) {
            Listcell idCell = new Listcell(id.toString());
            idCell.setParent(item);
        }

        if (fields == null || fields.length == 0) {
            Listcell cell = new Listcell(ObjectOperations.getInstanceName(data));
            cell.setParent(item);
        } else {
            for (String field : fields) {
                Object value = "";
                try {
                    String name = field.trim();
                    value = data instanceof BeanMap beanMap ? beanMap.get(name) : ObjectOperations.invokeGetMethod(data, name);
                } catch (Exception ignored) {
                }
                String cellValue = null;
                if (value != null) {
                    cellValue = value.toString();
                }
                Listcell cell = new Listcell(cellValue);
                cell.setParent(item);
            }
        }
    }

    public void setFields(String[] fields) {
        this.fields = fields;
    }
}
