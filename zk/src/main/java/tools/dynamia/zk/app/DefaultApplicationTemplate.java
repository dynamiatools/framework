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

package tools.dynamia.zk.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zul.Messagebox;
import tools.dynamia.app.template.ApplicationTemplate;
import tools.dynamia.app.template.InstallTemplate;
import tools.dynamia.app.template.Skin;
import tools.dynamia.app.template.TemplateContext;
import tools.dynamia.commons.MapBuilder;
import tools.dynamia.viewers.ViewTypeFactory;
import tools.dynamia.zk.app.bstemplate.BootstrapCrudViewRenderer;
import tools.dynamia.zk.app.bstemplate.BootstrapConfigViewRender;
import tools.dynamia.zk.app.bstemplate.BootstrapFormViewRenderer;
import tools.dynamia.zk.app.bstemplate.BootstrapTableViewRenderer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@InstallTemplate
public class DefaultApplicationTemplate implements ApplicationTemplate {

    /**
     *
     */
    private static final long serialVersionUID = 3095224909833774279L;
    private final Map<String, Object> properties;

    @Autowired
    private ViewTypeFactory viewTypeFactory;

    public DefaultApplicationTemplate() {
        properties = MapBuilder.put(AUTHOR, "Mario Serrano", DATE, "2018", COPYRIGHT, "Dynamia Soluciones IT 2017",
                VERSION, "4.0.0", ORIGINAL_AUTHOR, "Start Bootstrap @SBootstrap");
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
    public List<Skin> getSkins() {
        return Collections.emptyList();
    }

    @Override
    public Skin getDefaultSkin() {
        return new Skin("default", "default", "", "");
    }

    @Override
    public void init(TemplateContext context) {
        viewTypeFactory.setCustomViewRenderer("crud", BootstrapCrudViewRenderer.class);
        viewTypeFactory.setCustomViewRenderer("table", BootstrapTableViewRenderer.class);
        viewTypeFactory.setCustomViewRenderer("form", BootstrapFormViewRenderer.class);
        viewTypeFactory.setCustomViewRenderer("config", BootstrapConfigViewRender.class);
        Messagebox.setTemplate("~./templates/default/views/messagebox.zul");
    }

}
