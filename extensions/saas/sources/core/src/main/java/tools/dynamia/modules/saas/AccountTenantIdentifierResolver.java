
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

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;
import tools.dynamia.modules.saas.api.AccountServiceAPI;
import tools.dynamia.web.util.HttpUtils;

/**
 * Tells Hibernate which account is the current tenant, so entities that extend the SaaS base classes
 * ({@code SimpleEntitySaaS} and friends, whose {@code accountId} is a {@code @TenantId}) are isolated by account in
 * every query and load, not only in {@code CrudService} queries.
 * <p>
 * The tenant is resolved, in this order, from {@link AccountTenants#runAs(Long, java.util.function.Supplier)}, the
 * current request attribute and the {@link AccountSessionHolder}. It never touches the database, because Hibernate
 * calls it while opening a session. When there is no current account (startup, background jobs) it answers the
 * {@link AccountTenants#ROOT_TENANT_ID root} tenant, which sees every account: the same behaviour as the
 * {@code AccountAwareCrudServiceListener}, which does not filter either when there is no account.
 *
 * @author Mario Serrano Leones
 */
@Component
public class AccountTenantIdentifierResolver implements CurrentTenantIdentifierResolver<Long> {

    @Override
    public Long resolveCurrentTenantIdentifier() {
        Long forced = AccountTenants.getOverride();
        if (forced != null) {
            return forced;
        }

        Long id = fromRequest();
        if (id == null) {
            id = fromSessionHolder();
        }
        return id != null ? id : AccountTenants.ROOT_TENANT_ID;
    }

    private Long fromRequest() {
        try {
            if (HttpUtils.isInWebScope()) {
                var request = HttpUtils.getCurrentRequest();
                if (request != null) {
                    return (Long) request.getAttribute(AccountServiceAPI.CURRENT_ACCOUNT_ID_ATTRIBUTE);
                }
            }
        } catch (Exception e) {
            // no request in this thread
        }
        return null;
    }

    private Long fromSessionHolder() {
        try {
            return AccountSessionHolder.get().getId();
        } catch (Exception e) {
            // no session holder in this thread
            return null;
        }
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }

    @Override
    public boolean isRoot(Long tenantId) {
        return AccountTenants.ROOT_TENANT_ID.equals(tenantId);
    }
}
