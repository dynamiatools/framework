package tools.dynamia.app.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import tools.dynamia.app.controllers.ApplicationMetadataController;
import tools.dynamia.viewers.ViewDescriptor;

/**
 * Metadata information for a view descriptor in the application.
 * <p>
 * This class encapsulates properties and configuration details for view descriptors, such as view type, device, bean class, and endpoints.
 * It is used to describe views in a way that can be serialized and consumed by clients or other layers of the application.
 * <p>
 * The metadata is typically generated from a {@link ViewDescriptor} instance and includes information relevant for UI rendering and execution.
 *
 * @author Mario A. Serrano Leones
 * @since 2023
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ViewDescriptorMetadata extends BasicMetadata {

    /**
     * The type of view (e.g., form, list, detail).
     */
    private String view;
    /**
     * The device type for which this view is intended (e.g., desktop, mobile).
     */
    private String device;
    /**
     * The fully qualified class name of the bean associated with this view.
     */
    private String beanClass;
    /**
     * The underlying {@link ViewDescriptor} instance. This field is ignored during JSON serialization.
     */
    @JsonIgnore
    private ViewDescriptor descriptor;

    /**
     * Constructs a {@code ViewDescriptorMetadata} from the given {@link ViewDescriptor} instance.
     * <p>
     * Copies relevant properties from the descriptor, including id, view type, device, bean class, and sets the endpoint.
     *
     * @param descriptor the {@link ViewDescriptor} instance to extract metadata from
     */
    public ViewDescriptorMetadata(ViewDescriptor descriptor) {
        setId(descriptor.getId());
        this.descriptor = descriptor;
        this.view = descriptor.getViewTypeName();
        this.device = descriptor.getDevice();
        this.beanClass = descriptor.getBeanClass() != null ? descriptor.getBeanClass().getName() : null;
        if (beanClass != null) {
            setEndpoint(ApplicationMetadataController.PATH + "/entities/" + beanClass + "/views/" + view);
        }
    }

    /**
     * Default constructor for serialization and manual instantiation.
     */
    public ViewDescriptorMetadata() {
    }

    /**
     * Returns the underlying {@link ViewDescriptor} instance. This field is ignored during JSON serialization and is intended for internal use only.
     * @return the {@link ViewDescriptor} instance
     */
    public ViewDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Sets the underlying {@link ViewDescriptor} instance. This field is ignored during JSON serialization and is intended for internal use only.
     * @param descriptor the {@link ViewDescriptor} instance to set
     */
    public void setDescriptor(ViewDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    /**
     * Returns the type of view (e.g., form, list, detail).
     * @return the view type
     */
    public String getView() {
        return view;
    }

    /**
     * Sets the type of view (e.g., form, list, detail).
     * @param view the view type to set
     */
    public void setView(String view) {
        this.view = view;
    }

    /**
     * Returns the device type for which this view is intended (e.g., desktop, mobile).
     * @return the device type
     */
    public String getDevice() {
        return device;
    }

    /**
     * Sets the device type for which this view is intended (e.g., desktop, mobile).
     * @param device the device type to set
     */
    public void setDevice(String device) {
        this.device = device;
    }

    /**
     * Returns the fully qualified class name of the bean associated with this view.
     * @return the bean class name
     */
    public String getBeanClass() {
        return beanClass;
    }

    /**
     * Sets the fully qualified class name of the bean associated with this view.
     * @param beanClass the bean class name to set
     */
    public void setBeanClass(String beanClass) {
        this.beanClass = beanClass;
    }
}
