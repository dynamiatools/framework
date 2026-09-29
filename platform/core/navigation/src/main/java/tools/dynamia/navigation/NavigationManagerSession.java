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
 * This is a {@link ThreadLocal} holder, not a session-scoped bean: the only supported flow is a
 * server-side forward (e.g. {@code RequestDispatcher.forward()}, as used by
 * {@code PageNavigationController}/{@code PageEmbedController} to render an {@code index}/{@code embed}
 * ZUL view) where the code calling {@link #setPage(Page, Map)}/{@link #runLater(Callback)} and the
 * ZK desktop bootstrap that consumes it ({@code ZKNavigationManager.init()},
 * {@code ZKNavigationComposer.doAfterCompose()}) run on the very same thread, within the very same
 * HTTP request.
 * </p>
 * <p>
 * <b>Do not</b> call {@link #setPage(Page, Map)}/{@link #runLater(Callback)} before an HTTP
 * <b>redirect</b> expecting a later request to pick it up — a redirect is a new request, possibly
 * served by a different thread, and the thread-local value won't be there. If that use case ever
 * arises it needs a different, explicit hand-off mechanism, not this class.
 * </p>
 * <p>
 * Using a thread-local (instead of the session-scoped bean this class used to be) is what makes it
 * safe for multiple ZK desktops to bootstrap concurrently in the same HTTP session — e.g. several
 * {@code <iframe>}s, each loading its own ZK page, or several real browser tabs opened at once. A
 * session-scoped slot would be shared and overwritten across those concurrent requests; a
 * thread-local is naturally isolated per request/thread.
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
public class NavigationManagerSession implements Serializable {

    private static final ThreadLocal<NavigationManagerSession> CURRENT =
            ThreadLocal.withInitial(NavigationManagerSession::new);

    private Page page;
    private Map<String, Serializable> pageParams;

    private Queue<Callback> runLaterQueue = new LinkedList<>();

    /**
     * Returns the instance bound to the current thread, creating it lazily.
     */
    public static NavigationManagerSession getInstance() {
        return CURRENT.get();
    }

    /**
     * Removes the instance bound to the current thread. Must be called once the request that
     * populated it (and forwarded into the target ZK desktop) has finished, so pooled threads don't
     * retain a stale instance across unrelated requests.
     */
    public static void clear() {
        CURRENT.remove();
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
