
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

package tools.dynamia.modules.saas.api;

import tools.dynamia.commons.LocalizedMessagesProvider;
import tools.dynamia.commons.Messages;
import tools.dynamia.integration.Containers;
import tools.dynamia.modules.saas.api.dto.AccountDTO;

import java.util.Comparator;
import java.util.Locale;

/**
 * Interface for components that perform initialization tasks when a new account is created in a SaaS environment.
 * <p>
 * Implementations of this interface are automatically discovered and executed during account creation,
 * allowing modules to set up initial data, configurations, or resources for new accounts.
 * Multiple initializers can be chained together, with execution order controlled by priority.
 * <p>
 * Common use cases include:
 * <ul>
 *   <li>Creating default entities or master data</li>
 *   <li>Setting up initial configurations</li>
 *   <li>Creating default user roles</li>
 *   <li>Initializing account-specific resources</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * @Component
 * public class DefaultRolesInitializer implements AccountInitializer {
 *     @Override
 *     public void init(AccountDTO accountDTO) {
 *         // Create default roles for the new account
 *         createRole(accountDTO, "ADMIN");
 *         createRole(accountDTO, "USER");
 *     }
 *
 *     @Override
 *     public int getPriority() {
 *         return 100; // Execute after basic setup
 *     }
 * }
 * }</pre>
 *
 * @author Mario Serrano Leones
 */
public interface AccountInitializer {

    /**
     * Performs initialization tasks for a newly created account.
     * <p>
     * This method is called automatically after an account is created and persisted.
     * Implementations should create any necessary initial data or configurations
     * required for the account to function properly.
     *
     * @param accountDTO the newly created account's data transfer object
     */
    void init(AccountDTO accountDTO);

    /**
     * Defines the execution priority for this initializer.
     * <p>
     * When multiple initializers exist, they are executed in order from lowest to highest priority value.
     * Lower values indicate higher priority and will execute first.
     *
     * @return the priority value (default is 0, lower values execute first)
     */
    default int getPriority() {
        return 0;
    }

    /**
     * Retrieves a localized message for the account's configured locale.
     * <p>
     * This helper method simplifies obtaining localized strings during account initialization,
     * using the account's locale setting if available, or falling back to the system default.
     *
     * @param key the message key to retrieve
     * @param accountDTO the account containing locale information
     * @return the localized message string, or the key itself if no translation is found
     */
    default String localizedMessage(String key, AccountDTO accountDTO) {
        try {
            var provider = Containers.get().findObjects(LocalizedMessagesProvider.class)
                    .stream().min(Comparator.comparingInt(LocalizedMessagesProvider::getPriority))
                    .orElse(null);

            if (provider != null) {
                var locale = accountDTO.getLocale() != null ? Locale.forLanguageTag(accountDTO.getLocale()) : Messages.getDefaultLocale();
                if (locale != null) {
                    return provider.getMessage(key, "* " + getClass().getSimpleName(), locale, key);
                } else {
                    return key;
                }
            } else {
                return key;
            }
        } catch (Exception e) {
            System.err.println("Error loading localized message " + e);
            e.printStackTrace();
            return key;
        }
    }

}
