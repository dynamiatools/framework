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
package tools.dynamia.zk.crud;

import tools.dynamia.commons.BeanUtils;
import tools.dynamia.viewers.View;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.ViewRenderer;
import tools.dynamia.zk.crud.ui.EntityFiltersPanel;

/**
 * @author Mario A. Serrano Leones
 */
public class EntityFiltersPanelViewRenderer implements ViewRenderer {


    @Override
    public View render(ViewDescriptor descriptor, Object value) {
        EntityFiltersPanel filterPanel = new EntityFiltersPanel(descriptor.getBeanClass());

        try {
            BeanUtils.setupBean(filterPanel, descriptor.getParams());
        } catch (Exception ignored) {
        }
        filterPanel.setValue(value);
        customize(filterPanel);

        return filterPanel;

    }

    protected void customize(EntityFiltersPanel filterPanel) {

    }

}
