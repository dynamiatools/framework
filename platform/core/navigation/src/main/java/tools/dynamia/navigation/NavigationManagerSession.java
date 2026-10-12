package tools.dynamia.navigation;

import tools.dynamia.commons.Callback;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;

import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.function.Supplier;

/**
 * Holds a navigation intent (a {@link Page} plus optional params, and/or queued {@link Callback}s)
 * so it can be picked up by the {@link NavigationManager} of a ZK desktop that is about to be built.
 * <p>
 * Backed by a {@link ScopedValue}, not a session-scoped bean: the only supported flow is a
 * server-side forward (e.g. {@code RequestDispatcher.forward()}, as used by
 * {@code PageNavigationController}/{@code PageEmbedController} to render an {@code index}/{@code embed}
 * ZUL view) where the code calling {@link #setPage(Page, Map)}/{@link #runLater(Callback)} and the
 * ZK desktop bootstrap that consumes it ({@code ZKNavigationManager.init()},
 * {@code ZKNavigationComposer.doAfterCompose()}) run within the same dynamic scope — the whole
 * request, wrapped by a request-lifecycle filter that binds a fresh instance for its duration (see
 * {@code NavigationManagerSessionScopeFilter}).
 * </p>
 * <p>
 * A redirect starts a new request, so an intent recorded right before one (e.g. by a login listener)
 * would be lost with the scope. The filter therefore carries any still-pending intent across
 * requests through the HTTP session ({@link #hasPendingState()}/{@link #absorb(NavigationManagerSession)}):
 * it is restored into the next request's instance and consumed by the first desktop that bootstraps.
 * The restoration is <em>lazy</em>: the parked state stays in the HTTP session until a desktop actually consumes it
 * ({@link #updateNavManager(NavigationManager)} or {@link #executeQueue()}), pulled through the hook installed with
 * {@link #setParkedStateSupplier(Supplier)}, so concurrent requests that never bootstrap a desktop (static resources,
 * {@code /zkau}, {@code /api}) cannot take it. Parked state expires after {@link #DEFAULT_PARKED_STATE_TTL}.
 * </p>
 * <p>
 * {@link #getInstance()} throws {@link java.util.NoSuchElementException} if called outside that
 * scope — e.g. a background job, or test code that hasn't bound one itself via
 * {@code ScopedValue.where(NavigationManagerSession.SCOPE, new NavigationManagerSession()).run(...)}.
 * This is intentional for code that <em>records</em> an intent ({@code setPage}/{@code runLater}): failing fast beats
 * silently creating a throwaway instance whose state would be lost the moment the call returns. Code that merely
 * <em>consumes</em> an intent (a desktop bootstrap, which may run outside a bound scope: ZK server push,
 * {@code Executions.activate} from another thread, async/error dispatches) must use {@link #current()}, which returns
 * null instead of throwing.
 * </p>
 * <p>
 * Using a scoped value (instead of the session-scoped bean this class used to be, or a plain
 * {@link ThreadLocal}) is what makes it safe for multiple ZK desktops to bootstrap concurrently in
 * the same HTTP session — e.g. several {@code <iframe>}s, each loading its own ZK page, or several
 * real browser tabs opened at once — and plays correctly with virtual threads and structured
 * concurrency: the binding is strictly scoped to the dynamic extent of the request that created it,
 * torn down automatically (even on exception) with no manual cleanup step, and never leaks into an
 * unrelated request that happens to reuse the same platform thread.
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
public class NavigationManagerSession implements Serializable {

    /**
     * Scoped-value key binding a {@link NavigationManagerSession} to the dynamic extent of the
     * request bootstrapping a ZK desktop. Exposed so the request-lifecycle filter that establishes
     * the binding doesn't need extra indirection; ordinary callers should use {@link #getInstance()}
     * instead of touching this directly.
     */
    public static final ScopedValue<NavigationManagerSession> SCOPE = ScopedValue.newInstance();

    /**
     * How long a parked intent stays valid in the HTTP session before it is discarded instead of firing later in an
     * unrelated tab.
     */
    public static final Duration DEFAULT_PARKED_STATE_TTL = Duration.ofMinutes(2);

    private static final LoggingService LOGGER = new SLF4JLoggingService(NavigationManagerSession.class);

    private Page page;
    private Map<String, Serializable> pageParams;

    private Queue<Callback> runLaterQueue = new LinkedList<>();

    private transient Runnable onPending;
    private transient Supplier<NavigationManagerSession> parkedStateSupplier;

    /**
     * Installs the hook that pulls the intent parked by a previous request, called at most once, the first time a
     * desktop consumes this instance ({@link #updateNavManager(NavigationManager)} or {@link #executeQueue()}). It
     * must remove the parked state from wherever it is kept (so it is consumed once) and return null when there is
     * none or it expired. Keeps this module free of servlet dependencies: the request-lifecycle filter provides it.
     *
     * @param parkedStateSupplier the hook, or null to clear it
     */
    public void setParkedStateSupplier(Supplier<NavigationManagerSession> parkedStateSupplier) {
        this.parkedStateSupplier = parkedStateSupplier;
    }

    private void restoreParkedState() {
        Supplier<NavigationManagerSession> supplier = parkedStateSupplier;
        if (supplier != null) {
            parkedStateSupplier = null;
            absorb(supplier.get());
        }
    }

    /**
     * Registers a hook invoked every time an intent is recorded ({@link #setPage(Page, Map)} or
     * {@link #runLater(Callback)}). The request-lifecycle filter uses it to make sure an HTTP session
     * exists <i>before</i> the response may be committed (e.g. by a redirect), because that session is
     * where an unconsumed intent is parked for the next request.
     *
     * @param onPending the hook, or null to clear it
     */
    public void setOnPending(Runnable onPending) {
        this.onPending = onPending;
    }

    private void notifyPending() {
        if (onPending != null) {
            onPending.run();
        }
    }

    /**
     * Returns the instance bound to the current scope.
     *
     * @throws java.util.NoSuchElementException if called outside a bound scope
     */
    public static NavigationManagerSession getInstance() {
        return SCOPE.get();
    }

    /**
     * Returns the instance bound to the current scope, or null when there is none. Use it in code that consumes an
     * intent and may run outside a request scope (ZK server push, another thread, async/error dispatches); code that
     * records an intent keeps using {@link #getInstance()} and fails fast.
     *
     * @return the bound instance, or null if called outside a bound scope
     */
    public static NavigationManagerSession current() {
        return SCOPE.isBound() ? SCOPE.get() : null;
    }

    public void setPage(Page page, Map<String, Serializable> params) {
        this.page = page;
        this.pageParams = params;
        notifyPending();
    }

    public void updateNavManager(NavigationManager navigationManager) {
        if (navigationManager != null) {
            restoreParkedState();
            navigationManager.setCurrentPage(page, pageParams);
            page = null;
            pageParams = null;
        }
    }

    public void runLater(Callback callback) {
        if (runLaterQueue == null) {
            runLaterQueue = new LinkedList<>();
        }
        runLaterQueue.add(callback);
        notifyPending();
    }

    /**
     * Runs the queued callbacks, in order. It drains a snapshot of the queue, so a callback that calls
     * {@link #runLater(Callback)} queues its callback for the next call instead of looping forever, and an exception
     * thrown by a callback is logged without preventing the rest from running.
     */
    public void executeQueue() {
        restoreParkedState();
        if (runLaterQueue == null || runLaterQueue.isEmpty()) {
            return;
        }
        var batch = new ArrayList<>(runLaterQueue);
        runLaterQueue.clear();
        for (Callback callback : batch) {
            if (callback != null) {
                try {
                    callback.doSomething();
                } catch (Exception e) {
                    LOGGER.error("Error executing navigation callback: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Tells whether this instance still holds a navigation intent nobody has consumed: a pending
     * {@link Page} (not yet handed to a {@link NavigationManager} via {@link #updateNavManager}) or
     * queued {@link Callback}s (not yet run via {@link #executeQueue()}).
     *
     * @return true if there is a pending page or at least one queued callback
     */
    public boolean hasPendingState() {
        return page != null || (runLaterQueue != null && !runLaterQueue.isEmpty());
    }

    /**
     * Moves the pending intent of {@code other} into this instance, leaving {@code other} empty.
     * Used by the request-lifecycle filter to carry an unconsumed intent across requests (e.g. a
     * login that queues a page and then redirects). A pending page in {@code other} replaces the one
     * held here; its queued callbacks are appended after the ones already queued.
     *
     * @param other the instance to drain; ignored if null or the same instance
     */
    public void absorb(NavigationManagerSession other) {
        if (other == null || other == this) {
            return;
        }
        if (other.page != null) {
            this.page = other.page;
            this.pageParams = other.pageParams;
            other.page = null;
            other.pageParams = null;
        }
        if (other.runLaterQueue != null) {
            other.runLaterQueue.forEach(this::runLater);
            other.runLaterQueue.clear();
        }
    }

    public Page getPage() {
        return page;
    }

    public Map<String, Serializable> getPageParams() {
        return pageParams;
    }


}
