package tools.dynamia.commons.reflect;

import tools.dynamia.commons.SimpleCache;

import java.util.Collections;
import java.util.List;

/**
 * Class cache for reflections process
 */
public record ClassReflectionInfo(Class<?> targetClass, List<PropertyInfo> properties) {

    private static final SimpleCache<Class<?>, ClassReflectionInfo> CACHE = new SimpleCache<>();

    public static ClassReflectionInfo getFromCache(Class<?> targetClass) {
        return CACHE.get(targetClass);
    }

    public static void addToCache(ClassReflectionInfo info) {
        CACHE.add(info.targetClass(), info);
    }

    public static void clearCache(Class<?> targetClass) {
        CACHE.remove(targetClass);
    }

    public ClassReflectionInfo(Class<?> targetClass, List<PropertyInfo> properties) {
        this.targetClass = targetClass;
        this.properties = Collections.unmodifiableList(properties);
    }

    public String getName() {
        return targetClass().getName();
    }

    public String getSimpleName() {
        return targetClass().getSimpleName();
    }
}
