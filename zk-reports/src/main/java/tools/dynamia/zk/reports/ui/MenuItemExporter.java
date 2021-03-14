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
package tools.dynamia.zk.reports.ui;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Menuitem;
import tools.dynamia.commons.Messages;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.ui.icons.IconSize;
import tools.dynamia.zk.reports.actions.ExportExcelAction;
import tools.dynamia.zk.util.ZKUtil;

public class MenuItemExporter extends Menuitem {

    /**
     *
     */
    private static final long serialVersionUID = 8131649134677019960L;
    private String target;

    public MenuItemExporter() {
        setLabel(Messages.get(ExportExcelAction.class, "export_excel"));
        ZKUtil.configureComponentIcon("export-xls", this, IconSize.SMALL);
        addEventListener(Events.ON_CLICK, evt -> export());

    }

    @Override
    public String getTarget() {
        return target;
    }

    @Override
    public void setTarget(String target) {
        this.target = target;
    }

    private void export() {
        Component targetComponent = getFellow(target);
        UIMessages.showMessage("En construccion " + targetComponent);
    }

}
