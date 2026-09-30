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
 * request ends, it is parked in the HTTP session and restored into the next request's instance, so
 * the first desktop that bootstraps consumes it. Parked state is never shared between sessions, and
 * it is not persisted with the session (queued callbacks are not serializable).
 * <p>
 * <b>Ordering.</b> Registered with the highest precedence so the scope is already bound when Spring
 * Security authenticates the user and fires its login listeners, which run before the rest of the
 * chain.
 *
 * @author Mario A. Serrano Leones
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class NavigationManagerSessionScopeFilter extends OncePerRequestFilter {

    static final String HAND_OFF_ATTRIBUTE = NavigationManagerSessionScopeFilter.class.getName() + ".HAND_OFF";

    private static final LoggingService LOGGER = new SLF4JLoggingService(NavigationManagerSessionScopeFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var current = new NavigationManagerSession();
        current.setOnPending(() -> ensureSession(request));
        restoreParkedState(request, current);
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

    private void restoreParkedState(HttpServletRequest request, NavigationManagerSession target) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        synchronized (WebUtils.getSessionMutex(session)) {
            if (session.getAttribute(HAND_OFF_ATTRIBUTE) instanceof HandOff handOff) {
                session.removeAttribute(HAND_OFF_ATTRIBUTE);
                target.absorb(handOff.state());
            }
        }
    }

    private void parkPendingState(HttpServletRequest request, NavigationManagerSession current) {
        if (!current.hasPendingState()) {
            return;
        }
        try {
            HttpSession session = request.getSession(true);
            synchronized (WebUtils.getSessionMutex(session)) {
                HandOff handOff = session.getAttribute(HAND_OFF_ATTRIBUTE) instanceof HandOff existing ? existing : new HandOff();
                handOff.state().absorb(current);
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

        NavigationManagerSession state() {
            if (state == null) {
                state = new NavigationManagerSession();
            }
            return state;
        }
    }
}
