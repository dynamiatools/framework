package tools.dynamia.templates;

import tools.dynamia.integration.Containers;

/**
 * Current application template holder.
 */
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

    String getLogoURL();


    default void setSkin(String skinId) {
    }

    default void setLogoURL(String logo) {
    }

    ;

    default String getIconURL() {
        return null;
    }

    default void setIconURL(String iconURL) {
    }
}
