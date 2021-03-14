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
package tools.dynamia.zk.actions;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zul.impl.InputElement;
import tools.dynamia.actions.Action;
import tools.dynamia.actions.ActionRenderer;

public abstract class ZKActionRenderer<T extends Component> implements ActionRenderer<T> {

    private String style;
    private String styleClass;
    private String width;
    private String height;
    private String hflex;
    private String vflex;
    private String zclass;
    private String placeholder;

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getZclass() {
        return zclass;
    }

    public void setZclass(String zclass) {
        this.zclass = zclass;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getHflex() {
        return hflex;
    }

    public void setHflex(String hflex) {
        this.hflex = hflex;
    }

    public String getVflex() {
        return vflex;
    }

    public void setVflex(String vflex) {
        this.vflex = vflex;
    }

    protected void configureProperties(T component, Action action) {
        if (component instanceof HtmlBasedComponent) {
            HtmlBasedComponent hc = (HtmlBasedComponent) component;
            hc.setSclass(styleClass);
            hc.setStyle(style);
            hc.setWidth(width);
            hc.setHeight(height);
            hc.setVflex(vflex);
            hc.setHflex(hflex);

            if (zclass != null && !zclass.isEmpty()) {
                hc.setZclass(zclass);
            }
            String background = null;
            String color = null;
            if (action != null) {
                background = (String) action.getAttribute("background");
                color = (String) action.getAttribute("color");
            }
            StringBuilder styleBuilder = new StringBuilder();
            StringBuilder styleClassBuilder = new StringBuilder();

            if (background != null) {
                if (background.startsWith(".")) {
                    styleClassBuilder.append(background.replaceFirst(".", "")).append(" ");
                } else {
                    styleBuilder.append("background-color: ").append(background).append(";");
                }
            }

            if (color != null) {
                if (color.startsWith(".")) {
                    styleClassBuilder.append(color.replaceFirst(".", ""));
                } else {
                    styleBuilder.append("color: ").append(color).append(" !important;");
                }
            }

            if (styleClassBuilder.length() > 0) {
                hc.setSclass(getStyleClass() + " " + styleClassBuilder.toString());
            }

            if (styleBuilder.length() > 0) {
                hc.setStyle(styleBuilder.toString());
            }
        }

        if (component instanceof InputElement) {
            ((InputElement) component).setPlaceholder(placeholder);
        }
    }

}
