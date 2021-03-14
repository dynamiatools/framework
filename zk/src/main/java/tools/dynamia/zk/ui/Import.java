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
package tools.dynamia.zk.ui;

import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zk.ui.ext.DynamicPropertied;
import org.zkoss.zul.Div;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.util.ZKUtil;

import java.util.HashMap;
import java.util.Map;

public class Import extends Div implements DynamicPropertied, AfterCompose, IdSpace {

    private static final String VALUE = "value";

    static {
        BindingComponentIndex.getInstance().put("src", Import.class);
        ComponentAliasIndex.getInstance().add(Import.class);
    }

    /**
     *
     */
    private static final long serialVersionUID = -8236823411921386808L;
    private String src;
    private Map<String, Object> args = new HashMap<>();


    private boolean valueRequired = false;


    @Override
    public void afterCompose() {
        reload();
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        if (src != null && !src.equals(this.src)) {
            this.src = src;
            reload();
            Events.sendEvent(new Event(Events.ON_CHANGE, this, src));
        } else if (src == null) {
            this.src = null;
            clear();
        }
    }

    private void clear() {
        getChildren().clear();

    }

    public void reload() {
        if (valueRequired && getValue() == null) {
            return;
        }

        if (src != null) {
            clear();
            ZKUtil.createComponent(src, this, args);
        }
    }

    public void addArg(String name, Object value) {
        args.put(name, value);
    }

    public void addArgs(Map<String, Object> args) {
        this.args.putAll(args);
    }

    public Object getArg(String name) {
        return args.get(name);
    }

    public void setValue(Object value) {
        addArg(VALUE, value);
        reload();
    }

    public Object getValue() {
        return getArg(VALUE);
    }


    @Override
    public boolean hasDynamicProperty(String name) {
        return args.containsKey(name);
    }

    @Override
    public Object getDynamicProperty(String name) {
        return getArg(name);
    }

    @Override
    public void setDynamicProperty(String name, Object value) throws WrongValueException {
        addArg(name, value);
    }

    public boolean isValueRequired() {
        return valueRequired;
    }

    public void setValueRequired(boolean valueRequired) {
        this.valueRequired = valueRequired;
    }
}
