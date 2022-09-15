/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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

import org.zkoss.zhtml.I;
import org.zkoss.zul.Image;
import org.zkoss.zul.Span;
import tools.dynamia.ui.icons.Icon;
import tools.dynamia.ui.icons.IconSize;
import tools.dynamia.ui.icons.IconType;
import tools.dynamia.ui.icons.IconsTheme;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.util.ZKUtil;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class IconImageNotNull extends Span  implements LoadableOnly {

	/**
	 *
	 */
	private static final long serialVersionUID = 7867924187238624105L;

	static {
		ComponentAliasIndex.getInstance().add(IconImageNotNull.class);
		BindingComponentIndex.getInstance().put("value", IconImageNotNull.class);
	}
	private IconSize size = IconSize.SMALL;
	private String iconName;
	private Object value;
	private String color;
	private String label;

	public String getIconName() {
		return iconName;
	}

	public void setIconName(String iconName) {
		this.iconName = iconName;
	}

	private void setupIcon() {
		getChildren().clear();
		if (value != null) {
			if (label != null) {
				setTooltiptext(label + ": " + value.toString());
			} else {
				setTooltiptext(value.toString());
			}

			Icon icon = IconsTheme.get().getIcon(iconName);
			if (icon.getType() == IconType.IMAGE) {
				Image image = new Image();
				image.setParent(this);
				ZKUtil.configureComponentIcon(icon, image, size);
			} else {
				I i = new I();
				i.setParent(this);
				if (color != null) {
					i.setStyle("color: " + color);
				}
				ZKUtil.configureComponentIcon(icon, i, size);
			}
		}
	}

	public IconSize getSize() {
		return size;
	}

	public void setSize(IconSize size) {
		this.size = size;
	}

	public void setSize(String size) {
		setSize(IconSize.valueOf(size.toUpperCase()));
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
		setupIcon();
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
