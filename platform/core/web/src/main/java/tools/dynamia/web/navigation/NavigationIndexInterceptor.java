package tools.dynamia.web.navigation;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

/**
 * Executed after index path / is called in an MVC webapp
 */
@FunctionalInterface
public interface NavigationIndexInterceptor {

    void afterIndex(ModelAndView mv, HttpServletRequest request);
}
