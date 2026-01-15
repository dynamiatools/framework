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

import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Default Spring configuration for the SaaS Account API module.
 * <p>
 * This configuration class sets up the essential beans required for multi-tenant
 * account management, including the custom account scope and a fallback no-op
 * implementation of {@link AccountServiceAPI}.
 * <p>
 * The configuration is automatically loaded when the module is present in the classpath,
 * and provides sensible defaults that can be overridden by custom implementations.
 *
 * @author Mario Serrano Leones
 */
@Configuration
public class AccountAPIConfig {

    /**
     * Registers the custom "account" scope with the Spring container.
     * <p>
     * This bean configures the {@link AccountScope} which enables beans to be scoped
     * at the account level, ensuring proper isolation in multi-tenant environments.
     * Once registered, beans can use {@code @Scope("account")} to be managed at the account level.
     *
     * @return a {@link CustomScopeConfigurer} with the account scope registered
     * @see AccountScope
     */
    @Bean
    public CustomScopeConfigurer accountScopeConfigurer() {
        CustomScopeConfigurer configurer = new CustomScopeConfigurer();
        configurer.addScope("account", new AccountScope());
        return configurer;
    }

    /**
     * Provides a no-op implementation of {@link AccountServiceAPI} as a fallback.
     * <p>
     * This bean is only created if no other {@link AccountServiceAPI} implementation
     * is found in the application context. It's primarily useful for testing scenarios
     * or when the SaaS features are not fully configured.
     *
     * @return a {@link NoOpAccountServiceAPI} instance
     * @see NoOpAccountServiceAPI
     */
    @Bean
    @ConditionalOnMissingBean(AccountServiceAPI.class)
    public AccountServiceAPI noOpAccountServiceAPI() {
        return new NoOpAccountServiceAPI();
    }
}
