package tools.dynamia.viewers;

import java.util.List;
import java.util.Map;

/**
 * Core interface for form-based view components in the Dynamia Tools framework.
 * This interface defines the contract for UI components that render entity data as forms,
 * providing capabilities for field binding, validation, grouping, and dynamic UI updates.
 *
 * <p>FormViewComponent implementations are responsible for rendering entity properties as
 * form fields (textboxes, checkboxes, dropdowns, etc.), managing data binding between the UI
 * and domain model, organizing fields into groups, and handling user interactions. They support
 * both automatic data binding and manual control over persistence operations.</p>
 *
 * <h3>Key Features</h3>
 * <ul>
 *   <li>Automatic field rendering based on view descriptors</li>
 *   <li>Two-way data binding between UI components and entity properties</li>
 *   <li>Field grouping and layout management</li>
 *   <li>Integration with subviews (master-detail forms)</li>
 *   <li>Custom view template support</li>
 *   <li>Action management (save, cancel, etc.)</li>
 *   <li>Flexible binding strategies (auto-save or manual)</li>
 * </ul>
 *
 * <h3>Basic Usage</h3>
 * <pre>{@code
 * // Create a form view for a Customer entity
 * FormViewComponent<Customer, Component> formView = new FormView<>();
 * formView.setViewDescriptor(customerFormDescriptor);
 * formView.setValue(customer);
 *
 * // Access specific fields
 * IFormFieldComponent<Component> nameField = formView.getFieldComponent("name");
 * nameField.setValue("John Doe");
 *
 * // Save bindings manually
 * formView.saveBindings();
 * Customer updatedCustomer = formView.getValue();
 * }</pre>
 *
 * <h3>Auto-save Bindings</h3>
 * <pre>{@code
 * // Enable automatic binding updates
 * formView.setAutosaveBindings(true);
 * // Now changes in UI components are immediately reflected in the entity
 * // No need to call saveBindings() manually
 * }</pre>
 *
 * <h3>Field Groups</h3>
 * <pre>{@code
 * // Access field groups for conditional visibility or styling
 * IFormFieldGroupComponent<Component> addressGroup =
 *     formView.getFieldGroupComponent("address");
 * if (addressGroup != null) {
 *     addressGroup.setVisible(customer.getCountry() != null);
 * }
 * }</pre>
 *
 * <h3>View Descriptor Configuration</h3>
 * <p>Form views are typically configured through YAML descriptors:</p>
 * <pre>{@code
 * # CustomerForm.yml
 * view: form
 * beanClass: com.myapp.domain.Customer
 *
 * fields:
 *   name:
 *     label: Full Name
 *   email:
 *     params:
 *       type: email
 *   address:
 *     group: addressInfo
 *
 * groups:
 *   - name: addressInfo
 *     title: Address Information
 *     columns: 2
 * }</pre>
 *
 * <h3>Subviews (Master-Detail)</h3>
 * <pre>{@code
 * // Add a subview for child entities
 * TableView<OrderItem> itemsView = new TableView<>();
 * itemsView.setViewDescriptor(orderItemsDescriptor);
 * formView.addSubview("Order Items", itemsView);
 * }</pre>
 *
 * <h3>Custom Views</h3>
 * <p>You can specify custom ZUL templates for specialized form layouts:</p>
 * <pre>{@code
 * formView.setCustomView("/custom/customer-form.zul");
 * // The custom view will be loaded instead of auto-generated layout
 * }</pre>
 *
 * @param <T> the type of the entity/value object displayed in the form
 * @param <C> the type of the UI component used by the implementation (e.g., ZK Component, JavaFX Node)
 * @see View
 * @see IFormFieldComponent
 * @see IFormFieldGroupComponent
 * @see ViewDescriptor
 */
public interface FormViewComponent<T, C> extends View<T> {

    /**
     * Checks if automatic binding save is enabled.
     * When true, changes in UI components are immediately written back to the entity.
     * When false, {@link #saveBindings()} must be called manually to persist UI changes.
     *
     * @return true if auto-save bindings is enabled, false otherwise
     */
    boolean isAutosaveBindings();

    /**
     * Sets whether to automatically save bindings when UI components change.
     *
     * <p><strong>Auto-save enabled (true):</strong> Changes in any form field are immediately
     * reflected in the entity. This is convenient but may trigger frequent updates.</p>
     *
     * <p><strong>Auto-save disabled (false):</strong> Changes remain in the UI until
     * {@link #saveBindings()} is explicitly called. This provides better control over
     * when the entity is updated.</p>
     *
     * @param autosaveBindings true to enable auto-save, false to require manual save
     */
    void setAutosaveBindings(boolean autosaveBindings);

    /**
     * Saves all pending UI bindings to the entity.
     * This method transfers values from form field components back to the entity's properties,
     * effectively synchronizing the UI state with the domain model.
     *
     * <p>This method should be called before retrieving the updated entity with {@link #getValue()}
     * when auto-save bindings is disabled.</p>
     *
     * <p>Example:</p>
     * <pre>{@code
     * formView.saveBindings();
     * Customer customer = formView.getValue();
     * crudService.update(customer);
     * }</pre>
     */
    void saveBindings();

    /**
     * Gets the form field component for a specific entity property.
     * This allows direct access to individual field components for programmatic manipulation,
     * such as changing visibility, enabling/disabling, or setting values.
     *
     * @param fieldName the name of the entity property/field (e.g., "name", "email")
     * @return the field component instance, or null if the field does not exist
     *
     * <p>Example:</p>
     * <pre>{@code
     * IFormFieldComponent<Component> emailField = formView.getFieldComponent("email");
     * if (emailField != null) {
     *     emailField.setVisible(customer.isEmailEnabled());
     * }
     * }</pre>
     */
    IFormFieldComponent<C> getFieldComponent(String fieldName);

    /**
     * Gets the form field group component by name.
     * Field groups are used to organize related fields together, typically displayed
     * in sections or panels with optional titles and borders.
     *
     * @param groupName the name of the field group as defined in the view descriptor
     * @return the field group component instance, or null if the group does not exist
     *
     * <p>Example:</p>
     * <pre>{@code
     * IFormFieldGroupComponent<Component> contactGroup =
     *     formView.getFieldGroupComponent("contactInfo");
     * if (contactGroup != null) {
     *     contactGroup.setTitle("Contact Information");
     * }
     * }</pre>
     */
    IFormFieldGroupComponent<C> getFieldGroupComponent(String groupName);

    /**
     * Updates the user interface to reflect the current state of the entity.
     * This method refreshes all form field components, ensuring they display the latest
     * values from the entity. It is useful after programmatic changes to the entity
     * or when re-binding the form to a different entity instance.
     *
     * <p>This method does not save bindings; it only updates the UI to match the entity state.</p>
     */
    void updateUI();

    /**
     * Adds a subview to the form with the specified title.
     * Subviews are typically used for master-detail relationships, where the form (master)
     * contains one or more child views (details) such as tables or nested forms.
     *
     * <p>For example, an Order form might have a subview displaying OrderItems in a table.</p>
     *
     * @param title the title/label for the subview section
     * @param subview the view component to add (typically a TableView or another FormView)
     *
     * <p>Example:</p>
     * <pre>{@code
     * TableView<OrderItem> itemsTable = new TableView<>();
     * itemsTable.setViewDescriptor(itemsDescriptor);
     * orderFormView.addSubview("Items", itemsTable);
     * }</pre>
     */
    void addSubview(String title, View subview);

    /**
     * Gets all subviews that have been added to this form.
     * Subviews represent child or related entities displayed within the form context.
     *
     * @return a list of all subview components, or an empty list if no subviews exist
     */
    List<View> getSubviews();

    /**
     * Gets a map of all form field components indexed by field name.
     * This provides access to the complete set of fields rendered in the form,
     * useful for bulk operations or validation.
     *
     * @return a map where keys are field names and values are field components
     *
     * <p>Example:</p>
     * <pre>{@code
     * Map<String, ? extends IFormFieldComponent<Component>> fields =
     *     formView.getComponentsFieldsMap();
     * for (Map.Entry<String, ? extends IFormFieldComponent<Component>> entry : fields.entrySet()) {
     *     if (entry.getValue().getValue() == null) {
     *         System.out.println("Field " + entry.getKey() + " is empty");
     *     }
     * }
     * }</pre>
     */
    Map<String, ? extends IFormFieldComponent<C>> getComponentsFieldsMap();

    /**
     * Gets a map of all field group components indexed by group name.
     * This provides access to all field groups defined in the view descriptor,
     * useful for controlling group visibility or styling.
     *
     * @return a map where keys are group names and values are group components
     */
    Map<String, ? extends IFormFieldGroupComponent<C>> getGroupsComponentsMap();

    /**
     * Gets the raw entity value without triggering binding save.
     * This method returns the current entity instance as-is, without synchronizing
     * UI changes back to the entity. Useful when you need to access the entity
     * but don't want to apply pending UI changes yet.
     *
     * @return the raw entity instance
     * @see #getValue() which may trigger binding save depending on implementation
     */
    T getRawValue();

    /**
     * Gets the form title displayed to users.
     * The title typically appears at the top of the form and describes the entity
     * being edited (e.g., "Edit Customer", "New Order").
     *
     * @return the form title, or null if no title is set
     */
    String getTitle();

    /**
     * Sets the form title displayed to users.
     * This is useful for dynamically changing the form header based on context
     * (e.g., "Create Customer" vs "Edit Customer").
     *
     * @param title the title to display
     */
    void setTitle(String title);

    /**
     * {@inheritDoc}
     *
     * @return the source object or descriptor that created this view
     */
    @Override
    Object getSource();

    /**
     * {@inheritDoc}
     *
     * @param source the source object or descriptor
     */
    @Override
    void setSource(Object source);

    /**
     * Gets the custom view template path if one has been configured.
     * Custom views are typically ZUL files that define a specialized layout
     * instead of using the automatic form generation.
     *
     * @return the custom view template path, or null if using auto-generated layout
     */
    String getCustomView();

    /**
     * Sets a custom view template to use for rendering the form.
     * When set, the form view will load the specified template instead of
     * automatically generating the layout from the view descriptor.
     *
     * <p>This is useful for complex forms that require custom layouts, special
     * components, or specific styling that cannot be achieved with standard field rendering.</p>
     *
     * @param customView the path to the custom view template (e.g., "/views/custom-form.zul")
     *
     * <p>Example:</p>
     * <pre>{@code
     * formView.setCustomView("/custom/customer-form.zul");
     * // The form will now use the custom ZUL instead of auto-generation
     * }</pre>
     */
    void setCustomView(String customView);

    /**
     * Clears all actions from the form view.
     * Actions are typically buttons or menu items displayed in the form (save, cancel, delete, etc.).
     * This method removes all configured actions, useful when you want to provide a custom
     * action set or create a read-only form without action buttons.
     *
     * <p>After clearing actions, you can add custom actions using the view's action management methods.</p>
     */
    void clearActions();
}
