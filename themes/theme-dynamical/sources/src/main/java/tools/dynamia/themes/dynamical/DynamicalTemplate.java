
/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tools.dynamia.themes.dynamical;

import org.zkoss.lang.Library;
import org.zkoss.zul.Messagebox;
import tools.dynamia.commons.MapBuilder;
import tools.dynamia.templates.ApplicationTemplate;
import tools.dynamia.templates.ApplicationTemplateSkin;
import tools.dynamia.templates.InstallApplicationTemplate;
import tools.dynamia.themes.dynamical.viewers.DynamicalCrudViewRenderer;
import tools.dynamia.viewers.ViewTypeFactory;
import tools.dynamia.zk.viewers.BootstrapConfigViewRender;
import tools.dynamia.zk.viewers.BootstrapCrudViewRenderer;
import tools.dynamia.zk.viewers.BootstrapFormViewRenderer;
import tools.dynamia.zk.viewers.BootstrapTableViewRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Mario A. Serrano Leones
 */
@InstallApplicationTemplate
public class DynamicalTemplate implements ApplicationTemplate {

    /**
     *
     */
    private static final long serialVersionUID = -8646000381813253072L;

    private static final ApplicationTemplateSkin DEFAULT_APPLICATION_SKIN = newSkin("Blue", "Default blue skin", "#367fa9");


    private final ViewTypeFactory viewTypeFactory;


    private List<ApplicationTemplateSkin> applicationTemplateSkins = new ArrayList<>();

    private Map<String, Object> properties;

    public DynamicalTemplate(ViewTypeFactory viewTypeFactory) {
        this.viewTypeFactory = viewTypeFactory;
        createSkins();

        properties = MapBuilder.put(AUTHOR, "Mario Serrano", DATE, "2017", COPYRIGHT, "Dynamia Soluciones IT 2017",
                VERSION, "5.4.1", ORIGINAL_AUTHOR, "Almsaeed Studio");

    }

    @Override
    public String getName() {
        return "Dynamical";
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public void init() {

        Library.setProperty("org.zkoss.theme.preferred", "iceblue_c");

        viewTypeFactory.setCustomViewRenderer("crud", DynamicalCrudViewRenderer.class);
        viewTypeFactory.setCustomViewRenderer("table", BootstrapTableViewRenderer.class);
        viewTypeFactory.setCustomViewRenderer("form", BootstrapFormViewRenderer.class);
        viewTypeFactory.setCustomViewRenderer("config", BootstrapConfigViewRender.class);
        Messagebox.setTemplate("~./templates/dynamical/views/messagebox.zul");
    }


    @Override
    public List<ApplicationTemplateSkin> getSkins() {
        return applicationTemplateSkins;
    }

    @Override
    public ApplicationTemplateSkin getDefaultSkin() {
        return DEFAULT_APPLICATION_SKIN;
    }

    private void createSkins() {
        applicationTemplateSkins.add(DEFAULT_APPLICATION_SKIN);
        applicationTemplateSkins.add(newSkin("Black", null, "#808080"));
        applicationTemplateSkins.add(newSkin("Green", null, "#00a65a"));
        applicationTemplateSkins.add(newSkin("Purple", null, "#605ca8"));
        applicationTemplateSkins.add(newSkin("Red", null, "#dd4b39"));
        applicationTemplateSkins.add(newSkin("Yellow", null, "#FFC200"));
        applicationTemplateSkins.add(newSkin("Orange", null, "#f39c12"));
        applicationTemplateSkins.add(newSkin("DarkOrange", null, "#ff5722"));
        applicationTemplateSkins.add(newSkin("Olive", null, "#8FB442"));
        applicationTemplateSkins.add(newSkin("Dynamia", null, "#00709c"));
        applicationTemplateSkins.add(newSkin("Dark", null, "#282a36"));
    }

    private static ApplicationTemplateSkin newSkin(String name, String description, String color) {

        String n = name.toLowerCase();
        ApplicationTemplateSkin applicationTemplateSkin = new ApplicationTemplateSkin("skin-" + n, name, "skin-" + n + ".min.css", description);
        applicationTemplateSkin.setBaseBackgroundColor(color);
        applicationTemplateSkin.setBaseColor(color);
        return applicationTemplateSkin;
    }
}
