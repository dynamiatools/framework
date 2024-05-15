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

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Window;
import tools.dynamia.domain.services.AbstractService;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.converters.*;
import tools.dynamia.zk.util.ZKBindingUtil;
import tools.dynamia.zk.util.ZKUtil;

import java.util.Map;

/**
 * Helper class to build ViewModels
 */

public abstract class AbstractViewModel<T> extends AbstractService {

    protected T model;
    protected Window parentWindow;
    protected Component view;
    private EventListener<Event> closeListener;

    @AfterCompose
    public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
        this.view = view;
        afterViewCompose();
    }


    @SuppressWarnings("unchecked")
    @Init
    public void initDefaults() {
        //noinspection unchecked
        model = (T) ZKUtil.getExecutionEntity();
        parentWindow = ZKUtil.getExecutionParentWindow();
        afterInitDefaults();
    }

    protected void afterInitDefaults() {
        //do nothing here
    }


    protected void afterViewCompose() {
        //do nothing here
    }

    /**
     * Enable if a question confirmation dialog when user close parent window. If parent window is null nothing happens
     */
    protected void enableOnCloseWindowConfirmation(String message) {
        if (parentWindow != null) {
            closeListener = new EventListener<>() {
                @Override
                public void onEvent(Event event) {
                    event.stopPropagation();
                    UIMessages.showQuestion(message, () -> {
                        parentWindow.removeEventListener(Events.ON_CLOSE, this);
                        parentWindow.detach();
                        Events.postEvent(event);
                    });
                }
            };

            parentWindow.addEventListener(Events.ON_CLOSE, closeListener);
        }
    }

    public T getModel() {
        return model;
    }

    public void setModel(T model) {
        this.model = model;
    }

    protected void notifyChanges() {
        ZKBindingUtil.postNotifyChange(this);
    }

    protected void notifyChanges(String property) {
        ZKBindingUtil.postNotifyChange(this, property);
    }

    protected void notifyChanges(String... properties) {
        ZKBindingUtil.postNotifyChange(this, properties);
    }

    protected void showDialog(String uri, String title, Object data) {
        ZKUtil.showDialog(uri, title, data);
    }

    protected void postGlobalCommand(String command) {
        ZKBindingUtil.postGlobalCommand(command);
    }

    protected void postGlobalCommand(String command, Map<String, Object> params) {
        ZKBindingUtil.postGlobalCommand(command, params);
    }


    /**
     * Close current parent window without confirmation. If parent window is null nothing happens
     */
    protected void closeWindow() {
        if (parentWindow != null) {
            parentWindow.detach();
            if (closeListener != null) {
                parentWindow.removeEventListener(Events.ON_CLOSE, closeListener);
            }
            Events.postEvent(new Event(Events.ON_CLOSE, parentWindow));
        }
    }

    /**
     * Return an execution argument
     */
    public Object getArg(String name) {
        return ZKUtil.getExecutionArg(name);
    }

    /**
     * Return a request parameter
     */
    public String getRequestParam(String name) {
        return Executions.getCurrent().getParameter(name);
    }


    public Currency getCurrencyConverter() {
        return new Currency();
    }

    public CurrencySimple getCurrencySimpleConverter() {
        return new CurrencySimple();
    }

    public Date getDateConverter() {
        return new Date();
    }

    public DateTime getDateTimeConverter() {
        return new DateTime();
    }

    public LocalDate getLocalDateConverter() {
        return new LocalDate();
    }

}
