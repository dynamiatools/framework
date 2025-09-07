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
package tools.dynamia.actions;

import tools.dynamia.commons.ApplicableClass;

/**
 * Represents an action that is only applicable to specific classes.
 * <p>
 * Implementations of this interface define actions that can be executed only for certain target classes.
 * This is useful for scenarios where actions should be restricted or customized based on the type of object
 * or context in which they are used.
 * </p>
 * <p>
 * The {@link #getApplicableClasses()} method returns an array of {@link ApplicableClass} objects, indicating
 * the classes for which this action is valid. This allows dynamic filtering and handling of actions depending
 * on the runtime type of the target.
 * </p>
 */
public interface ClassAction extends Action {

    /**
     * Returns the array of {@link ApplicableClass} objects that this action is applicable to.
     * <p>
     * The returned array should contain all classes for which this action can be executed. If the array is empty,
     * the action may be considered applicable to all classes, depending on the implementation.
     * </p>
     *
     * @return an array of applicable classes
     */
    ApplicableClass[] getApplicableClasses();
}
