package tools.dynamia.ui;

@FunctionalInterface
public interface SelectEventCallback<T> {

    void onSelect(T value);
}
