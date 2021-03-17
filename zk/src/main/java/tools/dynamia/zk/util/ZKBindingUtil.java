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
package tools.dynamia.zk.util;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.DefaultBinder;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ui.CoolLabel;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

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

    @SuppressWarnings("rawtypes")
    public static void bindComponent(Binder binder, Component component, Map bindindingMap) {
        if (bindindingMap == null) {
            logger.warn("Binding map is null, nothing to do");
            return;
        }

        String prefix = "";
        if (bindindingMap.containsKey(KEY_EXP_PREFIX)) {
            prefix = bindindingMap.get(KEY_EXP_PREFIX) + ".";
            bindindingMap.remove(KEY_EXP_PREFIX);
        }


        for (Object object : bindindingMap.entrySet()) {
            Entry entry = (Entry) object;
            String bindingAttribute = entry.getKey().toString();
            String expression = null;
            String converter = null;

            if (entry.getValue() instanceof Map) {
                Map bindingDetail = (Map) entry.getValue();
                expression = (String) bindingDetail.get(VALUE);
                converter = (String) bindingDetail.get(CONVERTER);

            } else if (entry.getValue() instanceof String) {
                expression = entry.getValue().toString();
            }

            if (expression != null && !expression.isEmpty()) {

                if (expression.contains(prefix)) {
                    prefix = "";
                }

                bindComponent(binder, component, bindingAttribute, prefix + expression, converter);
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

        binder.addPropertyLoadBindings(component, bindingAttribute, expression, null, null, null, converterExpression,
                null);
        if (!(component instanceof Label) && !(component instanceof Image) && !(component instanceof CoolLabel)) {

            binder.addPropertySaveBindings(component, bindingAttribute, expression, null, saveAfterCmds, null,
                    converterExpression, null, null, null);
        }
    }

    /**
     * Notify changes on all property of view model
     *
     * @param viewmodel
     */
    public static void postNotifyChange(Object viewmodel) {
        postNotifyChange(viewmodel, "*");
    }

    /**
     * Notify changes on property of view model
     *
     * @param viewmodel
     * @param property
     */
    public static void postNotifyChange(Object viewmodel, String property) {
        if (ZKUtil.isInEventListener()) {
            BindUtils.postNotifyChange(null, null, viewmodel, property);
        }
    }

    /**
     * Notify changed on properties of view model
     *
     * @param viewmodel
     * @param properties
     */
    public static void postNotifyChange(Object viewmodel, String... properties) {
        if (properties != null) {
            Stream.of(properties).forEach(p -> postNotifyChange(viewmodel, p));
        }
    }

    /**
     * Notifiy changes on property of view model
     *
     * @param queue
     * @param scope
     * @param viewmodel
     * @param property
     */
    public static void postNotifyChange(String queue, String scope, Object viewmodel, String property) {
        BindUtils.postNotifyChange(queue, scope, viewmodel, property);

    }

    /**
     * Post a global command without arguments
     *
     * @param name
     */
    public static void postGlobalCommand(String name) {
        postGlobalCommand(name, null);
    }

    /**
     * Post a global command with arguments
     *
     * @param name
     * @param args
     */
    public static void postGlobalCommand(String name, Map<String, Object> args) {
        BindUtils.postGlobalCommand(null, null, name, args);
    }

    private ZKBindingUtil() {
    }
}
