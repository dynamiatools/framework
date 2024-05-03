package tools.dynamia.templates;

public class DefaultApplicationTemplateHolder implements ApplicationTemplateHolder {
    private final ApplicationTemplate template;
    private final ApplicationTemplateSkin skin;

    public DefaultApplicationTemplateHolder(ApplicationTemplate template, ApplicationTemplateSkin skin) {
        this.template = template;
        this.skin = skin;
    }

    @Override
    public String getLogoURL() {
        return null;
    }

    @Override
    public ApplicationTemplate getTemplate() {
        return template;
    }

    @Override
    public ApplicationTemplateSkin getSkin() {
        return skin;
    }


}
