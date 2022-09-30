/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zkoss.lang.Library;
import org.zkoss.zk.ui.Executions;
import tools.dynamia.app.ApplicationInfo;
import tools.dynamia.ui.MessageDisplayer;
import tools.dynamia.zk.ui.MessageNotification;
import tools.dynamia.zk.util.ZKUtil;

import javax.annotation.PostConstruct;

import static tools.dynamia.commons.BeanUtils.newInstance;

/**
 * @author Mario Serrano Leones
 */
@Configuration
public class ZKAppConfiguration {

    @Autowired
    private ApplicationInfo appInfo;

    public static void updateSkin(String skin) {
        if (ZKUtil.isInEventListener()) {
            Executions.getCurrent().sendRedirect("/?skin=" + skin, true);
        }
    }

    @PostConstruct
    public void init() {
        if (appInfo != null) {
            Library.setProperty("org.zkoss.zk.ui.WebApp.name", appInfo.getName());
        }
    }

    @Bean
    public MessageDisplayer messageDialog() {
        try {
            String className = appInfo.getProperty("MessageDisplayer");

            MessageDisplayer displayer = null;
            if (className != null) {
                displayer = newInstance(className);
            }

            if (displayer == null) {
                displayer = new MessageNotification();
            }
            return displayer;
        } catch (Exception e) {
            return new MessageNotification();
        }

    }

}
