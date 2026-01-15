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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ZK Execution scope; accessible only in one ZK execution(e.g. ZK event
 * handling).
 *
 * @author henrichen
 * @since 1.2
 */
public class ExecutionScope implements Scope {

    private static final String EXECUTION_SCOPE = "ZK_SPRING_EXECUTION_SCOPE";

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        final Execution exec = Executions.getCurrent();
        if (exec != null) {

            Map executionScope = (Map) exec.getAttribute(EXECUTION_SCOPE);
            if (executionScope == null) {
                exec.setAttribute(EXECUTION_SCOPE, executionScope = new ConcurrentHashMap());
            }
            Object scopedObject = executionScope.get(name);
            if (scopedObject == null) {
                scopedObject = objectFactory.getObject();
                //noinspection unchecked
                executionScope.put(name, scopedObject);
            }
            return scopedObject;
        }
        throw new IllegalStateException("Unable to get execution scope bean: " + name + ". Do you access it in ZK event listener?");
    }

    @Override
    public String getConversationId() {
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
            final Map desktopScope = (Map) desktop.getAttribute(EXECUTION_SCOPE);
            return (desktopScope != null) ? desktopScope.remove(name) : null;
        }
        throw new IllegalStateException("Unable to get desktop scope bean: " + name + ". Do you access it in ZK event listener?");
    }

    @Override
    public Object resolveContextualObject(String key) {

        return null;
    }

}
