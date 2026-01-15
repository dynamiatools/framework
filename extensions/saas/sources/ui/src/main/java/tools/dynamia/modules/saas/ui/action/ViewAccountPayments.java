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

import tools.dynamia.actions.ActionGroup;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.modules.saas.domain.Account;
import tools.dynamia.modules.saas.domain.AccountPayment;
import tools.dynamia.modules.saas.services.AccountService;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.util.ZKUtil;
import tools.dynamia.zk.viewers.ui.Viewer;

import java.util.List;

@InstallAction
public class ViewAccountPayments extends AbstractCrudAction {

    private final AccountService service;

    public ViewAccountPayments(AccountService service) {
        this.service = service;
        setName("View Payments");
        setApplicableClass(Account.class);
        setImage("table");
        setColor("white");
        setBackground("#00b19d");
        setMenuSupported(true);
        setGroup(ActionGroup.get("PAY"));
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        Account account = (Account) evt.getData();
        if (account != null) {
            List<AccountPayment> payments = service.findAllPayments(account);

            Viewer viewer = new Viewer("table", AccountPayment.class, payments);
            ZKUtil.showDialog(account.toString(), viewer,"80%","80%");
        } else {
            UIMessages.showMessage("Select account", MessageType.WARNING);
        }
    }
}
