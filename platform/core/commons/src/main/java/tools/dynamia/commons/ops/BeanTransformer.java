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

import org.springframework.beans.*;
import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.commons.ValueWrapper;
import tools.dynamia.commons.logger.LoggingService;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Specialized class for bean transformation and property copying operations.
 * <p>
 * This class provides powerful methods for transforming objects between different types,
 * copying properties, and creating modified clones. It's particularly useful for DTO conversions,
 * API response mapping, and data layer transformations.
 * </p>
 *
 * <h2>Core Features</h2>
 * <ul>
 *   <li><strong>Object Transformation:</strong> Convert objects between different types</li>
 *   <li><strong>Collection Transformation:</strong> Transform entire collections efficiently</li>
 *   <li><strong>Property Setup:</strong> Copy properties from maps or other objects</li>
 *   <li><strong>Map Conversion:</strong> Extract specific properties into maps</li>
 *   <li><strong>Copy with Modification:</strong> Clone and modify in one operation</li>
 *   <li><strong>Spring Integration:</strong> Uses BeanUtils for optimal performance</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 * <pre>{@code
 * // Transform single object
 * Person person = getPerson();
 * PersonDTO dto = BeanTransformer.transform(person, PersonDTO.class);
 *
 * // Transform collection
 * List<Person> persons = getPersons();
 * List<PersonDTO> dtos = BeanTransformer.transformAll(persons, PersonDTO.class);
 *
 * // Extract properties to map
 * Map<String, Object> data = BeanTransformer.mapToMap(person, "name", "email", "age");
 *
 * // Setup bean from map
 * Map<String, Object> values = new HashMap<>();
 * values.put("name", "John");
 * values.put("age", 30);
 * BeanTransformer.setupBean(person, values);
 *
 * // Copy and modify
 * Person modified = BeanTransformer.copyWith(person, p -> {
 *     p.setStatus("ACTIVE");
 *     p.setUpdatedAt(new Date());
 * });
 * }</pre>
 *
 * @author Ing. Mario Serrano Leones
 * @version 26.1
 * @since 26.1
 */
public final class BeanTransformer {

    private static final LoggingService LOGGER = LoggingService.get(BeanTransformer.class);

    /**
     * Private constructor to prevent instantiation.
     */
    private BeanTransformer() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Transforms a source object to a target class by copying properties.
     * <p>
     * Creates a new instance of the target class and copies all matching properties
     * from the source object. Uses Spring's {@link BeanUtils#copyProperties} for
     * efficient property copying with automatic type conversion.
     * </p>
     * <p>
     * Properties are matched by name. Only properties with matching names and compatible
     * types are copied. Missing properties in either source or target are ignored.
     * </p>
     *
     * @param <S>         the source type
     * @param <T>         the target type
     * @param source      the source object to transform
     * @param targetClass the target class to transform into
     * @return a new instance of target class with copied properties, or null if source is null
     * <p>
     * Example:
     * <pre>{@code
     * // Entity to DTO transformation
     * Person person = personRepository.findById(1L);
     * PersonDTO dto = BeanTransformer.transform(person, PersonDTO.class);
     *
     * // DTO to Entity transformation
     * PersonDTO dto = getPersonDTO();
     * Person person = BeanTransformer.transform(dto, Person.class);
     *
     * // Works with any compatible types
     * Order order = getOrder();
     * OrderResponse response = BeanTransformer.transform(order, OrderResponse.class);
     * }</pre>
     */
    public static <S, T> T transform(S source, Class<T> targetClass) {
        if (source == null || targetClass == null) {
            return null;
        }
        T target = ObjectCloner.newInstance(targetClass);
        BeanUtils.copyProperties(source, target);
        return target;
    }

    /**
     * Transforms a collection of objects to another type by copying properties.
     * <p>
     * This method efficiently transforms each object in the collection using
     * Java 8 Streams and the {@link #transform(Object, Class)} method.
     * Returns a new ArrayList containing the transformed objects.
     * </p>
     *
     * @param <S>         the source type
     * @param <T>         the target type
     * @param collection  the source collection to transform
     * @param targetClass the target class to transform into
     * @return a list of transformed objects, or empty list if collection is null
     * <p>
     * Example:
     * <pre>{@code
     * // Transform list of entities to DTOs
     * List<Person> persons = personRepository.findAll();
     * List<PersonDTO> dtos = BeanTransformer.transformAll(persons, PersonDTO.class);
     *
     * // Transform set to list of different type
     * Set<Product> products = getProducts();
     * List<ProductDTO> productDtos = BeanTransformer.transformAll(products, ProductDTO.class);
     *
     * // Use in REST API
     * @GetMapping("/users")
     * public List<UserDTO> getUsers() {
     *     List<User> users = userService.findAll();
     *     return BeanTransformer.transformAll(users, UserDTO.class);
     * }
     * }</pre>
     */
    public static <S, T> List<T> transformAll(Collection<S> collection, Class<T> targetClass) {
        if (collection == null || targetClass == null) {
            return new ArrayList<>();
        }
        return collection.stream()
                .map(source -> transform(source, targetClass))
                .collect(Collectors.toList());
    }

    /**
     * Extracts only the specified properties from a bean into a Map.
     * <p>
     * This method creates a map containing only the properties you specify,
     * useful for creating partial responses, logging, or data export.
     * Properties that don't exist or cannot be read are included with null values.
     * </p>
     *
     * @param bean       the source bean to extract properties from
     * @param properties the property names to extract (variable arguments)
     * @return a map with property names as keys and their values, or empty map if bean is null
     * <p>
     * Example:
     * <pre>{@code
     * Person person = getPerson();
     *
     * // Extract specific fields for logging
     * Map<String, Object> logData = BeanTransformer.mapToMap(person, "id", "name", "email");
     * logger.info("User data: {}", logData);
     * // Result: {"id": 1, "name": "John", "email": "john@mail.com"}
     *
     * // Extract for partial API response
     * Map<String, Object> publicData = BeanTransformer.mapToMap(user, "name", "username", "avatar");
     *
     * // Extract nested properties (if supported by property accessor)
     * Map<String, Object> data = BeanTransformer.mapToMap(person, "name", "address.city", "address.country");
     * }</pre>
     */
    public static Map<String, Object> mapToMap(Object bean, String... properties) {
        Map<String, Object> result = new HashMap<>();
        if (bean == null || properties == null) {
            return result;
        }

        for (String property : properties) {
            try {
                Object value = PropertyAccessor.invokeGetMethod(bean, property);
                result.put(property, value);
            } catch (Exception e) {
                result.put(property, null);
            }
        }
        return result;
    }

    /**
     * Sets bean properties using values from a map.
     * <p>
     * This method takes a map of property names to values and sets them on the bean.
     * It's useful for populating beans from request parameters, configuration maps,
     * or any key-value data source. Supports automatic type conversion via Spring's BeanWrapper.
     * </p>
     *
     * @param bean   the target bean to set properties on
     * @param values a map of property names to values
     *               <p>
     *               Example:
     *               <pre>{@code
     *                                                                                     Person person = new Person();
     *
     *                                                                                     // Setup from map
     *                                                                                     Map<String, Object> values = new HashMap<>();
     *                                                                                     values.put("name", "John Doe");
     *                                                                                     values.put("age", 30);
     *                                                                                     values.put("email", "john@example.com");
     *                                                                                     BeanTransformer.setupBean(person, values);
     *
     *                                                                                     // Setup from request parameters
     *                                                                                     @PostMapping("/users")
     *                                                                                     public void createUser(@RequestBody Map<String, Object> data) {
     *                                                                                         User user = new User();
     *                                                                                         BeanTransformer.setupBean(user, data);
     *                                                                                         userRepository.save(user);
     *                                                                                     }
     *
     *                                                                                     // Supports nested properties (dot notation)
     *                                                                                     Map<String, Object> data = Map.of(
     *                                                                                         "name", "John",
     *                                                                                         "address.city", "New York",
     *                                                                                         "address.zipCode", "10001"
     *                                                                                     );
     *                                                                                     BeanTransformer.setupBean(person, data);
     *                                                                                     }</pre>
     */
    public static void setupBean(final Object bean, final Map<String, Object> values) {
        if (bean == null || values == null || values.isEmpty()) {
            return;
        }

        MutablePropertyValues propertyValues = new MutablePropertyValues();

        values.forEach((key, value) -> {
            Object actualValue = value instanceof ValueWrapper wrapper ? wrapper.getValue() : value;
            propertyValues.addPropertyValue(new PropertyValue(key, actualValue));
        });

        try {
            BeanWrapperImpl beanWrapper = new BeanWrapperImpl(bean);
            beanWrapper.setPropertyValues(propertyValues, true, true);
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.warn("Failed to set properties on bean: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Sets bean properties using another bean's properties.
     * <p>
     * This method copies all compatible properties from the source object to the target bean.
     * Supports both Map sources and bean objects. Uses Spring's {@link BeanUtils#copyProperties}
     * for bean-to-bean copying.
     * </p>
     *
     * @param bean   the target bean to set properties on
     * @param source the source object (can be a Map or another bean)
     *               <p>
     *               Example:
     *               <pre>{@code
     *                                                                                     // Copy from another bean
     *                                                                                     Person person = new Person();
     *                                                                                     PersonDTO dto = getPersonDTO();
     *                                                                                     BeanTransformer.setupBean(person, dto);
     *
     *                                                                                     // Copy from map
     *                                                                                     Person person = new Person();
     *                                                                                     Map<String, Object> data = getPersonData();
     *                                                                                     BeanTransformer.setupBean(person, data);
     *
     *                                                                                     // Update existing entity from DTO
     *                                                                                     Person existingPerson = personRepository.findById(id);
     *                                                                                     PersonUpdateDTO updateData = getUpdateData();
     *                                                                                     BeanTransformer.setupBean(existingPerson, updateData);
     *                                                                                     personRepository.save(existingPerson);
     *                                                                                     }</pre>
     */
    public static void setupBean(Object bean, Object source) {
        if (bean == null || source == null) {
            return;
        }

        if (source instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) source;
            setupBean(bean, map);
        } else {
            BeanUtils.copyProperties(source, bean);
        }
    }

    /**
     * Creates a copy of a bean and applies modifications to it.
     * <p>
     * This method clones the source object and then applies the provided modifier function
     * to the clone. It's a convenient way to create modified copies without affecting the original.
     * Uses shallow cloning, so collections and complex objects are not deeply copied.
     * </p>
     *
     * @param <T>      the bean type
     * @param source   the source object to clone
     * @param modifier the consumer function to modify the copy (can be null for plain copy)
     * @return a modified copy of the source, or null if source is null
     * <p>
     * Example:
     * <pre>{@code
     * Person person = getPerson();
     *
     * // Create a copy with modifications
     * Person activePerson = BeanTransformer.copyWith(person, p -> {
     *     p.setStatus("ACTIVE");
     *     p.setUpdatedAt(new Date());
     * });
     *
     * // Create multiple variants
     * Person draft = BeanTransformer.copyWith(person, p -> p.setStatus("DRAFT"));
     * Person published = BeanTransformer.copyWith(person, p -> p.setStatus("PUBLISHED"));
     *
     * // Use in immutable-style updates
     * public Person updateEmail(Person person, String newEmail) {
     *     return BeanTransformer.copyWith(person, p -> {
     *         p.setEmail(newEmail);
     *         p.setEmailVerified(false);
     *         p.setUpdatedAt(new Date());
     *     });
     * }
     *
     * // Chain transformations
     * Person transformed = BeanTransformer.copyWith(person, p -> {
     *     p.setName(p.getName().toUpperCase());
     *     p.setEmail(p.getEmail().toLowerCase());
     *     p.setActive(true);
     * });
     * }</pre>
     */
    public static <T> T copyWith(T source, Consumer<T> modifier) {
        if (source == null) {
            return null;
        }
        T copy = ObjectCloner.clone(source);
        if (modifier != null) {
            modifier.accept(copy);
        }
        return copy;
    }

    /**
     * Creates a deep copy of a bean and applies modifications to it.
     * <p>
     * Unlike {@link #copyWith(Object, Consumer)}, this method performs a deep clone,
     * including collections and nested objects. The modifier is then applied to the deep copy.
     * </p>
     *
     * @param <T>      the bean type
     * @param source   the source object to deep clone
     * @param modifier the consumer function to modify the copy (can be null for plain deep copy)
     * @return a modified deep copy of the source, or null if source is null
     * <p>
     * Example:
     * <pre>{@code
     * Person person = getPerson();
     *
     * // Deep copy with modifications (collections are also copied)
     * Person clone = BeanTransformer.deepCopyWith(person, p -> {
     *     p.setStatus("ACTIVE");
     *     p.getAddresses().add(new Address("New Address"));
     * });
     *
     * // Original person.addresses is not affected
     * System.out.println(person.getAddresses().size()); // Original size
     * System.out.println(clone.getAddresses().size()); // Original size + 1
     * }</pre>
     */
    public static <T> T deepCopyWith(T source, Consumer<T> modifier) {
        if (source == null) {
            return null;
        }
        T copy = ObjectCloner.deepClone(source);
        if (modifier != null) {
            modifier.accept(copy);
        }
        return copy;
    }

    /**
     * Merges properties from multiple source objects into a target bean.
     * <p>
     * This method copies properties from each source object to the target in sequence.
     * Later sources override properties from earlier sources if they have the same property names.
     * </p>
     *
     * @param target  the target bean to merge properties into
     * @param sources the source objects to merge from (variable arguments)
     *                <p>
     *                Example:
     *                <pre>{@code
     *                                                                                           Person person = new Person();
     *
     *                                                                                           // Merge from multiple sources
     *                                                                                           PersonBasicDTO basic = getBasicInfo();
     *                                                                                           PersonContactDTO contact = getContactInfo();
     *                                                                                           PersonPreferencesDTO prefs = getPreferences();
     *
     *                                                                                           BeanTransformer.merge(person, basic, contact, prefs);
     *                                                                                           // person now has properties from all three DTOs
     *
     *                                                                                           // Build object from multiple partial sources
     *                                                                                           User user = new User();
     *                                                                                           BeanTransformer.merge(user,
     *                                                                                               userProfile,
     *                                                                                               userSettings,
     *                                                                                               userPermissions
     *                                                                                           );
     *                                                                                           }</pre>
     */
    public static void merge(Object target, Object... sources) {
        if (target == null || sources == null) {
            return;
        }

        for (Object source : sources) {
            if (source != null) {
                setupBean(target, source);
            }
        }
    }

    /**
     * Transforms a source object to target class, excluding specific properties.
     * <p>
     * Similar to {@link #transform(Object, Class)} but allows excluding certain properties
     * from being copied. Useful when you want to transform most properties but skip some.
     * </p>
     *
     * @param <S>                the source type
     * @param <T>                the target type
     * @param source             the source object
     * @param targetClass        the target class
     * @param excludedProperties property names to exclude from copying
     * @return a new instance of target class with copied properties (except excluded ones)
     * <p>
     * Example:
     * <pre>{@code
     * Person person = getPerson();
     *
     * // Transform but exclude sensitive data
     * PersonDTO dto = BeanTransformer.transformExcluding(person, PersonDTO.class,
     *     "password", "securityToken", "internalNotes");
     *
     * // Transform excluding audit fields
     * Product product = getProduct();
     * ProductDTO dto = BeanTransformer.transformExcluding(product, ProductDTO.class,
     *     "createdAt", "updatedAt", "createdBy", "updatedBy");
     * }</pre>
     */
    public static <S, T> T transformExcluding(S source, Class<T> targetClass, String... excludedProperties) {
        if (source == null || targetClass == null) {
            return null;
        }
        T target = ObjectCloner.newInstance(targetClass);
        if (excludedProperties != null && excludedProperties.length > 0) {
            BeanUtils.copyProperties(source, target, excludedProperties);
        } else {
            BeanUtils.copyProperties(source, target);
        }
        return target;
    }

    /**
     * Creates a Map representation of all bean properties.
     * <p>
     * Unlike {@link #mapToMap(Object, String...)}, this method extracts ALL readable properties
     * from the bean into a map. Useful for debugging, logging, or creating dynamic responses.
     * </p>
     *
     * @param bean the source bean
     * @return a map with all property names and values, or empty map if bean is null
     * <p>
     * Example:
     * <pre>{@code
     * Person person = getPerson();
     *
     * // Get all properties
     * Map<String, Object> allData = BeanTransformer.toMap(person);
     * // Result: {"id": 1, "name": "John", "email": "john@mail.com", "age": 30, ...}
     *
     * // Use for debugging
     * logger.debug("Person data: {}", BeanTransformer.toMap(person));
     *
     * // Create dynamic JSON response
     * @GetMapping("/user/{id}")
     * public Map<String, Object> getUser(@PathVariable Long id) {
     *     User user = userService.findById(id);
     *     return BeanTransformer.toMap(user);
     * }
     * }</pre>
     */
    public static Map<String, Object> toMap(Object bean) {
        Map<String, Object> result = new HashMap<>();
        if (bean == null) {
            return result;
        }

        List<tools.dynamia.commons.reflect.PropertyInfo> properties =
                tools.dynamia.commons.ObjectOperations.getPropertiesInfo(bean.getClass());

        for (tools.dynamia.commons.reflect.PropertyInfo property : properties) {
            try {
                Object value = property.getValue(bean);
                result.put(property.getName(), value);
            } catch (Exception e) {
                // Skip properties that cannot be read
            }
        }
        return result;
    }
}
