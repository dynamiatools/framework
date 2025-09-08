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

/**
 * Represents a global action that is available throughout the entire application.
 * <p>
 * Subclasses of {@code ApplicationGlobalAction} define actions that are not limited to a specific context or module,
 * but can be accessed and executed from anywhere in the application. These actions are typically used for
 * application-wide operations such as global shortcuts, settings, or utilities.
 * </p>
 * <p>
 * This abstract class extends {@link AbstractAction}, inheriting its properties and behavior, and serves as a base
 * for implementing custom global actions.
 * </p>
 */
public abstract class ApplicationGlobalAction extends AbstractAction {

    // No additional methods or fields. Subclasses should implement specific global actions.
}
