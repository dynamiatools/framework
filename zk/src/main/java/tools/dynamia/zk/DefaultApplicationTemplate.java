package tools.dynamia.zk;

import tools.dynamia.commons.MapBuilder;
import tools.dynamia.integration.sterotypes.Provider;
import tools.dynamia.templates.ApplicationTemplate;
import tools.dynamia.templates.ApplicationTemplateSkin;
import tools.dynamia.viewers.ViewTypeFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Provider
public class DefaultApplicationTemplate implements ApplicationTemplate {

    private final ViewTypeFactory viewTypeFactory;
    private final Map<String, Object> properties;
    private List<ApplicationTemplateSkin> skins;


    public DefaultApplicationTemplate(ViewTypeFactory viewTypeFactory) {
        this.viewTypeFactory = viewTypeFactory;

        properties = MapBuilder.put(AUTHOR, "Mario Serrano",
                DATE, "2025",
                COPYRIGHT, "Dynamia Soluciones IT 2025",
                VERSION, "5.3.2");

        buildSkins();

    }

    private void buildSkins() {
        this.skins = new ArrayList<>();
        skins.add(new ApplicationTemplateSkin("default", "Default", "", ""));
    }

    @Override
    public String getName() {
        return "Default";
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public List<ApplicationTemplateSkin> getSkins() {
        return skins;
    }

    @Override
    public ApplicationTemplateSkin getDefaultSkin() {
        return skins.getFirst();
    }

    @Override
    public void init() {

    }
}
