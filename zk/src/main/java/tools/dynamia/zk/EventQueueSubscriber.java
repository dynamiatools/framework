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

import org.springframework.jmx.access.InvocationFailureException;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.GlobalCommandEvent;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import tools.dynamia.commons.BeanUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class EventQueueSubscriber {

    private final Object target;

    public EventQueueSubscriber(Object target) {
        super();
        this.target = target;
    }

    public void loadAnnotations() {
        Method[] methods = BeanUtils.getMethodsWithAnnotation(target.getClass(), Subscribe.class);
        for (Method method : methods) {
            loadAnnotation(method);
        }
    }

    private void loadAnnotation(Method method) {
        Subscribe subscribe = method.getAnnotation(Subscribe.class);

        EventQueue<Event> queue = EventQueues.lookup(subscribe.value(), subscribe.scope(), subscribe.autocreate());
        queue.subscribe(evt -> {
            boolean completed = false;
            if (evt instanceof GlobalCommandEvent) {
                GlobalCommandEvent globalEvt = (GlobalCommandEvent) evt;
                if (subscribe.command().length > 0) {
                    for (String command : subscribe.command()) {
                        if (globalEvt.getCommand().equals(command)) {
                            completed = invoke(method, evt);
                            break;
                        }
                    }
                } else if (globalEvt.getCommand().equals(method.getName())) {
                    completed = invoke(method, evt);
                }
            } else if (subscribe.eventName().isEmpty() || subscribe.eventName().equals(evt.getName())) {
                completed = invoke(method, evt);
            }

            if (completed) {
                notifyChange(subscribe, method);
            }
        }, subscribe.async());

    }

    private void notifyChange(Subscribe subscribe, Method method) {
        NotifyChange notifyChange = method.getAnnotation(NotifyChange.class);
        if (notifyChange != null) {
            for (String property : notifyChange.value()) {
                BindUtils.postNotifyChange(null, null, target, property);
            }
        }

    }

    private boolean invoke(Method method, Event evt) {
        try {

            if (method.getParameterCount() == 0) {
                method.invoke(target);
                return true;
            }

            if (evt.getData() != null && evt.getData().getClass().isArray()) {
                Object[] evtData = (Object[]) evt.getData();
                Parameter[] parameters = method.getParameters();
                Object[] args = new Object[parameters.length];

                int start = 0;
                if (parameters[0].getType() == Event.class) {
                    args[0] = evt;
                    start = 1;
                }

                if (evtData.length + start != args.length) {
                    throw new ZKException("Invalid @Subscribe method, arguments not match");
                }

                System.arraycopy(evtData, 0, args, start, evtData.length);
                method.invoke(target, args);
                return true;

            }

            if (method.getParameterCount() == 1) {
                Class paramType = method.getParameterTypes()[0];

                if (paramType == Event.class) {
                    method.invoke(target, evt);
                    return true;
                } else if (paramType == Object.class) {
                    method.invoke(target, evt.getData());
                    return true;
                }
            }

            throw new InvocationFailureException("Subscribe method dont have correct parameters");

        } catch (Exception e) {
            throw new ZKException("Error invokin annotated @Subcribe method", e);
        }

    }
}
