package tools.dynamia.zk.ui;

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import tools.dynamia.ui.ButtonComponent;
import tools.dynamia.ui.EventCallback;

/**
 * ZK implementation of {@link ButtonComponent}
 */
public class ZKButton extends Button implements ButtonComponent {


    @Override
    public void setIcon(String icon) {
        setIconSclass(icon);
    }

    @Override
    public String getIcon() {
        return getIconSclass();
    }

    @Override
    public void onClick(EventCallback onClick) {
        addEventListener(Events.ON_CLICK, event -> onClick.onEvent());
    }
}
