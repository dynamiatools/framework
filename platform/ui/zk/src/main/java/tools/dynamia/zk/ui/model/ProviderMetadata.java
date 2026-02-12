package tools.dynamia.zk.ui.model;

/**
 * Interface for objects that provide metadata for display in UI components.
 * <p>
 * This interface defines a contract for objects that can be presented in selection components,
 * lists, or other UI elements that require identifying information and visual representation.
 * It provides the essential metadata needed to display and identify providers or options in
 * user interfaces, particularly useful for comboboxes, listboxes, and picker components.
 * </p>
 *
 * <p>
 * <b>Common use cases:</b>
 * <ul>
 *   <li>Service providers in selection components</li>
 *   <li>Plugin or module metadata for configuration interfaces</li>
 *   <li>Template or theme selectors</li>
 *   <li>Integration endpoint options</li>
 *   <li>Report or export format selectors</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Usage example:</b>
 * <pre>{@code
 * public class PaymentProvider implements ProviderMetadata {
 *     private String id;
 *     private String name;
 *     private String icon;
 *
 *     @Override
 *     public String getId() {
 *         return id; // e.g., "paypal"
 *     }
 *
 *     @Override
 *     public String getName() {
 *         return name; // e.g., "PayPal"
 *     }
 *
 *     @Override
 *     public String getIcon() {
 *         return icon; // e.g., "fa fa-paypal"
 *     }
 * }
 *
 * // Usage in UI
 * List<PaymentProvider> providers = paymentService.getProviders();
 * providerCombobox.setModel(new ListModelList<>(providers));
 * }</pre>
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
public interface ProviderMetadata {

    /**
     * Returns the unique identifier of this provider.
     * <p>
     * The ID is typically used for programmatic reference, configuration keys,
     * and internal processing. It should be unique, stable, and URL-safe.
     * </p>
     *
     * @return the provider's unique identifier
     */
    String getId();

    /**
     * Returns the display name of this provider.
     * <p>
     * The name is the human-readable label shown to users in UI components.
     * It should be descriptive and user-friendly.
     * </p>
     *
     * @return the provider's display name
     */
    String getName();

    /**
     * Returns the icon identifier or CSS class for this provider.
     * <p>
     * The icon can be a Font Awesome class (e.g., "fa fa-user"), an image path,
     * or an icon identifier that the UI framework can resolve to a visual representation.
     * </p>
     *
     * @return the provider's icon identifier or CSS class
     */
    String getIcon();
}
