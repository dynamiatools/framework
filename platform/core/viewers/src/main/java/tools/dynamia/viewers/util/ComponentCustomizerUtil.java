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
package tools.dynamia.viewers.util;

import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.integration.Containers;
import tools.dynamia.viewers.ComponentCustomizationException;
import tools.dynamia.viewers.ComponentCustomizer;
import tools.dynamia.viewers.Field;


/**
 * The Class ComponentCustomizerUtil.
 */
public abstract class ComponentCustomizerUtil {

    /**
     * Customize component.
     *
     * @param field the field
     * @param component the component
     * @param componentCustomizer the component customizers
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void customizeComponent(Field field, Object component, String componentCustomizer) {
        if (component == null) {
            return;
        }

        if (componentCustomizer != null) {
            try {
                ComponentCustomizer customizer = (ComponentCustomizer) ObjectOperations.newInstance(Class.forName(componentCustomizer));
                customizer.cutomize(field, component);
            } catch (ClassCastException e) {
                // ignore, this is beacuse generics
            } catch (Exception e) {
                throw new ComponentCustomizationException("Error customizing component " + component + " using customizers class:" + componentCustomizer, e);
            }
        } else {
            for (ComponentCustomizer cc : Containers.get().findObjects(ComponentCustomizer.class)) {
                try {
                    cc.cutomize(field, component);
                } catch (Exception ignored) {
                }
            }
        }
    }

    private ComponentCustomizerUtil() {
    }
}
