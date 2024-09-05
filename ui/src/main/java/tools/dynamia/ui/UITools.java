package tools.dynamia.ui;

import tools.dynamia.integration.Containers;

import java.util.List;

public class UITools {

    private static UIToolsProvider currentProvider;


    private static UIToolsProvider getProvider() {
        if (currentProvider == null) {
            currentProvider = Containers.get().findObject(UIToolsProvider.class);
            if (currentProvider == null) {
                throw new IllegalStateException("UIToolsProvider not found");
            }
        }
        return currentProvider;
    }

    public static void setCurrentProvider(UIToolsProvider currentProvider) {
        UITools.currentProvider = currentProvider;
    }


    /**
     * Check if current thread is the event thread
     *
     * @return true if current thread is the event thread
     */
    public static boolean isInEventThread() {
        return getProvider().isInEventThread();
    }

    /**
     * Create a dialog
     *
     * @param title the title
     * @return the dialog
     */
    public static DialogComponent dialog(String title) {
        return getProvider().createDialog(title);
    }

    /**
     * Create a dialog
     *
     * @param title    the title
     * @param chidlren the children
     * @return the dialog
     */
    public static DialogComponent dialog(String title, List<UIComponent> chidlren) {
        var dialog = dialog(title);
        if (chidlren != null) {
            chidlren.forEach(dialog::add);
        }
        return dialog;
    }

    /**
     * Show a dialog
     *
     * @param title   the title
     * @param content the content
     * @return the dialog
     */
    public static DialogComponent showDialog(String title, Object content) {
        return getProvider().showDialog(title, content);
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
    public static DialogComponent showDialog(String title, Object content, String width, String height) {
        return getProvider().showDialog(title, content, width, height);
    }

    /**
     * Show a dialog
     *
     * @param title   the title
     * @param content the content
     * @param onClose the on close callback
     * @return the dialog
     */
    public static DialogComponent showDialog(String title, Object content, EventCallback onClose) {
        return getProvider().showDialog(title, content, onClose);
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
    public static DialogComponent showDialog(String title, Object content, Object data, String width, String height, EventCallback onClose) {
        return getProvider().showDialog(title, content, data, width, height, onClose);
    }

    /**
     * Create a listbox
     *
     * @param items the items
     * @param <T>   the type
     * @return the listbox
     */
    public static <T> ListboxComponent<T> listbox(List<T> items) {
        return getProvider().createListbox(items);
    }

    /**
     * Create a combobox
     *
     * @param items the items
     * @param <T>   the type
     * @return the combobox
     */
    public static <T> ComboboxComponent<T> combobox(List<T> items) {
        return getProvider().createCombobox(items);
    }

    /**
     * Show a listbox selector
     *
     * @param title    the title
     * @param data     the data
     * @param onSelect the on select callback
     * @param <T>      the type
     * @return the dialog
     */
    public static <T> DialogComponent showListboxSelector(String title, List<T> data, SelectEventCallback<T> onSelect) {
        return getProvider().showListboxSelector(title, data, onSelect);
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
    public static <T> DialogComponent showListboxSelector(String title, List<T> data, T defaultSelection, SelectEventCallback<T> onSelect) {
        return getProvider().showListboxSelector(title, data, defaultSelection, onSelect);
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
    public static <T> DialogComponent showListboxMultiSelector(String title, String label, List<T> data, SelectionEventCallback<T> onSelect) {
        return getProvider().showListboxMultiSelector(title, label, data, onSelect);
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
    public static <T> DialogComponent showListboxMultiSelector(String title, String label, List<T> data, List<T> defaultSelection, SelectionEventCallback<T> onSelect) {
        return getProvider().showListboxMultiSelector(title, label, data, defaultSelection, onSelect);
    }

    /**
     * Create a button
     *
     * @param label the label
     * @return the button
     */
    public static ButtonComponent button(String label) {
        return getProvider().createButton(label);
    }

    /**
     * Create a button
     *
     * @param label   the label
     * @param onClick the on click callback
     * @return the button
     */
    public static ButtonComponent button(String label, EventCallback onClick) {
        return getProvider().createButton(label, onClick);
    }


}
