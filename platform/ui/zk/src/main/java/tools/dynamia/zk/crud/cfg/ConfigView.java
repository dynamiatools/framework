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
package tools.dynamia.zk.crud.cfg;

import tools.dynamia.domain.query.Parameter;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.util.ZKBindingUtil;
import tools.dynamia.zk.viewers.form.FormView;

import java.util.List;

/**
 * Specialized {@link FormView} for editing application {@link Parameter} values.
 * <p>
 * It keeps bindings explicit per parameter name and uses non-autosave mode by default.
 */
public class ConfigView extends FormView<List<Parameter>> {

    /**
     * Serialization identifier.
     */
    private static final long serialVersionUID = 4854627795725385607L;

    static {
        BindingComponentIndex.getInstance().put("value", ConfigView.class);
        ComponentAliasIndex.getInstance().add(ConfigView.class);
    }


    /**
     * Creates a config form with autoheight enabled and manual binding save mode.
     */
    public ConfigView() {
        setAutoheight(true);
        setAutosaveBindings(false);
    }

    /**
     * Loads each parameter into binder variables and refreshes components.
     */
    @Override
    public void updateUI() {
        if (getBinder() != null) {
            if (value != null) {
                for (Parameter parameter : value) {
                    ZKBindingUtil.bindBean(this, parameter.getName(), parameter);
                }
            }

            getBinder().loadComponent(this, false);
        }
    }

    /**
     * Sets the full parameter list without triggering extra form events.
     *
     * @param value parameters to edit
     */
    @Override
    public void setValue(List<Parameter> value) {
        this.value = value;
    }

    /**
     * Saves current bindings using base form behavior.
     */
    @Override
    public void saveBindings() {
        super.saveBindings();

    }
}
