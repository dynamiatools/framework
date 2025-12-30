package tools.dynamia.commons;

import tools.dynamia.commons.reflect.PropertyInfo;

/**
 * Maps properties between two beans based on their aliases.
 */
public class AliasBeanMapper {

    /**
     * Maps properties from the source object to the target object
     * based on matching aliases within the specified scope.
     *
     * @param source the source object to map from
     * @param target the target object to map to
     * @param scope  the scope to consider when resolving aliases
     */
    public static void map(Object source, Object target, String scope) {
        var sourceProps = PropertyInfo.getProperties(source.getClass());
        var targetProps = PropertyInfo.getProperties(target.getClass());

        for (var sourceProp : sourceProps) {
            String sourceAlias = resolvePropertyAlias(source.getClass(), sourceProp.getName(), scope);
            if (sourceAlias != null) {
                for (var targetProp : targetProps) {
                    String targetAlias = resolvePropertyAlias(target.getClass(), targetProp.getName(), scope);
                    if (sourceAlias.equals(targetAlias) && sourceProp.getType().equals(targetProp.getType())) {
                        Object value = sourceProp.getValue(source);
                        targetProp.setValue(target, value);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Resolves the alias for a property of a given type within a specified scope.
     *
     * @param type         the class containing the property
     * @param propertyName the name of the property
     * @param scope        the scope to consider when resolving the alias
     * @return the resolved alias or the original property name if no alias is found
     */
    private static String resolvePropertyAlias(Class<?> type, String propertyName, String scope) {
        try {
            var field = type.getDeclaredField(propertyName);
            return AliasResolver.resolve(field, scope, null);
        } catch (NoSuchFieldException e) {
            return propertyName;
        }
    }
}
