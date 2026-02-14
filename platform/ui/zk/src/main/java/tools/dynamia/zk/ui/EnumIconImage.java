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

import org.zkoss.zhtml.I;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Span;
import tools.dynamia.ui.icons.*;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.util.ZKUtil;

public class EnumIconImage extends Span implements LoadableOnly {

    /**
     *
     */
    private static final long serialVersionUID = -5975771607086380537L;

    static {
        ComponentAliasIndex.getInstance().add(EnumIconImage.class);
        BindingComponentIndex.getInstance().put("value", EnumIconImage.class);
    }

    private IconSize size = IconSize.NORMAL;
    private Enum value;
    private String[] iconsNames;

    public void setValue(Object value) {
        if (value instanceof Enum) {
            this.value = (Enum) value;
            render();
        }
    }

    private void render() {
        getChildren().clear();
        if (value != null) {
            setTooltiptext(null);
            String iconName = getIconName();
            if (iconName != null) {
                IconName iconNameObj = Icons.parseIconName(iconName);
                Icon icon = IconsTheme.get().getIcon(iconNameObj.name());
                setTooltiptext(value.name());
                if (icon.getType() == IconType.IMAGE) {
                    Image image = new Image();
                    image.setParent(this);
                    ZKUtil.configureComponentIcon(icon, image, size, iconNameObj.classes());
                } else {
                    I i = new I();
                    i.setParent(this);
                    ZKUtil.configureComponentIcon(icon, i, size, iconNameObj.classes());
                }
            } else {
                appendChild(new Label(value.name()));
            }
        }
    }

    private String getIconName() {
        try {
            if (value != null) {
                return iconsNames[value.ordinal()];
            }

        } catch (Exception ignored) {

        }
        return null;
    }

    public IconSize getSize() {
        return size;
    }

    public void setSize(IconSize size) {
        this.size = size;
    }

    public Enum getValue() {
        return value;
    }

    public void setSize(String size) {
        setSize(IconSize.valueOf(size.toUpperCase()));
    }

    public String[] getIconsNames() {
        return iconsNames;
    }

    public void setIconsNamesValues(String[] iconsNames) {
        this.iconsNames = iconsNames;
        render();
    }

    /**
     * Sets the icon names from a comma-separated string and triggers a re-render.
     * Spaces are automatically removed from the input string.
     *
     * @param iconsNames comma-separated string of icon names (e.g., "check,clock,times")
     *                   <p>
     *                   Example:
     *                   <pre>{@code
     *                                                       iconImage.setIconsNames("check-circle, clock, times-circle");
     *                                                       }</pre>
     */
    public void setIconsNames(String iconsNames) {
        if (iconsNames != null) {
            setIconsNamesValues(iconsNames.replace(" ", "").split(","));
        }
    }
}
