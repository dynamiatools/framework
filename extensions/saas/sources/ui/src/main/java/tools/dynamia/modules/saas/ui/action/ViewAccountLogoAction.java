
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

import org.zkoss.zul.Div;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.commons.ApplicableClass;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.crud.CrudState;
import tools.dynamia.modules.entityfile.ui.components.EntityFileImage;
import tools.dynamia.modules.saas.domain.Account;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.util.ZKUtil;

/**
 *
 * @author Mario Serrano Leones
 */
@InstallAction
public class ViewAccountLogoAction extends AbstractCrudAction {

    public ViewAccountLogoAction() {
        setName("View Logo");
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        Account account = (Account) evt.getData();
        if (account != null) {
            showLogo(account);
        } else {
            UIMessages.showMessage("Seleccione cuenta para ver logo", MessageType.WARNING);
        }
    }

    @Override
    public ApplicableClass[] getApplicableClasses() {
        return ApplicableClass.get(Account.class);
    }

    @Override
    public CrudState[] getApplicableStates() {
        return CrudState.get(CrudState.READ);
    }

    private void showLogo(Account account) {
        if (account.getLogo() == null) {
            UIMessages.showMessage("La cuenta seleccionada no tiene logo", MessageType.WARNING);
        } else {
            System.out.println(account.getLogo().getStoredEntityFile().getThumbnailUrl());
            EntityFileImage image = new EntityFileImage();
            image.setValue(account.getLogo());

            Div div = new Div();
            div.setHflex("1");
            div.setVflex("1");
            div.setStyle("overflow:auto;background:silver");

            div.appendChild(image);

            ZKUtil.showDialog("Logo " + account.getName(), div, "600px", "500px");

        }
    }

}
