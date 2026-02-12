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
import tools.dynamia.commons.ApplicableClass;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.crud.CrudState;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.modules.saas.domain.Account;
import tools.dynamia.modules.saas.domain.AccountLog;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.util.ZKUtil;
import tools.dynamia.zk.viewers.ui.Viewer;

import java.util.List;

@InstallAction
public class ViewAccountLogAction extends AbstractCrudAction {

    @Autowired
    private CrudService crudService;

    public ViewAccountLogAction() {
        setName("Logs");
        setMenuSupported(true);
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {

        Account account = (Account) evt.getData();
        if (account != null) {
            List<AccountLog> logs = crudService.find(AccountLog.class, QueryParameters.with("account", account)
                    .orderBy("date", false)
                    .paginate(50));

            if (logs.isEmpty()) {
                UIMessages.showMessage("No logs found", MessageType.WARNING);
            } else {
                Viewer viewer = new Viewer("table", AccountLog.class);
                viewer.setValue(logs);

                ZKUtil.showDialog("Logs: " + account, viewer, "80%", "70%");
            }


        }
    }

    @Override
    public CrudState[] getApplicableStates() {
        return CrudState.get(CrudState.READ);
    }

    @Override
    public ApplicableClass[] getApplicableClasses() {
        return ApplicableClass.get(Account.class);
    }
}
