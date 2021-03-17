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

package tools.dynamia.zk.crud.ui;

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.A;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.Callback;
import tools.dynamia.commons.Messages;
import tools.dynamia.integration.Containers;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.crud.actions.ViewDataAction;

public class EntityLink extends A {

    static {
        BindingComponentIndex.getInstance().put("entity", EntityLink.class);
        ComponentAliasIndex.getInstance().add("entitylink", EntityLink.class);
    }

    private Object entity;
    private String labelField;
    private Callback onClickCallback = () -> {
        ViewDataAction viewDataAction = Containers.get().findObject(ViewDataAction.class);
        if (viewDataAction != null) {
            viewDataAction.view(entity);
        }
    };

    public EntityLink() {
        this(null);
    }


    public EntityLink(Object entity) {
        this.entity = entity;
        renderLabel();
        initEvents();
        String tooltiptext = Messages.get(ViewDataAction.class, "viewData");
        setTooltiptext(tooltiptext);
    }

    private void initEvents() {
        addEventListener(Events.ON_CLICK, e -> {
            if (onClickCallback != null) {
                onClickCallback.doSomething();
            }
        });
    }

    private void renderLabel() {
        if (entity != null) {
            if (labelField != null) {
                setLabel(String.valueOf(BeanUtils.getFieldValue(labelField, entity)));
            } else {
                setLabel(entity.toString());
            }
        } else {
            setLabel("");
        }
    }

    public void onClick(Callback callback) {
        this.onClickCallback = callback;
    }

    public String getLabelField() {
        return labelField;
    }

    public void setLabelField(String labelField) {
        this.labelField = labelField;
        renderLabel();
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
        renderLabel();
    }
}
