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

package tools.dynamia.zk.viewers.form;

import org.zkoss.zk.ui.Component;
import tools.dynamia.viewers.IFormFieldGroupComponent;

import java.util.ArrayList;
import java.util.List;

public class FormFieldGroupComponent implements IFormFieldGroupComponent<Component> {

    private final String groupName;
    private final Component groupComponent;
    private List<FormFieldComponent> fieldsComponents = new ArrayList<>();

    public FormFieldGroupComponent(String groupName, Component groupComponent) {
        this.groupName = groupName;
        this.groupComponent = groupComponent;
    }

    public FormFieldGroupComponent(String groupName, Component groupComponent, List<FormFieldComponent> fieldsComponents) {
        this.groupName = groupName;
        this.groupComponent = groupComponent;
        this.fieldsComponents = fieldsComponents;
    }

    public String getGroupName() {
        return groupName;
    }

    public Component getGroupComponent() {
        return groupComponent;
    }


    public List<FormFieldComponent> getFieldsComponents() {
        return fieldsComponents;
    }

    @Override
    public void hide() {


        if (groupComponent != null) {
            groupComponent.setVisible(false);
        }

    }

    @Override
    public void show() {


        if (groupComponent != null) {
            groupComponent.setVisible(true);
        }

    }

    @Override
    public void remove() {
        if (groupComponent != null) {
            groupComponent.detach();
        }
    }

}
