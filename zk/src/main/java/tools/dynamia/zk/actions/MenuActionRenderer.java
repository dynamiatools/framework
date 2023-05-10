package tools.dynamia.zk.actions;

import org.zkoss.zul.Menu;
import org.zkoss.zul.Menubar;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Menuseparator;
import tools.dynamia.actions.Action;
import tools.dynamia.actions.ActionEventBuilder;
import tools.dynamia.actions.ActionPlaceholder;
import tools.dynamia.commons.Messages;
import tools.dynamia.integration.Containers;
import tools.dynamia.ui.icons.IconSize;
import tools.dynamia.zk.util.ZKUtil;

import java.util.Collection;
import java.util.List;

/**
 * Render collection of actions as {@link Menu} and {@link org.zkoss.zul.Menuitem}, support nested actions with {@link MenuActionRenderer}
 */
public class MenuActionRenderer extends ZKActionRenderer<Menubar> {

    private Class<? extends Action> actionItemClass;
    private List<Action> actionItems;

    public MenuActionRenderer() {
    }

    public MenuActionRenderer(Class<? extends Action> actionItemsClass) {
        this.actionItemClass = actionItemsClass;
    }

    public MenuActionRenderer(List<Action> actionItems) {
        this.actionItems = actionItems;
    }

    @Override
    public Menubar render(Action action, ActionEventBuilder actionEventBuilder) {

        if ((actionItems == null || actionItems.isEmpty()) && actionItemClass != null) {
            var actions = Containers.get().findObjects(actionItemClass);
            setActionItems(actions);
        }


        var menu = new Menu();
        String actionName = action.getLocalizedName(Messages.getDefaultLocale());
        ZKUtil.configureComponentIcon(action.getImage(), menu, IconSize.SMALL);
        menu.setLabel(actionName);

        var itemRenderer = new MenuitemActionRenderer();
        if (actionItems != null && !actionItems.isEmpty()) {
            var popup = new Menupopup();
            menu.appendChild(popup);

            actionItems.forEach(subaction -> {
                subaction.setParent(action);
                if (subaction.getRenderer() instanceof MenuActionRenderer submenuRenderer) {
                    var submenu = submenuRenderer.render(subaction, actionEventBuilder);
                    popup.appendChild(submenu);
                } else if (subaction instanceof ActionPlaceholder) {
                    popup.appendChild(new Menuseparator());
                } else {
                    var menuitem = itemRenderer.render(subaction, actionEventBuilder);
                    popup.appendChild(menuitem);
                }
            });
        }

        var menubar = new Menubar();
        menubar.appendChild(menu);
        return menubar;
    }

    public List<Action> getActionItems() {
        return actionItems;
    }

    public <T extends Action> void setActionItems(List<T> actionItems) {
        this.actionItems = (List<Action>) actionItems;
    }

    public <T extends Action> void setActionItems(Collection<T> actionItems) {
        setActionItems(actionItems.stream().toList());
    }
}
