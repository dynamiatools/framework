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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark an {@link Action} as the primary action in a context or group.
 * <p>
 * Use this annotation on an action class to indicate that it should be treated as the main or default action
 * among a set of actions. Primary actions are typically highlighted in the UI, given priority in execution,
 * or used as the default option in dialogs and toolbars.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 *     @PrimaryAction
 *     public class SaveAction implements Action {
 *         // Implementation details
 *     }
 * </pre>
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
@Target(ElementType.TYPE)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface PrimaryAction {
}
