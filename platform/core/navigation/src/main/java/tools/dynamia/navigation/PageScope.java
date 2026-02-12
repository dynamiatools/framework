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
package tools.dynamia.navigation;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.integration.Containers;

import java.io.Serializable;

/**
 * @author Mario A. Serrano Leones
 */
public class PageScope implements Scope {

    private final transient LoggingService logger = new SLF4JLoggingService(PageScope.class);

    @Override
    public Object get(String name, ObjectFactory objectFactory) {
        NavigationManager navManager = getNavManager();

        Object object = navManager.getCurrentPageAttributes().get(name);
        if (object == null) {
            object = objectFactory.getObject();
            if(object instanceof Serializable) {
                navManager.getCurrentPageAttributes().put(name, (Serializable) object);
            }
        }
        return object;
    }

    @Override
    public Object remove(String name) {
        NavigationManager navManager = getNavManager();
        return navManager.getCurrentPageAttributes().remove(name);
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
    }

    @Override
    public String getConversationId() {
        NavigationManager navManager = getNavManager();
        return navManager.getCurrentPage().getVirtualPath() + navManager;

    }

    @Override
    public Object resolveContextualObject(String string) {
        return getNavManager();
    }

    private NavigationManager getNavManager() {
        NavigationManager nav = Containers.get().findObject("navManager", NavigationManager.class);
        if (nav == null) {
            throw new RuntimeException("PageScope: Navigation Manager is null");
        }
        return nav;
    }

    private void log(String text) {
        logger.debug("PAGE-SCOPE: " + text);
    }
}
