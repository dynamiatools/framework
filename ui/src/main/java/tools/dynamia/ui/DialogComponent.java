package tools.dynamia.ui;

/**
 * The Interface DialogComponent. Represents a UI dialog component with configurable properties.
 * This interface defines the essential functionality for modal and non-modal dialog windows
 * in user interfaces. It provides comprehensive control over dialog presentation, content
 * management, user interaction, and lifecycle events. Dialogs are commonly used for forms,
 * confirmations, detailed views, and complex user interactions that require focused attention.
 * <br><br>
 * <b>Usage:</b><br>
 * <br>
 * <code>
 * DialogComponent dialog = uiFactory.createDialog();
 * dialog.setTitle("User Details");
 * dialog.setWidth("600px");
 * dialog.setHeight("400px");
 * dialog.setContent(userFormPanel);
 * dialog.setDraggable(true);
 * dialog.onClose(() -> refreshUserList());
 * dialog.show();
 * </code>
 *
 * @author Mario A. Serrano Leones
 */
public interface DialogComponent extends UIComponent{

    /**
     * Sets the title of the dialog.
     *
     * @param title the dialog title
     */
    void setTitle(String title);

    /**
     * Gets the title of the dialog.
     *
     * @return the dialog title
     */
    String getTitle();

    /**
     * Sets the width of the dialog.
     *
     * @param width the dialog width
     */
    void setWidth(String width);

    /**
     * Gets the width of the dialog.
     *
     * @return the dialog width
     */
    String getWidth();

    /**
     * Sets the height of the dialog.
     *
     * @param height the dialog height
     */
    void setHeight(String height);

    /**
     * Gets the height of the dialog.
     *
     * @return the dialog height
     */
    String getHeight();

    /**
     * Sets the content of the dialog.
     *
     * @param content the dialog content
     */
    void setContent(Object content);

    /**
     * Gets the content of the dialog.
     *
     * @return the dialog content
     */
    Object getContent();

    /**
     * Sets associated data for the dialog.
     *
     * @param data the data object
     */
    void setData(Object data);

    /**
     * Gets associated data for the dialog.
     *
     * @return the data object
     */
    Object getData();

    /**
     * Sets the callback to execute when the dialog is closed.
     *
     * @param callback the close event callback
     */
    void onClose(EventCallback callback);

    /**
     * Sets whether the dialog is draggable.
     *
     * @param draggable true if draggable, false otherwise
     */
    void setDraggable(boolean draggable);

    /**
     * Checks if the dialog is draggable.
     *
     * @return true if draggable, false otherwise
     */
    boolean isDraggable();

    /**
     * Sets whether the dialog is closable.
     *
     * @param closable true if closable, false otherwise
     */
    void setClosable(boolean closable);

    /**
     * Checks if the dialog is closable.
     *
     * @return true if closable, false otherwise
     */
    boolean isClosable();

    /**
     * Shows the dialog.
     */
    void show();

    /**
     * Closes the dialog.
     */
    void close();

}
