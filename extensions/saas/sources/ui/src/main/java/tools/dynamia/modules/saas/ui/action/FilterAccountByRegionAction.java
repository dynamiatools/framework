package tools.dynamia.modules.saas.ui.action;

import org.zkoss.zul.Combobox;
import tools.dynamia.actions.ActionRenderer;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.modules.saas.domain.Account;
import tools.dynamia.modules.saas.domain.AccountRegion;
import tools.dynamia.zk.actions.ComboboxActionRenderer;

import java.util.ArrayList;

@InstallAction
public class FilterAccountByRegionAction extends AbstractCrudAction {

    public FilterAccountByRegionAction() {
        setName("Filter by Region");
        setApplicableClass(Account.class);
        setAlwaysVisible(true);
    }

    @Override
    public ActionRenderer<Combobox> getRenderer() {
        var regions = new ArrayList<AccountRegion>();
        regions.add(null);
        regions.addAll(crudService().findAll(AccountRegion.class));
        return new ComboboxActionRenderer(regions);
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        if (evt.getData() instanceof AccountRegion region) {
            evt.getController().setParemeter("accountRegion", region);
            evt.getController().doQuery();
        }
    }
}
