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


import java.util.Locale;

/**
 * SystemLocaleProvider is an implementation of {@link LocaleProvider} that provides the system default locale.
 * <p>
 * This provider always returns the default {@link Locale} of the JVM, regardless of context.
 * <p>
 * The priority for this provider is set to 1000, making it a fallback when other providers are not available.
 *
 * @author Mario
 */

public class SystemLocaleProvider implements LocaleProvider {

    /**
     * Returns the priority of this provider. Higher values indicate higher priority.
     *
     * @return the priority value (1000)
     */
    @Override
    public int getPriority() {
        return 1000;
    }

    /**
     * Returns the default {@link Locale} of the JVM.
     * <p>
     * This method always returns {@link Locale#getDefault()}.
     *
     * @return the system default Locale
     */
    @Override
    public Locale getDefaultLocale() {
        return Locale.getDefault();
    }

}
