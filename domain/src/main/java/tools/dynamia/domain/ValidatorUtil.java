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
package tools.dynamia.domain;

import tools.dynamia.commons.DateTimeUtils;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.domain.contraints.EmailValidator;
import tools.dynamia.domain.services.ValidatorService;
import tools.dynamia.integration.Containers;

import java.util.Collection;
import java.util.Date;


/**
 * The Class ValidatorUtil.
 */
public class ValidatorUtil {

    final static EmailValidator EMAIL_VALIDATOR = new EmailValidator();

    /**
     * Execute validators.
     *
     * @param <T>    the generic type
     * @param object the object
     * @throws ValidationError the validation error
     */
    public static <T> void executeValidators(T object) throws ValidationError {
        Collection<Validator> validators = Containers.get().findObjects(Validator.class);
        if (validators != null) {
            for (Validator validator : validators) {
                try {
                    validator.validate(object);
                } catch (ClassCastException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * If object is null throw a new {@link ValidationError} exception with message
     *
     * @param object
     * @param message
     */
    public static void validateNull(Object object, String message) {
        if (object == null) {
            throw new ValidationError(message);
        }
    }

    /**
     * Validate if collection is null or empty and throw a {@link ValidationError} with message
     *
     * @param collections
     * @param message
     */
    public static void validateEmpty(Collection collections, String message) {
        if (collections == null || collections.isEmpty()) {
            throw new ValidationError(message);
        }
    }

    /**
     * Validate if text is null or empty and throw a {@link ValidationError} with message
     *
     * @param text
     * @param message
     */
    public static void validateEmpty(String text, String message) {
        if (text == null || text.isBlank()) {
            throw new ValidationError(message);
        }
    }

    /**
     * Validate if collection size is greather than maxSize and throw {@link ValidationError} with message
     *
     * @param collection
     * @param maxSize
     * @param message
     */
    public static void validateMaxSize(Collection collection, int maxSize, String message) {
        if (collection != null && collection.size() > maxSize) {
            throw new ValidationError(message);
        }
    }

    /**
     * Validate is text size is greather than maxSize and throw {@link ValidationError} with message
     *
     * @param text
     * @param maxSize
     * @param message
     */
    public static void validateMaxSize(String text, int maxSize, String message) {
        if (text != null && text.length() > maxSize) {
            throw new ValidationError(message);
        }
    }

    /**
     * Validate if collection size is less than minSize and throw {@link ValidationError} with message
     *
     * @param collection
     * @param minSize
     * @param message
     */
    public static void validateMinSize(Collection collection, int minSize, String message) {
        if (collection != null && collection.size() < minSize) {
            throw new ValidationError(message);
        }
    }

    /**
     * Validate is text size is less than minSize and throw {@link ValidationError} with message
     *
     * @param text
     * @param minSize
     * @param message
     */
    public static void validateMinSize(String text, int minSize, String message) {
        if (text != null && text.length() < minSize) {
            throw new ValidationError(message);
        }
    }

    /**
     * Validate if date is past and throw {@link ValidationError} with message
     *
     * @param date
     * @param message
     */
    public static void validatePastDate(Date date, String message) {
        if (date != null && DateTimeUtils.isPast(date)) {
            throw new ValidationError(message);
        }
    }

    /**
     * Validate if date is future and throw {@link ValidationError} with message
     *
     * @param date
     * @param message
     */
    public static void validateFutureDate(Date date, String message) {
        if (date != null && DateTimeUtils.isFuture(date)) {
            throw new ValidationError(message);
        }
    }

    /**
     * Validate if email is not a valid address and throw {@link ValidationError} message
     *
     * @param email
     * @param message
     */
    public static void validateEmail(String email, String message) {

        if (!EMAIL_VALIDATOR.isValid(email)) {
            throw new ValidationError(message);
        }
    }

    /**
     * Check if email is valid
     *
     * @param email
     * @return
     */
    public static boolean isValidEmail(String email) {
        return EMAIL_VALIDATOR.isValid(email);
    }

    /**
     * Validate if the is not a number and throw {@link ValidationError} message
     *
     * @param text
     * @param message
     */
    public static void validateTextIsNumber(String text, String message) {
        if (text != null && !StringUtils.isNumber(text)) {
            throw new ValidationError(message);
        }
    }

    /**
     * Validate if number is negative and throw {@link ValidationError} with message
     *
     * @param number
     * @param message
     */
    public static void validateNegative(Number number, String message) {
        if (number != null && number.doubleValue() < 0) {
            throw new ValidationError(message);
        }
    }

    /**
     * Validate if the value is true. If not throw an exception
     *
     * @param value
     * @param message
     */
    public static void validateTrue(boolean value, String message) {
        if (!value) {
            throw new ValidationError(message);
        }
    }

    /**
     * Validate if the value is false. If not throw an exception
     *
     * @param value
     * @param message
     */
    public static void validateFalse(boolean value, String message) {
        if (value) {
            throw new ValidationError(message);
        }
    }

    public static void validate(Object object) {
        var service = Containers.get().findObject(ValidatorService.class);
        if (service != null) {
            service.validate(object);
        }
    }

    private ValidatorUtil() {
    }
}
