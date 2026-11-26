
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.modules.saas.api.AccountServiceAPI;
import tools.dynamia.web.util.HttpUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

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
 * <p>
 * What happens when no account can be resolved is the {@link NoTenantPolicy}, set with the
 * {@value #NO_TENANT_POLICY_PROPERTY} property: {@code closed} (the default, described above), {@code strict} (throws
 * {@link NoTenantException} so tests find the code that forgot to bind an account) or {@code root} (a temporary
 * bridge that runs as root). Each call site that reaches this point is logged.
 * <p>
 * Which id is the root is configured with the {@value #ROOT_TENANT_PROPERTY} property (default
 * {@link AccountTenants#ROOT_TENANT_ID}). It is a plain value on purpose: Hibernate asks {@link #isRoot(Long)} while
 * opening sessions, so it cannot be looked up in the database (for example the system account).
 *
 * @author Mario Serrano Leones
 */
@Component
public class AccountTenantIdentifierResolver implements CurrentTenantIdentifierResolver<Long> {

    private static final LoggingService LOGGER = new SLF4JLoggingService(AccountTenantIdentifierResolver.class);
    private static final int MAX_CALL_SITES = 500;
    private static final String[] FRAMEWORK_PACKAGES = {"tools.dynamia.", "org.hibernate.", "org.springframework.",
            "jakarta.", "java.", "javax.", "jdk.", "sun.", "com.sun.", "org.apache.", "com.zaxxer."};
    private static final Map<String, LongAdder> CALL_SITES = new ConcurrentHashMap<>();

    /**
     * Property that sets the id of the root tenant.
     */
    public static final String ROOT_TENANT_PROPERTY = "dynamia.saas.root-tenant-id";

    /**
     * Property that sets the {@link NoTenantPolicy}: {@code closed} (default), {@code strict} or {@code root}.
     */
    public static final String NO_TENANT_POLICY_PROPERTY = "dynamia.saas.no-tenant-policy";

    private final Long rootTenantId;
    private volatile NoTenantPolicy policy;
    private volatile boolean ready;

    /**
     * Creates the resolver with the default root tenant, {@link AccountTenants#ROOT_TENANT_ID}.
     */
    public AccountTenantIdentifierResolver() {
        this(AccountTenants.ROOT_TENANT_ID);
    }

    /**
     * Creates the resolver with the {@link NoTenantPolicy#CLOSED} policy and makes {@code rootTenantId} the root
     * tenant of {@link AccountTenants}.
     *
     * @param rootTenantId the root tenant id, taken from {@value #ROOT_TENANT_PROPERTY}
     * @throws IllegalArgumentException if it is {@code null} or equals {@link AccountTenants#NO_TENANT_ID}
     */
    public AccountTenantIdentifierResolver(Long rootTenantId) {
        this(rootTenantId, NoTenantPolicy.CLOSED.name());
    }

    /**
     * Creates the resolver, makes {@code rootTenantId} the root tenant of {@link AccountTenants} and sets what to do
     * when no tenant can be resolved.
     *
     * @param rootTenantId the root tenant id, taken from {@value #ROOT_TENANT_PROPERTY}
     * @param noTenantPolicy the policy, taken from {@value #NO_TENANT_POLICY_PROPERTY}
     * @throws IllegalArgumentException if the root id is {@code null} or equals {@link AccountTenants#NO_TENANT_ID},
     *                                  or the policy is not {@code closed}, {@code strict} or {@code root}
     */
    @Autowired
    public AccountTenantIdentifierResolver(@Value("${" + ROOT_TENANT_PROPERTY + ":0}") Long rootTenantId,
                                           @Value("${" + NO_TENANT_POLICY_PROPERTY + ":closed}") String noTenantPolicy) {
        if (rootTenantId == null || AccountTenants.NO_TENANT_ID.equals(rootTenantId)) {
            throw new IllegalArgumentException(ROOT_TENANT_PROPERTY + " cannot be empty or " + AccountTenants.NO_TENANT_ID
                    + ", the tenant used when none can be resolved");
        }
        this.rootTenantId = rootTenantId;
        this.policy = NoTenantPolicy.parse(noTenantPolicy);
        AccountTenants.configureRootTenantId(rootTenantId);
    }

    /**
     * Returns what happens when no tenant can be resolved.
     *
     * @return the current policy
     */
    public NoTenantPolicy getNoTenantPolicy() {
        return policy;
    }

    /**
     * Changes the policy at runtime. Meant for tests that need a different one than the application (for example a
     * test that asserts the closed behaviour in an application that runs strict).
     *
     * @param noTenantPolicy the new policy
     */
    public void setNoTenantPolicy(NoTenantPolicy noTenantPolicy) {
        this.policy = noTenantPolicy == null ? NoTenantPolicy.CLOSED : noTenantPolicy;
    }

    /**
     * Arms the policy once the Spring context is up: Hibernate resolves a tenant while it starts (before any account
     * can exist), and that must never throw or run as root.
     *
     * @param event the refresh event
     */
    @EventListener(ContextRefreshedEvent.class)
    void onContextRefreshed(ContextRefreshedEvent event) {
        ready();
    }

    /**
     * Marks the application as started, so {@link NoTenantPolicy#STRICT} and {@link NoTenantPolicy#ROOT} apply.
     */
    void ready() {
        this.ready = true;
    }

    @Override
    public Long resolveCurrentTenantIdentifier() {
        Long id = effectiveTenantId();
        if (id != null) {
            return id;
        }
        return noTenant();
    }

    /**
     * Applies the {@link NoTenantPolicy} to a session opened with no account bound, and reports the call site.
     */
    private Long noTenant() {
        var effective = ready ? policy : NoTenantPolicy.CLOSED;
        var site = callSite();
        report(effective, site);
        return switch (effective) {
            case STRICT -> throw new NoTenantException("No tenant bound (" + NO_TENANT_POLICY_PROPERTY + "=strict) at "
                    + site + ". Bind an account with AccountTenants.runAs/with or AccountServiceAPI.runAsAccount, or "
                    + "AccountServiceAPI.runAsRoot for a lookup across accounts.");
            case ROOT -> rootTenantId;
            case CLOSED -> AccountTenants.NO_TENANT_ID;
        };
    }

    /**
     * Logs an unbound access, once per call site and then when its count reaches a power of ten, so a hot path does
     * not flood the log but every code that forgot to bind an account shows up.
     */
    private static void report(NoTenantPolicy effective, String site) {
        if (CALL_SITES.size() >= MAX_CALL_SITES && !CALL_SITES.containsKey(site)) {
            return;
        }
        var counter = CALL_SITES.computeIfAbsent(site, k -> new LongAdder());
        counter.increment();
        long count = counter.sum();
        if (!isPowerOfTen(count)) {
            return;
        }
        var outcome = switch (effective) {
            case CLOSED -> "using tenant " + AccountTenants.NO_TENANT_ID + ", which sees no data";
            case STRICT -> "throwing NoTenantException";
            case ROOT -> "running as the root tenant, which sees every account";
        };
        LOGGER.warn("No tenant could be resolved for this session at " + site + " (" + count + " time(s), "
                + NO_TENANT_POLICY_PROPERTY + "=" + effective.name().toLowerCase() + "): " + outcome
                + ". Bind one with AccountTenants.runAs/with or AccountServiceAPI.runAsAccount, or "
                + "AccountServiceAPI.runAsRoot for a lookup across accounts.");
    }

    private static boolean isPowerOfTen(long count) {
        while (count >= 10 && count % 10 == 0) {
            count /= 10;
        }
        return count == 1;
    }

    /**
     * The first class outside the framework in the stack, which is the code that opened the session without binding
     * an account; the first frames are shown when everything is framework code.
     */
    private static String callSite() {
        return StackWalker.getInstance().walk(frames -> {
            var all = frames.toList();
            return all.stream()
                    .filter(f -> !isFramework(f.getClassName()))
                    .findFirst()
                    .map(f -> f.getClassName() + "." + f.getMethodName() + "(" + f.getFileName() + ":" + f.getLineNumber() + ")")
                    .orElseGet(() -> "framework code only [" + all.stream().skip(2).limit(4)
                            .map(f -> f.getClassName() + "." + f.getMethodName()).collect(Collectors.joining(" <- ")) + "]");
        });
    }

    private static boolean isFramework(String className) {
        if (className.equals(AccountTenantIdentifierResolver.class.getName())) {
            return true;
        }
        for (var prefix : FRAMEWORK_PACKAGES) {
            if (className.startsWith(prefix)) {
                return true;
            }
        }
        return false;
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
        return rootTenantId.equals(tenantId);
    }
}
