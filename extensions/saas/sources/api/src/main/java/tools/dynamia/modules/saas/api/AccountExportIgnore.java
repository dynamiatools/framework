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
 * Marks a JPA {@code @Entity} class to be completely excluded from the Tenant Mobility
 * export/import/clone pipeline.
 *
 * <p>Apply this annotation to entities that contain ephemeral, audit, cache or metric data
 * that should never travel with a tenant:
 *
 * <pre>{@code
 * @Entity
 * @AccountExportIgnore
 * public class LoginAuditLog extends SimpleEntitySaaS {
 *     ...
 * }
 * }</pre>
 *
 * <p>Entities annotated with {@code @AccountExportIgnore} are silently skipped during
 * discovery, so they will never appear in an export file and will never be processed
 * on import.
 *
 * @author Mario Serrano Leones
 * @see ExportIgnore
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AccountExportIgnore {
}

