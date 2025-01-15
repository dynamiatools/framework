package tools.dynamia.app.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import tools.dynamia.app.controllers.ApplicationMetadataController;
import tools.dynamia.viewers.ViewDescriptor;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ViewDescriptorMetadata extends BasicMetadata {

    private String view;
    private String device;
    private String beanClass;

    @JsonIgnore
    private ViewDescriptor descriptor;


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

    public ViewDescriptorMetadata() {
    }

    public ViewDescriptor getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(ViewDescriptor descriptor) {
        this.descriptor = descriptor;
    }


    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(String beanClass) {
        this.beanClass = beanClass;
    }
}
