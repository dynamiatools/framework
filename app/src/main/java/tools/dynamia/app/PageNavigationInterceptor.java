package tools.dynamia.app;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import tools.dynamia.navigation.Page;

public interface PageNavigationInterceptor {

    void afterPage(Page page, ModelAndView mv, HttpServletRequest request);
}
