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

import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import tools.dynamia.commons.Messages;

import java.time.Month;
import java.time.format.TextStyle;

public class MonthItemRenderer implements ComboitemRenderer<Month> {

    private boolean useNumbers;

    public MonthItemRenderer() {
    }

    public MonthItemRenderer(boolean useNumbers) {
        this.useNumbers = useNumbers;
    }

    @Override
    public void render(Comboitem item, Month data, int index) {
        item.setLabel(data.getDisplayName(TextStyle.FULL, Messages.getDefaultLocale()).toUpperCase());
        if(!useNumbers) {
            item.setValue(data);
        }else{
            item.setValue(data.getValue());
        }
    }

    public boolean isUseNumbers() {
        return useNumbers;
    }

    public void setUseNumbers(boolean useNumbers) {
        this.useNumbers = useNumbers;
    }
}
