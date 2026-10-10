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

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Finds the implementation of a UI port for the environment the current code runs in.
 * <p>
 * Actions talk to the user through static facades such as {@link UIMessages}. Each facade is backed by an SPI (for
 * {@code UIMessages}, {@link MessageDisplayer}) and every {@link UIEnvironment} knows its implementation of each SPI it
 * supports. The environment is chosen in this order:
 * <ol>
 *     <li>the one bound to the current execution with {@link #with(UIEnvironment, Supplier)} (a replayed action, a
 *     test);</li>
 *     <li>otherwise the first {@link UIEnvironmentProvider} in the container that {@linkplain
 *     UIEnvironmentProvider#isActive() is active} (ZK, while a ZK execution exists);</li>
 *     <li>otherwise {@link NoUIEnvironment}.</li>
 * </ol>
 * The binding is a {@link ScopedValue}: it is visible to the code run inside {@code with(...)} (and the threads it
 * forks with structured concurrency), never leaks to other executions, and needs no cleanup.
 * <p>
 * To add a facade: define the SPI interface annotated with {@link UIPort}, write the facade with a static method that
 * calls {@code UIFacades.port(Spi.class)}, and make each environment that supports it answer for the SPI in
 * {@link UIEnvironment#port(Class)}. Actions only ever see the facade.
 */
public final class UIFacades {

    private static final ScopedValue<UIEnvironment> BOUND = ScopedValue.newInstance();

    private UIFacades() {
    }

    /**
     * @return the scoped value that holds the bound environment, for the capturer that carries context into tasks
     */
    static ScopedValue<UIEnvironment> environmentBinding() {
        return BOUND;
    }

    /**
     * Runs {@code work} with {@code environment} as the active one.
     *
     * @param environment the environment for this execution, not null
     * @param work        the code to run
     * @param <R>         result type
     * @return what {@code work} returns
     */
    public static <R> R with(UIEnvironment environment, Supplier<R> work) {
        return ScopedValue.where(BOUND, environment).call(work::get);
    }

    /**
     * Runs {@code work} with {@code implementation} as the implementation of {@code spi}, on top of whatever environment
     * is active now. Ports of other SPIs keep coming from that environment. It is the light way to replace one port,
     * mostly in tests.
     *
     * @param spi            the SPI type
     * @param implementation the implementation to use, not null
     * @param work           the code to run
     * @param <S>            SPI type
     * @param <R>            result type
     * @return what {@code work} returns
     */
    public static <S, R> R with(Class<S> spi, S implementation, Supplier<R> work) {
        return with(new Overlay(current(), spi, implementation), work);
    }

    /**
     * @return the environment active for the code running on this thread, never null
     */
    public static UIEnvironment current() {
        if (BOUND.isBound()) {
            return BOUND.get();
        }
        Collection<UIEnvironmentProvider> providers = Containers.get().findObjects(UIEnvironmentProvider.class);
        if (providers != null) {
            for (UIEnvironmentProvider provider : providers) {
                if (provider.isActive()) {
                    return provider.environment();
                }
            }
        }
        return NoUIEnvironment.INSTANCE;
    }

    /**
     * @param spi the SPI type
     * @param <S> the SPI type
     * @return the implementation of {@code spi} in the active environment
     * @throws UIUnavailableException when the active environment does not support it
     */
    public static <S> S port(Class<S> spi) {
        UIEnvironment environment = current();
        return environment.port(spi).orElseThrow(() -> new UIUnavailableException(portName(spi), environment.name()));
    }

    /**
     * Looks for an optional service the same way: the active environment first, then the container. Meant for helpers
     * that are not {@link UIPort}s.
     *
     * @param spi the service type
     * @param <S> the service type
     * @return the implementation, or {@code null} when there is none
     */
    public static <S> S find(Class<S> spi) {
        Optional<S> fromEnvironment = current().port(spi);
        return fromEnvironment.orElseGet(() -> Containers.get().findObject(spi));
    }

    private static String portName(Class<?> spi) {
        UIPort port = spi.getAnnotation(UIPort.class);
        return port != null ? port.name() : spi.getSimpleName();
    }

    /** One port replaced on top of a base environment. */
    private record Overlay(UIEnvironment base, Class<?> spi, Object implementation) implements UIEnvironment {

        @Override
        public String name() {
            return base.name();
        }

        @Override
        public <S> Optional<S> port(Class<S> requested) {
            if (requested == spi) {
                return Optional.of(requested.cast(implementation));
            }
            return base.port(requested);
        }
    }
}
