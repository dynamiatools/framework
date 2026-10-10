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

import java.util.Optional;

/**
 * Where an action is running from the point of view of the UI: it groups the implementation of every {@link UIPort}
 * for one kind of front end. ZK has one, a replayed (headless) action has one per pass, code that runs without a user
 * (jobs, background threads) has {@link NoUIEnvironment}, and tests bring their own.
 * <p>
 * Facades never look for an SPI on their own; they ask the active environment through
 * {@link UIFacades#port(Class)}. See {@link UIFacades} for how the active environment is chosen.
 */
public interface        UIEnvironment {

    /**
     * @return short name of the environment: {@code "zk"}, {@code "replay"}, {@code "none"}, {@code "test"}...
     */
    String name();

    /**
     * @param spi the port SPI
     * @param <S> the SPI type
     * @return the implementation of {@code spi} in this environment, or empty when the environment does not support it
     */
    <S> Optional<S> port(Class<S> spi);

    /**
     * @param spi the port SPI
     * @return whether this environment implements {@code spi}
     */
    default boolean supports(Class<?> spi) {
        return port(spi).isPresent();
    }
}
