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
package tools.dynamia.zk;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.*;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.io.IOUtils;
import tools.dynamia.io.Resource;
import tools.dynamia.zk.ui.DateRangebox;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

/**
 * @author Mario A. Serrano Leones
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ComponentAliasIndex extends HashMap<String, Class<? extends Component>> {

    private final static ComponentAliasIndex INDEX = new ComponentAliasIndex();

    static {
        getInstance().add(Label.class);
        getInstance().add(Textbox.class);
        getInstance().add(Combobox.class);
        getInstance().add(Selectbox.class);
        getInstance().add(Intbox.class);
        getInstance().add(Longbox.class);
        getInstance().add(Decimalbox.class);
        getInstance().add(Datebox.class);
        getInstance().add(Button.class);
        getInstance().add(A.class);
        getInstance().add("link", A.class);
        getInstance().add(Listbox.class);
        getInstance().add(Toolbarbutton.class);
        getInstance().add(Menuitem.class);
        getInstance().add(Menu.class);
        getInstance().add(Spinner.class);
        getInstance().add(Bandbox.class);
        getInstance().add(Checkbox.class);
        getInstance().add(Radio.class);
        getInstance().add(Radiogroup.class);
        getInstance().add(Timebox.class);
        getInstance().add(Audio.class);
        getInstance().add(Applet.class);
        getInstance().add(Area.class);
        getInstance().add(Flash.class);
        getInstance().add(Chart.class);
        getInstance().add(Captcha.class);
        getInstance().add(Doublebox.class);
        getInstance().add(Doublespinner.class);
        getInstance().add(Image.class);
        getInstance().add(Fileupload.class);
        getInstance().add(Slider.class);
        getInstance().add(Iframe.class);
        getInstance().add(Tree.class);
        getInstance().add(Calendar.class);
        getInstance().add(Groupbox.class);
        getInstance().add(Progressmeter.class);
        getInstance().add(Chart.class);
        getInstance().add(Rating.class);
        getInstance().add(DateRangebox.class);
        getInstance().add(Combobutton.class);

        LoggingService logger = new SLF4JLoggingService(ComponentAliasIndex.class);
        try {

            Resource[] resources = IOUtils.getResources("classpath*:/META-INF/dynamia/aliases.properties");
            for (Resource resource : resources) {
                Properties aliases = new Properties();
                aliases.load(resource.getInputStream());
                for (String alias : aliases.stringPropertyNames()) {
                    try {
                        Class clazz = Class.forName(aliases.getProperty(alias));
                        getInstance().add(alias, clazz);
                    } catch (Exception ex) {
                        logger.error(
                                "Error loading alias from " + resource.getFile().getAbsolutePath() + " ALIAS: " + alias + ". "
                                        + ex.getMessage(), ex);
                    }

                }
            }
        } catch (IOException ex) {
            logger.error(ex);
        }

    }

    public static ComponentAliasIndex getInstance() {
        return INDEX;
    }

    public void add(Class<? extends Component> componentClass) {
        add(componentClass.getSimpleName(), componentClass);
    }

    public void add(String alias, Class<? extends Component> componentClass) {
        put(alias, componentClass);
    }

    @Override
    public Class<? extends Component> put(String key, Class<? extends Component> value) {
        return super.put(key.toLowerCase(), value);
    }

    public Class<? extends Component> get(String alias) {
        return super.get(alias.toLowerCase());
    }

    public String getAlias(Class componentClass) {
        return entrySet().stream().filter(e -> e.getValue().equals(componentClass)).map(Entry::getKey).findFirst().orElse(null);
    }
}
