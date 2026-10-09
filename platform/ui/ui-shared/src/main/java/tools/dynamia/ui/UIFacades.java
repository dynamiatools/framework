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

import tools.dynamia.integration.Containers;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Finds the implementation of a UI service for the environment the current code runs in.
 * <p>
 * Actions talk to the user through static facades such as {@link UIMessages}. Each facade is backed by an SPI (for
 * {@code UIMessages}, {@link MessageDisplayer}) with one implementation per environment: ZK registers its own as a
 * bean, a headless run (REST client, tests) binds another one for the duration of an execution. This class is the
 * lookup they share:
 * <ol>
 *     <li>an implementation bound with {@link #with(Class, Object, Supplier)} for the current execution, if any;</li>
 *     <li>otherwise the one registered in the container.</li>
 * </ol>
 * The binding is a {@link ScopedValue}: it is visible to the code run inside {@code with(...)} (and the threads it
 * forks with structured concurrency), never leaks to other executions, and needs no cleanup.
 * <p>
 * To add a facade: define the SPI interface, write the facade with a static method that calls
 * {@code UIFacades.resolve(Spi.class)}, register the ZK implementation as a bean in the {@code zk} module and the
 * headless one wherever the runtime binds it. Actions only ever see the facade.
 */
public final class UIFacades {

    private static final ScopedValue<Map<Class<?>, Object>> BINDINGS = ScopedValue.newInstance();

    private UIFacades() {
    }

    /**
     * Runs {@code work} with {@code implementation} as the implementation of {@code spi}. Bindings of other SPIs made by
     * an enclosing call stay visible; a binding of the same SPI is replaced for the duration of {@code work}.
     *
     * @param spi            the SPI type
     * @param implementation the implementation to use, not null
     * @param work           the code to run
     * @return what {@code work} returns
     */
    public static <S, R> R with(Class<S> spi, S implementation, Supplier<R> work) {
        Map<Class<?>, Object> merged = BINDINGS.isBound() ? new HashMap<>(BINDINGS.get()) : new HashMap<>();
        merged.put(spi, implementation);
        return ScopedValue.where(BINDINGS, Map.copyOf(merged)).call(work::get);
    }

    /**
     * @param spi the SPI type
     * @return the implementation bound for the current execution, or {@code null} when none is bound (the container is
     * not consulted)
     */
    public static <S> S bound(Class<S> spi) {
        if (BINDINGS.isBound()) {
            return spi.cast(BINDINGS.get().get(spi));
        }
        return null;
    }

    /**
     * @param spi the SPI type
     * @return the bound implementation, else the one in the container, else {@code null}
     */
    public static <S> S find(Class<S> spi) {
        S bound = bound(spi);
        return bound != null ? bound : Containers.get().findObject(spi);
    }

    /**
     * @param spi the SPI type
     * @return the bound implementation, else the one in the container
     * @throws IllegalStateException when there is none
     */
    public static <S> S resolve(Class<S> spi) {
        S found = find(spi);
        if (found == null) {
            throw new IllegalStateException(spi.getSimpleName() + " not found: no implementation is bound for this execution "
                    + "and none is registered in the container");
        }
        return found;
    }
}
