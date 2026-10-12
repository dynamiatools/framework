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
package tools.dynamia.zk.ui;

import org.zkoss.zhtml.H3;
import org.zkoss.zhtml.Text;
import org.zkoss.zul.Div;
import org.zkoss.zul.Window;
import tools.dynamia.actions.FastAction;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.ui.FormOptions;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.ui.ViewDialog;
import tools.dynamia.ui.ViewOptions;
import tools.dynamia.ui.ViewsProvider;
import tools.dynamia.web.util.HttpUtils;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.zk.util.ZKUtil;
import tools.dynamia.zk.viewers.form.FormFieldComponent;
import tools.dynamia.zk.viewers.form.FormView;
import tools.dynamia.zk.viewers.ui.Viewer;

import java.util.Collection;
import java.util.function.BiConsumer;

/**
 * ZK implementation of {@link ViewsProvider}: a window with a {@link Viewer} of the bean and one action that submits.
 * The window stays open until the submit handler closes it, and a {@link ValidationError} thrown by the handler is shown
 * to the user instead of closing it.
 */
public class ZKViewsProvider implements ViewsProvider {

    @Override
    public <T> void showForm(FormOptions<T> options, BiConsumer<T, ViewDialog> onSubmit) {
        Viewer viewer = new Viewer(options.viewName(), options.beanClass(), options.value());
        if (HttpUtils.isSmartphone()) {
            viewer.setVflex("1");
            viewer.setContentVflex("0");
        }

        Window[] window = new Window[1];
        ViewDialog dialog = () -> {
            if (window[0] != null) {
                window[0].detach();
            }
        };

        String label = options.submitLabel() != null ? options.submitLabel() : "OK";
        viewer.addAction(new FastAction(label, e -> {
            try {
                onSubmit.accept(options.value(), dialog);
            } catch (ValidationError ex) {
                UIMessages.showMessage(ex.getMessage(), MessageType.WARNING);
            }
        }));

        window[0] = options.width() != null
                ? ZKUtil.showDialog(options.title(), viewer, options.width(), null)
                : ZKUtil.showDialog(options.title(), viewer);
    }

    /**
     * A read only form of the bean in a window. Inline collections (fields rendered with the {@code crudview} component)
     * are shown as tables below the form instead of inside it, and the window is sized from the number of fields unless
     * the options or the view descriptor ({@code viewWidth}, {@code viewHeight}) say otherwise.
     */
    @Override
    public <T> void showView(ViewOptions<T> options) {
        showView(options, null);
    }

    @Override
    public <T> void showView(ViewOptions<T> options, tools.dynamia.commons.Callback onClose) {
        Object entity = options.value();

        Div content = new Div();
        content.setStyle("overflow: auto");
        content.setSclass("view-data-content");
        if (HttpUtils.isSmartphone()) {
            content.setVflex("1");
        }

        FormView formView = (FormView) Viewers.getView(options.beanClass(), options.viewName(), entity);
        formView.setAutoheight(false);
        formView.setReadonly(true);
        content.appendChild(formView);

        ViewDescriptor viewDescriptor = formView.getViewDescriptor();

        viewDescriptor.getFields().stream()
                .filter(f -> "crudview".equals(f.getComponent()))
                .filter(f -> f.getParams().get(Viewers.PARAM_INPLACE) == Boolean.TRUE)
                .forEach(f -> {
                    FormFieldComponent formField = formView.getFieldComponent(f.getName());
                    if (formField != null) {
                        formField.hide();
                    }
                });

        Viewers.getFields(viewDescriptor).stream()
                .filter(Field::isCollection)
                .filter(f -> "crudview".equals(f.getComponent()))
                .forEach(f -> {
                    Collection subviewValue = (Collection) ObjectOperations.invokeGetMethod(entity, f.getPropertyInfo());
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
        if (options.width() != null) {
            width = options.width();
        }
        if (options.height() != null) {
            height = options.height();
        }

        Window window = ZKUtil.showDialog(options.title(), content, width, height);
        if (onClose != null) {
            window.addEventListener(org.zkoss.zk.ui.event.Events.ON_CLOSE, event -> onClose.doSomething());
        }
    }
}
