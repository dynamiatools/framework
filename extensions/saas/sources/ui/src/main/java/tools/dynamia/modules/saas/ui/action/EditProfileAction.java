
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
import tools.dynamia.commons.ApplicableClass;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.crud.CrudState;
import tools.dynamia.crud.actions.EditAction;
import tools.dynamia.modules.saas.domain.AccountProfile;
import tools.dynamia.modules.saas.ui.controllers.AccountProfileController;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;

/**
 *
 * @author Mario Serrano Leones
 */
//@InstallAction
public class EditProfileAction extends EditAction {

	@Override
	public void actionPerformed(CrudActionEvent evt) {
		if (evt.getData() != null) {
			AccountProfile perfil = (AccountProfile) evt.getData();
			AccountProfileController controller = (AccountProfileController) evt.getController();
			controller.edit(perfil);
		} else {
			UIMessages.showMessage("Seleccione perfil a editar", MessageType.ERROR);
		}

	}

	@Override
	public CrudState[] getApplicableStates() {
		return CrudState.get(CrudState.READ);
	}

	@Override
	public ApplicableClass[] getApplicableClasses() {
		return ApplicableClass.get(AccountProfile.class);
	}
}
