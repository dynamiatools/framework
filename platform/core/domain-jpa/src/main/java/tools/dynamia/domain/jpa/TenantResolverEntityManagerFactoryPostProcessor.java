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
package tools.dynamia.domain.jpa;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Component;

/**
 * Gives every {@link LocalContainerEntityManagerFactoryBean} a Hibernate tenant identifier resolver, also when Spring
 * Boot (and not {@link JpaConfigurationAdapter}) builds it.
 * <p>
 * Entities annotated with {@code @TenantId} (the security entities, the SaaS bases, reports) need one, otherwise
 * Hibernate refuses to open a session ("configured for multi-tenancy, but no tenant identifier specified"). The
 * application's own {@link CurrentTenantIdentifierResolver} bean is used when there is one (SaaS publishes it);
 * otherwise {@link RootTenantIdentifierResolver}, so a single tenant application works without extra configuration.
 * A resolver already set in the factory's properties is never replaced.
 *
 * @author Mario Serrano Leones
 */
@Component
public class TenantResolverEntityManagerFactoryPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof LocalContainerEntityManagerFactoryBean factory
                && !factory.getJpaPropertyMap().containsKey(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER)) {
            CurrentTenantIdentifierResolver<?> resolver = applicationContext == null ? null
                    : applicationContext.getBeanProvider(CurrentTenantIdentifierResolver.class).orderedStream().findFirst().orElse(null);
            factory.getJpaPropertyMap().put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER,
                    resolver != null ? resolver : new RootTenantIdentifierResolver());
        }
        return bean;
    }
}
