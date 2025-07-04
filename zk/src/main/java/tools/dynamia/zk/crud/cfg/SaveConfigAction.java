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
package tools.dynamia.zk.crud.cfg;

import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.ActionRenderer;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.commons.Messages;
import tools.dynamia.crud.cfg.AbstractConfigPageAction;
import tools.dynamia.domain.query.ApplicationParameters;
import tools.dynamia.domain.query.Parameter;
import tools.dynamia.domain.query.Parameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.navigation.NavigationManager;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.actions.ToolbarbuttonActionRenderer;

import java.util.List;

/**
 * @author Mario A. Serrano Leones
 */
@InstallAction
public class SaveConfigAction extends AbstractConfigPageAction {

    public SaveConfigAction() {
        setName(Messages.get(SaveConfigAction.class, "save"));
        setImage("save");
        setType("primary");
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        @SuppressWarnings("unchecked") List<Parameter> parameters = (List<Parameter>) evt.getData();
        if (parameters != null) {
            try {
                ApplicationParameters.get().save(parameters);
                if (evt.getSource() instanceof ConfigView configView) {
                    configView.reloadValue();
                } else {
                    NavigationManager.getCurrent().refresh();
                }
                UIMessages.showMessage(Messages.get(getClass(), "succesfull_save"));
            } catch (Exception e) {
                log("Error saving data", e);
                UIMessages.showException(Messages.get(getClass(), "error_save", e.getMessage()), e);
            }
        } else {
            UIMessages.showMessage(Messages.get(getClass(), "error_save", "Parameters are null"), MessageType.ERROR);
        }
    }

}
