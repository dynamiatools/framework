package tools.dynamia.app;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

public interface IndexInterceptor {

    void afterIndex(ModelAndView mv, HttpServletRequest request);
}
