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

import org.zkoss.zul.A;

import tools.dynamia.ui.icons.IconSize;
import tools.dynamia.ui.icons.IconsTheme;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.util.ZKUtil;

import java.io.Serial;

public class FilesCountImage extends A {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;
	private IconSize iconSize = IconSize.SMALL;
	private String icon = "attachment";
	private int value;

	static {
		BindingComponentIndex.getInstance().put("value", FilesCountImage.class);
		ComponentAliasIndex.getInstance().add("filescountimage", FilesCountImage.class);
	}

	public FilesCountImage() {

	}

	public FilesCountImage(int value) {
		this.value = value;
	}

	public void setValue(int value) {
		try {
            if (value > 0) {
				ZKUtil.configureComponentIcon(icon, this, iconSize);
				setTooltiptext(value + " archivos adjuntos");
			} else {
				setImage(null);
				getChildren().clear();
				setTooltiptext(null);
			}
			setLabel("");
		} catch (Exception e) {
			setLabel("");
			setImage(null);
		}
	}

	public int getValue() {
		return value;
	}

	public IconSize getIconSize() {
		return iconSize;
	}

	public void setIconSize(IconSize iconSize) {
		this.iconSize = iconSize;
	}

	public void setIconSize(String iconSize) {
		this.iconSize = IconSize.valueOf(iconSize.toUpperCase());
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

}
