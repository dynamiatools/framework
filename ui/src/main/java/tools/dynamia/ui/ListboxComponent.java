package tools.dynamia.ui;

import java.util.List;

public interface ListboxComponent<T> extends UIComponent {

    void setData(List<T> data);

    List<T> getData();

    T getSelected();

    void setSelected(T item);

    List<T> getSelection();

    void setSelection(List<T> items);

    int getSelectedIndex();

    void setSelectedIndex(int index);

    void clear();

    void refresh();

    void setItemRenderer(Object itemRenderer);

    void setMultiple(boolean multiple);

    boolean isMultiple();

    void onSelect(SelectEventCallback<T> onSelect);

    void onSelection(SelectionEventCallback<T> onSelection);

}
