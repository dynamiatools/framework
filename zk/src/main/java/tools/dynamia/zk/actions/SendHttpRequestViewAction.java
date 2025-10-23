package tools.dynamia.zk.actions;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.zkoss.zk.ui.Executions;
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.ActionLoader;
import tools.dynamia.actions.Actions;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.domain.Transferable;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.viewers.ViewAction;
import tools.dynamia.web.util.HttpRestClient;
import tools.dynamia.zk.util.ZKUtil;

import java.util.Map;

import static tools.dynamia.commons.Lambdas.ifValid;
import static tools.dynamia.commons.Lambdas.ifValidElse;

/**
 * Action to call a global command in the current view
 */
@InstallAction
public class SendHttpRequestViewAction extends ViewAction {

    public SendHttpRequestViewAction() {
        setId("send-http-request");
    }

    @Override
    public void actionPerformed(ActionEvent evt) {

        String method = getStringAttribute("method", "get");
        String contentType = getStringAttribute("contentType", "application/json");


        ifValidElse(getStringAttribute("url"), url -> {


            var data = evt.getData();
            if (data instanceof Transferable<?> transferable) {
                data = transferable.toDTO();
            }

            HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase().trim());
            HttpRestClient client = new HttpRestClient(url);
            client.setContentType(MediaType.valueOf(contentType));
            var response = client.exchange(httpMethod, "", data, Map.class);

            ifValid(getStringAttribute("successMessage"), UIMessages::showLocalizedMessage);

            ifValid(getStringAttribute("successAction"), actionId ->
                    ifValid(ActionLoader.findActionById(ViewAction.class, actionId), a -> Actions.run(a, response))
            );

            ifValid(getStringAttribute("redirect"), redirect -> {
                if (ZKUtil.isInEventListener()) {
                    Executions.getCurrent().sendRedirect(redirect);
                }
            });

        }, () -> UIMessages.showLocalizedMessage("Invalid URL ", MessageType.ERROR));

    }
}
