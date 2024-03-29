
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

package tools.dynamia.zk.viewers.table;

import tools.dynamia.integration.sterotypes.Provider;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.ViewDescriptorReader;
import tools.dynamia.viewers.ViewDescriptorReaderCustomizer;
import tools.dynamia.viewers.impl.YamlViewDescriptorReader;

import java.util.Map;

/**
 * @author Mario A. Serrano Leones
 */
@Provider
public class TableVDReaderCustomizer implements ViewDescriptorReaderCustomizer<Map> {

    @Override
    public Class<? extends ViewDescriptorReader> getTargetReader() {
        return YamlViewDescriptorReader.class;
    }

    @Override
    public void customize(Map content, ViewDescriptor viewDescriptor) {
        if (viewDescriptor.getViewTypeName().equals("table")) {
            parseFrozen(content, viewDescriptor);
            parseActions(content, viewDescriptor);
        }
    }

    private void parseFrozen(Map content, ViewDescriptor viewDescriptor) {
        try {
            String frozen = content.get("frozenColumns").toString();
            if (frozen != null) {
                viewDescriptor.addParam("frozenColumns", frozen);
            }
        } catch (Exception ignored) {
        }
    }

    private void parseActions(Map content, ViewDescriptor viewDescriptor) {
        try {
            Map actions = (Map) content.get("actions");
            viewDescriptor.addParam("actions", actions);
        } catch (Exception ignored) {
        }
    }
}
