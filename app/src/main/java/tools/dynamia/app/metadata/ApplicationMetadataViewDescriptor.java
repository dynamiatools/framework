package tools.dynamia.app.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import tools.dynamia.viewers.ViewDescriptor;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationMetadataViewDescriptor {

    private String id;
    private String view;
    private String device;
    private String beanClass;

    @JsonIgnore
    private ViewDescriptor descriptor;



    public ApplicationMetadataViewDescriptor(ViewDescriptor descriptor) {
        this.descriptor = descriptor;
        this.id = descriptor.getId();
        this.view = descriptor.getViewTypeName();
        this.device = descriptor.getDevice();
        this.beanClass = descriptor.getBeanClass() != null ? descriptor.getBeanClass().getName() : null;
    }

    public ApplicationMetadataViewDescriptor() {
    }

    public ViewDescriptor getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(ViewDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
