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

package tools.dynamia.modules.entityfile.ui.components;

import java.io.Serial;
import java.util.Optional;

import org.zkoss.zul.Combobox;
import org.zkoss.zul.ListModelList;

import tools.dynamia.integration.Containers;
import tools.dynamia.modules.entityfile.EntityFileStorage;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.util.ZKUtil;

public class StorageCombobox extends Combobox {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 3507817129731334840L;

	static {
		ComponentAliasIndex.getInstance().put("storagebox", StorageCombobox.class);
		BindingComponentIndex.getInstance().put("selected", StorageCombobox.class);
	}

	private String selected;

	public StorageCombobox() {
		setReadonly(true);

		setItemRenderer((item, data, index) -> {
			EntityFileStorage storage = (EntityFileStorage) data;
			item.setValue(storage.getId());
			item.setLabel(storage.getName());
		});

		ZKUtil.fillCombobox(this, Containers.get().findObjects(EntityFileStorage.class), true);
	}

	public String getSelected() {
		selected = null;
		if (getSelectedItem() != null) {
			selected = getSelectedItem().getValue();
		}
		return selected;
	}

	public void setSelected(String selected) {
		if (selected != this.selected) {
			this.selected = selected;
			try {
				Optional<EntityFileStorage> selectedStorage = Containers.get().findObjects(EntityFileStorage.class)
						.stream().filter(efs -> efs.getId().equals(selected)).findFirst();
				if (selectedStorage.isPresent()) {
					ListModelList model = (ListModelList) getModel();
					model.addToSelection(selectedStorage.get());
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

}
