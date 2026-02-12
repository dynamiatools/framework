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

package tools.dynamia.modules.saas.ui.action;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Button;
import org.zkoss.zul.Vlayout;
import tools.dynamia.actions.*;
import tools.dynamia.commons.ApplicableClass;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.crud.CrudState;
import tools.dynamia.modules.saas.api.AccountAdminAction;
import tools.dynamia.modules.saas.api.dto.AccountDTO;
import tools.dynamia.modules.saas.domain.Account;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.actions.ButtonActionRenderer;
import tools.dynamia.zk.util.ZKUtil;

import java.util.Comparator;

@InstallAction
public class ShowAccountAdminActions extends AbstractCrudAction {

    public ShowAccountAdminActions() {
        setName("Admin");
        setImage("settings");
    }

    @Override
    public CrudState[] getApplicableStates() {
        return CrudState.get(CrudState.READ);
    }

    @Override
    public ApplicableClass[] getApplicableClasses() {
        return ApplicableClass.get(Account.class);
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        Account account = (Account) evt.getData();
        if (account != null) {
            account = crudService().reload(account);
            AccountDTO info = account.toDTO();
            ActionEventBuilder evtBuilder = (source, params) -> {

                return new ActionEvent(info, this);
            };

            ActionLoader<AccountAdminAction> loader = new ActionLoader<>(AccountAdminAction.class);
            loader.setIgnoreRestrictions(true);
            Vlayout layout = new Vlayout();
            layout.setHflex("1");
            layout.setStyle("overflow: auto");
            ButtonActionRenderer defaultRenderer = new ButtonActionRenderer();
            defaultRenderer.setStyle("text-align: left");

            var actions = loader.load();
            actions.sort(Comparator.comparing(Action::getPosition).thenComparing(Action::getName));
            actions.forEach(a -> {
                ActionRenderer renderer = a.getRenderer() == null ? defaultRenderer : a.getRenderer();
                Object component = Actions.render(renderer, a, evtBuilder);
                if (component instanceof Button) {
                    if (a.getAttribute("type") != null) {
                        ((Button) component).setZclass("btn btn-" + a.getAttribute("type") + " btn-block");
                    } else {
                        ((Button) component).setZclass("btn btn-default btn-block");
                    }
                }
                layout.appendChild((Component) component);
            });

            var win = ZKUtil.showDialog("Actions for " + info.getName(), layout, "500px", "500px");


        } else {
            UIMessages.showMessage("Select account", MessageType.WARNING);
        }

    }

}
