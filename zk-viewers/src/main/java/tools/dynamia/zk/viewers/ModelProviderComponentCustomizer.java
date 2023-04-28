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

import org.zkoss.zul.Combobox;
import org.zkoss.zul.GroupsModel;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listbox;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.domain.util.ModelProvider;
import tools.dynamia.integration.sterotypes.Provider;
import tools.dynamia.viewers.ComponentCustomizer;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.ViewRendererException;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.zk.util.ZKUtil;

import java.util.Collection;

@Provider
public class ModelProviderComponentCustomizer implements ComponentCustomizer {

    @Override
    public void cutomize(Field field, Object component) {

        try {
            String modelProviderClass = (String) field.getParams().get(Viewers.PARAMS_MODEL_PROVIDER);
            if (modelProviderClass != null) {
                ModelProvider provider = BeanUtils.newInstance(modelProviderClass);
                Object model = provider.getModel();
                if (model != null) {

                    if (component instanceof Combobox) {
                        fillCombobox((Combobox) component, model);
                    } else if (component instanceof Listbox) {
                        fillListbox((Listbox) component, model);
                    } else {
                        if (component.getClass().getMethod("setModel", model.getClass()) != null) {
                            BeanUtils.invokeSetMethod(component, "model", model);
                        }
                    }

                }
            }
        } catch (Exception e) {
            String message = "Error setting model provider to component " + component + ". Field: " + field + " from view descriptor " + field.getViewDescriptor();

            throw new ViewRendererException(message, e);
        }
    }

    private void fillListbox(Listbox component, Object model) {
        if (model instanceof Collection) {
            ZKUtil.fillListbox(component, (Collection) model, true);
        } else if (model.getClass().isArray()) {
            ZKUtil.fillListbox(component, (Object[]) model, true);
        } else if (model instanceof ListModel) {
            component.setModel((ListModel<?>) model);
        } else if (model instanceof GroupsModel) {
            component.setModel((GroupsModel<?, ?, ?>) model);
        }
    }

    private void fillCombobox(Combobox component, Object model) {
        if (model instanceof Collection) {
            ZKUtil.fillCombobox(component, (Collection) model, true);
        } else if (model.getClass().isArray()) {
            ZKUtil.fillCombobox(component, (Object[]) model, true);
        } else if (model instanceof ListModel) {
            component.setModel((ListModel<?>) model);
        }
    }
}
