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
package tools.dynamia.templates;

import tools.dynamia.integration.Containers;

import java.util.HashMap;
import java.util.Map;

public class TemplateEngines {

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getParameters(Object target) {
        Map<String, Object> params = new HashMap<>();
        params.put("_TARGET_", target);

        Containers.get().findObjects(TemplateParametersProvider.class).forEach(tpp -> {
            try {
                params.putAll(tpp.getParameters(target));
            } catch (ClassCastException e) {
                // No compatible target. Nothing to do
            }
        });

        return params;
    }

    private TemplateEngines() {
    }

}
