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
import tools.dynamia.actions.FastAction;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.actions.PrimaryAction;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.modules.saas.domain.Account;
import tools.dynamia.modules.saas.domain.AccountPayment;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.web.util.HttpUtils;
import tools.dynamia.zk.util.ZKUtil;
import tools.dynamia.zk.viewers.ui.Viewer;

@InstallAction
@PrimaryAction
public class NewAccountPaymentAction extends AbstractCrudAction {


    public NewAccountPaymentAction() {
        setName(msg("newPayment"));
        setApplicableClass(Account.class);
        setImage("payment");
        setMenuSupported(true);
        setShowLabel(true);
        setGroup(ActionGroup.get("CRUD"));
        setType("success");
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        Account account = (Account) evt.getData();
        if (account != null) {

            AccountPayment payment = new AccountPayment();
            payment.setAccount(account);
            Viewer viewer = new Viewer("form", AccountPayment.class, payment);
            if (HttpUtils.isSmartphone()) {
                viewer.setVflex("1");
                viewer.setContentVflex("0");
            }
            viewer.addAction(new FastAction(msg("createPayment"), e -> UIMessages.showQuestion(msg("confirmNewPayment"), () -> {
                payment.computeComission();
                crudService().save(payment);
                UIMessages.showMessage(msg("paymentCreated"));
                viewer.getParent().detach();
                evt.getController().doQuery();
            })));
            ZKUtil.showDialog(account.toString(), viewer);
        } else {
            UIMessages.showMessage(msg("selectAccount"), MessageType.WARNING);
        }
    }
}
