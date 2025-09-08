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
package tools.dynamia.commons;

/**
 * <p>
 * Callback is a simple {@link FunctionalInterface} representing a generic callback action with no parameters and no return value.
 * It is used throughout the DynamiaTools framework to provide hooks, event handlers, or deferred execution logic.
 * </p>
 *
 * <p>
 * Typical use cases include passing actions to be executed later, implementing listeners, or providing default behaviors.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 *     Callback callback = () -> System.out.println("Action executed!");
 *     callback.doSomething();
 * </pre>
 * </p>
 *
 * <p>
 * The {@link #DO_NOTHING} constant provides a reusable no-op callback instance.
 * </p>
 *
 * @author Ing. Mario Serrano Leones
 */
@FunctionalInterface
public interface Callback {

    /**
     * A reusable no-op callback instance that performs no action when invoked.
     * Useful as a default or placeholder callback.
     */
    Callback DO_NOTHING = () -> {
        //empty body
    };

    /**
     * Executes the callback action. Implementations define the specific behavior to be performed.
     */
    void doSomething();

}
