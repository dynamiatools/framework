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

package tools.dynamia.modules.dashboard;

import tools.dynamia.zk.viewers.ui.Viewer;

/**
 * Extend this class if you need to render widgets that use another {@link tools.dynamia.viewers.View}
 * @author Mario Serrano Leones
 */
public abstract class ViewerDashboardWidget extends AbstractDashboardWidget<Viewer> {

    private Object viewValue;

    public abstract String getViewDescriptorId();

    public abstract String getViewType();

    public abstract Object initViewValue(DashboardContext context);

    @Override
    public void init(DashboardContext context) {
        viewValue = initViewValue(context);
    }

    @Override
    public Viewer getView() {
        Viewer viewer = new Viewer();
        viewer.setDescriptorId(getViewDescriptorId());
        if (viewer.getDescriptorId() == null) {
            viewer.setViewType(getViewType());
        }
        viewer.setVflex("1");
        viewer.setContentVflex("1");
        viewer.setValue(viewValue);

        return viewer;
    }

}
