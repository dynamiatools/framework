package tools.dynamia.zk.ui;

import tools.dynamia.integration.sterotypes.Component;
import tools.dynamia.ui.*;
import tools.dynamia.web.util.HttpUtils;
import tools.dynamia.zk.util.ZKUtil;

import java.util.List;

/**
 * ZK Implementation of {@link UIToolsProvider}
 * This class is used to provide UI tools for the ZK UI framework.
 * It is annotated with @Component to make it a Spring-managed bean.
 */
@Component
public class ZKUIToolsProvider implements UIToolsProvider {

    @Override
    public boolean isInEventThread() {
        return ZKUtil.isInEventListener();
    }

    @Override
    public DialogComponent createDialog(String title) {
        ZKDialog dialog = new ZKDialog();
        dialog.setTitle(title);
        dialog.setClosable(true);
        dialog.setBorder("normal");
        dialog.setPage(ZKUtil.getFirstPage());
        return dialog;
    }

    @Override
    public DialogComponent showDialog(String title, Object content, Object data, String width, String height, EventCallback onClose) {
        var dialog = createDialog(title);
        dialog.setContent(content);
        dialog.setData(data);
        dialog.onClose(onClose);
        dialog.setWidth(width);
        dialog.setHeight(height);
        dialog.show();


        if (HttpUtils.isSmartphone()) {
            dialog.setWidth("99%");
            dialog.setHeight("99%");
            dialog.setDraggable(false);
        }


        return dialog;
    }

    @Override
    public <T> ListboxComponent<T> createListbox(List<T> data) {
        ZKListbox<T> listbox = new ZKListbox<>();
        listbox.setData(data);
        return listbox;
    }

    @Override
    public <T> ComboboxComponent<T> createCombobox(List<T> data) {
        ZKCombobox<T> combobox = new ZKCombobox<>();
        combobox.setData(data);
        return combobox;
    }

    @Override
    public ButtonComponent createButton(String label, EventCallback onClick) {
        ZKButton button = new ZKButton();
        button.setLabel(label);
        if (onClick != null) {
            button.onClick(onClick);
        }
        return button;
    }

    @Override
    public <T> DialogComponent showTableSelector(String title, String label, List<String> columns, List<T> data, SelectEventCallback<T> onSelect) {
        ZKTableSelector<T> selector = new ZKTableSelector<>(label, columns, data);
        selector.onSelect(onSelect);
        return showDialog(title, selector, null, "90%", "90%", null);
    }

    @Override
    public <T> DialogComponent showTableMultiSelector(String title, String label, List<String> columns, List<T> data, SelectionEventCallback<T> onSelect) {
        ZKTableSelector<T> selector = new ZKTableSelector<>(label, columns, data, true);
        selector.onSelection(onSelect);
        return showDialog(title, selector, null, "90%", "90%", null);
    }
}
