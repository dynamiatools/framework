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
package tools.dynamia.zk.crud;

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
public class CrudVDReaderCustomizer implements ViewDescriptorReaderCustomizer<Map> {

    public static final String ACTIONS = "actions";
    public static final String CONTROLLER = "controller";
    public static final String DATA_SET_VIEW_TYPE = "dataSetViewType";
    public static final String PARENT_NAME = "parentName";
    public static final String CONTROLLER_CLASS = "controllerClass";
    public static final String DATA_SET_VIEW = "dataSetView";
    public static final String FORM_VIEW = "formView";
    public static final String FORM_VIEW_DESCRIPTOR_ID="formViewDescriptorId";

    @Override
    public Class<? extends ViewDescriptorReader> getTargetReader() {
        return YamlViewDescriptorReader.class;
    }

    @Override
    public void customize(Map content, ViewDescriptor viewDescriptor) {
        if (viewDescriptor.getViewTypeName().equals("crud")) {
            customizeCrud(content, viewDescriptor);
        }
    }

    protected void customizeCrud(Map content, ViewDescriptor viewDescriptor) {
        parseActions(content, viewDescriptor);
        parseParams(content, viewDescriptor);
    }

    private void parseActions(Map content, ViewDescriptor viewDescriptor) {
        try {
            Map actions = (Map) content.get(ACTIONS);
            viewDescriptor.addParam(ACTIONS, actions);
        } catch (Exception e) {
        }
    }

    private void parseParams(Map content, ViewDescriptor viewDescriptor) {
        Object controller = content.get(CONTROLLER);
        if (controller != null && controller instanceof String) {
            viewDescriptor.addParam(CONTROLLER_CLASS, controller);
        }

        Object dataSetViewType = content.get(DATA_SET_VIEW);
        if (dataSetViewType != null && dataSetViewType instanceof String) {
            viewDescriptor.addParam(DATA_SET_VIEW_TYPE, dataSetViewType);
        }

        Object parentName = content.get(PARENT_NAME);
        if (parentName != null && parentName instanceof String) {
            viewDescriptor.addParam(PARENT_NAME, parentName);
        }

        Object formView = content.get(FORM_VIEW);
        if (formView != null && formView instanceof String) {
            viewDescriptor.addParam(FORM_VIEW_DESCRIPTOR_ID, formView);
        }
    }
}
