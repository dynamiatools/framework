package mybookstore.providers;

import tools.dynamia.integration.sterotypes.Component;
import tools.dynamia.zk.ui.MicroFrontendHostContextProvider;

import java.util.Map;

/**
 * Demonstrates tools.dynamia.zk.ui.MicroFrontendHostContextProvider: every value returned here
 * reaches every mounted microfrontend on this app automatically as the "dynamiaHost" prop, without
 * any ZUL author having to bind it by hand. See microfrontend-demo.zul.
 */
@Component
public class DemoHostContextProvider implements MicroFrontendHostContextProvider {

    @Override
    public Map<String, Object> getHostContext() {
        return Map.of(
                "tenantId", "demo-bookstore",
                "locale", "es_CO",
                "apiBaseUrl", "/api"
        );
    }
}
