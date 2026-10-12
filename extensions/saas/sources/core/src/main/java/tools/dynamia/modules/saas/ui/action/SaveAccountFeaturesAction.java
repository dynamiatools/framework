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

import tools.dynamia.actions.InstallAction;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.domain.AbstractEntity;
import tools.dynamia.domain.query.ListDataSet;
import tools.dynamia.modules.saas.domain.AccountFeature;
import tools.dynamia.ui.UIMessages;

@InstallAction
public class SaveAccountFeaturesAction extends AbstractCrudAction {
    public SaveAccountFeaturesAction() {
        setName("Save");
        setApplicableClass(AccountFeature.class);
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        ListDataSet<AccountFeature> list = (ListDataSet<AccountFeature>) evt.getController().getQueryResult();
        crudService().executeWithinTransaction(() -> {
            list.getData().forEach(AbstractEntity::save);
        });
        UIMessages.showMessage("OK");
    }
}
