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

package tools.dynamia.modules.saas.ui;

import org.springframework.stereotype.Component;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.FieldCustomizer;
import tools.dynamia.viewers.FieldGroup;

/**
 * @author Mario Serrano Leones
 */
@Component
public class AccountFieldCustomizer implements FieldCustomizer {

    @Override
    public void customize(String viewTypeName, Field field) {
        if (field.getName().equals("accountId")) {
            if (field.getParams().get("forceVisible") == Boolean.TRUE) {
                return;
            }
            field.getViewDescriptor().removeField(field.getName());
            FieldGroup group = field.getGroup();
            if (group != null) {
                group.removeField(field);
            }
        }
    }

}
