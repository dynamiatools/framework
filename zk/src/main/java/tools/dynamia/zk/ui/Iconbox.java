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
package tools.dynamia.zk.ui;

import org.zkoss.lang.Objects;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Combobox;
import tools.dynamia.ui.icons.Icon;
import tools.dynamia.ui.icons.IconsComparator;
import tools.dynamia.ui.icons.IconsTheme;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.util.ZKUtil;

import java.util.List;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class Iconbox extends Combobox {

	/**
	 *
	 */
	private static final long serialVersionUID = 3281618334473692671L;

	static {
		BindingComponentIndex.getInstance().put("selected", Iconbox.class);
		ComponentAliasIndex.getInstance().add(Iconbox.class);
	}

	public Iconbox() {
		init();
	}

	public Iconbox(String value) throws WrongValueException {
		super(value);
		init();
	}

	private void init() {
		setItemRenderer(new IconboxItemRenderer());
		setReadonly(true);

		List<Icon> icons = IconsTheme.get().getAll();
		icons.sort(new IconsComparator());
		ZKUtil.fillCombobox(this, icons);
	}

	public String getSelected() {
		return getValue();
	}

	public void setSelected(String selected) {
		if (!Objects.equals(getSelected(), selected)) {
			setValue(selected);
			Events.postEvent(Events.ON_SELECT, this, getSelected());
		}
	}
}
