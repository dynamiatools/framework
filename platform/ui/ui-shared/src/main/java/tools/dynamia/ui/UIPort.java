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
package tools.dynamia.ui;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an SPI as a <em>UI port</em>: a capability an action can ask the user's front end for (messages, forms, files...).
 * A port is the facade ({@link UIMessages}, {@link UIFiles}...) plus this SPI, one implementation per
 * {@link UIEnvironment}, and the protocol steps that carry it to a remote client.
 * <p>
 * The name is what {@link UIFacades#port(Class)} reports when a port is missing, and the metadata the contract
 * generator reads to produce the client types.
 *
 * <pre>{@code
 * @UIPort(name = "files", steps = {"UPLOAD"})
 * public interface FileTransfer { ... }
 * }</pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UIPort {

    /**
     * @return short name of the port, such as {@code "messages"} or {@code "files"}
     */
    String name();

    /**
     * @return names of the flow protocol steps this port produces for remote clients, if any
     */
    String[] steps() default {};
}
