package tools.dynamia.navigation;

import java.util.Map;

public interface NavigationPageHolder {

    void setPage(Page page, Map<String, Object> params);

    Page getPage();

    Map<String, Object> getParams();

}
