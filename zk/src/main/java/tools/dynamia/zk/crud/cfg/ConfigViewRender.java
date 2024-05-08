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

import org.zkoss.bind.Binder;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.domain.query.Parameter;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.View;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.ViewRenderer;
import tools.dynamia.viewers.ViewRendererException;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.converters.Util;
import tools.dynamia.zk.util.ZKBindingUtil;
import tools.dynamia.zk.viewers.form.FormViewRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mario A. Serrano Leones
 */
public class ConfigViewRender implements ViewRenderer<List<Parameter>> {

    public static final String PARAM_PARAMETER_CLASS = "parameterClass";
    public static final String PARAM_PARAMETER_NAME = "parameterName";
    public static final String PARAM_CACHEABLE = "cacheable";
    public static final String PARAM_DEFAULT_VALUE = "defaultValue";

    public ConfigViewRender() {
    }

    protected ConfigView newConfigView() {
        ConfigView formView = new ConfigView();

        return formView;
    }

    @Override
    public View<List<Parameter>> render(ViewDescriptor descriptor, List<Parameter> value) {
        value = loadConfigValue(descriptor);
        ConfigView view = newConfigView();

        view.setValueSupplier(() -> loadConfigValue(descriptor));
        delegateRender(view, descriptor, value);
        createBindings(view, descriptor, value);
        view.setAutosaveBindings(true);
        view.setActionEventBuilder((source, params) -> new ActionEvent(view.getValue(), view));
        view.setValue(value);
        view.updateUI();
        return view;
    }

    private List<Parameter> loadConfigValue(ViewDescriptor descriptor) {
        List<Parameter> value = new ArrayList<>();
        descriptor.addParam(Viewers.PARAM_IGNORE_BINDINGS, true);


        for (Field field : descriptor.getFields()) {
            if (field.isVisible()) {
                loadParam(field, value);

                if (field.getComponent() == null || field.getComponent().equals("label")) {
                    field.setComponent("textbox");
                }

                if (field.getComponentClass() == null || field.getComponentClass() == Label.class) {
                    field.setComponentClass(Textbox.class);
                }
            }
        }

        return value;
    }

    private void createBindings(ConfigView view, ViewDescriptor descriptor, List<Parameter> value) {
        for (Field field : descriptor.getFields()) {
            Component component = view.getFieldComponent(field.getName()).getInputComponent();
            createBinding(component, field, view.getBinder(), view, value);
        }

    }

    protected void delegateRender(ConfigView view, ViewDescriptor descriptor, List<Parameter> value) {
        FormViewRenderer<List<Parameter>> delegateRenderer = new FormViewRenderer<>();
        delegateRenderer.render(view, descriptor, value);
    }

    protected void createBinding(Component comp, Field field, Binder binder, ConfigView view, List<Parameter> value) {
        String name = getParamName(field);
        Parameter param = value.stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null);

        if (param != null) {
            String attr = BindingComponentIndex.getInstance().getAttribute(comp.getClass());
            if (field.getParams().get(Viewers.PARAM_BINDING_ATTRIBUTE) instanceof String) {
                attr = field.getParams().get(Viewers.PARAM_BINDING_ATTRIBUTE).toString();
            }

            String typeConverter = null;
            if (field.getParams() != null) {
                typeConverter = (String) field.getParams().get(Viewers.PARAM_CONVERTER);
                typeConverter = Util.checkConverterClass(typeConverter);
            }

            if (attr != null && !attr.isEmpty()) {
                String expression = param.getName() + ".value";
                ZKBindingUtil.bindComponent(binder, comp, attr, expression, typeConverter);

            }
        }
    }

    private String getParamName(Field field) {
        String id = field.getViewDescriptor().getId();
        String paramName = id + "_" + field.getName();
        if (field.getParams().get(PARAM_PARAMETER_NAME) != null) {
            paramName = field.getParams().get(PARAM_PARAMETER_NAME).toString();
        }
        return paramName;
    }

    private Parameter loadParam(Field field, List<Parameter> params) {
        Class<? extends Parameter> parameterClass = getParameterClass(field.getViewDescriptor());
        String name = getParamName(field);

        Parameter par = BeanUtils.newInstance(parameterClass);
        par.setName(name);
        par.setValue(field.getParams().get(PARAM_DEFAULT_VALUE) != null ? field.getParams().get(PARAM_DEFAULT_VALUE).toString() : "");

        for (Parameter parameter : params) {
            if (parameter.getName().equals(name)) {
                par = parameter;
                break;
            }
        }
        if (par.getId() == null) {
            params.add(par);
        }
        par.setDescription(field.getDescription());
        par.setLabel(field.getLabel());
        par.setCacheable(field.getParams().get(PARAM_CACHEABLE) == Boolean.TRUE);
        return par;
    }

    private Class<? extends Parameter> getParameterClass(ViewDescriptor descriptor) {
        Class<? extends Parameter> parameterClass = DomainUtils.getDefaultParameterClass();
        String parameterClassName = (String) descriptor.getParams().get(PARAM_PARAMETER_CLASS);
        if (parameterClassName != null) {
            try {
                //noinspection unchecked
                parameterClass = (Class<? extends Parameter>) Class.forName(parameterClassName);
            } catch (ClassNotFoundException ex) {
                throw new ViewRendererException("Parameter Class not found " + parameterClassName, ex);
            }
        }
        return parameterClass;
    }

}
