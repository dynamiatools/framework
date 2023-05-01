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

import org.zkoss.zul.Combobox;
import org.zkoss.zul.ListModelList;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.util.ZKUtil;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TimeZoneCombobox extends Combobox {

    /**
     *
     */
    private static final long serialVersionUID = 4710970528102748639L;

    static {
        ComponentAliasIndex.getInstance().add("timezonebox", TimeZoneCombobox.class);
        BindingComponentIndex.getInstance().put("selected", TimeZoneCombobox.class);
    }

    private String selected;

    public TimeZoneCombobox() {
        setReadonly(true);
        initModel();

    }

    private void initModel() {
        LocalDateTime dt = LocalDateTime.now();

        setItemRenderer((item, data, index) -> {

            String zoneId = (String) data;
            item.setValue(zoneId);

            ZoneId zone = ZoneId.of(zoneId);
            ZonedDateTime zdt = dt.atZone(zone);
            ZoneOffset offset = zdt.getOffset();

            String out = String.format("%s (%s)", zone, offset);
            item.setLabel(out);

        });

        Set<String> allZones = ZoneId.getAvailableZoneIds();
        List<String> zoneList = new ArrayList<>(allZones);
        Collections.sort(zoneList);

        ZKUtil.fillCombobox(this, zoneList, ZoneId.systemDefault().getId(), true);

    }

    public String getSelected() {
        selected = null;
        if (getSelectedItem() != null) {
            selected = getSelectedItem().getValue();
        }
        return selected;
    }

    public void setSelected(String selected) {
        if (selected != this.selected) {
            this.selected = selected;
            try {
                ListModelList model = (ListModelList) getModel();
                //noinspection unchecked
                model.addToSelection(selected);
            } catch (Exception e) {
                // ignore
            }
        }
    }

}
