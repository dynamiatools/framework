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

package tools.dynamia.zk.viewers.mv;

import tools.dynamia.viewers.*;
import tools.dynamia.zk.viewers.ui.Viewer;

public class MultiViewRenderer implements ViewRenderer {

    @Override
    public View render(ViewDescriptor descriptor, Object value) {
        MultiView view = newMultiView();
        view.setValue(value);
        view.setViewDescriptor(descriptor);
        boolean loadNow = true;
        for (final Field field : descriptor.getFields()) {
            if (isValidSubviewField(field)) {
                view.addView(field.getLabel(), parentView -> MultiViewRenderer.this.loadSubview(field, parentView), loadNow);
                loadNow = false;
            }
        }
        return view;
    }

    protected MultiView newMultiView() {
        return new MultiView();
    }

    private boolean isValidSubviewField(Field field) {
        return field.isVisible() && field.getComponentClass() != null && field.getComponentClass().equals(Viewer.class);
    }

    private View loadSubview(Field field, MultiView parentView) {
        if (!field.getParams().containsKey("viewType")) {
            throw new ViewRendererException("No viewType found for subview");
        }

        if (!field.getParams().containsKey("beanClass")) {
            throw new ViewRendererException("No beanClass found for subview");
        }
        String viewType = field.getParams().get("viewType").toString();
        Class beanClass = null;
        try {
            beanClass = Class.forName(field.getParams().get("beanClass").toString());
        } catch (ClassNotFoundException e) {
            throw new ViewRendererException();
        }
        Viewer viewer = new Viewer(viewType, beanClass);
        viewer.setValue(parentView.getValue());

        return viewer.getView();
    }
}
