package tools.dynamia.web.navigation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import tools.dynamia.navigation.Page;

/**
 * {@link Page} interceptor, called after a page is opened directly using an or path URI
 */
@FunctionalInterface
public interface PageNavigationInterceptor {

    void afterPage(Page page, ModelAndView modelAndView, HttpServletRequest request, HttpServletResponse response);
}
