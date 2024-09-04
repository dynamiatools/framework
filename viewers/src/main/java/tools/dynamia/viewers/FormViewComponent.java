package tools.dynamia.viewers;

import java.util.List;
import java.util.Map;

/**
 * Basic interface for Form view implementations
 *
 * @param <T>
 * @param <C>
 */
public interface FormViewComponent<T, C> extends View<T> {
    boolean isAutosaveBindings();

    void setAutosaveBindings(boolean autosaveBindings);

    void saveBindings();

    IFormFieldComponent<C> getFieldComponent(String fieldName);

    IFormFieldGroupComponent<C> getFieldGroupComponent(String groupName);

    void updateUI();

    void addSubview(String title, View subview);

    List<View> getSubviews();

    Map<String, ? extends IFormFieldComponent<C>> getComponentsFieldsMap();

    Map<String, ? extends IFormFieldGroupComponent<C>> getGroupsComponentsMap();

    T getRawValue();

    String getTitle();

    void setTitle(String title);

    @Override
    Object getSource();

    @Override
    void setSource(Object source);

    String getCustomView();

    void setCustomView(String customView);

    void clearActions();
}
