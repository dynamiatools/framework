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
 * Tells an action which platform it is running on and what that platform can do, in the style of Capacitor's
 * {@code getPlatform()} and {@code isPluginAvailable()}.
 *
 * <pre>{@code
 * if (UIPlatform.supports(ChoicesProvider.class)) {
 *     UIChoices.chooseOne(...);
 * }
 * String where = UIPlatform.current().name(); // "zk", "replay", "none", "test"
 * }</pre>
 */
public final class UIPlatform {

    private UIPlatform() {
    }

    /**
     * @return the environment active for the code running on this thread
     */
    public static UIEnvironment current() {
        return UIFacades.current();
    }

    /**
     * @param spi a port SPI, such as {@code FileTransfer.class}
     * @return whether the current environment implements it
     */
    public static boolean supports(Class<?> spi) {
        return current().supports(spi);
    }
}
