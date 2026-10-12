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

/**
 * Contributes a {@link UIEnvironment} that is active only while some condition holds. It is registered as a bean (ZK
 * registers one that is active while a ZK {@code Execution} exists) and consulted by {@link UIFacades#current()} when
 * no environment is bound explicitly.
 */
public interface UIEnvironmentProvider {

    /**
     * @return whether the code running on this thread is inside the environment of this provider
     */
    boolean isActive();

    /**
     * @return the environment to use while {@link #isActive()} is true
     */
    UIEnvironment environment();
}
