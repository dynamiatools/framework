package tools.dynamia.modules.security;

import tools.dynamia.integration.Containers;
import tools.dynamia.modules.saas.api.AccountServiceAPI;

import java.util.function.Supplier;

/**
 * Runs security work that has no current account yet (login by username or token, startup defaults) with the
 * visibility it needs. Tenant resolution fails closed, so without a current account the {@code @TenantId} security
 * entities ({@code User}, {@code Profile}, {@code UserAccessToken}...) are invisible; this helper delegates to
 * {@link AccountServiceAPI#withRootIfNoAccount(Supplier)}, which binds the root tenant only in that case. When an
 * account is current (for example resolved from the subdomain) the work stays isolated to it, and without an
 * {@link AccountServiceAPI} (no SaaS) the work just runs.
 *
 * <pre>{@code
 * User user = SecurityTenancy.withRootIfNoAccount(() -> crudService.findSingle(User.class, "username", eq(name)));
 * }</pre>
 *
 * @author Mario Serrano Leones
 */
public final class SecurityTenancy {

    private SecurityTenancy() {
    }

    /**
     * Runs the work as root when there is no current account, otherwise as the current account.
     *
     * @param work the work
     * @param <T>  the result type
     * @return the result of the work
     */
    public static <T> T withRootIfNoAccount(Supplier<T> work) {
        return withRootIfNoAccount(Containers.get().findObject(AccountServiceAPI.class), work);
    }

    /**
     * Same as {@link #withRootIfNoAccount(Supplier)} with an explicit account service, for callers that cannot rely on
     * {@link Containers} yet (startup).
     *
     * @param accountServiceAPI the account service, may be null
     * @param work              the work
     * @param <T>               the result type
     * @return the result of the work
     */
    public static <T> T withRootIfNoAccount(AccountServiceAPI accountServiceAPI, Supplier<T> work) {
        if (accountServiceAPI == null) {
            return work.get();
        }
        return accountServiceAPI.withRootIfNoAccount(work);
    }

    /**
     * Runs the work as root when there is no current account, otherwise as the current account.
     *
     * @param work the work
     */
    public static void runWithRootIfNoAccount(Runnable work) {
        withRootIfNoAccount(() -> {
            work.run();
            return null;
        });
    }
}
