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
package tools.dynamia.zk.crud.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zhtml.H3;
import org.zkoss.zhtml.Text;
import org.zkoss.zul.Div;
import tools.dynamia.actions.ActionGroup;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.actions.ReadableOnly;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.Messages;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.crud.CrudState;
import tools.dynamia.domain.LazyLoadable;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.web.util.HttpUtils;
import tools.dynamia.zk.util.ZKUtil;
import tools.dynamia.zk.viewers.form.FormFieldComponent;
import tools.dynamia.zk.viewers.form.FormView;
import tools.dynamia.zk.viewers.ui.Viewer;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author Mario A. Serrano Leones
 */
@InstallAction
public class ViewDataAction extends AbstractCrudAction implements ReadableOnly {


    @Autowired
    private CrudService crudService;

    public ViewDataAction() {
        setName(Messages.get(ViewDataAction.class, "viewData"));
        setImage("info");
        setGroup(ActionGroup.get("CRUD"));
        setPosition(0);
        setMenuSupported(true);
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {

        Object data = evt.getData();
        Serializable id = DomainUtils.findEntityId(data);
        if (id != null) {
            data = crudService.find(data.getClass(), id);
        }

        view(data);


    }

    public void view(Object data) {
        if (data != null) {
            final Object entity = data;

            if (entity instanceof LazyLoadable) {
                ((LazyLoadable) entity).lazyLoad();
            }

            Div content = new Div();
            content.setStyle("overflow: auto");
            if (HttpUtils.isSmartphone()) {
                content.setVflex("1");
            }


            Viewer viewer = new Viewer("form", entity.getClass(), entity);
            viewer.setVflex(null);
            viewer.setContentVflex(null);
            viewer.setReadonly(true);
            content.appendChild(viewer);

            FormView formView = (FormView) viewer.getView();
            formView.getViewDescriptor().getFields().stream()
                    .filter(f -> "crudview".equals(f.getComponent()))
                    .filter(f -> f.getParams().get(Viewers.PARAM_INPLACE) == Boolean.TRUE)
                    .forEach(f -> {
                        FormFieldComponent formField = formView.getFieldComponent(f.getName());
                        if (formField != null) {
                            formField.hide();
                        }
                    });

            ViewDescriptor viewDescriptor = viewer.getView().getViewDescriptor();

            Viewers.getFields(viewDescriptor).stream()
                    .filter(Field::isCollection)
                    .filter(f -> "crudview".equals(f.getComponent()))
                    .forEach(f -> {

                        Collection subviewValue = (Collection) BeanUtils.invokeGetMethod(entity, f.getPropertyInfo());
                        if (subviewValue != null && !subviewValue.isEmpty()) {
                            Viewer subview = new Viewer("table", f.getPropertyInfo().getGenericType(), subviewValue);
                            subview.setContentVflex(null);
                            subview.setContentStyle("height: 300px");
                            subview.setVflex(null);
                            subview.setReadonly(true);
                            H3 subviewTitle = new H3();
                            subviewTitle.setSclass("header-title text-primary");
                            subviewTitle.appendChild(new Text(f.getLabel()));
                            content.appendChild(subviewTitle);
                            content.appendChild(subview);
                        }
                    });

            String width = "80%";
            String height = "70%";

            int fieldCount = Viewers.getFields(viewDescriptor).size();
            if (fieldCount <= 5) {
                width = "50%";
                height = null;
            } else if (fieldCount <= 10) {
                width = "60%";
                height = null;
            } else if (fieldCount > 20) {
                width = "90%";
                height = "90%";
            }

            if (viewDescriptor.getParams().containsKey("viewWidth")) {
                width = viewDescriptor.getParams().get("viewWidth").toString();
            }

            if (viewDescriptor.getParams().containsKey("viewHeight")) {
                height = viewDescriptor.getParams().get("viewHeight").toString();
            }


            ZKUtil.showDialog(data.toString(), content, width, height);
        } else {
            UIMessages.showMessage(Messages.get(ViewDataAction.class, "select_row"), MessageType.ERROR);
        }
    }

    @Override
    public CrudState[] getApplicableStates() {
        return CrudState.get(CrudState.READ);
    }

}
