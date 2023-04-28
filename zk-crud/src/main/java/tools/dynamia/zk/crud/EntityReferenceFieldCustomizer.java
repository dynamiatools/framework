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
package tools.dynamia.zk.crud;

import tools.dynamia.commons.BeanUtils;
import tools.dynamia.domain.EntityReference;
import tools.dynamia.domain.Reference;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.integration.sterotypes.Provider;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.FieldCustomizer;
import tools.dynamia.zk.crud.ui.EntityReferencePickerBox;

/**
 *
 * @author Mario A. Serrano Leones
 */
@Provider
public class EntityReferenceFieldCustomizer implements FieldCustomizer {

	@Override
	public void customize(String viewTypeName, Field field) {
		if (field == null || !field.isVisible()) {
			return;
		}

		Reference reference = getReferenceField(field);
		if (!BeanUtils.isAssignable(field.getFieldClass(), EntityReference.class) && reference == null) {
			return;
		}

		if (reference != null) {
			field.addParam("entityAlias", reference.value());

			if (DomainUtils.getEntityReferenceRepositoryByAlias(reference.value()) == null) {
				field.setVisible(false);
			}
		}

		if (viewTypeName.equals("form")) {
			field.setComponentClass(EntityReferencePickerBox.class);
		}
	}

	private Reference getReferenceField(Field field) {
		if (field != null && field.getViewDescriptor() != null) {
			Class beanClass = field.getViewDescriptor().getBeanClass();
			if (beanClass != null) {
				try {
					java.lang.reflect.Field classField = BeanUtils.getField(beanClass, field.getName());
					return classField.getAnnotation(Reference.class);
				} catch (NoSuchFieldException e) {

				}
			}
		}
		return null;

	}
}
