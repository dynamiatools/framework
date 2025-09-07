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

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as an installable {@link Action} in the application context.
 * <p>
 * Classes annotated with {@code InstallAction} are automatically registered as Spring components
 * with prototype scope, allowing them to be instantiated and managed by the framework as actions.
 * This annotation is typically used to facilitate the dynamic discovery and installation of actions
 * in modular or plugin-based systems.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 *     @InstallAction
 *     public class MyCustomAction implements Action {
 *         // Implementation details
 *     }
 * </pre>
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
@Target(ElementType.TYPE)
@Component
@Scope("prototype")
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface InstallAction {
}
