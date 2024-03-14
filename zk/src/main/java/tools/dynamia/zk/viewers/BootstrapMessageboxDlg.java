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
package tools.dynamia.zk.viewers;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.impl.MessageboxDlg;

public class BootstrapMessageboxDlg extends MessageboxDlg {

    /**
     *
     */
    private static final long serialVersionUID = 1109846367442761226L;

    public BootstrapMessageboxDlg() {
    }

    @Override
    public void setButtons(Messagebox.Button[] buttons, String[] btnLabels) {
        super.setButtons(buttons, btnLabels);

        final Component parent = getFellowIfAny("buttons");
        if (parent != null) {
            parent.getChildren().clear();

            for (int j = 0; j < buttons.length; ++j) {
                final Button mbtn = new Button();
                mbtn.setButton(buttons[j],
                        btnLabels != null && j < btnLabels.length ? btnLabels[j] : null);

                String btnType = getType(buttons[j]);

                mbtn.setZclass("btn " + btnType);
                mbtn.setAutodisable("self");
                parent.appendChild(mbtn);
            }
        }
    }

    private String getType(org.zkoss.zul.Messagebox.Button button) {
        String type;

        switch (button) {
            case OK, YES -> type = "btn-primary";
            case CANCEL, NO -> type = "btn-danger";
            default -> type = "btn-default";
        }

        return type;
    }

}
