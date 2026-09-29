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
 * Binds a fresh {@link NavigationManagerSession} to the {@link ScopedValue} scope of the whole
 * request, so a controller earlier in the chain (e.g. {@code PageNavigationController}/
 * {@code PageEmbedController}) can stash a pending page/callback and the ZK desktop bootstrap
 * later in the same request (reached via a server-side forward) can pick it up — see
 * {@link NavigationManagerSession}'s Javadoc for the full contract.
 * <p>
 * The binding is torn down automatically by {@link ScopedValue.Carrier#call} once the request
 * finishes, whether normally or via an exception — no manual cleanup needed, and nothing leaks into
 * a later, unrelated request even if the servlet container reuses this thread for it.
 *
 * @author Mario A. Serrano Leones
 */
@Component
public class NavigationManagerSessionScopeFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            ScopedValue.where(NavigationManagerSession.SCOPE, new NavigationManagerSession())
                    .call(() -> {
                        filterChain.doFilter(request, response);
                        return null;
                    });
        } catch (ServletException | IOException | RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
