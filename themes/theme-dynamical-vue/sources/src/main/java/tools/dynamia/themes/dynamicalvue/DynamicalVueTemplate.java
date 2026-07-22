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

package tools.dynamia.themes.dynamicalvue;

import tools.dynamia.commons.MapBuilder;
import tools.dynamia.templates.ApplicationTemplate;
import tools.dynamia.templates.ApplicationTemplateSkin;
import tools.dynamia.templates.InstallApplicationTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Application template for the Vue 3 + Tailwind CSS "Dynamical Vue" theme.
 * <p>
 * Unlike {@code DynamicalTemplate} (the ZK-based sibling theme), this template does not
 * register any {@code ViewTypeFactory} custom renderer: the entire UI — layout, navigation,
 * login and CRUD screens — is a client-rendered Vue 3 single-page app (see {@code src/main/frontend}),
 * built by {@code frontend-maven-plugin} and shipped as two static shells:
 * <ul>
 *   <li>{@code classpath:/views/index.html} — authenticated app shell (resolved by Spring's
 *   {@code ClassPathViewResolver} for view name {@code "index"})</li>
 *   <li>{@code classpath:/views/login.html} — standalone login page (view name {@code "login"})</li>
 * </ul>
 * Their hashed JS/CSS bundles live under {@code classpath:/web/templates/dynamicalvue/} and are
 * served by {@code ApplicationTemplateResourceHandler}.
 * <p>
 * Activate with:
 * <pre>{@code
 * dynamia.app.template=DynamicalVue
 * }</pre>
 *
 * @author Mario A. Serrano Leones
 */
@InstallApplicationTemplate
public class DynamicalVueTemplate implements ApplicationTemplate {

    private static final long serialVersionUID = 1L;

    private static final ApplicationTemplateSkin DEFAULT_SKIN = newSkin("Blue", "Default blue skin", "#2563eb");

    private final List<ApplicationTemplateSkin> skins = new ArrayList<>();
    private final Map<String, Object> properties;

    public DynamicalVueTemplate() {
        createSkins();
        properties = MapBuilder.put(AUTHOR, "Mario Serrano", DATE, "2026", COPYRIGHT, "Dynamia Soluciones IT 2026",
                VERSION, "1.0.0");
    }

    @Override
    public String getName() {
        return "DynamicalVue";
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public void init() {
        // No server-side view rendering to configure: the Vue SPA (src/main/frontend) owns
        // the entire UI. Static resources and shells are wired purely through classpath
        // conventions (see ApplicationTemplateResourceHandler / MvcConfiguration in
        // tools.dynamia.app), no framework hook is required here.
    }

    @Override
    public List<ApplicationTemplateSkin> getSkins() {
        return skins;
    }

    @Override
    public ApplicationTemplateSkin getDefaultSkin() {
        return DEFAULT_SKIN;
    }

    private void createSkins() {
        // Skin id maps 1:1 to the `data-skin` attribute the frontend sets on <html>
        // (see src/main/frontend/src/styles/skins.css) — add new skins in both places.
        skins.add(DEFAULT_SKIN);
        skins.add(newSkin("Dynamia", "Dynamia brand skin", "#00709c"));
        skins.add(newSkin("Dark", "Dark skin", "#1e293b"));
    }

    private static ApplicationTemplateSkin newSkin(String name, String description, String color) {
        String id = name.toLowerCase();
        ApplicationTemplateSkin skin = new ApplicationTemplateSkin(id, name, id + ".css", description);
        skin.setBaseBackgroundColor(color);
        skin.setBaseColor(color);
        return skin;
    }
}
