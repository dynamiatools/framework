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
package tools.dynamia.zk.spring;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WebApp;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ZK WebApp scope; accessible only in ZK event handling request.
 *
 * @author henrichen
 * @since 1.2
 */
public class ApplicationScope implements Scope {

    private static final String APP_SCOPE = "ZK_SPRING_APP_SCOPE";

    @Override
    public Object get(String name, ObjectFactory objectFactory) {
        final Execution exec = Executions.getCurrent();
        if (exec != null) {
            final WebApp app = exec.getDesktop().getWebApp();
            Map appScope = (Map) app.getAttribute(APP_SCOPE);
            if (appScope == null) {
                app.setAttribute(APP_SCOPE, appScope = new ConcurrentHashMap());
            }
            Object scopedObject = appScope.get(name);
            if (scopedObject == null) {
                scopedObject = objectFactory.getObject();
                appScope.put(name, scopedObject);
            }
            return scopedObject;
        }
        throw new IllegalStateException("Unable to get application scope bean: " + name + ". Do you access it in ZK event listener?");
    }

    @Override
    public String getConversationId() {
        final Execution exec = Executions.getCurrent();
        if (exec != null) {
            final WebApp app = exec.getDesktop().getWebApp();
            if (app != null) {
                return app.getAppName();
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
            final WebApp app = exec.getDesktop().getWebApp();
            final Map appScope = (Map) app.getAttribute(APP_SCOPE);
            return (appScope != null) ? appScope.remove(name) : null;
        }
        throw new IllegalStateException("Unable to get application scope bean: " + name + ". Do you access it in ZK event listener?");
    }

    @Override
    public Object resolveContextualObject(String arg0) {
        
        return null;
    }
}
