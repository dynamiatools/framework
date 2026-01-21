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

package tools.dynamia.app;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables only the API and core infrastructure of DynamiaTools without loading the full framework (UI, navigation, modules, etc.).
 *
 * <p>
 * Use this annotation when you need access to DynamiaTools APIs, services, and infrastructure components,
 * but don't require the complete application framework with ZK UI, navigation system, or automatic module loading.
 * This is useful for:
 * </p>
 * <ul>
 *   <li>REST API applications</li>
 *   <li>Backend services</li>
 *   <li>Microservices that only need DynamiaTools utilities</li>
 *   <li>Custom integrations requiring minimal DynamiaTools infrastructure</li>
 * </ul>
 *
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 * @EnableDynamiaToolsApi
 * @SpringBootApplication
 * public class MyApiApplication {
 *     public static void main(String[] args) {
 *         SpringApplication.run(MyApiApplication.class, args);
 *     }
 * }
 * }</pre>
 * </p>
 *
 * <p>
 * This annotation imports {@link DynamiaBaseConfiguration}, which registers only the essential beans
 * and services without the full web UI and navigation infrastructure.
 * </p>
 *
 * @see DynamiaBaseConfiguration
 * @author Dynamia Soluciones IT S.A.S
 * @since 2023
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(DynamiaBaseConfiguration.class)
public @interface EnableDynamiaToolsApi {
}
