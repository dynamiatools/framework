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

import org.zkoss.zul.Button;
import tools.dynamia.actions.Action;
import tools.dynamia.actions.ActionEventBuilder;

public class BoostrapButtonActionRenderer extends ButtonActionRenderer {

    @Override
    public Button render(Action action, ActionEventBuilder actionEventBuilder) {
        Button button = super.render(action, actionEventBuilder);

        String type = "default";
        if (action.getAttribute("type") != null) type = (String) action.getAttribute("type");

        button.setZclass("btn btn-" + type);

        String zclass = (String) action.getAttribute("zclass");
        if (zclass != null) {
            button.setZclass(zclass);
        }


        return button;
    }
}
