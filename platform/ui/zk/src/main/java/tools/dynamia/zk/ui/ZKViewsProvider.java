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

import org.zkoss.zul.Window;
import tools.dynamia.actions.FastAction;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.ui.FormOptions;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.ui.ViewDialog;
import tools.dynamia.ui.ViewsProvider;
import tools.dynamia.web.util.HttpUtils;
import tools.dynamia.zk.util.ZKUtil;
import tools.dynamia.zk.viewers.ui.Viewer;

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
}
