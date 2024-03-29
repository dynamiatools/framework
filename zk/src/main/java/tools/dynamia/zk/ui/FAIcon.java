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

import org.zkoss.zul.Menuitem;
import tools.dynamia.ui.icons.Icon;
import tools.dynamia.ui.icons.IconSize;
import tools.dynamia.ui.icons.IconType;

public class FAIcon extends Icon {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public FAIcon(String name, String internalName) {
        super(name, internalName, IconType.FONT);
    }

    @Override
    public String getRealPath(Object component, IconSize size) {
        // TODO Auto-generated method stub
        String path = super.getRealPath(component, size);
        String fontSize = "";
        String alt = "";

        switch (size) {
            case LARGE -> fontSize = " fa-2x";
            case NORMAL -> fontSize = " fa-lg";
        }

        if (component instanceof Menuitem) {
            alt = " fa-fw";
        }

        return path + fontSize + alt;
    }

}
