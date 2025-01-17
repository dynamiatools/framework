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

package tools.dynamia.zk.constraints;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zul.Constraint;
import tools.dynamia.commons.Messages;

public class Required implements Constraint {

	private boolean required = true;

	public Required() {
	}

	@Override
	public void validate(Component comp, Object value) throws WrongValueException {

		if (comp != null && comp.getId() != null && !comp.getId().isEmpty() && comp.getParent() != null) {
			if (value == null || value.toString().isEmpty()) {
				throw new WrongValueException(comp, Messages.get(Required.class, "required"));
			}
		}
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}
}
