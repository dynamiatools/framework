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

import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import tools.dynamia.zk.BindingComponentIndex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("rawtypes")
public class EnumListbox<T extends Enum> extends Listbox {

    /**
     *
     */
    private static final long serialVersionUID = 3499252837420704166L;
    private Class<? extends Enum> enumClass;
    private List<? extends Enum> enumValues;

    static {
        BindingComponentIndex.getInstance().put("selected", EnumListbox.class);
    }

    public EnumListbox() {

    }

    public EnumListbox(Class<? extends Enum> enumClass) {
        setEnum(enumClass);
    }

    public EnumListbox(Class<? extends Enum> enumClass, List<T> enumValues) {
        this.enumClass = enumClass;
        this.enumValues = enumValues;
        buildSelector();
    }

    public final void setEnum(Class<? extends Enum> enumClass) {
        this.enumClass = enumClass;
        buildSelector();
    }

    public final void setEnum(String enumClassName) {
        try {
            //noinspection unchecked
            setEnum((Class<? extends Enum>) Class.forName(enumClassName));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public List<T> getSelected() {
        List<T> selected = new ArrayList<>();
        for (Listitem listitem : getSelectedItems()) {
            selected.add(listitem.getValue());
        }

        return selected;
    }

    public void setSelected(List<T> selected) {
        if (selected != null && getItems() != null) {
            for (Listitem listitem : getItems()) {
                for (T enumValue : selected) {
                    if (listitem.getValue() == enumValue) {
                        listitem.setSelected(true);
                    }
                }
            }
        }
    }

    private void buildSelector() {
        getChildren().clear();
        if (enumValues == null || enumValues.isEmpty()) {
            enumValues = Arrays.asList(enumClass.getEnumConstants());
        }

        for (Enum enumValue : enumValues) {
            appendChild(new Listitem(enumValue.toString(), enumValue));
        }
        setCheckmark(true);
        setMultiple(true);
    }

}
