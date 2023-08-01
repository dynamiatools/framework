package tools.dynamia.zk.navigation;

import jakarta.servlet.http.HttpServletRequest;
import org.zkoss.zk.ui.Executions;
import tools.dynamia.integration.sterotypes.Component;
import tools.dynamia.navigation.NavigationManager;
import tools.dynamia.navigation.NavigationPageHolder;
import tools.dynamia.navigation.Page;
import tools.dynamia.web.util.HttpUtils;
import tools.dynamia.zk.util.ZKUtil;

import java.util.HashMap;
import java.util.Map;

@Component
public class RequestNavigationPageHolder implements NavigationPageHolder {


    private static HttpServletRequest getRequest() {
        HttpServletRequest request = null;
        if (ZKUtil.isInDesktopScope()) {
            request = (HttpServletRequest) Executions.getCurrent().getNativeRequest();
        } else {
            request = HttpUtils.getCurrentRequest();
        }
        return request;
    }

    @Override
    public void setPage(Page page, Map<String, Object> params) {
        HttpServletRequest request = getRequest();

        if (request != null) {
            request.setAttribute(NavigationManager.CURRENT_PAGE_ATTRIBUTE, page);
            request.setAttribute(NavigationManager.CURRENT_PAGE_PARAMS_ATTRIBUTE, params);
        }
    }

    @Override
    public Page getPage() {
        var request = getRequest();
        return request != null ? (Page) request.getAttribute(NavigationManager.CURRENT_PAGE_ATTRIBUTE) : null;
    }

    @Override
    public Map<String, Object> getParams() {
        var request = getRequest();
        try {
            return request != null ? (Map<String, Object>) request.getAttribute(NavigationManager.CURRENT_PAGE_PARAMS_ATTRIBUTE) : null;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}
