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
 * <p>
 * {@code EnableDynamiaTools} is a meta-annotation for Spring Boot applications that enables and configures the DynamiaTools framework,
 * including integration with ZK (ZKoss) UI components and other core features. When placed on a configuration class, this annotation
 * automatically imports the {@link DynamiaAppConfiguration}, which sets up beans, services, and infrastructure required for DynamiaTools.
 * </p>
 *
 * <p>
 * <b>Usage:</b>
 * <pre>
 * {@code
 * @EnableDynamiaTools
 * @SpringBootApplication
 * public class MyApplication {
 *     public static void main(String[] args) {
 *         SpringApplication.run(MyApplication.class, args);
 *     }
 * }
 * }
 * </pre>
 * </p>
 *
 * <p>
 * <b>Features enabled by this annotation:</b>
 * <ul>
 *   <li>Automatic registration of DynamiaTools beans and services</li>
 *   <li>Integration with ZK UI framework for rich web applications</li>
 *   <li>Support for modular architecture, navigation, domain, reporting, and more</li>
 *   <li>Configuration of core infrastructure for DynamiaTools modules</li>
 *   <li>Enables conventions for resource loading, event handling, and application lifecycle</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>How it works:</b>
 * <ul>
 *   <li>At runtime, Spring detects this annotation on a {@code @Configuration} or {@code @SpringBootApplication} class.</li>
 *   <li>Spring imports {@link DynamiaAppConfiguration}, which registers all necessary beans and configuration for DynamiaTools.</li>
 *   <li>No additional configuration is required; simply annotate your main class.</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Requirements:</b>
 * <ul>
 *   <li>Spring Boot 2.x or higher</li>
 *   <li>DynamiaTools framework and its dependencies</li>
 *   <li>Optional: ZKoss libraries for UI integration</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Best Practices:</b>
 * <ul>
 *   <li>Place {@code @EnableDynamiaTools} on your main application class or a dedicated configuration class.</li>
 *   <li>Use in combination with other Spring Boot annotations as needed.</li>
 *   <li>Review DynamiaTools documentation for advanced configuration and module usage.</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>See Also:</b>
 * <ul>
 *   <li>{@link DynamiaAppConfiguration}</li>
 *   <li><a href="https://github.com/dynamia/tools">DynamiaTools GitHub</a></li>
 *   <li><a href="https://dynamia.tools">DynamiaTools Documentation</a></li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Annotation Details:</b>
 * <ul>
 *   <li>{@code @Retention(RetentionPolicy.RUNTIME)}: The annotation is available at runtime for Spring's reflection-based configuration.</li>
 *   <li>{@code @Target(ElementType.TYPE)}: Can be applied to classes, typically configuration or application classes.</li>
 *   <li>{@code @Import(DynamiaAppConfiguration.class)}: Imports the main configuration for DynamiaTools.</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Limitations:</b>
 * <ul>
 *   <li>This annotation does not configure ZK itself; ensure ZK dependencies are present if UI features are required.</li>
 *   <li>Additional modules may require further configuration or annotations.</li>
 * </ul>
 * </p>
 *
 * @see DynamiaAppConfiguration
 * @see org.springframework.context.annotation.Import
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 *
 * @author Dynamia Soluciones IT S.A.S
 * @since 2023
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(DynamiaAppConfiguration.class)
public @interface EnableDynamiaTools {
}
