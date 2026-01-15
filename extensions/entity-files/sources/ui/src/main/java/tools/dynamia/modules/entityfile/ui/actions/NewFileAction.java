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
import org.zkoss.util.media.Media;
import org.zkoss.zul.Fileupload;

import tools.dynamia.actions.ActionGroup;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.modules.entityfile.UploadedFileInfo;
import tools.dynamia.modules.entityfile.service.EntityFileService;
import tools.dynamia.modules.entityfile.ui.util.EntityFileUtils;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;

@InstallAction
public class NewFileAction extends AbstractEntityFileAction {

	@Autowired
	private EntityFileService service;

	public NewFileAction() {
		setName("Nuevo Archivo");
		setImage("add");
		setGroup(ActionGroup.get("FILES"));
		setBackground(".green");
		setColor("white");
		setPosition(0);
	}

	@Override
	public void actionPerformed(final EntityFileActionEvent evt) {

		Fileupload.get(10, event -> {

			Media[] medias = event.getMedias();
			if (medias != null) {
				for (Media media : medias) {
					UploadedFileInfo info = EntityFileUtils.build(media);
					info.setParent(evt.getEntityFile());
					service.createEntityFile(info, evt.getTargetEntity());
				}
				evt.getCrudView().getController().doQuery();
				UIMessages.showMessage("Archivo(s) Cargado(s) Correctamente");
			} else {
				UIMessages.showMessage("Debe seleccionar al menos un archivo", MessageType.ERROR);
			}
		});

	}
}
