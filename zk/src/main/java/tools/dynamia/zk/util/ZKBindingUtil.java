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
package tools.dynamia.zk.util;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.DefaultBinder;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.A;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import tools.dynamia.commons.PropertyChangeEvent;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ui.LoadableOnly;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Utily class for using zk bindings programatic easily
 */
public class ZKBindingUtil {

    private static final String CONVERTER = "converter";
    private static final String VALUE = "value";
    private static final LoggingService logger = new SLF4JLoggingService(ZKBindingUtil.class);
    public static final String KEY_EXP_PREFIX = "_ExpPrefix";

    public static Binder createBinder() {
        return new DefaultBinder();
    }

    public static void initBinder(Binder binder, Component rootComponent, Object viewModel) {
        binder.init(rootComponent, viewModel, null);
    }

    public static void initBinder(Binder binder, Component rootComponent, Object viewModel, Map<String, Object> initArgs) {
        binder.init(rootComponent, viewModel, initArgs);
    }

    public static void bindBean(Component rootComponent, String name, Object bean) {
        rootComponent.setAttribute(name, bean);
    }

    public static void bindComponent(Binder binder, Component component, String expression,
                                     String converterExpression) {
        bindComponent(binder, component, null, expression, converterExpression);
    }

    public static void bindComponent(Binder binder, Component component, Map bindindingMap) {
        bindComponent(binder, component, bindindingMap, "");
    }

    @SuppressWarnings("rawtypes")
    public static void bindComponent(Binder binder, Component component, Map bindindingMap, String prefix) {
        if (bindindingMap == null) {
            logger.warn("Binding map is null, nothing to do");
            return;
        }

        if (prefix == null) {
            prefix = "";
        } else if (!prefix.isBlank() && !prefix.endsWith(".")) {
            prefix = prefix + ".";
        }


        for (Object object : bindindingMap.entrySet()) {
            Entry entry = (Entry) object;
            String bindingAttribute = entry.getKey().toString();
            String expression = null;
            String converter = null;

            if (entry.getValue() instanceof Map bindingDetail) {
                expression = (String) bindingDetail.get(VALUE);
                converter = (String) bindingDetail.get(CONVERTER);
            } else if (entry.getValue() instanceof String) {
                expression = entry.getValue().toString();
            }

            if (expression != null && !expression.isBlank()) {

                if (!prefix.isBlank() && expression.startsWith(prefix)) {
                    prefix = "";
                }

                final String finalExp = prefix + expression;
                bindComponent(binder, component, bindingAttribute, finalExp, converter);
            }
        }
    }

    public static void bindComponent(Binder binder, Component component, String bindingAttribute, String expression,
                                     String converterExpression) {
        bindComponent(binder, component, bindingAttribute, expression, converterExpression, null);
    }

    public static void bindComponent(Binder binder, Component component, String bindingAttribute, String expression,
                                     String converterExpression, String[] saveAfterCmds) {

        if (binder == null) {
            logger.warn("Trying to bind component but Binder is null");
            return;
        }

        if (bindingAttribute == null) {
            bindingAttribute = BindingComponentIndex.getInstance().getAttribute(component.getClass());
        }

        if (converterExpression != null) {
            try {
                Class.forName(converterExpression);
                converterExpression = "'" + converterExpression + "'";

            } catch (Exception e) {
                logger.warn("Cannot load converter class " + e.getMessage());
                converterExpression = null;
                // ignore
            }
        }

        if (expression != null && expression.startsWith("bean.bean.")) {
            //bug, temp fix
            expression = expression.replace("bean.bean.", "bean.");

        }

        binder.addPropertyLoadBindings(component, bindingAttribute, expression, null, null, null, converterExpression,
                null);

        if (!(component instanceof Label) && !(component instanceof Image) && !(component instanceof LoadableOnly) && !(component instanceof A)) {

            binder.addPropertySaveBindings(component, bindingAttribute, expression, null, saveAfterCmds, null,
                    converterExpression, null, null, null);
        }
    }

    /**
     * Notify changes on all property of view model
     *
     */
    public static void postNotifyChange(Object bean) {
        postNotifyChange(bean, "*");
    }

    /**
     * Notify changes of all property of all elements in the collections
     *
     */
    public static void postNotifyChange(Collection<?> objects) {
        objects.forEach(ZKBindingUtil::postNotifyChange);
    }

    /**
     * Notify changes on property of view model
     *
     */
    public static void postNotifyChange(Object bean, String property) {
        if (ZKUtil.isInEventListener()) {
            BindUtils.postNotifyChange(bean, property);
        }
    }

    /**
     * Notify changed on properties of view model
     *
     */
    public static void postNotifyChange(Object bean, String... properties) {
        if (ZKUtil.isInEventListener()) {
            BindUtils.postNotifyChange(bean, properties);
        }
    }

    /**
     * Notify change of {@link PropertyChangeEvent} source and property
     *
     */
    public static void postNotifyChange(PropertyChangeEvent evt) {
        if (evt != null && evt.source() != null && evt.propertyName() != null) {
            postNotifyChange(evt.source(), evt.propertyName());
        }
    }

    /**
     * Notifiy changes on property of view model
     *
     */
    public static void postNotifyChange(String queue, String scope, Object bean, String property) {
        if (ZKUtil.isInEventListener()) {
            BindUtils.postNotifyChange(queue, scope, bean, property);
        }

    }

    /**
     * Post a global command without arguments
     *
     */
    public static void postGlobalCommand(String name) {
        postGlobalCommand(name, null);
    }

    /**
     * Post a global command with arguments
     *
     */
    public static void postGlobalCommand(String name, Map<String, Object> args) {
        BindUtils.postGlobalCommand(null, null, name, args);
    }

    private ZKBindingUtil() {
    }
}
