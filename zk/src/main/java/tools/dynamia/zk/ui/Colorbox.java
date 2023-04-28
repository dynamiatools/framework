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

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import tools.dynamia.io.ImageUtil;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.util.ZKUtil;

/**
 * @author Ing. Mario Serrano
 */
public class Colorbox extends Combobox {

    static {
        BindingComponentIndex.getInstance().put("value", Colorbox.class);
        ComponentAliasIndex.getInstance().add(Colorbox.class);
    }

    private static final String BACKGROUND_COLOR = "background-color: ";

    private String[] availableColors = {
            "white", "black", "red", "blue", "green", "yellow",
            "#B71C1C", "#F44336", "#E57373", "#FFCDD2", //red
            "#880E4F", "#E91E63", "#F06292", "#F8BBD0", //pink
            "#4A148C", "#9C27B0", "#BA68C8", "#E1BEE7", //purple
            "#311B92", "#673AB7", "#9575CD", "#D1C4E9", //deep purple
            "#1A237E", "#3F51B5", "#7986CB", "#C5CAE9", //indigo
            "#0D47A1", "#2196F3", "#64B5F6", "#BBDEFB", //blue
            "#1B5E20", "#4CAF50", "#81C784", "#C8E6C9", //green
            "#33691E", "#8BC34A", "#AED581", "#DCEDC8", //light green
            "#F57F17", "#FFEB3B", "#FFF176", "#FFF9C4", //yellow
            "#E65100", "#FF9800", "#FFB74D", "#FFE0B2", //orange
            "#BF360C", "#FF5722", "#FF8A65", "#FFCCBC", //deep orange
            "#263238", "#607D8B", "#90A4AE", "#CFD8DC", //blue gray
            "#212121", "#9E9E9E", "#E0E0E0", "#F5F5F5", //gray
    };

    public Colorbox() {
        init();
    }

    public Colorbox(String color) {
        setValue(color);
        init();
    }

    @Override
    public void setValue(String color) {
        super.setValue(color);
        updateBackgroundColor();
    }

    private void updateBackgroundColor() {
        if (getValue() != null) {
            setStyle(BACKGROUND_COLOR + getValue());
            if (ImageUtil.isDark(getValue())) {
                setStyle(getStyle() + "; color: white");
            }
        } else {
            setStyle(null);
        }
    }

    private void init() {
        setItemRenderer((Comboitem item, String data, int index) -> {
            item.setValue(data);
            item.setLabel(data);
            item.setStyle(BACKGROUND_COLOR + data + "; text-shadow: none; text-align: right");
            item.setHeight("40px");
            if (ImageUtil.getBrightness(data) < 130) {
                item.setStyle(item.getStyle() + "; color: white");
            }
        });

        addEventListener(Events.ON_SELECT, e -> updateBackgroundColor());
        addEventListener(Events.ON_OK, e -> updateBackgroundColor());
        initModel();
    }

    private void initModel() {
        ZKUtil.fillCombobox(this, availableColors, true);
    }

    public String[] getAvailableColors() {
        return availableColors;
    }

    public void setAvailableColors(String[] availableColors) {
        this.availableColors = availableColors;
        initModel();
    }

    /**
     * Set a string of comma separated colors
     *
     * @param colors
     */
    public void setAvailableColors(String colors) {
        if (colors != null) {
            setAvailableColors(colors.trim().replace(" ", "").split(","));
        }
    }

}
