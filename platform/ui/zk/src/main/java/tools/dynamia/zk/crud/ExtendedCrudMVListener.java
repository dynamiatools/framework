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

import tools.dynamia.domain.AbstractEntity;
import tools.dynamia.viewers.View;
import tools.dynamia.zk.viewers.mv.MultiView;

public class ExtendedCrudMVListener extends AbstractExtendedMVListener {

    @Override
    public void subviewLoaded(final MultiView parentView, View subview) {
        if (subview instanceof CrudView crudView) {
            //noinspection unchecked
            crudView.getController().getDefaultEntityValues().put(beanProperty, parentView.getValue());
        }
    }

    @Override
    public void subviewSelected(MultiView parentView, View subview) {
        if (subview instanceof CrudView crudView) {
            if (parentView.getValue() != null && parentView.getValue() instanceof AbstractEntity) {
                crudView.getController().setParemeter(beanProperty, parentView.getValue());
                crudView.getController().doQuery();
            }

        }

    }

}
