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
package tools.dynamia.commons.ops;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Specialized class for bean validation and property matching operations.
 * <p>
 * This class provides methods for validating bean properties, checking property existence,
 * comparing property values between beans, and matching beans against criteria.
 * Useful for business rule validation, data integrity checks, and filtering operations.
 * </p>
 *
 * <h2>Core Features</h2>
 * <ul>
 *   <li><strong>Property Existence:</strong> Check if properties exist and have values</li>
 *   <li><strong>Property Matching:</strong> Match beans against criteria maps</li>
 *   <li><strong>Property Comparison:</strong> Compare properties between beans</li>
 *   <li><strong>Custom Validation:</strong> Validate with custom predicates</li>
 *   <li><strong>Bulk Validation:</strong> Validate multiple properties at once</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 * <pre>{@code
 * Person person = getPerson();
 *
 * // Check property existence
 * boolean hasEmail = BeanValidator.hasProperty(person, "email");
 * boolean emailNull = BeanValidator.isPropertyNull(person, "email");
 *
 * // Match against criteria
 * boolean matches = BeanValidator.matchesProperties(person,
 *     Map.of("status", "ACTIVE", "country", "USA"));
 *
 * // Custom validation
 * Map<String, Boolean> results = BeanValidator.validateProperties(person, Map.of(
 *     "email", email -> email != null && email.toString().contains("@"),
 *     "age", age -> age != null && ((Number)age).intValue() >= 18
 * ));
 *
 * // Compare between beans
 * boolean sameBasicInfo = BeanValidator.arePropertiesEqual(person1, person2,
 *     "name", "email");
 * }</pre>
 *
 * @author Ing. Mario Serrano Leones
 * @version 26.1
 * @since 26.1
 */
public final class BeanValidator {

    /**
     * Private constructor to prevent instantiation.
     */
    private BeanValidator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Checks if a bean has a non-null value for the specified property.
     * <p>
     * This method returns true only if the property exists, is readable, and has a non-null value.
     * Returns false if the property doesn't exist, cannot be accessed, or is null.
     * </p>
     *
     * @param bean         the bean to check
     * @param propertyName the property name to check (supports dot notation)
     * @return true if property exists and has a non-null value, false otherwise
     *
     * Example:
     * <pre>{@code
     * Person person = new Person();
     * person.setName("John");
     * person.setEmail(null);
     *
     * boolean hasName = BeanValidator.hasProperty(person, "name"); // true
     * boolean hasEmail = BeanValidator.hasProperty(person, "email"); // false (null)
     * boolean hasAge = BeanValidator.hasProperty(person, "age"); // false (null)
     *
     * // Nested properties
     * boolean hasCity = BeanValidator.hasProperty(person, "address.city");
     * }</pre>
     */
    public static boolean hasProperty(Object bean, String propertyName) {
        if (bean == null || propertyName == null) {
            return false;
        }
        try {
            Object value = PropertyAccessor.invokeGetMethod(bean, propertyName);
            return value != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if a property value is null or doesn't exist.
     * <p>
     * This is the logical inverse of {@link #hasProperty(Object, String)}.
     * Returns true if the property is null, doesn't exist, or cannot be accessed.
     * </p>
     *
     * @param bean         the bean to check
     * @param propertyName the property name to check (supports dot notation)
     * @return true if property is null or doesn't exist, false if it has a value
     *
     * Example:
     * <pre>{@code
     * Person person = new Person();
     * person.setName("John");
     * person.setEmail(null);
     *
     * boolean nameNull = BeanValidator.isPropertyNull(person, "name"); // false
     * boolean emailNull = BeanValidator.isPropertyNull(person, "email"); // true
     *
     * // Use in validation
     * if (BeanValidator.isPropertyNull(person, "email")) {
     *     throw new ValidationException("Email is required");
     * }
     * }</pre>
     */
    public static boolean isPropertyNull(Object bean, String propertyName) {
        return !hasProperty(bean, propertyName);
    }

    /**
     * Checks if a bean matches all property criteria in the map.
     * <p>
     * This method tests if all properties specified in the criteria map match their expected values.
     * Returns true only if ALL criteria are satisfied. Uses equals() for comparison.
     * </p>
     *
     * @param bean     the bean to test
     * @param criteria map of property names to expected values
     * @return true if all criteria match, false otherwise
     *
     * Example:
     * <pre>{@code
     * Person person = new Person("John", "USA", "ACTIVE");
     *
     * // Single criterion
     * boolean isActive = BeanValidator.matchesProperties(person,
     *     Map.of("status", "ACTIVE"));
     *
     * // Multiple criteria
     * boolean matches = BeanValidator.matchesProperties(person,
     *     Map.of("status", "ACTIVE", "country", "USA"));
     *
     * // Use in filtering
     * List<Person> activeUSUsers = persons.stream()
     *     .filter(p -> BeanValidator.matchesProperties(p,
     *         Map.of("status", "ACTIVE", "country", "USA")))
     *     .collect(Collectors.toList());
     *
     * // Nested properties
     * boolean matchesAddress = BeanValidator.matchesProperties(person,
     *     Map.of("address.city", "New York", "address.state", "NY"));
     * }</pre>
     */
    public static boolean matchesProperties(Object bean, Map<String, Object> criteria) {
        if (bean == null || criteria == null || criteria.isEmpty()) {
            return false;
        }
        return criteria.entrySet().stream()
                .allMatch(entry -> {
                    try {
                        Object value = PropertyAccessor.invokeGetMethod(bean, entry.getKey());
                        Object expected = entry.getValue();
                        return expected == null ? value == null : expected.equals(value);
                    } catch (Exception e) {
                        return false;
                    }
                });
    }

    /**
     * Checks if specific properties are equal between two beans.
     * <p>
     * This method compares the specified properties of two beans and returns true
     * only if ALL specified properties have equal values. Uses equals() for comparison.
     * </p>
     *
     * @param bean1      the first bean to compare
     * @param bean2      the second bean to compare
     * @param properties the property names to compare (variable arguments)
     * @return true if all specified properties are equal, false otherwise
     *
     * Example:
     * <pre>{@code
     * Person person1 = new Person("John", "john@mail.com", 30);
     * Person person2 = new Person("John", "john@mail.com", 25);
     *
     * // Compare basic info (ignoring age)
     * boolean sameBasicInfo = BeanValidator.arePropertiesEqual(person1, person2,
     *     "name", "email"); // true
     *
     * // Compare all
     * boolean identical = BeanValidator.arePropertiesEqual(person1, person2,
     *     "name", "email", "age"); // false (different ages)
     *
     * // Use in deduplication
     * boolean isDuplicate = BeanValidator.arePropertiesEqual(newPerson, existingPerson,
     *     "name", "email", "birthDate");
     *
     * // Compare nested properties
     * boolean sameAddress = BeanValidator.arePropertiesEqual(person1, person2,
     *     "address.street", "address.city", "address.zipCode");
     * }</pre>
     */
    public static boolean arePropertiesEqual(Object bean1, Object bean2, String... properties) {
        if (bean1 == null || bean2 == null || properties == null) {
            return false;
        }
        return Stream.of(properties)
                .allMatch(property -> {
                    try {
                        Object value1 = PropertyAccessor.invokeGetMethod(bean1, property);
                        Object value2 = PropertyAccessor.invokeGetMethod(bean2, property);
                        return value1 == null ? value2 == null : value1.equals(value2);
                    } catch (Exception e) {
                        return false;
                    }
                });
    }

    /**
     * Validates multiple properties using custom predicates.
     * <p>
     * This method allows complex validation by testing each property against a custom predicate.
     * Returns a map with validation results for each property (true = valid, false = invalid).
     * Useful for form validation, business rule checking, and data integrity verification.
     * </p>
     *
     * @param bean       the bean to validate
     * @param validators map of property names to validation predicates
     * @return a map of property names to validation results (true = valid, false = invalid)
     *
     * Example:
     * <pre>{@code
     * Person person = new Person();
     * person.setName("John");
     * person.setEmail("invalid-email");
     * person.setAge(15);
     *
     * // Define validators
     * Map<String, Predicate<Object>> validators = Map.of(
     *     "name", name -> name != null && name.toString().length() >= 2,
     *     "email", email -> email != null && email.toString().contains("@"),
     *     "age", age -> age != null && ((Number)age).intValue() >= 18
     * );
     *
     * // Validate
     * Map<String, Boolean> results = BeanValidator.validateProperties(person, validators);
     * // results: {"name": true, "email": false, "age": false}
     *
     * // Check if all valid
     * boolean allValid = results.values().stream().allMatch(v -> v);
     *
     * // Get failed validations
     * List<String> failed = results.entrySet().stream()
     *     .filter(e -> !e.getValue())
     *     .map(Map.Entry::getKey)
     *     .collect(Collectors.toList());
     * // failed: ["email", "age"]
     *
     * // Use in REST API validation
     * if (!allValid) {
     *     throw new ValidationException("Validation failed: " + failed);
     * }
     * }</pre>
     */
    public static Map<String, Boolean> validateProperties(Object bean, Map<String, Predicate<Object>> validators) {
        Map<String, Boolean> results = new HashMap<>();
        if (bean == null || validators == null) {
            return results;
        }

        validators.forEach((property, predicate) -> {
            try {
                Object value = PropertyAccessor.invokeGetMethod(bean, property);
                results.put(property, predicate.test(value));
            } catch (Exception e) {
                results.put(property, false);
            }
        });

        return results;
    }

    /**
     * Checks if all specified properties have non-null values.
     * <p>
     * This is a convenience method to check multiple properties at once.
     * Returns true only if ALL specified properties exist and have non-null values.
     * </p>
     *
     * @param bean       the bean to check
     * @param properties the property names to check (variable arguments)
     * @return true if all properties have non-null values, false otherwise
     *
     * Example:
     * <pre>{@code
     * Person person = new Person();
     * person.setName("John");
     * person.setEmail("john@mail.com");
     * person.setPhone(null);
     *
     * // Check required fields
     * boolean hasRequired = BeanValidator.hasAllProperties(person, "name", "email");
     * // true
     *
     * boolean hasAll = BeanValidator.hasAllProperties(person, "name", "email", "phone");
     * // false (phone is null)
     *
     * // Use in validation
     * if (!BeanValidator.hasAllProperties(person, "name", "email")) {
     *     throw new ValidationException("Name and email are required");
     * }
     * }</pre>
     */
    public static boolean hasAllProperties(Object bean, String... properties) {
        if (bean == null || properties == null || properties.length == 0) {
            return false;
        }
        return Stream.of(properties).allMatch(prop -> hasProperty(bean, prop));
    }

    /**
     * Checks if at least one of the specified properties has a non-null value.
     * <p>
     * This method returns true if ANY of the specified properties exist and have non-null values.
     * Useful for "at least one required" validation scenarios.
     * </p>
     *
     * @param bean       the bean to check
     * @param properties the property names to check (variable arguments)
     * @return true if at least one property has a non-null value, false otherwise
     *
     * Example:
     * <pre>{@code
     * Person person = new Person();
     * person.setEmail(null);
     * person.setPhone("555-1234");
     *
     * // Check if any contact method exists
     * boolean hasContact = BeanValidator.hasAnyProperty(person, "email", "phone");
     * // true (phone is present)
     *
     * // Use in validation
     * if (!BeanValidator.hasAnyProperty(person, "email", "phone", "address")) {
     *     throw new ValidationException("At least one contact method is required");
     * }
     * }</pre>
     */
    public static boolean hasAnyProperty(Object bean, String... properties) {
        if (bean == null || properties == null || properties.length == 0) {
            return false;
        }
        return Stream.of(properties).anyMatch(prop -> hasProperty(bean, prop));
    }

    /**
     * Validates that a property matches a specific predicate.
     * <p>
     * This is a convenience method for validating a single property with a custom predicate.
     * </p>
     *
     * @param bean         the bean to validate
     * @param propertyName the property name to validate
     * @param validator    the validation predicate
     * @return true if the property is valid according to the predicate, false otherwise
     *
     * Example:
     * <pre>{@code
     * Person person = new Person();
     * person.setAge(25);
     *
     * // Validate single property
     * boolean isAdult = BeanValidator.validateProperty(person, "age",
     *     age -> age != null && ((Number)age).intValue() >= 18);
     * // true
     *
     * boolean validEmail = BeanValidator.validateProperty(person, "email",
     *     email -> email != null && email.toString().contains("@"));
     * }</pre>
     */
    public static boolean validateProperty(Object bean, String propertyName, Predicate<Object> validator) {
        if (bean == null || propertyName == null || validator == null) {
            return false;
        }
        try {
            Object value = PropertyAccessor.invokeGetMethod(bean, propertyName);
            return validator.test(value);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if any property in the bean matches any of the criteria.
     * <p>
     * Returns true if at least one property-value pair from criteria matches the bean.
     * Useful for "OR" condition matching.
     * </p>
     *
     * @param bean     the bean to test
     * @param criteria map of property names to expected values
     * @return true if any criterion matches, false otherwise
     *
     * Example:
     * <pre>{@code
     * Person person = new Person("John", "USA", "INACTIVE");
     *
     * // Check if person is either active OR from USA
     * boolean matches = BeanValidator.matchesAnyProperty(person,
     *     Map.of("status", "ACTIVE", "country", "USA"));
     * // true (country matches)
     *
     * // Use in filtering with OR logic
     * List<Person> filtered = persons.stream()
     *     .filter(p -> BeanValidator.matchesAnyProperty(p,
     *         Map.of("status", "ACTIVE", "role", "ADMIN")))
     *     .collect(Collectors.toList());
     * }</pre>
     */
    public static boolean matchesAnyProperty(Object bean, Map<String, Object> criteria) {
        if (bean == null || criteria == null || criteria.isEmpty()) {
            return false;
        }
        return criteria.entrySet().stream()
                .anyMatch(entry -> {
                    try {
                        Object value = PropertyAccessor.invokeGetMethod(bean, entry.getKey());
                        Object expected = entry.getValue();
                        return expected == null ? value == null : expected.equals(value);
                    } catch (Exception e) {
                        return false;
                    }
                });
    }

    /**
     * Checks if a property value is empty (null, empty string, empty collection, or zero).
     * <p>
     * This method provides a comprehensive "emptiness" check for different types:
     * - null values: empty
     * - Strings: empty if blank or ""
     * - Collections: empty if size == 0
     * - Numbers: empty if == 0
     * </p>
     *
     * @param bean         the bean to check
     * @param propertyName the property name to check
     * @return true if property is empty, false otherwise
     *
     * Example:
     * <pre>{@code
     * Person person = new Person();
     * person.setName("");
     * person.setAge(0);
     * person.setTags(new ArrayList<>());
     *
     * boolean nameEmpty = BeanValidator.isPropertyEmpty(person, "name"); // true
     * boolean ageEmpty = BeanValidator.isPropertyEmpty(person, "age"); // true
     * boolean tagsEmpty = BeanValidator.isPropertyEmpty(person, "tags"); // true
     * }</pre>
     */
    public static boolean isPropertyEmpty(Object bean, String propertyName) {
        if (bean == null || propertyName == null) {
            return true;
        }
        try {
            Object value = PropertyAccessor.invokeGetMethod(bean, propertyName);
            if (value == null) {
                return true;
            }
            if (value instanceof String) {
                return ((String) value).trim().isEmpty();
            }
            if (value instanceof Collection) {
                return ((Collection<?>) value).isEmpty();
            }
            if (value instanceof Map) {
                return ((Map<?, ?>) value).isEmpty();
            }
            if (value instanceof Number) {
                return ((Number) value).doubleValue() == 0.0;
            }
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Validates all properties and returns true only if ALL are valid.
     * <p>
     * Convenience method that combines {@link #validateProperties(Object, Map)}
     * with an all-valid check.
     * </p>
     *
     * @param bean       the bean to validate
     * @param validators map of property names to validation predicates
     * @return true if all validations pass, false if any fail
     *
     * Example:
     * <pre>{@code
     * boolean valid = BeanValidator.isValid(person, Map.of(
     *     "email", email -> email != null && email.toString().contains("@"),
     *     "age", age -> age != null && ((Number)age).intValue() >= 18
     * ));
     *
     * if (!valid) {
     *     throw new ValidationException("Invalid person data");
     * }
     * }</pre>
     */
    public static boolean isValid(Object bean, Map<String, Predicate<Object>> validators) {
        Map<String, Boolean> results = validateProperties(bean, validators);
        return results.values().stream().allMatch(v -> v);
    }

    /**
     * Gets a list of property names that failed validation.
     * <p>
     * This method validates all properties and returns only the names of those that failed.
     * Useful for generating error messages.
     * </p>
     *
     * @param bean       the bean to validate
     * @param validators map of property names to validation predicates
     * @return list of property names that failed validation (empty if all valid)
     *
     * Example:
     * <pre>{@code
     * List<String> failed = BeanValidator.getInvalidProperties(person, Map.of(
     *     "name", name -> name != null && name.toString().length() >= 2,
     *     "email", email -> email != null && email.toString().contains("@"),
     *     "age", age -> age != null && ((Number)age).intValue() >= 18
     * ));
     *
     * if (!failed.isEmpty()) {
     *     throw new ValidationException("Invalid fields: " + String.join(", ", failed));
     * }
     * }</pre>
     */
    public static List<String> getInvalidProperties(Object bean, Map<String, Predicate<Object>> validators) {
        Map<String, Boolean> results = validateProperties(bean, validators);
        return results.entrySet().stream()
                .filter(entry -> !entry.getValue())
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toList());
    }
}
