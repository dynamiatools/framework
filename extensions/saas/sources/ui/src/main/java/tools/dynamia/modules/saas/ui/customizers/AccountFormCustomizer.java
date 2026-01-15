package tools.dynamia.modules.saas.ui.customizers;

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Combobox;
import tools.dynamia.integration.Containers;
import tools.dynamia.modules.saas.domain.Account;
import tools.dynamia.modules.saas.domain.AccountReseller;
import tools.dynamia.modules.saas.domain.AccountResellerAgent;
import tools.dynamia.modules.saas.services.AccountResellerService;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.viewers.ViewCustomizer;
import tools.dynamia.zk.util.ZKUtil;
import tools.dynamia.zk.viewers.form.FormView;

import java.util.List;

/**
 * Customizes the Account form view to handle reseller and reseller agent information.
 * It sets up the comboboxes for selecting resellers and their agents, and updates the
 * agent combobox based on the selected reseller.
 * <p>
 * This class listens for value changes in the form and updates the reseller and agent
 * information accordingly. It ensures that the agent combobox is populated with agents
 */
public class AccountFormCustomizer implements ViewCustomizer<FormView<Account>> {

    @Override
    public void customize(FormView<Account> view) {

        view.addEventListener(FormView.ON_VALUE_CHANGED, e -> {
            setupResellerInfo(view);
        });


    }

    /**
     * Setup reseller and reseller agent information in the form view.
     *
     * @param view
     */
    private void setupResellerInfo(FormView<Account> view) {
        var resellers = view.getFieldComponent("reseller").getInputComponent();
        var resellerAgents = view.getFieldComponent("resellerAgent").getInputComponent();

        var account = view.getValue();

        if (resellers instanceof Combobox resellerCombo && resellerAgents instanceof Combobox agentCombo) {

            var service = Containers.get().findObject(AccountResellerService.class);

            ZKUtil.fillCombobox(resellerCombo, service.findAllEnabledResellers(), account.getReseller(), true);

            if (account.getReseller() != null) {
                var agents = service.findAgentsByReseller(account.getReseller());
                fillComboboxAgents(agentCombo, agents, account);
            }

            resellerCombo.addEventListener(Events.ON_CHANGE, e -> {
                if (resellerCombo.getSelectedItem() == null) {
                    UIMessages.showMessage("Please select a valid reseller.");
                    return;
                }
                AccountReseller reseller = resellerCombo.getSelectedItem().getValue();
                var agents = service.findAgentsByReseller(reseller);
                fillComboboxAgents(agentCombo, agents, account);
            });

        }
    }

    private static void fillComboboxAgents(Combobox agentCombo, List<AccountResellerAgent> agents, Account account) {
        if (agents == null || agents.isEmpty()) {
            agentCombo.setValue(null);
            agentCombo.setPlaceholder(UIMessages.getLocalizedMessage("No agents available"));
            return;
        }

        ZKUtil.fillCombobox(agentCombo, agents, account.getResellerAgent(), true);
        agentCombo.setPlaceholder(agents.isEmpty() ?
                UIMessages.getLocalizedMessage("No agents available") :
                UIMessages.getLocalizedMessage("Select an agent"));
    }
}
