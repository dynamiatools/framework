package tools.dynamia.modules.saas;

import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.integration.Containers;
import tools.dynamia.modules.saas.api.AccountServiceAPI;
import tools.dynamia.web.util.HttpUtils;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@WebFilter(urlPatterns = "/*", displayName = "AccountRequestFilter", description = "Find and set current accountID in request attributes")
public class AccountRequestFilter implements Filter {

    private final static LoggingService logger = new SLF4JLoggingService(AccountServiceAPI.class);

    public AccountRequestFilter() {
        logger.info("Register SaaS Account request filter");
    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest req) {
            String subdomain = HttpUtils.getSubdomain(req);
            if (subdomain != null) {
                var accountId = (Long) req.getAttribute(AccountServiceAPI.CURRENT_ACCOUNT_ID_ATTRIBUTE);
                if (accountId == null) {
                    var accountServiceAPI = Containers.get().findObject(AccountServiceAPI.class);
                    if (accountServiceAPI != null) {
                        accountId = accountServiceAPI.getAccountIdByDomain(subdomain);
                        req.setAttribute(AccountServiceAPI.CURRENT_ACCOUNT_ID_ATTRIBUTE, accountId);
                    }
                }
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //do nothing
    }

    @Override
    public void destroy() {
        //do nothing
    }
}
