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
package tools.dynamia.commons;

/**
 * Represents a wrapper for a target {@link Class} to be used for applicability checks.
 * <p>
 * This record is commonly used to determine if an action or feature is applicable to a specific class or set of classes.
 * It provides utility methods for applicability checks and for creating arrays of applicable classes.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 *     ApplicableClass[] applicable = ApplicableClass.get(MyClass.class, OtherClass.class);
 *     boolean isValid = ApplicableClass.isApplicable(target.getClass(), applicable);
 * </pre>
 * </p>
 *
 * @param targetClass the target class to check applicability against; may be {@code null} to represent all classes
 * @author Mario A. Serrano Leones
 */
public record ApplicableClass(Class targetClass) {

    /**
     * Constant representing applicability to all classes.
     * If used, any class will be considered applicable.
     */
    public static final ApplicableClass[] ALL = {new ApplicableClass(null)};

    /**
     * Checks if the given object class is applicable to any of the provided {@link ApplicableClass} instances.
     *
     * @param objectClass the class to check
     * @param applicableClasses the array of applicable classes
     * @return {@code true} if the object class is applicable; {@code false} otherwise
     */
    public static boolean isApplicable(Class<?> objectClass, ApplicableClass[] applicableClasses) {
        return isApplicable(objectClass, applicableClasses, false);
    }

    /**
     * Checks if the given object class is applicable to any of the provided {@link ApplicableClass} instances,
     * optionally including parent classes and interfaces.
     *
     * @param objectClass the class to check
     * @param applicableClasses the array of applicable classes
     * @param includeParents if {@code true}, parent classes and interfaces are considered
     * @return {@code true} if the object class is applicable; {@code false} otherwise
     */
    public static boolean isApplicable(Class<?> objectClass, ApplicableClass[] applicableClasses,
                                       boolean includeParents) {
        if (applicableClasses == ALL || applicableClasses == null) {
            return true;
        }

        for (ApplicableClass applicableClass : applicableClasses) {
            if (applicableClass.targetClass() != null && applicableClass.targetClass().equals(objectClass)) {
                return true;
            } else if (includeParents && applicableClass.targetClass() != null) {
                if (ObjectOperations.isAssignable(objectClass, applicableClass.targetClass())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Constructs an {@code ApplicableClass} for the given target class.
     *
     * @param targetClass the target class
     */
    public ApplicableClass {
    }

    /**
     * Returns the target class for this {@code ApplicableClass}.
     *
     * @return the target class
     */
    @Override
    public Class targetClass() {
        return targetClass;
    }

    /**
     * Creates an array of {@code ApplicableClass} instances for the given classes.
     * If no classes are provided, returns {@link #ALL}.
     *
     * @param classes the classes to wrap
     * @return an array of {@code ApplicableClass} instances
     */
    public static ApplicableClass[] get(Class... classes) {
        if (classes.length == 0) {
            return ALL;
        }

        ApplicableClass[] appClasses = new ApplicableClass[classes.length];
        for (int i = 0; i < classes.length; i++) {
            appClasses[i] = new ApplicableClass(classes[i]);
        }
        return appClasses;
    }
}
