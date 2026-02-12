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

package tools.dynamia.modules.entityfile.ui.actions;

import org.springframework.beans.factory.annotation.Autowired;

import tools.dynamia.actions.ActionGroup;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.modules.entityfile.enums.EntityFileType;
import tools.dynamia.modules.entityfile.service.EntityFileService;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.ui.InputPanel;

@InstallAction
public class NewDirectoryAction extends AbstractEntityFileAction {

    @Autowired
    private EntityFileService service;

    public NewDirectoryAction() {
        setName("Nuevo Directorio");
        setImage("folder2");
        setGroup(ActionGroup.get("FILES"));
        setMenuSupported(true);
        setPosition(3);
    }

    @Override
    public void actionPerformed(final EntityFileActionEvent evt) {
        InputPanel inputPanel = new InputPanel("Nombre de Directorio", "", String.class);
        inputPanel.showDialog();
        inputPanel.addEventListener(InputPanel.ON_INPUT, event -> {
            String dirName = (String) event.getData();
            if (dirName != null && !dirName.isEmpty()) {
                if (evt.getEntityFile() != null && evt.getEntityFile().getType() == EntityFileType.DIRECTORY) {
                    service.createDirectory(evt.getEntityFile(), dirName, "");
                } else {
                    service.createDirectory(evt.getTargetEntity(), dirName, "");
                }
                evt.getCrudView().getController().doQuery();
            } else {
                UIMessages.showMessage("Ingrese nombre del nuevo directorio", MessageType.ERROR);
            }
        });

    }

}
