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
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.domain.AbstractEntity;

/**
 *
 * Render dynamic cells for each field
 *
 * @author Mario A. Serrano Leones
 */
public class DynamicListItemRenderer implements ListitemRenderer<Object> {

    private String[] fields;

    @Override
    public void render(Listitem item, Object data, int index) {
        item.setValue(data);

        if (data instanceof AbstractEntity ent) {
            Listcell idCell = new Listcell(ent.getId().toString());
            idCell.setParent(item);
        }

        if (fields == null || fields.length == 0) {
            Listcell cell = new Listcell(BeanUtils.getInstanceName(data));
            cell.setParent(item);
        } else {
            for (String field : fields) {
                Object value = "";
                try {
                    value = BeanUtils.invokeGetMethod(data, field.trim());
                } catch (Exception e) {
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
