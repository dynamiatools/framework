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
package tools.dynamia.domain.util;

import tools.dynamia.commons.BeanSorter;
import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.commons.Identifiable;
import tools.dynamia.domain.CurrencyExchangeProvider;
import tools.dynamia.domain.EntityReferenceRepository;
import tools.dynamia.domain.EntityUtilsProvider;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.query.Parameter;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.services.GraphCrudService;
import tools.dynamia.domain.services.ValidatorService;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.NotImplementationFoundException;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Objects;

/**
 * Utility class providing common domain operations and helper methods for the Dynamia Tools framework.
 * This class includes utilities for string manipulation, numeric operations, entity reference handling,
 * service lookups, and data transfer object transformations.
 *
 * <p>All methods are static and the class cannot be instantiated.</p>
 *
 * @author Mario Serrano Leones
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class DomainUtils {

    /**
     * Decimal formatter instance used for rounding operations.
     */
    private static DecimalFormat formatter;

    /**
     * Cleans a string by removing special characters and optionally keeping only numbers.
     * When onlyNumbers is false, keeps letters, digits, and whitespace. When true, keeps only digits and hyphens.
     * The result is always converted to uppercase.
     *
     * @param original    the original string to clean
     * @param onlyNumbers if true, keeps only digits and hyphens; if false, keeps letters, digits, and whitespace
     * @return the cleaned string in uppercase
     *
     * @see #cleanString(String)
     *
     * Example:
     * <pre>{@code
     * String result = cleanString("Hello-123!", false); // Returns "HELLO 123"
     * String result2 = cleanString("Hello-123!", true); // Returns "-123"
     * }</pre>
     */
    public static String cleanString(String original, boolean onlyNumbers) {
        StringBuilder newStr = new StringBuilder();
        for (int i = 0; i < original.length(); i++) {
            char c = original.charAt(i);
            if (onlyNumbers) {
                if (Character.isDigit(c) || c == '-') {
                    newStr.append(c);
                }
            } else if (Character.isLetterOrDigit(c) || Character.isWhitespace(c)) {
                newStr.append(c);
            }
        }

        return newStr.toString().toUpperCase();
    }

    /**
     * Cleans a string by removing special characters while keeping letters, digits, and whitespace.
     * The result is converted to uppercase. This is a convenience method that calls
     * {@link #cleanString(String, boolean)} with onlyNumbers set to false.
     *
     * @param original the original string to clean
     * @return the cleaned string in uppercase, containing only letters, digits, and whitespace
     *
     * @see #cleanString(String, boolean)
     *
     * Example:
     * <pre>{@code
     * String result = cleanString("Hello, World! 123"); // Returns "HELLO WORLD 123"
     * }</pre>
     */
    public static String cleanString(String original) {
        return cleanString(original, false);
    }

    /**
     * Builds a searchable string pattern suitable for SQL LIKE queries.
     * Surrounds alphanumeric characters with wildcards (%) and replaces special characters
     * and whitespace with wildcards. Asterisks (*) in the input are also converted to wildcards.
     *
     * @param src the source object to convert into a searchable pattern
     * @return a string pattern with wildcards, suitable for SQL LIKE queries; returns "%" if src is null
     *
     * Example:
     * <pre>{@code
     * String pattern = buildSearcheableString("John Doe"); // Returns "%J%o%h%n%D%o%e%"
     * String pattern2 = buildSearcheableString("user*123"); // Returns "%u%s%e%r%1%2%3%"
     * String pattern3 = buildSearcheableString(null);      // Returns "%"
     * }</pre>
     */
    public static String buildSearcheableString(Object src) {
        if (src == null) {
            return "%";
        }
        char comodin = '%';
        String string = src.toString();
        string = string.replace('*', comodin);
        StringBuilder sb = new StringBuilder();
        sb.append(comodin);
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (Character.isDigit(c) || Character.isLetter(c)) {
                sb.append(c);
            } else {
                sb.append(comodin);
            }
        }
        sb.append(comodin);
        return sb.toString();
    }


    /**
     * Appends an ORDER BY clause to a query string based on the provided {@link BeanSorter}.
     * If the sorter is null or has no column name, returns the original query unchanged.
     * Handles cases where the column name already includes ASC or DESC.
     *
     * @param queryText the original query string
     * @param sorter    the BeanSorter containing column name and sort direction
     * @return the query string with ORDER BY clause appended, or the original query if sorter is null
     *
     * Example:
     * <pre>{@code
     * BeanSorter sorter = new BeanSorter("name", true);
     * String query = configureSorter("SELECT * FROM users", sorter);
     * // Returns "SELECT * FROM users order by name asc "
     * }</pre>
     */
    public static String configureSorter(String queryText, BeanSorter sorter) {
        if (sorter != null && sorter.getColumnName() != null && !sorter.getColumnName().isEmpty()) {

            String direction = sorter.isAscending() ? " asc " : " desc ";
            if (sorter.getColumnName().toLowerCase().endsWith(" desc")
                    || sorter.getColumnName().toLowerCase().endsWith(" asc")) {
                direction = " ";
            }

            return queryText + " order by " + sorter.getColumnName() + direction;
        } else {
            return queryText;
        }
    }

    /**
     * Performs a simple rounding operation on a BigDecimal number to the nearest power of 10.
     * The zeros parameter determines the rounding magnitude (10, 100, 1000, etc.).
     * Numbers are rounded up if the decimal part is greater than 0.5.
     *
     * @param n     the BigDecimal number to round
     * @param zeros the number of zeros in the rounding pattern (e.g., 2 for hundreds, 3 for thousands)
     * @return the rounded BigDecimal value
     *
     * Example:
     * <pre>{@code
     * BigDecimal result = simpleRound(new BigDecimal("1567"), 2); // Returns 1600 (rounds to nearest 100)
     * BigDecimal result2 = simpleRound(new BigDecimal("1234"), 3); // Returns 1000 (rounds to nearest 1000)
     * }</pre>
     */
    public static BigDecimal simpleRound(BigDecimal n, int zeros) {
        String patternBuilder = "1" +
                "0".repeat(Math.max(0, zeros));

        int pattern = Integer.parseInt(patternBuilder);

        long value = n.longValue();
        double dec = (double) value / Double.parseDouble(pattern + ".0");
        value = value / pattern;
        dec = dec - value;
        value *= pattern;
        if (dec > 0.5) {
            value += pattern;
        }
        return new BigDecimal(value);
    }

    /**
     * Performs a simple rounding operation on a BigDecimal number, removing all decimal places.
     * Uses a DecimalFormat to truncate the fractional part. Returns null if the input is null.
     *
     * @param n the BigDecimal number to round; can be null
     * @return the rounded BigDecimal with no decimal places, or null if input is null
     *
     * Example:
     * <pre>{@code
     * BigDecimal result = simpleRound(new BigDecimal("1234.56")); // Returns 1234
     * BigDecimal result2 = simpleRound(null);                     // Returns null
     * }</pre>
     */
    public static BigDecimal simpleRound(BigDecimal n) {
        if (n == null) {
            return null;
        }

        if (formatter == null) {
            formatter = new DecimalFormat("###,###");
        }
        String val = formatter.format(n);

        val = val.replace(".", "").replace(",", "").trim();

        return new BigDecimal(val);
    }

    /**
     * Formats a number by padding it with leading zeros based on a reference number.
     * The number of zeros is determined by the difference in length between the reference and the number.
     *
     * @param number          the number to format
     * @param numberReference the reference number that determines the total length
     * @return the formatted string with leading zeros
     *
     * Example:
     * <pre>{@code
     * String result = formatNumberWithZeroes(5, 1000);  // Returns "0005"
     * String result2 = formatNumberWithZeroes(89, 1000); // Returns "0089"
     * String result3 = formatNumberWithZeroes(123, 1000); // Returns "0123"
     * }</pre>
     */
    public static String formatNumberWithZeroes(long number, long numberReference) {
        int numCeros = Long.toString(numberReference).length() - Long.toString(number).length();
        return "0".repeat(Math.max(0, numCeros)) + number;
    }

    /**
     * Calculates the sum of multiple BigDecimal values.
     * All values are added sequentially starting from BigDecimal.ZERO.
     *
     * @param values the BigDecimal values to sum
     * @return the sum of all values; returns BigDecimal.ZERO if no values are provided
     *
     * Example:
     * <pre>{@code
     * BigDecimal result = sum(new BigDecimal("10"), new BigDecimal("20"), new BigDecimal("30"));
     * // Returns 60
     * }</pre>
     */
    public static BigDecimal sum(BigDecimal... values) {
        BigDecimal result = BigDecimal.ZERO;
        for (BigDecimal v : values) {
            result = result.add(v);
        }
        return result;
    }

    /**
     * Calculates the result of subtracting multiple BigDecimal values sequentially.
     * The first value becomes the starting point, and subsequent values are subtracted from it.
     *
     * @param values the BigDecimal values to subtract; the first value is the minuend
     * @return the result after subtracting all values sequentially, or null if no values provided
     *
     * Example:
     * <pre>{@code
     * BigDecimal result = substract(new BigDecimal("100"), new BigDecimal("30"), new BigDecimal("20"));
     * // Returns 50 (100 - 30 - 20)
     * }</pre>
     */
    public static BigDecimal substract(BigDecimal... values) {
        BigDecimal result = null;
        for (BigDecimal v : values) {
            if (result == null) {
                result = v;
            } else {
                result = result.subtract(v);
            }
        }
        return result;
    }

    /**
     * Calculates the sum of a specific BigDecimal field across a collection of objects.
     * Uses reflection to access the field's getter method and accumulate the values.
     *
     * @param data      the collection of objects to sum
     * @param clazz     the class of the objects in the collection
     * @param fieldName the name of the field to sum (must be a BigDecimal type)
     * @return the sum of all field values across the collection
     * @throws ValidationError if the field cannot be accessed or summed
     *
     * Example:
     * <pre>{@code
     * List<Product> products = Arrays.asList(product1, product2, product3);
     * BigDecimal totalPrice = sumField(products, Product.class, "price");
     * }</pre>
     */
    public static BigDecimal sumField(Collection data, Class clazz, String fieldName) {
        BigDecimal result = BigDecimal.ZERO;
        try {
            Method getField = clazz.getMethod(ObjectOperations.formatGetMethod(fieldName));
            for (Object obj : data) {
                BigDecimal num = (BigDecimal) getField.invoke(obj);
                result = result.add(num);
            }
        } catch (Exception e) {
            throw new ValidationError("Error computing sum for field " + fieldName + ". Class: " + clazz);
        }
        return result;
    }


    /**
     * Retrieves the EntityReferenceRepository for the specified entity class.
     * Searches for a repository matching the entity class name.
     *
     * @param entityClass the entity class to find the repository for
     * @return the EntityReferenceRepository for the entity class, or null if not found
     *
     * @see #getEntityReferenceRepository(String)
     *
     * Example:
     * <pre>{@code
     * EntityReferenceRepository repo = getEntityReferenceRepository(Customer.class);
     * }</pre>
     */
    public static EntityReferenceRepository getEntityReferenceRepository(Class entityClass) {
        return getEntityReferenceRepository(entityClass.getName());
    }

    /**
     * Retrieves the EntityReferenceRepository for the specified entity class name.
     * Searches all registered repositories for one matching the given class name.
     *
     * @param className the fully qualified class name of the entity
     * @return the EntityReferenceRepository for the entity, or null if not found or className is null
     *
     * Example:
     * <pre>{@code
     * EntityReferenceRepository repo = getEntityReferenceRepository("com.example.Customer");
     * }</pre>
     */
    public static EntityReferenceRepository getEntityReferenceRepository(String className) {
        if (className == null) {
            return null;
        }

        return Containers.get().findObjects(EntityReferenceRepository.class)
                .stream().filter(r -> r.getEntityClassName().equals(className))
                .findFirst()
                .orElse(null);


    }

    /**
     * Retrieves the EntityReferenceRepository for the specified alias.
     * Searches all registered repositories for one matching the given alias.
     *
     * @param alias the alias to search for
     * @return the EntityReferenceRepository matching the alias, or null if not found or alias is null
     *
     * Example:
     * <pre>{@code
     * EntityReferenceRepository repo = getEntityReferenceRepositoryByAlias("customer");
     * }</pre>
     */
    public static EntityReferenceRepository getEntityReferenceRepositoryByAlias(String alias) {
        if (alias == null) {
            return null;
        }

        return Containers.get().findObjects(EntityReferenceRepository.class)
                .stream().filter(r -> r.getAlias().equals(alias))
                .findFirst()
                .orElse(null);
    }


    /**
     * Returns BigDecimal.ZERO if the provided number is null, otherwise returns the number unchanged.
     * This is a convenience method to safely handle null BigDecimal values.
     *
     * @param number the BigDecimal number to check
     * @return the original number if not null, or BigDecimal.ZERO if null
     *
     * Example:
     * <pre>{@code
     * BigDecimal result = getZeroIfNull(null);              // Returns BigDecimal.ZERO
     * BigDecimal result2 = getZeroIfNull(new BigDecimal("10")); // Returns 10
     * }</pre>
     */
    public static BigDecimal getZeroIfNull(BigDecimal number) {
        return Objects.requireNonNullElse(number, BigDecimal.ZERO);
    }


    /**
     * Finds the first implementation of CrudService in the application context.
     * This is a convenience method for quick access to CRUD operations on entities.
     *
     * @return the CrudService implementation
     * @throws NotImplementationFoundException if no CrudService implementation is found
     *
     * @see #lookupCrudService(Class)
     *
     * Example:
     * <pre>{@code
     * CrudService service = lookupCrudService();
     * List<Customer> customers = service.find(Customer.class);
     * }</pre>
     */
    public static CrudService lookupCrudService() {
        CrudService crudService = Containers.get().findObject(CrudService.class);
        if (crudService == null) {
            throw new NotImplementationFoundException("Cannot found a " + CrudService.class + " implementation");
        }
        return crudService;
    }

    /**
     * Finds a specific implementation of CrudService in the application context.
     * This method allows looking up specialized CrudService implementations.
     *
     * @param <T>       the type of CrudService to find
     * @param crudClass the class of the CrudService implementation to find
     * @return the CrudService implementation of the specified type
     * @throws NotImplementationFoundException if no implementation of the specified type is found
     *
     * @see #lookupCrudService()
     *
     * Example:
     * <pre>{@code
     * GraphCrudService graphService = lookupCrudService(GraphCrudService.class);
     * }</pre>
     */
    public static <T extends CrudService> T lookupCrudService(Class<T> crudClass) {
        T crudService = Containers.get().findObject(crudClass);
        if (crudService == null) {
            throw new NotImplementationFoundException("Cannot found a " + crudClass + " implementation");
        }
        return crudService;
    }

    /**
     * Finds the first implementation of GraphCrudService in the application context.
     * This is a convenience method for accessing graph-based CRUD operations.
     *
     * @return the GraphCrudService implementation
     * @throws NotImplementationFoundException if no GraphCrudService implementation is found
     *
     * @see #lookupCrudService(Class)
     *
     * Example:
     * <pre>{@code
     * GraphCrudService service = lookupGraphCrudService();
     * }</pre>
     */
    public static GraphCrudService lookupGraphCrudService() {
        return lookupCrudService(GraphCrudService.class);
    }


    private DomainUtils() {
    }

    /**
     * Finds and returns the identifier (ID) of an entity.
     * First checks if the entity implements {@link Identifiable}, otherwise uses registered
     * {@link EntityUtilsProvider} implementations to locate the ID.
     *
     * @param <E>    the entity type
     * @param entity the entity object to extract the ID from
     * @return the entity's ID as a Serializable, or null if entity is null or ID cannot be found
     *
     * Example:
     * <pre>{@code
     * Customer customer = new Customer();
     * customer.setId(123L);
     * Serializable id = findEntityId(customer); // Returns 123L
     * }</pre>
     */
    public static <E> Serializable findEntityId(E entity) {
        if (entity == null) {
            return null;
        }

        if (entity instanceof Identifiable) {
            return ((Identifiable) entity).getId();
        } else {
            for (EntityUtilsProvider finder : Containers.get().findObjects(EntityUtilsProvider.class)) {
                Serializable id = finder.findId(entity);
                if (id != null) {
                    return id;
                }
            }
        }
        return null;
    }

    /**
     * Determines whether the provided object is a domain entity.
     * Uses the registered {@link EntityUtilsProvider} to check entity status.
     *
     * @param entity the object to check
     * @return true if the object is an entity, false otherwise
     *
     * @see #isEntity(Class)
     *
     * Example:
     * <pre>{@code
     * Customer customer = new Customer();
     * boolean result = isEntity(customer); // Returns true if Customer is a JPA entity
     * }</pre>
     */
    public static boolean isEntity(Object entity) {
        EntityUtilsProvider entityUtilsProvider = Containers.get().findObject(EntityUtilsProvider.class);
        if (entityUtilsProvider != null) {
            return entityUtilsProvider.isEntity(entity);
        }
        return false;
    }

    /**
     * Determines whether the provided class represents a domain entity.
     * Uses the registered {@link EntityUtilsProvider} to check if the class is an entity type.
     *
     * @param entityClass the class to check
     * @return true if the class is an entity type, false otherwise
     *
     * @see #isEntity(Object)
     *
     * Example:
     * <pre>{@code
     * boolean result = isEntity(Customer.class); // Returns true if Customer is a JPA entity
     * }</pre>
     */
    public static boolean isEntity(Class entityClass) {
        EntityUtilsProvider entityUtilsProvider = Containers.get().findObject(EntityUtilsProvider.class);
        if (entityUtilsProvider != null) {
            return entityUtilsProvider.isEntity(entityClass);
        }
        return false;
    }

    /**
     * Checks whether a field can be persisted to the database.
     * Uses the registered {@link EntityUtilsProvider} to determine if the field is persistable.
     * Returns true by default if no EntityUtilsProvider is found.
     *
     * @param field the field to check for persistence capability
     * @return true if the field can be persisted, false otherwise; defaults to true
     *
     * Example:
     * <pre>{@code
     * Field nameField = Customer.class.getDeclaredField("name");
     * boolean canPersist = isPersitable(nameField); // Returns true if field is persistable
     * }</pre>
     */
    public static boolean isPersitable(Field field) {
        EntityUtilsProvider entityUtilsProvider = Containers.get().findObject(EntityUtilsProvider.class);
        if (entityUtilsProvider != null) {
            return entityUtilsProvider.isPersitable(field);
        }
        return true;
    }

    /**
     * Retrieves the default Parameter class used for query operations.
     * Finds the current {@link EntityUtilsProvider} implementation and returns its default {@link Parameter} class.
     *
     * @return the default Parameter class, or null if no EntityUtilsProvider is found
     *
     * Example:
     * <pre>{@code
     * Class<? extends Parameter> paramClass = getDefaultParameterClass();
     * }</pre>
     */
    public static Class<? extends Parameter> getDefaultParameterClass() {
        EntityUtilsProvider entityUtilsProvider = Containers.get().findObject(EntityUtilsProvider.class);
        if (entityUtilsProvider != null) {
            return entityUtilsProvider.getDefaultParameterClass();
        }
        return null;
    }

    /**
     * Automatically transfers all values from a target object to a new instance of a DTO class.
     * The DTO should be a POJO with getters and setters. If the target class has non-standard
     * Java properties (like entity references), this method attempts to parse them to String
     * or to an id property in the DTO class.
     *
     * <p>Supported mapping patterns:</p>
     * <ul>
     *   <li><b>Case 1:</b> Entity reference to String - Maps entity.toString() to DTO string field</li>
     *   <li><b>Case 2:</b> Entity reference to ID - Maps entity ID to DTO Long field with 'Id' suffix</li>
     *   <li><b>Case 3:</b> Entity reference to both - Maps both entity.toString() and ID to separate DTO fields</li>
     * </ul>
     *
     * @param <DTO>    the type of the DTO class
     * @param target   the source object to extract data from
     * @param dtoClass the class of the DTO to create and populate
     * @return a new instance of DTO class with all common properties set
     *
     * Example:
     * <pre>{@code
     * // Case 1: Entity to String
     * class Order {
     *     private Customer customer;
     *     // getters and setters
     * }
     * class OrderDTO {
     *     private String customer;
     *     // getters and setters
     * }
     *
     * // Case 2: Entity to ID
     * class OrderDTO {
     *     private Long customerId;
     *     // getters and setters
     * }
     *
     * // Case 3: Entity to both String and ID
     * class OrderDTO {
     *     private Long customerId;
     *     private String customer;
     *     // getters and setters
     * }
     *
     * Order order = new Order();
     * OrderDTO dto = autoDataTransferObject(order, OrderDTO.class);
     * }</pre>
     */
    public static <DTO> DTO autoDataTransferObject(Object target, Class<DTO> dtoClass) {
        return DataTransferObjectBuilder.buildDTO(target, dtoClass);
    }


    /**
     * Validates an object using the registered {@link ValidatorService} implementation.
     * This is a convenience method that delegates to the ValidatorService if available.
     * Typically validates JSR-303/JSR-380 Bean Validation annotations.
     *
     * @param obj the object to validate
     * @throws ValidationError if validation fails
     *
     * Example:
     * <pre>{@code
     * Customer customer = new Customer();
     * customer.setEmail("invalid-email");
     * validate(customer); // Throws ValidationError if email format is invalid
     * }</pre>
     */
    public static void validate(Object obj) {
        ValidatorService service = Containers.get().findObject(ValidatorService.class);
        if (service != null) {
            service.validate(obj);
        }
    }

    /**
     * Retrieves the display name of an entity reference by its alias and ID.
     * First finds the EntityReferenceRepository by alias, then loads the entity reference
     * and returns its name.
     *
     * @param alias the alias of the entity reference repository
     * @param id    the ID of the entity reference to load
     * @return the name of the entity reference, or null if not found or parameters are null
     *
     * @see #getEntityReferenceName(String, Serializable, String)
     *
     * Example:
     * <pre>{@code
     * String customerName = getEntityReferenceName("customer", 123L);
     * // Returns the name of customer with ID 123
     * }</pre>
     */
    public static String getEntityReferenceName(String alias, Serializable id) {
        if (alias != null && id != null) {
            var repo = getEntityReferenceRepositoryByAlias(alias);
            if (repo != null) {
                var reference = repo.load(id);
                if (reference != null) {
                    return reference.getName();
                }
            }
        }
        return null;
    }

    /**
     * Retrieves the display name of an entity reference by its alias and ID, with a default value fallback.
     * First finds the EntityReferenceRepository by alias, then loads the entity reference
     * and returns its name. If not found or blank, returns the provided default value.
     *
     * @param alias        the alias of the entity reference repository
     * @param id           the ID of the entity reference to load
     * @param defaultValue the default value to return if name is not found or blank
     * @return the name of the entity reference, or defaultValue if not found or blank
     *
     * @see #getEntityReferenceName(String, Serializable)
     *
     * Example:
     * <pre>{@code
     * String customerName = getEntityReferenceName("customer", 999L, "Unknown Customer");
     * // Returns "Unknown Customer" if customer with ID 999 is not found
     * }</pre>
     */
    public static String getEntityReferenceName(String alias, Serializable id, String defaultValue) {
        String name = getEntityReferenceName(alias, id);
        return name != null && !name.isBlank() ? name : defaultValue;
    }

    /**
     * Increments a counter field on an entity and returns the new value.
     * This is useful for generating sequential numbers like invoice numbers, order numbers, etc.
     *
     * @param entity      the entity containing the counter field
     * @param counterName the name of the counter field to increment
     * @return the new incremented counter value
     *
     * Example:
     * <pre>{@code
     * Invoice invoice = new Invoice();
     * long invoiceNumber = findNextCounterValue(invoice, "invoiceNumber");
     * // Returns the next invoice number and increments the counter
     * }</pre>
     */
    public static long findNextCounterValue(Object entity, String counterName) {
        var crud = lookupCrudService();
        crud.increaseCounter(entity, counterName);
        return crud.getFieldValue(entity, counterName, Long.class);
    }

    /**
     * Retrieves the registered CurrencyExchangeProvider implementation from the application context.
     * This provider is used for currency conversion operations.
     *
     * @return the CurrencyExchangeProvider instance, or null if no implementation is found
     *
     * Example:
     * <pre>{@code
     * CurrencyExchangeProvider provider = getCurrencyExchangeProvider();
     * if (provider != null) {
     *     BigDecimal rate = provider.getExchangeRate("USD", "EUR");
     * }
     * }</pre>
     */
    public static CurrencyExchangeProvider getCurrencyExchangeProvider() {
        return Containers.get().findObject(CurrencyExchangeProvider.class);
    }

    /**
     * Verifies if the provided value can be interpreted as a Long number.
     *
     * @param value the value to verify.
     * @return true if the value is a valid Long number; otherwise, false.
     */
    public static boolean isLong(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        try {
            Long.parseLong(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    /**
     * Verifies if the provided value can be interpreted as a Integer number.
     *
     * @param value the value to verify.
     * @return true if the value is a valid Integer number; otherwise, false.
     */
    public static boolean isInteger(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
