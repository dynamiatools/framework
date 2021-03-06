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
package tools.dynamia.zk.actions;

import tools.dynamia.commons.Messages;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Datebox;
import tools.dynamia.actions.Action;
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.ActionEventBuilder;
import tools.dynamia.commons.MapBuilder;
import tools.dynamia.web.util.HttpUtils;

import java.util.Date;

/**
 * @author Mario A. Serrano Leones
 */
public class DateboxActionRenderer extends ZKActionRenderer<Datebox> {

    @Override
    public Datebox render(final Action action, final ActionEventBuilder actionEventBuilder) {
        final Datebox box = new Datebox(new Date());
        box.setTooltiptext(action.getLocalizedDescription(Messages.getDefaultLocale()));
        box.addEventListener(Events.ON_CHANGE, event -> {
            Date data = box.getValue();
            ActionEvent evt = actionEventBuilder.buildActionEvent(box, MapBuilder.put("date", data));
            evt.setData(data);
            action.actionPerformed(evt);
        });

        if (HttpUtils.isSmartphone()) {
            box.setWidth("100%");
        }
        super.configureProperties(box,action);
        return box;
    }

}
