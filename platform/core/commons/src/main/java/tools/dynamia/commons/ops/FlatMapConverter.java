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

import tools.dynamia.commons.reflect.PropertyInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Specialized class for converting beans to and from flat maps with dot notation.
 * <p>
 * This class provides methods for flattening nested bean structures into flat maps
 * using dot notation for nested properties (e.g., "address.city"), and reconstructing
 * beans from such flat maps. Useful for form processing, data export/import, and
 * configuration management.
 * </p>
 *
 * <h2>Core Features</h2>
 * <ul>
 *   <li><strong>Bean to Flat Map:</strong> Flatten nested objects using dot notation</li>
 *   <li><strong>Flat Map to Bean:</strong> Reconstruct beans from flat maps</li>
 *   <li><strong>Nested Property Support:</strong> Handles arbitrary nesting levels</li>
 *   <li><strong>Automatic Instantiation:</strong> Creates nested objects as needed</li>
 *   <li><strong>Type Safety:</strong> Preserves property types during conversion</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 * <pre>{@code
 * // Bean to Flat Map
 * Person person = new Person();
 * person.setName("John");
 * Address address = new Address();
 * address.setCity("New York");
 * person.setAddress(address);
 *
 * Map<String, Object> flat = FlatMapConverter.toFlatMap(person);
 * // Result: {"name": "John", "address.city": "New York", "address.zipCode": null, ...}
 *
 * // Flat Map to Bean
 * Map<String, Object> data = Map.of(
 *     "name", "Jane",
 *     "address.city", "Los Angeles",
 *     "address.state", "CA"
 * );
 * Person newPerson = FlatMapConverter.fromFlatMap(data, Person.class);
 * // newPerson.name = "Jane"
 * // newPerson.address.city = "Los Angeles"
 * // newPerson.address.state = "CA"
 * }</pre>
 *
 * @author Ing. Mario Serrano Leones
 * @version 26.1
 * @since 26.1
 */
public final class FlatMapConverter {

    /**
     * Private constructor to prevent instantiation.
     */
    private FlatMapConverter() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Converts a bean to a flat Map using dot notation for nested properties.
     * <p>
     * This method recursively flattens all nested bean properties into a single-level map.
     * Nested properties are represented using dot notation (e.g., "address.city").
     * Collections and arrays are skipped. Only simple properties and nested objects are processed.
     * </p>
     *
     * @param bean the bean to flatten
     * @return a flat map with dot notation for nested properties, or empty map if bean is null
     *
     * Example:
     * <pre>{@code
     * Person person = new Person();
     * person.setName("John Doe");
     * person.setAge(30);
     *
     * Address address = new Address();
     * address.setCity("New York");
     * address.setState("NY");
     * address.setZipCode("10001");
     * person.setAddress(address);
     *
     * Map<String, Object> flat = FlatMapConverter.toFlatMap(person);
     * // Result: {
     * //   "name": "John Doe",
     * //   "age": 30,
     * //   "address.city": "New York",
     * //   "address.state": "NY",
     * //   "address.zipCode": "10001"
     * // }
     *
     * // Use for form data export
     * Map<String, Object> formData = FlatMapConverter.toFlatMap(user);
     * return formData; // Send to frontend
     *
     * // Use for logging
     * logger.info("User data: {}", FlatMapConverter.toFlatMap(user));
     * }</pre>
     */
    public static Map<String, Object> toFlatMap(Object bean) {
        return toFlatMap(bean, "", new HashMap<>());
    }

    /**
     * Creates a bean from a flat Map with dot notation for nested properties.
     * <p>
     * This method reconstructs a bean from a flat map where nested properties are represented
     * using dot notation. It automatically creates nested objects as needed and sets properties
     * at all levels. If a nested object already exists, it reuses it; otherwise, it creates
     * a new instance.
     * </p>
     *
     * @param <T>         the target type
     * @param flatMap     the flat map with dot notation (e.g., {"name": "John", "address.city": "NY"})
     * @param targetClass the target class to create
     * @return a new instance with properties set from the flat map, or null if inputs are null
     *
     * Example:
     * <pre>{@code
     * // Create bean from flat map
     * Map<String, Object> data = Map.of(
     *     "name", "Jane Doe",
     *     "age", 25,
     *     "address.city", "Los Angeles",
     *     "address.state", "CA",
     *     "address.zipCode", "90001"
     * );
     *
     * Person person = FlatMapConverter.fromFlatMap(data, Person.class);
     * // person.name = "Jane Doe"
     * // person.age = 25
     * // person.address.city = "Los Angeles"
     * // person.address.state = "CA"
     * // person.address.zipCode = "90001"
     *
     * // Use in form processing
     * @PostMapping("/users")
     * public User createUser(@RequestBody Map<String, Object> formData) {
     *     User user = FlatMapConverter.fromFlatMap(formData, User.class);
     *     return userRepository.save(user);
     * }
     *
     * // Use in configuration loading
     * Map<String, Object> config = loadConfigFromFile();
     * AppSettings settings = FlatMapConverter.fromFlatMap(config, AppSettings.class);
     * }</pre>
     */
    public static <T> T fromFlatMap(Map<String, Object> flatMap, Class<T> targetClass) {
        if (flatMap == null || targetClass == null) {
            return null;
        }

        T instance = ObjectCloner.newInstance(targetClass);

        // Group properties by root and nested
        Map<String, Object> directProps = new HashMap<>();
        Map<String, Map<String, Object>> nestedProps = new HashMap<>();

        for (Map.Entry<String, Object> entry : flatMap.entrySet()) {
            String key = entry.getKey();
            if (key.contains(".")) {
                String root = key.substring(0, key.indexOf('.'));
                String rest = key.substring(key.indexOf('.') + 1);
                nestedProps.computeIfAbsent(root, k -> new HashMap<>()).put(rest, entry.getValue());
            } else {
                directProps.put(key, entry.getValue());
            }
        }

        // Set direct properties
        BeanTransformer.setupBean(instance, directProps);

        // Set nested properties
        for (Map.Entry<String, Map<String, Object>> entry : nestedProps.entrySet()) {
            try {
                Object nestedBean = PropertyAccessor.invokeGetMethod(instance, entry.getKey());
                if (nestedBean == null) {
                    PropertyInfo propInfo = tools.dynamia.commons.ObjectOperations.getPropertyInfo(
                        targetClass, entry.getKey());
                    if (propInfo != null) {
                        nestedBean = ObjectCloner.newInstance(propInfo.getType());
                        PropertyAccessor.invokeSetMethod(instance, entry.getKey(), nestedBean);
                    }
                }
                if (nestedBean != null) {
                    BeanTransformer.setupBean(nestedBean, entry.getValue());
                }
            } catch (Exception e) {
                // ignore properties that can't be set
            }
        }

        return instance;
    }

    /**
     * Converts a bean to a flat map with custom prefix.
     * <p>
     * This is useful when you want to flatten a bean with a specific prefix for all keys,
     * for example when merging multiple beans into a single flat map.
     * </p>
     *
     * @param bean   the bean to flatten
     * @param prefix the prefix to prepend to all keys
     * @return a flat map with prefixed keys
     *
     * Example:
     * <pre>{@code
     * Person person = getPerson();
     * Address address = getAddress();
     *
     * Map<String, Object> combined = new HashMap<>();
     * combined.putAll(FlatMapConverter.toFlatMapWithPrefix(person, "person"));
     * combined.putAll(FlatMapConverter.toFlatMapWithPrefix(address, "address"));
     *
     * // Result: {
     * //   "person.name": "John",
     * //   "person.age": 30,
     * //   "address.city": "New York",
     * //   "address.state": "NY"
     * // }
     * }</pre>
     */
    public static Map<String, Object> toFlatMapWithPrefix(Object bean, String prefix) {
        if (bean == null) {
            return new HashMap<>();
        }
        return toFlatMap(bean, prefix, new HashMap<>());
    }

    /**
     * Merges multiple beans into a single flat map with prefixes.
     * <p>
     * This method flattens multiple beans and combines them into one flat map,
     * using the provided prefixes to avoid key collisions.
     * </p>
     *
     * @param beansWithPrefixes map of prefixes to beans
     * @return a merged flat map containing all beans with their prefixes
     *
     * Example:
     * <pre>{@code
     * Person person = getPerson();
     * Company company = getCompany();
     *
     * Map<String, Object> merged = FlatMapConverter.mergeFlatMaps(Map.of(
     *     "employee", person,
     *     "employer", company
     * ));
     *
     * // Result: {
     * //   "employee.name": "John",
     * //   "employee.age": 30,
     * //   "employer.name": "ACME Corp",
     * //   "employer.address.city": "New York"
     * // }
     * }</pre>
     */
    public static Map<String, Object> mergeFlatMaps(Map<String, Object> beansWithPrefixes) {
        Map<String, Object> result = new HashMap<>();
        if (beansWithPrefixes != null) {
            beansWithPrefixes.forEach((prefix, bean) ->
                result.putAll(toFlatMapWithPrefix(bean, prefix)));
        }
        return result;
    }

    /**
     * Extracts a subset of a flat map matching a specific prefix.
     * <p>
     * This method filters a flat map to only include keys that start with the given prefix,
     * and optionally removes the prefix from the resulting keys.
     * </p>
     *
     * @param flatMap      the source flat map
     * @param prefix       the prefix to filter by
     * @param removePrefix whether to remove the prefix from resulting keys
     * @return a filtered flat map
     *
     * Example:
     * <pre>{@code
     * Map<String, Object> data = Map.of(
     *     "user.name", "John",
     *     "user.email", "john@example.com",
     *     "company.name", "ACME"
     * );
     *
     * // Extract user data with prefix removed
     * Map<String, Object> userData = FlatMapConverter.extractByPrefix(data, "user", true);
     * // Result: {"name": "John", "email": "john@example.com"}
     *
     * // Extract user data keeping prefix
     * Map<String, Object> userDataWithPrefix = FlatMapConverter.extractByPrefix(data, "user", false);
     * // Result: {"user.name": "John", "user.email": "john@example.com"}
     * }</pre>
     */
    public static Map<String, Object> extractByPrefix(Map<String, Object> flatMap,
                                                       String prefix,
                                                       boolean removePrefix) {
        Map<String, Object> result = new HashMap<>();
        if (flatMap == null || prefix == null) {
            return result;
        }

        String searchPrefix = prefix.endsWith(".") ? prefix : prefix + ".";

        flatMap.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(searchPrefix))
            .forEach(entry -> {
                String key = removePrefix
                    ? entry.getKey().substring(searchPrefix.length())
                    : entry.getKey();
                result.put(key, entry.getValue());
            });

        return result;
    }

    /**
     * Helper method for recursive flattening of bean properties.
     * <p>
     * This method recursively traverses nested bean structures and flattens them
     * into a single-level map using dot notation for nested properties.
     * </p>
     *
     * @param bean   the current bean to flatten
     * @param prefix the current prefix for nested properties
     * @param result the accumulator map
     * @return the flat map with all properties added
     */
    private static Map<String, Object> toFlatMap(Object bean, String prefix, Map<String, Object> result) {
        if (bean == null) {
            return result;
        }

        List<PropertyInfo> properties = tools.dynamia.commons.ObjectOperations.getPropertiesInfo(bean.getClass());
        for (PropertyInfo prop : properties) {
            try {
                Object value = PropertyAccessor.invokeGetMethod(bean, prop);
                String key = prefix.isEmpty() ? prop.getName() : prefix + "." + prop.getName();

                if (value == null || prop.isStandardClass() || prop.isEnum()) {
                    result.put(key, value);
                } else if (!prop.isCollection() && !prop.isArray()) {
                    // Recursively flatten nested objects
                    toFlatMap(value, key, result);
                }
            } catch (Exception e) {
                // ignore properties that can't be read
            }
        }
        return result;
    }
}
