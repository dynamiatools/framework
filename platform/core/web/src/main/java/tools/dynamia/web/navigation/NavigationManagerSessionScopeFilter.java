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
package tools.dynamia.web.navigation;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.integration.sterotypes.Component;
import tools.dynamia.navigation.NavigationManagerSession;

import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;

/**
 * Binds a fresh {@link NavigationManagerSession} to the {@link ScopedValue} scope of the whole
 * request, so a controller earlier in the chain (e.g. {@code PageNavigationController}/
 * {@code PageEmbedController}) can stash a pending page/callback and the ZK desktop bootstrap
 * later in the same request (reached via a server-side forward) can pick it up — see
 * {@link NavigationManagerSession}'s Javadoc for the full contract.
 * <p>
 * The binding is torn down automatically by {@link ScopedValue.Carrier#call} once the request
 * finishes, whether normally or via an exception — no manual cleanup needed, and nothing leaks into
 * a later, unrelated request even if the servlet container reuses this thread for it.
 * <p>
 * <b>Hand-off across requests.</b> Some callers record a navigation intent and then end the request
 * without forwarding into a desktop — typically a login listener followed by an HTTP redirect, or a
 * ZK event that calls {@code sendRedirect}. If the instance still has a pending intent when the
 * request ends, it is parked in the HTTP session until the first desktop that bootstraps consumes it. Parked state
 * is never shared between sessions, and it is not persisted with the session (queued callbacks are not
 * serializable).
 * <p>
 * The restoration is <b>lazy</b>: a request does not touch the parked state when it starts. It installs a
 * {@link NavigationManagerSession#setParkedStateSupplier(java.util.function.Supplier) supplier} on its instance, and
 * the parked state is only taken from the HTTP session when a desktop consumes the instance, so concurrent requests
 * that never bootstrap a desktop (static resources, {@code /zkau}, {@code /api}) cannot steal it. Parked state
 * <b>expires</b> after {@link #getHandOffTtl() a TTL} (default
 * {@link NavigationManagerSession#DEFAULT_PARKED_STATE_TTL}) and is then discarded instead of firing later in an
 * unrelated tab.
 * <p>
 * <b>Ordering.</b> Runs just after Spring Session's {@code SessionRepositoryFilter} (so it uses the session managed by
 * Spring Session, when present, and not the container's one) and before Spring Security's filter chain, so the scope
 * is already bound when Spring Security authenticates the user and fires its login listeners.
 *
 * @author Mario A. Serrano Leones
 */
@Component
@Order(NavigationManagerSessionScopeFilter.ORDER)
public class NavigationManagerSessionScopeFilter extends OncePerRequestFilter {

    /**
     * Order of Spring Session's {@code SessionRepositoryFilter} ({@code Integer.MIN_VALUE + 50}), copied as a literal
     * so this module does not depend on spring-session.
     */
    static final int SPRING_SESSION_FILTER_ORDER = Ordered.HIGHEST_PRECEDENCE + 50;

    /**
     * Filter order: just after Spring Session's filter, and well before Spring Security's chain (order -100).
     */
    public static final int ORDER = SPRING_SESSION_FILTER_ORDER + 1;

    static final String HAND_OFF_ATTRIBUTE = NavigationManagerSessionScopeFilter.class.getName() + ".HAND_OFF";

    private static final LoggingService LOGGER = new SLF4JLoggingService(NavigationManagerSessionScopeFilter.class);

    private volatile Duration handOffTtl = NavigationManagerSession.DEFAULT_PARKED_STATE_TTL;

    /**
     * Returns how long a parked intent stays valid.
     *
     * @return the hand-off time to live
     */
    public Duration getHandOffTtl() {
        return handOffTtl;
    }

    /**
     * Sets how long a parked intent stays valid before it is discarded.
     *
     * @param handOffTtl the hand-off time to live; must not be null
     */
    public void setHandOffTtl(Duration handOffTtl) {
        this.handOffTtl = Objects.requireNonNull(handOffTtl, "handOffTtl");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var current = new NavigationManagerSession();
        current.setOnPending(() -> ensureSession(request));
        current.setParkedStateSupplier(() -> takeParkedState(request));
        try {
            ScopedValue.where(NavigationManagerSession.SCOPE, current)
                    .call(() -> {
                        filterChain.doFilter(request, response);
                        return null;
                    });
        } catch (ServletException | IOException | RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            parkPendingState(request, current);
        }
    }

    private void ensureSession(HttpServletRequest request) {
        try {
            request.getSession(true);
        } catch (IllegalStateException e) {
            LOGGER.warn("Cannot create HTTP session to hand off navigation state: " + e.getMessage());
        }
    }

    /**
     * Takes the parked state out of the HTTP session (so it is consumed once). Returns null when there is none or it
     * expired, in which case it is discarded.
     */
    private NavigationManagerSession takeParkedState(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        synchronized (WebUtils.getSessionMutex(session)) {
            if (session.getAttribute(HAND_OFF_ATTRIBUTE) instanceof HandOff handOff) {
                session.removeAttribute(HAND_OFF_ATTRIBUTE);
                if (handOff.isExpired(handOffTtl)) {
                    LOGGER.debug("Parked navigation state expired and was discarded");
                    return null;
                }
                return handOff.state();
            }
        }
        return null;
    }

    private void parkPendingState(HttpServletRequest request, NavigationManagerSession current) {
        if (!current.hasPendingState()) {
            return;
        }
        try {
            HttpSession session = request.getSession(true);
            synchronized (WebUtils.getSessionMutex(session)) {
                HandOff handOff = session.getAttribute(HAND_OFF_ATTRIBUTE) instanceof HandOff existing
                        && !existing.isExpired(handOffTtl) ? existing : new HandOff();
                handOff.state().absorb(current);
                handOff.touch();
                session.setAttribute(HAND_OFF_ATTRIBUTE, handOff);
            }
        } catch (IllegalStateException e) {
            LOGGER.warn("Pending navigation state dropped: no HTTP session available at end of request (" + e.getMessage() + ")");
        }
    }

    /**
     * Session attribute holding the parked intent. The state is {@code transient}: queued callbacks
     * are not serializable, so after a session passivation the parked intent is simply gone.
     */
    private static final class HandOff implements Serializable {
        private static final long serialVersionUID = 1L;
        private transient NavigationManagerSession state;
        private long parkedAt = System.currentTimeMillis();

        void touch() {
            parkedAt = System.currentTimeMillis();
        }

        boolean isExpired(Duration ttl) {
            return System.currentTimeMillis() - parkedAt >= ttl.toMillis();
        }

        NavigationManagerSession state() {
            if (state == null) {
                state = new NavigationManagerSession();
            }
            return state;
        }
    }
}
