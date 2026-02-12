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

import java.lang.annotation.*;

/**
 * Annotation to mark an {@link Action} as optional within a context or group.
 * <p>
 * Use this annotation on an action class to indicate that it is not required or mandatory in a set of actions.
 * Optional actions may be displayed differently in the UI, enabled/disabled based on context, or used for
 * secondary operations that complement primary actions.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 *     @OptionalAction
 *     public class ExportAction implements Action {
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
public @interface OptionalAction {
}
