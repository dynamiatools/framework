package tools.dynamia.commons.reflect;

import tools.dynamia.commons.SimpleCache;

import java.util.Collections;
import java.util.List;

/**
 * Class cache for reflections process
 */
public class ClassReflectionInfo {

    private static final SimpleCache<Class<?>, ClassReflectionInfo> CACHE = new SimpleCache<>();

    public static ClassReflectionInfo getFromCache(Class<?> targetClass) {
        return CACHE.get(targetClass);
    }

    public static void addToCache(ClassReflectionInfo info) {
        CACHE.add(info.getTargetClass(), info);
    }

    public static void clearCache(Class<?> targetClass){
        CACHE.remove(targetClass);
    }

    private final Class<?> targetClass;
    private final List<PropertyInfo> properties;

    public ClassReflectionInfo(Class<?> targetClass, List<PropertyInfo> properties) {
        this.targetClass = targetClass;
        this.properties = Collections.unmodifiableList(properties);
    }

    public List<PropertyInfo> getProperties() {
        return properties;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public String getName() {
        return getTargetClass().getName();
    }

    public String getSimpleName() {
        return getTargetClass().getSimpleName();
    }
}
