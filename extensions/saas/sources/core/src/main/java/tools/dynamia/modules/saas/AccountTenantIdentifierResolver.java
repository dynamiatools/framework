
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
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.modules.saas.api.AccountServiceAPI;
import tools.dynamia.web.util.HttpUtils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Tells Hibernate which account is the current tenant, so entities that extend the SaaS base classes
 * ({@code SimpleEntitySaaS} and friends, whose {@code accountId} is a {@code @TenantId}) are isolated by account in
 * every query and load, not only in {@code CrudService} queries.
 * <p>
 * The tenant is resolved, in this order, from {@link AccountTenants#runAs(Long, java.util.function.Supplier)}, the
 * current request attribute and the {@link AccountSessionHolder}. It never touches the database, because Hibernate
 * calls it while opening a session. It <strong>fails closed</strong>: when no account can be resolved (startup,
 * background jobs, a thread that lost its context) it answers {@link AccountTenants#NO_TENANT_ID}, a tenant that does
 * not exist, so nothing is visible. The {@link AccountTenants#ROOT_TENANT_ID root} tenant, which sees every account,
 * is only used inside an explicit {@code AccountTenants.withRoot/runAsRoot/callWithRoot}.
 *
 * @author Mario Serrano Leones
 */
@Component
public class AccountTenantIdentifierResolver implements CurrentTenantIdentifierResolver<Long> {

    private static final LoggingService LOGGER = new SLF4JLoggingService(AccountTenantIdentifierResolver.class);
    private static final long WARN_INTERVAL_MILLIS = 60_000;
    private static final AtomicLong lastWarning = new AtomicLong();

    @Override
    public Long resolveCurrentTenantIdentifier() {
        Long id = effectiveTenantId();
        if (id != null) {
            return id;
        }
        warnNoTenant();
        return AccountTenants.NO_TENANT_ID;
    }

    /**
     * Resolves the tenant of the code that is running now: the one bound with {@link AccountTenants}, else the
     * current request attribute, else the {@link AccountSessionHolder}. It never answers the
     * {@link AccountTenants#NO_TENANT_ID} sentinel.
     *
     * @return the tenant id, or {@code null} when none can be resolved
     */
    static Long effectiveTenantId() {
        Long forced = AccountTenants.forcedTenantId();
        if (forced != null) {
            return forced;
        }

        Long id = fromRequest();
        if (id == null) {
            id = fromSessionHolder();
        }
        return id;
    }

    private static void warnNoTenant() {
        long now = System.currentTimeMillis();
        long last = lastWarning.get();
        if (now - last >= WARN_INTERVAL_MILLIS && lastWarning.compareAndSet(last, now)) {
            LOGGER.warn("No tenant could be resolved for this session: using tenant " + AccountTenants.NO_TENANT_ID
                    + ", which sees no data. Bind one with AccountTenants.runAs/with, or AccountTenants.withRoot for "
                    + "cross-account work (warning repeated at most once a minute).");
        }
    }

    private static Long fromRequest() {
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

    private static Long fromSessionHolder() {
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
