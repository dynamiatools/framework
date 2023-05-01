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
package tools.dynamia.zk.reports.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import tools.dynamia.actions.ActionGroup;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.commons.ClassMessages;
import tools.dynamia.commons.Messages;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.crud.CrudState;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.reports.ReportParametersProvider;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.util.ZKUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@InstallAction
public class ViewReportParametersAction extends AbstractCrudAction {

    @Autowired
    private CrudService crudService;


    private static final ClassMessages MESSAGES = ClassMessages.get(ExportExcelAction.class);

    public ViewReportParametersAction() {
        setName(Messages.get(getClass(), "viewParams"));
        setImage("fa-code");
        setGroup(ActionGroup.get("EXPORT"));

    }


    @Override
    public void actionPerformed(CrudActionEvent evt) {
        Object data = evt.getData();
        if (data != null) {
            try {
                if (DomainUtils.isEntity(data)) {
                    data = crudService.reload(data);
                }

                Map<String, Object> params = ReportParametersProvider.loadParameters(data);
                if (params != null && !params.isEmpty()) {
                    Listbox listbox = new Listbox();
                    listbox.setVflex("1");
                    listbox.setHflex("1");

                    listbox.appendChild(new Listhead());
                    listbox.getListhead().appendChild(new Listheader(Messages.get(getClass(), "paramName")));
                    listbox.getListhead().appendChild(new Listheader(Messages.get(getClass(), "paramValue")));
                    listbox.getListhead().appendChild(new Listheader(Messages.get(getClass(), "paramType")));


                    List<String> keys = new ArrayList<>(params.keySet());
                    Collections.sort(keys);
                    keys.forEach(k -> {
                        Listitem item = new Listitem();
                        item.appendChild(new Listcell(k));

                        Object value = params.get(k);
                        String pclass = "";
                        if (value != null) {
                            pclass = value.getClass().getName();
                        }

                        item.appendChild(new Listcell(String.valueOf(value)));
                        item.appendChild(new Listcell(pclass));
                        listbox.appendChild(item);
                    });
                    ZKUtil.showDialog(data + " Parameters", listbox, "50%", "50%");
                } else {
                    UIMessages.showMessage(Messages.get(getClass(), "paramsNotFound"), "Error", MessageType.WARNING, evt.getSource());
                }
            } catch (Exception e) {
                UIMessages.showMessage("Error: " + e.getMessage(), MessageType.ERROR);
            }
        } else {
            UIMessages.showMessage(Messages.get(getClass(), "paramsSelectOne"), MessageType.ERROR);
        }
    }


    @Override
    public CrudState[] getApplicableStates() {
        return CrudState.get(CrudState.READ);
    }
}
