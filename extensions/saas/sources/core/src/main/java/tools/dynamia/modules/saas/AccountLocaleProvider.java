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

package tools.dynamia.modules.saas;

import tools.dynamia.commons.LocaleProvider;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.integration.sterotypes.Provider;

import java.util.Locale;

/**
 * AccountLocaleProvider is an implementation of {@link LocaleProvider} for SaaS environments.
 * <p>
 * Provides the default {@link Locale} based on the current account session context.
 * The priority for this provider is set to 10, making it suitable for account-level locale resolution.
 * <p>
 * If the account session or locale cannot be resolved, this provider returns null.
 *
 * @author Mario
 */
@Provider
public class AccountLocaleProvider implements LocaleProvider {
    /**
     * Logger for this provider, using SLF4J.
     */
    private final LoggingService logger = new SLF4JLoggingService(AccountLocaleProvider.class);

    /**
     * Returns the priority of this provider. Lower values indicate higher priority.
     *
     * @return the priority value (10)
     */
    @Override
    public int getPriority() {
        return 10;
    }

    /**
     * Returns the default {@link Locale} for the current account session.
     * <p>
     * Attempts to retrieve the account locale from the current session. If unavailable, returns null.
     *
     * @return the account's default Locale, or null if not available
     */
    @Override
    public Locale getDefaultLocale() {
        try {
            return AccountSessionHolder.get().getAccountLocale();
        } catch (Exception e) {
            return null;
        }
    }
}
