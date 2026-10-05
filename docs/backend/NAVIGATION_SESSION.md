# Navigation Session: multi-desktop & cross-request hand-off

How `NavigationManagerSession`, `NavigationManagerSessionScopeFilter`, `NavigationManager#getId()` and
`NavigationManagerRegistry` let one HTTP session drive several ZK desktops at once (browser tabs, or
several `<iframe>`s loading `/page-embed/**` from a non-ZK shell), and what callers must respect.

Background: epic #107, implemented in #108 / #109 / #110 (PR #112).

## Model

- One ZK desktop (tab / iframe) = one `ZKNavigationManager` (`zk-desktop` scope), each with its own current page.
- `setPageLater(page, params)` / `runLater(callback)` do not touch the manager directly: they record an
  *intent* in a `NavigationManagerSession`, which the next desktop bootstrap consumes
  (`ZKNavigationManager.init()` → page, `ZKNavigationComposer.doAfterCompose()` → callbacks).

## Scope: per request, not per HTTP session

`NavigationManagerSession` used to be a `@Scope("session")` bean: a single slot shared by every desktop of the
session. Concurrent `/page-embed/**` requests overwrote each other's pending page / `runLater` queue.

Now each request gets its own instance, bound through a `ScopedValue` (`NavigationManagerSession.SCOPE`) by
`NavigationManagerSessionScopeFilter` around the whole filter chain.

- The forward into the `.zul` view is synchronous and same-thread, so the code that calls `setPageLater` and the
  desktop bootstrap that consumes it share the same scope. No token/URL correlation is needed.
- `NavigationManagerSession.getInstance()` throws `NoSuchElementException` outside a bound scope (fail fast).
  **Background jobs / async threads must not call `setPageLater` / `runLater`.** Code that only *consumes* an intent
  (`ZKNavigationManager.init()`, `setCurrentComposer`) uses `NavigationManagerSession.current()`, which returns `null`
  outside a scope (ZK server push, `Executions.activate` from another thread, ASYNC/ERROR dispatches that
  `OncePerRequestFilter` skips) instead of throwing. In tests bind one explicitly:
  `ScopedValue.where(NavigationManagerSession.SCOPE, new NavigationManagerSession()).run(...)`.
- The binding is dropped automatically on return or exception; nothing leaks to other requests on the same thread.
- Requires Java 25 (`ScopedValue`, JEP 506).

## Cross-request hand-off (redirects)

Recording an intent and then **redirecting** is supported. Typical cases: a `LoginListener` calling
`setPageLater`/`runLater` on `AuthenticationSuccessEvent`, or a ZK command doing `setPageLater` + `sendRedirect`.

At the end of a request, if the instance still has a pending intent (`hasPendingState()`: a page not yet handed to a
manager, or queued callbacks not yet run), the filter parks it in the HTTP session (attribute
`NavigationManagerSessionScopeFilter.HAND_OFF`). Restoration is **lazy**: a request does not touch the parked state
when it starts; the filter installs a *parked state supplier* on the request's instance
(`setParkedStateSupplier`), and the state is only taken from the HTTP session (and removed) when a desktop consumes the
instance (`updateNavManager` / `executeQueue`). So static, `/zkau` and `/api` requests running concurrently cannot steal
it, and the **first desktop that bootstraps consumes it**. Consumed once: a second desktop gets the default page.
Parked state **expires** after a TTL (default 2 minutes, `setHandOffTtl` on the filter) and is discarded instead of
firing later in an unrelated tab. `executeQueue()` drains a snapshot: a callback that calls `runLater` is queued for
the next drain, and a failing callback is logged without stopping the others.

Details that matter:

| Aspect | Behavior |
|---|---|
| Filter order | `HIGHEST_PRECEDENCE + 51`: just **after** Spring Session's `SessionRepositoryFilter` (`MIN_VALUE + 50`, so the hand-off uses the Spring Session session) and **before** Spring Security's chain (`-100`), so the scope is bound when login listeners fire. |
| Session creation | An `onPending` hook calls `getSession(true)` as soon as an intent is recorded, i.e. before a redirect commits the response. Without it, a first request with no session lost the intent. |
| Persistence | Parked state is `transient` (callbacks are not serializable). After session passivation/restart it is gone. |
| Isolation | Never shared across sessions. Access is synchronized on the session mutex. |
| Merge rules | A pending page replaces a previously parked one; callbacks are appended in order. |
| No session at end of request | Intent is dropped with a `WARN` (`Pending navigation state dropped...`). |

### Known limit: residual race

The parked intent goes to the **first desktop that bootstraps**, not necessarily the intended one (e.g. two iframes
opened right after a login). It is strictly better than the old shared slot, but not exact. Removing it requires a
per-URL correlation token, which was deliberately ruled out in #107. If you need deterministic targeting, don't
rely on hand-off: navigate through the forward path (`PageNavigationController` / `PageEmbedController`) instead.

## Instance identity and registry

- `NavigationManager#getId()`: opaque `UUID` generated at construction in `BaseNavigationManager`. UI-framework
  agnostic (no ZK types in the core contract).
- `NavigationManagerRegistry` (`@Scope("session")`): every manager self-registers on construction (null-safe outside
  a Spring context) unless it overrides `BaseNavigationManager.shouldRegister()`; `ZKNavigationManager` only registers
  when built inside a desktop, because the desktop cleanup is what unregisters it. API: `getActiveInstances()`,
  `find(id)`, `register`, `unregister`. It holds strong references: managers of other desktops must not be
  manipulated outside their own ZK execution.
- `ZKNavigationManager.init()` unregisters on ZK `DesktopCleanup`. Best effort: correctness does not depend on it, since
  the registry dies with the session; it only keeps the list accurate while other tabs/iframes are still open.
- No REST/SDK endpoint exposes the registry; evaluate separately if a host shell needs it.

## Rules for callers

1. Call `setPageLater` / `runLater` inside a request handled by the filter (controller, security listener, ZK
   command). Never from a background thread.
2. Redirect after recording an intent is OK; the intent is delivered to the next desktop bootstrap of the session.
3. Don't assume the hand-off reaches a specific tab/iframe (see residual race).
4. Consumer code that may run outside a request scope uses `NavigationManagerSession.current()` and checks for `null`.
5. Don't register another filter that binds `NavigationManagerSession.SCOPE` or that must observe login events
   ahead of `NavigationManagerSessionScopeFilter`.

## Validation status

Covered by `NavigationManagerSessionTest` and `NavigationManagerSessionScopeFilterTest` (incl. concurrency,
hand-off, no-session redirect, filter order < Spring Security) and by the `/demo/handoff/*` endpoints of
`examples/demo-zk-books`. **Not yet validated against a real Spring Security login** (needs a run in dynamia-erp; see #107).
