package tools.dynamia.commons;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides predefined scope constants and utilities for working with the {@link Exposure} annotation.
 * <p>
 * Scopes are used to control the visibility and accessibility of classes, fields, and methods in different
 * contexts within the Dynamia Tools framework. These scopes are typically used in conjunction with the
 * {@code @Exposure} annotation to define where and how components should be exposed.
 * </p>
 *
 * <h2>Usage with @Exposure Annotation</h2>
 * <p>
 * The {@code @Exposure} annotation accepts an array of scope strings to define where a component is accessible.
 * This class provides predefined constants for common scopes used throughout the framework.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Expose a field only in UI context
 * @Exposure(scope = {Scopes.UI})
 * private String displayName;
 *
 * // Expose a method in both API and external contexts
 * @Exposure(scope = {Scopes.API, Scopes.EXTERNAL})
 * public String getPublicData() {
 *     return data;
 * }
 *
 * // Expose a class in multiple scopes
 * @Exposure(scope = {Scopes.MODULE, Scopes.INTERNAL})
 * public class InternalService {
 *     // ...
 * }
 * }</pre>
 *
 * <h2>Predefined Scopes</h2>
 * <ul>
 *   <li><strong>module:</strong> Component is accessible within its module</li>
 *   <li><strong>application:</strong> Component is accessible throughout the application</li>
 *   <li><strong>system:</strong> Component is accessible at system level</li>
 *   <li><strong>external:</strong> Component is accessible from external systems</li>
 *   <li><strong>internal:</strong> Component is for internal use only</li>
 *   <li><strong>ui:</strong> Component is exposed in user interface</li>
 *   <li><strong>api:</strong> Component is exposed via API</li>
 *   <li><strong>service:</strong> Component is a public service</li>
 *   <li><strong>data-api:</strong> Component is exposed in data API</li>
 *   <li><strong>admin-api:</strong> Component is exposed in admin API</li>
 *   <li><strong>auth-api:</strong> Component is exposed in authentication API</li>
 *   <li><strong>reporting-api:</strong> Component is exposed in reporting API</li>
 *   <li><strong>meta-api:</strong> Component is exposed in metadata API</li>
 * </ul>
 *
 * <h2>Utility Methods</h2>
 * <p>
 * This class also provides utility methods to retrieve fields, methods, and other annotated elements
 * filtered by specific scopes using reflection and {@link ObjectOperations}.
 * </p>
 *
 * <h3>Example: Finding Exposed Fields</h3>
 * <pre>{@code
 * // Get all fields exposed in UI scope
 * List<Field> uiFields = Scopes.getFieldsByScope(MyEntity.class, Scopes.UI);
 *
 * // Get all methods exposed in API scope
 * List<Method> apiMethods = Scopes.getMethodsByScope(MyService.class, Scopes.API);
 *
 * // Check if a field is exposed in a specific scope
 * Field field = MyEntity.class.getDeclaredField("name");
 * boolean isExposed = Scopes.isExposedInScope(field, Scopes.UI);
 * }</pre>
 *
 * @author Ing. Mario Serrano Leones
 * @see Exposure
 * @see ObjectOperations
 */
public class Scopes {

    /**
     * Scope for module-level accessibility
     */
    public static final String MODULE = "module";

    /**
     * Scope for application-wide accessibility
     */
    public static final String APPLICATION = "application";

    /**
     * Scope for system-level accessibility
     */
    public static final String SYSTEM = "system";

    /**
     * Scope for external system accessibility
     */
    public static final String EXTERNAL = "external";

    /**
     * Scope for internal use only
     */
    public static final String INTERNAL = "internal";

    /**
     * Scope for user interface exposure
     */
    public static final String UI = "ui";

    /**
     * Scope for API exposure
     */
    public static final String API = "api";

    /**
     * Scope for public services
     */
    public static final String PUBLIC = "service";

    /**
     * Scope for data API exposure
     */
    public static final String DATA_API = "data-api";

    /**
     * Scope for admin API exposure
     */
    public static final String ADMIN_API = "admin-api";

    /**
     * Scope for authentication API exposure
     */
    public static final String AUTH_API = "auth-api";

    /**
     * Scope for reporting API exposure
     */
    public static final String REPORTING_API = "reporting-api";

    /**
     * Scope for metadata API exposure
     */
    public static final String METADATA_API = "meta-api";

    /**
     * Set of all registered scopes
     */
    private static final Set<String> REGISTERED_SCOPES = new HashSet<>();

    static {
        REGISTERED_SCOPES.add(MODULE);
        REGISTERED_SCOPES.add(APPLICATION);
        REGISTERED_SCOPES.add(SYSTEM);
        REGISTERED_SCOPES.add(EXTERNAL);
        REGISTERED_SCOPES.add(INTERNAL);
        REGISTERED_SCOPES.add(UI);
        REGISTERED_SCOPES.add(API);
        REGISTERED_SCOPES.add(PUBLIC);
        REGISTERED_SCOPES.add(DATA_API);
        REGISTERED_SCOPES.add(ADMIN_API);
        REGISTERED_SCOPES.add(AUTH_API);
        REGISTERED_SCOPES.add(REPORTING_API);
        REGISTERED_SCOPES.add(METADATA_API);
    }

    /**
     * Private constructor to prevent instantiation
     */
    private Scopes() {
        // Utility class
    }

    /**
     * Registers a custom scope to the set of known scopes.
     *
     * @param scope the scope to register
     */
    public static void registerScope(String scope) {
        if (scope != null && !scope.isEmpty()) {
            REGISTERED_SCOPES.add(scope.toLowerCase());
        }
    }

    /**
     * Checks if a scope is registered.
     *
     * @param scope the scope to check
     * @return true if the scope is registered, false otherwise
     */
    public static boolean isRegistered(String scope) {
        return scope != null && REGISTERED_SCOPES.contains(scope.toLowerCase());
    }

    /**
     * Returns an unmodifiable set of all registered scopes.
     *
     * @return set of registered scopes
     */
    public static Set<String> getRegisteredScopes() {
        return Collections.unmodifiableSet(REGISTERED_SCOPES);
    }

    /**
     * Retrieves all fields from a class that are annotated with {@link Exposure}
     * and exposed in the specified scope.
     *
     * @param clazz the class to inspect
     * @param scope the scope to filter by
     * @return list of fields exposed in the specified scope
     *
     * <p>Example:
     * <pre>{@code
     * List<Field> uiFields = Scopes.getFieldsByScope(Person.class, Scopes.UI);
     * uiFields.forEach(field -> System.out.println(field.getName()));
     * }</pre>
     */
    public static List<Field> getFieldsByScope(Class<?> clazz, String scope) {
        if (clazz == null || scope == null) {
            return Collections.emptyList();
        }

        return getAllFields(clazz)
                .filter(field -> isExposedInScope(field, scope))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all methods from a class that are annotated with {@link Exposure}
     * and exposed in the specified scope.
     *
     * @param clazz the class to inspect
     * @param scope the scope to filter by
     * @return list of methods exposed in the specified scope
     *
     * <p>Example:
     * <pre>{@code
     * List<Method> apiMethods = Scopes.getMethodsByScope(UserService.class, Scopes.API);
     * apiMethods.forEach(method -> System.out.println(method.getName()));
     * }</pre>
     */
    public static List<Method> getMethodsByScope(Class<?> clazz, String scope) {
        if (clazz == null || scope == null) {
            return Collections.emptyList();
        }

        return getAllMethods(clazz)
                .filter(method -> isExposedInScope(method, scope))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all fields from a class that are annotated with {@link Exposure}
     * and exposed in any of the specified scopes.
     *
     * @param clazz  the class to inspect
     * @param scopes the scopes to filter by
     * @return list of fields exposed in any of the specified scopes
     *
     * <p>Example:
     * <pre>{@code
     * List<Field> exposedFields = Scopes.getFieldsByScopes(Person.class, Scopes.UI, Scopes.API);
     * }</pre>
     */
    public static List<Field> getFieldsByScopes(Class<?> clazz, String... scopes) {
        if (clazz == null || scopes == null || scopes.length == 0) {
            return Collections.emptyList();
        }

        return getAllFields(clazz)
                .filter(field -> isExposedInAnyScope(field, scopes))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all methods from a class that are annotated with {@link Exposure}
     * and exposed in any of the specified scopes.
     *
     * @param clazz  the class to inspect
     * @param scopes the scopes to filter by
     * @return list of methods exposed in any of the specified scopes
     *
     * <p>Example:
     * <pre>{@code
     * List<Method> exposedMethods = Scopes.getMethodsByScopes(UserService.class, Scopes.API, Scopes.EXTERNAL);
     * }</pre>
     */
    public static List<Method> getMethodsByScopes(Class<?> clazz, String... scopes) {
        if (clazz == null || scopes == null || scopes.length == 0) {
            return Collections.emptyList();
        }

        return getAllMethods(clazz)
                .filter(method -> isExposedInAnyScope(method, scopes))
                .collect(Collectors.toList());
    }

    /**
     * Checks if a field is exposed in the specified scope.
     *
     * @param field the field to check
     * @param scope the scope to verify
     * @return true if the field is exposed in the scope, false otherwise
     *
     * <p>Example:
     * <pre>{@code
     * Field field = Person.class.getDeclaredField("email");
     * if (Scopes.isExposedInScope(field, Scopes.API)) {
     *     // Field is exposed in API
     * }
     * }</pre>
     */
    public static boolean isExposedInScope(Field field, String scope) {
        if (field == null || scope == null) {
            return false;
        }

        Exposure exposure = field.getAnnotation(Exposure.class);
        return exposure != null && containsScope(exposure.scope(), scope);
    }

    /**
     * Checks if a method is exposed in the specified scope.
     *
     * @param method the method to check
     * @param scope  the scope to verify
     * @return true if the method is exposed in the scope, false otherwise
     *
     * <p>Example:
     * <pre>{@code
     * Method method = UserService.class.getMethod("getUser", Long.class);
     * if (Scopes.isExposedInScope(method, Scopes.API)) {
     *     // Method is exposed in API
     * }
     * }</pre>
     */
    public static boolean isExposedInScope(Method method, String scope) {
        if (method == null || scope == null) {
            return false;
        }

        Exposure exposure = method.getAnnotation(Exposure.class);
        return exposure != null && containsScope(exposure.scope(), scope);
    }

    /**
     * Checks if a class is exposed in the specified scope.
     *
     * @param clazz the class to check
     * @param scope the scope to verify
     * @return true if the class is exposed in the scope, false otherwise
     *
     * <p>Example:
     * <pre>{@code
     * if (Scopes.isExposedInScope(UserService.class, Scopes.EXTERNAL)) {
     *     // Class is exposed externally
     * }
     * }</pre>
     */
    public static boolean isExposedInScope(Class<?> clazz, String scope) {
        if (clazz == null || scope == null) {
            return false;
        }

        Exposure exposure = clazz.getAnnotation(Exposure.class);
        return exposure != null && containsScope(exposure.scope(), scope);
    }

    /**
     * Checks if a field is exposed in any of the specified scopes.
     *
     * @param field  the field to check
     * @param scopes the scopes to verify
     * @return true if the field is exposed in any scope, false otherwise
     */
    public static boolean isExposedInAnyScope(Field field, String... scopes) {
        if (field == null || scopes == null || scopes.length == 0) {
            return false;
        }

        Exposure exposure = field.getAnnotation(Exposure.class);
        if (exposure == null) {
            return false;
        }

        return Arrays.stream(scopes).anyMatch(scope -> containsScope(exposure.scope(), scope));
    }

    /**
     * Checks if a method is exposed in any of the specified scopes.
     *
     * @param method the method to check
     * @param scopes the scopes to verify
     * @return true if the method is exposed in any scope, false otherwise
     */
    public static boolean isExposedInAnyScope(Method method, String... scopes) {
        if (method == null || scopes == null || scopes.length == 0) {
            return false;
        }

        Exposure exposure = method.getAnnotation(Exposure.class);
        if (exposure == null) {
            return false;
        }

        return Arrays.stream(scopes).anyMatch(scope -> containsScope(exposure.scope(), scope));
    }

    /**
     * Gets the exposed scopes for a field.
     *
     * @param field the field to inspect
     * @return array of scopes, or empty array if not annotated
     */
    public static String[] getExposedScopes(Field field) {
        if (field == null) {
            return new String[0];
        }

        Exposure exposure = field.getAnnotation(Exposure.class);
        return exposure != null ? exposure.scope() : new String[0];
    }

    /**
     * Gets the exposed scopes for a method.
     *
     * @param method the method to inspect
     * @return array of scopes, or empty array if not annotated
     */
    public static String[] getExposedScopes(Method method) {
        if (method == null) {
            return new String[0];
        }

        Exposure exposure = method.getAnnotation(Exposure.class);
        return exposure != null ? exposure.scope() : new String[0];
    }

    /**
     * Gets the exposed scopes for a class.
     *
     * @param clazz the class to inspect
     * @return array of scopes, or empty array if not annotated
     */
    public static String[] getExposedScopes(Class<?> clazz) {
        if (clazz == null) {
            return new String[0];
        }

        Exposure exposure = clazz.getAnnotation(Exposure.class);
        return exposure != null ? exposure.scope() : new String[0];
    }

    /**
     * Helper method to get all fields from a class including inherited fields.
     *
     * @param clazz the class to inspect
     * @return stream of fields
     */
    private static Stream<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;

        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }

        return fields.stream();
    }

    /**
     * Helper method to get all methods from a class including inherited methods.
     *
     * @param clazz the class to inspect
     * @return stream of methods
     */
    private static Stream<Method> getAllMethods(Class<?> clazz) {
        List<Method> methods = new ArrayList<>();
        Class<?> current = clazz;

        while (current != null && current != Object.class) {
            methods.addAll(Arrays.asList(current.getDeclaredMethods()));
            current = current.getSuperclass();
        }

        return methods.stream();
    }

    /**
     * Helper method to check if a scope array contains a specific scope (case-insensitive).
     *
     * @param scopes       the array of scopes
     * @param targetScope  the scope to find
     * @return true if the scope is found, false otherwise
     */
    private static boolean containsScope(String[] scopes, String targetScope) {
        if (scopes == null || targetScope == null) {
            return false;
        }

        return Arrays.stream(scopes)
                .anyMatch(s -> s.equalsIgnoreCase(targetScope));
    }
}
