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

package tools.dynamia.zk.actions;

import org.zkoss.zul.Button;
import tools.dynamia.actions.Action;
import tools.dynamia.actions.ActionEventBuilder;

public class BootstrapButtonActionRenderer extends ButtonActionRenderer {

    private boolean small;
    private boolean large;
    private boolean block;

    @Override
    public Button render(Action action, ActionEventBuilder actionEventBuilder) {
        Button button = super.render(action, actionEventBuilder);

        String type = "default";
        if (action.getAttribute("type") != null) type = (String) action.getAttribute("type");

        button.setZclass("btn btn-" + type);

        if (isSmall()) {
            button.addSclass("btn-sm");
        } else if (isLarge()) {
            button.addSclass("btn-lg");
        }

        if (isBlock()) {
            button.addSclass("btn-block");
        }

        String zclass = (String) action.getAttribute("zclass");
        if (zclass != null) {
            button.setZclass(zclass);
        }


        return button;
    }

    public boolean isSmall() {
        return small;
    }

    /**
     * Add style class btn-sm
     */
    public void setSmall(boolean small) {
        this.small = small;
    }


    public boolean isBlock() {
        return block;
    }

    /**
     * Add style class btn-block
     */
    public void setBlock(boolean block) {
        this.block = block;
    }

    public boolean isLarge() {
        return large;
    }

    /**
     * Add style class btn-lg
     */
    public void setLarge(boolean large) {
        this.large = large;
    }
}
