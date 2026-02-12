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

import org.springframework.beans.factory.annotation.Autowired;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.modules.saas.domain.Account;
import tools.dynamia.modules.saas.domain.AccountStatsData;
import tools.dynamia.modules.saas.services.AccountService;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.util.ZKUtil;
import tools.dynamia.zk.viewers.ui.Viewer;

import java.util.List;

@InstallAction
public class ViewAccountStatsAction extends AbstractCrudAction {

    @Autowired
    private AccountService service;

    public ViewAccountStatsAction() {
        setName("Stats");
        setImage("chart");
        setApplicableClass(Account.class);
        setMenuSupported(true);
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        Account account = (Account) evt.getData();
        if (account != null) {
            service.updateStats(account);
            List<AccountStatsData> stats = crudService().find(AccountStatsData.class, QueryParameters.with("account", account));

            if (stats.isEmpty()) {
                UIMessages.showMessage("No stats found", MessageType.WARNING);
                return;
            } else {
                Viewer viewer = new Viewer("table", AccountStatsData.class);
                viewer.setValue(stats);

                ZKUtil.showDialog("Stats: " + account, viewer, "60%", "60%");
            }
            UIMessages.showMessage("Account stats updated succesfully");
        } else {
            UIMessages.showMessage("Select account", MessageType.WARNING);
        }
    }
}
