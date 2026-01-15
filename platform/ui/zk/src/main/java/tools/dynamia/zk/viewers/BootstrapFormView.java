package tools.dynamia.zk.viewers;

import org.zkoss.zul.South;
import tools.dynamia.web.util.HttpUtils;
import tools.dynamia.zk.actions.BootstrapButtonActionRenderer;
import tools.dynamia.zk.viewers.form.FormView;

public class BootstrapFormView<T> extends FormView<T> {


    @Override
    protected void initActionsArea() {
        super.initActionsArea();

        if (getActionPanel() != null) {
            var renderer = new BootstrapButtonActionRenderer();
            getActionPanel().setActionRenderer(renderer);

            if (HttpUtils.isSmartphone() && getActionPanel().getParent() instanceof South south) {
                renderer.setSmall(true);
                south.setHeight("78px");
                getActionPanel().setStyle("padding-top:2px;text-align:center" );

            }
        }


    }
}
