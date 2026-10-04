
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

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import tools.dynamia.integration.Containers;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Runs work as a given account (tenant), or as the <em>root</em> tenant that sees every account, when Hibernate
 * multi-tenancy is active (see {@link AccountTenantIdentifierResolver}).
 * <p>
 * Hibernate fixes the tenant when a session is opened. There are two flavours:
 * <ul>
 *   <li>{@code runAs} / {@code runAsRoot} run the work in its own transaction ({@code REQUIRES_NEW}, which opens a
 *   new session), so it does not share the caller's transaction or persistence context. Use them for short units of
 *   work that touch another account's data: account initializers, administration.</li>
 *   <li>{@code with} / {@code withRoot} only bind the tenant to the current thread, without a transaction, for
 *   code that manages its own sessions and transactions (long running jobs such as account migration). The binding
 *   is per thread: code that hands work to other threads must bind the tenant again inside them.</li>
 * </ul>
 *
 * <pre>{@code
 * AccountTenants.runAs(newAccountId, () -> crudService.create(new Customer("default")));
 * long total = AccountTenants.runAsRoot(() -> crudService.count(Invoice.class));
 * }</pre>
 *
 * @author Mario Serrano Leones
 */
public final class AccountTenants {

    /**
     * Tenant id of the root tenant: it is not an account, and sees and may write every account's data.
     */
    public static final Long ROOT_TENANT_ID = 0L;

    private static final ThreadLocal<Long> OVERRIDE = new ThreadLocal<>();

    private AccountTenants() {
    }

    /**
     * Returns the tenant forced by {@code runAs}, {@code runAsRoot}, {@code with}, {@code withRoot} or {@code callWithRoot} on this thread.
     *
     * @return the forced tenant id, or {@code null} when none is forced
     */
    public static Long forcedTenantId() {
        return OVERRIDE.get();
    }

    /**
     * Runs the work as the given account, in a new transaction.
     *
     * @param accountId the account id
     * @param work      the work
     * @param <T>       the result type
     * @return the result of the work
     */
    public static <T> T runAs(Long accountId, Supplier<T> work) {
        if (accountId == null) {
            throw new IllegalArgumentException("accountId is required; use runAsRoot to run for every account");
        }
        Long previous = OVERRIDE.get();
        OVERRIDE.set(accountId);
        try {
            return inNewTransaction(work);
        } finally {
            restore(previous);
        }
    }

    /**
     * Runs the work as the given account, in a new transaction.
     *
     * @param accountId the account id
     * @param work      the work
     */
    public static void runAs(Long accountId, Runnable work) {
        runAs(accountId, () -> {
            work.run();
            return null;
        });
    }

    /**
     * Runs the work as the root tenant: it sees every account and may persist data for any account.
     * Entities persisted without an {@code accountId} get the {@link #ROOT_TENANT_ID}.
     *
     * @param work the work
     * @param <T>  the result type
     * @return the result of the work
     */
    public static <T> T runAsRoot(Supplier<T> work) {
        Long previous = OVERRIDE.get();
        OVERRIDE.set(ROOT_TENANT_ID);
        try {
            return inNewTransaction(work);
        } finally {
            restore(previous);
        }
    }

    /**
     * Runs the work as the root tenant, in a new transaction.
     *
     * @param work the work
     */
    public static void runAsRoot(Runnable work) {
        runAsRoot(() -> {
            work.run();
            return null;
        });
    }

    /**
     * Binds the given account as the tenant of the current thread while the work runs, without opening a
     * transaction. Sessions opened by the work (also by {@code EntityManagerFactory.createEntityManager()}) use it.
     *
     * @param accountId the account id
     * @param work      the work
     * @param <T>       the result type
     * @return the result of the work
     */
    public static <T> T with(Long accountId, Supplier<T> work) {
        if (accountId == null) {
            throw new IllegalArgumentException("accountId is required; use withRoot to run for every account");
        }
        return bind(accountId, work);
    }

    /**
     * Binds the root tenant to the current thread while the work runs, without opening a transaction.
     *
     * @param work the work
     * @param <T>  the result type
     * @return the result of the work
     */
    public static <T> T withRoot(Supplier<T> work) {
        return bind(ROOT_TENANT_ID, work);
    }

    /**
     * Binds the root tenant to the current thread while the work runs, without opening a transaction.
     *
     * @param work the work
     */
    public static void withRoot(Runnable work) {
        withRoot(() -> {
            work.run();
            return null;
        });
    }

    /**
     * Same as {@link #withRoot(Supplier)} for work that throws checked exceptions, such as a task submitted to an
     * executor: bind the root tenant inside the task, because the worker thread does not inherit it.
     *
     * @param work the work
     * @param <T>  the result type
     * @return the result of the work
     * @throws Exception whatever the work throws
     */
    public static <T> T callWithRoot(Callable<T> work) throws Exception {
        Long previous = OVERRIDE.get();
        OVERRIDE.set(ROOT_TENANT_ID);
        try {
            return work.call();
        } finally {
            restore(previous);
        }
    }

    private static <T> T bind(Long tenantId, Supplier<T> work) {
        Long previous = OVERRIDE.get();
        OVERRIDE.set(tenantId);
        try {
            return work.get();
        } finally {
            restore(previous);
        }
    }

    private static void restore(Long previous) {
        if (previous == null) {
            OVERRIDE.remove();
        } else {
            OVERRIDE.set(previous);
        }
    }

    private static <T> T inNewTransaction(Supplier<T> work) {
        PlatformTransactionManager txManager = Containers.get().findObject(PlatformTransactionManager.class);
        if (txManager == null) {
            return work.get();
        }
        var template = new TransactionTemplate(txManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return template.execute(status -> work.get());
    }
}
