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

import org.zkoss.zul.Label;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.integration.sterotypes.Provider;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.FieldCustomizer;
import tools.dynamia.zk.crud.ui.EntityLink;
import tools.dynamia.zk.crud.ui.EntityPickerBox;

/**
 * @author Mario A. Serrano Leones
 */
@Provider
public class EntityFieldCustomizer implements FieldCustomizer {

    @Override
    public void customize(String viewTypeName, Field field) {
        if (field == null || !field.isVisible()) {
            return;
        }

        if (!DomainUtils.isEntity(field.getFieldClass())) {
            return;
        }

        if ((viewTypeName.equals("form") && (field.getComponentClass() == Label.class || field.getComponentClass() == null))
                || field.getComponentClass() == EntityPickerBox.class) {

            field.setComponentClass(EntityPickerBox.class);
            field.set("entityClass", field.getFieldClass());

        }

        if (viewTypeName.equals("table") && (field.getComponentClass() == Label.class || field.getComponentClass() == null)) {
            field.setComponentClass(EntityLink.class);
        }
    }
}
