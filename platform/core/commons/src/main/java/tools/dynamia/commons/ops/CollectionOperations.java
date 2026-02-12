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
import java.util.stream.Collectors;

/**
 * Specialized class for collection operations on Java beans.
 * <p>
 * This class provides powerful functional-style operations for working with collections of objects,
 * including mapping, filtering, grouping, counting, and aggregating based on bean properties.
 * All operations use property names with support for nested properties via dot notation.
 * </p>
 *
 * <h2>Core Features</h2>
 * <ul>
 *   <li><strong>Property Mapping:</strong> Extract property values from collections</li>
 *   <li><strong>Filtering:</strong> Filter collections by property values or predicates</li>
 *   <li><strong>Searching:</strong> Find objects by property criteria</li>
 *   <li><strong>Grouping:</strong> Group objects by property values</li>
 *   <li><strong>Counting:</strong> Count occurrences by property values</li>
 *   <li><strong>Aggregation:</strong> Sum numeric properties</li>
 *   <li><strong>Sorting:</strong> Create comparators for property-based sorting</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 * <pre>{@code
 * List<Person> persons = getPersons();
 *
 * // Extract properties
 * List<String> names = CollectionOperations.mapProperty(persons, "name");
 *
 * // Filter
 * List<Person> adults = CollectionOperations.filterByProperty(persons, "age",
 *     age -> ((Number)age).intValue() >= 18);
 *
 * // Find
 * Optional<Person> john = CollectionOperations.findByProperty(persons, "email", "john@example.com");
 *
 * // Group
 * Map<Object, List<Person>> byCountry = CollectionOperations.groupBy(persons, "country");
 *
 * // Count
 * Map<Object, Long> countByStatus = CollectionOperations.countByProperty(persons, "status");
 *
 * // Sum
 * Number totalAge = CollectionOperations.sumProperty(persons, "age");
 *
 * // Sort
 * persons.sort(CollectionOperations.getComparator("lastName"));
 * }</pre>
 *
 * @author Ing. Mario Serrano Leones
 * @version 26.1
 * @since 26.1
 */
public final class CollectionOperations {

    /**
     * Private constructor to prevent instantiation.
     */
    private CollectionOperations() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Extracts property values from a collection into a list.
     * <p>
     * This method maps each object in the collection to its property value, creating a new list.
     * Supports nested properties using dot notation (e.g., "address.city").
     * </p>
     *
     * @param <T>          the type of objects in the collection
     * @param <R>          the type of the property values
     * @param collection   the collection to map
     * @param propertyName the property name to extract (supports dot notation)
     * @return a list of property values
     *
     * Example:
     * <pre>{@code
     * List<Person> persons = getPersons();
     * List<String> names = CollectionOperations.mapProperty(persons, "name");
     * List<String> cities = CollectionOperations.mapProperty(persons, "address.city");
     * }</pre>
     */
    public static <T, R> List<R> mapProperty(Collection<T> collection, String propertyName) {
        if (collection == null || propertyName == null) {
            return new ArrayList<>();
        }
        return collection.stream()
                .map(obj -> {
                    try {
                        return (R) PropertyAccessor.invokeGetMethod(obj, propertyName);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Finds the first object in a collection where the property matches the given value.
     * <p>
     * This method searches through the collection and returns the first object whose
     * property value equals the specified value. Supports nested properties.
     * </p>
     *
     * @param <T>          the type of objects in the collection
     * @param collection   the collection to search
     * @param propertyName the property name to match (supports dot notation)
     * @param value        the value to match
     * @return an Optional containing the first matching object, or empty if not found
     *
     * Example:
     * <pre>{@code
     * Optional<Person> person = CollectionOperations.findByProperty(persons, "email", "john@mail.com");
     * if (person.isPresent()) {
     *     System.out.println("Found: " + person.get().getName());
     * }
     * }</pre>
     */
    public static <T> Optional<T> findByProperty(Collection<T> collection, String propertyName, Object value) {
        if (collection == null || propertyName == null) {
            return Optional.empty();
        }
        return collection.stream()
                .filter(obj -> {
                    try {
                        Object propValue = PropertyAccessor.invokeGetMethod(obj, propertyName);
                        return value == null ? propValue == null : value.equals(propValue);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .findFirst();
    }

    /**
     * Filters a collection by a property value using a custom predicate.
     * <p>
     * This method allows complex filtering by testing property values against a custom predicate.
     * The predicate receives the property value and returns true to include the object.
     * </p>
     *
     * @param <T>          the type of objects in the collection
     * @param collection   the collection to filter
     * @param propertyName the property name to test (supports dot notation)
     * @param predicate    the predicate to test property values
     * @return a filtered list containing only objects where the predicate returns true
     *
     * Example:
     * <pre>{@code
     * // Find all persons older than 18
     * List<Person> adults = CollectionOperations.filterByProperty(persons, "age",
     *     age -> age != null && ((Number)age).intValue() >= 18);
     *
     * // Find all persons with email containing "gmail"
     * List<Person> gmailUsers = CollectionOperations.filterByProperty(persons, "email",
     *     email -> email != null && email.toString().contains("gmail"));
     * }</pre>
     */
    public static <T> List<T> filterByProperty(Collection<T> collection, String propertyName,
                                               Predicate<Object> predicate) {
        if (collection == null || propertyName == null || predicate == null) {
            return new ArrayList<>();
        }
        return collection.stream()
                .filter(obj -> {
                    try {
                        Object propValue = PropertyAccessor.invokeGetMethod(obj, propertyName);
                        return predicate.test(propValue);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Filters a collection by matching a property value exactly.
     * <p>
     * This is a convenience method for filtering by exact value match.
     * For more complex filtering, use {@link #filterByProperty(Collection, String, Predicate)}.
     * </p>
     *
     * @param <T>          the type of objects in the collection
     * @param collection   the collection to filter
     * @param propertyName the property name to match (supports dot notation)
     * @param value        the value to match (uses equals() for comparison)
     * @return a filtered list containing only objects with matching property value
     *
     * Example:
     * <pre>{@code
     * List<Person> activeUsers = CollectionOperations.filterByProperty(persons, "status", "ACTIVE");
     * List<Person> fromUSA = CollectionOperations.filterByProperty(persons, "address.country", "USA");
     * }</pre>
     */
    public static <T> List<T> filterByProperty(Collection<T> collection, String propertyName, Object value) {
        return filterByProperty(collection, propertyName,
                propValue -> value == null ? propValue == null : value.equals(propValue));
    }

    /**
     * Groups objects in a collection by a property value.
     * <p>
     * This method creates a map where keys are the distinct property values and
     * values are lists of objects that have that property value.
     * Supports nested properties via dot notation.
     * </p>
     *
     * @param <T>          the type of objects in the collection
     * @param collection   the collection to group
     * @param propertyName the property name to group by (supports dot notation)
     * @return a map with property values as keys and lists of objects as values
     *
     * Example:
     * <pre>{@code
     * List<Person> persons = getPersons();
     *
     * // Group by country
     * Map<Object, List<Person>> byCountry = CollectionOperations.groupBy(persons, "country");
     * // Result: {"USA": [person1, person2], "Canada": [person3], ...}
     *
     * // Group by nested property
     * Map<Object, List<Person>> byCity = CollectionOperations.groupBy(persons, "address.city");
     * }</pre>
     */
    public static <T> Map<Object, List<T>> groupBy(Collection<T> collection, String propertyName) {
        if (collection == null || propertyName == null) {
            return new HashMap<>();
        }
        return collection.stream()
                .collect(Collectors.groupingBy(obj -> {
                    try {
                        Object value = PropertyAccessor.invokeGetMethod(obj, propertyName);
                        return value != null ? value : "null";
                    } catch (Exception e) {
                        return "error";
                    }
                }));
    }

    /**
     * Counts objects grouped by a property value.
     * <p>
     * This method creates a map where keys are the distinct property values and
     * values are the count of objects having that property value.
     * Useful for creating statistics and reports.
     * </p>
     *
     * @param <T>          the type of objects in the collection
     * @param collection   the collection to count
     * @param propertyName the property name to group by (supports dot notation)
     * @return a map with property values as keys and counts as values
     *
     * Example:
     * <pre>{@code
     * Map<Object, Long> countByStatus = CollectionOperations.countByProperty(persons, "status");
     * // Result: {"ACTIVE": 10, "INACTIVE": 5, "PENDING": 2}
     *
     * Map<Object, Long> countByCountry = CollectionOperations.countByProperty(persons, "address.country");
     * // Result: {"USA": 50, "Canada": 30, "Mexico": 20}
     * }</pre>
     */
    public static <T> Map<Object, Long> countByProperty(Collection<T> collection, String propertyName) {
        if (collection == null || propertyName == null) {
            return new HashMap<>();
        }
        return collection.stream()
                .collect(Collectors.groupingBy(obj -> {
                    try {
                        Object value = PropertyAccessor.invokeGetMethod(obj, propertyName);
                        return value != null ? value : "null";
                    } catch (Exception e) {
                        return "error";
                    }
                }, Collectors.counting()));
    }

    /**
     * Sums numeric property values from a collection.
     * <p>
     * This method extracts numeric values from the specified property and calculates their sum.
     * Non-numeric values are ignored. Returns 0 if the collection is empty or no numeric values found.
     * </p>
     *
     * @param <T>          the type of objects in the collection
     * @param collection   the collection to sum
     * @param propertyName the numeric property name (supports dot notation)
     * @return the sum as a Number (Double)
     *
     * Example:
     * <pre>{@code
     * List<Order> orders = getOrders();
     *
     * // Sum total prices
     * Number totalAmount = CollectionOperations.sumProperty(orders, "price");
     * System.out.println("Total: $" + totalAmount);
     *
     * // Sum nested property
     * Number totalTax = CollectionOperations.sumProperty(orders, "invoice.taxAmount");
     * }</pre>
     */
    public static <T> Number sumProperty(Collection<T> collection, String propertyName) {
        if (collection == null || propertyName == null) {
            return 0;
        }
        return collection.stream()
                .mapToDouble(obj -> {
                    try {
                        Object value = PropertyAccessor.invokeGetMethod(obj, propertyName);
                        if (value instanceof Number) {
                            return ((Number) value).doubleValue();
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                    return 0.0;
                })
                .sum();
    }

    /**
     * Compares two objects by a specific property value.
     * <p>
     * This method extracts the property value from both objects and compares them.
     * The property values must implement {@link Comparable} for proper comparison.
     * Handles null values gracefully (nulls are considered less than non-nulls).
     * </p>
     *
     * @param obj1         the first object to compare
     * @param obj2         the second object to compare
     * @param propertyName the property name to compare by (supports dot notation)
     * @return negative if obj1 < obj2, zero if equal, positive if obj1 > obj2
     *
     * Example:
     * <pre>{@code
     * List<Person> persons = getPersons();
     * persons.sort((p1, p2) -> CollectionOperations.compareByProperty(p1, p2, "age"));
     * }</pre>
     */
    @SuppressWarnings("unchecked")
    public static int compareByProperty(Object obj1, Object obj2, String propertyName) {
        if (obj1 == null && obj2 == null) return 0;
        if (obj1 == null) return -1;
        if (obj2 == null) return 1;

        try {
            Object value1 = PropertyAccessor.invokeGetMethod(obj1, propertyName);
            Object value2 = PropertyAccessor.invokeGetMethod(obj2, propertyName);

            if (value1 == null && value2 == null) return 0;
            if (value1 == null) return -1;
            if (value2 == null) return 1;

            if (value1 instanceof Comparable) {
                return ((Comparable<Object>) value1).compareTo(value2);
            }
        } catch (Exception e) {
            // ignore
        }
        return 0;
    }

    /**
     * Creates a Comparator for sorting objects by a property in ascending order.
     * <p>
     * This method returns a Comparator that can be used with Collections.sort() or Stream.sorted()
     * to sort objects based on a specific property value.
     * </p>
     *
     * @param <T>          the type of objects to compare
     * @param propertyName the property name to sort by (supports dot notation)
     * @return a Comparator for the specified property in ascending order
     *
     * Example:
     * <pre>{@code
     * List<Person> persons = getPersons();
     *
     * // Sort by name (ascending)
     * persons.sort(CollectionOperations.getComparator("name"));
     *
     * // Sort by nested property
     * persons.sort(CollectionOperations.getComparator("address.city"));
     *
     * // Use with streams
     * List<Person> sorted = persons.stream()
     *     .sorted(CollectionOperations.getComparator("age"))
     *     .collect(Collectors.toList());
     * }</pre>
     */
    public static <T> Comparator<T> getComparator(String propertyName) {
        return (obj1, obj2) -> compareByProperty(obj1, obj2, propertyName);
    }

    /**
     * Creates a Comparator for sorting objects by a property in descending order.
     * <p>
     * This is the reverse of {@link #getComparator(String)}, sorting from highest to lowest.
     * </p>
     *
     * @param <T>          the type of objects to compare
     * @param propertyName the property name to sort by (supports dot notation)
     * @return a Comparator for the specified property in descending order
     *
     * Example:
     * <pre>{@code
     * List<Person> persons = getPersons();
     *
     * // Sort by age (descending, oldest first)
     * persons.sort(CollectionOperations.getComparatorDesc("age"));
     *
     * // Sort by salary (descending, highest first)
     * persons.sort(CollectionOperations.getComparatorDesc("salary"));
     * }</pre>
     */
    public static <T> Comparator<T> getComparatorDesc(String propertyName) {
        return (obj1, obj2) -> compareByProperty(obj2, obj1, propertyName);
    }

    /**
     * Checks if all objects in a collection match a property predicate.
     * <p>
     * This method returns true only if all objects in the collection satisfy the predicate
     * for the specified property. Returns false if any object fails the test.
     * </p>
     *
     * @param <T>          the type of objects in the collection
     * @param collection   the collection to test
     * @param propertyName the property name to test (supports dot notation)
     * @param predicate    the predicate to test property values
     * @return true if all objects match the predicate, false otherwise
     *
     * Example:
     * <pre>{@code
     * boolean allAdults = CollectionOperations.allMatch(persons, "age",
     *     age -> age != null && ((Number)age).intValue() >= 18);
     * }</pre>
     */
    public static <T> boolean allMatch(Collection<T> collection, String propertyName, Predicate<Object> predicate) {
        if (collection == null || propertyName == null || predicate == null) {
            return false;
        }
        return collection.stream().allMatch(obj -> {
            try {
                Object propValue = PropertyAccessor.invokeGetMethod(obj, propertyName);
                return predicate.test(propValue);
            } catch (Exception e) {
                return false;
            }
        });
    }

    /**
     * Checks if any object in a collection matches a property predicate.
     * <p>
     * This method returns true if at least one object in the collection satisfies the predicate
     * for the specified property. Returns false if no objects match.
     * </p>
     *
     * @param <T>          the type of objects in the collection
     * @param collection   the collection to test
     * @param propertyName the property name to test (supports dot notation)
     * @param predicate    the predicate to test property values
     * @return true if any object matches the predicate, false otherwise
     *
     * Example:
     * <pre>{@code
     * boolean hasMinors = CollectionOperations.anyMatch(persons, "age",
     *     age -> age != null && ((Number)age).intValue() < 18);
     * }</pre>
     */
    public static <T> boolean anyMatch(Collection<T> collection, String propertyName, Predicate<Object> predicate) {
        if (collection == null || propertyName == null || predicate == null) {
            return false;
        }
        return collection.stream().anyMatch(obj -> {
            try {
                Object propValue = PropertyAccessor.invokeGetMethod(obj, propertyName);
                return predicate.test(propValue);
            } catch (Exception e) {
                return false;
            }
        });
    }

    /**
     * Checks if no objects in a collection match a property predicate.
     * <p>
     * This method returns true only if no objects in the collection satisfy the predicate
     * for the specified property. Returns false if any object matches.
     * </p>
     *
     * @param <T>          the type of objects in the collection
     * @param collection   the collection to test
     * @param propertyName the property name to test (supports dot notation)
     * @param predicate    the predicate to test property values
     * @return true if no objects match the predicate, false otherwise
     *
     * Example:
     * <pre>{@code
     * boolean noInactiveUsers = CollectionOperations.noneMatch(persons, "status",
     *     status -> "INACTIVE".equals(status));
     * }</pre>
     */
    public static <T> boolean noneMatch(Collection<T> collection, String propertyName, Predicate<Object> predicate) {
        if (collection == null || propertyName == null || predicate == null) {
            return true;
        }
        return collection.stream().noneMatch(obj -> {
            try {
                Object propValue = PropertyAccessor.invokeGetMethod(obj, propertyName);
                return predicate.test(propValue);
            } catch (Exception e) {
                return false;
            }
        });
    }

    /**
     * Gets distinct values of a property from a collection.
     * <p>
     * This method extracts all unique values of the specified property from the collection,
     * removing duplicates. Useful for getting distinct categories, statuses, etc.
     * </p>
     *
     * @param <T>          the type of objects in the collection
     * @param <R>          the type of the property values
     * @param collection   the collection to process
     * @param propertyName the property name to extract (supports dot notation)
     * @return a list of distinct property values
     *
     * Example:
     * <pre>{@code
     * List<String> countries = CollectionOperations.distinctProperty(persons, "country");
     * // Result: ["USA", "Canada", "Mexico"] (no duplicates)
     *
     * List<String> statuses = CollectionOperations.distinctProperty(orders, "status");
     * // Result: ["PENDING", "COMPLETED", "CANCELLED"]
     * }</pre>
     */
    public static <T, R> List<R> distinctProperty(Collection<T> collection, String propertyName) {
        if (collection == null || propertyName == null) {
            return new ArrayList<>();
        }
        return collection.stream()
                .map(obj -> {
                    try {
                        return (R) PropertyAccessor.invokeGetMethod(obj, propertyName);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .distinct()
                .collect(Collectors.toList());
    }
}
