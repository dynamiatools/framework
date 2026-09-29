package tools.dynamia.navigation;

import tools.dynamia.commons.Callback;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

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
 * {@link #getInstance()} throws {@link java.util.NoSuchElementException} if called outside that
 * scope — e.g. a background job, or test code that hasn't bound one itself via
 * {@code ScopedValue.where(NavigationManagerSession.SCOPE, new NavigationManagerSession()).run(...)}.
 * This is intentional: failing fast beats silently creating a throwaway instance whose state would
 * be lost the moment the call returns.
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

    private Page page;
    private Map<String, Serializable> pageParams;

    private Queue<Callback> runLaterQueue = new LinkedList<>();

    /**
     * Returns the instance bound to the current scope.
     *
     * @throws java.util.NoSuchElementException if called outside a bound scope
     */
    public static NavigationManagerSession getInstance() {
        return SCOPE.get();
    }

    public void setPage(Page page, Map<String, Serializable> params) {
        this.page = page;
        this.pageParams = params;
    }

    public void updateNavManager(NavigationManager navigationManager) {
        if (navigationManager != null) {
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
    }

    public void executeQueue() {
        while (!runLaterQueue.isEmpty()) {
            var callback = runLaterQueue.poll();
            if (callback != null) {
                callback.doSomething();
            }
        }
    }

    public Page getPage() {
        return page;
    }

    public Map<String, Serializable> getPageParams() {
        return pageParams;
    }


}
