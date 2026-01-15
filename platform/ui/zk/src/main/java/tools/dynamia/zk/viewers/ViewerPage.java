
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

package tools.dynamia.zk.viewers;

import tools.dynamia.navigation.RendereablePage;
import tools.dynamia.zk.viewers.ui.Viewer;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class ViewerPage extends RendereablePage<Viewer> {

    /**
     *
     */
    private static final long serialVersionUID = -2841565724260612568L;
    private final String descriptorId;

    public ViewerPage(String id, String label, String descriptorId) {
        setId(id);
        setName(label);
        this.descriptorId = descriptorId;
    }

    @Override
    public Viewer renderPage() {

        Viewer viewer = new Viewer(getDescriptorId());
        viewer.setContentVflex(null);
        viewer.setVflex("1");
        return viewer;
    }

    public String getDescriptorId() {
        return descriptorId;
    }

}
