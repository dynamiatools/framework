
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

import java.util.Locale;

/**
 * What {@link AccountTenantIdentifierResolver} does when Hibernate opens a session and no account can be resolved
 * (a public endpoint, an {@code @Async} or scheduled thread, a thread that lost its context). Chosen with the
 * {@value AccountTenantIdentifierResolver#NO_TENANT_POLICY_PROPERTY} property.
 * <p>
 * Whatever the policy, the unbound access is logged once per call site (the first class outside the framework that
 * asked for it), so the code that forgot to bind an account can be found.
 *
 * @author Mario Serrano Leones
 */
public enum NoTenantPolicy {

    /**
     * The default and the one for production: the session gets {@link AccountTenants#NO_TENANT_ID}, a tenant that does
     * not exist, so nothing is visible. Safe (no data of another account can leak), but the failure is silent.
     */
    CLOSED,

    /**
     * For tests, development and staging: an access with no tenant throws {@link NoTenantException} with the call site,
     * so it fails where it happens instead of returning nothing. It is only enforced once the Spring context has
     * refreshed, because Hibernate resolves a tenant while it starts.
     * <p>
     * Hibernate asks for the tenant every time it opens a session, before it knows which entities the work will touch.
     * So strict also throws for legitimate access with no account to entities that are not tenant-aware (for example
     * looking up an {@code Account} by its domain). Use it to find the call sites, not as an application-wide switch,
     * unless that code is wrapped.
     */
    STRICT,

    /**
     * A temporary bridge, not for production: the session runs as the {@link AccountTenants#rootTenantId() root}
     * tenant, which sees and may write every account's data. An unbound lookup can then return or modify the data of
     * any account, so use it only while the call sites reported in the log are fixed.
     */
    ROOT;

    /**
     * Reads a policy from a property value.
     *
     * @param value the value, case insensitive; {@code null} or blank means {@link #CLOSED}
     * @return the policy
     * @throws IllegalArgumentException if the value is not {@code closed}, {@code strict} or {@code root}
     */
    public static NoTenantPolicy parse(String value) {
        if (value == null || value.isBlank()) {
            return CLOSED;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(AccountTenantIdentifierResolver.NO_TENANT_POLICY_PROPERTY
                    + " must be closed, strict or root, but was '" + value + "'");
        }
    }
}
