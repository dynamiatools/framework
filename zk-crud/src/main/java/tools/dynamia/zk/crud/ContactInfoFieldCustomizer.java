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
package tools.dynamia.zk.crud;

import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.reflect.PropertyInfo;
import tools.dynamia.domain.util.AbstractContactInfo;
import tools.dynamia.integration.sterotypes.Provider;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.FieldGroup;
import tools.dynamia.zk.viewers.DefaultFieldCustomizer;

import java.util.List;

/**
 * Field Customizer that explode any field of type {@link AbstractContactInfo} into inner properties.
 * Only applicable to form and table views
 *
 * @author Mario A. Serrano Leones
 */
@Provider
public class ContactInfoFieldCustomizer extends DefaultFieldCustomizer {

    private static final List<String> APPLICABLE_VIEWS = List.of("form", "table");

    @Override
    public void customize(String viewTypeName, Field field) {
        if (APPLICABLE_VIEWS.contains(viewTypeName) && isContactInfo(field) && field.isVisible()) {
            FieldGroup group = field.getGroup();
            field.getViewDescriptor().removeField(field.getName());

            List<PropertyInfo> info = BeanUtils.getPropertiesInfo(AbstractContactInfo.class);

            for (PropertyInfo pi : info) {
                if (!"phones".equals(pi.getName())) {
                    Field newfield = new Field(field.getName() + "." + pi.getName(), pi.getType());

                    if (field.getViewDescriptor().getField(newfield.getName()) == null) {
                        newfield.setPropertyInfo(pi);
                        newfield.setLabel(pi.getName());
                        field.getViewDescriptor().addField(newfield);
                        if (group != null) {
                            group.addField(newfield);
                        }
                        super.customize(viewTypeName, newfield);
                    }
                }
            }

        }
    }

    private boolean isContactInfo(Field field) {
        return BeanUtils.isAssignable(field.getFieldClass(), AbstractContactInfo.class);
    }
}
