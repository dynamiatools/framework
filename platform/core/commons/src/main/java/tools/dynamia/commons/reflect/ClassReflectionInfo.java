package tools.dynamia.commons.reflect;

import tools.dynamia.commons.SimpleCache;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * ClassReflectionInfo is an immutable record that encapsulates reflection metadata for a target class, including its properties.
 * It provides convenient access to the class name, simple name, and a list of {@link PropertyInfo} objects describing the class's properties.
 * </p>
 *
 * <p>
 * This class also manages a static cache for efficient reuse of reflection information, reducing the overhead of repeated reflection operations.
 * </p>
 *
 * <p>
 * Typical use cases include frameworks, libraries, or utilities that require introspection of class structure, such as property mapping, serialization, or UI generation.
 * </p>
 *
 * @author Dynamia Soluciones IT S.A.S
 * @since 2023
 */
public record ClassReflectionInfo(Class<?> targetClass, List<PropertyInfo> properties) {

    /**
     * Static cache for storing and retrieving ClassReflectionInfo instances by target class.
     * Improves performance by avoiding repeated reflection operations.
     */
    private static final SimpleCache<Class<?>, ClassReflectionInfo> CACHE = new SimpleCache<>();

    /**
     * Retrieves the ClassReflectionInfo for the specified class from the cache.
     *
     * @param targetClass the class whose reflection info is to be retrieved
     * @return the cached ClassReflectionInfo, or null if not present
     */
    public static ClassReflectionInfo getFromCache(Class<?> targetClass) {
        return CACHE.get(targetClass);
    }

    /**
     * Adds the given ClassReflectionInfo to the cache.
     *
     * @param info the ClassReflectionInfo to cache
     */
    public static void addToCache(ClassReflectionInfo info) {
        CACHE.add(info.targetClass(), info);
    }

    /**
     * Removes the cached ClassReflectionInfo for the specified class.
     *
     * @param targetClass the class whose cache entry should be removed
     */
    public static void clearCache(Class<?> targetClass) {
        CACHE.remove(targetClass);
    }

    /**
     * Constructs a new ClassReflectionInfo for the given class and its properties.
     * The properties list is wrapped as an unmodifiable list to ensure immutability.
     *
     * @param targetClass the class being reflected
     * @param properties the list of property metadata
     */
    public ClassReflectionInfo(Class<?> targetClass, List<PropertyInfo> properties) {
        this.targetClass = targetClass;
        this.properties = Collections.unmodifiableList(properties);
    }

    /**
     * Returns the fully qualified name of the target class.
     *
     * @return the class name
     */
    public String getName() {
        return targetClass().getName();
    }

    /**
     * Returns the simple name of the target class (without package).
     *
     * @return the simple class name
     */
    public String getSimpleName() {
        return targetClass().getSimpleName();
    }
}
