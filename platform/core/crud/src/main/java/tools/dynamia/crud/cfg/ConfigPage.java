package tools.dynamia.crud.cfg;

import tools.dynamia.actions.ActionsContainer;
import tools.dynamia.integration.Containers;
import tools.dynamia.navigation.PageAction;
import tools.dynamia.navigation.RendereablePage;
import tools.dynamia.viewers.View;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.ViewDescriptorNotFoundException;
import tools.dynamia.viewers.util.Viewers;

import java.util.Collection;
import java.util.List;

/**
 * Basic Config {@link tools.dynamia.navigation.Page} to store {@link tools.dynamia.domain.query.Parameter} values
 */
public class ConfigPage extends RendereablePage<View> {

    public ConfigPage() {
    }

    /**
     * Create a Config page using descriptor id of type config
     *
     * @param id
     * @param name
     * @param descriptorId
     * @param closeable
     */
    public ConfigPage(String id, String name, String descriptorId, boolean closeable) {
        super(id, name, descriptorId, closeable);
    }

    /**
     * Create a Config page using descriptor id of type config
     *
     * @param id
     * @param name
     * @param descriptorId
     */
    public ConfigPage(String id, String name, String descriptorId) {
        super(id, name, descriptorId);
    }

    @Override
    public View renderPage() {

        ViewDescriptor descriptor = Viewers.findViewDescriptor(getPath());
        if (!"config".equalsIgnoreCase(descriptor.getViewTypeName())) {
            throw new ViewDescriptorNotFoundException("View descriptor with id " + getPath() + " is not a [config] type");
        }


        var view = Viewers.getView(descriptor);
        if (view instanceof ActionsContainer cnt) {
            loadActions().forEach(cnt::addAction);
        }

        return view;
    }

    @Override
    public List<PageAction> getActions() {
        if (super.getActions().isEmpty()) {
            loadActions().forEach(a -> addAction(new PageAction(this, a)));
        }
        return super.getActions();
    }

    private Collection<AbstractConfigPageAction> loadActions() {

        return Containers.get().findObjects(AbstractConfigPageAction.class, a -> a.getApplicableConfig() == null || a.getApplicableConfig().equals(getDescriptorId()));
    }

    public String getDescriptorId() {
        return getPath();
    }
}
