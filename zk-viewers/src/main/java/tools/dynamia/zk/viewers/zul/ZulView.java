
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

package tools.dynamia.zk.viewers.zul;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zul.Div;
import org.zkoss.zul.Window;
import tools.dynamia.viewers.View;
import tools.dynamia.viewers.ViewDescriptor;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class ZulView extends Div implements View<String>, IdSpace {

    private ViewDescriptor viewDescriptor;
    private View parentView;
    private String value;
    private final Map<String, Object> arguments = new HashMap<>();

    public ZulView() {
    }

    public ZulView(String value) {
        this.value = value;
    }

    public void addArgument(String name, Object value) {
        arguments.put(name, value);
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }

    @Override
    public void setParent(Component parent) {
        renderZul();
        super.setParent(parent); //To change body of generated methods, choose Tools | Templates.
    }

    private void renderZul() {
        Executions.createComponents(value, this, getArguments());
    }

    @Override
    public ViewDescriptor getViewDescriptor() {
        return viewDescriptor;
    }

    @Override
    public void setViewDescriptor(ViewDescriptor viewDescriptor) {
        this.viewDescriptor = viewDescriptor;
    }

    @Override
    public View getParentView() {
        return parentView;
    }

    @Override
    public void setParentView(View parentView) {
        this.parentView = parentView;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

}
