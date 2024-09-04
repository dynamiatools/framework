package tools.dynamia.ui;

import java.util.List;

/**
 * Interface to provide utilities in UI backends like ZK, Vaadin or others
 */
public interface UIToolsProvider {

    /**
     * Check if current thread is the event thread
     *
     * @return true if current thread is the event thread
     */
    boolean isInEventThread();


    /**
     * Create a dialog
     *
     * @param title the title
     * @return the dialog
     */
    DialogComponent createDialog(String title);

    /**
     * Show a dialog
     *
     * @param title   the title
     * @param content the content
     * @return the dialog
     */
    default DialogComponent showDialog(String title, Object content) {
        return showDialog(title, content, null, null, null, null);
    }

    /**
     * Show a dialog
     *
     * @param title   the title
     * @param content the content
     * @param width   the width
     * @param height  the height
     * @return the dialog
     */
    default DialogComponent showDialog(String title, Object content, String width, String height) {
        return showDialog(title, content, null, width, height, null);
    }

    /**
     * Show a dialog
     *
     * @param title   the title
     * @param content the content
     * @param onClose the on close callback
     * @return the dialog
     */
    default DialogComponent showDialog(String title, Object content, EventCallback onClose) {
        return showDialog(title, content, null, null, null, onClose);
    }

    /**
     * Show a dialog
     *
     * @param title   the title
     * @param content the content
     * @param data    the data
     * @param width   the width
     * @param height  the height
     * @param onClose the on close callback
     * @return the dialog
     */
    DialogComponent showDialog(String title, Object content, Object data, String width, String height, EventCallback onClose);


    /**
     * Create a listbox
     *
     * @param items the items
     * @param <T>   the type
     * @return the listbox
     */
    <T> ListboxComponent<T> createListbox(List<T> items);

    /**
     * Create a combobox
     *
     * @param items the items
     * @param <T>   the type
     * @return the combobox
     */
    <T> ComboboxComponent<T> createCombobox(List<T> items);

    /**
     * Show a listbox selector
     *
     * @param title    the title
     * @param data     the data
     * @param onSelect the on select callback
     * @param <T>      the type
     * @return the dialog
     */
    default <T> DialogComponent showListboxSelector(String title, List<T> data, SelectEventCallback<T> onSelect) {
        return showListboxSelector(title, data, null, onSelect);
    }

    /**
     * Show a listbox selector
     *
     * @param title            the title
     * @param data             the data
     * @param defaultSelection the default selection
     * @param onSelect         the on select callback
     * @param <T>              the type
     * @return the dialog
     */
    default <T> DialogComponent showListboxSelector(String title, List<T> data, T defaultSelection, SelectEventCallback<T> onSelect) {
        var listbox = createListbox(data);
        var dialog = showDialog(title, listbox, "500px", "500px");
        listbox.setWidth("100%");
        listbox.setHeight("100%");
        if (defaultSelection != null) {
            listbox.setSelected(defaultSelection);
        }
        listbox.onSelect(value -> {
            if (onSelect != null) {
                onSelect.onSelect(value);
            }
            dialog.close();
        });
        return dialog;

    }


    /**
     * Show a listbox multi selector
     *
     * @param title    the title
     * @param data     the data
     * @param onSelect the on select callback
     * @param <T>      the type
     * @return the dialog
     */
    default <T> DialogComponent showListboxMultiSelector(String title, String label, List<T> data, SelectionEventCallback<T> onSelect) {
        return showListboxMultiSelector(title, label, data, null, onSelect);
    }

    /**
     * Show a listbox multi selector
     *
     * @param title            the title
     * @param data             the data
     * @param defaultSelection the default selection
     * @param onSelect         the on select callback
     * @param <T>              the type
     * @return the dialog
     */
    default <T> DialogComponent showListboxMultiSelector(String title, String label, List<T> data, List<T> defaultSelection, SelectionEventCallback<T> onSelect) {
        var listbox = createListbox(data);
        var dialog = showDialog(title, listbox, "500px", "500px");
        var button = createButton(label);
        dialog.add(button);

        listbox.setWidth("100%");
        listbox.setHeight("100%");
        listbox.setMultiple(true);
        if (defaultSelection != null) {
            listbox.setSelection(defaultSelection);
        }
        button.onClick(() -> {
            if (onSelect != null) {
                onSelect.onSelect(listbox.getSelection());
            }
            dialog.close();
        });
        return dialog;
    }

    /**
     * Create a button
     *
     * @param label the label
     * @return the button
     */
    default ButtonComponent createButton(String label) {
        return createButton(label, null);
    }

    /**
     * Create a button
     *
     * @param label   the label
     * @param onClick the on click callback
     * @return the button
     */
    ButtonComponent createButton(String label, EventCallback onClick);

}
