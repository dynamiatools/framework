package mybookstore.handoff;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.dynamia.navigation.NavigationManager;

import java.io.IOException;

/**
 * Simulates what a real login does in an app that uses Spring Security: a listener reacting to the
 * authentication success ({@code LoginListener.onLoginSuccess}) calls {@code setPageLater}/{@code runLater}
 * <b>inside the security filter chain</b> (order -100, before any controller) and the request ends with an
 * HTTP redirect. Both things must work: the navigation scope must already be bound at that point, and the
 * pending intent must survive the redirect.
 * <p>
 * Try: {@code GET /demo/handoff/login} (redirects to {@code /demo/handoff/desktop}, which must open Customers).
 */
@Component
@Order(-100)
public class HandOffDemoLoginFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"/demo/handoff/login".equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        NavigationManager.setPageLater("library/customers");
        NavigationManager.runLater(HandOffDemoState.CALLBACKS_RUN::incrementAndGet);
        response.sendRedirect("/demo/handoff/desktop");
    }
}
