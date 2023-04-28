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
 * The Class ApplicableClass.
 *
 * @author Mario A. Serrano Leones
 */
public class ApplicableClass {

	/**
	 * The Constant ALL.
	 */
	public static final ApplicableClass[] ALL = { new ApplicableClass(null) };

	/**
	 * The target class.
	 */
	private final Class targetClass;

	/**
	 * Checks if is applicable.
	 *
	 * @param objectClass
	 *            the object class
	 * @param applicableClasses
	 *            the applicable classes
	 * @return true, if is applicable
	 */
	public static boolean isApplicable(Class<?> objectClass, ApplicableClass[] applicableClasses) {
		return isApplicable(objectClass, applicableClasses, false);
	}

	/**
	 * Checks if is applicable.
	 *
	 * @param objectClass
	 *            the object class
	 * @param applicableClasses
	 *            the applicable classes
	 * @param includeParents
	 *            the include parents
	 * @return true, if is applicable
	 */
	public static boolean isApplicable(Class<?> objectClass, ApplicableClass[] applicableClasses,
			boolean includeParents) {
		if (applicableClasses == ALL || applicableClasses == null) {
			return true;
		}

		for (ApplicableClass applicableClass : applicableClasses) {
			if (applicableClass.getTargetClass().equals(objectClass)) {
				return true;
			} else if (includeParents) {
				if (BeanUtils.isAssignable(objectClass, applicableClass.getTargetClass())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Instantiates a new applicable class.
	 *
	 * @param targetClass
	 *            the target class
	 */
	public ApplicableClass(Class targetClass) {
		this.targetClass = targetClass;
	}

	/**
	 * Gets the target class.
	 *
	 * @return the target class
	 */
	public Class getTargetClass() {
		return targetClass;
	}

	/**
	 * Gets the.
	 *
	 * @param classes
	 *            the classes
	 * @return the applicable class[]
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
