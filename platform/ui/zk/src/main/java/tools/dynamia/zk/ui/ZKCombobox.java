package tools.dynamia.zk.ui;

import org.zkoss.zul.Combobox;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.ListModelList;
import tools.dynamia.ui.ComboboxComponent;
import tools.dynamia.ui.UIComponent;

import java.util.List;

/**
 * ZK implementation of {@link ComboboxComponent}
 */
public class ZKCombobox<T> extends Combobox implements ComboboxComponent<T> {

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
        if (itemRenderer instanceof ComboitemRenderer<?> renderer) {
            super.setItemRenderer(renderer);
        }
    }

}
