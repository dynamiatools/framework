package tools.dynamia.ui;

/**
 * The Interface ButtonComponent. Represents a UI button component with basic operations.
 * This interface defines the essential functionality for button components in user interfaces,
 * including label and icon management, as well as click event handling. It's commonly used
 * by UI framework adapters to provide a consistent button API across different UI technologies
 * like Swing, JavaFX, web frameworks, or mobile interfaces.
 * <br><br>
 * <b>Usage:</b><br>
 * <br>
 * <code>
 * ButtonComponent saveButton = uiFactory.createButton();
 * saveButton.setLabel("Save");
 * saveButton.setIcon("fa-save");
 * saveButton.onClick(() -> saveDocument());
 * 
 * // Or with method reference
 * saveButton.onClick(this::handleSaveAction);
 * </code>
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
