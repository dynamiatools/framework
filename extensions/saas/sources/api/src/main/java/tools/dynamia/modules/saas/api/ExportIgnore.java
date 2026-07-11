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
package tools.dynamia.modules.saas.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a specific field in a JPA entity to be excluded from Tenant Mobility
 * serialization during export.
 *
 * <p>Use this for fields that are computed, cached, or otherwise should not be
 * persisted to a different environment:
 *
 * <pre>{@code
 * @Entity
 * public class Customer extends SimpleEntitySaaS {
 *
 *     private String name;
 *
 *     @ExportIgnore
 *     private transient String cachedFullName;   // computed
 *
 *     @ExportIgnore
 *     private String internalToken;              // environment-specific secret
 * }
 * }</pre>
 *
 * <p>Fields with this annotation are silently skipped during entity serialization.
 * On import, those fields will retain their default (null / primitive default) values.
 *
 * @author Mario Serrano Leones
 * @see AccountExportIgnore
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExportIgnore {
}

