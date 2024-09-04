package tools.dynamia.ui;

import java.util.List;

@FunctionalInterface
public interface SelectionEventCallback<T> {

    void onSelect(List<T> selection);
}
