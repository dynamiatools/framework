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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * The Class EmailValidator.
 *
 * @author Mario A. Serrano Leones
 */
public class EmailValidator implements ConstraintValidator<Email, CharSequence> {

    /**
     * The Constant EMAIL_PATTERN.
     */
    public static final String EMAIL_PATTERN = "^(?=.{1,64}@)[A-Za-z0-9\\+_-]+(\\.[A-Za-z0-9\\+_-]+)*@"
            + "[^-][A-Za-z0-9\\+-]+(\\.[A-Za-z0-9\\+-]+)*(\\.[A-Za-z]{2,})$";

    /**
     * The Constant pattern.
     */
    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    /* (non-Javadoc)
     * @see javax.validation.ConstraintValidator#initialize(java.lang.annotation.Annotation)
     */
    @Override
    public void initialize(Email constraintAnnotation) {
    }

    /* (non-Javadoc)
     * @see javax.validation.ConstraintValidator#isValid(java.lang.Object, javax.validation.ConstraintValidatorContext)
     */
    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value != null) {
            /**
             * The matcher.
             */
            Matcher matcher = pattern.matcher(value);
            return matcher.matches();
        } else {
            return true;
        }
    }

    public boolean isValid(CharSequence value) {
        return isValid(value, null);
    }
}
