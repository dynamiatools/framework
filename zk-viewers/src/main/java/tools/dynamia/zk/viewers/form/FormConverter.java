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

package tools.dynamia.zk.viewers.form;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;


@SuppressWarnings("deprecation")
public abstract class FormConverter<UI, BEAN> implements Converter<Object, Object, Component> {

    @Override
    public Object coerceToUi(Object val, Component comp, BindContext ctx) {
        FormView formView = (FormView) comp.getAttribute("form-view");
        Object formValue = formView.getRawValue();
        return convertToUi((BEAN) val, formValue, comp);
    }

    @Override
    public Object coerceToBean(Object val, Component comp, BindContext ctx) {
        FormView formView = (FormView) comp.getAttribute("form-view");
        Object formValue = formView.getRawValue();
        return convertToBean((UI) val, formValue, comp);
    }


    public abstract UI convertToUi(BEAN beanValue, Object formValue, Component comp);

    public abstract BEAN convertToBean(UI uiValue, Object formValue, Component comp);

}
