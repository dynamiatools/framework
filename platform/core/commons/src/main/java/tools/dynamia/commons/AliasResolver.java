package tools.dynamia.commons;

import tools.dynamia.commons.reflect.PropertyInfo;

import java.lang.reflect.AnnotatedElement;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Resolves the most suitable alias for a class or field
 * based on scope, locale, and priority ordering.
 * <p>
 * Resolution rules:
 * 1. Filter aliases by scope if provided.
 * 2. Then filter by locale if provided.
 * 3. If multiple candidates remain, pick the highest priority.
 * 4. If the alias has multiple primary values (`value()`),
 * return the first one.
 * 5. If no alias matches, fallback to the element's real name.
 */
public final class AliasResolver {

    private AliasResolver() {
        // utility class
    }

    /**
     * Resolves the alias for the given type using default scope and empty locale.
     *
     * @param type the class to resolve the alias for
     * @return the resolved alias or the simple name of the class if no alias is found
     */
    public static String resolve(Class<?> type) {
        return resolveAliasValue(type, "default", "", type.getSimpleName());
    }

    /**
     * Resolves the alias for the given type using the specified scope and empty locale.
     *
     * @param type  the class to resolve the alias for
     * @param scope the scope to filter aliases
     * @return the resolved alias or the simple name of the class if no alias is found
     */
    public static String resolve(Class<?> type, String scope) {
        return resolveAliasValue(type, scope, "", type.getSimpleName());
    }

    /**
     * Resolves all matching alias values for the given type, scope, and locale.
     *
     * @param type   the class to resolve aliases for
     * @param scope  the scope to filter aliases
     * @param locale the locale to filter aliases
     * @return a list of resolved alias values
     */
    public static List<String> resolveAll(Class<?> type, String scope, String locale) {
        return resolveAliasValues(type, scope, locale);
    }

    /**
     * Resolves the alias for the given type using the specified scope and locale.
     *
     * @param type   the class to resolve the alias for
     * @param scope  the scope to filter aliases
     * @param locale the locale to filter aliases
     * @return the resolved alias or the simple name of the class if no alias is found
     */
    public static String resolve(Class<?> type, String scope, String locale) {
        return resolveAliasValue(type, scope, locale, type.getSimpleName());
    }

    /**
     * Resolves the alias for the given field using the specified scope and locale.
     *
     * @param field  the field to resolve the alias for
     * @param scope  the scope to filter aliases
     * @param locale the locale to filter aliases
     * @return the resolved alias or the name of the field if no alias is found
     */
    public static String resolve(java.lang.reflect.Field field, String scope, String locale) {
        return resolveAliasValue(field, scope, locale, field.getName());
    }

    private static String resolveAliasValue(AnnotatedElement element, String scope, String locale, String fallback) {
        return resolveAliasValues(element, scope, locale)
                .stream()
                .findFirst()
                .orElse(fallback);
    }

    private static List<String> resolveAliasValues(AnnotatedElement element, String scope, String locale) {
        List<Alias> aliases = extractAliases(element);

        if (aliases.isEmpty()) {
            return Collections.emptyList();
        }

        // first filter by scope if provided
        if (scope != null && !scope.isBlank()) {
            aliases = aliases.stream()
                    .filter(a -> a.scope().equalsIgnoreCase(scope))
                    .collect(Collectors.toList());
        }

        // then filter by locale if provided
        if (locale != null && !locale.isBlank()) {
            List<Alias> localeMatches = aliases.stream()
                    .filter(a -> a.locale().equalsIgnoreCase(locale))
                    .collect(Collectors.toList());

            if (!localeMatches.isEmpty()) {
                aliases = localeMatches;
            }
        }

        // if empty, go back to all aliases (fallback)
        if (aliases.isEmpty()) {
            aliases = extractAliases(element);
        }

        // pick highest priority
        return aliases.stream()

                .map(a -> a.value().length > 0 ? a.value()[0] : null)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Extracts all {@link Alias} annotations from the given element, including those inside {@link AliasSet}.
     *
     * @param element the element to extract aliases from
     * @return a list of found aliases
     */
    public static List<Alias> extractAliases(AnnotatedElement element) {
        List<Alias> list = new ArrayList<>();

        Alias direct = element.getAnnotation(Alias.class);
        if (direct != null) {
            list.add(direct);
        }

        AliasSet set = element.getAnnotation(AliasSet.class);
        if (set != null) {
            list.addAll(Arrays.asList(set.value()));
        }
        return list.stream().sorted(Comparator.comparingInt(Alias::priority).reversed()).toList();
    }

    /**
     * Gets the highest priority {@link Alias} for the given class.
     *
     * @param type the class to get the alias for
     * @return the highest priority alias or null if none found
     */
    public static Alias getClassAlias(Class<?> type) {
        List<Alias> aliases = extractAliases(type);
        return aliases.stream().findFirst()
                .orElse(null);

    }

    /**
     * Gets the highest priority {@link Alias} for the given field in the specified class.
     *
     * @param type      the class containing the field
     * @param fieldName the name of the field
     * @return the highest priority alias or null if none found
     */
    public static Alias getFieldAlias(Class<?> type, String fieldName) {
        try {
            java.lang.reflect.Field field = type.getDeclaredField(fieldName);
            List<Alias> aliases = extractAliases(field);
            return aliases.stream().findFirst()
                    .orElse(null);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    /**
     * Gets the highest priority {@link Alias} for the given property.
     *
     * @param propertyInfo the property to get the alias for
     * @return the highest priority alias or null if none found
     */
    public static Alias getPropertyAlias(PropertyInfo propertyInfo) {
        return extractAliases(propertyInfo.getType())
                .stream().findFirst()
                .orElse(null);
    }

    /**
     * Gets the highest priority {@link Alias} for the given method in the specified class.
     *
     * @param type       the class containing the method
     * @param methodName the name of the method
     * @return the highest priority alias or null if none found
     */
    public static Alias getMethodAlias(Class<?> type, String methodName) {
        try {
            java.lang.reflect.Method method = type.getDeclaredMethod(methodName);
            List<Alias> aliases = extractAliases(method);
            return aliases.stream().findFirst()
                    .orElse(null);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }


}
