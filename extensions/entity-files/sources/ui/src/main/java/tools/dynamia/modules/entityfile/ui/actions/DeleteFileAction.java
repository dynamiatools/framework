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
import tools.dynamia.modules.entityfile.domain.EntityFile;
import tools.dynamia.modules.entityfile.enums.EntityFileType;
import tools.dynamia.modules.entityfile.service.EntityFileService;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;

@InstallAction
public class DeleteFileAction extends AbstractEntityFileAction {

    @Autowired
    private EntityFileService service;

    public DeleteFileAction() {
        setName("Borrar");
        setImage("icons:delete");
        setGroup(ActionGroup.get("FILES"));
        setMenuSupported(true);
        setPosition(5);
    }

    @Override
    public void actionPerformed(final EntityFileActionEvent evt) {
        try {
            final EntityFile entityFile = evt.getEntityFile();
            if (entityFile != null) {
                UIMessages.showQuestion("Esta seguro que desea borrar el archivo/directorio " + entityFile.getName() + "?", () -> {
				    service.delete(entityFile);
				    if (entityFile.getType() == EntityFileType.DIRECTORY) {
				        UIMessages.showMessage("Carpeta borrada correctamente");
				    } else {
				        UIMessages.showMessage("Archivo borrado correctamente");
				    }
				    evt.getController().doQuery();
				});

            } else {
                UIMessages.showMessage("Seleccion archivo para borrar", MessageType.WARNING);
            }
        } catch (Exception e) {
            e.printStackTrace();
            UIMessages.showMessage("Error al borrar archivo", MessageType.ERROR);
        }
    }

}
