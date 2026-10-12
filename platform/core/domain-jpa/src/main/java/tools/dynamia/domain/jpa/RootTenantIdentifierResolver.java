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

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

/**
 * Default Hibernate tenant resolver, used by {@link JpaConfigurationAdapter} only when the application publishes no
 * {@link CurrentTenantIdentifierResolver} bean of its own. It always answers the <em>root</em> tenant, which Hibernate
 * does not filter by, so entities annotated with {@code @TenantId} (for example the SaaS extension's base entities, or
 * the security entities) keep working in an application that is not multi-tenant: every row is visible, and rows
 * persisted without a tenant value get {@link #ROOT_TENANT_ID}.
 *
 * @author Mario Serrano Leones
 */
public class RootTenantIdentifierResolver implements CurrentTenantIdentifierResolver<Long> {

    /**
     * The id of the root tenant.
     */
    public static final Long ROOT_TENANT_ID = 0L;

    @Override
    public Long resolveCurrentTenantIdentifier() {
        return ROOT_TENANT_ID;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }

    @Override
    public boolean isRoot(Long tenantId) {
        return ROOT_TENANT_ID.equals(tenantId);
    }
}
