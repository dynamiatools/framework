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
public class IconImage extends Span  implements LoadableOnly{

    /**
     *
     */
    private static final long serialVersionUID = 7867924187238624105L;

    static {
        ComponentAliasIndex.getInstance().add(IconImage.class);
        BindingComponentIndex.getInstance().put("src", IconImage.class);
    }
    private IconSize size = IconSize.SMALL;
    private String trueIcon;
    private String falseIcon;
    private String src;

    public void setSrc(String src) {
        if ("false".equals(src)) {
            src = falseIcon;
        } else if ("true".equals(src)) {
            src = trueIcon;
        }

        this.src = src;
        getChildren().clear();
        Icon icon = IconsTheme.get().getIcon(src);
        if (icon.getType() == IconType.IMAGE) {
            Image image = new Image();
            image.setParent(this);
            ZKUtil.configureComponentIcon(icon, image, size);
        } else {
            I i = new I();
            i.setParent(this);
            ZKUtil.configureComponentIcon(icon, i, size);
        }

    }

    public void setSrc(boolean src) {
        if (src) {
            setSrc(trueIcon);
        } else {
            setSrc(falseIcon);
        }
    }

    public String getSrc() {
        return src;
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

    public String getTrueIcon() {
        return trueIcon;
    }

    public void setTrueIcon(String trueIcon) {
        this.trueIcon = trueIcon;
    }

    public String getFalseIcon() {
        return falseIcon;
    }

    public void setFalseIcon(String falseIcon) {
        this.falseIcon = falseIcon;
    }

}
