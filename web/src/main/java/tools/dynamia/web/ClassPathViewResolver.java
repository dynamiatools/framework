package tools.dynamia.web;

import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.AbstractCachingViewResolver;

import java.lang.reflect.Constructor;
import java.util.Locale;

public class ClassPathViewResolver extends AbstractCachingViewResolver implements Ordered {

    private String prefix = "views/";
    private String suffix = ".html";
    private int order = Ordered.LOWEST_PRECEDENCE;
    private String contentType = "text/html;charset=UTF-8";
    private Class<? extends ClassPathView> viewClass = ClassPathView.class;

    @Override
    protected View loadView(String viewName, Locale locale) throws Exception {
        try {
            ClassPathResource resource = new ClassPathResource(prefix + viewName + suffix);
            if (resource.exists()) {
                Constructor<? extends ClassPathView> constructor = viewClass.getConstructor(ClassPathResource.class, String.class);
                return constructor.newInstance(resource, contentType);
            }
        } catch (Exception e) {
            // Log or ignore, to allow chaining
        }
        return null;
    }


    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    @Override
    public int getOrder() {
        return order;
    }


    public void setOrder(int order) {
        this.order = order;
    }

    public String getContentType() {
        return contentType;
    }

    public Class<? extends ClassPathView> getViewClass() {
        return viewClass;
    }

    public void setViewClass(Class<? extends ClassPathView> viewClass) {
        this.viewClass = viewClass;
    }
}
