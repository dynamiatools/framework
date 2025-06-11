package tools.dynamia.zk.viewers;

import tools.dynamia.zk.actions.BootstrapButtonActionRenderer;
import tools.dynamia.zk.viewers.form.FormView;

public class BootstrapFormView<T> extends FormView<T> {


    @Override
    protected void initActionsArea() {
        super.initActionsArea();

        if (getActionPanel() != null) {
            getActionPanel().setActionRenderer(new BootstrapButtonActionRenderer());
        }
    }
}
