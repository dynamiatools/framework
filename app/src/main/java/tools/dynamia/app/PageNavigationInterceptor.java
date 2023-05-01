package tools.dynamia.app;

import org.springframework.web.servlet.ModelAndView;
import tools.dynamia.navigation.Page;

import jakarta.servlet.http.HttpServletRequest;

public interface PageNavigationInterceptor {

    void afterPage(Page page, ModelAndView mv, HttpServletRequest request);
}
