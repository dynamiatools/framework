package tools.dynamia.navigation;

import org.springframework.context.annotation.Scope;
import tools.dynamia.commons.Callback;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.sterotypes.Component;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

@Component
@Scope("session")
public class NavigationManagerSession {

    private Page page;
    private Map<String, Object> pageParams;

    private Queue<Callback> runLaterQueue = new LinkedList<>();

    public static NavigationManagerSession getInstance() {
        return Containers.get().findObject(NavigationManagerSession.class);
    }

    public void setPage(Page page, Map<String, Object> params) {
        this.page = page;
        this.pageParams = params;
    }

    public void updateNavManager(NavigationManager navigationManager) {
        if (navigationManager != null) {
            navigationManager.setCurrentPage(page, pageParams);
            page = null;
            pageParams = null;
        }
    }

    public void runLater(Callback callback) {
        runLaterQueue.add(callback);
    }

    public void executeQueue() {
        while (!runLaterQueue.isEmpty()) {
            var callback = runLaterQueue.poll();
            if (callback != null) {
                callback.doSomething();
            }
        }
    }

    public Page getPage() {
        return page;
    }

    public Map<String, Object> getPageParams() {
        return pageParams;
    }


}
