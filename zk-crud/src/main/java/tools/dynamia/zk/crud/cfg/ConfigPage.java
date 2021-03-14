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
package tools.dynamia.zk.crud.cfg;

import org.zkoss.zul.Toolbar;
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.integration.Containers;
import tools.dynamia.zk.actions.ActionToolbar;
import tools.dynamia.zk.viewers.ViewerPage;
import tools.dynamia.zk.viewers.ui.Viewer;

/**
 * @author Mario A. Serrano Leones
 */
public class ConfigPage extends ViewerPage {

    /**
     *
     */
    private static final long serialVersionUID = 2477610135447918635L;

    public ConfigPage(String id, String label, String descriptorId) {
        super(id, label, descriptorId);
    }

    @Override
    public Viewer renderPage() {
        Viewer viewer = super.renderPage();
        viewer.setToolbar(createToolbar(viewer));
        return viewer;
    }

    private Toolbar createToolbar(final Viewer viewer) {
        ActionToolbar toolbar = new ActionToolbar((source, params) -> new ActionEvent(viewer.getValue(), viewer));

        Containers.get().findObjects(AbstractConfigPageAction.class).forEach(a -> {
            if (a.getApplicableConfig() == null || a.getApplicableConfig().equals(getDescriptorId())) {
                toolbar.addAction(a);
            }
        });

        return toolbar;
    }

}
