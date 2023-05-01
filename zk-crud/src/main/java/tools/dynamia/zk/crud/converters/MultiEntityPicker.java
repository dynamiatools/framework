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

package tools.dynamia.zk.crud.converters;

import org.zkoss.zk.ui.Component;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.reflect.PropertyInfo;
import tools.dynamia.viewers.Field;
import tools.dynamia.zk.viewers.form.FormConverter;
import tools.dynamia.zk.viewers.form.FormView;

import java.util.ArrayList;
import java.util.List;

//TODO: Terminar de implementar este Converter
public class MultiEntityPicker extends FormConverter<List, List> {

    @Override
    public List convertToUi(List beanValue, Object formValue, Component comp) {
        tools.dynamia.zk.crud.ui.MultiEntityPicker mep = (tools.dynamia.zk.crud.ui.MultiEntityPicker) comp;
        Field field = getField(comp);

        Class uiValueClass = mep.getEntityClass();
        Class beanValueClass = field.getClass();
        List values = new ArrayList();
        for (Object object : beanValue) {
            Object child = findDep(object, beanValueClass, uiValueClass);
            //noinspection unchecked
            values.add(child);
        }

        return values;
    }

    @Override
    public List convertToBean(List uiValue, Object formValue, Component comp) {
        tools.dynamia.zk.crud.ui.MultiEntityPicker mep = (tools.dynamia.zk.crud.ui.MultiEntityPicker) comp;
        Field field = getField(comp);

        Class uiValueClass = mep.getEntityClass();
        Class beanValueClass = field.getClass();

        List values = (List) BeanUtils.invokeGetMethod(formValue, field.getName());
        for (Object uiv : uiValue) {

        }

        return values;
    }

    private Object findDep(Object bean, Class beanValueClass, Class uiValueClass) {
        List<PropertyInfo> info = BeanUtils.getPropertiesInfo(beanValueClass);
        for (PropertyInfo prop : info) {
            if (prop.getType() == uiValueClass) {
                return BeanUtils.invokeGetMethod(bean, prop.getName());
            }
        }
        return null;
    }

    private Field getField(Component comp) {
        FormView view = (FormView) comp.getAttribute("form-view");
        String fieldName = (String) comp.getAttribute("field-name");

        return view.getViewDescriptor().getField(fieldName);
    }

}
