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
import org.springframework.web.filter.OncePerRequestFilter;
import tools.dynamia.integration.sterotypes.Component;
import tools.dynamia.navigation.NavigationManagerSession;

import java.io.IOException;

/**
 * Clears the thread-local {@link NavigationManagerSession} once a request finishes, so pooled
 * request-handling threads don't retain a stale navigation intent (page/params/queued callbacks)
 * across unrelated later requests.
 * <p>
 * {@link NavigationManagerSession} is populated and consumed within the same request/thread (a
 * controller sets a pending page, then a server-side forward into the ZK desktop's view consumes
 * it on that same thread) — see its Javadoc for the full contract. This filter only guarantees the
 * thread-local is removed afterward regardless of how the request completes.
 *
 * @author Mario A. Serrano Leones
 */
@Component
public class NavigationManagerSessionCleanupFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            NavigationManagerSession.clear();
        }
    }
}
