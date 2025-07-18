package tools.dynamia.ui;

/**
 * The Interface ButtonComponent. Represents a UI button component with basic operations.
 *
 * @author Mario A. Serrano Leones
 */
public interface ButtonComponent extends UIComponent {

    /**
     * Sets the label text of the button.
     *
     * @param label the label text
     */
    void setLabel(String label);

    /**
     * Gets the label text of the button.
     *
     * @return the label text
     */
    String getLabel();

    /**
     * Sets the icon of the button.
     *
     * @param icon the icon name or path
     */
    void setIcon(String icon);

    /**
     * Gets the icon of the button.
     *
     * @return the icon name or path
     */
    String getIcon();

    /**
     * Sets the click event callback for this button.
     *
     * @param onClick the event callback
     */
    void onClick(EventCallback onClick);
}
