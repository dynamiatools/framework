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

package tools.dynamia.zk.viewers;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Label;
import org.zkoss.zul.impl.LabelElement;
import tools.dynamia.ui.icons.IconSize;
import tools.dynamia.viewers.Field;
import tools.dynamia.zk.util.ZKUtil;

/**
 * Simply util class for ZK based viwers
 */
public class ZKViewersUtil {

    public static void setupFieldIcon(Field field, Component component) {
        if (field.getIcon() != null && !field.getIcon().isBlank()) {
            ZKUtil.configureComponentIcon(field.getIcon(), component, IconSize.SMALL);

            if (field.isShowIconOnly()) {
                if (component instanceof Label) {
                    ((Label) component).setValue(null);
                } else if (component instanceof LabelElement) {
                    ((LabelElement) component).setLabel(null);
                }
            }
        }
    }
}
