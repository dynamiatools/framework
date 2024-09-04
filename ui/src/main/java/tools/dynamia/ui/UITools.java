package tools.dynamia.ui;

import tools.dynamia.integration.Containers;

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
    public DialogComponent createDialog(String title) {
        return getProvider().createDialog(title);
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


}
