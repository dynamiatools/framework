package tools.dynamia.templates;

import tools.dynamia.integration.Containers;

public interface ApplicationTemplateHolder {

    static ApplicationTemplateHolder get() {
        var delegate = Containers.get().findObject(ApplicationTemplateHolder.class);
        if (delegate == null) {
            delegate = new DefaultApplicationTemplateHolder(null, null);
        }
        return delegate;
    }

    ApplicationTemplate getTemplate();

    ApplicationTemplateSkin getSkin();
}
