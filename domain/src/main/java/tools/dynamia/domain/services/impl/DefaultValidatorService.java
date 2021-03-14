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
package tools.dynamia.domain.services.impl;

import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.services.ValidatorService;
import tools.dynamia.integration.Containers;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Collection;
import java.util.Set;

/**
 * ValidatorService Default Implementation.
 *
 * @author Mario A. Serrano Leones
 */

public class DefaultValidatorService implements ValidatorService {

	/**
	 * The validator factory.
	 */
	private ValidatorFactory validatorFactory;

	/**
	 * The validator.
	 */
	private Validator validator;

	/**
	 * Instantiates a new validator service impl.
	 */
	public DefaultValidatorService() {
		validatorFactory = Validation.buildDefaultValidatorFactory();
		validator = validatorFactory.getValidator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dynamia.tools.domain.services.ValidatorService#validate(java.lang.
	 * Object)
	 */
	@Override
	/**
	 * Validate obj and throw a ValidationError exception if the object has an
	 * invalid value, nothing happens if the obj is ok. Use this method as a
	 * filter
	 */
	public void validate(Object obj) {
		fireExternalsValidators(obj);

		Set<ConstraintViolation<Object>> violations = validator.validate(obj);
		if (violations != null && !violations.isEmpty()) {
			for (ConstraintViolation<Object> cv : violations) {
				throw new ValidationError(cv.getPropertyPath().toString() + "  " + cv.getMessage(),
						cv.getInvalidValue(), cv.getPropertyPath().toString(), obj.getClass());
			}
		}

	}

	/**
	 * Fire externals validators.
	 *
	 * @param obj
	 *            the obj
	 */
	private void fireExternalsValidators(Object obj) {
		Collection<tools.dynamia.domain.Validator> validators = Containers.get()
				.findObjects(tools.dynamia.domain.Validator.class);
		if (validators != null) {

			for (tools.dynamia.domain.Validator validator : validators) {
				try {
					validator.validate(obj);
				} catch (ClassCastException e) {
					// ignore
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dynamia.tools.domain.services.ValidatorService#validateAll(java.lang.
	 * Object)
	 */
	@Override
	/**
	 * Validate all obj properties and return all the constraint violations
	 *
	 * @return Set<ConstraintViolation>
	 */
	public <T> Set<ConstraintViolation<T>> validateAll(T obj) {
		return validator.validate(obj);

	}
}
