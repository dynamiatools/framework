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
package tools.dynamia.domain.contraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Collection;
import java.util.Map;


/**
 * The Class NotEmptyValidator.
 *
 * @author Mario A. Serrano Leones
 */
public class NotEmptyValidator implements ConstraintValidator<NotEmpty, Object> {

    /* (non-Javadoc)
     * @see jakarta.validation.ConstraintValidator#initialize(java.lang.annotation.Annotation)
     */
    @Override
    public void initialize(NotEmpty constraintAnnotation) {
    }

    /* (non-Javadoc)
     * @see jakarta.validation.ConstraintValidator#isValid(java.lang.Object, javax.validation.ConstraintValidatorContext)
     */
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        return switch (value) {
            case null -> false;
            case CharSequence charSequence -> !value.toString().trim().isEmpty();
            case Collection collection -> !collection.isEmpty();
            case Map map -> !map.isEmpty();
            default -> true;
        };

    }

    public boolean isValid(Object value) {
        return isValid(value, null);
    }
}
