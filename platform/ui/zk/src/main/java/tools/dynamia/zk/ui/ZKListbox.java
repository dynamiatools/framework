package tools.dynamia.zk.ui;

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.ListitemRenderer;
import tools.dynamia.ui.ListboxComponent;
import tools.dynamia.ui.SelectEventCallback;
import tools.dynamia.ui.SelectionEventCallback;

import java.util.List;

/**
 * ZK implementation of {@link ListboxComponent}
 */
public class ZKListbox<T> extends Listbox implements ListboxComponent<T> {

    private ListModelList<T> model;
    private List<T> data;

    @Override
    public void setData(List<T> data) {
        this.data = data;
        model = new ListModelList<>(data);
        setModel(model);
    }

    @Override
    public List<T> getData() {
        return data;
    }

    @Override
    public T getSelected() {
        if (model != null) {
            var selection = model.getSelection();
            if (selection != null) {
                return selection.stream().findFirst().orElse(null);
            }
        }
        return null;
    }

    @Override
    public List<T> getSelection() {
        if (model != null) {
            return model.getSelection().stream().toList();
        }
        return List.of();
    }

    @Override
    public void setSelection(List<T> selection) {
        if (model != null) {
            model.setSelection(selection);
        }
    }

    @Override
    public void setSelected(T item) {
        if (model != null && item != null) {
            model.addToSelection(item);
        }
    }

    @Override
    public void clear() {
        if (model != null) {
            model.clear();
        }
    }

    @Override
    public void refresh() {
        setData(data);
    }

    @Override
    public void setItemRenderer(Object itemRenderer) {
        if (itemRenderer instanceof ListitemRenderer<?> listitemRenderer) {
            super.setItemRenderer(listitemRenderer);
        }
    }

    @Override
    public void setMultiple(boolean multiple) {
        super.setMultiple(multiple);
        setCheckmark(multiple);
    }

    @Override
    public void onSelect(SelectEventCallback<T> onSelect) {
        addEventListener(Events.ON_SELECT, event -> onSelect.onSelect(getSelected()));
    }

    @Override
    public void onSelection(SelectionEventCallback<T> onSelection) {
        addEventListener(Events.ON_SELECT, event -> onSelection.onSelect(getSelection()));
    }

}
