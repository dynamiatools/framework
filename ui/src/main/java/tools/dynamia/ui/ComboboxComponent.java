package tools.dynamia.ui;

import java.util.List;

public interface ComboboxComponent<T> extends UIComponent {

    void setData(List<T> items);

    List<T> getData();

    T getSelected();

    void setSelected(T item);

    int getSelectedIndex();

    void setSelectedIndex(int index);

    void clear();

    void refresh();

    void setVflex(String vflex);

    String getVflex();

    void setHflex(String hflex);

    String getHflex();

    void setItemRenderer(Object itemRenderer);
}
