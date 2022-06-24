package tools.dynamia.app;

import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

public interface IndexInterceptor {

    void afterIndex(ModelAndView mv, HttpServletRequest request);
}
