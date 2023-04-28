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
package tools.dynamia.zk.spring;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ZK Desktop scope; accessible only in ZK event handling request.
 *
 * @author henrichen
 * @since 1.2
 */
public class DesktopScope implements Scope {

    private static final String DESKTOP_SCOPE = "ZK_SPRING_DESKTOP_SCOPE";

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        final Execution exec = Executions.getCurrent();
        if (exec != null) {
            final Desktop desktop = exec.getDesktop();
            Map desktopScope = (Map) desktop.getAttribute(DESKTOP_SCOPE);
            if (desktopScope == null) {
                desktop.setAttribute(DESKTOP_SCOPE, desktopScope = new ConcurrentHashMap());
            }
            Object scopedObject = desktopScope.get(name);
            if (scopedObject == null) {
                scopedObject = objectFactory.getObject();
                desktopScope.put(name, scopedObject);
            }
            return scopedObject;
        }
        throw new IllegalStateException("Unable to get desktop scope bean: " + name + ". Do you access it in ZK event listener?");
    }

    @Override
    public String getConversationId() {
        final Execution exec = Executions.getCurrent();
        if (exec != null) {
            final Desktop desktop = exec.getDesktop();
            if (desktop != null) {
                return desktop.getId();
            }
        }
        return null;
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        // do nothing
    }

    @Override
    public Object remove(String name) {
        final Execution exec = Executions.getCurrent();
        if (exec != null) {
            final Desktop desktop = exec.getDesktop();
            final Map desktopScope = (Map) desktop.getAttribute(DESKTOP_SCOPE);
            return (desktopScope != null) ? desktopScope.remove(name) : null;
        }
        throw new IllegalStateException("Unable to get desktop scope bean: " + name + ". Do you access it in ZK event listener?");
    }

    @Override
    public Object resolveContextualObject(String key) {
        
        return null;
    }

}
