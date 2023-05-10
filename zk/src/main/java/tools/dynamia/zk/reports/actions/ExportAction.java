package tools.dynamia.zk.reports.actions;

import tools.dynamia.actions.ActionLifecycleAware;
import tools.dynamia.actions.ActionRenderer;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.commons.Messages;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.integration.Containers;
import tools.dynamia.zk.actions.MenuActionRenderer;
import tools.dynamia.zk.crud.CrudController;
import tools.dynamia.zk.crud.CrudControllerAware;

@InstallAction
public class ExportAction extends AbstractCrudAction implements ActionLifecycleAware, CrudControllerAware {

    private CrudController crudController;

    public ExportAction() {
        setName(Messages.get(getClass(), "export"));
        setRenderer(new MenuActionRenderer());
    }

    @Override
    public void beforeRenderer(ActionRenderer renderer) {
        if (renderer instanceof MenuActionRenderer mar) {
            var actions = Containers.get().findObjects(AbstractExportAction.class);

            actions.forEach(a -> {
                a.setCrudController(crudController);
                a.setParent(this);
            });


            mar.setActionItems(actions);
        }
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
//do nothing
    }

    @Override
    public void setCrudController(CrudController crudController) {
        this.crudController = crudController;
    }
}
